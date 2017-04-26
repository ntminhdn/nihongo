package com.honkidenihongo.pre.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.honkidenihongo.pre.MainApplication;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.dac.PrimaryKeyFactory;
import com.honkidenihongo.pre.model.TimeLog;

import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;

/**
 * Created by datpt on 8/8/16.
 */
public class UsedAppLogReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = UsedAppLogReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        final Realm realm = Realm.getDefaultInstance();

        if (intent.getAction().equalsIgnoreCase(Definition.LogReceiverConstant.ACTION_START_LOG)) {
            Log.e(LOG_TAG, "----------Save Log Start----------");
            final long logID = PrimaryKeyFactory.getInstance().nextKey(TimeLog.class);
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    TimeLog timeLog = realm.createObject(TimeLog.class);
                    timeLog.id = logID;
                    timeLog.start = new Date();
                    timeLog.is_send = false;
                    timeLog.type = Definition.Constants.TYPE_USED_APP;
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    MainApplication.isSaveLog = true;
                    Log.e(LOG_TAG, "Saving..." + MainApplication.isSaveLog);
                    realm.close();
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    error.printStackTrace();
                    Log.e(LOG_TAG, "Saved Error!");
                    realm.close();
                }
            });
        } else {
            Log.e(LOG_TAG, "----------Save Log End----------");
            Number number = realm.where(TimeLog.class)
                    .equalTo(Definition.Database.FIELD_TYPE, Definition.Constants.TYPE_USED_APP)
                    .max(Definition.Database.FIELD_ID);
            if (number != null) {
                final long maxID = number.longValue();
                final Calendar currentTime = Calendar.getInstance();
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        TimeLog timeLog = realm.where(TimeLog.class)
                                .equalTo(Definition.Database.FIELD_ID, maxID)
                                .findFirst();
                        Date startDate = timeLog.getStart();
                        Calendar startTime = Calendar.getInstance();
                        startTime.setTime(startDate);
                        if (startTime.get(Calendar.DAY_OF_MONTH) == currentTime.get(Calendar.DAY_OF_MONTH)) {
                            timeLog.setEnd(currentTime.getTime());
                        } else {
                            // update end in case end of previous day
                            Calendar endTime = Calendar.getInstance();
                            endTime.set(startTime.get(Calendar.YEAR), startTime.get(Calendar.MONTH), startTime.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
                            timeLog.setEnd(endTime.getTime());

                            // create new log in case start of current day
                            Calendar newStartTime = Calendar.getInstance();
                            newStartTime.set(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                            TimeLog newTimeLog = realm.createObject(TimeLog.class);
                            newTimeLog.id = PrimaryKeyFactory.getInstance().nextKey(TimeLog.class);
                            newTimeLog.start = newStartTime.getTime();
                            newTimeLog.end = currentTime.getTime();
                            newTimeLog.course_id = timeLog.getCourse_id();
                            newTimeLog.unit_id = timeLog.getUnit_id();
                            newTimeLog.type = timeLog.getType();
                            newTimeLog.is_send = false;
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        Log.e(LOG_TAG, "Saved..." + MainApplication.isSaveLog);
                        MainApplication.isSaveLog = false;
                        realm.close();
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        realm.close();
                    }
                });
            }
        }
    }
}
