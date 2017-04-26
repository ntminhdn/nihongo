package com.honkidenihongo.pre.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.TokenUtil;
import com.honkidenihongo.pre.common.util.NetworkUtil;

/**
 * Created by datpt on 8/18/16.
 */
//public class RefreshTokenReceiver extends BroadcastReceiver {
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
//        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
//            long expires = sharedPreferences.getLong(Definition.SharedPreferencesKey.EXPIRES_IN, 0);
//            if (expires < System.currentTimeMillis()) {
//                refreshToken(context, sharedPreferences);
//            }
//        } else if (intent.getAction().equalsIgnoreCase(Definition.Constants.ACTION_REFRESH_TOKEN)) {
//            refreshToken(context, sharedPreferences);
//        }
//    }
//
//    private void refreshToken(Context context, SharedPreferences sharedPreferences) {
//        Log.e("datpt", "REFRESH TOKEN");
//        String refresh_token = sharedPreferences.getString(Definition.SharedPreferencesKey.REFRESH_TOKEN, "");
//        if (NetworkUtil.isAvailable(context)) {
//            TokenUtil.refreshToken(context, refresh_token);
//        }
//    }
//}