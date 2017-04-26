package com.honkidenihongo.pre.gui.practice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.adapter.S22PracticeResult_Adapter;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.BreadcrumbUtil;
import com.honkidenihongo.pre.common.util.LessonNameUtil;
import com.honkidenihongo.pre.common.util.LocalAppUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.common.util.MediaUtil;
import com.honkidenihongo.pre.common.util.ToolbarUtil;
import com.honkidenihongo.pre.dac.dao.KnowledgeDao;
import com.honkidenihongo.pre.gui.knowledge.S23KnowledgeDetail_Activity;
import com.honkidenihongo.pre.gui.widget.DividerItemDecoration;
import com.honkidenihongo.pre.gui.widget.WrappingLinearLayoutManager;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.Result;
import com.honkidenihongo.pre.model.UserModel;
import com.honkidenihongo.pre.model.constant.LanguageNumberCode;
import com.honkidenihongo.pre.service.AudioService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Screen hiển thị danh sách PracticeResult.
 * breadcrumb.
 *
 * @author binh.dt modify.
 * @since 27-Nov-2016.
 */
public class S22PracticeResult_Activity extends AppCompatActivity {
    private static final String LOG_TAG = S22PracticeResult_Activity.class.getName();
    public static final String S22_LESSON_PARCELABLE_OBJECT = "S22_LESSON_PARCELABLE_OBJECT";
    public static final String S22_RESULT_LIST_PARCELABLE = "S22_RESULT_LIST_PARCELABLE";
    public static final String S22_TYPE_QUESTION = "S22_TYPE_QUESTION";

    private Realm mRealm;

    // For View.
    private Toolbar mToolbar;
    private AppCompatTextView mTvTitle;
    private AppCompatButton mTvTryAgainMistake;
    private AppCompatButton mTvTryAgainAll;
    private AppCompatButton mTvTryAgainAllGone;
    private RecyclerView mRecyclerViewResultList;
    private AppCompatTextView mTvTitleToolbar;

    // Define variable about play audio service.
    private AudioService mAudioService;
    private boolean isBound = false;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nếu đối tượng nhận được là null thì tắt màn hình và return.
        if (getLesson2IntentData() == null || getResults() == null || getResults().isEmpty()) {
            finish();

            return;
        }

        // Khởi tạo đối tượng realm.
        mRealm = Realm.getDefaultInstance();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /**
         * Set layout for window.
         */
        setContentView(R.layout.s22_practice_result_activity);

        // Init View.
        initView(getResults());

        // Init toolbar.
        ToolbarUtil.initToolbar(mToolbar, this);

        // Set data.
        setData(getResults());

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

        closeRealm();

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(menuItem);
    }

    /**
     * Init View.
     */
    private void initView(List<Result> resultList) {
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mTvTitle = (AppCompatTextView) findViewById(R.id.mTvTitle);
        mTvTryAgainMistake = (AppCompatButton) findViewById(R.id.mTvTryAgainMistake);
        mTvTryAgainAll = (AppCompatButton) findViewById(R.id.mTvTryAgainAll);
        mTvTryAgainAllGone = (AppCompatButton) findViewById(R.id.mTvTryAgainAllGone);
        mRecyclerViewResultList = (RecyclerView) findViewById(R.id.mRecyclerView);
        mTvTitleToolbar = (AppCompatTextView) mToolbar.findViewById(R.id.mTvTitleToolbar);

        AppCompatTextView mTvTotal = (AppCompatTextView) findViewById(R.id.mTvTotal);
        AppCompatTextView mTvCorrect = (AppCompatTextView) findViewById(R.id.mTvCorrect);
        AppCompatImageView mImgMax = (AppCompatImageView) findViewById(R.id.mImgMax);
        AppCompatImageView mImgSpace = (AppCompatImageView) findViewById(R.id.mImgSpace);

        // Tính số câu trả lời đúng.
        int correct = valueCorrect(resultList);

        mTvCorrect.setText(String.valueOf(correct));
        mTvTotal.setText(String.valueOf(resultList.size()));

        mTvTotal.setVisibility(correct == resultList.size() ? View.GONE : View.VISIBLE);
        mTvCorrect.setVisibility(correct == resultList.size() ? View.GONE : View.VISIBLE);
        mImgSpace.setVisibility(correct == resultList.size() ? View.GONE : View.VISIBLE);
        mImgMax.setVisibility(correct == resultList.size() ? View.VISIBLE : View.GONE);

        mTvTryAgainMistake.setVisibility(resultList.size() - correct == 0 ? View.GONE : View.VISIBLE);

        // Trường hợp ko sai câu nào thì hiện view trail all giả lên, ẩn 2 view thực đi.
        if (mTvTryAgainMistake.getVisibility() == View.GONE) {
            mTvTryAgainAll.setVisibility(View.GONE);
            mTvTryAgainAllGone.setVisibility(View.VISIBLE);
        }

        if(correct == resultList.size()){
            saveArmorialToRealm();
        }
    }

    private void saveArmorialToRealm() {

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
     * Tính câu trả lời đúng.
     *
     * @return Số câu đúng.
     */
    private int valueCorrect(List<Result> resultList) {
        int total = 0;

        if (resultList.isEmpty()) {
            return total;
        }

        for (int i = 0; i < resultList.size(); i++) {
            if (resultList.get(i).is_correct()) {
                total = total + 1;
            }
        }

        return total;
    }

    /**
     * Get list question mistake.
     *
     * @param resultList List mistake.
     * @return List question.
     */
    private List<Result> getListQuestionMistake(List<Result> resultList) {
        List<Result> listMistakes = new ArrayList<>();

        for (int i = 0; i < resultList.size(); i++) {
            if (!resultList.get(i).is_correct()) {
                listMistakes.add(resultList.get(i));
            }
        }

        return listMistakes;
    }

    /**
     * Set Data.
     */
    private void setData(List<Result> resultList) {
        Lesson lesson = getLesson2IntentData();

        setTitleScreen(lesson);

        S22PracticeResult_Adapter s22PracticeResult_adapter = new S22PracticeResult_Adapter(this, resultList, getTypeQuestion());

        Drawable dividerDrawable = ContextCompat.getDrawable(this, R.drawable.bg_list_line_divider);
        mRecyclerViewResultList.addItemDecoration(new DividerItemDecoration(dividerDrawable));
        mRecyclerViewResultList.setLayoutManager(new WrappingLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerViewResultList.setNestedScrollingEnabled(false);
        mRecyclerViewResultList.setFocusable(false);
        mRecyclerViewResultList.setAdapter(s22PracticeResult_adapter);
    }

    /**
     * Phương thức dừng để gọi từ adapter của class {@link S22PracticeResult_Adapter}.
     *
     * @param position Value int.
     */
    public void onAdapterItemClick(int position) {
        // Đi đến màn hình Knowledge Detail Activity.
        Lesson lesson = getLesson2IntentData();

        if (lesson != null) {
            // Tìm ra vị trí hiện tại của con knowledge detail trước khi goto đến nó.
            S22PracticeResult_Adapter s22PracticeResult_adapter = (S22PracticeResult_Adapter) mRecyclerViewResultList.getAdapter();

            if (s22PracticeResult_adapter != null && s22PracticeResult_adapter.getItem(position) != null) {
                RealmResults<KnowledgeDao> knowledgeDaos = mRealm.where(KnowledgeDao.class)
                        .equalTo(Definition.Database.Question.QUESTION_FIELD_LESSON_NUMBER, lesson.getNumber())
                        .equalTo(Definition.Database.Lesson.LESSON_FIELD_LEVEL, lesson.getLevel())
                        .equalTo(Definition.Database.Lesson.LESSON_FIELD_CATEGORY, lesson.getCategory())
                        .findAll();

                // Tìm con knowledgeDao Detail và vị trí của nó trong mảng.
                KnowledgeDao knowledgeDao = mRealm.where(KnowledgeDao.class)
                        .equalTo(Definition.Database.Question.QUESTION_FIELD_LESSON_NUMBER, lesson.getNumber())
                        .equalTo(Definition.Database.Lesson.LESSON_FIELD_LEVEL, lesson.getLevel())
                        .equalTo(Definition.Database.Lesson.LESSON_FIELD_CATEGORY, lesson.getCategory())
                        .equalTo(Definition.Database.Knowledge.KNOWLEDGE_FIELD_NUMBER, s22PracticeResult_adapter.getItem(position).knowledge_number)
                        .findFirst();

                if (!knowledgeDaos.isEmpty() && knowledgeDao != null) {
                    if (knowledgeDaos.contains(knowledgeDao)) {
                        int index = knowledgeDaos.indexOf(knowledgeDao);

                        Intent intent = new Intent(this, S23KnowledgeDetail_Activity.class);
                        intent.putExtra(S23KnowledgeDetail_Activity.S23_LESSON_PARCELABLE_OBJECT, lesson);
                        intent.putExtra(S23KnowledgeDetail_Activity.S23_POSITION_CURRENT, index);
                        intent.putExtra(S23KnowledgeDetail_Activity.S23_GO_TO_FROM_SCREEN, 1);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                    }
                }
            }
        }
    }

    /**
     * Phương thức dừng để gọi từ adapter của class {@link S22PracticeResult_Adapter}.
     *
     * @param position Value int.
     */
    public void onImgSoundItemClick(int position) {
        playAudioWithPath(position);
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

        String breadcrumb = String.format("%s%s%s", BreadcrumbUtil.getBreadcrumb(this, lesson, getString(R.string.common_module__practice)), Definition.General.BREADCRUMB_SEPARATOR, getString(R.string.common_module__testing__result));

        mTvTitleToolbar.setText(lessonName);
        mTvTitle.setText(breadcrumb);
    }

    /**
     * Handle pause audio from service.
     */
    private void pauseAudio() {
        if (mAudioService != null && isBound) {
            mAudioService.pause();
        }
    }

    /**
     * Play audio with file path.
     *
     * @param position Value.
     */
    public void playAudioWithPath(int position) {
        S22PracticeResult_Adapter s22PracticeResult_adapter = (S22PracticeResult_Adapter) mRecyclerViewResultList.getAdapter();

        // Lấy tên file audio.
        if (s22PracticeResult_adapter != null && s22PracticeResult_adapter.getItem(position) != null) {
            Result result = s22PracticeResult_adapter.getItem(position);

            if (result == null) {
                return;
            }

            File fileAudio = getFileVoice(result.getCategory(), result.getAudio_data());

            if (fileAudio != null && fileAudio.exists()) {
                // Pause audio trước đó nếu có.
                pauseAudio();

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
    public File getFileVoice(int category, String nameFile) {
        UserModel userModel = LocalAppUtil.getLastLoginUserInfo(this);

        if (TextUtils.isEmpty(nameFile) || getLesson2IntentData() == null || userModel == null) {
            return null;
        }

        return MediaUtil.audioIsPrepare(this, getLesson2IntentData(), userModel, category, nameFile);
    }

    /**
     * Method set sự kiện for View.
     */
    private void setEvent() {
        mTvTryAgainMistake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Người dùng chọn thử lại thì set thông tin để activity trước bắt sự kiện.
                Intent intent = getIntent();
                intent.putParcelableArrayListExtra(S21PracticeDetail_Activity.S21_LIST_QUESTION_USER_MISTAKE, (ArrayList<? extends Parcelable>) getListQuestionMistake(getResults()));

                setResult(RESULT_OK, intent);
                finish();
            }
        });

        mTvTryAgainAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Người dùng chọn thử lại thì set thông tin để activity trước bắt sự kiện.
                Intent intent = getIntent();
                setResult(RESULT_OK, intent);

                finish();
            }
        });

        // Chức năng thay thế view trên bị gone đi.
        mTvTryAgainAllGone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Người dùng chọn thử lại thì set thông tin để activity trước bắt sự kiện.
                Intent intent = getIntent();
                setResult(RESULT_OK, intent);

                finish();
            }
        });
    }

    // Get data be send through intent.
    private Lesson getLesson2IntentData() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getParcelable(S22_LESSON_PARCELABLE_OBJECT);
        }

        return null;
    }

    // Get data be send through intent.
    private int getTypeQuestion() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getInt(S22_TYPE_QUESTION);
        }

        return 0;
    }

    // Get data be send through intent.
    private List<Result> getResults() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getParcelableArrayList(S22_RESULT_LIST_PARCELABLE);
        }

        return null;
    }
}
