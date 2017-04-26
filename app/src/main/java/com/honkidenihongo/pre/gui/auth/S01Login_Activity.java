package com.honkidenihongo.pre.gui.auth;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.api.json.A01AccessTokenGenerator_JsonData;
import com.honkidenihongo.pre.api.json.A03Me_JsonData;
import com.honkidenihongo.pre.api.util.A01AccessTokenGenerator_Util;
import com.honkidenihongo.pre.api.util.A03Me_Util;
import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.ApplicationUpdateUtil;
import com.honkidenihongo.pre.common.util.DatabaseUtil;
import com.honkidenihongo.pre.common.util.IoUtil;
import com.honkidenihongo.pre.common.util.KeyboardUtil;
import com.honkidenihongo.pre.common.util.LanguageCodeUtil;
import com.honkidenihongo.pre.common.util.LocalAppUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.common.util.LoginUtil;
import com.honkidenihongo.pre.common.util.LogoutUtil;
import com.honkidenihongo.pre.common.util.MessageDialogUtil;
import com.honkidenihongo.pre.common.util.NetworkUtil;
import com.honkidenihongo.pre.common.util.StringUtil;
import com.honkidenihongo.pre.gui.MainActivity;
import com.honkidenihongo.pre.gui.listener.NetworkConnectionCallback;
import com.honkidenihongo.pre.model.UserModel;
import com.honkidenihongo.pre.model.UserModelUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * The Login Screen: S01Login.
 *
 * @author long.tt.
 * @since 28-Nov-2016.
 */
public class S01Login_Activity extends AppCompatActivity {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S01Login_Activity.class.getName();

    // View.
    private AppCompatImageView mImgVietnamese;
    private AppCompatImageView mImgEnglish;

    private AppCompatTextView mLblUsername;
    private AppCompatEditText mTxtUsername;

    private AppCompatTextView mLblPassword;
    private AppCompatEditText mTxtPassword;

    private AppCompatTextView mLblForgotPassword;
    private AppCompatTextView mLblRegister;

    private AppCompatButton mBtnLoginSystemServer;
    private LoginButton mBtnLoginFacebook;

    // Login with Facebook.
    private CallbackManager mCallbackManager;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Get layout for window.
        setContentView(R.layout.s01_login_activity);

        //Call method initView.
        initView();

        // Call method set event for View.
        setEvent();

        // Call method init loginFacebook.
        initLoginWithFacebook();
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
    protected void onResume() {
        super.onResume();

        /**
         * Set data into WebView.
         */
        displayByLanguage();
    }

    /**
     * Method initView.
     */
    private void initView() {
        // Chọn ngôn ngữ.
        mImgVietnamese = (AppCompatImageView) findViewById(R.id.s01_login_activity_img_vietnamese);
        mImgEnglish = (AppCompatImageView) findViewById(R.id.s01_login_activity_img_english);

        // Thông tin đăng nhập.
        mLblUsername = (AppCompatTextView) findViewById(R.id.s01_login_activity_lbl_username);
        mTxtUsername = (AppCompatEditText) findViewById(R.id.s01_login_activity_txt_username);

        mLblPassword = (AppCompatTextView) findViewById(R.id.s01_login_activity_lbl_password);
        mTxtPassword = (AppCompatEditText) findViewById(R.id.s01_login_activity_txt_password);
        mTxtPassword.setTypeface(Typeface.DEFAULT);

        mLblForgotPassword = (AppCompatTextView) findViewById(R.id.s01_login_activity_lbl_forgot_password);
        mLblRegister = (AppCompatTextView) findViewById(R.id.s01_login_activity_lbl_register);

        // Add line below text view.
        mLblForgotPassword.setPaintFlags(mLblForgotPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // Add line below text view.
        mLblRegister.setPaintFlags(mLblRegister.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // Button Login sử dụng System Server.
        mBtnLoginSystemServer = (AppCompatButton) findViewById(R.id.s01_login_activity_btn_login_system_server);

        // Button Login sử dụng Facebook.
        mBtnLoginFacebook = (LoginButton) findViewById(R.id.s01_login_activity_btn_login_facebook);

        // Khởi tạo đi dialog.
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.common_msg__content_info__processing));
        mProgressDialog.setCancelable(false);
    }


    /**
     * Set data into View.
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

        mLblUsername.setText(resources.getString(R.string.common_auth__lbl_username));
        mLblPassword.setText(resources.getString(R.string.common_auth__lbl_password));

        mLblForgotPassword.setText(resources.getString(R.string.common_auth__lbl_reset_password));
        mLblRegister.setText(resources.getString(R.string.s01_login__lbl_register));

        mBtnLoginSystemServer.setText(resources.getString(R.string.s01_login__btn_login));
        mBtnLoginFacebook.setText(resources.getString(R.string.s01_login__btn_facebook_login));
    }

    /**
     * Method set event for View.
     */
    private void setEvent() {
        mImgVietnamese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocaleHelper.setLocale(S01Login_Activity.this, Definition.LanguageCode.VIETNAMESE);
                displayByLanguage();
//                restart();
            }
        });

        mImgEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocaleHelper.setLocale(S01Login_Activity.this, Definition.LanguageCode.ENGLISH);
//                restart();
                displayByLanguage();
            }
        });

        mBtnLoginSystemServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cứ click Login là xóa thông tin cũ. Đây là bước để cẩn thận security chống hack.
                LocalAppUtil.deleteLastAppInfo(S01Login_Activity.this);

                // Xóa thông tin facebook đã lấy trước đó nếu có để UI button login with facebook set lại trạng thái đăng nhập.
                try {
                    LoginManager.getInstance().logOut();
                } catch (FacebookException e) {
                    Log.d(LOG_TAG, e.getMessage());
                }
                // Xử lý sự kiện click vào Login.
                loginUsingServer_Clicked();
            }
        });

        mLblUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTxtUsername.requestFocus();
                KeyboardUtil.showKeyboard(mTxtUsername, S01Login_Activity.this);
            }
        });

        mLblPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTxtPassword.requestFocus();
                KeyboardUtil.showKeyboard(mTxtPassword, S01Login_Activity.this);
            }
        });

        mLblForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleForgotPassword();
            }
        });

        mLblRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleRegister();
            }
        });

        mBtnLoginFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cứ click Login là xóa thông tin cũ. Đây là bước để cẩn thận security chống hack.
                LocalAppUtil.deleteLastAppInfo(S01Login_Activity.this);

                // Xóa thông tin facebook đã lấy trước đó nếu có để UI button login with facebook set lại trạng thái đăng nhập.
                try {
                    LoginManager.getInstance().logOut();
                } catch (FacebookException e) {
                    Log.d(LOG_TAG, e.getMessage());
                }
            }
        });

        mTxtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    // Xử lý sự kiện click vào action done trên bàn phím ảo tăng tính trải nghiệm người không cần hạ bàn phím ảo mới bấm login.
                    loginUsingServer_Clicked();
                }

                return false;
            }
        });

    }

    // Todo not restart app.
//    /**
//     * Run app again.
//     */
//    private void restart() {
//        // Chỉ mở lại activity login ko chạy lại splash.
//        Intent intent = new Intent(S01Login_Activity.this, S01Login_Activity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
//        startActivity(intent);
//    }

    /**
     * Method init login with Facebook.
     */
    private void initLoginWithFacebook() {
        // Thường xuyên theo dõi tên các Permissions trên Facebook: https://developers.facebook.com/docs/facebook-login/permissions#permissions
        List<String> abc = new ArrayList<>();

        mCallbackManager = CallbackManager.Factory.create();
        mBtnLoginFacebook.setReadPermissions("public_profile", "email");
        mBtnLoginFacebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                final AccessToken accessToken = loginResult.getAccessToken();

                GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        // Check kiểm tra kết nối mạng.
                        if (NetworkUtil.isAvailable(S01Login_Activity.this)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Lấy access token của facebook để đưa lên server app.
                                    String access_token_facebook = accessToken.getToken();

                                    // Thực hiện request.
                                    requestLoginFacebook(access_token_facebook);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MessageDialogUtil.showNetworkUnavailableDialog(S01Login_Activity.this, new NetworkConnectionCallback() {
                                                @Override
                                                public void onTryAgain() {
                                                    onSuccess(loginResult);
                                                }
                                            }
                                    );
                                }
                            });
                        }
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link,cover,email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d(LOG_TAG, "onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(LOG_TAG, error.getMessage());
            }
        });
    }

    /**
     * Process when the Login button is clicked.
     */
    private void loginUsingServer_Clicked() {
        // Đầu tiên cần validate form với hộp thoại hiển thị nếu không hợp lệ.
        if (!checkValidate().isEmpty()) {
            MessageDialogUtil.showNotificationDialog(S01Login_Activity.this, getString(R.string.common_msg__title__warning), checkValidate().get(0));

            return;
        }

        if (NetworkUtil.isAvailable(S01Login_Activity.this)) {
            // Lấy thông tin đăng nhập.
            String username = mTxtUsername.getText().toString();
            String password = mTxtPassword.getText().toString();

            // Thực hiện request đăng nhập.
            requestAsyncLoginUsingServer(username, password);
        } else {
            MessageDialogUtil.showNetworkUnavailableDialog(
                    S01Login_Activity.this,
                    new NetworkConnectionCallback() {
                        @Override
                        public void onTryAgain() {
                            loginUsingServer_Clicked();
                        }
                    }
            );
        }
    }

    /**
     * Request login with Facebook.
     *
     * @param access_token Value token.
     */
    private void requestLoginFacebook(String access_token) {
        // Cẩn thận luôn khởi tạo mới.
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.common_msg__content_info__processing));
            mProgressDialog.setCancelable(false);
        }

        // Handle login facebook.
        mProgressDialog.show();

        OkHttpClient client = NetworkUtil.getDefaultHttpClient(this);

        RequestBody formBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_FACEBOOK_ACCESS_TOKEN, access_token)
                .add(Definition.Request.PARAM_CLIENT_ID, AppConfig.getClientId())
                .add(Definition.Request.PARAM_CLIENT_SECRET, AppConfig.getClientSecret())
                .build();

        Request request = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + Definition.API.FACEBOOK_LOGIN)
                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                .header(Definition.Request.PARAM_CLIENT_TOKEN, AppConfig.getClientToken())
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();

                // Xóa thông tin facebook đã lấy trước đó nếu có để UI button login with facebook set lại trạng thái đăng nhập.
                try {
                    LoginManager.getInstance().logOut();
                } catch (FacebookException e1) {
                    Log.d(LOG_TAG, e1.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mProgressDialog.dismiss();

                // Nếu request thành công và có data.
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    // Xử lý data trả về từ server để lưu thông tin đăng nhập với facebook dưới local.
                    handleLoginFacebookData(response.body().string());
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Show thông báo lỗi login cho người dùng.
                            MessageDialogUtil.showNotificationDialog(S01Login_Activity.this, getString(R.string.common_msg__title__error), getString(R.string.s01_login__content_error__login_failure));

                            // Xóa thông tin facebook đã lấy trước đó nếu có để UI button login with facebook set lại trạng thái đăng nhập.
                            try {
                                LoginManager.getInstance().logOut();
                            } catch (FacebookException e) {
                                Log.d(LOG_TAG, e.getMessage());
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Login sử dụng API Server. Việc Login được gọi đến API Server theo cơ chế Asynchronous.
     * <pre>
     * Quá trình Login qua các bước như sau:
     * <b>Step 1.</b> Bật Dialog ngăn người dùng thao tác, chờ request lên API Server (sẽ có 2 thao tác kết nối đến API Server).
     * <b>Step 2.</b> Request lần 1: Gọi API để có được Access-Token <b>thành công</b> và <b>hợp lệ</b>.
     * <b>Step 3.</b> Request lần 2: Gọi API để có được User Profile Information <b>thành công</b> và <b>hợp lệ</b>.
     * Việc Login chỉ được gọi là thành công nếu cả 2 thao tác trên <b>thành công</b> và <b>hợp lệ</b>.
     * Nếu Login thành công thì lưu lại thông tin User Profile cơ bản rồi đi tới màn hình Dashboard.
     * Nếu Login không thành công thì hiển thị thông báo lỗi.
     * Sẽ có 4 trường hợp xảy ra lỗi:
     *      - Việc request lần 1 (lấy Access-Token) thất bại.
     *      - Việc request lần 1 (lấy Access-Token) OK, nhưng dữ liệu lấy được không <b>hợp lệ</b>.
     *      - Việc request lần 2 (lấy User Profile Information) thất bại.
     *      - Việc request lần 2 (lấy User Profile Information) OK, nhưng dữ liệu lấy được không <b>hợp lệ</b>.
     * <b>Step 4.</b> Tắt Dialog chờ, tiếp tục cho người dùng thao tác.
     * </pre>
     *
     * @param username The username.
     * @param password The password.
     */
    private void requestAsyncLoginUsingServer(String username, String password) {
        /* Bước 1: Bật hộp thoại chờ request lên server. */
        // Cẩn thận luôn khởi tạo mới.
        mProgressDialog.show();

        /* Bước 2: Request lần 1 lên API Server để lấy thông tin Access-Token. */
        // Chuẩn bị dữ kiện để gửi lên Server.
        RequestBody loginRequestBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_GRANT_TYPE, Definition.Constants.GRANT_TYPE)
                .add(Definition.Request.PARAM_CLIENT_ID, AppConfig.getClientId())
                .add(Definition.Request.PARAM_CLIENT_SECRET, AppConfig.getClientSecret())
                .add(Definition.Request.PARAM_USER_NAME, username)
                .add(Definition.Request.PARAM_PASSWORD, password)
                .add(Definition.Request.PARAM_LANGUAGE_CODE, LanguageCodeUtil.getLanguageCode(this))
                .build();

        Request loginRequest = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + Definition.API.GET_ACCESS_TOKEN)
                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                .header(Definition.Request.PARAM_CLIENT_TOKEN, AppConfig.getClientToken())
                .post(loginRequestBody)
                .build();

        // Get OkHttpClient object with default timeout configurations.
        OkHttpClient httpClient = NetworkUtil.getDefaultHttpClient(this);

        // Tiến hành gửi request bất đồng bộ (asynchronous) lên Server.
        httpClient.newCall(loginRequest).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Khởi tạo dữ liệu dạng đối tượng cần lấy.
                A01AccessTokenGenerator_JsonData tokenInfo = null;

                // Nếu dữ liệu Response trả về OK.
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    tokenInfo = A01AccessTokenGenerator_Util.parseData(response.body().string());
                }

                // Nếu dữ liệu trả về hợp lệ, tức là bước đầu đã đăng nhập thành công.
                if (tokenInfo != null) {
                    /* Bước 3: Request lần 2 lên API Server để lấy thông tin User Information. */
                    requestAsyncGetUserInfo(tokenInfo);
                } else {
                    // Todo...
                    // Khi đăng nhập thất bại.
                    // Thoát cả facebook nếu có ra.
//                    LoginManager.getInstance().logOut();

                    /* Bước 4: Tắt Dialog chờ, tiếp tục cho người dùng thao tác. */
                    final String errorMessage = getString(R.string.s01_login__content_error__login_failure);
                    processUiWhenLoginFail(errorMessage);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // Show the log in development environment.
                Log.e(LOG_TAG, "requestAsyncLoginUsingServer()-> onFailure(): " + e.getMessage());

                /* Bước 4: Tắt Dialog chờ, tiếp tục cho người dùng thao tác. */
                final String errorMessage = getString(R.string.s01_login__content_error__login_failure);
                processUiWhenLoginFail(errorMessage);
            }
        });
    }

    /**
     * Go to screen S25ResetPassword.
     */
    private void handleForgotPassword() {
        Intent intent = new Intent(this, S25ResetPassword_Step1_Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivityForResult(intent, 0);
    }

    /**
     * Go to screen S02Register.
     */
    private void handleRegister() {
        Intent registerIntent = new Intent(this, S02Register_Activity.class);
        registerIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(registerIntent);
    }

    /**
     * Lấy thông tin của User sử dụng API Server, kết nối theo cơ chế Asynchronous.<br/>
     * {@link S01Login_Activity#requestAsyncLoginUsingServer(String, String)}
     *
     * @param tokenInfo The token information.
     */
    private void requestAsyncGetUserInfo(final A01AccessTokenGenerator_JsonData tokenInfo) {
           /* Bước 1: Bật hộp thoại chờ request lên server. */
        // Cẩn thận luôn khởi tạo mới.
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.common_msg__content_info__processing));
            mProgressDialog.setCancelable(false);
        }

        mProgressDialog.show();

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

                    //                Realm myOtherRealm =
//                        Realm.getInstance(
//                                new RealmConfiguration().Builder(context)
//                                        .name("myOtherRealm.realm")
//                                        .build()
//                        );

//                // Todo: Xóa dữ liệu của User cũ khác User hiện tại.
//                int oldUserID = mSharedPreferences.getInt(Define.SharedPreferencesKey.LAST_CONNECTION_USER_ID, -1);
//                if (oldUserID != userInfoJsonObject.getInt(Define.Response.ID)) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mRealm.executeTransactionAsync(new Realm.Transaction() {
//                                @Override
//                                public void execute(Realm realm) {
//                                    realm.deleteAll();
//                                }
//                            });
//                        }
//                    });
//
//                    String logUrl = AppConfig.getApiBaseUrl() + Define.API.LOG_DOWNLOAD;
//                    requestDownloadLog(logUrl, true);
//                } else {
//                    Intent intent = new Intent(this, MainActivity.class);
//                    startActivity(intent);
//                    finish();
//                }
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
                    LocalAppUtil.saveLastLoginUserInfo(S01Login_Activity.this, userModel);

//                    // Todo: Setup alarm to refresh token when it is expire
//                    Intent alarmIntent = new Intent(Define.Constants.ACTION_REFRESH_TOKEN);
//                    PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
//                            S01Login_Activity.this,
//                            Define.Constants.REQ_REFRESH_TOKEN,
//                            alarmIntent,
//                            PendingIntent.FLAG_UPDATE_CURRENT
//                    );
//
//                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//                    long alarmDelay = userModel.tokenInfo.expires_in * 1000;
//                    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmDelay, alarmPendingIntent);
//
//                    // Todo...
//                    // requestSendDeviceToken(FirebaseInstanceId.getInstance().getToken(), tokenInfo.accessToken);

                    processUiWhenLoginSuccess();
                } else {
                    /* Bước 4: Tắt Dialog chờ, tiếp tục cho người dùng thao tác. */
                    final String errorMessage = getString(R.string.s01_login__content_error__login_failure);
                    processUiWhenLoginFail(errorMessage);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // Show the log in development environment.
                Log.e(LOG_TAG, "requestAsyncGetUserInfo()-> onFailure(): " + e.getMessage());

                /* Bước 4: Tắt Dialog chờ, tiếp tục cho người dùng thao tác. */
                final String errorMessage = getString(R.string.s01_login__content_error__login_failure);
                processUiWhenLoginFail(errorMessage);
            }

        });
    }

    /**
     * Lấy thông tin của User sử dụng API Server, kết nối theo cơ chế Asynchronous.<br/>
     * {@link S01Login_Activity#requestAsyncLoginUsingServer(String, String)}
     *
     * @param tokenInfo The token information.
     */
    private void requestAsyncGetUserInfoWithFacebook(final A01AccessTokenGenerator_JsonData tokenInfo) {
          /* Bước 1: Bật hộp thoại chờ request lên server. */
        // Cẩn thận luôn khởi tạo mới.
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.common_msg__content_info__processing));
            mProgressDialog.setCancelable(false);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.show();
            }
        });

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
                    userModel.authType = Definition.AuthType.FACEBOOK;

                    /* Token information. */
                    userModel.tokenInfo = new A01AccessTokenGenerator_JsonData();
                    userModel.tokenInfo.access_token = tokenInfo.access_token;
                    userModel.tokenInfo.refresh_token = tokenInfo.refresh_token;
                    userModel.tokenInfo.expires_in = tokenInfo.expires_in;

                    // Lưu (asynchronous) thông tin User xuống Local: SharedPreferences.
                    //userModel.expires = tokenGeneratorData.expires_in * 1000 + System.currentTimeMillis();
                    LocalAppUtil.saveLastLoginUserInfo(S01Login_Activity.this, userModel);

//                    // Todo: Setup alarm to refresh token when it is expire
//                    Intent alarmIntent = new Intent(Define.Constants.ACTION_REFRESH_TOKEN);
//                    PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
//                            S01Login_Activity.this,
//                            Define.Constants.REQ_REFRESH_TOKEN,
//                            alarmIntent,
//                            PendingIntent.FLAG_UPDATE_CURRENT
//                    );
//
//                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//                    long alarmDelay = userModel.tokenInfo.expires_in * 1000;
//                    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmDelay, alarmPendingIntent);
//
//                    // Todo...
//                    // requestSendDeviceToken(FirebaseInstanceId.getInstance().getToken(), tokenInfo.accessToken);

                    processUiWhenLoginSuccess();
                } else {
                    /* Bước 4: Tắt Dialog chờ, tiếp tục cho người dùng thao tác. */
                    final String errorMessage = getString(R.string.s01_login__content_error__login_failure);
                    processUiWhenLoginFail(errorMessage);

                    // Xóa thông tin facebook đã lấy trước đó nếu có để UI button login with facebook set lại trạng thái đăng nhập.
                    try {
                        LoginManager.getInstance().logOut();
                    } catch (FacebookException e) {
                        Log.d(LOG_TAG, e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // Show the log in development environment.
                Log.e(LOG_TAG, "requestAsyncGetUserInfo()-> onFailure(): " + e.getMessage());

                /* Bước 4: Tắt Dialog chờ, tiếp tục cho người dùng thao tác. */
                final String errorMessage = getString(R.string.s01_login__content_error__login_failure);
                processUiWhenLoginFail(errorMessage);
            }
        });
    }

    /**
     * Chạy trong UI Thread để xử lý GUI khi Login thành công.
     */
    private void processUiWhenLoginSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /* Bước 4 (trong nghiệp vụ Login): Tắt Dialog chờ, hiển thị màn hình chính cho người dùng tương tác. */
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }

                // Trước khi chuyển đến màn hình chính cấu hình file realm database theo user login hiện tại.
                DatabaseUtil.configRealmDatabase(S01Login_Activity.this);

                // Update data for app version current<2.
                ApplicationUpdateUtil.updateFolder(S01Login_Activity.this);

                // Save value version code.
                LoginUtil.saveVersionCodeCurrent(S01Login_Activity.this);

                // Đi đến màn hình chính và chắc chắn rằng không còn màn hình đăng ký hay đăng nhập trước đó.
                Intent intent = new Intent(S01Login_Activity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    /**
     * Chạy trong UI Thread để xử lý GUI khi Login thất bại.
     *
     * @param errorMessage The error message.
     */
    private void processUiWhenLoginFail(final String errorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /* Bước 4 (trong nghiệp vụ Login): Tắt Dialog chờ, hiển thị thông điệp lỗi. */
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }

                String errorTitle = getString(R.string.s01_login__content_error__login_failure);
                MessageDialogUtil.showNotificationDialog(S01Login_Activity.this, errorTitle, errorMessage);
            }
        });
    }

    /**
     * Method request send device token.
     *
     * @param device_token Value deviceToken.
     * @param access_token Value accessToken.
     */
    private void requestSendDeviceToken(String device_token, String access_token) {
        if (device_token == null || device_token.isEmpty()) {
            return;
        }

        // Handle send device token to server
        String deviceTokenUrl = AppConfig.getApiBaseUrl() + Definition.API.DEVICE_TOKEN;
        Log.d(LOG_TAG, "DeviceTokenURL: " + deviceTokenUrl);
        Log.d(LOG_TAG, "AccessToken: " + access_token);
        Log.d(LOG_TAG, "DeviceToken: " + device_token);

        OkHttpClient client = NetworkUtil.getDefaultHttpClient(this);

        RequestBody formBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_DEVICE_TOKEN, device_token)
                .add(Definition.Request.PARAM_OS, Definition.Constants.ANDROID)
                .build();

        Request request = new Request.Builder()
                .header(Definition.Request.HEADER_AUTHORIZATION, Definition.Request.HEADER_BEARER + access_token)
                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                .header(Definition.Request.PARAM_CLIENT_TOKEN, AppConfig.getClientToken())
                .url(deviceTokenUrl)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(LOG_TAG, "onFailure()");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Read data on the worker thread
                final String responseData = response.body().string();
                Log.d(LOG_TAG, "onResponse()");
                Log.d(LOG_TAG, "Response: " + responseData);
            }
        });
    }

    // Final String logUrl = AppConfig.getApiBaseUrl() + Define.API.LOG_DOWNLOAD.
    private void requestDownloadLog(String logUrl, final boolean isShowingProgress) {
        Log.d(LOG_TAG, "DownloadUrl: " + logUrl);

        // Lấy Access-Token gần nhất từ Local.
        String accessToken = "";
        UserModel lastUserModel = LocalAppUtil.getLastLoginUserInfo(getApplicationContext());

        if (lastUserModel != null) {
//            accessToken = lastUserModel.accessToken;
        }

        IoUtil.downloadFileFromUrl(this,
                accessToken,
                logUrl,
                Definition.FileData.DATA_DIRECTORY,
                new IoUtil.HandleFileCallback<Integer, Map<String, String>>() {
                    @Override
                    public void onPreExecute() {
//                        if (isShowingProgress && !isFinishing()) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (mLoadingDataDialog != null && !mLoadingDataDialog.isShowing()) {
//                                        mLoadingDataDialog.show();
//                                    }
//                                }
//                            });
//                        }
                    }

                    @Override
                    public void onMaxProgress(int max) {

                    }

                    @Override
                    public void onProgressUpdate(Integer... progress) {

                    }

                    @Override
                    public void onPostExecute(Map<String, String> stringMap) {
                        if (isShowingProgress) {
                            handleUnzipLog(stringMap, true);
                        } else {
                            handleUnzipLog(stringMap, false);
                        }
                    }
                });
    }

    /**
     * Todo method check later.
     *
     * @param unzipFileInfo
     * @param isDownloadContinue
     */
    private void handleUnzipLog(Map<String, String> unzipFileInfo, final boolean isDownloadContinue) {
//        IoUtil.unzip(this, unzipFileInfo, Define.FileData.DATA_DIRECTORY, new IoUtil.HandleFileCallback<Integer, Map<String, String>>() {
//            @Override
//            public void onPreExecute() {
//
//            }
//
//            @Override
//            public void onMaxProgress(int max) {
//
//            }
//
//            @Override
//            public void onProgressUpdate(Integer... progress) {
//
//            }
//
//            @Override
//            public void onPostExecute(Map<String, String> stringMap) {
//                if (stringMap != null) {
//                    String dataDir = stringMap.get(Define.General.FILE_PATH);
//                    String date = stringMap.get(Define.General.DATE);
//                    handleSaveLog(dataDir, date, isDownloadContinue);
//                } else {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mLoadingDataDialog.dismiss();
//                        }
//                    });
//                    Intent intent = new Intent(S01Login_Activity.this, MainActivity.class);
//                    startActivity(intent);
//                    finish();
//                }
//            }
//        });
    }

    /**
     * Todo method check later.
     */
    private void handleSaveLog(String dataDir, final String date, final boolean isDownloadContinue) {
//        mDatabaseStoreLogTask = new DatabaseStoreLogTask(this, dataDir, mRealm, new CreateDatabaseCallback() {
//            @Override
//            public void onStartCreated() {
//                // TODO: Nothing to do
//            }
//
//            @Override
//            public void onComplete(RealmAsyncTask transaction) {
//                if (isDownloadContinue) {
//                    String newDate = date.contains(":") ? date : (date + " 00:00:00");
//                    String logUrl = AppConfig.getApiBaseUrl() + String.format(Define.API.LOG_DOWNLOAD_RECENT, newDate);
//                    requestDownloadLog(logUrl, false);
//                } else {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mLoadingDataDialog.dismiss();
//                        }
//                    });
//                    Intent intent = new Intent(S01Login_Activity.this, MainActivity.class);
//                    startActivity(intent);
//                    finish();
//                }
//                mTransaction = transaction;
//            }
//        });
//        mDatabaseStoreLogTask.execute();
    }

    /**
     * Method check info loginFacebook.
     *
     * @param loginData Value login with facebook.
     */
    private void handleLoginFacebookData(String loginData) {
        try {
            JSONObject loginJsonObject = new JSONObject(loginData);
            boolean isSuccess = loginJsonObject.getBoolean(Definition.Response.SUCCESS);

            if (isSuccess) {
                A01AccessTokenGenerator_JsonData a01AccessTokenGenerator_jsonData = A01AccessTokenGenerator_Util.parseData(loginData);

                // Nếu thông tin lấy được từ server có giá trị khác null.
                if (a01AccessTokenGenerator_jsonData != null) {
                    // Save thông tin đăng nhập của người dùng facebook.
                    UserModel userModel = new UserModel();

                    userModel.tokenInfo = a01AccessTokenGenerator_jsonData;

                    LocalAppUtil.saveLastLoginUserInfo(getApplicationContext(), userModel);

                    // Request lên server một lần nữa để lấy thông tin đầy đủ.
                    requestAsyncGetUserInfoWithFacebook(a01AccessTokenGenerator_jsonData);

                    // SendDeviceToken to Server.
                    requestSendDeviceToken(FirebaseInstanceId.getInstance().getToken(), a01AccessTokenGenerator_jsonData.access_token);
                }
            } else {
                final String errMsg = loginJsonObject.getString(Definition.Response.TITLE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MessageDialogUtil.showNotificationDialog(S01Login_Activity.this, getString(R.string.common_msg__title__error), getString(R.string.s01_login__content_error__login_failure));
                    }
                });
            }
        } catch (JSONException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
    }

    /**
     * Validate Login form.
     * <pre>
     * Nếu không hợp lệ sẽ hiển thị Dialog Message và trả về false.
     * Nếu hợp lệ thì trả về true.
     * </pre>
     */
    private List<String> checkValidate() {
        List<String> errors = new ArrayList<>();

        String usernameOrEmail = mTxtUsername.getText().toString();

        if (TextUtils.isEmpty(usernameOrEmail)) {
            errors.add(getString(R.string.common_auth__txt_username__validation_required));
        } else {
            // Check username min.
            if (usernameOrEmail.length() < getResources().getInteger(R.integer.common_txt_username_min_length)) {
                errors.add(getString(R.string.common_auth__txt_username__validation_too_short));
            }

            // Check username max.
            if (usernameOrEmail.length() >= getResources().getInteger(R.integer.common_txt_username_max_length)) {
                errors.add(getString(R.string.common_auth__txt_username__validation_too_long));
            }

            if (!StringUtil.isValidUserName(usernameOrEmail)) {
                errors.add(getString(R.string.common_auth__txt_username__validation_invalid));
            }
        }

        String password = mTxtPassword.getText().toString();

        if (TextUtils.isEmpty(password)) {
            errors.add(getString(R.string.common_auth__txt_password__validation_required));
        } else {
            // Check password min.
            if (password.length() < getResources().getInteger(R.integer.common_txt_password_min_length)) {
                errors.add(getString(R.string.common_auth__txt_password__validation_too_short));
            }

            // Check password max.
            if (password.length() >= getResources().getInteger(R.integer.common_txt_password_max_length)) {
                errors.add(getString(R.string.common_auth__txt_password__validation_too_long));
            }
        }

        return errors;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    //    @Override

    //    public void onClick(final View view) {
    //        view.setEnabled(false);
    //        new android.os.Handler().postDelayed(new Runnable() {
    //            @Override
    //            public void run() {
    //                view.setEnabled(true);
    //            }
    //        }, 500);
    //        if (view.getId() == mBtnLoginSystemServer.getId()) {
    //            if (NetworkUtil.isAvailable(this)) {
    //                requestAsyncLoginUsingServer();
    //            } else {
    //                showNetworkUnavailableDialog(new NetworkConnectionCallback() {
    //                    @Override
    //                    public void onTryAgain() {
    //                        onClick(view);
    //                    }
    //                });
    //            }
    //        } else if (view.getId() == mUtvForgotPassword.getId()) {
    //            handleForgotPassword();
    //        } else if (view.getId() == mUtvRegister.getId()) {
    //            handleRegister();
    //        }
    //        if (view.getId() == mRlChangeEng.getId()) {
    //            LocaleHelper.setLocale(this, Define.SharedPreferencesKey.ENGLISH);
    //            setLanguage(Define.SharedPreferencesKey.ENGLISH);
    //        } else if (view.getId() == mRlChangeVi.getId()) {
    //            LocaleHelper.setLocale(this, Define.SharedPreferencesKey.VIETNAM);
    //            setLanguage(Define.SharedPreferencesKey.VIETNAM);
    //        }
    //    }

    @Override
    protected void onDestroy() {
        // Cẩn thận giải phóng tài nguyên.
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }

        //        if (mRealm != null && !mRealm.isClosed()) {
        //            mRealm.close();
        //        }
        //        if (mLoadingDataDialog != null && mLoadingDataDialog.isShowing()) {
        //            mLoadingDataDialog.cancel();
        //        }

        mProgressDialog = null;

        super.onDestroy();
    }

}
