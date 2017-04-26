package com.honkidenihongo.pre.gui.auth;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.KeyboardUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.common.util.MessageDialogUtil;
import com.honkidenihongo.pre.common.util.NetworkUtil;
import com.honkidenihongo.pre.common.util.ToolbarUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Screen reset password step 3.
 *
 * @author binh.dt.
 * @since 21-Nov-2016.
 */
public class S25ResetPassword_Step3_Activity extends AppCompatActivity {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S25ResetPassword_Step3_Activity.class.getName();
    public static final String S25_STEP_3_EMAIL_ADDRESS = "S25_STEP_3_EMAIL_ADDRESS";
    public static final String S25_STEP_3_CODE = "S25_STEP_3_CODE";

    private ProgressDialog mProgressDialog;

    // For View.
    private Toolbar mToolbar;
    private AppCompatTextView mTxtPassword;
    private AppCompatTextView mTxtPasswordVerify;
    private AppCompatEditText mEdtPassword;
    private AppCompatEditText mEdtPasswordVerify;
    private AppCompatTextView mTvTitleToolbar;
    private AppCompatButton mBtnSend;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nếu email and code nhận được là null thì tắt màn hình và return.
        if (TextUtils.isEmpty(getEmailFromIntentData()) || TextUtils.isEmpty(getCodeFromIntentData())) {
            finish();

            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.s25_reset_password_step3_activity);

        /**
         * Call method init View inside layout.
         */
        initView();

        // Init toolbar.
        ToolbarUtil.initToolbar(mToolbar, this);

        /**
         * Call method set event for View.
         */
        setEvent();
    }

    /**
     * Phương thức dùng để lắng nghe sự thay đổi ngôn ngữ của app.
     *
     * @param base Value context.
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(0, 0);
    }

    /**
     * Method create View.
     */
    private void initView() {
        mTxtPassword = (AppCompatTextView) findViewById(R.id.mTxtPassword);
        mTxtPasswordVerify = (AppCompatTextView) findViewById(R.id.mTxtPasswordVerify);
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mEdtPassword = (AppCompatEditText) findViewById(R.id.mEdtPassword);
        mEdtPasswordVerify = (AppCompatEditText) findViewById(R.id.mEdtPasswordVerify);
        mBtnSend = (AppCompatButton) findViewById(R.id.mBtnSend);
        mTvTitleToolbar = (AppCompatTextView) mToolbar.findViewById(R.id.mTvTitleToolbar);
        mTvTitleToolbar.setText(getString(R.string.common_app__name));

        mEdtPassword.setTypeface(Typeface.DEFAULT);
        mEdtPasswordVerify.setTypeface(Typeface.DEFAULT);

        // Khởi tạo dialog.
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.common_msg__content_info__processing));
        mProgressDialog.setCancelable(false);
    }

    /**
     * Method set event for View.
     */
    private void setEvent() {
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPasswordValid().isEmpty()) {
                    // Nếu có kết nối mạng.
                    if (NetworkUtil.isAvailable(S25ResetPassword_Step3_Activity.this)) {
                        sendNewPasswordForUser(getEmailFromIntentData(), getCodeFromIntentData(), mEdtPassword.getText().toString().trim());
                    } else {
                        MessageDialogUtil.showNotificationDialog(S25ResetPassword_Step3_Activity.this, getString(R.string.common_msg__title__error), getString(R.string.common_msg__content_error__have_no_internet));
                    }
                } else {
                    MessageDialogUtil.showNotificationDialog(S25ResetPassword_Step3_Activity.this, getString(R.string.common_msg__title__warning), isPasswordValid().get(0));
                }
            }
        });

        mTxtPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEdtPassword.requestFocus();
                KeyboardUtil.showKeyboard(mEdtPassword, S25ResetPassword_Step3_Activity.this);
            }
        });

        mTxtPasswordVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEdtPasswordVerify.requestFocus();
                KeyboardUtil.showKeyboard(mEdtPasswordVerify, S25ResetPassword_Step3_Activity.this);
            }
        });
    }

    /**
     * Method send new password to server.
     *
     * @param email    Value email.
     * @param code     Value code.
     * @param password Value password.
     */
    private void sendNewPasswordForUser(String email, String code, String password) {
        mProgressDialog.show();

        OkHttpClient okHttpClient = NetworkUtil.getDefaultHttpClient(this);

        // Chuẩn bị dữ kiện để gửi lên Server.
        final RequestBody formBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_EMAIL, email)
                .add(Definition.Request.PARAM_RESET_CODE, code)
                .add(Definition.Request.PARAM_PASSWORD, password)
                .build();

        Request request = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + Definition.API.PASSWORD_RESET)
                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                .header(Definition.Request.PARAM_CLIENT_TOKEN, AppConfig.getClientToken())
                .post(formBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        MessageDialogUtil.showNotificationDialog(S25ResetPassword_Step3_Activity.this, getString(R.string.common_msg__title__error), getString(R.string.s25_reset_password__content_error__reset_password_failure));
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                mProgressDialog.dismiss();

                // Nếu có Response thành công thì parse để lấy dữ liệu.
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showMessageChangePasswordSuccess();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MessageDialogUtil.showNotificationDialog(S25ResetPassword_Step3_Activity.this, getString(R.string.common_msg__title__error), getString(R.string.s25_reset_password__content_error__reset_password_failure));
                        }
                    });
                }
            }
        });
    }

    /**
     * Show message change password success after goto screen login.
     */
    private void showMessageChangePasswordSuccess() {
        AlertDialog.Builder builder = new AlertDialog.Builder(S25ResetPassword_Step3_Activity.this);
        builder.setMessage(getString(R.string.s25_reset_password__content_info__reset_password_success));
        builder.setTitle(getString(R.string.common_msg__title__info));
        builder.setCancelable(false);

        builder.setPositiveButton(getString(R.string.common_text__ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                // Set đổi password thành công, và finish màn hình hiện tại trở về màn hình login.
                Intent intent = getIntent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Method is used for checking valid password id format.
     *
     * @return Boolean true for valid false for invalid.
     */
    private List<String> isPasswordValid() {
        String password = mEdtPassword.getText().toString();
        String passwordVerify = mEdtPasswordVerify.getText().toString();

        List<String> errors = new ArrayList<>();

        if (TextUtils.isEmpty(password)) {
            errors.add(getString(R.string.common_auth__txt_password__validation_required));
        } else {
            // Check password min.
            if (password.length() < getResources().getInteger(R.integer.common_txt_password_min_length)) {
                errors.add(getString(R.string.common_auth__txt_password__validation_min_length));
            }

            // Check password max.
            if (password.length() >= getResources().getInteger(R.integer.common_txt_password_max_length)) {
                errors.add(getString(R.string.common_auth__txt_password__validation_max_length));
            }
        }

        if (TextUtils.isEmpty(passwordVerify)) {
            errors.add(getString(R.string.common_auth__txt_confirm_password__validation_required));
        } else {
            // Check passwordConfirm min.
            if (passwordVerify.length() < getResources().getInteger(R.integer.common_txt_password_min_length)) {
                errors.add(getString(R.string.common_auth__txt_confirm_password__validation_min_length));
            }

            // Check passwordConfirm max.
            if (passwordVerify.length() >= getResources().getInteger(R.integer.common_txt_password_max_length)) {
                errors.add(getString(R.string.common_auth__txt_confirm_password__validation_max_length));
            }
        }

        if (!password.equals(passwordVerify)) {
            errors.add(getString(R.string.common_auth__txt_confirm_password__validation_not_match));
        }

        return errors;
    }

    /**
     * Get address email from intent.
     *
     * @return address email.
     */
    private String getEmailFromIntentData() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getString(S25_STEP_3_EMAIL_ADDRESS);
        }

        return null;
    }

    /**
     * Get code from intent.
     *
     * @return code.
     */
    private String getCodeFromIntentData() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getString(S25_STEP_3_CODE);
        }

        return null;
    }
}
