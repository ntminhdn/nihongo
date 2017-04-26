package com.honkidenihongo.pre.model;

import android.support.annotation.NonNull;

import com.honkidenihongo.pre.api.json.A03Me_JsonData;

/**
 * Util class related to Model classes.
 *
 * @author long.tt.
 * @since 17-Nov-2016.
 */
public class UserModelUtil {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = UserModelUtil.class.getName();

    /**
     * The private constructor to prevent creating object.
     */
    private UserModelUtil() {
    }

    /**
     * Lấy thông tin User Id gần nhất được lưu ở Local.
     *
     * @param a03Me_JsonData The Json Data object.
     * @return UserModel object.
     */
    public static UserModel makeFrom(@NonNull A03Me_JsonData a03Me_JsonData) {
        UserModel userModel = new UserModel();

        /* System Server information. */
        userModel.id = a03Me_JsonData.id;
        userModel.code = a03Me_JsonData.code;
        userModel.email = a03Me_JsonData.email;
        userModel.username = a03Me_JsonData.username;
        userModel.fullName = a03Me_JsonData.full_name;
        userModel.urlSlug = a03Me_JsonData.url_slug;
        userModel.avatarUrl = a03Me_JsonData.avatar_url;

        /* Facebook information. */
        userModel.facebookId = a03Me_JsonData.facebook_id;
        userModel.facebookEmail = a03Me_JsonData.facebook_email;
        userModel.facebookUsername = a03Me_JsonData.facebook_username;
        userModel.facebookDisplayName = a03Me_JsonData.facebook_display_name;
        userModel.facebookAvatarUrl = a03Me_JsonData.facebook_avatar_url;

        userModel.facebookConnected = a03Me_JsonData.facebook_connected;
        userModel.hasPassword = a03Me_JsonData.has_password;

        return userModel;
    }

}
