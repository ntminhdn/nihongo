package com.honkidenihongo.pre.gui.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.honkidenihongo.pre.R;

import java.util.List;

/**
 * Class show update app version for user.
 *
 * @author BinhDT.
 */
public class UpdateAppDialog extends Dialog {
    private static final String LOG_TAG = HelperDialog.class.getName();

    private AppCompatTextView mTvCancel;
    private AppCompatTextView mTvContent;
    private AppCompatTextView mTvTitle;
    private AppCompatTextView mTvOk;

    private String mMessage = "";
    private String mTitle = "";
    private Context mContext;

    /**
     * Constructor of class.
     *
     * @param context    Value context of Screen current.
     * @param themeResId Value theme id.
     */
    public UpdateAppDialog(Context context, int themeResId, String title, String message) {
        super(context, themeResId);

        mContext = context;
        mTitle = title;
        mMessage = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Set layout for window.
         */
        setContentView(R.layout.view_dialog_update);

        /**
         * Method init.
         */
        init();

        /**
         * Call method set event.
         */
        setEvent();
    }

    /**
     * Method init dialog help.
     */
    private void init() {
        /**
         * Set Attributes.
         */
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(this.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.CENTER;
        this.getWindow().setAttributes(layoutParams);

        // Init View.
        mTvContent = (AppCompatTextView) findViewById(R.id.mTvContent);
        mTvTitle = (AppCompatTextView) findViewById(R.id.mTvTitle);
        mTvCancel = (AppCompatTextView) findViewById(R.id.mTvCancel);
        mTvOk = (AppCompatTextView) findViewById(R.id.mTvOk);

        mTvTitle.setText(mTitle);
        mTvContent.setText(mMessage);
    }

    /**
     * Set event for view.
     */
    private void setEvent() {
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mTvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

                gotoUpdateApplication();
            }
        });
    }

    /**
     * Method using open chPlay or chPlay from browse for user update appilcation.
     */
    private void gotoUpdateApplication() {
        if (mContext == null) {
            return;
        }

        final String appPackageName = mContext.getPackageName();

        if (!checkPlayServices((Activity) mContext) || !isPlayStoreInstalled((Activity) mContext)) {
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));

            mContext.startActivity(intent);
            ((Activity) mContext).finish();
        } else {
            Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage("com.android.vending");
            ComponentName comp = new ComponentName("com.android.vending", "com.google.android.finsky.activities.LaunchUrlHandlerActivity");
            launchIntent.setComponent(comp);
            launchIntent.setData(Uri.parse("market://details?id=" + appPackageName));

            mContext.startActivity(launchIntent);
            ((Activity) mContext).finish();
        }
    }

    /**
     * http://stackoverflow.com/questions/30334237/android-check-somewhere-an-app-is-installed-via-store-or-manually
     * <p>
     *
     * @param context Value of activity current.
     * @return True or false.
     */
    private boolean checkPlayServices(Activity context) {
        PackageManager pm = context.getPackageManager();
        boolean isInstalled;

        try {
            PackageInfo info = pm.getPackageInfo("com.android.vending", PackageManager.GET_ACTIVITIES);
            String label = (String) info.applicationInfo.loadLabel(pm);
            isInstalled = (label != null && !label.equals("Market"));
        } catch (PackageManager.NameNotFoundException e) {
            isInstalled = false;
        }

        return isInstalled;
    }

    /**
     * Method check ch play is installed.
     *
     * @param activity Value of activity current.
     * @return True or false.
     */
    private boolean isPlayStoreInstalled(Activity activity) {
        int playStoreInstalled = -1;

        PackageManager packageManager = activity.getPackageManager();
        String sPlayStorePackageNameOld = "com.google.market";
        String sPlayStorePackageNameNew = "com.android.vending";
        String sPackageName = "";

        List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);

        for (PackageInfo packageInfo : packages) {
            sPackageName = packageInfo.packageName;

            if (sPackageName.equals(sPlayStorePackageNameOld) || sPackageName.equals(sPlayStorePackageNameNew)) {
                playStoreInstalled = 1;

                break;
            }
        }

        return (playStoreInstalled > 0);
    }
}
