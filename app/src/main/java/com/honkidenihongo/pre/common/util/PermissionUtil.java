package com.honkidenihongo.pre.common.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.honkidenihongo.pre.R;

/**
 * Class support permissions for application.
 * Reference: http://www.theappguruz.com/blog/android-take-photo-camera-gallery-code-sample.
 *
 * @author binh.dt.
 * @since 16-Jan-2017.
 */
public class PermissionUtil {
    public static final int REQUEST_PERMISSIONS_READ_EXTERNAL_STORAGE = 123;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermissionStorage(final Context context) {
        // Lấy version android của device đang chạy ứng dụng.
        int apiCurrent = Build.VERSION.SDK_INT;

        // Build.VERSION_CODES.M=23 , version android is 6.0.
        if (apiCurrent >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
}
