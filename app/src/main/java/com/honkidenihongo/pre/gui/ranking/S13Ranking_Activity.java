package com.honkidenihongo.pre.gui.ranking;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.LessonNameUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.common.util.MessageDialogUtil;
import com.honkidenihongo.pre.common.util.NetworkUtil;
import com.honkidenihongo.pre.common.util.ToolbarUtil;
import com.honkidenihongo.pre.gui.listener.NetworkConnectionCallback;
import com.honkidenihongo.pre.gui.widget.FixedViewPager;
import com.honkidenihongo.pre.gui.widget.HelperDialog;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.constant.LanguageNumberCode;

/**
 * Màn hình thực hiện chức năng Ranking.
 *
 * @author binh.dt modify.
 * @since 30-Nov-2016.
 */
public class S13Ranking_Activity extends AppCompatActivity {
    private static final String LOG_TAG = S13Ranking_Activity.class.getName();
    public static final String S13_LESSON_OBJECT = "S13_LESSON_OBJECT";

    // For View.
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private FixedViewPager mViewPager;
    private AppCompatTextView mTvTitleToolbar;

    private HelperDialog mHelperDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nếu data nhận được bằng null thì finish màn hình.
        if (getDataIntent() == null) {
            finish();
            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Get layout for window.
        setContentView(R.layout.s13_ranking_activity);

        /**
         * Init View.
         */
        initView();

        // Init toolbar.
        ToolbarUtil.initToolbar(mToolbar, this);

        // Set data.
        setData();

        /**
         * Show dialog help.
         */
        showDialogHelp();
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
            finish();
        }

        return true;
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
    public void finish() {
        super.finish();

        // Khử animation khi finish 2 activity cùng 1 lúc.
        overridePendingTransition(0, 0);
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

                boolean isShowFirstOfScreen = prefs.getBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S13_RANKING, true);

                if (isShowFirstOfScreen) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S13_RANKING, false);
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
     * Initialize all view of content layout
     */
    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mTabLayout = (TabLayout) findViewById(R.id.mTabLayout);
        mViewPager = (FixedViewPager) findViewById(R.id.mViewPager);
        mTvTitleToolbar = (AppCompatTextView) mToolbar.findViewById(R.id.mTvTitleToolbar);
        mHelperDialog = new HelperDialog(this, R.style.TransparentDialog, getString(R.string.common_help__s13_user_ranking__title), getString(R.string.common_help__s13_user_ranking__content));
    }

    /**
     * Set data for screen.
     */
    private void setData() {
        Lesson lesson = getDataIntent();

        // Lấy tên bài học dựa theo ngôn ngữ.
        String lessonName = "";

        if (LocaleHelper.getLanguage(this).equals(Definition.LanguageCode.ENGLISH)) {
            lessonName = LessonNameUtil.getLessonName(lesson, LanguageNumberCode.ENGLISH);
        } else {
            lessonName = LessonNameUtil.getLessonName(lesson, LanguageNumberCode.VIETNAMESE);
        }

        mTvTitleToolbar.setText(lessonName);

        // Kiểm tra kết nối mạng trước khi thực hiện add view.
        checkNetwork();
    }

    /**
     * Method kiểm tra kết nối mạng.
     */
    private void checkNetwork() {
        // Nếu có kết nối mạng.
        if (NetworkUtil.isAvailable(this)) {
            setupViewPager();
        } else {
            MessageDialogUtil.showNetworkUnavailableDialog(S13Ranking_Activity.this, new NetworkConnectionCallback() {
                        @Override
                        public void onTryAgain() {
                            checkNetwork();
                        }
                    }
            );
        }
    }

    /**
     * Nếu có mạng thì tiến hành load data cho 2 trang view pager của activity.
     */
    private void setupViewPager() {
        S13RankingPagerAdapter s13RankingPagerAdapter = new S13RankingPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(s13RankingPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    /**
     * Get data through Intent
     */
    private Lesson getDataIntent() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getParcelable(S13_LESSON_OBJECT);
        }

        return null;
    }

    /**
     * Class Custom Adapter for ViewPager.
     */
    private class S13RankingPagerAdapter extends FragmentPagerAdapter {

        /**
         * Constructor of Class.
         */
        private S13RankingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return S13RankingToday_Fragment.newInstance(getDataIntent());
            }

            return S13RankingWeek_Fragment.newInstance(getDataIntent());
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return S13Ranking_Activity.this.getString(R.string.common_app__time__today);

                case 1:
                    return S13Ranking_Activity.this.getString(R.string.common_app__time__this_week);
            }

            return super.getPageTitle(position);
        }
    }
}
