package com.honkidenihongo.pre.gui.practice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.BreadcrumbUtil;
import com.honkidenihongo.pre.common.util.ToolbarUtil;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.Result;
import com.honkidenihongo.pre.model.constant.LanguageNumberCode;
import com.honkidenihongo.pre.common.util.LessonNameUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen hiển thị danh sách PracticeStarting.
 * breadcrumb.
 *
 * @author binh.dt modify.
 * @since 27-Nov-2016.
 */
public class S20PracticeStarting_Activity extends AppCompatActivity {
    private static final int REQUEST_S21_PRACTICE_DETAIL = 101;
    public static final String S20_LESSON_PARCELABLE_OBJECT = "S20_LESSON_PARCELABLE_OBJECT";
    public static final String S20_TYPE_QUESTION = "S20_TYPE_QUESTION";

    // Value receive total number question user take mis.
    private final ArrayList<Result> mResultMistakes = new ArrayList<>();

    // For View.
    private Toolbar mToolbar;
    private AppCompatTextView mTvTitle;
    private AppCompatTextView mTvTitleContent;
    private RelativeLayout mViewStarting;
    private AppCompatImageView mImgStarting;
    private AppCompatTextView mTvTitleToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nếu đối tượng nhận được là null thì tắt màn hình và return.
        if (getLesson2IntentData() == null) {
            finish();

            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.s20_practice_starting_activity);

        // Init View.
        initView();

        // Init toolbar.
        ToolbarUtil.initToolbar(mToolbar, this);

        // Set data.
        setData();

        // Set event for View.
        setEvent();
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
            onBackPressed();
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_S21_PRACTICE_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                // Clear data old before add.
                mResultMistakes.clear();

                if (data.getExtras() != null) {
                    List<Result> listMistakeResult = data.getExtras().getParcelableArrayList(S21PracticeDetail_Activity.S21_LIST_QUESTION_USER_MISTAKE);

                    // Receive list question mistake.
                    if (listMistakeResult != null) {
                        mResultMistakes.addAll(listMistakeResult);
                    }
                }
            } else {
                // If người dùng không chọn Try Again ở màn hình kết quả thì tắt màn hình này.
                finish();
            }
        }
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
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mTvTitle = (AppCompatTextView) findViewById(R.id.mTvTitle);
        mTvTitleContent = (AppCompatTextView) findViewById(R.id.mTvTitleContent);
        mViewStarting = (RelativeLayout) findViewById(R.id.mViewStarting);
        mImgStarting = (AppCompatImageView) findViewById(R.id.mImgStarting);
        mTvTitleToolbar = (AppCompatTextView) mToolbar.findViewById(R.id.mTvTitleToolbar);
    }

    /**
     * Set Data.
     */
    private void setData() {
        Lesson lesson = getLesson2IntentData();

        setTitleScreen(lesson);

        //mTvTitleContent.setText(titleContent);
    }

    /**
     * Method set sự kiện for View.
     */
    protected void setEvent() {
        mViewStarting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToScreenS21PracticeDetail(getLesson2IntentData(), getTypeQuestion());

            }
        });

        mImgStarting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToScreenS21PracticeDetail(getLesson2IntentData(), getTypeQuestion());
            }
        });
    }

    /**
     * Go to screen ScreenS21PracticeDetail.
     */
    private void goToScreenS21PracticeDetail(Lesson lesson, int typeQuestion) {
        if (lesson != null) {
            Intent intent = new Intent(this, S21PracticeDetail_Activity.class);
            intent.putExtra(S21PracticeDetail_Activity.S21_LESSON_PARCELABLE_OBJECT, lesson);
            intent.putExtra(S21PracticeDetail_Activity.S21_TYPE_QUESTION, typeQuestion);
            intent.putParcelableArrayListExtra(S21PracticeDetail_Activity.S21_LIST_QUESTION_USER_MISTAKE, (ArrayList<? extends Parcelable>) mResultMistakes);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivityForResult(intent, REQUEST_S21_PRACTICE_DETAIL);
        }
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
        mTvTitle.setText(BreadcrumbUtil.getBreadcrumb(this, lesson, getString(R.string.common_module__practice)));
    }

    // Get data be send through intent.
    private Lesson getLesson2IntentData() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getParcelable(S20_LESSON_PARCELABLE_OBJECT);
        }

        return null;
    }

    // Get data be send through intent.
    private int getTypeQuestion() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getInt(S20_TYPE_QUESTION);
        }


        return 0;
    }
}
