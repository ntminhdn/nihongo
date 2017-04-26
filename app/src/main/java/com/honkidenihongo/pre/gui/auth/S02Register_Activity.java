package com.honkidenihongo.pre.gui.auth;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.api.json.A01AccessTokenGenerator_JsonData;
import com.honkidenihongo.pre.api.json.A03Me_JsonData;
import com.honkidenihongo.pre.api.util.A01AccessTokenGenerator_Util;
import com.honkidenihongo.pre.api.util.A03Me_Util;
import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.DatabaseUtil;
import com.honkidenihongo.pre.common.util.KeyboardUtil;
import com.honkidenihongo.pre.common.util.LanguageCodeUtil;
import com.honkidenihongo.pre.common.util.LocalAppUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.common.util.MessageDialogUtil;
import com.honkidenihongo.pre.common.util.NetworkUtil;
import com.honkidenihongo.pre.common.util.StringUtil;
import com.honkidenihongo.pre.gui.MainActivity;
import com.honkidenihongo.pre.gui.common.S26TermsOfService_Activity;
import com.honkidenihongo.pre.gui.listener.NetworkConnectionCallback;
import com.honkidenihongo.pre.common.util.ToolbarUtil;
import com.honkidenihongo.pre.model.UserModel;
import com.honkidenihongo.pre.model.UserModelUtil;

import org.json.JSONException;
import org.json.JSONObject;

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
 * Created by datpt on 7/6/16.
 */
public class S02Register_Activity extends AppCompatActivity {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S02Register_Activity.class.getName();
    private static final int REQUEST_TERM_OF_SERVICE = 100;

    // For View.
    private Toolbar mToolbar;
    private AppCompatTextView mTvTitle;
    private AppCompatImageView mImgVietnamese;
    private AppCompatImageView mImgEnglish;
    private AppCompatTextView mTxtUsername;
    private AppCompatTextView mTxtEmail;
    private AppCompatTextView mTxtPassword;
    private AppCompatTextView mTxtConfirmPassword;
    private AppCompatEditText mEdtUsername;
    private AppCompatEditText mEdtEmail;
    private AppCompatEditText mEdtPassword;
    private AppCompatEditText mEdtConfirmPassword;
    private AppCompatButton mBtnRegister;
    private AppCompatCheckBox mChbAgree;
    private AppCompatTextView mTvAgreeTerm;
    private AppCompatTextView mLblService;
    private AppCompatTextView mTvTitleToolbar;

    private ProgressDialog mProgressDialog;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Get layout for window.
        setContentView(R.layout.s02_register_activity);

        // Call method initView.
        initView();

        // Init toolbar.
        ToolbarUtil.initToolbar(mToolbar, this);

        // Call method set event for View.
        setEvent();
    }

    /**
     * Phương thức dùng để lắng nghe sự thay đổi ngôn ngữ của app, override ui không cần restart app.
     *
     * @param base Value context.
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * Display follow to laguage current.
         */
        initData();
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
     * Method initView.
     */
    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mTvTitle = (AppCompatTextView) findViewById(R.id.mTvTitle);
        mImgVietnamese = (AppCompatImageView) findViewById(R.id.s02_register_activity_img_vietnamese);
        mImgEnglish = (AppCompatImageView) findViewById(R.id.s02_register_activity_img_english);
        mTxtUsername = (AppCompatTextView) findViewById(R.id.mTxtUsername);
        mTxtEmail = (AppCompatTextView) findViewById(R.id.mTxtEmail);
        mTxtPassword = (AppCompatTextView) findViewById(R.id.mTxtPassword);
        mTxtConfirmPassword = (AppCompatTextView) findViewById(R.id.mTxtConfirmPassword);
        mTvTitleToolbar = (AppCompatTextView) mToolbar.findViewById(R.id.mTvTitleToolbar);

        mEdtUsername = (AppCompatEditText) findViewById(R.id.s02_register_activity_txt_username);
        mEdtEmail = (AppCompatEditText) findViewById(R.id.mEdtEmail);
        mEdtPassword = (AppCompatEditText) findViewById(R.id.s02_register_activity_txt_password);
        mEdtConfirmPassword = (AppCompatEditText) findViewById(R.id.mEdtConfirmPass);

        // Set lại font default cho editText.
        mEdtPassword.setTypeface(Typeface.DEFAULT);
        mEdtConfirmPassword.setTypeface(Typeface.DEFAULT);

        mBtnRegister = (AppCompatButton) findViewById(R.id.mBtnRegister);
        mChbAgree = (AppCompatCheckBox) findViewById(R.id.mChbAgree);
        mTvAgreeTerm = (AppCompatTextView) findViewById(R.id.mTvAgreeTerm);
        mLblService = (AppCompatTextView) findViewById(R.id.s02_register_lbl_service);

        // Add line below text view.
        mTvAgreeTerm.setPaintFlags(mTvAgreeTerm.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
    }

    /**
     * Set data into View.
     */
    private void initData() {
        displayByLanguage();
    }

    /**
     * Hiện thị theo ngông ngữ tiếng anh.
     */
    private void displayByLanguage() {
        if (LocaleHelper.getLanguage(this).equals(Definition.LanguageCode.ENGLISH)) {
            mImgEnglish.setEnabled(false);
            mImgVietnamese.setEnabled(true);

            mImgVietnamese.setAlpha(Definition.Graphic.LIMPIDITY);
            mImgEnglish.setAlpha(Definition.Graphic.BLEAR_NATIONAL_FLAG);
        } else {
            mImgVietnamese.setEnabled(false);
            mImgEnglish.setEnabled(true);

            mImgEnglish.setAlpha(Definition.Graphic.LIMPIDITY);
            mImgVietnamese.setAlpha(Definition.Graphic.BLEAR_NATIONAL_FLAG);
        }

        Context context = LocaleHelper.setLocale(this, LocaleHelper.getLanguage(this));
        Resources resources = context.getResources();


        mTvTitleToolbar.setText(resources.getString(R.string.common_app__name));
        mTvTitle.setText(resources.getString(R.string.s02_register__btn_register));
        mTxtUsername.setText(resources.getString(R.string.common_auth__lbl_username));
        mTxtEmail.setText(resources.getString(R.string.common_auth__lbl_email));
        mTxtPassword.setText(resources.getString(R.string.common_auth__lbl_password));
        mTxtConfirmPassword.setText(resources.getString(R.string.common_auth__lbl_confirm_password));
        mBtnRegister.setText(resources.getString(R.string.s02_register__btn_register));
        mTvAgreeTerm.setText(resources.getString(R.string.s02_register__lbl_terms_of_service));
        mLblService.setText(resources.getString(R.string.s02_register__chk_agree_to_terms_of_service));
    }

    /**
     * Method register.
     */
    private void requestRegister() {
        if (!checkValidate().isEmpty()) {
            // Show dialog lỗi và return.
            MessageDialogUtil.showNotificationDialog(S02Register_Activity.this, getString(R.string.common_msg__title__warning), checkValidate().get(0));

            return;
        }

        String registerUrl = AppConfig.getApiBaseUrl() + Definition.API.REGISTER;
        Log.d(LOG_TAG, "RegisterURL: " + registerUrl);

        // Khởi tạo và show dialog.
        mProgressDialog = new ProgressDialog(S02Register_Activity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.common_msg__content_info__processing));
        mProgressDialog.show();

        OkHttpClient client = NetworkUtil.getDefaultHttpClient(this);
        RequestBody formBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_USER_NAME, mEdtUsername.getText().toString())
                .add(Definition.Request.PARAM_EMAIL, mEdtEmail.getText().toString())
                .add(Definition.Request.PARAM_PASSWORD, mEdtPassword.getText().toString())
                .add(Definition.Request.PARAM_LANGUAGE_CODE, LanguageCodeUtil.getLanguageCode(this))
                .build();

        Request request = new Request.Builder()
                .url(registerUrl)
                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showMessageErrorRegister(getString(R.string.s02_register__content_error__register_failure));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    // Read data on the worker thread.
                    final String responseData = response.body().string();
                    handleRegisterData(responseData);
                } else {
                    showMessageErrorRegister(getString(R.string.s02_register__content_error__register_failure));
                }
            }
        });
    }

    /**
     * Method call request AccessToken.
     */
    private void requestGetAccessToken() {
        // Handle login thì set message cho dialog đang register thành login.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.setMessage(getString(R.string.common_msg__content_info__processing));
                }
            }
        });

        OkHttpClient client = NetworkUtil.getDefaultHttpClient(this);

        RequestBody formBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_USER_NAME, mEdtUsername.getText().toString())
                .add(Definition.Request.PARAM_PASSWORD, mEdtPassword.getText().toString())
                .add(Definition.Request.PARAM_GRANT_TYPE, Definition.Constants.GRANT_TYPE)
                .add(Definition.Request.PARAM_CLIENT_ID, AppConfig.getClientId())
                .add(Definition.Request.PARAM_CLIENT_SECRET, AppConfig.getClientSecret())
                .build();

        Request request = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + Definition.API.GET_ACCESS_TOKEN)
                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                .header(Definition.Request.PARAM_CLIENT_TOKEN, AppConfig.getClientToken())
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showMessageErrorRegister(getString(R.string.s02_register__content_error__register_failure));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    final String responseData = response.body().string();
                    // Khởi tạo dữ liệu dạng đối tượng cần lấy.

                    if (!TextUtils.isEmpty(responseData)) {
                        final A01AccessTokenGenerator_JsonData tokenInfo = A01AccessTokenGenerator_Util.parseData(responseData);
                        // Nếu dữ liệu trả về hợp lệ, tức là bước đầu đã đăng nhập thành công.
                        if (tokenInfo != null) {
                            /* Request lần 2 lên API Server để lấy thông tin User Information. */
                            requestAsyncGetUserInfo(tokenInfo);
                        } else {
                            showMessageErrorRegister(getString(R.string.s02_register__content_error__register_failure));
                        }
                        // handleLoginData(responseData);
                    }
                } else {
                    showMessageErrorRegister(getString(R.string.s02_register__content_error__register_failure));
                }
            }
        });
    }

    /**
     * Lấy thông tin của User sử dụng API Server, kết nối theo cơ chế Asynchronous.<br/>
     * {@link S01Login_Activity#requestAsyncLoginUsingServer(String, String)}
     *
     * @param tokenInfo The token information.
     */
    private void requestAsyncGetUserInfo(final A01AccessTokenGenerator_JsonData tokenInfo) {
        // Chuẩn bị dữ kiện để gửi lên Server.
        Request userInfoRequest = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + Definition.API.GET_USER)
                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                .header(Definition.Request.HEADER_AUTHORIZATION, String.format("%s %s", Definition.Request.HEADER_BEARER2, tokenInfo.access_token))
                .header(Definition.Request.PARAM_CLIENT_TOKEN, AppConfig.getClientToken())
                .build();

        OkHttpClient httpClient = NetworkUtil.getDefaultHttpClient(this);

        // Tiến hành gửi request bất đồng bộ (asynchronous) lên Server.
        httpClient.newCall(userInfoRequest).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Khởi tạo dữ liệu dạng đối tượng cần lấy.
                A03Me_JsonData a03Me_JsonData = null;

                // Nếu dữ liệu Response trả về OK.
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    a03Me_JsonData = A03Me_Util.parseData(response.body().string());
                }

                // Nếu lấy được thông tin, tức là đã Đăng Nhập thành công.
                if (a03Me_JsonData != null) {
                    // Convert to UserModel object.
                    UserModel userModel = UserModelUtil.makeFrom(a03Me_JsonData);
                    userModel.authType = Definition.AuthType.SYSTEM_SERVER;

                    /* Token information. */
                    userModel.tokenInfo = new A01AccessTokenGenerator_JsonData();
                    userModel.tokenInfo.access_token = tokenInfo.access_token;
                    userModel.tokenInfo.refresh_token = tokenInfo.refresh_token;
                    userModel.tokenInfo.expires_in = tokenInfo.expires_in;

                    // Lưu (asynchronous) thông tin User xuống Local: SharedPreferences.
                    //userModel.expires = tokenGeneratorData.expires_in * 1000 + System.currentTimeMillis();
                    LocalAppUtil.saveLastLoginUserInfo(S02Register_Activity.this, userModel);

                    // Nếu lấy thành công thông tin của user info thì goto screen main.
                    gotoScreenMain();
                } else {
                    showMessageErrorRegister(getString(R.string.s02_register__content_error__register_failure));
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                showMessageErrorRegister(getString(R.string.s02_register__content_error__register_failure));
            }
        });
    }

    /**
     * Đi đến màn hình chính sau khi đã đăng kí thành công và handle login ok.
     */
    private void gotoScreenMain() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Tắt dialog đang show đi.
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }

                // Chú ý tương tự như màn hình login trước khi người dùng goto tới màn hình main thì phải cấu hình lại file realm cho từng user login.
                // Để tránh trường hợp user đang online logout và đăng ký tài khoản mới, app sẽ sử dụng file realm của user đăng nhập lúc trước.
                // Trước khi chuyển đến màn hình chính cấu hình file realm database theo user login hiện tại.
                DatabaseUtil.configRealmDatabase(S02Register_Activity.this);

                // Đi đến màn hình chính và chắc chắn rằng không còn màn hình đăng ký hay đăng nhập trước đó.
                Intent intent = new Intent(S02Register_Activity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
    }

    /**
     * Phương thức dùng để show thông báo lỗi khi đăng kí đến user.
     *
     * @param message Value content Message.
     */
    private void showMessageErrorRegister(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Tắt dialog đang show đi.
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }

                // Hiển thị dialog thông báo lỗi.
                MessageDialogUtil.showNotificationDialog(S02Register_Activity.this, getString(R.string.common_msg__title__error), message);
            }
        });
    }

    /**
     * Todo hiện tại phương thức này chưa dùng đến để call refresh token.
     * Method handle login after register ok.
     *
     * @param loginData Value String.
     */
    private void handleLoginData(String loginData) {
        try {
            JSONObject loginJsonObject = new JSONObject(loginData);

            if (loginJsonObject.has(Definition.Response.DATA)) {
                JSONObject loginDataJson = loginJsonObject.getJSONObject(Definition.Response.DATA);

                String access_token = loginDataJson.getString(Definition.Response.ACCESS_TOKEN);
                String refresh_token = loginDataJson.getString(Definition.Response.REFRESH_TOKEN);
                long expires = loginDataJson.getLong(Definition.Response.EXPIRES_IN) * 1000 + System.currentTimeMillis();

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(Definition.SharedPreferencesKey.ACCESS_TOKEN, access_token);
                editor.putString(Definition.SharedPreferencesKey.REFRESH_TOKEN, refresh_token);
                editor.putLong(Definition.SharedPreferencesKey.EXPIRES_IN, expires);
                editor.apply();

                // Setup alarm to refresh token when it is expire.
                Intent alarmIntent = new Intent(Definition.Constants.ACTION_REFRESH_TOKEN);
                PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, Definition.Constants.REQ_REFRESH_TOKEN, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                long alarmDelay = loginDataJson.getLong(Definition.Response.EXPIRES_IN) * 1000;
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmDelay, alarmPendingIntent);

                // Đi đến màn hình chính và chắc chắn rằng không còn màn hình đăng ký hay đăng nhập trước đó.
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            } else {
//                JSONObject errJsonObject = loginJsonObject.getJSONObject(Definition.Response.ERRORS);
//                String errMsg = errJsonObject.getString(Definition.Response.TITLE);
//                MessageDialogUtil.showNotificationDialog(S02Register_Activity.this, getString(R.string.s02_register_message_error_title), errMsg);

                // Lấy message từ local để hiện thị theo ngôn ngữ hiện tại của app.
                showMessageErrorRegister(getString(R.string.s02_register__content_error__register_failure));
            }
        } catch (JSONException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
    }

    /**
     * Method kiểm tra thông tin đăng ký.
     *
     * @return true or false.
     */
    private List<String> checkValidate() {
        List<String> errors = new ArrayList<>();

        String username = mEdtUsername.getText().toString();
        String email = mEdtEmail.getText().toString();
        String password = mEdtPassword.getText().toString();
        String passwordConfirm = mEdtConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(username)) {
            errors.add(getString(R.string.common_auth__txt_username__validation_required));
        } else {
            // Check username min.
            if (username.length() < getResources().getInteger(R.integer.common_txt_username_min_length)) {
                errors.add(getString(R.string.common_auth__txt_username__validation_min_length));
            }

            // Check username max.
            if (username.length() >= getResources().getInteger(R.integer.common_txt_username_max_length)) {
                errors.add(getString(R.string.common_auth__txt_username__validation_max_length));
            }

            if (!StringUtil.isValidUserName(username)) {
                errors.add(getString(R.string.common_auth__txt_username__validation_invalid));
            }
        }

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

        if (TextUtils.isEmpty(passwordConfirm)) {
            errors.add(getString(R.string.common_auth__txt_confirm_password__validation_required));
        } else {
            // Check passwordConfirm min.
            if (passwordConfirm.length() < getResources().getInteger(R.integer.common_txt_password_min_length)) {
                errors.add(getString(R.string.common_auth__txt_confirm_password__validation_min_length));
            }

            // Check passwordConfirm max.
            if (passwordConfirm.length() >= getResources().getInteger(R.integer.common_txt_password_max_length)) {
                errors.add(getString(R.string.common_auth__txt_confirm_password__validation_max_length));
            }
        }

        if (!password.equals(passwordConfirm)) {
            errors.add(getString(R.string.common_auth__txt_confirm_password__validation_not_match));
        }

        if (!mChbAgree.isChecked()) {
            errors.add(getString(R.string.s02_register__content_error__un_check_terms));
        }

        return errors;
    }

    /**
     * Method get info register.
     *
     * @param registerData Value String.
     */
    private void handleRegisterData(String registerData) {
        try {
            JSONObject registerJsonObject = new JSONObject(registerData);
            boolean isSuccess = registerJsonObject.getBoolean(Definition.Response.SUCCESS);

            if (!isSuccess) {
                String errMsg = registerJsonObject.getString(Definition.Response.TITLE);
                showMessageErrorRegister(getString(R.string.s02_register__content_error__register_failure));
            } else {
                requestGetAccessToken();
                //               JSONObject userJsonObj = registerJsonObject.getJSONObject(Definition.Response.DATA);
                //              SharedPreferences.Editor editor = mSharedPreferences.edit();

                //               editor.putInt(Definition.SharedPreferencesKey.USER_ID, userJsonObj.getInt(Definition.Response.ID));
                //              editor.putString(Definition.SharedPreferencesKey.USER_EMAIL, userJsonObj.getString(Definition.Response.EMAIL));
                //              editor.putString(Definition.SharedPreferencesKey.USER_NAME, userJsonObj.getString(Definition.Response.USER_NAME));
//                editor.putString(Define.SharedPreferencesKey.FULL_NAME, mEdtFullName.getText().toString());
                //               editor.putBoolean(Definition.SharedPreferencesKey.HAS_PASSWORD, true);
                //               editor.putBoolean(Definition.SharedPreferencesKey.FACEBOOK_CONNECTED, false);

                /*editor.putInt(Define.SharedPreferences.USER_ID, userJsonObj.getInt(Define.Response.ID));
                editor.putString(Define.SharedPreferences.USER_EMAIL, userJsonObj.getString(Define.Response.EMAIL));
                editor.putString(Define.SharedPreferences.USER_NAME, userJsonObj.getString(Define.Response.USER_NAME));
                editor.putBoolean(Define.SharedPreferences.HAS_PASSWORD, true);
                editor.putBoolean(Define.SharedPreferences.FACEBOOK_CONNECTED, false);
*/
                //              editor.apply();
            }
        } catch (JSONException e) {
            showMessageErrorRegister(getString(R.string.s02_register__content_error__register_failure));
            Log.d(LOG_TAG, e.getMessage());
        }
    }

    /**
     * Set sự kiện for View.
     */
    private void setEvent() {
        mImgVietnamese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocaleHelper.setLocale(S02Register_Activity.this, Definition.LanguageCode.VIETNAMESE);
                displayByLanguage();
            }
        });

        mImgEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocaleHelper.setLocale(S02Register_Activity.this, Definition.LanguageCode.ENGLISH);
                displayByLanguage();
            }
        });

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkRegister();
            }
        });

        mTvAgreeTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToScreenS15S26TermOfService();
            }
        });

        mLblService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChbAgree.setChecked(!mChbAgree.isChecked());
            }
        });

        mTxtUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEdtUsername.requestFocus();
                KeyboardUtil.showKeyboard(mEdtUsername, S02Register_Activity.this);
            }
        });

        mTxtEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEdtEmail.requestFocus();
                KeyboardUtil.showKeyboard(mEdtEmail, S02Register_Activity.this);
            }
        });

        mTxtPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEdtPassword.requestFocus();
                KeyboardUtil.showKeyboard(mEdtPassword, S02Register_Activity.this);
            }
        });

        mTxtConfirmPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEdtConfirmPassword.requestFocus();
                KeyboardUtil.showKeyboard(mEdtConfirmPassword, S02Register_Activity.this);
            }
        });
    }

    /**
     * Go to screen S26TermOfService.
     */
    private void goToScreenS15S26TermOfService() {
        Intent intent = new Intent(this, S26TermsOfService_Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivityForResult(intent, REQUEST_TERM_OF_SERVICE);
    }

    /**
     * Phương thức check register với network.
     */
    private void checkRegister() {
        if (NetworkUtil.isAvailable(this)) {
            requestRegister();
        } else {
            MessageDialogUtil.showNetworkUnavailableDialog(S02Register_Activity.this, new NetworkConnectionCallback() {
                @Override
                public void onTryAgain() {
                    checkRegister();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TERM_OF_SERVICE) {
            if (resultCode == Activity.RESULT_OK) {
                mChbAgree.setChecked(true);
            }
        }
    }
}
