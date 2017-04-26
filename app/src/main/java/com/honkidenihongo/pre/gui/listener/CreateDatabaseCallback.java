package com.honkidenihongo.pre.gui.listener;

import io.realm.RealmAsyncTask;

/**
 * Created by datpt on 7/14/16.
 */
public interface CreateDatabaseCallback {

    void onStartCreated();

    void onComplete(RealmAsyncTask transaction);
}
