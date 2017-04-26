package com.honkidenihongo.pre.gui.auth;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.api.json.A03Me_JsonData;
import com.honkidenihongo.pre.api.util.A03Me_Util;
import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.InternalStorageContentProvider;
import com.honkidenihongo.pre.common.util.IoUtil;
import com.honkidenihongo.pre.common.util.LocalAppUtil;
import com.honkidenihongo.pre.common.util.MessageDialogUtil;
import com.honkidenihongo.pre.common.util.NetworkUtil;
import com.honkidenihongo.pre.common.util.PermissionUtil;
import com.honkidenihongo.pre.gui.MainActivity;
import com.honkidenihongo.pre.gui.cropphoto.CropPhotoActivity;
import com.honkidenihongo.pre.gui.listener.OnMainActivityListener;
import com.honkidenihongo.pre.gui.widget.CircleImageView;
import com.honkidenihongo.pre.gui.widget.HelperDialog;
import com.honkidenihongo.pre.model.UserModel;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Screen using Edit Profile of User.
 *
 * @author binh.tt.
 * @since 08-Nov-2016.
 */
public class S04UserProfile_Fragment extends Fragment {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S04UserProfile_Fragment.class.getName();
    private static final String PHOTO_TYPE = "image/*";
    private static final String FILE_TYPE = ".jpg";
    private static final int REQUEST_CODE_PICK_GALLERY = 0x1;
    private static final int REQUEST_CODE_TAKE_PICTURE = 0x2;
    private static final int REQUEST_CODE_CROPPED_PICTURE = 0x3;
    public static final int REQUEST_MEDIA_PERMISSION_GRANTED = 0x4;
    public static final int REQUEST_MEDIA_PERMISSION_DENIED = 0x5;
    public static final String TEMP_PHOTO_FILE_NAME = "temp_photo.jpg";
    private static final String REQUEST_DATA_TAKE_PICTURE = "return-data";
    private static final String PACKAGE = "package";

    // File for capturing camera images.
    private File mFileTemp;

    private CircleImageView mImgAvatar;
    private AppCompatImageView mImgCamera;
    private AppCompatTextView mEdtUserName;
    private AppCompatTextView mEdtEmail;
    private AppCompatTextView mEdtUserCode;
    private AppCompatTextView mEdtFacebook;
    private AppCompatTextView mTvFacebook;
    private SwitchCompat mSwitchFacebook;

    private OnMainActivityListener mActivityListener;
    private ProgressDialog mProgressDialog;
    private HelperDialog mHelperDialog;
    public Context mContext;

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
        return inflater.inflate(R.layout.s04_user_profile_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Method using create View inside layout.
        initView(view);

        // Method using load data to View.
        // If có mạng thì lấy thông tin mới nhất từ server.
        // Ngược lại lấy thông tin đã luu trước đó.
        if (NetworkUtil.isAvailable(mContext)) {
            requestAsyncGetUserInfo();
        } else {
            setData();
        }

        // Method using set event for View child inside Layout.
        setEvent();

        /**
         * Show dialog help.
         */
        showDialogHelp();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mActivityListener != null) {
            mActivityListener.setTitleScreen(getString(R.string.common_module__user_profile));
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (mHelperDialog != null && mHelperDialog.isShowing()) {
                mHelperDialog.dismiss();
                mHelperDialog = null;
            }

            // Cẩn thận giải phóng tài nguyên.
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }

            mProgressDialog = null;

        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_MEDIA_PERMISSION_GRANTED) {
            // Phương thức được triệu gọi khi app hỏi quyền truy cập bộ nhớ của thiết bị,
            // Activity cha sau khi lắng nghe người dùng cho phép truy cập bộ nhớ sẽ mở lại dialog
            // Chọn ảnh từ camera hay gallery cho người dùng.

            selectPhoto();
        }

        if (requestCode == REQUEST_MEDIA_PERMISSION_DENIED) {
            showDialogConfirmPermission();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Nhận data từ camera.
        if (requestCode == REQUEST_CODE_TAKE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                if (mFileTemp == null) {
                    createTempFile();
                } else {
                    gotoScreenCropPhoto(mFileTemp.getPath());
                }
            }
        }

        // Nhận data từ gallery.
        if (requestCode == REQUEST_CODE_PICK_GALLERY) {
            if (resultCode == Activity.RESULT_OK) {
                if (mFileTemp == null) {
                    createTempFile();
                } else {
                    try {
                        InputStream inputStream = mContext.getContentResolver().openInputStream(data.getData());
                        FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
                        IoUtil.copy(inputStream, fileOutputStream);
                        fileOutputStream.close();
                        inputStream.close();

                        gotoScreenCropPhoto(mFileTemp.getPath());
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                }
            }
        }

        // Nhận data từ activity crop về.
        if (requestCode == REQUEST_CODE_CROPPED_PICTURE) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();

                if (uri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
                        uploadPhotoToServer(bitmap);
                    } catch (IOException e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Method using init View.
     */
    private void initView(View view) {
        mImgAvatar = (CircleImageView) view.findViewById(R.id.mImgAvatar);
        mImgCamera = (AppCompatImageView) view.findViewById(R.id.mImgCamera);
        mEdtUserName = (AppCompatTextView) view.findViewById(R.id.s04_user_profile_txt_username);
        mEdtEmail = (AppCompatTextView) view.findViewById(R.id.mEdtEmail);
        mEdtUserCode = (AppCompatTextView) view.findViewById(R.id.mEdtUserCode);
        mEdtFacebook = (AppCompatTextView) view.findViewById(R.id.mEdtFacebook);
        mSwitchFacebook = (SwitchCompat) view.findViewById(R.id.mSwitchFacebook);
        mTvFacebook = (AppCompatTextView) view.findViewById(R.id.mTvFacebook);

        // Khởi tạo dialog.
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(getString(R.string.common_msg__content_info__processing));
        mProgressDialog.setCancelable(false);

        mHelperDialog = new HelperDialog(mContext, R.style.TransparentDialog, getString(R.string.common_help__s04_user_profile__title), getString(R.string.common_help__s04_user_profile__content));
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

                boolean isShowFirstOfScreen = prefs.getBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S04_USER_PROFILE, true);

                if (isShowFirstOfScreen) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S04_USER_PROFILE, false);
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
    private void setData() {
        // Lấy thông tin user profile.
        UserModel userModel = LocalAppUtil.getLastLoginUserInfo(mContext);

        if (userModel != null) {
            // Load ảnh avatar.
            Picasso.with(mContext).invalidate(userModel.avatarUrl);
            Picasso.with(mContext)
                    .load(userModel.avatarUrl)
                    .networkPolicy(NetworkUtil.isAvailable(mContext) ? NetworkPolicy.NO_CACHE : NetworkPolicy.OFFLINE)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_avatar_default)
                    .error(R.drawable.ic_avatar_default)
                    .into(mImgAvatar);

            mEdtUserName.setText(userModel.username);
            mEdtEmail.setText(userModel.email);
            mEdtUserCode.setText(userModel.code);

            mEdtFacebook.setText(userModel.facebookDisplayName);
            mEdtFacebook.setVisibility(userModel.facebookConnected ? View.VISIBLE : View.GONE);
            mTvFacebook.setVisibility(userModel.facebookConnected ? View.VISIBLE : View.GONE);
            mSwitchFacebook.setVisibility(View.GONE);

            // Todo at version current remove function connection and disconnection facebook.
            // Nếu tài khoản đang đăng nhập và facebook thì ẩn nó đi , ngược lại để người dùng connection or disconnection facebook đối với tài khoản hệ thống.
//            mSwitchFacebook.setVisibility(userModel.authType == Definition.AuthType.FACEBOOK ? View.GONE : View.VISIBLE);
//            mSwitchFacebook.setChecked(userModel.facebookConnected);
        }
    }

    /**
     * // Todo không gọi phương thức này trong version mới.
     * Method dùng để get mã code.
     *
     * @param userModel User login.
     */
    private void getUserCode(UserModel userModel) {
        // Cẩn thận luôn khởi tạo mới.
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.common_msg__content_info__processing));
        mProgressDialog.show();

        OkHttpClient okHttpClient = NetworkUtil.getDefaultHttpClient(mContext);

        // Lấy thông tin user hiện thời vừa đăng nhập để lấy Access-Token hợp lệ.
        String accessToken = null;

        if (userModel != null && userModel.tokenInfo != null) {
            accessToken = userModel.tokenInfo.access_token;
        }

        String authHeader = String.format("%s %s", Definition.Request.HEADER_BEARER2, accessToken);

        Request request = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + Definition.API.GET_USER_CODE)
                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                .header(Definition.Request.HEADER_AUTHORIZATION, authHeader)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // Tắt dialog.
                mProgressDialog.dismiss();

                // Nếu có Response thành công thì parse để lấy dữ liệu.
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    final String responseString = response.body().string();
                    Log.d(LOG_TAG, responseString);

                    if (!TextUtils.isEmpty(responseString)) {
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handleGetUserCodeData(responseString);
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Lấy mã user code from server.
     *
     * @param data String from Response.
     */
    private void handleGetUserCodeData(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            boolean isSuccess = jsonObject.getBoolean(Definition.Response.SUCCESS);

            if (isSuccess) {
                JSONObject dataJon = jsonObject.getJSONObject(Definition.Response.DATA);
                mEdtUserCode.setText(dataJon.get(Definition.Response.CODE).toString());
            }
        } catch (JSONException e) {
            Log.d(LOG_TAG, "" + e.getMessage());
        }
    }

    /**
     * Method using set Event for View inside screen.
     */
    private void setEvent() {
        mImgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PermissionUtil.checkPermissionStorage(mContext)) {
                    selectPhoto();
                } else {
                    // Ngược lại user đã disable dialog request permission media của app khi click check box, ta cần request lại.
                    ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PermissionUtil.REQUEST_PERMISSIONS_READ_EXTERNAL_STORAGE);
                }
            }
        });

        mImgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PermissionUtil.checkPermissionStorage(mContext)) {
                    selectPhoto();
                } else {
                    // Ngược lại user đã disable dialog request permission media của app khi click check box, ta cần request lại.
                    ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PermissionUtil.REQUEST_PERMISSIONS_READ_EXTERNAL_STORAGE);
                }
            }
        });

        // Set sự kiện khi người dùng tương tác với UI Switch.
        mSwitchFacebook.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // Set trạng thái cho switch khi người dùng click là true.
                mSwitchFacebook.setTag(Boolean.TRUE);

                return false;
            }
        });

        //   Todo version hiện tại chưa cần đến chức năng này.
//        // Lắng nghe sự thay đổi trạng thái của switch.
//        mSwitchFacebook.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
//                if (mSwitchFacebook.getTag() instanceof Boolean) {
//                    Boolean userClick = (Boolean) mSwitchFacebook.getTag();
//
//                    if (userClick != null && userClick) {
//                        // If đúng là người dùng tương tác với switch.
//                        // Set lại trạng thái cho switch là flase khi người dùng không tương tác với nó.
//                        mSwitchFacebook.setTag(Boolean.FALSE);
//
//                        if (isCheck) {
//                            connectToFacebook();
//                        } else {
//                            requestAsyncDisConnectFacebook();
//                        }
//                    }
//                }
//            }
//        });
    }

    /**
     * Show dialog confirm permission for application to user.
     */
    private void showDialogConfirmPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false)
                .setMessage(mContext.getString(R.string.common_msg__content_warning__confirm_permission_gallery))
                .setTitle(mContext.getString(R.string.common_msg__title__warning))
                .setNegativeButton(R.string.common_text__cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton(R.string.common_text__ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts(PACKAGE, mContext.getPackageName(), null);
                        intent.setData(uri);

                        // Lấy activity start.
                        ((Activity) mContext).startActivityForResult(intent, PermissionUtil.REQUEST_PERMISSIONS_READ_EXTERNAL_STORAGE);
                    }
                });

        AlertDialog dialogPermission = builder.create();
        dialogPermission.show();
    }

    //   Todo version hiện tại chưa cần đến chức năng này.
//    /**
//     * Connection facebook of user.
//     */
//    private void connectToFacebook() {
//        final Activity activity = ((Activity) mContext);
//
//        // Lấy data để kết nối đến facebook from activity parent.
//        if (activity != null && !activity.isFinishing() && activity instanceof MainActivity) {
//
//            CallbackManager callbackManager = ((MainActivity) activity).getCallbackManager();
//
//            LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile", "email"));
//            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//                @Override
//                public void onSuccess(LoginResult loginResult) {
//                    // Lấy access token của user facebook gởi lên server app.
//                    final AccessToken accessToken = loginResult.getAccessToken();
//
//                    String facebook_access_token = accessToken.getToken();
//
//                    // Thực hiện request với access token có được
//                    if (!TextUtils.isEmpty(facebook_access_token)) {
//                        checkAndConnectFacebook(activity, facebook_access_token);
//                    }
//                }
//
//                @Override
//                public void onCancel() {
//                    // Lỗi thì update lại trạng thái của switch như cũ.
//                    mSwitchFacebook.setChecked(!mSwitchFacebook.isChecked());
//                }
//
//                @Override
//                public void onError(FacebookException error) {
//                    // Lỗi thì update lại trạng thái của switch như cũ.
//                    mSwitchFacebook.setChecked(!mSwitchFacebook.isChecked());
//                }
//            });
//        }
//    }

    /**
     * Todo version hiện tại chưa cần đến chức năng này.
     * Check connection to facebook.
     *
     * @param activity              Activity current.
     * @param facebook_access_token access token of facebook.
     */
//    private void checkAndConnectFacebook(final Activity activity, final String facebook_access_token) {
//        // Kiểm tra kết nối mạng.
//        if (NetworkUtil.isAvailable(activity)) {
//            requestAsyncConnectFacebook(facebook_access_token);
//        } else {
//            // Thông báo lỗi đến người dùng, show dialog chọn thử lại.
//            MessageDialogUtil.showNetworkUnavailableDialog(mContext, new NetworkConnectionCallback() {
//                        @Override
//                        public void onTryAgain() {
//                            checkAndConnectFacebook(activity, facebook_access_token);
//                        }
//                    }
//            );
//        }
//    }

//    /**
//     * Connect to facebook.
//     *
//     * @param facebook_access_token Value Access token.
//     */
//    private void requestAsyncConnectFacebook(final String facebook_access_token) {
//        // Cẩn thận luôn khởi tạo mới.
//        mProgressDialog = new ProgressDialog(mContext);
//        mProgressDialog.setCancelable(false);
//        mProgressDialog.setMessage(getString(R.string.common_msg__content_info__processing));
//        mProgressDialog.show();
//
//        OkHttpClient okHttpClient = new OkHttpClient();
//
//        // Lấy thông tin user hiện thời vừa đăng nhập để lấy Access-Token hợp lệ.
//        String accessToken = null;
//
//        UserModel userModel = LocalAppUtil.getLastLoginUserInfo(mContext);
//
//        if (userModel != null && userModel.tokenInfo != null) {
//            accessToken = userModel.tokenInfo.access_token;
//        }
//
//        String authHeader = String.format("%s %s", Definition.Request.HEADER_BEARER2, accessToken);
//
//        // Truyền lên server access token của facebook user.
//        final RequestBody formBody = new FormBody.Builder().add(Definition.Request.PARAM_FACEBOOK_ACCESS_TOKEN, facebook_access_token).build();
//
//        final Request request = new Request.Builder()
//                .header(Definition.Request.HEADER_AUTHORIZATION, authHeader)
//                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
//                .url(AppConfig.getApiBaseUrl() + Definition.API.FACEBOOK_CONNECT)
//                .post(formBody)
//                .build();
//
//        okHttpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                ((Activity) mContext).runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mProgressDialog.dismiss();
//                        // Lỗi thì update lại trạng thái của switch như cũ.
//                        mSwitchFacebook.setChecked(!mSwitchFacebook.isChecked());
//
//                        // Show thông báo lỗi  cho người dùng.
//                        MessageDialogUtil.showNotificationDialog(mContext, getString(R.string.common_msg__title__error), getString(R.string.common_msg__content_error__cannot_connect_to_server));
//
//                        // Xóa thông tin facebook đã lấy trước đó nếu có để UI button login with facebook set lại trạng thái đăng nhập.
//                        try {
//                            LoginManager.getInstance().logOut();
//                        } catch (Exception e) {
//                            Log.d(LOG_TAG, "" + e.getMessage());
//                        }
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                // Nếu có Response thành công thì parse để lấy dữ liệu.
//                if (response.code() == HttpURLConnection.HTTP_OK) {
//
//                    final String responseString = response.body().string();
//
//                    if (!TextUtils.isEmpty(responseString)) {
//                        ((Activity) mContext).runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                mProgressDialog.dismiss();
//
//                                try {
//                                    JSONObject jsonObject = new JSONObject(responseString);
//                                    boolean isSuccess = jsonObject.getBoolean(Definition.Response.SUCCESS);
//                                    if (isSuccess) {
//                                        // Call lại api để get user info.
//                                        requestAsyncGetUserInfo();
//                                    } else {
//                                        // Show thông báo lỗi  cho người dùng.
//                                        MessageDialogUtil.showNotificationDialog(mContext, getString(R.string.common_msg__title__error), getString(R.string.s04_user_profile__content_error__connect_fb_failure));
//                                    }
//                                } catch (JSONException e) {
//                                    Log.d(LOG_TAG, "" + e.getMessage());
//                                }
//                            }
//                        });
//                    }
//                } else {
//                    ((Activity) mContext).runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            // Lỗi thì update lại trạng thái của switch như cũ.
//                            mSwitchFacebook.setChecked(!mSwitchFacebook.isChecked());
//
//                            mProgressDialog.dismiss();
//
//                            // Show thông báo lỗi  cho người dùng.
//                            MessageDialogUtil.showNotificationDialog(mContext, getString(R.string.common_msg__title__error), getString(R.string.common_msg__content_error__cannot_connect_to_server));
//                        }
//                    });
//
//                    // Xóa thông tin facebook đã lấy trước đó nếu có để UI button login with facebook set lại trạng thái đăng nhập.
//                    try {
//                        LoginManager.getInstance().logOut();
//                    } catch (Exception e) {
//                        Log.d(LOG_TAG, "" + e.getMessage());
//                    }
//                }
//            }
//        });
//    }

    /**
     * Api updateInfo.
     */
    private void requestAsyncGetUserInfo() {
        // Cẩn thận luôn khởi tạo mới.
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(getString(R.string.common_msg__content_info__processing));
        }

        mProgressDialog.show();

        // Handle update info
        OkHttpClient okHttpClient = NetworkUtil.getDefaultHttpClient(mContext);

        final UserModel userModel = LocalAppUtil.getLastLoginUserInfo(mContext);

        String accessToken = null;
        if (userModel != null && userModel.tokenInfo != null) {
            accessToken = userModel.tokenInfo.access_token;
        }

        String authHeader = String.format("%s %s", Definition.Request.HEADER_BEARER2, accessToken);

        Request request = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + Definition.API.GET_USER)
                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                .header(Definition.Request.HEADER_AUTHORIZATION, authHeader)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();

                        // Lỗi thì update lại trạng thái của switch như cũ.
                        mSwitchFacebook.setChecked(!mSwitchFacebook.isChecked());

                        // Trường hợp bị lỗi thì lấy thông tin đã lưu trước đó.
                        setData();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mProgressDialog.dismiss();
                final String responseString = response.body().string();

                // Nếu có Response thành công thì parse để lấy dữ liệu.
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Khởi tạo dữ liệu dạng đối tượng cần lấy.
                            A03Me_JsonData a03Me_JsonData = null;

                            if (!TextUtils.isEmpty(responseString)) {
                                a03Me_JsonData = A03Me_Util.parseData(responseString);

                                if (a03Me_JsonData != null && userModel != null) {
                                    // Bổ sung các thuộc tính cho user login hiện tại và save lại đồng thời update lại ui.
                                    userModel.facebookId = a03Me_JsonData.facebook_id;
                                    userModel.facebookEmail = a03Me_JsonData.facebook_email;
                                    userModel.facebookUsername = a03Me_JsonData.facebook_username;
                                    userModel.facebookDisplayName = a03Me_JsonData.facebook_display_name;
                                    userModel.facebookAvatarUrl = a03Me_JsonData.facebook_avatar_url;
                                    userModel.facebookConnected = a03Me_JsonData.facebook_connected;

                                    mEdtFacebook.setVisibility(userModel.facebookConnected ? View.VISIBLE : View.GONE);
                                    mEdtFacebook.setText(userModel.facebookDisplayName);

                                    // Save lại thông tin.
                                    LocalAppUtil.saveLastLoginUserInfo(mContext, userModel);

                                    // Cập nhật lại data lên ui.
                                    setData();
                                }
                            } else {
                                // Lỗi thì update lại trạng thái của switch như cũ.
                                mSwitchFacebook.setChecked(!mSwitchFacebook.isChecked());
                                // Show thông báo lỗi  cho người dùng.
                                MessageDialogUtil.showNotificationDialog(mContext, getString(R.string.common_msg__title__error), getString(R.string.common_msg__content_error__cannot_connect_to_server));
                            }
                        }
                    });
                } else {
                    // Todo trường hợp này sẽ tự động logout để người dùng login lại app.
                }
            }
        });
    }

    //   Todo version hiện tại chưa cần đến chức năng này.
//    /**
//     * Disconnection facebook.
//     */
//    private void requestAsyncDisConnectFacebook() {
//        // Cẩn thận luôn khởi tạo mới.
//        mProgressDialog = new ProgressDialog(mContext);
//        mProgressDialog.setCancelable(false);
//        mProgressDialog.setMessage(getString(R.string.common_msg__content_info__processing));
//        mProgressDialog.show();
//
//        OkHttpClient okHttpClient = new OkHttpClient();
//
//        UserModel userModel = LocalAppUtil.getLastLoginUserInfo(mContext);
//
//        String accessToken = null;
//        if (userModel != null && userModel.tokenInfo != null) {
//            accessToken = userModel.tokenInfo.access_token;
//        }
//
//        String authHeader = String.format("%s %s", Definition.Request.HEADER_BEARER2, accessToken);
//
//        Request request = new Request.Builder()
//                .header(Definition.Request.HEADER_AUTHORIZATION, authHeader)
//                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
//                .url(AppConfig.getApiBaseUrl() + Definition.API.FACEBOOK_DISCONNECT)
//                .post(new FormBody.Builder().build())
//                .build();
//
//        okHttpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                ((Activity) mContext).runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mProgressDialog.dismiss();
//
//                        // Lỗi thì update lại trạng thái của switch như cũ.
//                        mSwitchFacebook.setChecked(!mSwitchFacebook.isChecked());
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                // Nếu có Response thành công thì parse để lấy dữ liệu.
//                if (response.code() == HttpURLConnection.HTTP_OK) {
//                    final String responseString = response.body().string();
//
//                    if (!TextUtils.isEmpty(responseString)) {
//                        ((Activity) mContext).runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                // Todo chi cho hien thi dialog mot lan ket hop get user info.
//                                mProgressDialog.dismiss();
//
//                                try {
//                                    JSONObject jsonObject = new JSONObject(responseString);
//                                    boolean isSuccess = jsonObject.getBoolean(Definition.Response.SUCCESS);
//                                    if (isSuccess) {
//                                        // Call lại api để get user info.
//                                        requestAsyncGetUserInfo();
//                                    } else {
//                                        // Show thông báo lỗi  cho người dùng.
//                                        MessageDialogUtil.showNotificationDialog(mContext, getString(R.string.common_msg__title__error), getString(R.string.s04_user_profile__content_error__disconnect_fb_failure));
//                                    }
//                                } catch (JSONException e) {
//                                    Log.d(LOG_TAG, "" + e.getMessage());
//                                }
//                            }
//                        });
//                    }
//                } else {
//                    ((Activity) mContext).runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            // Lỗi thì update lại trạng thái của switch như cũ.
//                            mSwitchFacebook.setChecked(!mSwitchFacebook.isChecked());
//                            mProgressDialog.dismiss();
//
//                            // Show thông báo lỗi  cho người dùng.
//                            MessageDialogUtil.showNotificationDialog(mContext, getString(R.string.common_msg__title__error), getString(R.string.common_msg__content_error__cannot_connect_to_server));
//                        }
//                    });
//                }
//            }
//        });
//    }

    /**
     * Show dialog choose photo from.
     */
    private void selectPhoto() {
        final CharSequence[] items = {mContext.getString(R.string.common_image__btn_camera), mContext.getString(R.string.common_image__btn_gallery), mContext.getString(R.string.common_text__cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setTitle(mContext.getString(R.string.common_image__title__select_photo));

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        if (null == mFileTemp) {
                            createTempFile();
                        }

                        takePic();
                        break;
                    case 1:
                        if (null == mFileTemp) {
                            createTempFile();
                        }

                        pickImage();
                        break;
                    case 2:
                        dialog.dismiss();

                        break;
                }
            }
        });

        builder.show();
    }

    /**
     * Đi đến màn hình crop photo với link ảnh nhận được.
     *
     * @param pathFile Path of photo.
     */
    private void gotoScreenCropPhoto(String pathFile) {
        Intent intent = new Intent(mContext, CropPhotoActivity.class);
        intent.putExtra(CropPhotoActivity.PATH_PHOTO_RECEIVE, pathFile);
        startActivityForResult(intent, REQUEST_CODE_CROPPED_PICTURE);
    }

    /**
     * Phương thức tạo ra một file tạm thời để lưu ảnh.
     */
    private void createTempFile() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
        } else {
            mFileTemp = new File(mContext.getFilesDir(), TEMP_PHOTO_FILE_NAME);
        }
    }

    /**
     * Phương thức được triệu gọi khi người dùng chọn camera để chụp ảnh.
     */
    private void takePic() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            Uri imageCaptureUri = null;
            String state = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                imageCaptureUri = Uri.fromFile(mFileTemp);
            } else {
                /*
                 * The solution is taken from here: http://stackoverflow.com/questions/10042695/how-to-get-camera-result-as-a-uri-in-data-folder
	        	 */
                imageCaptureUri = InternalStorageContentProvider.CONTENT_URI;
            }

            takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageCaptureUri);
            takePictureIntent.putExtra(REQUEST_DATA_TAKE_PICTURE, true);

            // Mở camera of device.
            startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PICTURE);

        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, mContext.getString(R.string.common_image__content_error__has_no_camera), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Get data if user chọn ảnh từ gallery.
     */
    private void pickImage() {
        Intent intent = new Intent();
        intent.setType(PHOTO_TYPE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, mContext.getString(R.string.common_image__title__choose_application)), REQUEST_CODE_PICK_GALLERY);
    }

    /**
     * Method dùng để upload ảnh photo avatar lên server.
     *
     * @param bitmap Value Bitmap.
     */
    private void uploadPhotoToServer(final Bitmap bitmap) {
        mProgressDialog.show();

        OkHttpClient okHttpClient = NetworkUtil.getDefaultHttpClient(mContext);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        // Lấy thông tin user hiện thời vừa đăng nhập để lấy Access-Token hợp lệ.
        String accessToken = null;

        UserModel userModel = LocalAppUtil.getLastLoginUserInfo(mContext);

        if (userModel != null && userModel.tokenInfo != null) {
            accessToken = userModel.tokenInfo.access_token;
        }

        String authHeader = String.format("%s %s", Definition.Request.HEADER_BEARER2, accessToken);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(Definition.Request.PARAM_AVATAR, System.currentTimeMillis() + FILE_TYPE, RequestBody.create(MediaType.parse("image/jpeg"), byteArray))
                .build();

        Request request = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + Definition.API.UPDATE_AVATAR)
                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                .header(Definition.Request.HEADER_AUTHORIZATION, authHeader)
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handleErrorUploadPhoto();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // Nếu có Response thành công thì parse để lấy dữ liệu.
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    final String responseString = response.body().string();
                    Log.d(LOG_TAG, responseString);

                    if (!TextUtils.isEmpty(responseString)) {
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Tắt dialog.
                                mProgressDialog.dismiss();
                                handleChangeAvatarResponse(responseString, bitmap);
                            }
                        });
                    }
                } else {
                    handleErrorUploadPhoto();
                }
            }
        });
    }

    /**
     * Thông báo lỗi không upload được ảnh.
     */
    private void handleErrorUploadPhoto() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Tắt dialog và thông báo lỗi.
                mProgressDialog.dismiss();
                MessageDialogUtil.showNotificationDialog(mContext, mContext.getString(R.string.common_msg__title__error), mContext.getString(R.string.s04_user_profile__content_error__update_avatar_failure));
            }
        });
    }

    /**
     * Lấy mã user code from server.
     *
     * @param data String from Response.
     */
    private void handleChangeAvatarResponse(String data, Bitmap bitmap) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            boolean isSuccess = jsonObject.getBoolean(Definition.Response.SUCCESS);

            if (isSuccess) {
                JSONObject dataJon = jsonObject.getJSONObject(Definition.Response.DATA);
                String url = dataJon.getString(Definition.Response.URL);

                UserModel userModel = LocalAppUtil.getLastLoginUserInfo(mContext);

                if (!TextUtils.isEmpty(url) && userModel != null) {
                    // Cập nhật lại đường dẫn avatar for user và update lại hiện thị ảnh.
                    userModel.avatarUrl = url;
                    LocalAppUtil.saveLastLoginUserInfo(mContext, userModel);
                    mImgAvatar.setImageBitmap(bitmap);
                }
            }
        } catch (JSONException e) {
            Log.d(LOG_TAG, "" + e.getMessage());
        }
    }
}
