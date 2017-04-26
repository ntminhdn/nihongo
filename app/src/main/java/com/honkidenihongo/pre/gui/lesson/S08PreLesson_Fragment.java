package com.honkidenihongo.pre.gui.lesson;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.LessonNameUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.gui.MainActivity;
import com.honkidenihongo.pre.gui.listener.OnMainActivityListener;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.constant.Category;
import com.honkidenihongo.pre.model.constant.LanguageNumberCode;

/**
 * Hiển thị màn hình S08PreLesson.
 *
 * @author binh.tt.
 * @since 08-Nov-2016.
 */
public class S08PreLesson_Fragment extends Fragment {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S08PreLesson_Fragment.class.getName();
    private static final String SO8_LESSON_PARCELABLE_OBJECT = "SO8_LESSON_PARCELABLE_OBJECT";

    /* Define controls. */
    private AppCompatTextView mBtnHiragana;
    private AppCompatTextView mBtnKatakana;
    private AppCompatTextView mBtnNumber;
    private AppCompatTextView mBtnCommonSentence;

    private OnMainActivityListener mActivityListener;
    private Context mContext;

    /**
     * Method receive object lesson.
     *
     * @param lesson Value.
     */
    public static S08PreLesson_Fragment newInstance(Lesson lesson) {
        S08PreLesson_Fragment fragment = new S08PreLesson_Fragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(SO8_LESSON_PARCELABLE_OBJECT, lesson);
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
        return inflater.inflate(R.layout.s08_pre_lesson_category_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        /**
         * Call method initView.
         */
        initView(view);

        /**
         * Call method set event for View.
         */
        setEvent();
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

    /**
     * Lấy đối tượng lesson nhận được.
     *
     * @return Lesson;
     */
    private Lesson getLessonData() {
        Bundle arguments = getArguments();

        if (arguments != null) {
            return arguments.getParcelable(SO8_LESSON_PARCELABLE_OBJECT);
        }

        return null;
    }

    /**
     * Khởi tạo các controls cho layout.
     */
    private void initView(View view) {
        mBtnHiragana = (AppCompatTextView) view.findViewById(R.id.mBtnHiragana);
        mBtnKatakana = (AppCompatTextView) view.findViewById(R.id.mBtnKatakana);
        mBtnNumber = (AppCompatTextView) view.findViewById(R.id.mBtnNumber);
        mBtnCommonSentence = (AppCompatTextView) view.findViewById(R.id.mBtnCommonSentence);
    }

    /**
     * Method event click for View.
     */
    private void setEvent() {
        mBtnHiragana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getLessonData() != null && mActivityListener != null) {
                    Lesson mLesson = getLessonData();
                    mLesson.setCategory(Category.PRE_HIRAGANA);
                    mActivityListener.goToScreenS19LessonCategory(mLesson);
                }
            }
        });

        mBtnKatakana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getLessonData() != null && mActivityListener != null) {
                    Lesson mLesson = getLessonData();
                    mLesson.setCategory(Category.PRE_KATAKANA);
                    mActivityListener.goToScreenS19LessonCategory(mLesson);
                }
            }
        });

        mBtnNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getLessonData() != null && mActivityListener != null) {
                    Lesson mLesson = getLessonData();
                    mLesson.setCategory(Category.PRE_NUMBER);
                    mActivityListener.goToScreenS19LessonCategory(mLesson);
                }
            }
        });

        mBtnCommonSentence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getLessonData() != null && mActivityListener != null) {
                    Lesson mLesson = getLessonData();
                    mLesson.setCategory(Category.PRE_COMMON);
                    mActivityListener.goToScreenS19LessonCategory(mLesson);
                }
            }
        });
    }
}
