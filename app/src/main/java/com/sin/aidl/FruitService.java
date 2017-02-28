package com.sin.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import org.sin.fruit.aidl.Fruit;
import org.sin.fruit.aidl.IFruitManager;
import org.sin.fruit.aidl.IOnNewFruitArrivedListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Sin on 2017/2/28.
 */

public class FruitService extends Service {
    public static final String TAG = "FruitService";

    private AtomicBoolean isServiceDestory = new AtomicBoolean(false);

    private CopyOnWriteArrayList<Fruit> fruits = new CopyOnWriteArrayList<>();

    private RemoteCallbackList<IOnNewFruitArrivedListener> mListenerList =
            new RemoteCallbackList<>();

    private Binder mBinder = new IFruitManager.Stub(){
        @Override
        public void addFruit(Fruit fruit) throws RemoteException {
            fruits.add(fruit);
        }

        @Override
        public List<Fruit> getFruits() throws RemoteException {
            return fruits;
        }

        @Override
        public void registerListener(IOnNewFruitArrivedListener listener) throws RemoteException {
            mListenerList.register(listener);
        }

        @Override
        public void unRegisterListener(IOnNewFruitArrivedListener listener) throws RemoteException {
            mListenerList.unregister(listener);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fruit apple = new Fruit("apple",3);
        Fruit banana = new Fruit("banana",4);
        Fruit qq = new Fruit("qq",5);

        fruits.add(apple);
        fruits.add(banana);
        fruits.add(qq);

        new Thread(new ProduceFruit()).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceDestory.set(true);
    }

    private void onNewFruitArrived(Fruit fruit) throws RemoteException{
        fruits.add(fruit);
        final int N = mListenerList.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IOnNewFruitArrivedListener l = mListenerList.getBroadcastItem(i);
            if (l != null){
                try {
                    l.onNewFruitArrived(fruit);
                }catch (RemoteException e){
                    Log.e(TAG, "onNewFruitArrived: ", e);
                }
            }
        }
        mListenerList.finishBroadcast();
    }

    private class ProduceFruit implements Runnable{
        @Override
        public void run() {
            while(!isServiceDestory.get()){
                try{
                    Thread.sleep(5000);
                }catch (InterruptedException e){
                    Log.e(TAG, "run: ", e);
                }

                Fruit f = new Fruit("fruit"+(fruits.size()+1),fruits.size()+1);

                try{
                    onNewFruitArrived(f);
                }catch (RemoteException e){
                    Log.e(TAG, "run: ", e);
                }
            }
        }
    }
}
