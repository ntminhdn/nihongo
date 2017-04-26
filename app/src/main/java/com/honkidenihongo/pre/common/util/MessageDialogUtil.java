package com.honkidenihongo.pre.common.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.gui.listener.NetworkConnectionCallback;
import com.honkidenihongo.pre.gui.widget.UpdateAppDialog;
import com.honkidenihongo.pre.gui.widget.HelperDialog;

/**
 * Util class related to displaying message dialog.
 *
 * @author long.tt.
 * @since 17-Nov-2016.
 */
public class MessageDialogUtil {
    /**
     * The network connection timeout: 30 seconds.
     */
    private static final long CONNECT_TIMEOUT = 30;

    /**
     * The network read timeout: 30 seconds.
     */
    private static final long READ_TIMEOUT = 30;

    /**
     * The network write timeout: 30 seconds.
     */
    private static final long WRITE_TIMEOUT = 30;

    /**
     * The private constructor to prevent creating object.
     */
    private MessageDialogUtil() {
    }

    public static void showNotificationDialog(Context context, String title, String message) {
        AlertDialog.Builder errDialogBuilder = new AlertDialog.Builder(context);

        errDialogBuilder.setCancelable(false)
                .setMessage(message)
                .setTitle(title)
                .setNegativeButton(R.string.common_text__ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        AlertDialog errDialog = errDialogBuilder.create();

        Activity activity = (Activity) context;

        // Kiểm tra activity tồn tại mới show dialog.
        if (!activity.isFinishing()) {
            errDialog.show();
        }
    }

    public static void showNetworkUnavailableDialog(Context context, final NetworkConnectionCallback callback) {
        AlertDialog.Builder errDialogBuilder = new AlertDialog.Builder(context);

        errDialogBuilder.setCancelable(false)
                .setMessage(context.getString(R.string.common_msg__content_error__have_no_internet))
                .setTitle(context.getString(R.string.common_msg__title__error))
                .setNegativeButton(R.string.common_text__cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if (callback != null) {
                            callback.onCancel();
                        }
                    }
                })
                .setPositiveButton(R.string.common_text__retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if (callback != null) {
                            callback.onTryAgain();
                        }
                    }
                });

        AlertDialog errDialog = errDialogBuilder.create();

        Activity activity = (Activity) context;

        // Kiểm tra activity tồn tại mới show dialog.
        if (!activity.isFinishing()) {
            errDialog.show();
        }
    }

    /**
     * Show message no data to user.
     *
     * @param message Value content.
     */
    public static void showMessageNoData(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setTitle(context.getString(R.string.common_msg__title__info));
        builder.setCancelable(false);

        builder.setPositiveButton(context.getString(R.string.common_text__ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();

        Activity activity = (Activity) context;

        // Kiểm tra activity tồn tại mới show dialog.
        if (!activity.isFinishing()) {
            alertDialog.show();
        }
    }
}
