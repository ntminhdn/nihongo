package com.honkidenihongo.pre.common.util;

import android.content.Context;
import android.util.Log;

import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.DownloadFileConfig;
import com.honkidenihongo.pre.dac.S06LessonList_Dac;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.UserModel;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;

/**
 * Class hỗ trợ cho việc update data... của application khi release lên version mới.
 *
 * @author binh.dt.
 * @since 6-Mar-2017.
 */
public class ApplicationUpdateUtil {
    /**
     * The Tag for logging.
     */
    private final static String LOG_TAG = ApplicationUpdateUtil.class.getName();

    /**
     * The private constructor to prevent creating new object.
     */
    private ApplicationUpdateUtil() {
    }

    /**
     * Method using update folder data of version old <2.
     */
    public static void updateFolder(Context context) {
        UserModel lastUserModel = LocalAppUtil.getLastLoginUserInfo(context);

        if (lastUserModel == null) {
            return;
        }

        // Thư mục Files của App.
        File filesDir = context.getFilesDir();

        // Thư mục theo User.
        String userDirString = String.format("%s%s", AppConfig.SharedPreferencesKey.USER_INFO_PREFIX, lastUserModel.id);

        // Thư mục theo User.
        File userLessonFilesDir = new File(filesDir, userDirString);

        List<Lesson> lessonsInstalled = new S06LessonList_Dac(Realm.getDefaultInstance()).readLessonList();

        // Nếu thư mục User Files này không tồn tại thì tạo. Nếu tạo không thành công thì trả về null.
        if (userLessonFilesDir.exists() && !lessonsInstalled.isEmpty()) {
            for (Lesson lesson : lessonsInstalled) {
                // Thư mục theo Lesson.
                String lessonDirWithId = String.format("%s%s", DownloadFileConfig.DOWNLOAD_FILE_PREFIX_LESSON, lesson.getId());
                String lessonDirWithNumber = String.format(Locale.US, "%s%02d", DownloadFileConfig.DOWNLOAD_FILE_PREFIX_LESSON, lesson.getNumber());

                try {
                    File lessonFilesDirId = new File(userLessonFilesDir, lessonDirWithId);

                    // Check exits of folder.
                    if (!lessonFilesDirId.exists()) {
                        return;
                    }

                    File lessonFilesDirNumber = new File(userLessonFilesDir, lessonDirWithNumber);
                    File lessonFilesDirVoid = new File(lessonFilesDirNumber, MediaUtil.FOLDER_VOICE);
                    IoUtil.copyDirectory(lessonFilesDirId, lessonFilesDirVoid);
                    IoUtil.deleteDirectory(lessonFilesDirId);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "" + e.getMessage());
                }
            }
        }
    }
}
