package com.honkidenihongo.pre.service.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.model.CoinLog;
import com.honkidenihongo.pre.model.ExamLog;
import com.honkidenihongo.pre.model.PracticeCoin;
import com.honkidenihongo.pre.model.TimeLog;
import com.honkidenihongo.pre.model.UnitDataProgressLog;
import com.honkidenihongo.pre.model.UnitProgressLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by datpt on 9/6/16.
 */
public class SendLogReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = SendLogReceiver.class.getSimpleName();

    private SharedPreferences mSharedPreferences;
    private Realm mRealm;

    @Override
    public void onReceive(Context context, Intent intent) {
        mSharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        mRealm = Realm.getDefaultInstance();
        handleSendLog(context);

        // Setup alarm to refresh token when it is expire
        Intent alarmIntent = new Intent(Definition.Constants.ACTION_SEND_LOG);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, Definition.Constants.REQ_SEND_LOG, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long alarmDelay = 5 * 60 * 1000;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        alarmDelay, alarmPendingIntent);
    }

    // Handle send all log
    private void handleSendLog(Context context) {
        // Send Online Log
        RealmResults<TimeLog> listOnlineLog = mRealm.where(TimeLog.class)
                .beginGroup()
                .equalTo(Definition.Database.FIELD_TYPE, Definition.Constants.TYPE_USED_APP)
                .equalTo(Definition.Database.FIELD_IS_SEND, false)
                .isNotNull(Definition.Database.FIELD_END)
                .endGroup().findAll();

        // Send Knowledge Log
        RealmResults<TimeLog> listKnowledgeLog = mRealm.where(TimeLog.class)
                .beginGroup()
                .equalTo(Definition.Database.FIELD_TYPE, Definition.Constants.TYPE_IMPROVE_KNOWLEDGE)
                .equalTo(Definition.Database.FIELD_IS_SEND, false)
                .isNotNull(Definition.Database.FIELD_END)
                .endGroup().findAll();

        // Send Practice Log
        RealmResults<ExamLog> listPracticeLog = mRealm.where(ExamLog.class)
                .beginGroup()
                .equalTo(Definition.Database.FIELD_TYPE, Definition.Constants.TYPE_PRACTICE)
                .equalTo(Definition.Database.FIELD_IS_SEND, false)
                .endGroup().findAll();

        // Send Test Log
        RealmResults<ExamLog> listTestLog = mRealm.where(ExamLog.class)
                .beginGroup()
                .equalTo(Definition.Database.FIELD_TYPE, Definition.Constants.TYPE_TEST)
                .equalTo(Definition.Database.FIELD_IS_SEND, false)
                .endGroup().findAll();

        // Send Unit-Content Progress Log
        RealmResults<UnitDataProgressLog> listUnitDataProgressLog = mRealm.where(UnitDataProgressLog.class)
                .equalTo(Definition.Database.FIELD_IS_SEND, false)
                .findAll();

        // Send Unit Progress Log
        RealmResults<UnitProgressLog> listUnitProgressLog = mRealm.where(UnitProgressLog.class)
                .equalTo(Definition.Database.FIELD_IS_SEND, false)
                .findAll();

        // Send Unit Progress Log
        RealmResults<PracticeCoin> listPracticeCoin = mRealm.where(PracticeCoin.class)
                .equalTo(Definition.Database.FIELD_IS_SEND, false)
                .findAll();

        // Send Coin Log
        RealmResults<CoinLog> listCoinLogs = mRealm.where(CoinLog.class)
                .equalTo(Definition.Database.FIELD_IS_SEND, false)
                .findAll();

        requestSendTimeLog(context, Definition.Constants.TYPE_USED_APP, listOnlineLog);
        requestSendTimeLog(context, Definition.Constants.TYPE_IMPROVE_KNOWLEDGE, listKnowledgeLog);
        requestSendHistory(context, Definition.Constants.TYPE_PRACTICE, listPracticeLog);
        requestSendHistory(context, Definition.Constants.TYPE_TEST, listTestLog);
        requestSendUnitProgress(context, listUnitProgressLog);
        requestSendUnitDataProgress(context, listUnitDataProgressLog);
        requestSendPracticeCoin(context, listPracticeCoin);
        requestSendCointLog(context, listCoinLogs);
    }

    // Request send TimeLog to server (contain KnowledgeLog, UsedAppLog)
    private void requestSendTimeLog(final Context context, final String typeLog, RealmResults<TimeLog> listTimeLog) {
        if (listTimeLog == null || listTimeLog.size() == 0) {
            return;
        }
        String sendLogUrl = AppConfig.getApiBaseUrl() + Definition.API.LOG;
        Log.d(LOG_TAG, "TimeLogURL: " + sendLogUrl);
        Log.d(LOG_TAG, "AccessToken: " + mSharedPreferences.getString(Definition.SharedPreferencesKey.ACCESS_TOKEN, ""));

        JSONArray jsonArray = new JSONArray();
        for (TimeLog timeLog : listTimeLog) {
            jsonArray.put(timeLog.toJsonObject());
        }

        Log.d(LOG_TAG, "TimeLogType: " + typeLog);
        Log.d(LOG_TAG, "TimeLogData: " + jsonArray.toString());

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_TYPE, typeLog)
                .add(Definition.Request.PARAM_DATA, jsonArray.toString())
                .build();

        Request request = new Request.Builder()
                .header(Definition.Request.HEADER_AUTHORIZATION,
                        Definition.Request.HEADER_BEARER + mSharedPreferences.getString(Definition.SharedPreferencesKey.ACCESS_TOKEN, ""))
                .url(sendLogUrl)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(LOG_TAG, "TimeLog onFailure()");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Read data on the worker thread
                final String responseData = response.body().string();
                Log.d(LOG_TAG, "TimeLog onResponse()");
                Log.d(LOG_TAG, "TimeLogResponse: " + responseData);
                try {
                    JSONObject responseJsonObject = new JSONObject(responseData);
                    boolean isSuccess = responseJsonObject.getBoolean(Definition.Response.SUCCESS);
                    if (isSuccess) {
                        Handler mainHandler = new Handler(context.getMainLooper());
                        Runnable mainRunnable = new Runnable() {
                            @Override
                            public void run() {
                                mRealm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        RealmResults<TimeLog> listLog = realm.where(TimeLog.class)
                                                .beginGroup()
                                                .equalTo(Definition.Database.FIELD_TYPE, typeLog)
                                                .equalTo(Definition.Database.FIELD_IS_SEND, false)
                                                .isNotNull(Definition.Database.FIELD_END)
                                                .endGroup().findAll();
                                        for (TimeLog log : listLog) {
                                            log.setIs_send(true);
                                        }
                                    }
                                });
                            }
                        };
                        mainHandler.post(mainRunnable);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Send practice/test logs to server
    private void requestSendHistory(final Context context, final String typeLog, RealmResults<ExamLog> listExamLog) {
        if (listExamLog == null || listExamLog.size() == 0) {
            return;
        }
        String sendLogUrl = AppConfig.getApiBaseUrl() + Definition.API.LOG;
        Log.d(LOG_TAG, "ExamLogURL: " + sendLogUrl);
        Log.d(LOG_TAG, "AccessToken: " + mSharedPreferences.getString(Definition.SharedPreferencesKey.ACCESS_TOKEN, ""));

        JSONArray jsonArray = new JSONArray();
        for (ExamLog examLog : listExamLog) {
            jsonArray.put(examLog.toJsonObject());
        }

        Log.d(LOG_TAG, "ExamLogType: " + typeLog);
        Log.d(LOG_TAG, "ExamLogData: " + jsonArray.toString());

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_TYPE, typeLog)
                .add(Definition.Request.PARAM_DATA, jsonArray.toString())
                .build();

        Request request = new Request.Builder()
                .header(Definition.Request.HEADER_AUTHORIZATION,
                        Definition.Request.HEADER_BEARER + mSharedPreferences.getString(Definition.SharedPreferencesKey.ACCESS_TOKEN, ""))
                .url(sendLogUrl)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(LOG_TAG, "ExamLog onFailure()");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Read data on the worker thread
                final String responseData = response.body().string();
                Log.d(LOG_TAG, "ExamLog onResponse()");
                Log.d(LOG_TAG, "ExamLogResponse: " + responseData);
                try {
                    JSONObject responseJsonObject = new JSONObject(responseData);
                    boolean isSuccess = responseJsonObject.getBoolean(Definition.Response.SUCCESS);
                    if (isSuccess) {
                        Handler mainHandler = new Handler(context.getMainLooper());
                        Runnable mainRunnable = new Runnable() {
                            @Override
                            public void run() {
                                mRealm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        RealmResults<ExamLog> listLog = realm.where(ExamLog.class)
                                                .beginGroup()
                                                .equalTo(Definition.Database.FIELD_TYPE, typeLog)
                                                .equalTo(Definition.Database.FIELD_IS_SEND, false)
                                                .endGroup().findAll();
                                        for (ExamLog log : listLog) {
                                            log.setIs_send(true);
                                        }
                                    }
                                });
                            }
                        };
                        mainHandler.post(mainRunnable);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Send progress logs of unit to server
    private void requestSendUnitProgress(final Context context, RealmResults<UnitProgressLog> listUnitProgressLogs) {
        if (listUnitProgressLogs == null || listUnitProgressLogs.size() == 0) {
            return;
        }
        String sendLogUrl = AppConfig.getApiBaseUrl() + Definition.API.LOG_PROGRESS;
        Log.d(LOG_TAG, "UnitProgressLogURL: " + sendLogUrl);
        Log.d(LOG_TAG, "AccessToken: " + mSharedPreferences.getString(Definition.SharedPreferencesKey.ACCESS_TOKEN, ""));

        JSONArray jsonArray = new JSONArray();
        for (UnitProgressLog unitProgressLog : listUnitProgressLogs) {
            jsonArray.put(unitProgressLog.toJSONObject());
        }

        Log.d(LOG_TAG, "UnitProgressLogData: " + jsonArray.toString());

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_TYPE, Definition.Constants.TYPE_UNIT)
                .add(Definition.Request.PARAM_DATA, jsonArray.toString())
                .build();

        Request request = new Request.Builder()
                .header(Definition.Request.HEADER_AUTHORIZATION,
                        Definition.Request.HEADER_BEARER + mSharedPreferences.getString(Definition.SharedPreferencesKey.ACCESS_TOKEN, ""))
                .url(sendLogUrl)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(LOG_TAG, "UnitProgress onFailure()");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Read data on the worker thread
                final String responseData = response.body().string();
                Log.d(LOG_TAG, "UnitProgress onResponse()");
                Log.d(LOG_TAG, "UnitProgressResponse: " + responseData);
                try {
                    JSONObject responseJsonObject = new JSONObject(responseData);
                    boolean isSuccess = responseJsonObject.getBoolean(Definition.Response.SUCCESS);
                    if (isSuccess) {
                        Handler mainHandler = new Handler(context.getMainLooper());
                        Runnable mainRunnable = new Runnable() {
                            @Override
                            public void run() {
                                mRealm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        RealmResults<UnitProgressLog> listLog = realm.where(UnitProgressLog.class)
                                                .beginGroup()
                                                .equalTo(Definition.Database.FIELD_IS_SEND, false)
                                                .endGroup().findAll();
                                        for (UnitProgressLog log : listLog) {
                                            log.setIs_send(true);
                                        }
                                    }
                                });
                            }
                        };
                        mainHandler.post(mainRunnable);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Send progress logs of unit data to server
    private void requestSendUnitDataProgress(final Context context, RealmResults<UnitDataProgressLog> listUnitDataProgressLogs) {
        if (listUnitDataProgressLogs == null || listUnitDataProgressLogs.size() == 0) {
            return;
        }
        String sendLogUrl = AppConfig.getApiBaseUrl() + Definition.API.LOG_PROGRESS;
        Log.d(LOG_TAG, "UnitDataProgressLogURL: " + sendLogUrl);
        Log.d(LOG_TAG, "AccessToken: " + mSharedPreferences.getString(Definition.SharedPreferencesKey.ACCESS_TOKEN, ""));

        JSONArray jsonArray = new JSONArray();
        for (UnitDataProgressLog unitDataProgressLog : listUnitDataProgressLogs) {
            jsonArray.put(unitDataProgressLog.toJSONObject());
        }

        Log.d(LOG_TAG, "UnitDataProgressLogData: " + jsonArray.toString());

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_TYPE, Definition.Constants.TYPE_UNIT_CONTENT)
                .add(Definition.Request.PARAM_DATA, jsonArray.toString())
                .build();

        Request request = new Request.Builder()
                .header(Definition.Request.HEADER_AUTHORIZATION,
                        Definition.Request.HEADER_BEARER + mSharedPreferences.getString(Definition.SharedPreferencesKey.ACCESS_TOKEN, ""))
                .url(sendLogUrl)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(LOG_TAG, "UnitDataProgress onFailure()");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Read data on the worker thread
                final String responseData = response.body().string();
                Log.d(LOG_TAG, "UnitDataProgress onResponse()");
                Log.d(LOG_TAG, "UnitDataProgressResponse: " + responseData);
                try {
                    JSONObject responseJsonObject = new JSONObject(responseData);
                    boolean isSuccess = responseJsonObject.getBoolean(Definition.Response.SUCCESS);
                    if (isSuccess) {
                        Handler mainHandler = new Handler(context.getMainLooper());
                        Runnable mainRunnable = new Runnable() {
                            @Override
                            public void run() {
                                mRealm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        RealmResults<UnitDataProgressLog> listLog = realm.where(UnitDataProgressLog.class)
                                                .beginGroup()
                                                .equalTo(Definition.Database.FIELD_IS_SEND, false)
                                                .endGroup().findAll();
                                        for (UnitDataProgressLog log : listLog) {
                                            log.setIs_send(true);
                                        }
                                    }
                                });
                            }
                        };
                        mainHandler.post(mainRunnable);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Send practice coint to server
    private void requestSendPracticeCoin(final Context context, RealmResults<PracticeCoin> listPracticeCoins) {
        if (listPracticeCoins == null || listPracticeCoins.size() == 0) {
            return;
        }
        String sendLogUrl = AppConfig.getApiBaseUrl() + Definition.API.LOG_RANKING;
        Log.d(LOG_TAG, "PracticeCoinLogURL: " + sendLogUrl);
        Log.d(LOG_TAG, "AccessToken: " + mSharedPreferences.getString(Definition.SharedPreferencesKey.ACCESS_TOKEN, ""));

        JSONArray jsonArray = new JSONArray();
        for (PracticeCoin practiceCoin : listPracticeCoins) {
            jsonArray.put(practiceCoin.toJSONObject());
        }

        Log.d(LOG_TAG, "PracticeCoinLogData: " + jsonArray.toString());

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_TYPE, Definition.Constants.TYPE_PRACTICE_RANKING)
                .add(Definition.Request.PARAM_DATA, jsonArray.toString())
                .build();

        Request request = new Request.Builder()
                .header(Definition.Request.HEADER_AUTHORIZATION,
                        Definition.Request.HEADER_BEARER + mSharedPreferences.getString(Definition.SharedPreferencesKey.ACCESS_TOKEN, ""))
                .url(sendLogUrl)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(LOG_TAG, "PracticeCoin onFailure()");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Read data on the worker thread
                final String responseData = response.body().string();
                Log.d(LOG_TAG, "PracticeCoin onResponse()");
                Log.d(LOG_TAG, "PracticeCoinResponse: " + responseData);
                try {
                    JSONObject responseJsonObject = new JSONObject(responseData);
                    boolean isSuccess = responseJsonObject.getBoolean(Definition.Response.SUCCESS);
                    if (isSuccess) {
                        Handler mainHandler = new Handler(context.getMainLooper());
                        Runnable mainRunnable = new Runnable() {
                            @Override
                            public void run() {
                                mRealm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        RealmResults<PracticeCoin> listPracticeCoins = realm.where(PracticeCoin.class)
                                                .beginGroup()
                                                .equalTo(Definition.Database.FIELD_IS_SEND, false)
                                                .endGroup().findAll();
                                        for (PracticeCoin practiceCoin : listPracticeCoins) {
                                            practiceCoin.setIs_send(true);
                                        }
                                    }
                                });
                            }
                        };
                        mainHandler.post(mainRunnable);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Send coin log to server
    private void requestSendCointLog(final Context context, RealmResults<CoinLog> listCoinLogs) {
        if (listCoinLogs == null || listCoinLogs.size() == 0) {
            return;
        }
        String sendLogUrl = AppConfig.getApiBaseUrl() + Definition.API.LOG_COIN;
        Log.d(LOG_TAG, "CoinLogURL: " + sendLogUrl);
        Log.d(LOG_TAG, "AccessToken: " + mSharedPreferences.getString(Definition.SharedPreferencesKey.ACCESS_TOKEN, ""));

        JSONArray jsonArray = new JSONArray();
        for (CoinLog coinLog : listCoinLogs) {
            jsonArray.put(coinLog.toJSONObject());
        }

        Log.d(LOG_TAG, "CoinLogData: " + jsonArray.toString());

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_DATA, jsonArray.toString())
                .build();

        Request request = new Request.Builder()
                .header(Definition.Request.HEADER_AUTHORIZATION,
                        Definition.Request.HEADER_BEARER + mSharedPreferences.getString(Definition.SharedPreferencesKey.ACCESS_TOKEN, ""))
                .url(sendLogUrl)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(LOG_TAG, "CoinLog onFailure()");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Read data on the worker thread
                final String responseData = response.body().string();
                Log.d(LOG_TAG, "CoinLog onResponse()");
                Log.d(LOG_TAG, "CoinResponse: " + responseData);
                try {
                    JSONObject responseJsonObject = new JSONObject(responseData);
                    boolean isSuccess = responseJsonObject.getBoolean(Definition.Response.SUCCESS);
                    if (isSuccess) {
                        Handler mainHandler = new Handler(context.getMainLooper());
                        Runnable mainRunnable = new Runnable() {
                            @Override
                            public void run() {
                                mRealm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        RealmResults<CoinLog> coinLogs = realm.where(CoinLog.class)
                                                .beginGroup()
                                                .equalTo(Definition.Database.FIELD_IS_SEND, false)
                                                .endGroup().findAll();
                                        for (CoinLog coinLog : coinLogs) {
                                            coinLog.setIs_send(true);
                                        }
                                    }
                                });
                            }
                        };
                        mainHandler.post(mainRunnable);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
