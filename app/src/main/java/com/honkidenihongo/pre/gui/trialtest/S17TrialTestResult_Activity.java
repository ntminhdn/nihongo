package com.honkidenihongo.pre.gui.trialtest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
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
import android.view.WindowManager;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.adapter.S17TrialTestResult_Adapter;
import com.honkidenihongo.pre.adapter.S22PracticeResult_Adapter;
import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.LessonNameUtil;
import com.honkidenihongo.pre.common.util.LocalAppUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.common.util.MediaUtil;
import com.honkidenihongo.pre.common.util.NetworkUtil;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Screen hiển thị list kết quả bài test trial của user.
 *
 * @author binh.dt modify.
 * @since 01-Dec-2016.
 */
public class S17TrialTestResult_Activity extends AppCompatActivity {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S17TrialTestResult_Activity.class.getName();
    public static final String S17_LESSON_PARCELABLE_OBJECT = "S17_LESSON_PARCELABLE_OBJECT";
    public static final String S17_RESULT_LIST_PARCELABLE = "S17_RESULT_LIST_PARCELABLE";

    // For View.
    private Toolbar mToolbar;
    private AppCompatTextView mTvTitle;
    private AppCompatButton mTvTryAgain;
    private AppCompatTextView mTvPointNumber;
    private AppCompatTextView mTvRankingNumber;
    private RecyclerView mRecyclerViewResultList;
    private AppCompatTextView mTvTitleToolbar;

    // Define variable about play audio service.
    private AudioService mAudioService;
    private boolean isBound = false;

    private Realm mRealm;

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
        if (getLesson2IntentData() == null || getResults() == null) {
            finish();

            return;
        }

        // Khởi tạo đối tượng realm.
        mRealm = Realm.getDefaultInstance();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /**
         * Set layout for window.
         */
        setContentView(R.layout.s17_trial_test_result_activity);

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

    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(0, 0);
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
     * Phương thức dừng để gọi từ adapter của class {@link S22PracticeResult_Adapter}.
     *
     * @param position Value int.
     */
    public void onImgSoundItemClick(int position) {
        playAudioWithPath(position);
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
        S17TrialTestResult_Adapter s17TrialTestResultAdapter = (S17TrialTestResult_Adapter) mRecyclerViewResultList.getAdapter();

        // Lấy tên file audio.
        if (s17TrialTestResultAdapter != null && s17TrialTestResultAdapter.getItem(position) != null) {
            Result result = s17TrialTestResultAdapter.getItem(position);

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
     * Init View.
     */
    private void initView(List<Result> resultList) {
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mTvTitle = (AppCompatTextView) findViewById(R.id.mTvTitle);
        mTvTryAgain = (AppCompatButton) findViewById(R.id.mTvTryAgain);
        mTvPointNumber = (AppCompatTextView) findViewById(R.id.mTvPointNumber);
        mTvRankingNumber = (AppCompatTextView) findViewById(R.id.mTvRankingNumber);
        mRecyclerViewResultList = (RecyclerView) findViewById(R.id.mRecyclerView);
        mTvTitleToolbar = (AppCompatTextView) mToolbar.findViewById(R.id.mTvTitleToolbar);

        AppCompatTextView mTvTotal = (AppCompatTextView) findViewById(R.id.mTvTotal);
        AppCompatTextView mTvCorrect = (AppCompatTextView) findViewById(R.id.mTvCorrect);
        AppCompatImageView mImgMax = (AppCompatImageView) findViewById(R.id.mImgMax);
        AppCompatImageView mImgSpace = (AppCompatImageView) findViewById(R.id.mImgSpace);

        mTvCorrect.setText(String.valueOf(totalCorrect(resultList)));
        mTvTotal.setText(String.valueOf(resultList.size()));

        // Set giá trị default cho number ranking.
        mTvRankingNumber.setText("...");

        // Tính số câu trả lời đúng.
        int correct = totalCorrect(resultList);

        mTvTotal.setVisibility(correct == resultList.size() ? View.GONE : View.VISIBLE);
        mTvCorrect.setVisibility(correct == resultList.size() ? View.GONE : View.VISIBLE);
        mImgSpace.setVisibility(correct == resultList.size() ? View.GONE : View.VISIBLE);
        mImgMax.setVisibility(correct == resultList.size() ? View.VISIBLE : View.GONE);
    }

    /**
     * Tính câu trả lời đúng.
     *
     * @return Số câu đúng.
     */
    private int totalCorrect(List<Result> resultList) {
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
     * Lấy tổng thời gian trả lời.
     *
     * @return Số câu đúng.
     */
    private double timeTotal(List<Result> resultList) {
        double timeTotal = 0;

        if (resultList.isEmpty()) {
            return timeTotal;
        }

        for (int i = 0; i < resultList.size(); i++) {
            if (resultList.get(i).is_correct()) {
                timeTotal = timeTotal + resultList.get(i).time_complete;
            }
        }

        return timeTotal;
    }

    /**
     * Set Data.
     */
    private void setData(List<Result> resultList) {
        Lesson lesson = getLesson2IntentData();

        setTitleScreen(lesson);
        S17TrialTestResult_Adapter s17TrialTestResult_adapter = new S17TrialTestResult_Adapter(this, resultList);
        String sendLogUrl = AppConfig.getApiBaseUrl() + Definition.API.LOG;

        Drawable dividerDrawable = ContextCompat.getDrawable(this, R.drawable.bg_list_line_divider);
        mRecyclerViewResultList.addItemDecoration(new DividerItemDecoration(dividerDrawable));
        mRecyclerViewResultList.setLayoutManager(new WrappingLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerViewResultList.setNestedScrollingEnabled(false);
        mRecyclerViewResultList.setFocusable(false);
        mRecyclerViewResultList.setAdapter(s17TrialTestResult_adapter);

        // Tính point theo công thức.
        double totalCorrect = (double) totalCorrect(resultList);
        double point = 0;

        if (totalCorrect < 30) {
            point = totalCorrect * 5;
        } else if (totalCorrect < 40) {
            point = totalCorrect * 6;
        } else if (totalCorrect < 50) {
            point = totalCorrect * 7;
        } else if (totalCorrect < 60) {
            point = totalCorrect * 9;
        } else {
            point = 600;
        }

        double timeTotal = timeTotal(resultList);

        double timeAverage = timeTotal / resultList.size();

        double timePoint = (4 - timeAverage) * (4 - timeAverage) * 0.85 * totalCorrect;

        if (timePoint > 400) {
            timePoint = 400;
        }

        String pointNumber = String.format(Locale.US, Definition.Result.RESULT_FORMAT_TIME_POINT, (point + timePoint));
        mTvPointNumber.setText(pointNumber);

        double rankingPoint = point + timePoint;

        if (rankingPoint >= 1000) {
            rankingPoint = 1000;
        }

        // Gởi ranking point lên server.
        sendRankingPointToServer(rankingPoint, lesson);
    }

    /**
     * Gởi ranking point to server.
     *
     * @param point Value.
     */
    private void sendRankingPointToServer(double point, Lesson lesson) {
        // Lấy data ngầm nên không show dialog.
        // Lấy thông tin user hiện thời vừa đăng nhập để lấy Access-Token hợp lệ.
        UserModel userModel = LocalAppUtil.getLastLoginUserInfo(this);
        String accessToken = null;
        if (userModel != null && userModel.tokenInfo != null) {
            accessToken = userModel.tokenInfo.access_token;
        }

        RequestBody formBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_LESSON_ID, String.valueOf(lesson.getId()))
                .add(Definition.Request.PARAM_POINT, String.valueOf(point))
                .build();

        /* Bước 2: Request lên API Server để lấy danh sách Lesson List. */
        // Chuẩn bị dữ kiện để gửi lên Server.
        String authHeader = String.format("%s %s", Definition.Request.HEADER_BEARER2, accessToken);
        Request getRankingWeekRequest = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + Definition.API.USER_POINT_TEST)
                .header(Definition.Request.HEADER_AUTHORIZATION, authHeader)
                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                .header(Definition.Request.PARAM_CLIENT_TOKEN, AppConfig.getClientToken())
                .post(formBody)
                .build();

        // Get OkHttpClient object with default timeout configurations.
        OkHttpClient okHttpClient = NetworkUtil.getDefaultHttpClient(this);

        // Tiến hành gửi request bất đồng bộ (asynchronous) lên Server.
        okHttpClient.newCall(getRankingWeekRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    final String responseString = response.body().string();

                    // Hiển thị data lên text view ranking.
                    if (!TextUtils.isEmpty(responseString))
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                processDataTesting(responseString);
                            }
                        });
                }
            }
        });
    }

    /**
     * Method progressing data testing.
     */
    private void processDataTesting(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject(data);

                    if (jsonObject.has(Definition.Response.DATA)) {
                        boolean isSuccess = jsonObject.getBoolean(Definition.Response.SUCCESS);

                        if (isSuccess) {
                            JSONObject data = jsonObject.getJSONObject(Definition.Response.DATA);

                            int ranking = data.getInt(Definition.JSON.RANKING_KEY);

                            if (ranking != 0) {
                                mTvRankingNumber.setText(String.valueOf(ranking));
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.d(LOG_TAG, e.getMessage());
                }
            }
        });
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

        String title = getString(R.string.common_module__testing__trial_title) + Definition.General.BREADCRUMB_SEPARATOR + getString(R.string.common_module__take_a_test) + Definition.General.BREADCRUMB_SEPARATOR + getString(R.string.common_module__testing__result);
        mTvTitle.setText(title);
    }

    /**
     * Method set sự kiện for View.
     */
    protected void setEvent() {
        mTvTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Người dùng chọn thử lại thì set thông tin để activity trước bắt sự kiện.
                Intent intent = getIntent();
                setResult(RESULT_OK, intent);

                finish();
            }
        });
    }

    /**
     * Đi đến màn hình S15KnowledgeDetail khi click 1 item trên list result Adapter.
     * Open public method using call it from adapter.
     *
     * @param position Value position nơi xảy ra sự kiện click.
     */
    public void gotoScreenS15KnowLedgeList(int position) {
        // Đi đến màn hình Knowledge Detail Activity.
        Lesson lesson = getLesson2IntentData();

        if (lesson != null) {
            // Tìm ra vị trí hiện tại của con knowledge detail trước khi goto đến nó.
            S17TrialTestResult_Adapter s17TrialTestResultAdapter = (S17TrialTestResult_Adapter) mRecyclerViewResultList.getAdapter();

            if (s17TrialTestResultAdapter != null && s17TrialTestResultAdapter.getItem(position) != null) {
                Result result = s17TrialTestResultAdapter.getItem(position);

                // Tìm tất cả KnowledgeDao dựa vào lesson id và level value , category value of question.
                RealmResults<KnowledgeDao> knowledgeDaos = mRealm.where(KnowledgeDao.class)
                        .equalTo(Definition.Database.Question.QUESTION_FIELD_LESSON_NUMBER, lesson.getNumber())
                        .equalTo(Definition.Database.Knowledge.KNOWLEDGE_FIELD_LEVEL, result.getLevel())
                        .equalTo(Definition.Database.Knowledge.KNOWLEDGE_FIELD_CATEGORY, result.getCategory())
                        .findAll();

                // Tìm con knowledgeDao Detail và vị trí của nó trong mảng.
                KnowledgeDao knowledgeDao = mRealm.where(KnowledgeDao.class)
                        .equalTo(Definition.Database.Question.QUESTION_FIELD_LESSON_NUMBER, lesson.getNumber())
                        .equalTo(Definition.Database.Knowledge.KNOWLEDGE_FIELD_LEVEL, result.getLevel())
                        .equalTo(Definition.Database.Knowledge.KNOWLEDGE_FIELD_CATEGORY, result.getCategory())
                        .equalTo(Definition.Database.Knowledge.KNOWLEDGE_FIELD_NUMBER, result.getKnowledge_number())
                        .findFirst();

                // Nếu tìm có dữ liệu mảng và con detail.
                if (!knowledgeDaos.isEmpty() && knowledgeDao != null) {
                    if (knowledgeDaos.contains(knowledgeDao)) {
                        int index = knowledgeDaos.indexOf(knowledgeDao);

                        // Ở đây ta phải set các giá trị level và category của lesson lấy value từ con question cụ thể là con result.
                        lesson.setLevel(result.getLevel());
                        lesson.setCategory(result.getCategory());

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
     * Lấy đối tượng lesson receive from intent.
     *
     * @return Object lesson2.
     */

    private Lesson getLesson2IntentData() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getParcelable(S17_LESSON_PARCELABLE_OBJECT);
        }

        return null;
    }

    /**
     * Lấy danh sách kết quả test của user.
     *
     * @return List Result.
     */
    private List<Result> getResults() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getParcelableArrayList(S17_RESULT_LIST_PARCELABLE);
        }

        return null;
    }

    // Todo sẽ remove tất cả các biến và phương thức cũ sau khi convert thay đổi chức năng của màn hình là ok.
//    private long mCourseID = -1;
//    private long mHistoryID = -1;
//    private String mExamType = "";
//    private String mDataDir;
//
//    private RelativeLayout mRlResultHeader;
//    private PieChart mPcResult;
//    private TextView mTvLevelGood;
//    private TextView mTvLevelBad;
//    private RecyclerView mRvListAnswers;
//    private Toolbar mToolbar;
//
//    private SharedPreferences mSharedPreferences;
//    private Realm mRealm;
//    private S17TrialTestResult_Adapter mS17TrialTestResultAdapter;
//
//    private AudioService mAudioService;
//    private boolean isBound = false;
//    private ServiceConnection mAudioServiceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//            AudioService.AudioBinder binder = (AudioService.AudioBinder) iBinder;
//            mAudioService = binder.getService();
//            isBound = true;
//            mAudioService.setPlayAudioCallback(null);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            isBound = false;
//            Log.e(LOG_TAG, "Connect Service error!");
//        }
//    };
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.s17_trial_test_result_activity);
//        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
//        getIntentData();
//        initializeLayout();
//        loadAndFillData();
//    }
//
//    private void getIntentData() {
//        if (getIntent().getExtras() != null) {
//            mCourseID = getIntent().getExtras().getLong(Definition.General.COURSE_ID, -1);
//            mHistoryID = getIntent().getExtras().getLong(Definition.General.HISTORY_ID, -1);
//            mExamType = getIntent().getExtras().getString(Definition.General.LEARNING_TYPE, "");
//        }
//    }
//
//    private void initializeLayout() {
//        mToolbar = (Toolbar) findViewById(R.id.toolbar);
//        mRlResultHeader = (RelativeLayout) findViewById(R.id.rl_result);
//        mPcResult = (PieChart) findViewById(R.id.pc_result);
//        mTvLevelGood = (TextView) findViewById(R.id.tv_level_good);
//        mTvLevelBad = (TextView) findViewById(R.id.tv_level_bad);
//        mRvListAnswers = (RecyclerView) findViewById(R.id.rv_history);
//
//        setSupportActionBar(mToolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
//        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onBackPressed();
//            }
//        });
//
//        mTvLevelGood.setOnClickListener(this);
//        mTvLevelBad.setOnClickListener(this);
//
//        Drawable dividerDrawable = ContextCompat.getDrawable(this, R.drawable.bg_list_line_divider);
//        mRvListAnswers.setLayoutManager(new WrappingLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
//        mRvListAnswers.addItemDecoration(new DividerItemDecoration(dividerDrawable));
//        mRvListAnswers.setNestedScrollingEnabled(false);
//        mRvListAnswers.setFocusable(false);
//        setUpPieChart();
//    }
//
//    private void setUpPieChart() {
//        mPcResult.setDescription(new Description());
//        mPcResult.setExtraOffsets(0, 0, 0, 0);
//        mPcResult.setDragDecelerationFrictionCoef(0.95f);
//        mPcResult.setDrawHoleEnabled(true);
//        mPcResult.setHoleColor(Color.TRANSPARENT);
//        mPcResult.setTransparentCircleColor(Color.TRANSPARENT);
//        mPcResult.setTransparentCircleAlpha(110);
//        mPcResult.setHoleRadius(80f);
//        mPcResult.setTransparentCircleRadius(80f);
//        mPcResult.setDrawCenterText(true);
//        mPcResult.setCenterTextSize(27f);
//        mPcResult.setRotationAngle(-90);
//        mPcResult.setRotationEnabled(false);
//        mPcResult.setHighlightPerTapEnabled(false);
//        mPcResult.setDrawEntryLabels(false);
//        mPcResult.getLegend().setEnabled(false);
//    }
//
//    private void loadAndFillData() {
//        mRealm = Realm.getDefaultInstance();
//        Course course = mRealm.where(Course.class)
//                .equalTo(Definition.Database.FIELD_ID, mCourseID)
//                .findFirst();
//        if (course != null) {
//            mDataDir = course.getData_directory();
//        }
//        ExamLog examLog = mRealm.where(ExamLog.class)
//                .equalTo(Definition.Database.FIELD_ID, mHistoryID)
//                .findFirst();
//        if (examLog != null) {
//            if (examLog.getType().equalsIgnoreCase(Definition.General.PRACTICE)) {
//                setTitle(getString(R.string.title_practice_result));
//            } else {
//                setTitle(getString(R.string.title_test_result));
//            }
//            RealmList<Result> listQuestions = examLog.getQuestions();
//            if (listQuestions != null && listQuestions.size() > 0) {
//                int correctCount = 0;
//                for (Result result : listQuestions) {
//                    if (result.is_correct()) {
//                        correctCount++;
//                    }
//                }
//                updatePieChartResult(correctCount, listQuestions.size());
//                float rate = (float) correctCount / (float) listQuestions.size();
//                if (rate >= 0.75) {
//                    updateLevelMessage(true);
//                } else {
//                    updateLevelMessage(false);
//                }
//                mS17TrialTestResultAdapter = new S17TrialTestResult_Adapter(this, listQuestions);
//                mRvListAnswers.setAdapter(mS17TrialTestResultAdapter);
//            }
//            requestSendLog(examLog);
//        }
//    }
//
//    private void requestSendLog(ExamLog examLog) {
//
//        String sendLogUrl = AppConfig.getServer() + Definition.API.LOG;
//
//        Log.d(LOG_TAG, "SendLogURL: " + sendLogUrl);
//        Log.d(LOG_TAG, "AccessToken: " + mSharedPreferences.getString(Definition.SharedPreferencesKey.ACCESS_TOKEN, ""));
//
//        OkHttpClient client = new OkHttpClient.Builder()
//                .connectTimeout(15, TimeUnit.SECONDS)
//                .writeTimeout(15, TimeUnit.SECONDS)
//                .readTimeout(15, TimeUnit.SECONDS)
//                .build();
//
//        JSONArray jsonArray = new JSONArray();
//        jsonArray.put(examLog.toJsonObject());
//
//        Log.d(LOG_TAG, "HistoryLog: " + jsonArray.toString());
//
//        RequestBody formBody = new FormBody.Builder()
//                .add(Definition.Request.PARAM_TYPE, examLog.getType())
//                .add(Definition.Request.PARAM_DATA, jsonArray.toString())
//                .build();
//
//        Request request = new Request.Builder()
//                .header(Definition.Request.HEADER_AUTHORIZATION,
//                        Definition.Request.HEADER_BEARER + mSharedPreferences.getString(Definition.SharedPreferencesKey.ACCESS_TOKEN, ""))
//                .url(sendLogUrl)
//                .post(formBody)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.e(LOG_TAG, "onFailure()");
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                // Read data on the worker thread
//                final String responseData = response.body().string();
//                Log.d(LOG_TAG, "onResponse()");
//                Log.d(LOG_TAG, "Response: " + responseData);
//                try {
//                    JSONObject responseJsonObject = new JSONObject(responseData);
//                    boolean isSuccess = responseJsonObject.getBoolean(Definition.Response.SUCCESS);
//                    if (isSuccess) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                mRealm.executeTransaction(new Realm.Transaction() {
//                                    @Override
//                                    public void execute(Realm realm) {
//                                        ExamLog examLog = mRealm.where(ExamLog.class)
//                                                .equalTo(Definition.Database.FIELD_ID, mHistoryID)
//                                                .findFirst();
//                                        examLog.setIs_send(true);
//                                    }
//                                });
//                            }
//                        });
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
//
//    private void updateLevelMessage(boolean isGood) {
//        int res_id = 0;
//        if (isGood) {
//            mTvLevelGood.setVisibility(View.VISIBLE);
//            mTvLevelBad.setVisibility(View.GONE);
//            res_id = R.drawable.bg_good_result;
//        } else {
//            mTvLevelGood.setVisibility(View.GONE);
//            mTvLevelBad.setVisibility(View.VISIBLE);
//            if (mExamType.equalsIgnoreCase(Definition.Constants.TYPE_PRACTICE)) {
//                res_id = R.drawable.bg_practice_result_bad;
//            } else if (mExamType.equalsIgnoreCase(Definition.Constants.TYPE_TEST)) {
//                res_id = R.drawable.bg_test_result_bad;
//            }
//        }
//        int headerWith = getResources().getDisplayMetrics().widthPixels;
//        int headerHeight = getResources().getDisplayMetrics().densityDpi / 160 * 245;
//        Bitmap bitmap = BitmapUtils.decodeBitmapFromResource(this, res_id, headerWith, headerHeight);
//        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            mRlResultHeader.setBackground(drawable);
//        } else {
//            //noinspection deprecation
//            mRlResultHeader.setBackgroundDrawable(drawable);
//        }
//    }
//
//    private void updatePieChartResult(int correctCount, int totalQuesiton) {
//        String text = correctCount + "/" + totalQuesiton;
//        SpannableString textCenter = new SpannableString(text);
//        textCenter.setSpan(new RelativeSizeSpan(2f), 0, text.indexOf("/"), 0);
//
//        ArrayList<PieEntry> entries = new ArrayList<>();
//        entries.add(new PieEntry((totalQuesiton - correctCount), "Wrong"));
//        entries.add(new PieEntry(correctCount, "Correct"));
//        ArrayList<Integer> colors = new ArrayList<>();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            colors.add(getColor(R.color.result_wrong_color));
//            colors.add(getColor(R.color.result_correct_color));
//        } else {
//            colors.add(getResources().getColor(R.color.result_wrong_color));
//            colors.add(getResources().getColor(R.color.result_correct_color));
//        }
//        PieDataSet dataSet = new PieDataSet(entries, "Result");
//        dataSet.setDrawValues(false);
//        dataSet.setSliceSpace(0f);
//        dataSet.setSelectionShift(5f);
//        dataSet.setColors(colors);
//
//        PieData data = new PieData(dataSet);
//        mPcResult.setCenterText(textCenter);
//        mPcResult.setData(data);
//        mPcResult.highlightValues(null);
//        mPcResult.invalidate();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Intent bindServiceIntent = new Intent(this, AudioService.class);
//        bindService(bindServiceIntent, mAudioServiceConnection, Context.BIND_AUTO_CREATE);
//    }
//
//    public void playAudio(String audioName, int offset, int duration) {
//        if (mAudioService != null && isBound) {
//            mAudioService.playAudio(mDataDir + Definition.FileData.AUDIO_DIRECTORY + audioName + Definition.General.MP3_TYPE, offset, duration);
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (mAudioService != null && isBound) {
//            unbindService(mAudioServiceConnection);
//            isBound = false;
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        setResult(RESULT_CANCELED);
//        finish();
//    }
//
//    @Override
//    protected void onDestroy() {
//        if (mRealm != null) {
//            mRealm.close();
//        }
//        super.onDestroy();
//    }
//
//    @Override
//    public void onClick(View view) {
//        Intent data = new Intent();
//        if (view.getId() == R.id.tv_level_good) {
//            data.putExtra(Definition.General.RESULT, Definition.Constants.RESULT_GOOD);
//            setResult(RESULT_OK, data);
//            finish();
//        } else if (view.getId() == R.id.tv_level_bad) {
//            data.putExtra(Definition.General.RESULT, Definition.Constants.RESULT_BAD);
//            setResult(RESULT_OK, data);
//            finish();
//        }
//    }
}
