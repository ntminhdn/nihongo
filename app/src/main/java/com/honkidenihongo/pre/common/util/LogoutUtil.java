package com.honkidenihongo.pre.common.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.honkidenihongo.pre.common.config.DateFormatString;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.gui.auth.S01Login_Activity;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.TimeLog;
import com.honkidenihongo.pre.model.constant.LessonStatus;
import com.honkidenihongo.pre.service.DownloadLessonService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Class handler processing logout user.
 *
 * @author BinhDT.
 */
public class LogoutUtil {
    private static final String LOG_TAG = LogoutUtil.class.getName();

    /**
     * Default constructor.
     */
    public LogoutUtil() {
    }

    /**
     * Khi người dùng đồng ý logout khỏi ứng dụng thì methoad này được triệu gọi.
     */
    public static void handleLogoutUser(final Context context) {
        // Các method phía dưới phải thưc hiện trước khi clear thông tin user đăng nhập.

        /**
         * Khi người dùng logout thì thực hiện update lại trạng thái đang downloading của lesson dưới Preferences về unDownload
         * đồng thởi gởi message stop service đến service download lesson đang chạy nếu có để dừng.
         */
        updateStatusLessonInsidePreferences(context);

        // STOP SERVICE DOWNLOAD.
        Intent stopServiceDownloadLessonIntent = new Intent();
        stopServiceDownloadLessonIntent.setAction(DownloadLessonService.ACTION_STOP_DOWNLOAD_LESSON);
        context.sendBroadcast(stopServiceDownloadLessonIntent);

        // Khi người dùng thoát tài khoản thì update lại thời gian
        // kết thúc online và recode cho lần online cuối cùng này không sửa thay đổi được nữa bởi thuộc tính set_isSend=false.
        updateTimeEndOnline(context);

                             /* Nếu có mạng thì request (asynchronous) Logout lên Server.
                                Không quan tâm đến kết quả có OK hay không, cũng phải xóa thông tin cũ ở Local
                                Sau đó không hiển thị thông báo nào cả, rồi đi đến màn hình Login.
                                */
        if (NetworkUtil.isAvailable(context)) {
            // Cần thực hiện request trước khi xóa vì thông tin Access-Token đang lưu ở Local.
            // Todo... Xử lý Logout trên Server.
            //  requestAsynLogout();

            // Cứ click Logout là xóa thông tin cũ. Đây là bước để cẩn thận security chống hack.
            LocalAppUtil.deleteLastAppInfo(context);

            // Xóa thông tin facebook đã lấy trước đó nếu có để UI button login with facebook set lại trạng thái đăng nhập.
            try {
                LoginManager.getInstance().logOut();
            } catch (FacebookException e) {
                Log.d(LOG_TAG, e.getMessage());
            }
        } else {
            // Nếu không có mạng thì cứ âm thầm xóa thẳng tay thông tin Login cũ ở Local.
            // Cứ click Logout là xóa thông tin cũ. Đây là bước để cẩn thận security chống hack.
            LocalAppUtil.deleteLastAppInfo(context);

            // Xóa thông tin facebook đã lấy trước đó nếu có để UI button login with facebook set lại trạng thái đăng nhập.
            try {
                LoginManager.getInstance().logOut();
            } catch (FacebookException e) {
                Log.d(LOG_TAG, e.getMessage());
            }
        }

        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Cho delay để đóng class đóng hẳn menu lựa chọn để tránh hiện tượng giật gây khó chịu cho người dùng.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Đi đến màn hình Login, khử hiệu ứng chuyển window.
                        Intent intent = new Intent(context, S01Login_Activity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                    }
                }, 300); // Delay 0.3s.
            }
        });
    }

    /**
     * Update status list lesson.
     * Todo phương thức này cũng được triệu gọi trong start main Application.
     */
    private static void updateStatusLessonInsidePreferences(Context context) {
        /**
         * Khi người dùng tắt app trong background thì service sẽ bị hủy, app sẽ khởi động lại từ đầu
         * ta sẽ set lại các giá trị downloading của lesson thành unDownload.
         */
        List<Lesson> lessonListPreferences = LocalAppUtil.getLastLessonList(context);

        if (lessonListPreferences != null && !lessonListPreferences.isEmpty()) {
            for (Lesson lessonLocal : lessonListPreferences) {
                if (lessonLocal.status == LessonStatus.WAITING || lessonLocal.status == LessonStatus.WAITING_UPDATE || lessonLocal.status == LessonStatus.UPDATING || lessonLocal.status == LessonStatus.DOWNLOADING) {
                    lessonLocal.status = LessonStatus.UN_DOWNLOADED;
                }
            }

            LocalAppUtil.saveLastLessonList(context, lessonListPreferences);
        }
    }

    /**
     * Update time off of user.
     */
    private static void updateTimeEndOnline(Context context) {
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
