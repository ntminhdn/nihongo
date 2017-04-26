package com.honkidenihongo.pre.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Class BroadCast manager using handler receive data from service download.
 *
 * @author BinhDT.
 */
public class DownloadLessonBroadcastReceiver {
    public static final String FILTER_DOWNLOAD_LESSON = "FILTER_DOWNLOAD_LESSON";

    public static final String ARG_STATUS_DOWNLOAD_LESSON = "ARG_STATUS_DOWNLOAD_LESSON";
    public static final String ARG_POSITION = "ARG_POSITION";

    public static void sendUpdateStatusDownloadLesson(Context context,int status, int position) {
        Intent intent = new Intent(FILTER_DOWNLOAD_LESSON);
        intent.putExtra(ARG_STATUS_DOWNLOAD_LESSON, status);
        intent.putExtra(ARG_POSITION, position);

        // Gởi kết quả trả về thông qua intent.
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
