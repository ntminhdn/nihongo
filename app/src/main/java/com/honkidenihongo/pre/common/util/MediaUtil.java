package com.honkidenihongo.pre.common.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;

import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.DownloadFileConfig;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.UserModel;
import com.honkidenihongo.pre.model.constant.Category;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

/**
 * Util class dùng để check file audio.
 *
 * @author binh.dt.
 * @since 10-Dec-2016.
 */
public class MediaUtil {
    private static final String PATH = "/";
    public static final String FOLDER_VOICE = "voice";
    private static final String FOLDER_KANA = "Kana";
    private static final String FOLDER_COMMON = "Common";
    private static final String FOLDER_NUMBER = "Number";
    private static final String FOLDER_WORD = "Word";
    private static final String FOLDER_SENTENCE = "Sentence";
    private static final String FOLDER_PICTURE = "picture";

    /**
     * The private constructor.
     */
    private MediaUtil() {
    }

    /**
     * Phương thức kiểm tra file audio có chạy được trên device hay không?
     *
     * @param context   Value context cuả screen current.
     * @param lesson    Value lesson object.
     * @param userModel User đang đăng nhập.
     * @param fileName  Tên file void.
     * @return File audio.
     */
    public static File audioIsPrepare(Context context, Lesson lesson, UserModel userModel, int category, String fileName) {
        String placeFolderAudio = "";

        switch (category) {
            case Category.PRE_HIRAGANA:
            case Category.PRE_KATAKANA:

                placeFolderAudio = FOLDER_KANA;

                break;

            case Category.PRE_COMMON:
                placeFolderAudio = FOLDER_COMMON;

                break;

            case Category.PRE_NUMBER:
                placeFolderAudio = FOLDER_NUMBER;

                break;

            case Category.UNIT_WORD:
                placeFolderAudio = FOLDER_WORD;

                break;

            case Category.UNIT_SENTENCE:
                placeFolderAudio = FOLDER_SENTENCE;

                break;
        }

        String filepath = context.getFilesDir().getPath()
                + PATH
                + String.format("%s%s", AppConfig.SharedPreferencesKey.USER_INFO_PREFIX, userModel.id)
                + PATH + String.format(Locale.US, "%s%02d", DownloadFileConfig.DOWNLOAD_FILE_PREFIX_LESSON, lesson.getNumber())
                + PATH + FOLDER_VOICE
                + PATH + placeFolderAudio
                + PATH + fileName;

        try {
            File file = new File(filepath);
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();

            return file;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Method using get uri of file picture.
     *
     * @param context   Value context of screen current.
     * @param userModel User login.
     * @param lesson    Lesson current.
     * @param fileName  Name file.
     * @return Value Uri of file picture.
     */
    public static File getFilePicture(Context context, UserModel userModel, Lesson lesson, String fileName) {
        if (context == null || userModel == null || lesson == null || TextUtils.isEmpty(fileName)) {
            return null;
        }

        String filepath = context.getFilesDir().getPath()
                + PATH
                + String.format("%s%s", AppConfig.SharedPreferencesKey.USER_INFO_PREFIX, userModel.id)
                + PATH + String.format(Locale.US, "%s%02d", DownloadFileConfig.DOWNLOAD_FILE_PREFIX_LESSON, lesson.getNumber())
                + PATH + FOLDER_PICTURE
                + PATH + FOLDER_WORD
                + PATH + fileName;

        File file = new File(filepath);

        if (file.exists()) {
            return file;
        }

        return null;
    }
}
