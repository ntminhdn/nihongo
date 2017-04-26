package com.honkidenihongo.pre.common.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.gui.S00SplashActivity;

/**
 * Class handler processing login user.
 *
 * @author BinhDT.
 */
public class LoginUtil {

    /**
     * Default constructor.
     */
    public LoginUtil() {
    }

    /**
     * Method save value of version code app current.
     *
     * @param context Value context of screen current.
     */
    public static void saveVersionCodeCurrent(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Definition.SettingApp.SETTING_APPLICATION, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Definition.SettingApp.APP_CODE_VERSION, AppConfig.getVersionCode());
        editor.apply();
    }

    /**
     * Method save value version code of app current.
     *
     * @param context Value context of screen current.
     * @return Value version code save data.
     */
    public static int getVersionCodeCurrent(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Definition.SettingApp.SETTING_APPLICATION, Context.MODE_PRIVATE);
        return prefs.getInt(Definition.SettingApp.APP_CODE_VERSION, 0);
    }
}
