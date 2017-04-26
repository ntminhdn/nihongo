package com.honkidenihongo.pre.gui.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.common.util.MessageDialogUtil;
import com.honkidenihongo.pre.gui.MainActivity;
import com.honkidenihongo.pre.gui.listener.OnMainActivityListener;
import com.honkidenihongo.pre.gui.widget.HelperDialog;

/**
 * Màn hình cài đặt cho ứng dụng.
 *
 * @author binh.dt.
 * @since 08-Nov-2016.
 */
public class S18Setting_Fragment extends Fragment {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S18Setting_Fragment.class.getName();

    private OnMainActivityListener mOnMainActivityListener;
    private Context mContext;

    // For view.
    private AppCompatCheckBox mChkVietnamese;
    private AppCompatCheckBox mChkEnglish;
    private AppCompatCheckBox mChkShowPopup;
    private AppCompatImageView mImgVietnamese;
    private AppCompatImageView mImgEnglish;

    private HelperDialog mHelperDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.s18_setting_fragment, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = getActivity();

        if (mContext instanceof MainActivity) {
            mOnMainActivityListener = (MainActivity) mContext;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mOnMainActivityListener != null) {
            mOnMainActivityListener.setTitleScreen(getString(R.string.common_module__setting));
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (mHelperDialog != null && mHelperDialog.isShowing()) {
                mHelperDialog.dismiss();
                mHelperDialog = null;
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        /**
         * Init View for layout.
         */
        initView(view);

        /**
         * Set data for View.
         */
        setData();

        /**
         * Set event for View.
         */
        setEvent();

        /**
         * Show dialog help.
         */
        showDialogHelp();
    }

    /**
     * Method using init View inside layout.
     */
    protected void initView(View view) {
        mChkVietnamese = (AppCompatCheckBox) view.findViewById(R.id.mChkVietnamese);
        mChkEnglish = (AppCompatCheckBox) view.findViewById(R.id.mChkEnglish);
        mChkShowPopup = (AppCompatCheckBox) view.findViewById(R.id.mChkShowPopup);
        mImgVietnamese = (AppCompatImageView) view.findViewById(R.id.mImgVietnamese);
        mImgEnglish = (AppCompatImageView) view.findViewById(R.id.mImgEnglish);

        mHelperDialog = new HelperDialog(mContext, R.style.TransparentDialog, getString(R.string.common_help__s18_setting__title), getString(R.string.common_help__s18_setting__content));
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

                boolean isShowFirstOfScreen = prefs.getBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S18_SETTING, true);

                if (isShowFirstOfScreen) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S18_SETTING, false);
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
     * Method using set data for View.
     */
    protected void setData() {
        Log.d(LOG_TAG, LocaleHelper.getLanguage(mContext));

        // Làm mờ ảnh ứng với ngôn ngữ hiện tại của nó được chọn.
        if (LocaleHelper.getLanguage(mContext).equals(Definition.LanguageCode.ENGLISH)) {
            mImgVietnamese.setAlpha(Definition.Graphic.LIMPIDITY);
            mImgEnglish.setAlpha(Definition.Graphic.BLEAR_NATIONAL_FLAG);
            mImgEnglish.setEnabled(false);
        } else {
            mImgEnglish.setAlpha(Definition.Graphic.LIMPIDITY);
            mImgVietnamese.setAlpha(Definition.Graphic.BLEAR_NATIONAL_FLAG);
            mImgVietnamese.setEnabled(false);
        }

        mChkEnglish.setChecked(LocaleHelper.getLanguage(mContext).equals(Definition.LanguageCode.ENGLISH));
        mChkVietnamese.setChecked(LocaleHelper.getLanguage(mContext).equals(Definition.LanguageCode.VIETNAMESE));

        mChkEnglish.setEnabled(!LocaleHelper.getLanguage(mContext).equals(Definition.LanguageCode.ENGLISH));
        mChkVietnamese.setEnabled(!LocaleHelper.getLanguage(mContext).equals(Definition.LanguageCode.VIETNAMESE));

        // Kiểm tra trạng thái cài đặt của show popup value default is true.
        SharedPreferences prefs = mContext.getSharedPreferences(Definition.SettingApp.SETTING_APPLICATION, Context.MODE_PRIVATE);
        boolean isShow = prefs.getBoolean(Definition.SettingApp.DialogHelp.SHOW_DIALOG_HELP_ALL_APPLICATION, false);

        mChkShowPopup.setChecked(isShow);
    }

    /**
     * Method using set event for view inside layout.
     */
    protected void setEvent() {
        // Click on checkbox Vietnamese.
        mChkVietnamese.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
                if (isCheck) {
                    mChkEnglish.setChecked(!mChkVietnamese.isChecked());
                    LocaleHelper.setLocale(mContext, Definition.LanguageCode.VIETNAMESE);
                    restart();
                }
            }
        });

        mImgVietnamese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChkEnglish.setChecked(!mChkVietnamese.isChecked());
                mChkVietnamese.setChecked(!mChkVietnamese.isChecked());
                LocaleHelper.setLocale(mContext, Definition.LanguageCode.VIETNAMESE);
                restart();
            }
        });

        // Click on checkbox English.
        mChkEnglish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
                if (isCheck) {
                    mChkVietnamese.setChecked(!mChkVietnamese.isChecked());
                    LocaleHelper.setLocale(mContext, Definition.LanguageCode.ENGLISH);
                    restart();
                }
            }
        });

        mImgEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChkEnglish.setChecked(!mChkVietnamese.isChecked());
                mChkVietnamese.setChecked(!mChkVietnamese.isChecked());
                LocaleHelper.setLocale(mContext, Definition.LanguageCode.ENGLISH);
                restart();
            }
        });

        mChkShowPopup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Lưu thông tin show popup hay không?
                SharedPreferences sharedPref = mContext.getSharedPreferences(Definition.SettingApp.SETTING_APPLICATION, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(Definition.SettingApp.DialogHelp.SHOW_DIALOG_HELP_ALL_APPLICATION, isChecked);
                editor.apply();
            }
        });
    }

    /**
     * Run app again.
     */
    private void restart() {
        // Chỉ mở lại activity Main Activity ko chạy lại màn hình splash.
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(intent);
    }
}
