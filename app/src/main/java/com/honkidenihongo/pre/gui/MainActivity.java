package com.honkidenihongo.pre.gui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.honkidenihongo.pre.MainApplication;
import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.LocalAppUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.common.util.LogoutUtil;
import com.honkidenihongo.pre.common.util.MessageDialogUtil;
import com.honkidenihongo.pre.common.util.NetworkUtil;
import com.honkidenihongo.pre.common.util.PermissionUtil;
import com.honkidenihongo.pre.common.util.TokenUtil;
import com.honkidenihongo.pre.common.util.ToolbarUtil;
import com.honkidenihongo.pre.gui.auth.S04UserProfile_Fragment;
import com.honkidenihongo.pre.gui.common.S18Setting_Fragment;
import com.honkidenihongo.pre.gui.common.S29AboutUs_Fragment;
import com.honkidenihongo.pre.gui.dashboard.S03Dashboard_Fragment;
import com.honkidenihongo.pre.gui.grammar.S27GrammarList_Fragment;
import com.honkidenihongo.pre.gui.lesson.S06LessonList_Fragment;
import com.honkidenihongo.pre.gui.lesson.S07LessonCategory_Fragment;
import com.honkidenihongo.pre.gui.lesson.S08PreLesson_Fragment;
import com.honkidenihongo.pre.gui.lesson.S19LessonCategoryContent_Fragment;
import com.honkidenihongo.pre.gui.listener.OnMainActivityListener;
import com.honkidenihongo.pre.gui.widget.CircleImageView;
import com.honkidenihongo.pre.gui.widget.HelperDialog;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.UserModel;
import com.honkidenihongo.pre.model.constant.LessonType;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Screen S05NavigationMenu, là màn hình chính khi user login vào app, từ màn hình này sẽ đi đến tất cả màn hình khác trong ứng dụng.
 * Modify.
 *
 * @author binh.dt.
 * @since 20-Dec-2016.
 */
public class MainActivity extends AppCompatActivity implements OnMainActivityListener {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = MainApplication.class.getName();

    private static final int VALUE_DEFAULT_STACK = 0;

    // Id of team when open from notification.
    private long mTeamID = -1;
    private boolean isFromNotification = false;

    // Value index to identify current nav menu item.
    public int mNavItemIndex = 0;

    // Define progress dialog.
    private ProgressDialog mProgressDialog;

    // For view.
    private Toolbar mToolbar;
    private AppCompatTextView mTvTitleToolbar;
    private CircleImageView mCivAvatar;
    private AppCompatTextView mTvUserName;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    // Biến này để toàn cục dùng để show lên thông báo đến người dùng muốn thoát ứng dụng thì bấm lại lần nữa.
    private Toast mToast;

    // Define CallbackManager to login, logout Facebook.
    private CallbackManager mCallbackManager;

    private HelperDialog mHelperDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCallbackManager = CallbackManager.Factory.create();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /**
         * Get layout for window.
         */
        setContentView(R.layout.activity_main);

        /**
         * Get view for layout.
         */
        initView();

        /**
         * Kiểm tra thời gian hết hạn của accessToken.
         */
        checkExpireAccessToken();

        /**
         * Setup menu and toolbar.
         */
        setUpNavigationBar();

        /**
         * Hiện thị màn hình dashboard.
         */
        goToScreen_S03Dashboard();

        /**
         * Set event for view.
         */
        setEvent();

        // Todo version này chưa nhận notification.
//        getIntentData(getIntent());
//        if (isFromNotification && mTeamID != -1) {
//            goToTeamDetail(mTeamID, mTeamName, 3);
//        } else {
//            goToScreen_S03Dashboard();
//        }
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

        // Todo version này chưa send log.
//        Log.e(LOG_TAG, "Log Save: " + MainApplication.isSaveLog);
//
//        if (!MainApplication.isSaveLog) {
//            Intent intentSaveLog = new Intent(Definition.LogReceiverConstant.ACTION_START_LOG);
//            sendBroadcast(intentSaveLog);
//        }
    }

    @Override
    protected void onDestroy() {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionUtil.REQUEST_PERMISSIONS_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0) {

                // Lấy fragment hiện tại đang hiển thị trong main activity.
                Fragment fragment = getCurrentFragment();

                // Nếu user cho phép quyền truy cập media.
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && fragment != null && fragment instanceof S04UserProfile_Fragment) {
                    fragment.onRequestPermissionsResult(S04UserProfile_Fragment.REQUEST_MEDIA_PERMISSION_GRANTED, permissions, grantResults);
                }

                // Nếu user đã disable hẳn luôn quyền truy cập media của app, cần show thông báo đến cho người dùng.
                // http://stackoverflow.com/questions/32854169/does-checking-the-never-ask-again-box-when-asking-for-a-runtime-permission-disable.
                if (grantResults[0] == PackageManager.PERMISSION_DENIED && fragment != null && fragment instanceof S04UserProfile_Fragment) {
                    fragment.onRequestPermissionsResult(S04UserProfile_Fragment.REQUEST_MEDIA_PERMISSION_DENIED, permissions, grantResults);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            onClickHomeIcon(true);
        }
    }

    /**
     * Phương thức khởi tạo view.
     */
    private void initView() {
        // Khởi tạo thông báo nhưng không show nó lên.
        mToast = Toast.makeText(MainActivity.this, getString(R.string.common_msg__content_warning__press_again_to_exit), Toast.LENGTH_SHORT);

        mHelperDialog = new HelperDialog(this, R.style.TransparentDialog, getString(R.string.common_help__s05_navigation_menu__title), getString(R.string.common_help__s05_navigation_menu__content));

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.common_msg__content_info__processing));
        mProgressDialog.setCancelable(false);

        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mTvTitleToolbar = (AppCompatTextView) mToolbar.findViewById(R.id.mTvTitleToolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);

        View navigationViewHeader = mNavigationView.getHeaderView(0);
        mCivAvatar = (CircleImageView) navigationViewHeader.findViewById(R.id.civ_menu_avatar);
        mTvUserName = (AppCompatTextView) navigationViewHeader.findViewById(R.id.tv_menu_full_name);
    }

    /***
     * Todo phương thức để check and remove later.
     *
     * @param intent Value.
     */
    private void getIntentData(Intent intent) {
        if (intent.getExtras() != null) {
            mTeamID = intent.getExtras().getLong(Definition.General.TEAM_ID, -1);
            String mTeamName = intent.getExtras().getString(Definition.General.TEAM_NAME, "");
            isFromNotification = intent.getExtras().getBoolean(Definition.General.PUSH_NOTIFICATION, false);
        }
    }

    /**
     * Check access token alive or not, if not, do request server to refresh token.
     */
    private void checkExpireAccessToken() {
        UserModel userModel = LocalAppUtil.getLastLoginUserInfo(this);

        if (userModel != null && userModel.tokenInfo != null) {
            if (userModel.tokenInfo.expires_in < System.currentTimeMillis()) {

                if (NetworkUtil.isAvailable(this)) {
                    TokenUtil.refreshToken(this, userModel.tokenInfo.refresh_token);
                }
            }
        }
    }

    /**
     * Setup NavigationBar for application.
     */
    private void setUpNavigationBar() {
        ToolbarUtil.initToolbar(mToolbar, this);

        mDrawerLayout.setDrawerShadow(R.drawable.s05_navigation_menu_shadow, GravityCompat.END);

        // Todo: Nghiên cứu cách không cần truyền String ResourceId (vì việc sinh ra 1 string là bất tiện).
        // ActionBarDrawerToggle mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.s05_navigation_menu_drawer_open, R.string.s05_navigation_menu_drawer_close) {
        ActionBarDrawerToggle mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, 0, 0) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                // Cập nhật lại avatar if người dùng thay đổi thông tin trong màn hình S04.
                // Lấy thông tin user profile mới nhất.
                UserModel userModel = LocalAppUtil.getLastLoginUserInfo(MainActivity.this);

                if (userModel != null && !TextUtils.isEmpty(userModel.avatarUrl)) {
                    // Load ảnh avatar.
                    Picasso.with(MainActivity.this).invalidate(userModel.avatarUrl);
                    Picasso.with(MainActivity.this)
                            .load(userModel.avatarUrl)
                            .networkPolicy(NetworkUtil.isAvailable(MainActivity.this) ? NetworkPolicy.NO_CACHE : NetworkPolicy.OFFLINE)
                            .fit()
                            .centerCrop()
                            .noPlaceholder()
                            .error(R.drawable.ic_avatar_default)
                            .into(mCivAvatar);
                }

                /**
                 * Show dialog help.
                 */
                showDialogHelp();
            }
        };

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickHomeIcon(false);
            }
        });

        // Lấy thông tin user profile.
        UserModel userModel = LocalAppUtil.getLastLoginUserInfo(this);

        // Load ảnh avatar.
        if (userModel != null && !TextUtils.isEmpty(userModel.avatarUrl)) {
            Picasso.with(this)
                    .load(userModel.avatarUrl)
                    .fit()
                    .centerCrop()
                    .error(R.drawable.ic_avatar_default)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(mCivAvatar);

            mTvUserName.setText(userModel.authType == Definition.AuthType.SYSTEM_SERVER ? userModel.username : userModel.facebookDisplayName);
        }

        // Đầu tiên sẽ load màn hình S03Dashboard thì menu item=0.
        mNavigationView.getMenu().getItem(mNavItemIndex).setChecked(true);

        // Set sự kiện click cho mỗi item trong menu.
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int menuItemId = item.getItemId();

                // Lấy fragment đang hiển thị trong main.
                Fragment fragmentCurrent = getCurrentFragment();

                if (fragmentCurrent != null && mDrawerLayout != null) {

                    boolean isFragmentS03Dashboard = fragmentCurrent instanceof S03Dashboard_Fragment;
                    boolean isFragmentS04UserProfile = fragmentCurrent instanceof S04UserProfile_Fragment;
                    boolean isFragmentS06LessonList = fragmentCurrent instanceof S06LessonList_Fragment;
                    boolean isFragmentS18Setting = fragmentCurrent instanceof S18Setting_Fragment;
                    boolean isFragmentS29AboutUs = fragmentCurrent instanceof S29AboutUs_Fragment;


                    // Tất cả câu lệnh phía dưới chung 1 cấu trúc kiểm tra xem fragment hiện tại là chính nó thì đóng menu.
                    // Ngược lại go to tới nó.
                    switch (menuItemId) {
                        case R.id.menu_item_dashboard:
                            mNavItemIndex = 0;

                            if (isFragmentS03Dashboard) {
                                mDrawerLayout.closeDrawer(GravityCompat.START);
                            } else {
                                goToScreen_S03Dashboard();
                            }

                            break;
                        case R.id.menu_item_lesson:
                            mNavItemIndex = 1;

                            if (isFragmentS06LessonList) {
                                mDrawerLayout.closeDrawer(GravityCompat.START);
                            } else {
                                goToScreen_S06LessonList();
                            }

                            break;
                        case R.id.menu_item_profile:
                            mNavItemIndex = 2;

                            if (isFragmentS04UserProfile) {
                                mDrawerLayout.closeDrawer(GravityCompat.START);
                            } else {
                                goToScreen_S04UserProfile();
                            }

                            break;
                        case R.id.menu_item_setting:
                            mNavItemIndex = 3;

                            if (isFragmentS18Setting) {
                                mDrawerLayout.closeDrawer(GravityCompat.START);
                            } else {
                                goToScreen_S18Setting();
                            }

                            break;
                        case R.id.menu_item_about:
                            mNavItemIndex = 4;

                            if (isFragmentS29AboutUs) {
                                mDrawerLayout.closeDrawer(GravityCompat.START);
                            } else {
                                goToScreenS29About();
                            }

                            break;
                        case R.id.menu_item_logout:
                            // Thực hiện đóng menu.
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                            showMessageConfirmLogout();

                            break;
                        default:

                            break;
                    }

                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }

                // Checking if the item is in checked state or not, if not make it in checked state.
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }

                item.setChecked(true);
                mNavigationView.getMenu().getItem(mNavItemIndex).setChecked(true);

                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method using show dialog help.
     */
    private void showDialogHelp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mHelperDialog == null) {
                    return;
                }

                boolean isShow = false;

                SharedPreferences prefs = getSharedPreferences(Definition.SettingApp.SETTING_APPLICATION, Context.MODE_PRIVATE);

                boolean isShowFirstOfScreen = prefs.getBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S05_NAVIGATION_MENU, true);

                if (isShowFirstOfScreen) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S05_NAVIGATION_MENU, false);
                    editor.apply();

                    isShow = true;
                } else {
                    boolean isShowApplication = prefs.getBoolean(Definition.SettingApp.DialogHelp.SHOW_DIALOG_HELP_ALL_APPLICATION, false);

                    if (isShowApplication) {
                        isShow = true;
                    }
                }

                if (isShow && !isFinishing() && !mHelperDialog.isShowing()) {
                    mHelperDialog.show();
                }
            }
        });
    }

    /**
     * Show message confirm logout to user.
     */
    private void showMessageConfirmLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(getString(R.string.common_auth__content_warning__confirm_logout));
        builder.setTitle(getString(R.string.common_msg__title__warning));
        builder.setCancelable(false);

        // Đồng ý thoát khỏi ứng dụng.
        builder.setPositiveButton(getString(R.string.common_text__ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                requestLogout();
            }
        });

        // Hủy đóng dialog.
        builder.setNegativeButton(getString(R.string.common_text__cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                // Khi user hủy không logout nữa thì set lại item check trước đó.
                mNavigationView.getMenu().getItem(mNavItemIndex).setChecked(true);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Method call api logout.
     */
    private void requestLogout() {
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

        // Lấy thông tin user hiện thời vừa đăng nhập để lấy Access-Token hợp lệ.
        UserModel userModel = LocalAppUtil.getLastLoginUserInfo(this);
        String accessToken = null;
        if (userModel != null && userModel.tokenInfo != null) {
            accessToken = userModel.tokenInfo.access_token;
        }

        /* Bước 2: Request lên API Server để lấy danh sách Lesson List. */
        // Chuẩn bị dữ kiện để gửi lên Server.
        String authHeader = String.format("%s %s", Definition.Request.HEADER_BEARER2, accessToken);

        String device_token = "";

        if (FirebaseInstanceId.getInstance() != null) {
            device_token = FirebaseInstanceId.getInstance().getToken();
        }

        RequestBody formBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_DEVICE_TOKEN, device_token == null ? "" : device_token)
                .add(Definition.Request.PARAM_OS, Definition.Constants.ANDROID)
                .build();

        // Chuẩn bị dữ kiện để gửi lên Server.
        Request userInfoRequest = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + Definition.API.LOGOUT)
                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                .header(Definition.Request.HEADER_AUTHORIZATION, authHeader)
                .header(Definition.Request.PARAM_CLIENT_TOKEN, AppConfig.getClientToken())
                .post(formBody)
                .build();

        OkHttpClient httpClient = NetworkUtil.getDefaultHttpClient(this);

        // Tiến hành gửi request bất đồng bộ (asynchronous) lên Server.
        httpClient.newCall(userInfoRequest).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Nếu dữ liệu Response trả về OK or lỗi cũng thực hiện logout cho người dùng.
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    processingLogout();
                } else {
                    processingLogout();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                processingLogout();
            }
        });
    }

    /**
     * Xử lý logout trên luồng chính.
     */
    private void processingLogout() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Tắt dialog.
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }

                LogoutUtil.handleLogoutUser(MainActivity.this);
            }
        });
    }

    /**
     * Method set event for view.
     */
    private void setEvent() {
        mCivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayS04UserProfile();
            }
        });

        mTvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayS04UserProfile();
            }
        });
    }

    /**
     * Display Screen S04UserProfile when click avatar inside menu.
     */
    private void displayS04UserProfile() {
        // Lấy fragment đang hiển thị trong main.
        Fragment fragmentCurrent = getCurrentFragment();

        if (fragmentCurrent != null && mDrawerLayout != null) {
            boolean isFragmentS04UserProfile = fragmentCurrent instanceof S04UserProfile_Fragment;

            if (isFragmentS04UserProfile) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                goToScreen_S04UserProfile();
                mNavItemIndex = 2;
                mNavigationView.getMenu().getItem(mNavItemIndex).setChecked(true);
            }
        }
    }

    /**
     * Show Fragment into content of Activity.
     *
     * @param fragment Fragment to show.
     */
    public void showFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

            boolean isFragmentS03Dashboard = fragment instanceof S03Dashboard_Fragment;
            boolean isFragmentS04UserProfile = fragment instanceof S04UserProfile_Fragment;
            boolean isFragmentS06LessonList = fragment instanceof S06LessonList_Fragment;
            boolean isFragmentS18Setting = fragment instanceof S18Setting_Fragment;
            boolean isFragmentS29AboutUs = fragment instanceof S29AboutUs_Fragment;

            // Nếu không phải là 1 trong 4 màn hình trên thì thêm fragment hiện tại vào ngăn xếp.
            if (!isFragmentS03Dashboard && !isFragmentS04UserProfile && !isFragmentS06LessonList && !isFragmentS18Setting && !isFragmentS29AboutUs) {
                fragmentTransaction.addToBackStack(fragment.getClass().getName());
            }

            fragmentTransaction.replace(R.id.fl_main_content, fragment).commit();

            // Gọi phương thức set icon cho home button là nút back hay nút home.
            setIconHome(fragment);
        }
    }

    /**
     * Set icon for button home inside screen main.
     *
     * @param fragment Fragment current.
     */
    private void setIconHome(Fragment fragment) {
        // Kểm tra fragment hiện tại có phải lả 1 trong 4 fragment dưới hay không.
        boolean isFragmentS03Dashboard = fragment instanceof S03Dashboard_Fragment;
        boolean isFragmentS04UserProfile = fragment instanceof S04UserProfile_Fragment;
        boolean isFragmentS06LessonList = fragment instanceof S06LessonList_Fragment;
        boolean isFragmentS18Setting = fragment instanceof S18Setting_Fragment;
        boolean isFragmentS29AboutUs = fragment instanceof S29AboutUs_Fragment;

        if (isFragmentS03Dashboard || isFragmentS04UserProfile || isFragmentS06LessonList || isFragmentS18Setting || isFragmentS29AboutUs) {

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white);
            if (mDrawerLayout != null) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        } else {
            getSupportActionBar().setHomeAsUpIndicator(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material);
            if (mDrawerLayout != null) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        }
    }

    /**
     * Go to Profile screen.
     */
    private void goToScreen_S04UserProfile() {
        S04UserProfile_Fragment s04UserProfile_fragment = new S04UserProfile_Fragment();
        showFragment(s04UserProfile_fragment);
    }

    /**
     * Go to Screen S03Dashboard.
     */
    private void goToScreen_S03Dashboard() {
        S03Dashboard_Fragment s03Dashboard_fragment = new S03Dashboard_Fragment();
        showFragment(s03Dashboard_fragment);
    }

    @Override
    public void goToScreen_S06LessonList() {
        S06LessonList_Fragment s06LessonListFragment = new S06LessonList_Fragment();
        showFragment(s06LessonListFragment);
    }

    @Override
    public void goToScreen_S06LessonList_Item(Lesson lesson) {
        if (lesson.getType() == LessonType.PRE_LESSON) {
            S08PreLesson_Fragment preLessonFragment = S08PreLesson_Fragment.newInstance(lesson);
            showFragment(preLessonFragment);
        } else {
            S07LessonCategory_Fragment s07UnitLessonsFragment = S07LessonCategory_Fragment.newInstance(lesson);
            showFragment(s07UnitLessonsFragment);
        }
    }

    public void goToScreen_S18Setting() {
        S18Setting_Fragment s18_setting_fragment = new S18Setting_Fragment();
        showFragment(s18_setting_fragment);
    }

    @Override
    public void goToScreenS19LessonCategory(Lesson lesson) {
        S19LessonCategoryContent_Fragment s19LessonCategoryContentFragment = S19LessonCategoryContent_Fragment.newInstance(lesson);
        showFragment(s19LessonCategoryContentFragment);
    }

    @Override
    public void goToScreenS27GrammarList(Lesson lesson) {
        S27GrammarList_Fragment s27GrammarListFragment = S27GrammarList_Fragment.newInstance(lesson);
        showFragment(s27GrammarListFragment);
    }

    /**
     * Goto screen S29About.
     */
    private void goToScreenS29About() {
        S29AboutUs_Fragment s29AboutUsFragment = new S29AboutUs_Fragment();
        showFragment(s29AboutUsFragment);
    }

    @Override
    public void setTitleScreen(String title) {
        mTvTitleToolbar.setText(title);
    }

    /**
     * Phương thức dùng để login và logout facebook trong fragment S04UserProfile.
     *
     * @return CallbackManager.
     */
    public CallbackManager getCallbackManager() {
        return mCallbackManager;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        getIntentData(intent);

        if (isFromNotification && mTeamID != -1) {
//            goToTeamDetail(mTeamID, mTeamName, 3);
        }
    }

    /**
     * Phương thức dùng để get fragment đang hiển thị trong main.
     */
    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fl_main_content);
    }

    /**
     * Method lắng nghe sự kiện click home button.
     *
     * @param isBackPress Value is true thì bạn đang bấm nút back của device ngược lại là nút back trên toolbar.
     */
    private void onClickHomeIcon(boolean isBackPress) {
        // Lấy fragment hiện tại đang hiển thị trong main.
        Fragment fragmentCurrent = getCurrentFragment();

        // Kểm tra fragment hiện tại có phải lả 1 trong 4 fragment dưới hay không.
        boolean isFragmentS03Dashboard = fragmentCurrent instanceof S03Dashboard_Fragment;
        boolean isFragmentS04UserProfile = fragmentCurrent instanceof S04UserProfile_Fragment;
        boolean isFragmentS06LessonList = fragmentCurrent instanceof S06LessonList_Fragment;
        boolean isFragmentS18Setting = fragmentCurrent instanceof S18Setting_Fragment;
        boolean isFragmentS29AboutUs = fragmentCurrent instanceof S29AboutUs_Fragment;

        if (fragmentCurrent != null) {
            if (!isBackPress) {
                // Nếu là 1 trong 4 fragment trên là cùng cấp.
                if (isFragmentS03Dashboard || isFragmentS04UserProfile || isFragmentS06LessonList || isFragmentS18Setting || isFragmentS29AboutUs) {
                    if (mDrawerLayout != null && !mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                        mDrawerLayout.openDrawer(GravityCompat.START);
                    }
                } else {
                    if (getSupportFragmentManager().getBackStackEntryCount() > VALUE_DEFAULT_STACK) {
                        getSupportFragmentManager().popBackStack();

                        // Nếu fragment trong ngăn xếp là 1 thì mở khóa menu.
                        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white);
                            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        }
                    }
                }
            } else {
                // Nếu fragment trong ngăn xếp lớn hơn không thì thực hiện pop back.
                if (getSupportFragmentManager().getBackStackEntryCount() > VALUE_DEFAULT_STACK) {
                    getSupportFragmentManager().popBackStack();

                    // Nếu fragment trong ngăn xếp là 1 thì mở khóa menu.
                    if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white);
                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    }
                } else {
                    // Kiểm tra thông báo đã được show đến người dùng chưa, nếu chưa thì show, ngược lại thì finish.
                    if (mToast != null && !mToast.getView().isShown()) {
                        mToast.show();
                    } else {
                        finish();
                    }
                }
            }
        }
    }
}
