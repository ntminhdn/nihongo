package com.honkidenihongo.pre.gui.practice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.adapter.S11PracticeList_Adapter;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.BreadcrumbUtil;
import com.honkidenihongo.pre.common.util.LessonNameUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.common.util.MessageDialogUtil;
import com.honkidenihongo.pre.dac.dao.QuestionDao;
import com.honkidenihongo.pre.common.util.ToolbarUtil;
import com.honkidenihongo.pre.gui.widget.HelperDialog;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.TypeQuestion;
import com.honkidenihongo.pre.model.constant.Category;
import com.honkidenihongo.pre.model.constant.LanguageNumberCode;
import com.honkidenihongo.pre.model.constant.QuestionType;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * Screen hiển thị danh sách Practice.
 * breadcrumb.
 *
 * @author binh.dt modify.
 * @since 27-Nov-2016.
 */
public class S11PracticeList_Activity extends AppCompatActivity {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S11PracticeList_Activity.class.getName();
    public static final String S11_LESSON_PARCELABLE_OBJECT = "S11_LESSON_PARCELABLE_OBJECT";
    private static final int LESSON_NUMBER_FOUR = 4;
    private HelperDialog mHelperDialog;
    private Realm mRealm;

    // For View.
    private RecyclerView mRecyclerViewPracticeList;
    private AppCompatTextView mTvTitleList;
    private AppCompatTextView mTvTitleToolbar;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nếu đối tượng nhận được là null thì tắt màn hình và return.
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
        setContentView(R.layout.s11_practice_list_activity);

        // Init View.
        initView();

        // Init toolbar.
        ToolbarUtil.initToolbar(mToolbar, this);

        // Set data.
        setData(getLessonIntentData());

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
            // Đóng database.
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

                boolean isShowFirstOfScreen = prefs.getBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S11_PRACTICE_LIST, true);

                if (isShowFirstOfScreen) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S11_PRACTICE_LIST, false);
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
     * Init View.
     */
    private void initView() {
        mRecyclerViewPracticeList = (RecyclerView) findViewById(R.id.mRecyclerView);
        mTvTitleList = (AppCompatTextView) findViewById(R.id.mTvTitleList);
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mTvTitleToolbar = (AppCompatTextView) mToolbar.findViewById(R.id.mTvTitleToolbar);
        mHelperDialog = new HelperDialog(this, R.style.TransparentDialog, getString(R.string.common_help__s11_practice_list__title), getString(R.string.common_help__s11_practice_list__content));
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
     * Method dung để kiểm tra ứng với question type hiện tại và lesson hiện tại có data hay ko?
     */
    private boolean getTypeQuestion(int questionType) {
        Lesson lesson = getLessonIntentData();

        if (lesson == null) {
            return false;
        }

        if (mRealm == null) {
            return false;
        }

        // Kiểm tra với questionType có data hay ko?
        return mRealm.where(QuestionDao.class)
                .equalTo(Definition.Database.Question.QUESTION_FIELD_LESSON_NUMBER, lesson.getNumber())
                .equalTo(Definition.Database.Lesson.LESSON_FIELD_LEVEL, lesson.getLevel())
                .equalTo(Definition.Database.Lesson.LESSON_FIELD_CATEGORY, lesson.getCategory())
                .equalTo(Definition.Database.Question.QUESTION_FIELD_TYPE, questionType)
                .findAll().isEmpty();
    }

    /**
     * Set Data.
     */
    private void setData(Lesson lesson) {

        setTitleScreen(lesson);

        List<TypeQuestion> typeQuestions = new ArrayList<>();

        // Todo tất cả nội dung hiển thị trên list ở version luôn lấy là tiếng nhật.
        if (lesson.getCategory() == Category.PRE_HIRAGANA || lesson.getCategory() == Category.PRE_KATAKANA) {

            if (!getTypeQuestion(QuestionType.TEXT_KANA_ROMAJI)) {
                String questionName_Hiragana = LocaleHelper.getStringByLocale(this, Definition.LanguageCode.VIETNAMESE, R.string.s11_practice_list__question_type_text__hiragana_romaji);
                String questionName_Katakana = LocaleHelper.getStringByLocale(this, Definition.LanguageCode.VIETNAMESE, R.string.s11_practice_list__question_type_text__katakana_romaji);

                String questionName = lesson.getCategory() == Category.PRE_HIRAGANA ? questionName_Hiragana : questionName_Katakana;

                typeQuestions.add(new TypeQuestion(questionName, QuestionType.TEXT_KANA_ROMAJI));
            }

            if (!getTypeQuestion(QuestionType.TEXT_ROMAJI_KANA)) {
                String questionName_Hiragana = LocaleHelper.getStringByLocale(this, Definition.LanguageCode.VIETNAMESE, R.string.s11_practice_list__question_type_text__romaji_hiragana);
                String questionName_Katakana = LocaleHelper.getStringByLocale(this, Definition.LanguageCode.VIETNAMESE, R.string.s11_practice_list__question_type_text__romaji_katakana);

                String questionName = lesson.getCategory() == Category.PRE_HIRAGANA ? questionName_Hiragana : questionName_Katakana;

                typeQuestions.add(new TypeQuestion(questionName, QuestionType.TEXT_ROMAJI_KANA));
            }

            if (!getTypeQuestion(QuestionType.VOICE_KANA_ROMAJI)) {
                String questionName_Hiragana = LocaleHelper.getStringByLocale(this, Definition.LanguageCode.VIETNAMESE, R.string.s11_practice_list__question_type_voice__hiragana_romaji);
                String questionName_Katakana = LocaleHelper.getStringByLocale(this, Definition.LanguageCode.VIETNAMESE, R.string.s11_practice_list__question_type_voice__katakana_romaji);

                String questionName = lesson.getCategory() == Category.PRE_HIRAGANA ? questionName_Hiragana : questionName_Katakana;

                typeQuestions.add(new TypeQuestion(questionName, QuestionType.VOICE_KANA_ROMAJI));
            }

            if (!getTypeQuestion(QuestionType.VOICE_KANA_KANA)) {
                String questionName_Hiragana = LocaleHelper.getStringByLocale(this, Definition.LanguageCode.VIETNAMESE, R.string.s11_practice_list__question_type_voice__hiragana_hiragana);
                String questionName_Katakana = LocaleHelper.getStringByLocale(this, Definition.LanguageCode.VIETNAMESE, R.string.s11_practice_list__question_type_voice__katakana_katakana);

                String questionName = lesson.getCategory() == Category.PRE_HIRAGANA ? questionName_Hiragana : questionName_Katakana;

                typeQuestions.add(new TypeQuestion(questionName, QuestionType.VOICE_KANA_KANA));
            }
        } else { // Ngược lại category của lesson khác Hiragana và katakana.
            if (!getTypeQuestion(QuestionType.TEXT_JA_NLANG)) {
                String questionName = LocaleHelper.getStringByLocale(this, Definition.LanguageCode.VIETNAMESE, R.string.s11_practice_list__question_type_text__ja_nlang);

                typeQuestions.add(new TypeQuestion(questionName, QuestionType.TEXT_JA_NLANG));
            }

            if (!getTypeQuestion(QuestionType.TEXT_NLANG_JA)) {
                String questionName = LocaleHelper.getStringByLocale(this, Definition.LanguageCode.VIETNAMESE, R.string.s11_practice_list__question_type_text__nlang_ja);

                typeQuestions.add(new TypeQuestion(questionName, QuestionType.TEXT_NLANG_JA));
            }

            // Từ bài học lesson 4 trở đi trường này sẽ không hiển thị
            if (lesson.getNumber() < LESSON_NUMBER_FOUR) {
                if (!getTypeQuestion(QuestionType.VOICE_JA_JA)) {
                    String questionName = LocaleHelper.getStringByLocale(this, Definition.LanguageCode.VIETNAMESE, R.string.s11_practice_list__question_type_voice__ja_ja);

                    typeQuestions.add(new TypeQuestion(questionName, QuestionType.VOICE_JA_JA));
                }
            }

            if (!getTypeQuestion(QuestionType.VOICE_JA_NLANG)) {
                String questionName = LocaleHelper.getStringByLocale(this, Definition.LanguageCode.VIETNAMESE, R.string.s11_practice_list__question_type_voice__ja_nlang);

                typeQuestions.add(new TypeQuestion(questionName, QuestionType.VOICE_JA_NLANG));
            }
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerViewPracticeList.setLayoutManager(layoutManager);
        mRecyclerViewPracticeList.setHasFixedSize(true);
        S11PracticeList_Adapter s11PracticeList_adapter = new S11PracticeList_Adapter(S11PracticeList_Activity.this, typeQuestions, null);
        mRecyclerViewPracticeList.setAdapter(s11PracticeList_adapter);
    }

    /**
     * Phương thức dừng để gọi từ adapter của class {@link S11PracticeList_Adapter}.
     *
     * @param position Value int.
     */
    public void onAdapterItemClick(int position) {
        S11PracticeList_Adapter s11PracticeList_adapter = (S11PracticeList_Adapter) mRecyclerViewPracticeList.getAdapter();

        if (s11PracticeList_adapter == null || getLessonIntentData() == null) {
            return;
        }

        TypeQuestion typeQuestion = s11PracticeList_adapter.getItem(position);

        if (typeQuestion == null) {
            return;
        }

        goToScreenS20PracticeStarting(getLessonIntentData(), typeQuestion.getTypeQuestion());
    }

    /**
     * Go to screen ScreenS20PracticeStarting.
     */
    private void goToScreenS20PracticeStarting(Lesson lesson, int typeQuestion) {
        if (lesson != null) {
            Intent intent = new Intent(this, S20PracticeStarting_Activity.class);
            intent.putExtra(S20PracticeStarting_Activity.S20_LESSON_PARCELABLE_OBJECT, lesson);
            intent.putExtra(S20PracticeStarting_Activity.S20_TYPE_QUESTION, typeQuestion);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }
    }

    // Get data be send through intent.
    private Lesson getLessonIntentData() {
        // Todo code cua framgia.
//        if (getIntent().getExtras() != null) {
//            mUnitDataID = getIntent().getExtras().getLong(Define.General.UNIT_DATA_ID, -1);
//        }

        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getParcelable(S11_LESSON_PARCELABLE_OBJECT);
        }

        return null;
    }

    /**
     * Set title for screen.
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
        mTvTitleList.setText(BreadcrumbUtil.getBreadcrumb(this, lesson, getString(R.string.common_module__practice)));
    }
}
