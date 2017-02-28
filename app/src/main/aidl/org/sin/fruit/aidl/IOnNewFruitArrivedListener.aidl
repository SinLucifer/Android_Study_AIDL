// IOnNewFruitArrivedListerner.aidl
package org.sin.fruit.aidl;
import org.sin.fruit.aidl.Fruit;

interface IOnNewFruitArrivedListener {
    void onNewFruitArrived(in Fruit fruit);
}
