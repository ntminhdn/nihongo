package com.honkidenihongo.pre.gui.lesson;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.LessonNameUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.common.util.MessageDialogUtil;
import com.honkidenihongo.pre.gui.MainActivity;
import com.honkidenihongo.pre.gui.listener.OnMainActivityListener;
import com.honkidenihongo.pre.gui.ranking.S13Ranking_Activity;
import com.honkidenihongo.pre.gui.trialtest.S24TrialTestStarting_Activity;
import com.honkidenihongo.pre.gui.widget.HelperDialog;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.constant.Category;
import com.honkidenihongo.pre.model.constant.LanguageNumberCode;
import com.honkidenihongo.pre.model.constant.Level;

/**
 * Screen hiển thị LessonCategory.
 *
 * @author binh.tt.
 * @since 08-Nov-2016.
 */
public class S07LessonCategory_Fragment extends Fragment {
    /**
     * The Tag for logging.
     */
    private static final String TAG = S07LessonCategory_Fragment.class.getName();
    private static final String SO7_LESSON_PARCELABLE_OBJECT = "SO7_LESSON_PARCELABLE_OBJECT";

    private OnMainActivityListener mActivityListener;

    private Context mContext;

    // For view.
    private AppCompatImageView mImgBasicWord;
    private AppCompatImageView mImgBasicSentence;
    private AppCompatImageView mImgAdvanceWord;
    private AppCompatImageView mImgAdvanceSentence;
    private AppCompatImageView mImgTest;
    private AppCompatImageView mImgRanking;

    private HelperDialog mHelperDialog;

    /**
     * Method receive object lesson.
     *
     * @param lesson Value.
     */
    public static S07LessonCategory_Fragment newInstance(Lesson lesson) {
        S07LessonCategory_Fragment fragment = new S07LessonCategory_Fragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(SO7_LESSON_PARCELABLE_OBJECT, lesson);
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
        return inflater.inflate(R.layout.s07_lesson_category_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        /**
         * Init View for layout.
         */
        initView(view);

        /**
         * Set event for View.
         */
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
            // Lấy tên bài học dựa theo ngôn ngữ.
            String lessonName = "";

            if (getLessonData() != null) {
                Lesson lesson = getLessonData();

                if (LocaleHelper.getLanguage(mContext).equals(Definition.LanguageCode.ENGLISH)) {
                    lessonName = LessonNameUtil.getLessonName(lesson, LanguageNumberCode.ENGLISH);
                } else {
                    lessonName = LessonNameUtil.getLessonName(lesson, LanguageNumberCode.VIETNAMESE);
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
            Log.d(TAG, e.getMessage());
        }

        super.onDestroy();
    }

    /**
     * Lấy đối tượng lesson nhận được.
     *
     * @return Lesson;
     */
    private Lesson getLessonData() {
        Bundle arguments = getArguments();

        if (arguments != null) {
            return arguments.getParcelable(SO7_LESSON_PARCELABLE_OBJECT);
        }

        return null;
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

                boolean isShowFirstOfScreen = prefs.getBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S07_LESSON_CATEGORY, true);

                if (isShowFirstOfScreen) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S07_LESSON_CATEGORY, false);
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
     * Phương thức khởi tạo view.
     */
    protected void initView(View view) {
        mImgBasicWord = (AppCompatImageView) view.findViewById(R.id.mImgBasicWord);
        mImgBasicSentence = (AppCompatImageView) view.findViewById(R.id.mImgBasicSentence);
        mImgAdvanceWord = (AppCompatImageView) view.findViewById(R.id.mImgAdvanceWord);
        mImgAdvanceSentence = (AppCompatImageView) view.findViewById(R.id.mImgAdvanceSentence);
        mImgTest = (AppCompatImageView) view.findViewById(R.id.mImgTest);
        mImgRanking = (AppCompatImageView) view.findViewById(R.id.mImgRanking);

        mHelperDialog = new HelperDialog(mContext, R.style.TransparentDialog, getString(R.string.common_help__s07_lesson_category__title), getString(R.string.common_help__s07_lesson_category__content));
    }

    /**
     * Phương thức set sự kiện cho view.
     */
    protected void setEvent() {
        mImgBasicWord.setOnTouchListener(new OnViewTouchListener());
        mImgBasicWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getLessonData() != null && mActivityListener != null) {
                    Lesson mLesson = getLessonData();
                    mLesson.setCategory(Category.UNIT_WORD);
                    mLesson.setLevel(Level.BASIC);
                    mActivityListener.goToScreenS19LessonCategory(mLesson);
                }
            }
        });

        mImgBasicSentence.setOnTouchListener(new OnViewTouchListener());
        mImgBasicSentence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getLessonData() != null && mActivityListener != null) {
                    Lesson mLesson = getLessonData();
                    mLesson.setCategory(Category.UNIT_SENTENCE);
                    mLesson.setLevel(Level.BASIC);
                    mActivityListener.goToScreenS19LessonCategory(mLesson);
                }
            }
        });

        mImgAdvanceWord.setOnTouchListener(new OnViewTouchListener());
        mImgAdvanceWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getLessonData() != null && mActivityListener != null) {
                    Lesson mLesson = getLessonData();
                    mLesson.setCategory(Category.UNIT_WORD);
                    mLesson.setLevel(Level.ADVANCE);
                    mActivityListener.goToScreenS19LessonCategory(mLesson);
                }
            }
        });

        mImgAdvanceSentence.setOnTouchListener(new OnViewTouchListener());
        mImgAdvanceSentence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getLessonData() != null && mActivityListener != null) {
                    Lesson mLesson = getLessonData();
                    mLesson.setCategory(Category.UNIT_SENTENCE);
                    mLesson.setLevel(Level.ADVANCE);
                    mActivityListener.goToScreenS19LessonCategory(mLesson);
                }
            }
        });

        mImgTest.setOnTouchListener(new OnViewTouchListener());
        mImgTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToScreenS24TrialTestStarting(getLessonData());
            }
        });

        mImgRanking.setOnTouchListener(new OnViewTouchListener());
        mImgRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoScreenS13Ranking(getLessonData());
            }
        });
    }

    /**
     * Go to screen S13Ranking.
     *
     * @param lesson Value lesson.
     */
    private void gotoScreenS13Ranking(Lesson lesson) {
        if (lesson != null) {
            Intent intent = new Intent(mContext, S13Ranking_Activity.class);
            intent.putExtra(S13Ranking_Activity.S13_LESSON_OBJECT, lesson);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            S07LessonCategory_Fragment.this.startActivity(intent);
        }
    }

    /**
     * Go to screen S24TrialTestStarting.
     *
     * @param lesson Value lesson.
     */
    private void goToScreenS24TrialTestStarting(Lesson lesson) {
        if (lesson != null) {
            Intent intent = new Intent(mContext, S24TrialTestStarting_Activity.class);
            intent.putExtra(S24TrialTestStarting_Activity.S24_LESSON_PARCELABLE_OBJECT, lesson);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            S07LessonCategory_Fragment.this.startActivity(intent);
        }
    }

    /**
     * Class set thay đổi màu của ảnh imageView khi bắt sự kiện onTouch không cần thêm drawable của ảnh.
     */
    private class OnViewTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Kiểm tra view đang xảy ra sự kiện có thuộc AppCompatImageView.
                    if (view instanceof AppCompatImageView) {
                        AppCompatImageView imageView = (AppCompatImageView) view;

                        // Overlay is black with transparency of 0x77 (119).
                        imageView.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                    }

                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Kiểm tra view đang xảy ra sự kiện có thuộc AppCompatImageView.
                    if (view instanceof AppCompatImageView) {
                        AppCompatImageView imageView = (AppCompatImageView) view;

                        // Clear the overlay.
                        imageView.getDrawable().clearColorFilter();
                        imageView.invalidate();
                    }

                    break;
            }

            return false;
        }
    }
}
