package com.honkidenihongo.pre.gui.practice;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.config.FontsConfig;
import com.honkidenihongo.pre.common.util.BreadcrumbUtil;
import com.honkidenihongo.pre.common.util.LessonNameUtil;
import com.honkidenihongo.pre.common.util.LocalAppUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.common.util.MathUtil;
import com.honkidenihongo.pre.common.util.MediaUtil;
import com.honkidenihongo.pre.common.util.ToolbarUtil;
import com.honkidenihongo.pre.dac.dao.ChoiceDao;
import com.honkidenihongo.pre.dac.dao.QuestionDao;
import com.honkidenihongo.pre.gui.widget.AutoResizeTextView;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.Result;
import com.honkidenihongo.pre.model.UserModel;
import com.honkidenihongo.pre.model.constant.LanguageNumberCode;
import com.honkidenihongo.pre.model.constant.QuestionType;
import com.honkidenihongo.pre.service.AudioService;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Screen hiển thị danh sách PracticeDetail.
 * breadcrumb.
 *
 * @author binh.dt modify.
 * @since 27-Nov-2016.
 */
public class S21PracticeDetail_Activity extends AppCompatActivity implements AudioService.PlayAudioCallback {
    private static final String LOG_TAG = S21PracticeDetail_Activity.class.getName();
    private static final int REQUEST_S22_PRACTICE_RESULT = 101;
    public static final String S21_LESSON_PARCELABLE_OBJECT = "S21_LESSON_PARCELABLE_OBJECT";
    public static final String S21_LIST_QUESTION_USER_MISTAKE = "S21_LIST_QUESTION_USER_MISTAKE";
    public static final String S21_TYPE_QUESTION = "S21_TYPE_QUESTION";
    private static final int MSG = 1;

    private final List<QuestionDao> mQuestionDaos = new ArrayList<>();
    private final List<List<ChoiceDao>> mChoiceDaos = new ArrayList<>();
    private final ArrayList<Result> mListResults = new ArrayList<>();

    private Realm mRealm;

    // For View.
    private Toolbar mToolbar;
    private AppCompatTextView mTvTitle;
    private AppCompatTextView mTvPosition;
    private AppCompatTextView mTvQuestion;
    private AutoResizeTextView mBtnAnswer1;
    private AutoResizeTextView mBtnAnswer2;
    private AutoResizeTextView mBtnAnswer3;
    private AppCompatImageView mImgStatus;
    private AppCompatImageButton mImgSound;
    private AppCompatTextView mTvTitleToolbar;
    private Typeface mTypeface;

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
            mAudioService.setPlayAudioCallback(S21PracticeDetail_Activity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
            mAudioService.setPlayAudioCallback(S21PracticeDetail_Activity.this);
            Log.e(LOG_TAG, "Connect Service error!");
        }
    };

    // Thread play audio at position.
    private Handler mHandlerAutoPlayAudio = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            synchronized (S21PracticeDetail_Activity.this) {
                if (msg.what == MSG) {
                    int position = msg.arg1;
                    playAudioWithPath(position);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nếu đối tượng nhận được là null thì tắt màn hình và return.
        if (getLesson2IntentData() == null) {
            finish();

            return;
        }

        // Khởi tạo đối tượng realm.
        mRealm = Realm.getDefaultInstance();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /**
         * Set layout for window.
         */
        setContentView(R.layout.s21_practice_detail_activity);

        // Init View.
        initView();

        // Init toolbar.
        ToolbarUtil.initToolbar(mToolbar, this);

        // Set data.
        setData(getLesson2IntentData());

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

        if (mHandlerAutoPlayAudio != null) {
            // Hủy đăng ký service.
            mHandlerAutoPlayAudio.removeMessages(MSG);
        }

        // Hủy service.
        if (mAudioService != null && isBound) {
            mAudioService.setPlayAudioCallback(null);
            unbindService(mAudioServiceConnection);
            isBound = false;
        }

        // Nếu người dùng đã chọn câu hỏi hiện tại thì gọi tiếp câu hỏi tiếp theo.
        if (mTvPosition.getTag() != null && mImgStatus.getDrawable() != null) {
            int positionCurrent = (int) mTvPosition.getTag();

            if (positionCurrent < mQuestionDaos.size() - 1) {
                showQuestionAtPosition(positionCurrent + 1);
            } else {
                goToScreenS22PracticeResult(getLesson2IntentData());
            }
        }

        // Enable các nút lựa chọn lên để khi người dùng mở lại điện thoại có thể tương tác lại như bình thường
        enableClickAnswers();

        // Đồng thời mở nút phát lại âm thanh lên như ban đầu.
        mImgSound.setEnabled(true);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mHandlerAutoPlayAudio != null) {
            // Hủy đối tượng và hủy đăng ký service.
            mHandlerAutoPlayAudio.removeMessages(MSG);
            mHandlerAutoPlayAudio = null;
        }

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
    public void finish() {
        super.finish();

        overridePendingTransition(0, 0);
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

        if (requestCode == REQUEST_S22_PRACTICE_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                // If người dùng chọn Try again.
                Intent intent = getIntent();

                // Push data list question user mistake.
                if (data.getExtras() != null) {
                    intent.putParcelableArrayListExtra(S21_LIST_QUESTION_USER_MISTAKE, data.getExtras().getParcelableArrayList(S21PracticeDetail_Activity.S21_LIST_QUESTION_USER_MISTAKE));
                }

                setResult(RESULT_OK, intent);
                finish();
            } else {
                finish();
            }
        }
    }

    /**
     * Init View.
     */
    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mTvTitle = (AppCompatTextView) findViewById(R.id.mTvTitle);
        mTvPosition = (AppCompatTextView) findViewById(R.id.mTvPosition);
        mTvQuestion = (AppCompatTextView) findViewById(R.id.mTvQuestion);
        mBtnAnswer1 = (AutoResizeTextView) findViewById(R.id.mBtnAnswer1);
        mBtnAnswer2 = (AutoResizeTextView) findViewById(R.id.mBtnAnswer2);
        mBtnAnswer3 = (AutoResizeTextView) findViewById(R.id.mBtnAnswer3);
        mImgStatus = (AppCompatImageView) findViewById(R.id.mImgStatus);
        mImgSound = (AppCompatImageButton) findViewById(R.id.mImgSound);
        mTvTitleToolbar = (AppCompatTextView) mToolbar.findViewById(R.id.mTvTitleToolbar);
        mTypeface = FontsConfig.getInstance(this).getFont(FontsConfig.AppFont.KLEE);
    }

    /**
     * Set Data.
     */
    private void setData(Lesson lesson) {

        setTitleScreen(lesson);

        List<QuestionDao> questionDaos = new ArrayList<>();

        // Kiểm tra user có làm lại số câu mistake hay ko?
        if (getQuestionUserMistake() != null && !getQuestionUserMistake().isEmpty()) {

            for (Result result : getQuestionUserMistake()) {
                // Find question user mistake.
                questionDaos.add(mRealm.where(QuestionDao.class)
                        .equalTo(Definition.Database.Question.QUESTION_FIELD_LESSON_NUMBER, lesson.getNumber())
                        .equalTo(Definition.Database.Lesson.LESSON_FIELD_LEVEL, lesson.getLevel())
                        .equalTo(Definition.Database.Lesson.LESSON_FIELD_CATEGORY, lesson.getCategory())
                        .equalTo(Definition.Database.Question.QUESTION_FIELD_TYPE, getTypeQuestion())
                        .equalTo(Definition.Database.Question.QUESTION_FIELD_ID, result.question_id)
                        .findFirst());
            }
        } else {
            // Tìm all List QuestionDao from lesson và type của question.
            questionDaos.addAll(mRealm.where(QuestionDao.class)
                    .equalTo(Definition.Database.Question.QUESTION_FIELD_LESSON_NUMBER, lesson.getNumber())
                    .equalTo(Definition.Database.Lesson.LESSON_FIELD_LEVEL, lesson.getLevel())
                    .equalTo(Definition.Database.Lesson.LESSON_FIELD_CATEGORY, lesson.getCategory())
                    .equalTo(Definition.Database.Question.QUESTION_FIELD_TYPE, getTypeQuestion())
                    .findAll());
        }

        // Tìm 3 câu trả lời ứng với mỗi câu hỏi.
        if (!questionDaos.isEmpty()) {
            // Random questions.
            List<QuestionDao> questionDaoRandoms = pickQuestionRandom(questionDaos);

            for (QuestionDao questionDao : questionDaoRandoms) {
                mQuestionDaos.add(questionDao);

                // Ứng với một câu hỏi sẽ có 3 câu trả lời.
                RealmResults<ChoiceDao> choiceDaos = mRealm.where(ChoiceDao.class)
                        .equalTo(Definition.Database.Choice.CHOICE_FIELD_QUESTION_ID, questionDao.id)
                        .findAll();

                if (!choiceDaos.isEmpty()) {
                    // Random mảng trả lời trước khi đưa nó vào list câu trả lời.
                    mChoiceDaos.add(pickChoiceRandom(choiceDaos));
                }
            }
        }

        if (!mQuestionDaos.isEmpty()) {
            showQuestionAtPosition(0);
        } else {
            // Tắt màn hình nếu không có data.
            finish();
        }
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
     * Random list choices.
     *
     * @param choiceDaos List choices using random.
     * @return List choices after random.
     */
    private List<ChoiceDao> pickChoiceRandom(List<ChoiceDao> choiceDaos) {
        List<ChoiceDao> copy = new LinkedList<>(choiceDaos);
        Collections.shuffle(copy);

        return copy.subList(0, choiceDaos.size());
    }

    /**
     * Random list questions.
     *
     * @param questionDaos List questions using random.
     * @return List questions after random.
     */
    private List<QuestionDao> pickQuestionRandom(List<QuestionDao> questionDaos) {
        List<QuestionDao> copy = new LinkedList<>(questionDaos);
        Collections.shuffle(copy);

        return copy.subList(0, questionDaos.size());
    }

    /**
     * Hiện thị nội dung của từng câu hỏi.
     *
     * @param position Vị trí của từng câu hỏi.
     */
    private void showQuestionAtPosition(int position) {

        if (position < mQuestionDaos.size() && position < mChoiceDaos.size()) {
            mTvPosition.setText(String.valueOf(position + 1) + "/" + String.valueOf(mQuestionDaos.size()));
            // Set tag cho câu hỏi hiện tại vào textView question.
            mTvPosition.setTag(position);
            mImgStatus.setImageResource(0);

            QuestionDao questionDao = mQuestionDaos.get(position);
            List<ChoiceDao> choiceDaos = mChoiceDaos.get(position);

            switch (questionDao.type) {
                case QuestionType.TEXT_ROMAJI_KANA:
                    mTvQuestion.setText(questionDao.content_ja);

                    if (mTypeface != null) {
                        mTvQuestion.setTypeface(mTypeface);
                    }

                    // Todo hiện tại nội dung đều hiện thị tiếng việt.
                    // Lấy câu trả lời dựa theo ngôn ngữ.
//                    if (LocaleHelper.getLanguage(this).equals(Definition.LanguageCode.ENGLISH)) {
//                        mTvQuestion.setText(questionDao.content_en);
//                    } else {
//                        mTvQuestion.setText(questionDao.content_vi);
//                    }

                    mTvQuestion.setText(questionDao.content_vi);

                    mBtnAnswer1.setText(choiceDaos.get(0).content_ja);
                    mBtnAnswer2.setText(choiceDaos.get(1).content_ja);
                    mBtnAnswer3.setText(choiceDaos.get(2).content_ja);

                    // Set font tiếng Nhật for text view.
                    if (mTypeface != null) {
                        mBtnAnswer1.setTypeface(mTypeface);
                        mBtnAnswer2.setTypeface(mTypeface);
                        mBtnAnswer3.setTypeface(mTypeface);
                    }

                    mBtnAnswer1.setTag(choiceDaos.get(0));
                    mBtnAnswer2.setTag(choiceDaos.get(1));
                    mBtnAnswer3.setTag(choiceDaos.get(2));

                    // Enable 3 nút trả lời.
                    enableClickAnswers();

                    break;
                case QuestionType.TEXT_JA_NLANG:
                case QuestionType.TEXT_KANA_ROMAJI:

                    mTvQuestion.setText(questionDao.content_ja);

                    // Set font tiếng Nhật for text view.
                    if (mTypeface != null) {
                        mTvQuestion.setTypeface(mTypeface);
                    }

                    if (mTypeface != null) {
                        mTvQuestion.setTypeface(mTypeface);
                    }

                    // Todo hiện tại nội dung đều hiện thị tiếng việt.
                    // Lấy câu trả lời dựa theo ngôn ngữ.
//                    if (LocaleHelper.getLanguage(this).equals(Definition.LanguageCode.ENGLISH)) {
//                        mBtnAnswer1.setText(choiceDaos.get(0).content_en);
//                        mBtnAnswer2.setText(choiceDaos.get(1).content_en);
//                        mBtnAnswer3.setText(choiceDaos.get(2).content_en);
//
//                        mBtnAnswer1.setTag(choiceDaos.get(0));
//                        mBtnAnswer2.setTag(choiceDaos.get(1));
//                        mBtnAnswer3.setTag(choiceDaos.get(2));
//
//                    } else {
//                        mBtnAnswer1.setText(choiceDaos.get(0).content_vi);
//                        mBtnAnswer2.setText(choiceDaos.get(1).content_vi);
//                        mBtnAnswer3.setText(choiceDaos.get(2).content_vi);
//
//                        mBtnAnswer1.setTag(choiceDaos.get(0));
//                        mBtnAnswer2.setTag(choiceDaos.get(1));
//                        mBtnAnswer3.setTag(choiceDaos.get(2));
//                    }

                    mBtnAnswer1.setText(choiceDaos.get(0).content_vi);
                    mBtnAnswer2.setText(choiceDaos.get(1).content_vi);
                    mBtnAnswer3.setText(choiceDaos.get(2).content_vi);

                    mBtnAnswer1.setTag(choiceDaos.get(0));
                    mBtnAnswer2.setTag(choiceDaos.get(1));
                    mBtnAnswer3.setTag(choiceDaos.get(2));

                    // Enable 3 nút trả lời.
                    enableClickAnswers();

                    break;
                case QuestionType.TEXT_NLANG_JA:
                    // Todo version hiện tại tất cả dùng tiếng việt.
                    // Lấy content Question dựa theo ngôn ngữ.
//                    if (LocaleHelper.getLanguage(this).equals(Definition.LanguageCode.ENGLISH)) {
//                        mTvQuestion.setText(questionDao.content_en);
//                    } else {
//                        mTvQuestion.setText(questionDao.content_vi);
//                    }

                    mTvQuestion.setText(questionDao.content_vi);

                    // Lấy câu trả lời là tiếng Nhật.
                    mBtnAnswer1.setText(choiceDaos.get(0).content_ja);
                    mBtnAnswer2.setText(choiceDaos.get(1).content_ja);
                    mBtnAnswer3.setText(choiceDaos.get(2).content_ja);

                    // Set font tiếng Nhật for text view.
                    if (mTypeface != null) {
                        mBtnAnswer1.setTypeface(mTypeface);
                        mBtnAnswer2.setTypeface(mTypeface);
                        mBtnAnswer3.setTypeface(mTypeface);
                    }

                    mBtnAnswer1.setTag(choiceDaos.get(0));
                    mBtnAnswer2.setTag(choiceDaos.get(1));
                    mBtnAnswer3.setTag(choiceDaos.get(2));

                    // Enable 3 nút trả lời.
                    enableClickAnswers();

                    break;
                case QuestionType.VOICE_JA_JA:
                case QuestionType.VOICE_KANA_KANA:

                    // Disable click 3 button choose question.
                    disableClickAnswers();

                    // Ẩn text câu hỏi và show nút âm thanh.
                    mTvQuestion.setVisibility(View.GONE);
                    mImgSound.setVisibility(View.VISIBLE);

                    // Kiểm tra làm mờ image âm thanh.
                    File fileAudio = getFileVoice(mQuestionDaos.get(position));

                    // Kiểm tra file audio có tồn tại hay không if tồn tại thì show , không tồn tại thì làm mờ nó đi và không cho click.
                    if (fileAudio != null && fileAudio.exists()) {
                        mImgSound.setAlpha(1.0f);
                        mImgSound.setEnabled(true);
                    } else {
                        mImgSound.setAlpha(0.5f);
                        mImgSound.setEnabled(false);
                    }

                    mBtnAnswer1.setText(choiceDaos.get(0).content_ja);
                    mBtnAnswer2.setText(choiceDaos.get(1).content_ja);
                    mBtnAnswer3.setText(choiceDaos.get(2).content_ja);

                    // Set font tiếng Nhật for text view.
                    if (mTypeface != null) {
                        mBtnAnswer1.setTypeface(mTypeface);
                        mBtnAnswer2.setTypeface(mTypeface);
                        mBtnAnswer3.setTypeface(mTypeface);
                    }

                    mBtnAnswer1.setTag(choiceDaos.get(0));
                    mBtnAnswer2.setTag(choiceDaos.get(1));
                    mBtnAnswer3.setTag(choiceDaos.get(2));

                    mHandlerAutoPlayAudio.removeMessages(MSG);

                    // Pause audio nếu có file nào đang chạy.
                    pauseAudio();
                    Message msg = new Message();
                    msg.what = MSG;
                    msg.arg1 = position;

                    mHandlerAutoPlayAudio.sendMessageDelayed(msg, 300);

                    break;
                case QuestionType.VOICE_JA_NLANG:
                case QuestionType.VOICE_KANA_ROMAJI:

                    // Disable click 3 button choose question.
                    disableClickAnswers();

                    // Ẩn text câu hỏi và show nút âm thanh.
                    mTvQuestion.setVisibility(View.GONE);
                    mImgSound.setVisibility(View.VISIBLE);

                    // Kiểm tra làm mờ image âm thanh.
                    File fileVoice = getFileVoice(mQuestionDaos.get(position));

                    // Kiểm tra file audio có tồn tại hay không if tồn tại thì show , không tồn tại thì làm mờ nó đi và không cho click.
                    if (fileVoice != null && fileVoice.exists()) {
                        mImgSound.setAlpha(1.0f);
                        mImgSound.setEnabled(true);
                    } else {
                        mImgSound.setAlpha(0.5f);
                        mImgSound.setEnabled(false);
                    }

                    // Todo hiện tại nội dung đều hiện thị tiếng việt.
                    // Lấy câu trả lời dựa theo ngôn ngữ.
//                    if (LocaleHelper.getLanguage(this).equals(Definition.LanguageCode.ENGLISH)) {
//                        mBtnAnswer1.setText(choiceDaos.get(0).content_en);
//                        mBtnAnswer2.setText(choiceDaos.get(1).content_en);
//                        mBtnAnswer3.setText(choiceDaos.get(2).content_en);
//
//                        mBtnAnswer1.setTag(choiceDaos.get(0));
//                        mBtnAnswer2.setTag(choiceDaos.get(1));
//                        mBtnAnswer3.setTag(choiceDaos.get(2));
//
//                    } else {
//                        mBtnAnswer1.setText(choiceDaos.get(0).content_vi);
//                        mBtnAnswer2.setText(choiceDaos.get(1).content_vi);
//                        mBtnAnswer3.setText(choiceDaos.get(2).content_vi);
//
//                        mBtnAnswer1.setTag(choiceDaos.get(0));
//                        mBtnAnswer2.setTag(choiceDaos.get(1));
//                        mBtnAnswer3.setTag(choiceDaos.get(2));
//                    }

                    mBtnAnswer1.setText(choiceDaos.get(0).content_vi);
                    mBtnAnswer2.setText(choiceDaos.get(1).content_vi);
                    mBtnAnswer3.setText(choiceDaos.get(2).content_vi);

                    mBtnAnswer1.setTag(choiceDaos.get(0));
                    mBtnAnswer2.setTag(choiceDaos.get(1));
                    mBtnAnswer3.setTag(choiceDaos.get(2));

                    mHandlerAutoPlayAudio.removeMessages(MSG);
                    // Nếu có audio nào đang chạy theo dừng lại.
                    pauseAudio();

                    Message msgMessage = new Message();
                    msgMessage.what = MSG;
                    msgMessage.arg1 = position;
                    mHandlerAutoPlayAudio.sendMessageDelayed(msgMessage, 300);

                    break;
            }
        }

        // Set thời gian bắt đầu cho mỗi câu hỏi.
        long startedAt = SystemClock.elapsedRealtime();
        mImgStatus.setTag(startedAt);
    }

    /**
     * Check file is Exits.
     *
     * @return File.
     */
    public File getFileVoice(QuestionDao questionDao) {
        UserModel userModel = LocalAppUtil.getLastLoginUserInfo(this);

        if (questionDao == null || TextUtils.isEmpty(questionDao.voice_file) || getLesson2IntentData() == null || userModel == null) {
            return null;
        }

        return MediaUtil.audioIsPrepare(this, getLesson2IntentData(), userModel, questionDao.category, questionDao.voice_file);
    }

    /**
     * Method set sự kiện for View.
     */
    protected void setEvent() {
        mBtnAnswer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonAnswerChoice(mBtnAnswer1, 1);
            }
        });

        mBtnAnswer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonAnswerChoice(mBtnAnswer2, 2);
            }
        });

        mBtnAnswer3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonAnswerChoice(mBtnAnswer3, 3);
            }
        });

        mImgSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Vì người dùng nghe lại nên enable các nút trả lời lên.
                enableClickAnswers();

                if (mTvPosition.getTag() != null) {
                    int position = (int) mTvPosition.getTag();
                    // Chạy audio ở vị trí position.
                    playAudioWithPath(position);
                }
            }
        });
    }

    /**
     * Handle work when answer to be choose.
     *
     * @param button Button be pressed.
     */
    private void onButtonAnswerChoice(AutoResizeTextView button, int indexButton) {
        // Tránh trường hợp dữ liệu không có( có thể lúc import bị lỗi) or dữ liệu bị lỗi không có trong all các loại case question type nhằm tránh bị crash phía user.

        if (button.getTag() == null) {
            Toast.makeText(this, getString(R.string.common_msg__content_info__have_no_data), Toast.LENGTH_SHORT).show();

            // Tắt luôn màn hình này.
            finish();

            return;
        }

        long endedAt = SystemClock.elapsedRealtime();

        // Không cho người dung tương tác vào 3 nút.
        disableClickAnswers();

        // Nếu người dùng chọn câu trả lời thì pause audio lại để chạy audio đúng or sai.
        pauseAudio();

        Result result = new Result();
        result.setQuestion(mTvQuestion.getText().toString());
        result.setAnswer(getAnswerCorret());

        // Lâý tên file âm thanh nếu có.
        if (mTvPosition.getTag() != null) {
            int positionCurrent = (int) mTvPosition.getTag();

            if (!mQuestionDaos.isEmpty() && positionCurrent < mQuestionDaos.size()) {
                // 3 giá trị dùng để set cho kết quả tương ứng với câu hỏi, để khi đi đến màn hình kết quả chạy file âm thanh và đi đến knowledge detail dựa vào nó.
                result.setAudio_data(mQuestionDaos.get(positionCurrent).voice_file);
                result.knowledge_number = mQuestionDaos.get(positionCurrent).knowledge_number;
                result.setCategory(mQuestionDaos.get(positionCurrent).category);

                // Giá trị dùng để tìm những con question khi người dùng làm sai và thử làm lại những câu sai.
                result.setQuestion_id(mQuestionDaos.get(positionCurrent).id);
            }
        }

        // Tính thời gian kết thúc khi người dùng trả lời 1 câu hỏi.
        if (mImgStatus.getTag() != null) {
            long startedAt = (long) mImgStatus.getTag();
            // Dùng class support MathUtil.
            double timeCompleted = MathUtil.round((((double) (endedAt - startedAt)) / 1000), 2);

            // Tạo ra đối tượng kết quả cho một câu trả lời.
            result.setTime_complete(timeCompleted);
        }

        ChoiceDao choiceDao = (ChoiceDao) button.getTag();

        mImgStatus.setImageResource(choiceDao.is_correct ? R.drawable.s21_practice_detail_ic_true : R.drawable.s21_practice_detail_ic_fail);

        switch (indexButton) {
            case 1:
                if (choiceDao.is_correct) {
                    playEffectAudio(true);
                    result.setIs_correct(true);
                    mBtnAnswer1.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.s21_practice_detail_btn_true));
                } else {
                    playEffectAudio(false);
                    result.setIs_correct(false);
                    mBtnAnswer1.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.s21_practice_detail_btn_fail));

                    mBtnAnswer2.setBackgroundDrawable(ContextCompat.getDrawable(this, ((ChoiceDao) mBtnAnswer2.getTag()).is_correct ? R.drawable.s21_practice_detail_btn_true : R.drawable.s21_practice_detail_btn_normal));
                    mBtnAnswer3.setBackgroundDrawable(ContextCompat.getDrawable(this, ((ChoiceDao) mBtnAnswer3.getTag()).is_correct ? R.drawable.s21_practice_detail_btn_true : R.drawable.s21_practice_detail_btn_normal));
                }

                break;
            case 2:
                if (choiceDao.is_correct) {
                    playEffectAudio(true);
                    result.setIs_correct(true);
                    mBtnAnswer2.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.s21_practice_detail_btn_true));
                } else {
                    playEffectAudio(false);
                    result.setIs_correct(false);
                    mBtnAnswer2.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.s21_practice_detail_btn_fail));

                    mBtnAnswer1.setBackgroundDrawable(ContextCompat.getDrawable(this, ((ChoiceDao) mBtnAnswer1.getTag()).is_correct ? R.drawable.s21_practice_detail_btn_true : R.drawable.s21_practice_detail_btn_normal));
                    mBtnAnswer3.setBackgroundDrawable(ContextCompat.getDrawable(this, ((ChoiceDao) mBtnAnswer3.getTag()).is_correct ? R.drawable.s21_practice_detail_btn_true : R.drawable.s21_practice_detail_btn_normal));
                }

                break;

            case 3:
                if (choiceDao.is_correct) {
                    playEffectAudio(true);
                    result.setIs_correct(true);
                    mBtnAnswer3.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.s21_practice_detail_btn_true));
                } else {
                    playEffectAudio(false);
                    result.setIs_correct(false);
                    mBtnAnswer3.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.s21_practice_detail_btn_fail));

                    mBtnAnswer1.setBackgroundDrawable(ContextCompat.getDrawable(this, ((ChoiceDao) mBtnAnswer1.getTag()).is_correct ? R.drawable.s21_practice_detail_btn_true : R.drawable.s21_practice_detail_btn_normal));
                    mBtnAnswer2.setBackgroundDrawable(ContextCompat.getDrawable(this, ((ChoiceDao) mBtnAnswer2.getTag()).is_correct ? R.drawable.s21_practice_detail_btn_true : R.drawable.s21_practice_detail_btn_normal));
                }

                break;
        }

        mListResults.add(result);
    }

    /**
     * Lấy câu trả lời đúng cho mỗi câu hỏi.
     *
     * @return Value đúng.
     */
    private String getAnswerCorret() {
        ChoiceDao choiceDao1 = (ChoiceDao) mBtnAnswer1.getTag();

        if (choiceDao1 != null) {
            if (choiceDao1.is_correct) {
                return mBtnAnswer1.getText().toString();
            }
        }

        ChoiceDao choiceDao2 = (ChoiceDao) mBtnAnswer2.getTag();

        if (choiceDao2 != null) {
            if (choiceDao2.is_correct) {
                return mBtnAnswer2.getText().toString();
            }
        }

        ChoiceDao choiceDao3 = (ChoiceDao) mBtnAnswer3.getTag();

        if (choiceDao3 != null) {
            if (choiceDao3.is_correct) {
                return mBtnAnswer3.getText().toString();
            }
        }

        return "";
    }

    /**
     * Go to screen ScreenS22PracticeResult.
     */
    private void goToScreenS22PracticeResult(Lesson lesson) {
        if (lesson != null) {
            Intent intent = new Intent(this, S22PracticeResult_Activity.class);
            intent.putExtra(S22PracticeResult_Activity.S22_LESSON_PARCELABLE_OBJECT, lesson);
            intent.putParcelableArrayListExtra(S22PracticeResult_Activity.S22_RESULT_LIST_PARCELABLE, mListResults);
            intent.putExtra(S22PracticeResult_Activity.S22_TYPE_QUESTION, mQuestionDaos.get(0).type);

            // Chỉ start 1 activity when user click multi.
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            // Start activity và lắng nghe sự kiện người dùng có thử lại hay ko.
            startActivityForResult(intent, REQUEST_S22_PRACTICE_RESULT);
        }
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
        File fileAudio = getFileVoice(mQuestionDaos.get(position));

        if (fileAudio != null && fileAudio.exists()) {
            if (mAudioService != null && isBound) {
                // Nếu file audio device đang chạy thì disable click image sound.
                mImgSound.setEnabled(false);
                mAudioService.playAudioWithPath(fileAudio.getPath());
            }
        } else {
            // Phải bật các nút choose lên cho người dùng tương tác để chạy các file tiếp theo.
            enableClickAnswers();
        }
    }

    /**
     * Handle play effect audio from service after choose answer
     *
     * @param isCorrect Result of answer is chosen (correct or not correct)
     */
    public void playEffectAudio(boolean isCorrect) {
        if (mAudioService != null && isBound) {
            mAudioService.playEffectAudio(isCorrect);
        }
    }

    // Disable all answer to user cannot choose its.
    protected void disableClickAnswers() {
        mBtnAnswer1.setBackgroundResource(R.drawable.s21_practice_detail_btn_disable);
        mBtnAnswer2.setBackgroundResource(R.drawable.s21_practice_detail_btn_disable);
        mBtnAnswer3.setBackgroundResource(R.drawable.s21_practice_detail_btn_disable);

        mBtnAnswer1.setEnabled(false);
        mBtnAnswer2.setEnabled(false);
        mBtnAnswer3.setEnabled(false);
    }

    // Enable all answer, access user choose its.
    private void enableClickAnswers() {
        mBtnAnswer1.setBackgroundResource(R.drawable.s21_practice_detail_btn_normal);
        mBtnAnswer2.setBackgroundResource(R.drawable.s21_practice_detail_btn_normal);
        mBtnAnswer3.setBackgroundResource(R.drawable.s21_practice_detail_btn_normal);

        mBtnAnswer1.setEnabled(true);
        mBtnAnswer2.setEnabled(true);
        mBtnAnswer3.setEnabled(true);
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
            return getIntent().getExtras().getParcelable(S21_LESSON_PARCELABLE_OBJECT);
        }

        return null;
    }

    // Get data be send through intent.
    private int getTypeQuestion() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getInt(S21_TYPE_QUESTION);
        }

        return 0;
    }

    /**
     * Get total number question mistake of user from S22Practice_Result.
     */
    private List<Result> getQuestionUserMistake() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getParcelableArrayList(S21PracticeDetail_Activity.S21_LIST_QUESTION_USER_MISTAKE);
        }

        return null;
    }

    @Override
    public void onPlayAudioFinish() {
        Log.d(LOG_TAG, "onPlayAudioFinish");

        // Set thời gian bắt đầu cho câu hỏi loại âm thanh khi file âm thanh chạy xong.
        long startedAt = SystemClock.elapsedRealtime();
        mImgStatus.setTag(startedAt);

        // Khi file âm thanh lấy từ device chạy xong thì enable các button cho người dùng chọn trả lời.
        enableClickAnswers();

        // Mở lại image sound cho người dùng bấm nghe lại.
        mImgSound.setEnabled(true);
    }

    @Override
    public void onPlayAudioCompletion() {
        Log.d(LOG_TAG, "onPlayAudioCompletion");

        // Khi file âm thanh đúng or sai chạy xong thì load câu hỏi tiếp theo nếu còn, ko thì đi đến màn hình kết quả.
        if (mTvPosition.getTag() != null) {
            int positionCurrent = (int) mTvPosition.getTag();

            if (positionCurrent < mQuestionDaos.size() - 1) {
                showQuestionAtPosition(positionCurrent + 1);
            } else {
                // Disable all button.
                goToScreenS22PracticeResult(getLesson2IntentData());
            }
        }
    }
}
