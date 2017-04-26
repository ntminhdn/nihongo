package com.honkidenihongo.pre.dac;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.honkidenihongo.pre.gui.listener.CreateDatabaseCallback;
import com.honkidenihongo.pre.model.ExamLog;
import com.honkidenihongo.pre.model.PracticeCoin;
import com.honkidenihongo.pre.model.TimeLog;
import com.honkidenihongo.pre.model.UnitDataProgressLog;
import com.honkidenihongo.pre.model.UnitProgressLog;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmAsyncTask;

/**
 * Created by datpt on 9/21/16.
 */
public class DatabaseStoreLogTask extends AsyncTask<Void, Void, Void> {

    private static final String LOG_TAG = DatabaseStoreLogTask.class.getSimpleName();

    private Context mContext;

    private String mDataDirectory;

    private List<UnitProgressLog> mUnitProgressLogList;
    private List<UnitDataProgressLog> mUnitDataProgressLogList;
    private List<ExamLog> mExamLogList;
    private List<TimeLog> mTimeLogList;
    private List<PracticeCoin> mPracticeCoinList;

    private Realm mRealm;
    private DatabaseParser parser;
    private CreateDatabaseCallback mCallback;
    private RealmAsyncTask mTransaction;

    public DatabaseStoreLogTask(Context context, String dataDirectory, Realm realm, CreateDatabaseCallback callback) {
        mContext = context;
        if (dataDirectory.endsWith("/")) {
            mDataDirectory = dataDirectory;
        } else {
            mDataDirectory = dataDirectory + "/";
        }
        parser = new DatabaseParser(mContext, mDataDirectory);
        mCallback = callback;
        mRealm = realm;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        mUnitProgressLogList = parser.parserUnitProgress();
        mUnitDataProgressLogList = parser.parserUnitDataProgress();
        mExamLogList = parser.parserExamLog();
        mTimeLogList = parser.parserTimeLog();
        mPracticeCoinList = parser.parserRank();
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mCallback != null) {
            mCallback.onStartCreated();
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        mTransaction = DatabaseManager.createLogDatabase(
                mRealm, mUnitProgressLogList, mUnitDataProgressLogList, mExamLogList, mTimeLogList, mPracticeCoinList,
                new DatabaseManager.ChangeDatabaseCallback() {
                    @Override
                    public void onSuccess(long id) {
                        Log.d(LOG_TAG, "StoreLog: onSuccess()");
                        if (mCallback != null) {
                            mCallback.onComplete(mTransaction);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.d(LOG_TAG, "StoreLog: onError()");
                        if (mCallback != null) {
                            mCallback.onComplete(mTransaction);
                        }
                    }
                });
        if (mTransaction == null) {
            if (mCallback != null) {
                mCallback.onComplete(mTransaction);
            }
        }
    }

}
