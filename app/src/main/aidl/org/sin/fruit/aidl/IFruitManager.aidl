// IFruitManager.aidl
package org.sin.fruit.aidl;

import org.sin.fruit.aidl.Fruit;
import org.sin.fruit.aidl.IOnNewFruitArrivedListener;

interface IFruitManager{
    void addFruit(in Fruit fruit);
    List<Fruit> getFruits();
    void registerListener(IOnNewFruitArrivedListener listener);
    void unRegisterListener(IOnNewFruitArrivedListener listener);
}