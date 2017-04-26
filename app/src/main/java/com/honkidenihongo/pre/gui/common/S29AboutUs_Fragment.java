package com.honkidenihongo.pre.gui.common;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.common.util.NetworkUtil;
import com.honkidenihongo.pre.gui.MainActivity;
import com.honkidenihongo.pre.gui.listener.OnMainActivityListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Screen S29AboutUs.
 * <p>
 * Modify.
 *
 * @author binh.dt.
 * @since 7-Mar-2017.
 */
public class S29AboutUs_Fragment extends Fragment {
    private static final String LOG_TAG = S29AboutUs_Fragment.class.getName();
    private static final String VENDING = "com.android.vending";
    private static final String MARKET = "com.google.market";
    private static final String MARKET_VALUE = "Market";
    private static final String MARKET_ID = "market://details?id=";
    private static final String LINK_STORE = "https://play.google.com/store/apps/details?id=";
    private static final String FINSKY = "com.google.android.finsky.activities.LaunchUrlHandlerActivity";

    private OnMainActivityListener mActivityListener;
    public Context mContext;

    // For View.
    private AppCompatButton mBtnUpdate;
    private AppCompatTextView mTvTerm;
    private AppCompatTextView mTvVersionLater;
    private AppCompatTextView mTvVersionCurrent;
    private AppCompatTextView mTvVersionLaterName;
    private AppCompatTextView mTvDeception;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = getActivity();

        if (mContext instanceof MainActivity) {
            mActivityListener = (MainActivity) mContext;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.s29_about_us_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Method using create View inside layout.
        initView(view);

        if (NetworkUtil.isAvailable(mContext)) {
            // Call api check have version app ?
            checkVersionApp();
        }

        // Method using set event for View child inside Layout.
        setEvent();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mActivityListener != null) {
            mActivityListener.setTitleScreen(getString(R.string.common_module__about_us));
        }
    }

    /**
     * Method create View.
     */
    private void initView(View view) {
        mTvTerm = (AppCompatTextView) view.findViewById(R.id.mTvTerm);
        mBtnUpdate = (AppCompatButton) view.findViewById(R.id.mBtnUpdate);
        mTvDeception = (AppCompatTextView) view.findViewById(R.id.mTvDeception);
        mTvVersionLater = (AppCompatTextView) view.findViewById(R.id.mTvVersionLater);
        mTvVersionCurrent = (AppCompatTextView) view.findViewById(R.id.mTvVersionCurrent);
        mTvVersionLaterName = (AppCompatTextView) view.findViewById(R.id.mTvVersionLaterName);

        mTvVersionCurrent.setText(String.format("%s%s", ": ", AppConfig.getAppVersionName()));
        mBtnUpdate.setVisibility(View.GONE);
        mTvVersionLater.setVisibility(View.GONE);
        mTvVersionLaterName.setVisibility(View.GONE);
        mTvTerm.setPaintFlags(mTvTerm.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    /**
     * Method set event for View.
     */
    private void setEvent() {
        mBtnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoUpdateApplication();
            }
        });

        mTvTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Go to screen S26TermOfService.
                 */
                Intent intent = new Intent(mContext, S26TermsOfService_Activity.class);
                intent.putExtra(S26TermsOfService_Activity.GO_TO_FROM_SCREEN, S29AboutUs_Fragment.class.getName());
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                S29AboutUs_Fragment.this.startActivity(intent);
            }
        });
    }

    /**
     * Call api check version app.
     */
    private void checkVersionApp() {
        OkHttpClient okHttpClient = NetworkUtil.getDefaultHttpClient(mContext);

        Request request = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + Definition.API.CHECK_VERSION_APP)
                .build();

        Log.d("vvvv", "" + request.url());

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                processCheckVersionApp(null);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                processCheckVersionApp(response.body().string());
            }
        });
    }

    /**
     *
     */
    /**
     * Handle process check version app.
     */
    private void processCheckVersionApp(final String data) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(data)) {

                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(data);

                    if (jsonObject.has(Definition.JSON.LATEST)) {
                        JSONObject value = jsonObject.getJSONObject(Definition.JSON.LATEST);
                        int code = value.getInt(Definition.JSON.CODE);
                        String name = value.getString(Definition.JSON.NAME_KEY);
                        String deception = "";

                        if (LocaleHelper.getLanguage(mContext).equals(Definition.LanguageCode.ENGLISH)) {
                            deception = value.getString(Definition.JSON.NEW_FEATURE_EN);
                        }

                        if (LocaleHelper.getLanguage(mContext).equals(Definition.LanguageCode.VIETNAMESE)) {
                            deception = value.getString(Definition.JSON.NEW_FEATURE_VN);
                        }

                        if (AppConfig.getVersionCode() < code) {
                            mBtnUpdate.setVisibility(View.VISIBLE);
                            mTvVersionLater.setVisibility(View.VISIBLE);
                            mTvVersionLaterName.setVisibility(View.VISIBLE);
                            mTvVersionLaterName.setText(String.format("%s%s", ": ", name));
                            mTvDeception.setText(deception);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
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
            intent.setData(Uri.parse(LINK_STORE + appPackageName));

            mContext.startActivity(intent);
            ((Activity) mContext).finish();
        } else {
            Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(VENDING);
            ComponentName comp = new ComponentName(VENDING, FINSKY);
            launchIntent.setComponent(comp);
            launchIntent.setData(Uri.parse(MARKET_ID + appPackageName));

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
            PackageInfo info = pm.getPackageInfo(VENDING, PackageManager.GET_ACTIVITIES);
            String label = (String) info.applicationInfo.loadLabel(pm);
            isInstalled = (label != null && !label.equals(MARKET_VALUE));
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
        String sPackageName = "";

        List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);

        for (PackageInfo packageInfo : packages) {
            sPackageName = packageInfo.packageName;

            if (sPackageName.equals(MARKET) || sPackageName.equals(VENDING)) {
                playStoreInstalled = 1;

                break;
            }
        }

        return (playStoreInstalled > 0);
    }
}
