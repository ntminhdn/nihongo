package com.honkidenihongo.pre.common.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.honkidenihongo.pre.api.json.A01AccessTokenGenerator_JsonData;
import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.UserModel;
import com.honkidenihongo.pre.model.constant.LessonType;

import java.util.ArrayList;
import java.util.List;

/**
 * Util class related to common operations of the Application.
 *
 * @author long.tt.
 * @since 17-Nov-2016.
 */
public class LocalAppUtil {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = LocalAppUtil.class.getName();

    /**
     * Đối tượng lưu thông tin UserModel.
     */
//    private static UserModel userInfo;

    /**
     * The private constructor to prevent creating object.
     */
    private LocalAppUtil() {
    }

    /**
     * Lấy thông tin User Id gần nhất được lưu ở Local.
     *
     * @param context The context.
     * @return User Id if OK, otherwise return 0.
     */
    public static long getLastUserId(Context context) {
        // Get SharedPreferences AppInfo chung đã được lưu lần gần nhất.
        SharedPreferences prefs = context.getSharedPreferences(AppConfig.SharedPreferencesKey.FILE_LAST_APP_INFO, Context.MODE_PRIVATE);

        // Return the last User Id.
        return prefs.getLong(AppConfig.SharedPreferencesKey.LAST_USER_ID, 0);
    }

    /**
     * Lưu danh sách Lesson List xuống local (Share preferences) theo thứ tự mặc định của List.
     * Chú ý: Muốn lưu theo thứ tự của Lesson Number thì lúc gọi hãy sắp xếp.
     * Method này lưu theo cơ chế ở background, không cần biết kết quả.
     */
    public static void saveLastLessonList(Context context, List<Lesson> lessonList) {
        // Get last User Id.
        long userId = getLastUserId(context);

        // Nếu không lấy được UserId, tức chưa đăng nhập lần nào hoặc lỗi gì đó thì return.
        if (userId == 0) {
            return;
        }

        // Nếu không có LessonList thì return;
        if (lessonList == null || lessonList.isEmpty()) {
            return;
        }

        // Lấy tên SharedPreference theo UserId.
        String userPrefName = AppConfig.SharedPreferencesKey.USER_INFO_PREFIX_NAME + userId;
        SharedPreferences prefs = context.getSharedPreferences(userPrefName, Context.MODE_PRIVATE);

        // Tạo chuỗi thông tin Lesson List.
        StringBuilder lessonListString = new StringBuilder();

        // Nếu là phần tử đầu tiên của list thì không thêm kí tự phân biệt ";" object lesson và mảng sẽ for từ vị trí thứ 1.
        lessonListString
                .append(lessonList.get(0).getId())
                .append(AppConfig.SharedPreferencesKey.USER_LESSONS_SEPARATOR_FIELD)
                .append(lessonList.get(0).status)
                .append(AppConfig.SharedPreferencesKey.USER_LESSONS_SEPARATOR_FIELD)
                .append(lessonList.get(0).getTitle_vi())
                .append(AppConfig.SharedPreferencesKey.USER_LESSONS_SEPARATOR_FIELD)
                .append(lessonList.get(0).getTitle_en())
                .append(AppConfig.SharedPreferencesKey.USER_LESSONS_SEPARATOR_FIELD)
                .append(lessonList.get(0).getTitle_ja())
                .append(AppConfig.SharedPreferencesKey.USER_LESSONS_SEPARATOR_FIELD)
                .append(lessonList.get(0).getVersion());


        for (int i = 1; i < lessonList.size(); i++) {
            Lesson lesson = lessonList.get(i);
            lessonListString
                    .append(AppConfig.SharedPreferencesKey.USER_LESSONS_SEPARATOR_LESSON)
                    .append(lesson.getId())
                    .append(AppConfig.SharedPreferencesKey.USER_LESSONS_SEPARATOR_FIELD)
                    .append(lesson.status)
                    .append(AppConfig.SharedPreferencesKey.USER_LESSONS_SEPARATOR_FIELD)
                    .append(lessonList.get(i).getTitle_vi())
                    .append(AppConfig.SharedPreferencesKey.USER_LESSONS_SEPARATOR_FIELD)
                    .append(lessonList.get(i).getTitle_en())
                    .append(AppConfig.SharedPreferencesKey.USER_LESSONS_SEPARATOR_FIELD)
                    .append(lessonList.get(i).getTitle_ja())
                    .append(AppConfig.SharedPreferencesKey.USER_LESSONS_SEPARATOR_FIELD)
                    .append(lessonList.get(i).getVersion());
        }

        // Lưu chuỗi thông tin Lesson List.
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(AppConfig.SharedPreferencesKey.USER_LESSONS, lessonListString.toString());

        // Save in background without waiting result.
        prefsEditor.apply();
    }

    /**
     * Lấy danh sách Lesson List từ SharedPreferences đã được lưu gần nhất theo User mới login gần nhất.
     * Chú ý: Danh sách Lesson này chỉ biết được thông tin Lesson Number và Lesson Id, không biết được đã Download chưa.
     * Todo: Có thể xảy ra lỗ hổng: User mới login gần nhất không phải là User hiện tại, vì thế cần truyền User Id.
     *
     * @param context The context.
     * @return The Lesson List, that includes only Lesson Number and Lesson Id.
     */
    @Nullable
    public static List<Lesson> getLastLessonList(Context context) {
        // Get last User Id.
        long userId = getLastUserId(context);

        // Nếu không lấy được UserId, tức chưa đăng nhập lần nào hoặc lỗi gì đó thì return null.
        if (userId == 0) {
            return null;
        }

        // Lấy tên SharedPreference theo UserId.
        String userPrefName = AppConfig.SharedPreferencesKey.USER_INFO_PREFIX_NAME + userId;
        SharedPreferences prefs = context.getSharedPreferences(userPrefName, Context.MODE_PRIVATE);

        // Khởi tạo danh sách Lessons List.
        List<Lesson> lessonList = new ArrayList<>();

        // Lấy chuỗi Lessons List từ SharedPreferences.
        String lessonsString = prefs.getString(AppConfig.SharedPreferencesKey.USER_LESSONS, "");
        String[] lessonsArray = lessonsString.split(AppConfig.SharedPreferencesKey.USER_LESSONS_SEPARATOR_LESSON);

        for (int i = 0; i < lessonsArray.length; i++) {
            String lessonString = lessonsArray[i];

            // Bỏ qua chuỗi rỗng.
            if ("".equals(lessonString)) {
                continue;
            }

            try {
                // Parse LessonId and LessonStatus.
                String[] lessonInfoArray = lessonString.split(AppConfig.SharedPreferencesKey.USER_LESSONS_SEPARATOR_FIELD);

                long lessonId = Long.parseLong(lessonInfoArray[0]);
                int lessonStatus = Integer.parseInt(lessonInfoArray[1]);

                // Tạo đối tượng Lesson.
                Lesson lesson = new Lesson();
                lesson.setNumber(i);
                lesson.setId(lessonId);
                lesson.status = lessonStatus;
                lesson.setTitle_vi(lessonInfoArray.length > 2 ? lessonInfoArray[2] : "");
                lesson.setTitle_en(lessonInfoArray.length > 3 ? lessonInfoArray[3] : "");
                lesson.setTitle_ja(lessonInfoArray.length > 4 ? lessonInfoArray[4] : "");
                lesson.setVersion(lessonInfoArray.length > 5 ? Integer.parseInt(lessonInfoArray[5]) : 0);

                // Set type for lesson.
                if (i == 0) {
                    lesson.setType(LessonType.PRE_LESSON);
                } else {
                    lesson.setType(LessonType.UNIT);
                }

                lessonList.add(lesson);

            } catch (Exception ex) {
                Log.e(LOG_TAG, "" + ex.getMessage());
            }
        }

        return lessonList;
    }


    /**
     * Lấy thông tin User đã login được lưu gần nhất (ở SharedPreferences chung toàn Application).
     * Chú ý: Một khi đã lấy được đối tượng thì các thuộc tính trong nó cũng đã được đảm bảo hợp lệ, not null rồi.
     *
     * @param context The context.
     * @return The UserModel object that have stored in SharedPreferences.
     */
    public static UserModel getLastLoginUserInfo(Context context) {
        // Get default SharedPreferences (SharedPreferences chung toàn Application).
        // Todo: Dùng default.
        // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences prefs = context.getSharedPreferences(AppConfig.SharedPreferencesKey.FILE_LAST_APP_INFO, Context.MODE_PRIVATE);

        // Khởi tạo.
        UserModel userModel = null;

        // Invalid integer value.
        final int invalid = -1;

        /* Auth type. */
        int authType = prefs.getInt(AppConfig.SharedPreferencesKey.LAST_USER_AUTH_TYPE, invalid);

        /* Token information. */
        String accessToken = prefs.getString(AppConfig.SharedPreferencesKey.LAST_USER_TOKEN_ACCESS_TOKEN, "");
        String refreshToken = prefs.getString(AppConfig.SharedPreferencesKey.LAST_USER_TOKEN_REFRESH_TOKEN, "");
        long expire = prefs.getLong(AppConfig.SharedPreferencesKey.LAST_USER_TOKEN_EXPIRE, invalid);

        /* System Server information. */
        long userId = prefs.getLong(AppConfig.SharedPreferencesKey.LAST_USER_ID, invalid);
        String code = prefs.getString(AppConfig.SharedPreferencesKey.LAST_USER_CODE, "");
        String email = prefs.getString(AppConfig.SharedPreferencesKey.LAST_USER_EMAIL, "");
        String username = prefs.getString(AppConfig.SharedPreferencesKey.LAST_USER_USERNAME, "");
        String fullName = prefs.getString(AppConfig.SharedPreferencesKey.LAST_USER_FULL_NAME, "");
        String urlSlug = prefs.getString(AppConfig.SharedPreferencesKey.LAST_USER_URL_SLUG, "");
        String avatarUrl = prefs.getString(AppConfig.SharedPreferencesKey.LAST_USER_AVATAR_URL, "");

        /* Facebook information. */
        String facebookId = prefs.getString(AppConfig.SharedPreferencesKey.LAST_USER_FACEBOOK_ID, "");
        String facebookEmail = prefs.getString(AppConfig.SharedPreferencesKey.LAST_USER_FACEBOOK_EMAIL, "");
        String facebookUsername = prefs.getString(AppConfig.SharedPreferencesKey.LAST_USER_FACEBOOK_USERNAME, "");
        String facebookDisplayName = prefs.getString(AppConfig.SharedPreferencesKey.LAST_USER_FACEBOOK_DISPLAY_NAME, "");
        String facebookAvatarUrl = prefs.getString(AppConfig.SharedPreferencesKey.LAST_USER_FACEBOOK_AVATAR_URL, "");
        boolean facebookConnected = prefs.getBoolean(AppConfig.SharedPreferencesKey.LAST_USER_FACEBOOK_CONNECTED, false);

        // Validation, bỏ qua những trường không cần thiết.
        if (authType > invalid
                && userId > invalid
                && !accessToken.isEmpty()
                && !refreshToken.isEmpty()
                && expire > invalid) {
            // Các dự liệu đã hợp lệ, tạo mới đối tượng UserModel.
            userModel = new UserModel();

            /* Auth type. */
            userModel.authType = authType;

            /* Token information. */
            userModel.tokenInfo = new A01AccessTokenGenerator_JsonData();
            userModel.tokenInfo.access_token = accessToken;
            userModel.tokenInfo.refresh_token = refreshToken;
            userModel.tokenInfo.expires_in = expire;

            /* System Server information. */
            userModel.id = userId;
            userModel.code = code;
            userModel.email = email;
            userModel.username = username;
            userModel.fullName = fullName;
            userModel.urlSlug = urlSlug;
            userModel.avatarUrl = avatarUrl;

            /* Facebook information. */
            userModel.facebookId = facebookId;
            userModel.facebookEmail = facebookEmail;
            userModel.facebookUsername = facebookUsername;
            userModel.facebookDisplayName = facebookDisplayName;
            userModel.facebookAvatarUrl = facebookAvatarUrl;
            userModel.facebookConnected = facebookConnected;

        }

        return userModel;
    }

    /**
     * Xóa thông tin Login gần nhất đã lưu vào SharedPreferences lúc trước.<br/>
     * Method này không xảy ra lỗi, lưu theo cơ chế ở background, không cần biết kết quả.
     * Chú ý: Đây là bước để cẩn thận chống hack.
     *
     * @param context The context.
     */
    public static void deleteLastAppInfo(Context context) {
        // Get default SharedPreferences (SharedPreferences chung toàn Application).
        // Todo: Dùng default.
        // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences prefs = context.getSharedPreferences(AppConfig.SharedPreferencesKey.FILE_LAST_APP_INFO, Context.MODE_PRIVATE);


        // Clear all data.
        prefs.edit().clear().apply();
    }

    /**
     * Lưu thông tin User đã Login gần nhất xuống (ở SharedPreferences chung toàn Application).
     * Method này không xảy ra lỗi, lưu theo cơ chế ở background, không cần biết kết quả.
     *
     * @param context   The Context.
     * @param userModel The UserModel object.
     */
    public static void saveLastLoginUserInfo(Context context, UserModel userModel) {
        // Get default SharedPreferences (SharedPreferences chung toàn Application).
        // Todo: Dùng default.
        // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences prefs = context.getSharedPreferences(AppConfig.SharedPreferencesKey.FILE_LAST_APP_INFO, Context.MODE_PRIVATE);

        SharedPreferences.Editor prefsEditor = prefs.edit();

        // Lưu lần lượt các thông tin của User.
        /* Auth type. */
        prefsEditor.putInt(AppConfig.SharedPreferencesKey.LAST_USER_AUTH_TYPE, userModel.authType);

        /* Token information. */
        if (userModel.tokenInfo != null) {
            prefsEditor.putString(AppConfig.SharedPreferencesKey.LAST_USER_TOKEN_ACCESS_TOKEN, userModel.tokenInfo.access_token);
            prefsEditor.putString(AppConfig.SharedPreferencesKey.LAST_USER_TOKEN_REFRESH_TOKEN, userModel.tokenInfo.refresh_token);
            prefsEditor.putLong(AppConfig.SharedPreferencesKey.LAST_USER_TOKEN_EXPIRE, userModel.tokenInfo.expires_in);
        }

        /* System Server information. */
        prefsEditor.putLong(AppConfig.SharedPreferencesKey.LAST_USER_ID, userModel.id);
        prefsEditor.putString(AppConfig.SharedPreferencesKey.LAST_USER_CODE, userModel.code);
        prefsEditor.putString(AppConfig.SharedPreferencesKey.LAST_USER_EMAIL, userModel.email);
        prefsEditor.putString(AppConfig.SharedPreferencesKey.LAST_USER_USERNAME, userModel.username);
        prefsEditor.putString(AppConfig.SharedPreferencesKey.LAST_USER_FULL_NAME, userModel.fullName);
        prefsEditor.putString(AppConfig.SharedPreferencesKey.LAST_USER_URL_SLUG, userModel.urlSlug);
        prefsEditor.putString(AppConfig.SharedPreferencesKey.LAST_USER_AVATAR_URL, userModel.avatarUrl);

        /* Facebook information. */
        prefsEditor.putString(AppConfig.SharedPreferencesKey.LAST_USER_FACEBOOK_ID, userModel.facebookId);
        prefsEditor.putString(AppConfig.SharedPreferencesKey.LAST_USER_FACEBOOK_EMAIL, userModel.facebookEmail);
        prefsEditor.putString(AppConfig.SharedPreferencesKey.LAST_USER_FACEBOOK_USERNAME, userModel.facebookUsername);
        prefsEditor.putString(AppConfig.SharedPreferencesKey.LAST_USER_FACEBOOK_DISPLAY_NAME, userModel.facebookDisplayName);
        prefsEditor.putString(AppConfig.SharedPreferencesKey.LAST_USER_FACEBOOK_AVATAR_URL, userModel.facebookAvatarUrl);
        prefsEditor.putBoolean(AppConfig.SharedPreferencesKey.LAST_USER_FACEBOOK_CONNECTED, userModel.facebookConnected);

        // Save in background without waiting result.
        prefsEditor.apply();
    }

}
