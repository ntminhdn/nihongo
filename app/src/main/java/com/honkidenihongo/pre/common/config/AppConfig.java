package com.honkidenihongo.pre.common.config;

import com.honkidenihongo.pre.BuildConfig;

import java.util.TimeZone;

/**
 * Các cấu hình chung của toàn ứng dụng.
 *
 * @author long.tt.
 * @since 18-Nov-2016.
 */
public final class AppConfig {
    /**
     * The private constructor to prevent creating new object.
     */
    private AppConfig() {
    }

    /**
     * TimeZone ở phía Server.
     */
    public static TimeZone getServerTimezone() {
        return TimeZone.getTimeZone(BuildConfig.SERVER_TIMEZONE);
    }

    public static String getApiBaseUrl() {
        return BuildConfig.API_BASE_URL;
    }

    public static String getClientId() {
        return BuildConfig.CLIENT_ID;
    }

    public static String getClientSecret() {
        return BuildConfig.CLIENT_SECRET;
    }

    public static String getClientToken() {
        return BuildConfig.CLIENT_TOKEN;
    }

    public static long getDatabaseVersion() {
        return BuildConfig.DATABASE_VERSION;
    }

    public static int getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    public static String getAppVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * Class liên quan đến tên các cấu hình lưu ở SharedPreferences.
     */
    public final static class SharedPreferencesKey {
        /**
         * The private constructor to prevent creating new object.
         */
        private SharedPreferencesKey() {
        }

        /**
         * Tên file lưu last App Information.
         */
        public final static String FILE_LAST_APP_INFO = "last_app_info";

        /**
         * User information prefix: "u".
         */
        public final static String USER_INFO_PREFIX = "u";

        /**
         * The SharedPreference key of the last logined user id: "last_user_id".
         */
        public final static String LAST_USER_AUTH_TYPE = "last_user_auth_type";

        public final static String LAST_USER_TOKEN_ACCESS_TOKEN = "last_user_access_token";
        public final static String LAST_USER_TOKEN_REFRESH_TOKEN = "last_user_refresh_token";
        public final static String LAST_USER_TOKEN_EXPIRE = "last_user_expire";

        public final static String LAST_USER_ID = "last_user_id";
        public final static String LAST_USER_CODE = "last_user_code";
        public final static String LAST_USER_EMAIL = "last_user_email";
        public final static String LAST_USER_USERNAME = "last_user_username";
        public final static String LAST_USER_FULL_NAME = "last_user_full_name";
        public final static String LAST_USER_URL_SLUG = "last_user_url_slug";
        public final static String LAST_USER_AVATAR_URL = "last_user_avatar_url";

        public final static String LAST_USER_FACEBOOK_ID = "last_user_facebook_id";
        public final static String LAST_USER_FACEBOOK_EMAIL = "last_user_facebook_email";
        public final static String LAST_USER_FACEBOOK_USERNAME = "last_user_facebook_username";
        public final static String LAST_USER_FACEBOOK_DISPLAY_NAME = "last_user_facebook_display_name";
        public final static String LAST_USER_FACEBOOK_AVATAR_URL = "last_user_facebook_avatar_url";
        public final static String LAST_USER_FACEBOOK_CONNECTED = "last_user_facebook_connected";

        /**
         * The prefix name of SharedPreference key of User: "u".
         */
        public final static String USER_INFO_PREFIX_NAME = "u";

        /**
         * The SharedPreference key of User for the Id: "id".
         */
        public final static String USER_INFO_ID = "id";

        /**
         * The SharedPreference key of User for the Lessons List: "lessons".
         */
        public final static String USER_LESSONS = "lessons";

        /**
         * The separator of the Lessons List string: ";".
         */
        public final static String USER_LESSONS_SEPARATOR_LESSON = ";";

        /**
         * The separator of the Lessons List string in each Field: ":".
         */
        public final static String USER_LESSONS_SEPARATOR_FIELD = ":";

        /**
         * The SharedPreference key of User LanguageCode: "language".
         */
        public final static String USER_INFO_LANGUAGE = "language";
    }
}
