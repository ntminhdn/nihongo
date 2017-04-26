package com.honkidenihongo.pre.gui.trialtest;

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
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.config.FontsConfig;
import com.honkidenihongo.pre.common.util.CountDownTimer;
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
import com.honkidenihongo.pre.model.constant.Category;
import com.honkidenihongo.pre.model.constant.LanguageNumberCode;
import com.honkidenihongo.pre.model.constant.QuestionType;
import com.honkidenihongo.pre.service.AudioService;
import com.honkidenihongo.pre.service.clickhomedevice.HomeWatcher;
import com.honkidenihongo.pre.service.clickhomedevice.OnHomePressedListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Màn hình thực hiện chức năng trialTest cho mỗi bài học.
 *
 * @author binh.dt modify.
 * @since 30-Nov-2016.
 */
public class S12TrialTestDetail_Activity extends AppCompatActivity implements AudioService.PlayAudioCallback {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S12TrialTestDetail_Activity.class.getName();
    private static final int REQUEST_S17_TRIAL_TEST_RESULT = 101;

    // Thời gian đếm ngược từ giây thứ 4 vể giây thứ 1.
    private static final long MILLI_S_IN_FUTURE = 4000;
    private static final long COUNT_DOWN_INTERVAL = 1000;
    public static final String S12_LESSON_OBJECT = "S12_LESSON_OBJECT";
    private static final int MSG = 1;
    private Typeface mTypeface;
    private Realm mRealm;

    private final List<QuestionDao> mQuestionDaos = new ArrayList<>();
    private final List<List<ChoiceDao>> mChoiceDaos = new ArrayList<>();
    private final ArrayList<Result> mListResults = new ArrayList<>();

    // Tạo luồng đến ngược cho mỗi câu hỏi.
    private CountDownTimer mCountDownTimer;

    // For View.
    private Toolbar mToolbar;
    private AppCompatTextView mTvCountDown;
    private AppCompatTextView mTvTitle;
    private AppCompatTextView mTvPosition;
    private AppCompatTextView mTvQuestion;
    private AutoResizeTextView mBtnAnswer1;
    private AutoResizeTextView mBtnAnswer2;
    private AutoResizeTextView mBtnAnswer3;
    private AppCompatImageButton mImgSound;
    private AppCompatTextView mTvTitleToolbar;

    // Define variable about play audio service.
    private AudioService mAudioService;
    private boolean isBound = false;
    private HomeWatcher mHomeWatcher;

    // Connection to bind a service that play special audio.
    private ServiceConnection mAudioServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AudioService.AudioBinder binder = (AudioService.AudioBinder) iBinder;
            mAudioService = binder.getService();
            isBound = true;

            mAudioService.setPlayAudioCallback(S12TrialTestDetail_Activity.this);
            Log.d(LOG_TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;

            Log.d(LOG_TAG, "Connect Service error!");
        }
    };

    // Thread play audio at position.
    private Handler mHandlerAutoPlayAudio = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            synchronized (S12TrialTestDetail_Activity.this) {
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
        setContentView(R.layout.s12_trial_test_detail_activity);

        // Init View.
        initView();

        // Init toolbar.
        ToolbarUtil.initToolbar(mToolbar, this);

        // Tạo luống đếm ngược cho mỗi câu hỏi.
        createCountDown();

        // Set data.
        setData(getLessonIntentData());

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
    public void onPause() {
        // Pause audio when pause activity.
        pauseAudio();

        // Hủy luồng đếm.
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }

        if (mHandlerAutoPlayAudio != null) {
            // Hủy đối tượng và hủy đăng ký service.
            mHandlerAutoPlayAudio.removeMessages(MSG);
        }

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
        // Hủy luồng đếm.
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }

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

        // Dừng lắng nghe sự kiện click nút home và percent của device.
        if (mHomeWatcher != null) {
            mHomeWatcher.stopWatch();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_S17_TRIAL_TEST_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                // If người dùng chọn Try again.
                Intent intent = getIntent();
                setResult(RESULT_OK, intent);
                finish();
            } else {
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

    @Override
    public void onBackPressed() {
        // Back trở về màn hình Starting.
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);

        super.onBackPressed();
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
     * Play audio with file path.
     *
     * @param position Value.
     */
    public void playAudioWithPath(int position) {
        File fileAudio = getFileVoice(mQuestionDaos.get(position));

        if (fileAudio != null && fileAudio.exists()) {
            if (mAudioService != null && isBound) {
                mAudioService.playAudioWithPath(fileAudio.getPath());
            }
        } else {
            // Phải bật các nút choose lên cho người dùng tương tác để chạy các file tiếp theo.
            enableClickAnswers();
        }
    }

    /**
     * Check file is Exits.
     *
     * @return File.
     */
    public File getFileVoice(QuestionDao questionDao) {
        UserModel userModel = LocalAppUtil.getLastLoginUserInfo(this);

        if (questionDao == null || TextUtils.isEmpty(questionDao.voice_file) || getLessonIntentData() == null || userModel == null) {
            return null;
        }

        return MediaUtil.audioIsPrepare(this, getLessonIntentData(), userModel, questionDao.category, questionDao.voice_file);
    }

    /**
     * Init View.
     */
    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mTvCountDown = (AppCompatTextView) findViewById(R.id.mTvCountDown);
        mTvTitle = (AppCompatTextView) findViewById(R.id.mTvTitle);
        mTvPosition = (AppCompatTextView) findViewById(R.id.mTvPosition);
        mTvQuestion = (AppCompatTextView) findViewById(R.id.mTvQuestion);
        mBtnAnswer1 = (AutoResizeTextView) findViewById(R.id.mBtnAnswer1);
        mBtnAnswer2 = (AutoResizeTextView) findViewById(R.id.mBtnAnswer2);
        mBtnAnswer3 = (AutoResizeTextView) findViewById(R.id.mBtnAnswer3);
        mImgSound = (AppCompatImageButton) findViewById(R.id.mImgSound);
        mTvTitleToolbar = (AppCompatTextView) mToolbar.findViewById(R.id.mTvTitleToolbar);
        mTypeface = FontsConfig.getInstance(this).getFont(FontsConfig.AppFont.KLEE);
    }

    /**
     * Lấy về mảng câu hỏi ứng với lesson id, category và questionType truyền vào.
     **/
    private void get15ElementTypeQuestion(Lesson lesson, int category, int questionType) {
        List<QuestionDao> questionDaos = new ArrayList<>();

        questionDaos.addAll(mRealm.where(QuestionDao.class)
                .equalTo(Definition.Database.Question.QUESTION_FIELD_LESSON_NUMBER, lesson.getNumber())
                .equalTo(Definition.Database.Question.QUESTION_FIELD_CATEGORY, category)
                .equalTo(Definition.Database.Question.QUESTION_FIELD_TYPE, questionType)
                .findAll());

        if (!questionDaos.isEmpty()) {
            List<QuestionDao> questionDaoList15Element = new ArrayList<>();

            if (questionDaos.size() > 15) {
                // Nếu danh sách lớn hơn 15 element thì chỉ random lấy 15 phần tử, ngược lại lấy all.
                questionDaoList15Element.addAll(pickNRandomQuestion(questionDaos, 15));
            } else {
                questionDaoList15Element.addAll(pickNRandomQuestion(questionDaos, questionDaos.size()));
            }

            if (!questionDaoList15Element.isEmpty()) {
                for (QuestionDao questionDao : questionDaoList15Element) {
                    mQuestionDaos.add(questionDao);

                    // Ứng với một câu hỏi sẽ có 3 câu trả lời.
                    RealmResults<ChoiceDao> choiceDaos = mRealm.where(ChoiceDao.class)
                            .equalTo(Definition.Database.Choice.CHOICE_FIELD_QUESTION_ID, questionDao.id)
                            .findAll();

                    if (!choiceDaos.isEmpty()) {
                        // Random mảng trả lời trước khi đưa nó vào list câu trả lời.
                        mChoiceDaos.add(pickNRandomChoice(choiceDaos));
                    }
                }
            }
        }
    }

    /**
     * Set Data.
     *
     * @param lesson Value receive.
     */
    private void setData(Lesson lesson) {
        setTitleScreen(lesson);

        // Lấy 15 câu hỏi cho mỗi loại.
        get15ElementTypeQuestion(lesson, Category.UNIT_WORD, QuestionType.TEXT_JA_NLANG);

        get15ElementTypeQuestion(lesson, Category.UNIT_WORD, QuestionType.VOICE_JA_NLANG);

        get15ElementTypeQuestion(lesson, Category.UNIT_SENTENCE, QuestionType.TEXT_JA_NLANG);

        get15ElementTypeQuestion(lesson, Category.UNIT_SENTENCE, QuestionType.VOICE_JA_NLANG);

        // If mảng câu hỏi khác rỗng và size của nó bằng 60 câu thì gọi bắt đầu đếm ngược bắt đầu từ câu hỏi đầu tiên của danh sách câu hỏi.
        if (!mQuestionDaos.isEmpty() && mQuestionDaos.size() == 60) {
            showQuestionAtPosition(0);
        } else {
            // Ẩn các view thành phần đi khi không đủ dữ kiện.
            mTvCountDown.setVisibility(View.GONE);
            mBtnAnswer1.setVisibility(View.GONE);
            mBtnAnswer2.setVisibility(View.GONE);
            mBtnAnswer3.setVisibility(View.GONE);
        }
    }

    /**
     * Random list câu hỏi.
     *
     * @param questionDaos List using random.
     * @return List after random.
     */
    private List<QuestionDao> pickNRandomQuestion(List<QuestionDao> questionDaos, int n) {
        List<QuestionDao> copy = new LinkedList<>(questionDaos);
        Collections.shuffle(copy);

        return copy.subList(0, n);
    }

    /**
     * Bắt đầu tính thời gian test.
     * <p>
     * MILLI_S_IN_FUTURE    The number of millis in the future from the call
     * to start() until the countdown is done and onFinish().
     * is called.
     * COUNT_DOWN_INTERVAL The interval along the way to receive
     * onTick(long) callbacks.
     */
    private void createCountDown() {
        if (mCountDownTimer == null) {
            mCountDownTimer = new CountDownTimer(MILLI_S_IN_FUTURE, COUNT_DOWN_INTERVAL) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // Tính ra đơn vị s.
                    int downTimer = (int) (millisUntilFinished / 1000);

                    // Hiện tại trong màn hình kết quả ta đã kiểm tra nếu thời gian vượt quá 3s hoàn thành thì set là 3.00s nên không cần đến lệnh này
//                    // Nếu thời gian đếm ngược bằng 0 thì không cho người dùng tương tác nữa để ngăn việc thời gian xảy ra độ trễ vượt quá 3s và để load câu hỏi mới.
//                    if (downTimer == 0) {
//                        disableClickAnswers();
//                    }

                    if (downTimer != 4) {
                        mTvCountDown.setText(String.valueOf(downTimer));
                    }
                }

                @Override
                public void onFinish() {
                    // Cập nhật giao diện UI.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (mTvPosition.getTag() != null) {
                                // Lấy câu hỏi hiện tại để check.
                                int positionCurrent = (int) mTvPosition.getTag();

                                if (positionCurrent < mQuestionDaos.size() - 1) {
                                    showQuestionAtPosition(positionCurrent + 1);

                                    // Todo remove later.
                                    // Khởi động looping lại count down.
                                    // start();

                                } else {
                                    // Todo remove later.
                                    // Dừng count down và đi đến màn hình kết quả.
                                    //  cancel();

                                    goToScreenS17TrialTestResult(getLessonIntentData());
                                }
                            }
                        }
                    });
                }
            };
        }
    }

    /**
     * Hiện thị nội dung của từng câu hỏi.
     *
     * @param position Vị trí của từng câu hỏi.
     */
    private void showQuestionAtPosition(int position) {
        // Set lại thời gian đếm khi load mỗi câu hỏi là 3s.
        mTvCountDown.setText(String.valueOf(3));

        // Khi bắt đầu load câu hỏi thì disable các button choose kết quả.
        // Disable click 3 button choose question.
        disableClickAnswers();

        if (position < mQuestionDaos.size() && position < mChoiceDaos.size()) {
            mTvPosition.setText(String.valueOf(position + 1) + "/" + String.valueOf(mQuestionDaos.size()));

            // Lấy câu hỏi hiện tại đang hiển thị, để sau khi người dùng trả lời xong thì gọi câu hỏi tiếp theo.
            mTvPosition.setTag(position);

            QuestionDao questionDao = mQuestionDaos.get(position);
            List<ChoiceDao> choiceDaos = mChoiceDaos.get(position);

            switch (questionDao.type) {
                case QuestionType.TEXT_JA_NLANG:
                case QuestionType.TEXT_JA_JA:

                    // Ẩn nút âm thanh và show câu hỏi.
                    mTvQuestion.setVisibility(View.VISIBLE);
                    mImgSound.setVisibility(View.GONE);

                    mTvQuestion.setText(questionDao.content_ja);

                    if (mTypeface != null) {
                        mTvQuestion.setTypeface(mTypeface);
                    }

                    // Todo hiện tại version này all nội dung là tiếng việt.
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

                    // Đối với trường hợp câu hỏi là loại text thì sau khi load nội dung câu hỏi xong thực hiện đếm luồng.
                    if (mCountDownTimer != null) {
                        mCountDownTimer.start();

                        // Set thời gian bắt đầu cho mỗi câu hỏi khi luồng đếm start.
                        long startedAt = SystemClock.elapsedRealtime();
                        mTvQuestion.setTag(startedAt);
                    }

                    // Đối với trường hợp câu hỏi là text thì ngay sau khi load xong câu hỏi thì enable nút choose lên.
                    enableClickAnswers();

                    break;
                case QuestionType.TEXT_NLANG_JA:
                    // Ẩn nút âm thanh và show câu hỏi.
                    mTvQuestion.setVisibility(View.VISIBLE);
                    mImgSound.setVisibility(View.GONE);

                    // Todo version hiện tại tất cả là tiếng viêt.
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

                    break;
                case QuestionType.VOICE_JA_JA:
                    // Dừng luồng đếm ngược 3 giây để sau khi file âm thanh đối với loại câu hỏi là voice
                    // đọc xong thì mới chạy luồng đếm ngược này start nó ở call back gọi về onPlayAudioFinish.
                    mCountDownTimer.cancel();

                    // Ẩn text câu hỏi và show nút âm thanh.
                    mTvQuestion.setText("");
                    mTvQuestion.setVisibility(View.GONE);
                    mImgSound.setVisibility(View.VISIBLE);

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

                    // Kiểm tra làm mờ image âm thanh.
                    File fileAudio = getFileVoice(mQuestionDaos.get(position));

                    // Kiểm tra file audio có tồn tại hay không if tồn tại thì show , không tồn tại thì làm mờ nó đi và không cho click.
                    if (fileAudio != null && fileAudio.exists()) {
                        mImgSound.setAlpha(1.0f);
                        mImgSound.setEnabled(true);

                        // Chạy file âm thanh.
                        Message msg = new Message();
                        msg.what = MSG;
                        msg.arg1 = position;

                        mHandlerAutoPlayAudio.sendMessageDelayed(msg, 50);
                    } else {
                        // Nếu không thể chơi file âm thanh trên device thì khởi động lại luồng đếm để cho next câu hỏi tiếp theo.
                        mImgSound.setAlpha(0.5f);
                        mImgSound.setEnabled(false);
                        mCountDownTimer.start();

                        // Set thời gian bắt đầu cho mỗi câu hỏi khi luồng đếm start.
                        long startedAt = SystemClock.elapsedRealtime();
                        mTvQuestion.setTag(startedAt);

                        // Enable các button cho người dùng chọn trả lời.
                        enableClickAnswers();
                    }

                    break;
                case QuestionType.VOICE_JA_NLANG:
                    // Dừng luồng đếm ngược 3 giây để sau khi file âm thanh đối với loại câu hỏi là voice
                    // đọc xong thì mới chạy luồng đếm ngược này start nó ở call back gọi về onPlayAudioFinish.
                    mCountDownTimer.cancel();

                    // Ẩn text câu hỏi và show nút âm thanh.
                    mTvQuestion.setText("");
                    mTvQuestion.setVisibility(View.GONE);
                    mImgSound.setVisibility(View.VISIBLE);

                    // Todo hiện tại version này all dùng tiếng việt.
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

                    // Kiểm tra làm mờ image âm thanh.
                    File fileVoice = getFileVoice(mQuestionDaos.get(position));

                    // Kiểm tra file audio có tồn tại hay không if tồn tại thì show , không tồn tại thì làm mờ nó đi và không cho click.
                    if (fileVoice != null && fileVoice.exists()) {
                        mImgSound.setAlpha(1.0f);
                        mImgSound.setEnabled(true);

                        // Chạy file âm thanh.
                        Message msgMessage = new Message();
                        msgMessage.what = MSG;
                        msgMessage.arg1 = position;

                        mHandlerAutoPlayAudio.sendMessageDelayed(msgMessage, 50);
                    } else {
                        // Nếu không thể chơi file âm thanh trên device thì khởi động lại luồng đếm để cho next câu hỏi tiếp theo.
                        mImgSound.setAlpha(0.5f);
                        mImgSound.setEnabled(false);
                        mCountDownTimer.start();

                        // Set thời gian bắt đầu cho mỗi câu hỏi khi luồng đếm start.
                        long startedAt = SystemClock.elapsedRealtime();
                        mTvQuestion.setTag(startedAt);
                        // Enable các button cho người dùng chọn trả lời.
                        enableClickAnswers();
                    }

                    break;
            }
        }

        // Tạo ra đối tượng kết quả ứng với mỗi câu hỏi if người dùng bỏ qua không chọn thì cho nó vào list Default thời gian trả lời theo bộ đếm là 3s
        // và người dùng không chọn là false, kiểm tra và remove khỏi list khi người dùng chọn câu trả lời trong method onButtonAnswerChoice.
        Result resultDefault = new Result();
        resultDefault.setId(mQuestionDaos.get(position).id);
        resultDefault.setIs_correct(false);
        resultDefault.setTime_complete(Definition.Result.TIME_COMPLETED_ONE_QUESTION_MAX);
        resultDefault.setQuestion(mTvQuestion.getText().toString());
        resultDefault.setAnswer(getAnswerCorrect());
        resultDefault.setAudio_data(mQuestionDaos.get(position).voice_file);

        // 4 thuộc tính dùng để biết câu hỏi thuộc loại gì và có liên hệ với đối tượng knowledge để đi đến màn hình Detail khi click vào item result.
        resultDefault.level = mQuestionDaos.get(position).level;
        resultDefault.category = mQuestionDaos.get(position).category;
        resultDefault.typeQuestion = mQuestionDaos.get(position).type;
        resultDefault.knowledge_number = mQuestionDaos.get(position).knowledge_number;

        // Thêm câu trả lời mặc định vào list trả lời.
        mListResults.add(resultDefault);
    }

    /**
     * Random list câu trả lời.
     *
     * @param choiceDaos List using random.
     * @return List after random.
     */
    private List<ChoiceDao> pickNRandomChoice(List<ChoiceDao> choiceDaos) {
        List<ChoiceDao> copy = new LinkedList<>(choiceDaos);
        Collections.shuffle(copy);

        return copy.subList(0, choiceDaos.size());
    }

    /**
     * Set title for screen.
     * * @param lesson Value lesson receive.
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

        String title = getString(R.string.common_module__testing__trial_title) + Definition.General.BREADCRUMB_SEPARATOR + getString(R.string.common_module__take_a_test);
        mTvTitle.setText(title);
    }

    /**
     * Method set sự kiện for View.
     */
    protected void setEvent() {
        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new OnHomePressedListener() {
            @Override
            public void onSystemMenuPressed() {
                // Khi đang test người dùng bấm nút home thì finish trở về màn hình S24Starting.
                Intent intent = getIntent();
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onHomePressed() {
                // Khi đang test người dùng bấm nút percent thì finish trở về màn hình S24Starting.
                Intent intent = getIntent();
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void offScreenPressed() {
                // Khi đang test người dùng bấm tắt màn hình và mở lại thì finish trở về màn hình S24Starting.
                Intent intent = getIntent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        mHomeWatcher.startWatch();

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
    }

    /**
     * Handle work when answer to be choose.
     *
     * @param button      Button be pressed.
     * @param indexButton Vị trí button xảy ra sự kiện.
     */
    protected void onButtonAnswerChoice(AutoResizeTextView button, int indexButton) {
        long endedAt = SystemClock.elapsedRealtime();

        // Dừng đến ngược và load câu hỏi tiếp theo if còn, nếu người dùng trả lời hết thì đi đến màn hình kết quả.Thời gian delay cho load câu hỏi mới là 1s.
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }

        // Tạo ra đối tượng kết quả cho một câu trả lời.
        Result resultChoose = new Result();

        // Tính thời gian kết thúc khi người dùng trả lời 1 câu hỏi.
        if (mTvQuestion.getTag() != null) {
            long startedAt = (long) mTvQuestion.getTag();

            // Dùng class support MathUtil.
            double timeCompleted = MathUtil.round((((double) (endedAt - startedAt)) / 1000), 2);
            resultChoose.setTime_complete(timeCompleted);
        }

        // Không cho người dung tương tác vào 3 nút.
        disableClickAnswers();

        // Lấy id và file âm thanh nếu có của câu hỏi hiện tại set id cho câu trả lời tương ứng.
        if (mTvPosition.getTag() != null) {
            int positionCurrent = (int) mTvPosition.getTag();

            if (positionCurrent < mQuestionDaos.size()) {
                resultChoose.setId(mQuestionDaos.get(positionCurrent).id);
                resultChoose.setAudio_data(mQuestionDaos.get(positionCurrent).voice_file);
                // 4 giá trị dùng để set cho kết quả tương ứng với câu hỏi, để khi đi đến màn hình kết quả chạy file âm thanh và đi đến knowledge detail dựa vào nó.
                resultChoose.typeQuestion = mQuestionDaos.get(positionCurrent).type;
                resultChoose.knowledge_number = mQuestionDaos.get(positionCurrent).knowledge_number;
                resultChoose.level = mQuestionDaos.get(positionCurrent).level;
                resultChoose.category = mQuestionDaos.get(positionCurrent).category;
            }
        }

        resultChoose.setQuestion(mTvQuestion.getText().toString());
        resultChoose.setAnswer(getAnswerCorrect());

        ChoiceDao choiceDao = (ChoiceDao) button.getTag();
        resultChoose.setIs_correct(choiceDao.is_correct);

        switch (indexButton) {
            case 1:
                mBtnAnswer1.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.s12_trial_test_detail_btn_true));

                break;
            case 2:
                mBtnAnswer2.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.s12_trial_test_detail_btn_true));

                break;

            case 3:
                mBtnAnswer3.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.s12_trial_test_detail_btn_true));

                break;
        }

        // Kiểm tra xem câu hỏi được người dùng chọn thì remove giá trị mặc định của câu trả lời của nó đi để lấy giá trị trả lời mới.
        if (!mListResults.isEmpty()) {
            for (Result resultDefault : mListResults) {
                if (resultDefault.getId() == resultChoose.getId()) {
                    // Remove giá trị cũ mặc định 3s, trả lời sai đi.
                    mListResults.remove(resultDefault);

                    // Lấy giá trị mới.
                    mListResults.add(resultChoose);

                    // Dừng kiểm tra.
                    break;
                }
            }
        }

        // Delay cho show câu hỏi tiếp theo là 0.5s.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mTvPosition.getTag() != null) {
                    int positionCurrent = (int) mTvPosition.getTag();

                    if (positionCurrent < mQuestionDaos.size() - 1) {
                        showQuestionAtPosition(positionCurrent + 1);
                    } else {
                        goToScreenS17TrialTestResult(getLessonIntentData());
                    }
                }
            }
        }, 100); // 0.1s.
    }

    /**
     * Không cho người dùng click vào button trả lời.
     */
    private void disableClickAnswers() {
        mBtnAnswer1.setBackgroundResource(R.drawable.s12_trial_test_detail_btn_disable);
        mBtnAnswer2.setBackgroundResource(R.drawable.s12_trial_test_detail_btn_disable);
        mBtnAnswer3.setBackgroundResource(R.drawable.s12_trial_test_detail_btn_disable);

        mBtnAnswer1.setEnabled(false);
        mBtnAnswer2.setEnabled(false);
        mBtnAnswer3.setEnabled(false);
    }

    /**
     * Cho phép người dùng tương tác vào các button trả lời.
     */
    private void enableClickAnswers() {
        mBtnAnswer1.setBackgroundResource(R.drawable.s12_trial_test_detail_btn_normal);
        mBtnAnswer2.setBackgroundResource(R.drawable.s12_trial_test_detail_btn_normal);
        mBtnAnswer3.setBackgroundResource(R.drawable.s12_trial_test_detail_btn_normal);

        mBtnAnswer1.setEnabled(true);
        mBtnAnswer2.setEnabled(true);
        mBtnAnswer3.setEnabled(true);
    }

    /**
     * Go to screen ScreenS17TrialTestResult.
     *
     * @param lesson Value lesson receive.
     */
    private void goToScreenS17TrialTestResult(Lesson lesson) {
        if (lesson != null) {
            Intent intent = new Intent(this, S17TrialTestResult_Activity.class);
            intent.putExtra(S17TrialTestResult_Activity.S17_LESSON_PARCELABLE_OBJECT, lesson);
            intent.putParcelableArrayListExtra(S17TrialTestResult_Activity.S17_RESULT_LIST_PARCELABLE, mListResults);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            // Chỉ start 1 activity when user click multi.
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            // Start activity và lắng nghe sự kiện trả về.
            startActivityForResult(intent, REQUEST_S17_TRIAL_TEST_RESULT);
        }
    }

    /**
     * Lấy câu trả lời đúng cho mỗi câu hỏi.
     *
     * @return Value đúng.
     */
    private String getAnswerCorrect() {
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
     * Nhận data từ intent.
     */
    private Lesson getLessonIntentData() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getParcelable(S12_LESSON_OBJECT);
        }

        return null;
    }

    @Override
    public void onPlayAudioFinish() {
        Log.d(LOG_TAG, "onPlayAudioFinish");

        // Khi file âm thanh chạy xong, bắt đầu đếm ngược trước khi chọn câu trả lời vì thời gian làm bài được tính ở luồng đếm.
        mCountDownTimer.start();

        // Set thời gian bắt đầu cho mỗi câu hỏi khi luồng đếm start.
        long startedAt = SystemClock.elapsedRealtime();
        mTvQuestion.setTag(startedAt);

        // Khi file âm thanh lấy từ device chạy xong thì enable các button cho người dùng chọn trả lời.
        enableClickAnswers();
    }

    @Override
    public void onPlayAudioCompletion() {
    }
}
