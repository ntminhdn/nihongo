package com.honkidenihongo.pre.common.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.model.UserModel;

/**
 * Util class related to the UserModel.
 *
 * @author long.tt.
 * @since 17-Nov-2016.
 */
public class LocalUserInfoUtil {
    /**
     * The private constructor to prevent creating object.
     */
    private LocalUserInfoUtil() {
    }

    /**
     * Save asynchronous the UserModel to the SharedPreferences.
     * Chú ý: đây là hàm void, lưu 1 cách bất đồng bộ ở background.
     *
     * @param context  The context.
     * @param userModel The UserModel.
     */
    public static void saveUserInfo(Context context, UserModel userModel) {
        String sharedPreferenceName = AppConfig.SharedPreferencesKey.USER_INFO_PREFIX_NAME + userModel.id;
        SharedPreferences userSP = context.getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = userSP.edit();
        editor.putLong(AppConfig.SharedPreferencesKey.USER_INFO_ID, userModel.id);

        // Save asynchronous.
        editor.apply();
    }

    /**
     * Lấy Access-Token từ lần đăng nhập gần nhất.
     *
     * @param context
     * @return
     */
    public static String getLastAccessTokenFromLocal(Context context, long userId) {
        // Lấy tên SharedPreference theo UserId.
        String userSPName = AppConfig.SharedPreferencesKey.USER_INFO_PREFIX_NAME + userId;
        SharedPreferences prefs = context.getSharedPreferences(userSPName, Context.MODE_PRIVATE);

        return prefs.getString(Definition.SharedPreferencesKey.ACCESS_TOKEN, "");
    }

}
