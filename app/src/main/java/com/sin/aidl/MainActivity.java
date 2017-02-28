package com.sin.aidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.sin.fruit.aidl.Fruit;
import org.sin.fruit.aidl.IFruitManager;
import org.sin.fruit.aidl.IOnNewFruitArrivedListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    public static final int NEW_FRUIT_ARRIVED = 1;

    private IFruitManager iFruitManager;
    private Intent intent;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iFruitManager = IFruitManager.Stub.asInterface(service);

            try{
                service.linkToDeath(deathRecipient,0);
                List<Fruit> fruitList = iFruitManager.getFruits();

                iFruitManager.registerListener(listener);

                for (Fruit f:fruitList) {
                    Log.i(TAG, "onServiceConnected: " + f.getName());
                }

                iFruitManager.addFruit(new Fruit("pear",6));

                fruitList = iFruitManager.getFruits();

                for (Fruit f:fruitList) {
                    Log.i(TAG, "onServiceConnected: " + f.getName());
                }

            }catch (RemoteException e){
                Log.e(TAG, "onServiceConnected: ", e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iFruitManager = null;
        }
    };

    private IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.i(TAG, "Service dead and try to reconnect");
            if (iFruitManager == null)
                return;

            iFruitManager.asBinder().unlinkToDeath(deathRecipient,0);
            iFruitManager = null;

            bindService(intent,mConnection,Context.BIND_AUTO_CREATE);
        }
    };

    private IOnNewFruitArrivedListener listener = new IOnNewFruitArrivedListener.Stub() {
        @Override
        public void onNewFruitArrived(Fruit fruit) throws RemoteException {
            mHandler.obtainMessage(NEW_FRUIT_ARRIVED,fruit)
                    .sendToTarget();
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case NEW_FRUIT_ARRIVED:
                    Log.i(TAG, "New Fruit Coming: " + ((Fruit)msg.obj).getName());
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bn = (Button)findViewById(R.id.bn_start);
        bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MainActivity.this,FruitService.class);
                bindService(intent,mConnection, Context.BIND_AUTO_CREATE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (iFruitManager != null && iFruitManager.asBinder().isBinderAlive()){
            try{
                iFruitManager.unRegisterListener(listener);
            }catch (RemoteException e){
                Log.e(TAG, "unBindListener: ", e);
            }
        }
        unbindService(mConnection);
        super.onDestroy();
    }
}
