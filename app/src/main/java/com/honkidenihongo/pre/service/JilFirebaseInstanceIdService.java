/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.honkidenihongo.pre.service;

import android.content.SharedPreferences;
import android.util.Log;

import com.facebook.login.LoginManager;
import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class JilFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String LOG_TAG = JilFirebaseInstanceIdService.class.getSimpleName();

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refreshToken]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(LOG_TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        String access_token = sharedPreferences.getString(Definition.SharedPreferencesKey.ACCESS_TOKEN, "");
        if (access_token != null && !access_token.isEmpty()) {
            sendRegistrationToServer(access_token, refreshedToken);
        }
    }
    // [END refreshToken]

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param device_token The new token.
     */
    private void sendRegistrationToServer(String access_token, String device_token) {
        // TODO: Implement this method to send token to your app server.
        String deviceTokenUrl = AppConfig.getApiBaseUrl() + Definition.API.DEVICE_TOKEN;
        Log.d(LOG_TAG, "DeviceTokenURL: " + deviceTokenUrl);
        Log.d(LOG_TAG, "AccessToken: " + access_token);
        Log.d(LOG_TAG, "DeviceToken: " + device_token);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_DEVICE_TOKEN, device_token)
                .add(Definition.Request.PARAM_OS, Definition.Constants.ANDROID)
                .build();

        Request request = new Request.Builder()
                .header(Definition.Request.HEADER_AUTHORIZATION, Definition.Request.HEADER_BEARER + access_token)
                .url(deviceTokenUrl)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(LOG_TAG, "onFailure()");
                LoginManager.getInstance().logOut();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Read data on the worker thread
                final String responseData = response.body().string();
                Log.d(LOG_TAG, "onResponse()");
                Log.d(LOG_TAG, "Response: " + responseData);
            }
        });
    }
}