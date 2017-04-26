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
 * Screen reset password step 2.
 *
 * @author binh.dt.
 * @since 21-Nov-2016.
 */
public class S25ResetPassword_Step2_Activity extends AppCompatActivity {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S25ResetPassword_Step2_Activity.class.getName();
    private static final int REQUEST_S25_RESET_PASSWORD_STEP_2_DETAIL = 102;
    public static final String S25_STEP_2_EMAIL_ADDRESS = "S25_STEP_2_EMAIL_ADDRESS";

    private ProgressDialog mProgressDialog;

    // For View.
    private AppCompatTextView mTxtCode;
    private Toolbar mToolbar;
    private AppCompatEditText mEdtCode;
    private AppCompatButton mBtnSend;
    private AppCompatTextView mTvTitleToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nếu email nhận được là null thì tắt màn hình và return.
        if (TextUtils.isEmpty(getEmailFromIntentData())) {
            finish();

            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.s25_reset_password_step2_activity);

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
        mTxtCode = (AppCompatTextView) findViewById(R.id.mTxtCode);
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mEdtCode = (AppCompatEditText) findViewById(R.id.mEdtCode);
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
                if (isCodeValid().isEmpty()) {
                    // Nếu có kết nối mạng.
                    if (NetworkUtil.isAvailable(S25ResetPassword_Step2_Activity.this)) {
                        sendCodeAndEmailToServer(getEmailFromIntentData(), mEdtCode.getText().toString());
                        mEdtCode.setText("");
                    } else {
                        MessageDialogUtil.showNotificationDialog(S25ResetPassword_Step2_Activity.this, getString(R.string.common_msg__title__error), getString(R.string.common_msg__content_error__have_no_internet));
                    }
                } else {
                    MessageDialogUtil.showNotificationDialog(S25ResetPassword_Step2_Activity.this, getString(R.string.common_msg__title__warning), isCodeValid().get(0));
                }
            }
        });

        mTxtCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEdtCode.requestFocus();
                KeyboardUtil.showKeyboard(mEdtCode, S25ResetPassword_Step2_Activity.this);
            }
        });
    }

    /**
     * Method send code and email to server.
     *
     * @param email Value email.
     * @param code  Value code.
     */
    private void sendCodeAndEmailToServer(final String email, final String code) {
        mProgressDialog.show();

        OkHttpClient okHttpClient = NetworkUtil.getDefaultHttpClient(this);

        // Chuẩn bị dữ kiện để gửi lên Server.
        RequestBody formBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_EMAIL, email)
                .add(Definition.Request.PARAM_RESET_CODE, code)
                .build();


        Request request = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + Definition.API.CONFIRM_RESET_CODE)
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
                        MessageDialogUtil.showNotificationDialog(S25ResetPassword_Step2_Activity.this, getString(R.string.common_msg__title__error), getString(R.string.s25_reset_password__content_error__reset_password_failure));
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
                            showMessageConfirmCodeSuccess(email, code);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            MessageDialogUtil.showNotificationDialog(S25ResetPassword_Step2_Activity.this, getString(R.string.common_msg__title__error), getString(R.string.s25_reset_password__content_error__reset_password_failure));
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
    private void showMessageConfirmCodeSuccess(final String email, final String code) {
        // Hide keyboard.
        KeyboardUtil.hideKeyboard(mEdtCode, S25ResetPassword_Step2_Activity.this);

        AlertDialog.Builder builder = new AlertDialog.Builder(S25ResetPassword_Step2_Activity.this);
        builder.setMessage(getString(R.string.s25_reset_password__content_info__confirm_reset_code_success));
        builder.setTitle(getString(R.string.common_msg__title__info));
        builder.setCancelable(false);

        builder.setPositiveButton(getString(R.string.common_text__ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                gotoScreenS25ResetPasswordStep3(email, code);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    /**
     * Method is used for checking valid code id format.
     *
     * @return Boolean true for valid false for invalid.
     */
    private List<String> isCodeValid() {
        String code = mEdtCode.getText().toString();
        List<String> errors = new ArrayList<>();

        if (TextUtils.isEmpty(code)) {
            errors.add(getString(R.string.s25_reset_password__txt_reset_code__validation_required));
        } else {
            // Check code min.
            if (code.length() < getResources().getInteger(R.integer.s25_reset_password_txt_reset_code_min_length)) {
                errors.add(getString(R.string.s25_reset_password__txt_reset_code__validation_too_short));
            }

            // Check code max.
            if (code.length() >= getResources().getInteger(R.integer.s25_reset_password_txt_reset_code_max_length)) {
                errors.add(getString(R.string.s25_reset_password__txt_reset_code__validation_too_long));
            }
        }

        return errors;
    }

    /**
     * Chuyển sang bước 3.
     *
     * @param email Địa chỉ email.
     * @param code  Code value.
     */
    private void gotoScreenS25ResetPasswordStep3(String email, String code) {
        Intent intent = new Intent(this, S25ResetPassword_Step3_Activity.class);
        intent.putExtra(S25ResetPassword_Step3_Activity.S25_STEP_3_EMAIL_ADDRESS, email);
        intent.putExtra(S25ResetPassword_Step3_Activity.S25_STEP_3_CODE, code);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivityForResult(intent, REQUEST_S25_RESET_PASSWORD_STEP_2_DETAIL);
    }

    /**
     * Get address email from intent.
     *
     * @return address email.
     */
    private String getEmailFromIntentData() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getString(S25_STEP_2_EMAIL_ADDRESS);
        }

        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_S25_RESET_PASSWORD_STEP_2_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                // Set thông tin đã đổi password thành công khi trở về step 1.
                Intent intent = getIntent();
                setResult(RESULT_OK, intent);
                finish();
            } else {
                finish();
            }
        }
    }
}
