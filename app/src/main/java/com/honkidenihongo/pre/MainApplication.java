package com.honkidenihongo.pre;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.facebook.appevents.AppEventsLogger;
import com.honkidenihongo.pre.common.config.DateFormatString;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.config.FontsConfig;
import com.honkidenihongo.pre.common.util.DatabaseUtil;
import com.honkidenihongo.pre.common.util.LocalAppUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.TimeLog;
import com.honkidenihongo.pre.model.UserModel;
import com.honkidenihongo.pre.model.constant.LessonStatus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.exceptions.RealmException;

/**
 * // Todo chú ý ở class này không đóng realm database vì file realm cấu hình theo user login.
 * Class MainApplication, main management of the Application.
 *
 * @author dat.pt.
 * @since 09-Oct-2016.
 */
public class MainApplication extends Application {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = MainApplication.class.getName();
    public static boolean isSaveLog = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // Update status of lesson inside local.
        updateStatusLessonInsidePreferences();

        AppEventsLogger.activateApp(this);

        // Init Database.
        Realm.init(this);

        // Config database realm.
        DatabaseUtil.configRealmDatabase(getApplicationContext());

        if (com.honkidenihongo.pre.BuildConfig.DEBUG) {
            ViewRealmDataDebugUtil.initialize(getApplicationContext());
        }

        configSendLog();

        // Đăng ký lắng nge vòng đời của ứng dụng.
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                // Kiểm tra nếu user login là khác null thì tạo new record.
                UserModel userModel = LocalAppUtil.getLastLoginUserInfo(activity);

                if (userModel != null) {
                    // Nếu Main activity là tồn tại thì tạo ra bộ time log cho user hiện tại dùng tương ứng.
                    Number currentIdNumber = Realm.getDefaultInstance().where(TimeLog.class).max(Definition.Database.TimeLog.TIME_LOG_FIELD_ID);

                    long nextId;

                    if (currentIdNumber == null) {
                        nextId = 1;
                    } else {
                        nextId = currentIdNumber.intValue() + 1;
                    }

                    TimeLog timeLog = new TimeLog();
                    timeLog.setId(nextId);
                    timeLog.setStart(new Date());
                    DateFormat dateFormat = new SimpleDateFormat(DateFormatString.YYYY_MM_DD, Locale.getDefault());
                    timeLog.setShortDay(dateFormat.format(timeLog.start));

                    // Trường này dùng để set thời gian online hiện tại của user được phép sửa.
                    timeLog.setIs_send(true);

                    timeLog.saveOrUpdate();
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                // Khi tắt màn hình cũng update data cho record mới nhất trong database.
                // Điều kiện kiểm tra nếu user login là khác null.
                UserModel userModel = LocalAppUtil.getLastLoginUserInfo(activity);

                if (userModel != null) {
                    updateTimeEndOnline(activity);
                }
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                // Khi tắt màn hình cũng update data cho record mới nhất trong database.
                // Điều kiện kiểm tra nếu user login là khác null.
                UserModel userModel = LocalAppUtil.getLastLoginUserInfo(activity);

                if (userModel != null) {
                    updateTimeEndOnline(activity);
                }

            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }
        });

        /**
         * Trong mỗi phiên làm việc của người dùng, ta cho phép chỉ show dialog update 1 lần trong màn hình main, cụ thể là S03Dashboard.
         */
        settingShowDialogUpdate();

        // Font from assets: "assets/fonts/Roboto-Regular.ttf.
        FontsConfig.overrideFont(getApplicationContext(), Definition.Fonts.FONT_DEFAULT, Definition.Fonts.PATH_FONT_ROBOTO);
    }

    /**
     * Method set language default is Vietnamese when install app times first.
     *
     * @param base Value content.
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, Definition.LanguageCode.VIETNAMESE));

        MultiDex.install(this);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            Log.d(LOG_TAG, "onTrimMemory(TRIM_MEMORY_UI_HIDDEN)");

            // Todo remove later.
            // Intent intent = new Intent(Definition.LogReceiverConstant.ACTION_END_LOG);
            //sendBroadcast(intent);
        }
    }

    /**
     * Setting key show dialog update inside screen S03DashBoard is true when application run first.
     */
    private void settingShowDialogUpdate() {
        SharedPreferences prefs = getSharedPreferences(Definition.SettingApp.SETTING_APPLICATION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Definition.SettingApp.DialogHelp.DIALOG_UPDATE_S03_DASH_BOARD, true);
        editor.apply();
    }

    /**
     * Update status list lesson.
     * Todo phương thức này cũng được triệu gọi trong screen main khi người dùng logout.
     */
    private void updateStatusLessonInsidePreferences() {
        /**
         * Khi người dùng tắt app trong background thì service sẽ bị hủy, app sẽ khởi động lại từ đầu
         * ta sẽ set lại các giá trị downloading của lesson thành unDownload.
         */
        List<Lesson> lessonListPreferences = LocalAppUtil.getLastLessonList(getApplicationContext());

        if (lessonListPreferences != null && !lessonListPreferences.isEmpty()) {
            for (Lesson lessonLocal : lessonListPreferences) {
                if (lessonLocal.status == LessonStatus.WAITING || lessonLocal.status == LessonStatus.WAITING_UPDATE || lessonLocal.status == LessonStatus.UPDATING || lessonLocal.status == LessonStatus.DOWNLOADING) {
                    lessonLocal.status = LessonStatus.UN_DOWNLOADED;
                }
            }

            LocalAppUtil.saveLastLessonList(getApplicationContext(), lessonListPreferences);
        }
    }

    private void configSendLog() {
        // Setup alarm to refresh token when it is expire
        Intent alarmIntent = new Intent(Definition.Constants.ACTION_SEND_LOG);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
                this, Definition.Constants.REQ_SEND_LOG, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long alarmDelay = 5 * 60 * 1000;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + alarmDelay, alarmPendingIntent);
    }

    /**
     * Update time off of user.
     *
     * @param context Value context of screen current.
     */
    private void updateTimeEndOnline(Context context) {
        // Lấy ngày hiện tại.
        DateFormat dateFormat = new SimpleDateFormat(DateFormatString.YYYY_MM_DD, Locale.getDefault());
        Date date = new Date();
        String currentDay = dateFormat.format(date);

        // Value shortDay là giá trị format theo ngày hiện tại.
        // App is in background, tìm tất cả đối tượng các ngày format theo ngày hiện tại.
        RealmResults<TimeLog> timeLogs = Realm.getDefaultInstance().where(TimeLog.class)
                .equalTo(Definition.Database.TimeLog.TIME_LOG_FIELD_SHORT_DAY, currentDay)
                .findAll();

        if (!timeLogs.isEmpty()) {
            final TimeLog timeLog = timeLogs.get(timeLogs.size() - 1);
            timeLog.editTimeLog(timeLog.getId(), context);
        }
    }
}
