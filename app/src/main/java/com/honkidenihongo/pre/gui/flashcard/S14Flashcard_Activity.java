package com.honkidenihongo.pre.gui.flashcard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.LessonNameUtil;
import com.honkidenihongo.pre.common.util.LocalAppUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.common.util.MediaUtil;
import com.honkidenihongo.pre.common.util.MessageDialogUtil;
import com.honkidenihongo.pre.dac.dao.KnowledgeDao;
import com.honkidenihongo.pre.gui.widget.FixedViewPager;
import com.honkidenihongo.pre.common.util.ToolbarUtil;
import com.honkidenihongo.pre.gui.widget.HelperDialog;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.UserModel;
import com.honkidenihongo.pre.model.constant.Category;
import com.honkidenihongo.pre.model.constant.LanguageNumberCode;
import com.honkidenihongo.pre.model.constant.LessonStatus;
import com.honkidenihongo.pre.model.constant.LessonType;
import com.honkidenihongo.pre.service.AudioService;

import java.io.File;

/**
 * Screen display with Flashcard.
 *
 * @author binh.dt.
 * @since 25-Nov-2016.
 */
public class S14Flashcard_Activity extends AppCompatActivity {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S14Flashcard_Activity.class.getName();
    public static final String S14_LESSON_PARCELABLE_OBJECT = "S14_LESSON_PARCELABLE_OBJECT";

    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private FixedViewPager mViewPager;
    private AppCompatTextView mTvTitleToolbar;

    // Define variable about play audio service.
    private AudioService mAudioService;
    private boolean isBound = false;
    private HelperDialog mHelperDialog;

    // Connection to bind a service that play special audio.
    private ServiceConnection mAudioServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AudioService.AudioBinder binder = (AudioService.AudioBinder) iBinder;
            mAudioService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
            Log.e(LOG_TAG, "Connect Service error!");
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getLessonDataIntent() == null) {
            finish();
            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /**
         * Set layout for window.
         */
        setContentView(R.layout.s14_flashcard_activity);

        /**
         * Init View.
         */
        initView();

        // Init toolbar.
        ToolbarUtil.initToolbar(mToolbar, this);

        // Set data.
        setData(getLessonDataIntent());

        /**
         * Show dialog help.
         */
        if (getLessonDataIntent() != null && getLessonDataIntent().getLevel() == LessonType.UNIT) {
            showDialogHelp();
        }
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

        Intent bindServiceIntent = new Intent(this, AudioService.class);
        bindService(bindServiceIntent, mAudioServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        // Pause audio when pause activity.
        pauseAudio();

        // Hủy service.

        if (mAudioService != null && isBound) {
            mAudioService.setPlayAudioCallback(null);
            unbindService(mAudioServiceConnection);
            isBound = false;
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Hủy service.
        if (mAudioService != null && isBound) {
            mAudioService.setPlayAudioCallback(null);
            unbindService(mAudioServiceConnection);
            isBound = false;
        }

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
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }

        return true;
    }

    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(0, 0);
    }

    /**
     * Set data for screen.
     */
    private void setData(Lesson lesson) {

        // Lấy tên bài học dựa theo ngôn ngữ.
        String lessonName = "";
        if (LocaleHelper.getLanguage(this).equals(Definition.LanguageCode.ENGLISH)) {
            lessonName = LessonNameUtil.getLessonName(lesson, LanguageNumberCode.ENGLISH);
        } else {
            lessonName = LessonNameUtil.getLessonName(lesson, LanguageNumberCode.VIETNAMESE);
        }

        mTvTitleToolbar.setText(lessonName);

        FlashcardPagerAdapter flashcardPagerAdapter = new FlashcardPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(flashcardPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
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

                boolean isShowFirstOfScreen = prefs.getBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S14_FLASH_CASH, true);

                if (isShowFirstOfScreen) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S14_FLASH_CASH, false);
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
     * Play mp3 with file path.
     *
     * @param knowledgeDao Value object.
     */
    public void playAudioWithPath(KnowledgeDao knowledgeDao) {
        if (knowledgeDao != null && !TextUtils.isEmpty(knowledgeDao.voice_file)) {
            File fileAudio = getFileAudio(knowledgeDao.category, knowledgeDao.voice_file);

            // Check file tồn tại trong device hay ko?
            if (fileAudio != null && fileAudio.exists()) {
                if (mAudioService != null && isBound) {
                    mAudioService.playAudioWithPath(fileAudio.getPath());
                }
            }
        }
    }

    /**
     * Check file is Exits.
     *
     * @return File.
     */
    public File getFileAudio(int category, String nameFile) {
        UserModel userModel = LocalAppUtil.getLastLoginUserInfo(this);

        if (TextUtils.isEmpty(nameFile) || getLessonDataIntent() == null || userModel == null) {
            return null;
        }

        return MediaUtil.audioIsPrepare(this, getLessonDataIntent(), userModel, category, nameFile);
    }

    /**
     * Get data through Intent
     */
    private Lesson getLessonDataIntent() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getParcelable(S14_LESSON_PARCELABLE_OBJECT);
        }

        return null;
    }

    /**
     * Initialize all view of content layout
     */
    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mTabLayout = (TabLayout) findViewById(R.id.mTabLayout);
        mViewPager = (FixedViewPager) findViewById(R.id.mViewPager);
        mTvTitleToolbar = (AppCompatTextView) mToolbar.findViewById(R.id.mTvTitleToolbar);
        mHelperDialog = new HelperDialog(this, R.style.TransparentDialog, getString(getLessonDataIntent().getCategory() == Category.UNIT_WORD ? R.string.common_help__s14_flashcard__word__title : R.string.common_help__s14_flashcard__sentence__title),
                getString(getLessonDataIntent().getCategory() == Category.UNIT_WORD ? R.string.common_help__s14_flashcard__word__content : R.string.common_help__s14_flashcard__sentence__content));
    }

    /**
     * Handle pause audio from service
     */
    public void pauseAudio() {
        if (mAudioService != null && isBound) {
            mAudioService.pause();
        }
    }

    /**
     * Class Custom Adapter for ViewPager.
     */
    private class FlashcardPagerAdapter extends FragmentPagerAdapter {

        /**
         * Constructor of Class.
         */
        private FlashcardPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return S14Flashcard_All_Fragment.newInstance(getLessonDataIntent());
            }

            return S14Flashcard_Note_Fragment.newInstance(getLessonDataIntent());
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return S14Flashcard_Activity.this.getString(R.string.s14_flashcard__tab_all);

                case 1:
                    return S14Flashcard_Activity.this.getString(R.string.s14_flashcard__tab_note);
            }

            return super.getPageTitle(position);
        }
    }
}
