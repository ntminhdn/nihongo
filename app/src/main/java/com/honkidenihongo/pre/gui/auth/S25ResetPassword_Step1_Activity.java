package com.honkidenihongo.pre.gui.auth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.honkidenihongo.pre.common.util.StringUtil;
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
 * Screen reset password step 1.
 *
 * @author binh.dt.
 * @since 21-Nov-2016.
 */
public class S25ResetPassword_Step1_Activity extends AppCompatActivity {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S25ResetPassword_Step1_Activity.class.getName();
    private static final int REQUEST_S25_RESET_PASSWORD_STEP_1_DETAIL = 101;

    private ProgressDialog mProgressDialog;

    // For View.
    private Toolbar mToolbar;
    private AppCompatTextView mTxtEmail;
    private AppCompatEditText mEdtEmail;
    private AppCompatButton mBtnSend;
    private AppCompatTextView mTvTitleToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.s25_reset_password_step1_activity);

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
        mTxtEmail = (AppCompatTextView) findViewById(R.id.mTxtEmail);
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mEdtEmail = (AppCompatEditText) findViewById(R.id.mEdtEmail);
        mBtnSend = (AppCompatButton) findViewById(R.id.mBtnSend);
        mTvTitleToolbar = (AppCompatTextView) mToolbar.findViewById(R.id.mTvTitleToolbar);
        mTvTitleToolbar.setText(getString(R.string.common_app__name));

        // Khởi tạo dialog.
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.common_msg__content_info__processing));
        mProgressDialog.setCancelable(false);
    }

    /**
     * Method set evet for View.
     */
    private void setEvent() {
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEmailValid().isEmpty()) {
                    // Nếu có kết nối mạng.
                    if (NetworkUtil.isAvailable(S25ResetPassword_Step1_Activity.this)) {
                        sendCodeToEmailUser(mEdtEmail.getText().toString().trim());
                        mEdtEmail.setText("");
                    } else {
                        MessageDialogUtil.showNotificationDialog(S25ResetPassword_Step1_Activity.this, getString(R.string.common_msg__title__error), getString(R.string.common_msg__content_error__have_no_internet));
                    }
                } else {
                    MessageDialogUtil.showNotificationDialog(S25ResetPassword_Step1_Activity.this, getString(R.string.common_msg__title__warning), isEmailValid().get(0));
                }
            }
        });

        mTxtEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEdtEmail.requestFocus();
                KeyboardUtil.showKeyboard(mEdtEmail, S25ResetPassword_Step1_Activity.this);
            }
        });
    }

    /**
     * Method get send one code to address email of user.
     *
     * @param email Value email.
     */
    private void sendCodeToEmailUser(final String email) {
        mProgressDialog.show();

        OkHttpClient okHttpClient = NetworkUtil.getDefaultHttpClient(this);

        // Chuẩn bị dữ kiện để gửi lên Server.
        RequestBody formBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_EMAIL, email)
                .build();

        Request request = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + Definition.API.SEND_RESET_CODE)
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
                        MessageDialogUtil.showNotificationDialog(S25ResetPassword_Step1_Activity.this, getString(R.string.common_msg__title__error), getString(R.string.s25_reset_password__content_error__reset_password_failure));
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
                            showMessageSendMailSuccess(email);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MessageDialogUtil.showNotificationDialog(S25ResetPassword_Step1_Activity.this, getString(R.string.common_msg__title__error), getString(R.string.s25_reset_password__content_error__reset_password_failure));
                        }
                    });
                }
            }
        });
    }

    /**
     * Show thông báo đã gởi mã code thành công đến địa chỉ email của người dùng.
     *
     * @param email Value address email.
     */
    private void showMessageSendMailSuccess(final String email) {
        // Hide keyboard.
        KeyboardUtil.hideKeyboard(mEdtEmail, S25ResetPassword_Step1_Activity.this);

        AlertDialog.Builder builder = new AlertDialog.Builder(S25ResetPassword_Step1_Activity.this);
        builder.setMessage(getString(R.string.s25_reset_password__content_info__sending_email_success));
        builder.setTitle(getString(R.string.common_msg__title__info));
        builder.setCancelable(false);

        builder.setPositiveButton(getString(R.string.common_text__ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                gotoScreenS25ResetPasswordStep2(email);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    /**
     * Chuyển sang bước 2.
     *
     * @param email Địa chỉ email.
     */
    private void gotoScreenS25ResetPasswordStep2(String email) {
        Intent intent = new Intent(this, S25ResetPassword_Step2_Activity.class);
        intent.putExtra(S25ResetPassword_Step2_Activity.S25_STEP_2_EMAIL_ADDRESS, email);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivityForResult(intent, REQUEST_S25_RESET_PASSWORD_STEP_1_DETAIL);
    }

    /**
     * Method is used for checking valid email id format.
     *
     * @return Boolean true for valid false for invalid.
     */
    private List<String> isEmailValid() {
        String email = mEdtEmail.getText().toString();
        List<String> errors = new ArrayList<>();

        if (TextUtils.isEmpty(email)) {
            errors.add(getString(R.string.common_auth__txt_email__validation_required));
        } else {
            // Check email min.
            if (email.length() < getResources().getInteger(R.integer.common_txt_email_min_length)) {
                errors.add(getString(R.string.common_auth__txt_email__validation_min_length));
            }

            // Check email max.
            if (email.length() >= getResources().getInteger(R.integer.common_txt_email_max_length)) {
                errors.add(getString(R.string.common_auth__txt_email__validation_max_length));
            }
        }

        if (!StringUtil.isValidEmail(email)) {
            errors.add(getString(R.string.common_auth__txt_email__validation_invalid));
        }

        return errors;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_S25_RESET_PASSWORD_STEP_1_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                finish();
            }
        }
    }
}
