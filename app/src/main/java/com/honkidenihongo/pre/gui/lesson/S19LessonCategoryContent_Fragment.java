package com.honkidenihongo.pre.gui.lesson;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.MessageDialogUtil;
import com.honkidenihongo.pre.dac.dao.GrammarDao;
import com.honkidenihongo.pre.dac.dao.KnowledgeDao;
import com.honkidenihongo.pre.dac.dao.QuestionDao;
import com.honkidenihongo.pre.gui.widget.HelperDialog;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.constant.Category;
import com.honkidenihongo.pre.model.constant.LanguageNumberCode;
import com.honkidenihongo.pre.model.constant.LessonType;
import com.honkidenihongo.pre.model.constant.Level;
import com.honkidenihongo.pre.gui.MainActivity;
import com.honkidenihongo.pre.gui.flashcard.S14Flashcard_Activity;
import com.honkidenihongo.pre.gui.knowledge.S15KnowledgeList_Activity;
import com.honkidenihongo.pre.gui.practice.S11PracticeList_Activity;
import com.honkidenihongo.pre.gui.listener.OnMainActivityListener;
import com.honkidenihongo.pre.common.util.LessonNameUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Hiển thị màn hình S19LessonCategoryContent.
 *
 * @author binh.tt.
 * @since 08-Nov-2016.
 */
public class S19LessonCategoryContent_Fragment extends Fragment {
    /**
     * The Tag for logging.
     */
    public static final String LOG_TAG = S19LessonCategoryContent_Fragment.class.getName();
    private static final String S19_LESSON_PARCELABLE_OBJECT = "S19_LESSON_PARCELABLE_OBJECT";

    private OnMainActivityListener mActivityListener;
    private HelperDialog mHelperDialog;
    private Context mContext;

    /* Define controls. */
    private AppCompatTextView mTvTitle;
    private AppCompatImageView mImgLogo;
    private RelativeLayout mBtnExplain;
    private RelativeLayout mBtnKnowledge;
    private RelativeLayout mBtnFlashcard;
    private RelativeLayout mBtnPractice;

    /**
     * Khởi tạo đối tượng S19LessonCategoryContent.
     *
     * @param lesson Đối tượng nhận vào.
     * @return Đối tượng S19LessonCategoryContent.
     */
    public static S19LessonCategoryContent_Fragment newInstance(Lesson lesson) {
        S19LessonCategoryContent_Fragment fragment = new S19LessonCategoryContent_Fragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(S19_LESSON_PARCELABLE_OBJECT, lesson);
        fragment.setArguments(arguments);

        return fragment;
    }

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
        return inflater.inflate(R.layout.s19_lesson_category_content_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Method using create View inside layout.
        initView(view);

        // Method using load data to View.
        if (getLessonData() != null) {
            setData(getLessonData());
        }

        // Method using set event for View child inside Layout.
        setEvent();

        /**
         * Show dialog help.
         */
        if (getLessonData() != null && getLessonData().getLevel() == LessonType.UNIT) {
            showDialogHelp();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mActivityListener != null) {
            // Lấy tên bài học dựa theo ngôn ngữ.
            String lessonName = "";

            if (getLessonData() != null) {
                Lesson mLesson = getLessonData();

                if (LocaleHelper.getLanguage(mContext).equals(Definition.LanguageCode.ENGLISH)) {
                    lessonName = LessonNameUtil.getLessonName(mLesson, LanguageNumberCode.ENGLISH);
                } else {
                    lessonName = LessonNameUtil.getLessonName(mLesson, LanguageNumberCode.VIETNAMESE);
                }
            }

            mActivityListener.setTitleScreen(lessonName);
        }
    }

    @Override
    public void onDestroy() {
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

                boolean isShowFirstOfScreen = prefs.getBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S19_LESSON_CATEGORY_CONTENT, true);

                if (isShowFirstOfScreen) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S19_LESSON_CATEGORY_CONTENT, false);
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
     * Lấy đối tượng lesson nhận được.
     *
     * @return Lesson;
     */
    private Lesson getLessonData() {
        Bundle arguments = getArguments();

        if (arguments != null) {
            return arguments.getParcelable(S19_LESSON_PARCELABLE_OBJECT);
        }

        return null;
    }

    /**
     * Method using init View.
     */
    protected void initView(View view) {
        mTvTitle = (AppCompatTextView) view.findViewById(R.id.mTvTitle);
        mImgLogo = (AppCompatImageView) view.findViewById(R.id.mImgLogo);
        mBtnExplain = (RelativeLayout) view.findViewById(R.id.mBtnExplain);
        mBtnKnowledge = (RelativeLayout) view.findViewById(R.id.mBtnKnowledge);
        mBtnFlashcard = (RelativeLayout) view.findViewById(R.id.mBtnFlashcard);
        mBtnPractice = (RelativeLayout) view.findViewById(R.id.mBtnPractice);
        mHelperDialog = new HelperDialog(mContext, R.style.TransparentDialog, getString(getLessonData().getCategory() == Category.UNIT_WORD ? R.string.common_help__s19_lesson_category_content__word__title : R.string.common_help__s19_lesson_category_content__sentence__title),
                getString(getLessonData().getCategory() == Category.UNIT_WORD ? R.string.common_help__s19_lesson_category_content__word__content : R.string.common_help__s19_lesson_category_content__sentence__content));
    }

    /**
     * Method using set data for View.
     */
    protected void setData(Lesson lesson) {
        // Set title for screen.
        if (lesson.getType() == LessonType.PRE_LESSON) {
            switch (lesson.getCategory()) {
                case Category.PRE_HIRAGANA:
                    mTvTitle.setText(getString(R.string.common_app__category__pre_hiragana));
                    break;

                case Category.PRE_KATAKANA:
                    mTvTitle.setText(getString(R.string.common_app__category__pre_katakna));
                    break;

                case Category.PRE_NUMBER:
                    mTvTitle.setText(getString(R.string.common_app__category__pre_number));
                    break;

                case Category.PRE_COMMON:
                    mTvTitle.setText(getString(R.string.common_app__category__pre_common));
                    break;
            }
        } else {
            switch (lesson.getCategory()) {
                case Category.UNIT_WORD:
                    switch (lesson.getLevel()) {
                        case Level.BASIC:
                            mTvTitle.setText(String.format("%s" + Definition.General.BREADCRUMB_SEPARATOR + "%s", getString(R.string.common_app__level__basic), getString(R.string.common_app__category__unit_word)));
                            break;

                        case Level.ADVANCE:
                            mTvTitle.setText(String.format("%s" + Definition.General.BREADCRUMB_SEPARATOR + "%s", getString(R.string.common_app__level__advance), getString(R.string.common_app__category__unit_word)));
                            break;
                    }
                    break;

                case Category.UNIT_SENTENCE:
                    switch (lesson.getLevel()) {
                        case Level.BASIC:
                            mTvTitle.setText(String.format("%s" + Definition.General.BREADCRUMB_SEPARATOR + "%s", getString(R.string.common_app__level__basic), getString(R.string.common_app__category__unit_sentence)));
                            break;

                        case Level.ADVANCE:
                            mTvTitle.setText(String.format("%s" + Definition.General.BREADCRUMB_SEPARATOR + "%s", getString(R.string.common_app__level__advance), getString(R.string.common_app__category__unit_sentence)));
                            break;
                    }
                    break;
            }
        }

        mBtnExplain.setVisibility(lesson.getLevel() == Level.BASIC && lesson.getCategory() == Category.UNIT_SENTENCE ? View.VISIBLE : View.GONE);

        if (lesson.getType() == LessonType.PRE_LESSON) {
            mImgLogo.setImageResource(R.drawable.s07_lesson_category_word);
        } else {
            mImgLogo.setImageResource(lesson.getCategory() == Category.UNIT_WORD ? R.drawable.s07_lesson_category_word : R.drawable.s07_lesson_category_sentence);
        }
    }

    /**
     * Method using set Event for View inside screen.
     */
    public void setEvent() {
        mBtnExplain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mActivityListener != null && getLessonData() != null) {
                    RealmResults<GrammarDao> grammarDaos = Realm.getDefaultInstance().where(GrammarDao.class)
                            .equalTo(Definition.Database.Grammar.GRAMMAR_FIELD_LESSON_NUMBER, getLessonData().getNumber())
                            .findAll();

                    if (grammarDaos.isEmpty()) {
                        MessageDialogUtil.showMessageNoData(mContext, mContext.getString(R.string.common_msg__content_info__have_no_data));
                    } else {
                        mActivityListener.goToScreenS27GrammarList(getLessonData());
                    }
                }
            }
        });

        mBtnKnowledge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getLessonData() != null) {
                    goToScreenS15KnowledgeList(getLessonData());
                }
            }
        });

        mBtnFlashcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getLessonData() != null) {
                    goToScreenS14FlashCash(getLessonData());
                }
            }
        });

        mBtnPractice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getLessonData() != null) {
                    goToScreenS11Practice(getLessonData());
                }
            }
        });
    }

    /**
     * Go to screen ScreenS11Practice.
     */
    private void goToScreenS11Practice(Lesson lesson) {
        if (lesson != null) {
            RealmResults<QuestionDao> questionDaos = Realm.getDefaultInstance().where(QuestionDao.class)
                    .equalTo(Definition.Database.Question.QUESTION_FIELD_LESSON_NUMBER, lesson.getNumber())
                    .equalTo(Definition.Database.Lesson.LESSON_FIELD_LEVEL, lesson.getLevel())
                    .equalTo(Definition.Database.Lesson.LESSON_FIELD_CATEGORY, lesson.getCategory())
                    .findAll();

            if (!questionDaos.isEmpty()) {
                Intent intent = new Intent(mContext, S11PracticeList_Activity.class);
                intent.putExtra(S11PracticeList_Activity.S11_LESSON_PARCELABLE_OBJECT, lesson);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                S19LessonCategoryContent_Fragment.this.startActivity(intent);
            } else {
                MessageDialogUtil.showMessageNoData(mContext, mContext.getString(R.string.common_msg__content_info__have_no_data));
            }
        }
    }

    /**
     * Go to screen S14FlashCash.
     */
    private void goToScreenS14FlashCash(Lesson lesson) {
        if (lesson != null) {
            RealmResults<KnowledgeDao> knowledgeDaos = Realm.getDefaultInstance().where(KnowledgeDao.class)
                    .equalTo(Definition.Database.Knowledge.KNOWLEDGE_FIELD_LESSON_NUMBER, lesson.getNumber())
                    .equalTo(Definition.Database.Lesson.LESSON_FIELD_LEVEL, lesson.getLevel())
                    .equalTo(Definition.Database.Lesson.LESSON_FIELD_CATEGORY, lesson.getCategory())
                    .findAll();

            if (!knowledgeDaos.isEmpty()) {
                Intent intent = new Intent(mContext, S14Flashcard_Activity.class);
                intent.putExtra(S14Flashcard_Activity.S14_LESSON_PARCELABLE_OBJECT, lesson);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                S19LessonCategoryContent_Fragment.this.startActivity(intent);
            } else {
                MessageDialogUtil.showMessageNoData(mContext, mContext.getString(R.string.common_msg__content_info__have_no_data));
            }
        }
    }

    /**
     * Go to screen S15KnowledgeList.
     */
    private void goToScreenS15KnowledgeList(Lesson lesson) {
        if (lesson != null) {
            // Kiểm tra dữ liệu có hay ko trước khi start screen.
            RealmResults<KnowledgeDao> knowledgeDaos = Realm.getDefaultInstance().where(KnowledgeDao.class)
                    .equalTo(Definition.Database.Knowledge.KNOWLEDGE_FIELD_LESSON_NUMBER, lesson.getNumber())
                    .equalTo(Definition.Database.Lesson.LESSON_FIELD_LEVEL, lesson.getLevel())
                    .equalTo(Definition.Database.Lesson.LESSON_FIELD_CATEGORY, lesson.getCategory())
                    .findAll();

            if (!knowledgeDaos.isEmpty()) {
                Intent intent = new Intent(mContext, S15KnowledgeList_Activity.class);
                intent.putExtra(S15KnowledgeList_Activity.S15_LESSON_PARCELABLE_OBJECT, lesson);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                S19LessonCategoryContent_Fragment.this.startActivity(intent);
            } else {
                MessageDialogUtil.showMessageNoData(mContext, mContext.getString(R.string.common_msg__content_info__have_no_data));
            }
        }
    }
}
