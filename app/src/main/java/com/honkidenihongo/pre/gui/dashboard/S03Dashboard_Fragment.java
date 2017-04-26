package com.honkidenihongo.pre.gui.dashboard;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.DateFormatString;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.MessageDialogUtil;
import com.honkidenihongo.pre.common.util.NetworkUtil;
import com.honkidenihongo.pre.gui.MainActivity;
import com.honkidenihongo.pre.gui.listener.OnMainActivityListener;
import com.honkidenihongo.pre.gui.widget.DayView;
import com.honkidenihongo.pre.gui.widget.HelperDialog;
import com.honkidenihongo.pre.gui.widget.UpdateAppDialog;
import com.honkidenihongo.pre.model.TimeLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Screen Dashboard.
 * <p>
 * Modify.
 *
 * @author binh.dt.
 * @since 11-Dec-2016.
 */
public class S03Dashboard_Fragment extends Fragment {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S03Dashboard_Fragment.class.getName();
    private static final int MAX_SECOND_OF_DAY = 86400;

    private AppCompatTextView mTvTitle;
    private LinearLayout mLLDayView;

    private Boolean[] isLearned;
    private Date[] mDayOfWeeks;

    private HelperDialog mHelperDialog;
    private UpdateAppDialog mUpdateAppDialog;
    private OnMainActivityListener mActivityListener;
    private Context mContext;

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
        return inflater.inflate(R.layout.s03_dashboard_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        /**
         * Khởi tạo view.
         */
        initView(view);

        if (NetworkUtil.isAvailable(mContext)) {
            // Call api check have version app ?
            checkVersionApp();
        } else {
            showDialogHelp();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mActivityListener != null) {
            mActivityListener.setTitleScreen(getString(R.string.common_module__dashboard));
        }

        /**
         * Load data from database.
         */
        loadData();

        /**
         * Hiển thị data to view.
         */
        displayDataToView();
    }

    @Override
    public void onDestroy() {
        try {
            if (mHelperDialog != null && mHelperDialog.isShowing()) {
                mHelperDialog.dismiss();
                mHelperDialog = null;
            }

            if (mUpdateAppDialog != null && mUpdateAppDialog.isShowing()) {
                mUpdateAppDialog.dismiss();
                mUpdateAppDialog = null;
            }

        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        super.onDestroy();
    }

    // Initialize all child view inside root view.
    private void initView(View view) {
        mTvTitle = (AppCompatTextView) view.findViewById(R.id.mTvTitle);
        mLLDayView = (LinearLayout) view.findViewById(R.id.mLlDayView);

        mHelperDialog = new HelperDialog(mContext, R.style.TransparentDialog, getString(R.string.common_help__s03_dashboard__title), getString(R.string.common_help__s03_dashboard__content));
        mUpdateAppDialog = new UpdateAppDialog(mContext, R.style.TransparentDialog, getString(R.string.common_text__update), mContext.getString(R.string.common_msg__content_has_new_version));
    }

    /**
     * Convert second to min and hours.
     *
     * @param bigDecimal Value.
     */
    private void viewToComponentTimes(BigDecimal bigDecimal) {
        long longVal = bigDecimal.longValue();
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int minutes = remainder / 60;

        String content = String.format("%s %s %s %s %s", mContext.getString(R.string.s03_dashboard__learning_information)
                , String.valueOf(hours)
                , mContext.getString(hours > 1 ? R.string.common_unit__hour__plural : R.string.common_unit__hour__singular)
                , String.valueOf(minutes)
                , mContext.getString(minutes > 1 ? R.string.common_unit__minute__plural : R.string.common_unit__minute__singular));

        mTvTitle.setText(content);
    }

    /**
     * Call api check version app.
     */
    private void checkVersionApp() {
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + Definition.API.CHECK_VERSION_APP)
                .build();

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
        if (TextUtils.isEmpty(data)) {
            showDialogHelp();

            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(data);

            if (jsonObject.has(Definition.JSON.LATEST)) {
                JSONObject value = jsonObject.getJSONObject(Definition.JSON.LATEST);
                int code = value.getInt(Definition.JSON.CODE);

                if (AppConfig.getVersionCode() < code) {
                    showDialogUpdate();
                } else {
                    showDialogHelp();
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());

            showDialogHelp();
        }
    }

    /**
     * Method using show dialog help.
     */
    private void showDialogHelp() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mContext == null || mHelperDialog == null) {
                    return;
                }

                boolean isShow = false;

                SharedPreferences prefs = mContext.getSharedPreferences(Definition.SettingApp.SETTING_APPLICATION, Context.MODE_PRIVATE);

                boolean isShowFirstOfScreen = prefs.getBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S03_DASH_BOARD, true);

                if (isShowFirstOfScreen) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S03_DASH_BOARD, false);
                    editor.apply();

                    isShow = true;
                } else {
                    boolean isShowApplication = prefs.getBoolean(Definition.SettingApp.DialogHelp.SHOW_DIALOG_HELP_ALL_APPLICATION, false);

                    if (isShowApplication) {
                        isShow = true;
                    }
                }

                if (isShow && isAdded() && !((Activity) mContext).isFinishing() && !mHelperDialog.isShowing()) {
                    mHelperDialog.show();
                }
            }
        });
    }

    /**
     * Method using show dialog update.
     */
    private void showDialogUpdate() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mContext == null || mUpdateAppDialog == null) {
                    return;
                }

                SharedPreferences prefs = mContext.getSharedPreferences(Definition.SettingApp.SETTING_APPLICATION, Context.MODE_PRIVATE);

                boolean isShowFirstOfScreen = prefs.getBoolean(Definition.SettingApp.DialogHelp.DIALOG_UPDATE_S03_DASH_BOARD, false);

                if (isShowFirstOfScreen && isAdded() && !((Activity) mContext).isFinishing() && !mUpdateAppDialog.isShowing()) {
                    mUpdateAppDialog.show();

                    // Only show one times, when user close app inside background of systems and open app again, it be show again.
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Definition.SettingApp.DialogHelp.DIALOG_UPDATE_S03_DASH_BOARD, false);
                    editor.apply();
                }
            }
        });
    }

    /**
     * Tính tổng thời gian trong ngày đó mà user đã học ra đơn vị s.
     *
     * @param date Value Day.
     * @return Value long.
     */
    private long totalPerDay(Date date) {
        long secondOfDay = 0;

        // Format theo ngày được truyền vào.
        DateFormat dateFormat = new SimpleDateFormat(DateFormatString.YYYY_MM_DD, Locale.getDefault());
        String currentDay = dateFormat.format(date);

        /**
         * shortDay là 1 trường dữ liệu trong bảng dữ liệu để so sánh nó với ngày hiện tại lấy ra tính thời gian user đã học.
         */
        RealmResults<TimeLog> timeLogs = Realm.getDefaultInstance().where(TimeLog.class)
                .equalTo("shortDay", currentDay)
                .findAll();

        if (!timeLogs.isEmpty()) {
            // Nếu phiên làm việc có value.
            for (TimeLog timeLog : timeLogs) {
                if (timeLog.getDuration() != 0) {
                    secondOfDay = (long) (secondOfDay + timeLog.getDuration());
                }
            }
        }

        return secondOfDay;
    }

    /**
     * Method using get data from database.
     */
    private void loadData() {
        // Khởi tạo mảng data.
        mDayOfWeeks = new Date[7];
        isLearned = new Boolean[7];

        getDaysOfCurrentWeek();

        // Lấy ngày hiện tại.
        viewToComponentTimes(new BigDecimal(totalPerDay(new Date())));
    }

    /**
     * Tính phần trăm ngày đưa vào method user đã học được bao nhiêu.
     *
     * @param date Value receive.
     * @return Value Float.
     */
    private double getPercentOfDay(Date date) {
        // Tính phần trăm user đã học trên 1 ngày.
        return (double) totalPerDay(date) / MAX_SECOND_OF_DAY;
    }

    /**
     * Method using hiển thị data to view.
     */
    private void displayDataToView() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);

        if (mLLDayView != null && mLLDayView.getChildCount() > 0) {
            mLLDayView.removeAllViews();
        }

        // Add từng View day vô trong mLLDayView.
        for (int i = 0; i < 7; i++) {
            mLLDayView.addView(DayView.newInstance(mContext, getDayFromIndex(mDayOfWeeks[i]), getPercentOfDay(mDayOfWeeks[i])), params);
        }
    }

    /**
     * Method dùng add view cho ngày được đưa vào từ vị trí position của mảng day.
     *
     * @param date Vị trí day trong mảng days.
     * @return All view ứng với ngày.
     */
    private DayView.Day getDayFromIndex(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // Kiểm tra xem ngày truyền vào là thứ mấy trong tuần để add view tương ứng.
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:

                return DayView.Day.MONDAY;

            case Calendar.TUESDAY:

                return DayView.Day.TUESDAY;

            case Calendar.WEDNESDAY:

                return DayView.Day.WEDNESDAY;

            case Calendar.THURSDAY:

                return DayView.Day.THURSDAY;

            case Calendar.FRIDAY:

                return DayView.Day.FRIDAY;

            case Calendar.SATURDAY:

                return DayView.Day.SATURDAY;

            case Calendar.SUNDAY:

                return DayView.Day.SUNDAY;

            default:

                return DayView.Day.SATURDAY;
        }
    }

    /**
     * Method lấy tất cả các ngày của tuần.
     */
    private void getDaysOfCurrentWeek() {
        // Lấy 7 ngày trước từ ngày hiện tại đưa vào mảng.
        for (int i = 0; i < 7; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, i - 7);

            mDayOfWeeks[i] = calendar.getTime();
        }
    }
}
