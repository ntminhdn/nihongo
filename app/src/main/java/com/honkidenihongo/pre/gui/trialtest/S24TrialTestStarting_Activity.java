package com.honkidenihongo.pre.gui.trialtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.LessonNameUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.common.util.MessageDialogUtil;
import com.honkidenihongo.pre.common.util.ToolbarUtil;
import com.honkidenihongo.pre.dac.dao.QuestionDao;
import com.honkidenihongo.pre.dac.dao.TestDao;
import com.honkidenihongo.pre.gui.widget.HelperDialog;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.constant.LanguageNumberCode;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Screen hiển thị bắt đầu khi người dùng vào TrialTest.
 *
 * @author binh.dt modify.
 * @since 27-Nov-2016.
 */
public class S24TrialTestStarting_Activity extends AppCompatActivity {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S24TrialTestStarting_Activity.class.getName();
    private static final int REQUEST_S12_TRIAL_TEST_DETAIL = 101;
    public static final String S24_LESSON_PARCELABLE_OBJECT = "S24_LESSON_PARCELABLE_OBJECT";
    private Realm mRealm;

    // For View.
    private Toolbar mToolbar;
    private AppCompatTextView mTvTitle;
    private AppCompatTextView mTvTitleContent;
    private RelativeLayout mViewStarting;
    private AppCompatImageView mImgStarting;
    private AppCompatTextView mTvTitleToolbar;

    private HelperDialog mHelperDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nếu không lấy được lesson thì tắt màn hình hiện tại.
        if (getLessonIntentData() == null) {
            finish();

            return;
        }

        // Khởi tạo đối tượng realm.
        mRealm = Realm.getDefaultInstance();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /**
         * Set layout for window.
         */
        setContentView(R.layout.s24_trial_test_starting_activity);

        // Init View.
        initView();

        // Init toolbar.
        ToolbarUtil.initToolbar(mToolbar, this);

        // Set data.
        setData(getLessonIntentData());

        // Set event for View.
        setEvent();

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
    protected void onDestroy() {
        try {
            closeRealm();

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
            onBackPressed();
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_S12_TRIAL_TEST_DETAIL) {
            if (resultCode != Activity.RESULT_OK) {
                // If người dùng không chọn Try Again ở màn hình kết quả @S17TrialTestResult_Activity thì tắt màn hình này.
                finish();
            }
        }
    }

    @Override
    public void finish() {
        super.finish();

        // Khử animation khi finish 2 activity cùng 1 lúc.
        overridePendingTransition(0, 0);
    }

    /**
     * Init View.
     */
    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mTvTitle = (AppCompatTextView) findViewById(R.id.mTvTitle);
        mTvTitleContent = (AppCompatTextView) findViewById(R.id.mTvTitleContent);
        mViewStarting = (RelativeLayout) findViewById(R.id.mViewStarting);
        mImgStarting = (AppCompatImageView) findViewById(R.id.mImgStarting);
        mTvTitleToolbar = (AppCompatTextView) mToolbar.findViewById(R.id.mTvTitleToolbar);
        mHelperDialog = new HelperDialog(this, R.style.TransparentDialog, getString(R.string.common_help__s24_trial_test_starting__title), getString(R.string.common_help__s24_trial_test_starting__content));
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

                boolean isShowFirstOfScreen = prefs.getBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S24_TRIAL_TEST, true);

                if (isShowFirstOfScreen) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S24_TRIAL_TEST, false);
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
     * Close database.
     */
    private void closeRealm() {
        if (mRealm != null && !mRealm.isClosed()) {
            try {
                mRealm.close();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Couldn't close realm.");
            }

            mRealm = null;
        }
    }

    /**
     * Set Data.
     *
     * @param lesson Value receive.
     */
    private void setData(Lesson lesson) {
        setTitleScreen(lesson);

        // Hiển thị mô tả nội dung bài test trong màn hình hiện tại.
        TestDao testDao = mRealm.where(TestDao.class)
                .equalTo(Definition.Database.Lesson.LESSON_FIELD_LESSON_ID, lesson.getId())
                .findFirst();

        if (testDao != null) {
            // Lấy description theo ngôn ngữ.
            String description = "";
            if (LocaleHelper.getLanguage(this).equals(Definition.LanguageCode.ENGLISH)) {
                description = testDao.description_en;
            } else {
                description = testDao.description_vi;
            }

            mTvTitleContent.setText(description);
        }
    }

    /**
     * Method set sự kiện for View.
     */
    protected void setEvent() {
        mViewStarting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getLessonIntentData() != null) {
                    goToScreenS12TrialTestDetail(getLessonIntentData());
                }
            }
        });

        mImgStarting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getLessonIntentData() != null) {
                    goToScreenS12TrialTestDetail(getLessonIntentData());
                }
            }
        });
    }

    /**
     * Go to screen ScreenS12TrialTestDetail.
     *
     * @param lesson Value lesson.
     */
    private void goToScreenS12TrialTestDetail(Lesson lesson) {
        if (lesson != null) {
            RealmResults<QuestionDao> questionDaos = mRealm.where(QuestionDao.class)
                    .equalTo(Definition.Database.Question.QUESTION_FIELD_LESSON_NUMBER, lesson.getNumber())
                    .findAll();

            if (questionDaos.isEmpty()) {
                MessageDialogUtil.showMessageNoData(this, getString(R.string.common_msg__content_info__have_no_data));
            } else {
                Intent intent = new Intent(this, S12TrialTestDetail_Activity.class);
                intent.putExtra(S12TrialTestDetail_Activity.S12_LESSON_OBJECT, lesson);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                // Start activity và lắng nghe sự kiện trả về.
                startActivityForResult(intent, REQUEST_S12_TRIAL_TEST_DETAIL);
            }
        }
    }

    /**
     * Method set nội dung hiện thị cho tiêu đề màn hình.
     *
     * @param lesson Value lesson.
     */
    private void setTitleScreen(Lesson lesson) {
        // Lấy tên bài học dựa theo ngôn ngữ.
        String lessonName = "";

        if (LocaleHelper.getLanguage(this).equals(Definition.LanguageCode.ENGLISH)) {
            lessonName = LessonNameUtil.getLessonName(lesson, LanguageNumberCode.ENGLISH);
        } else {
            lessonName = LessonNameUtil.getLessonName(lesson, LanguageNumberCode.VIETNAMESE);
        }

        mTvTitleToolbar.setText(lessonName);

        String title = getString(R.string.common_module__testing__trial_title);
        mTvTitle.setText(title);
    }

    // Get data be send through intent.
    private Lesson getLessonIntentData() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getParcelable(S24_LESSON_PARCELABLE_OBJECT);
        }

        return null;
    }
}
