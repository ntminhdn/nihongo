package com.honkidenihongo.pre.gui.knowledge;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.adapter.S15KnowledgeList_Adapter;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.BreadcrumbUtil;
import com.honkidenihongo.pre.common.util.LocalAppUtil;
import com.honkidenihongo.pre.common.util.MediaUtil;
import com.honkidenihongo.pre.common.util.MessageDialogUtil;
import com.honkidenihongo.pre.common.util.ToolbarUtil;
import com.honkidenihongo.pre.gui.widget.HelperDialog;
import com.honkidenihongo.pre.model.UserModel;
import com.honkidenihongo.pre.model.constant.Category;
import com.honkidenihongo.pre.model.constant.LanguageNumberCode;
import com.honkidenihongo.pre.dac.dao.KnowledgeDao;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.common.util.LessonNameUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.model.constant.LessonType;
import com.honkidenihongo.pre.service.AudioService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Screen hiển thị danh sách Knowledge.
 * breadcrumb.
 *
 * @author binh.tt.
 * @since 08-Nov-2016.
 */
public class S15KnowledgeList_Activity extends AppCompatActivity implements AudioService.PlayAudioCallback {
    /**
     * The Tag for logging.
     */
    public static final String LOG_TAG = S15KnowledgeList_Activity.class.getName();
    public static final String S15_LESSON_PARCELABLE_OBJECT = "S15_LESSON_PARCELABLE_OBJECT";
    private static final int MSG = 1;

    // For View.
    private RecyclerView mRecyclerViewKnowledgeList;
    private AppCompatTextView mTvTitleList;
    private AppCompatTextView mTvTitleToolbar;
    private Toolbar mToolbar;

    // Define variable about play audio service
    private AudioService mAudioService;
    private Intent bindServiceIntent;
    private boolean isBound = false;
    private Realm mRealm;
    private HelperDialog mHelperDialog;

    // Connection to bind a service that play special audio
    private ServiceConnection mAudioServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(LOG_TAG, "onServiceConnected");

            AudioService.AudioBinder binder = (AudioService.AudioBinder) iBinder;
            mAudioService = binder.getService();
            isBound = true;
            mAudioService.setPlayAudioCallback(S15KnowledgeList_Activity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
            mAudioService.setPlayAudioCallback(S15KnowledgeList_Activity.this);

            Log.d(LOG_TAG, "Connect Service error!");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getDataIntent() == null) {
            finish();

            return;
        }

        // Khởi tạo đối tượng realm.
        mRealm = Realm.getDefaultInstance();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Get layout for window.
        setContentView(R.layout.s15_knowledge_list_activity);

        // Init View.
        initView();

        // Init toolbar.
        ToolbarUtil.initToolbar(mToolbar, this);

        // Set data.
        setData(getDataIntent());

        /**
         * Show dialog help.
         */
        if (getDataIntent() != null && getDataIntent().getLevel() == LessonType.UNIT) {
            showDialogHelp();
        }
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

        bindServiceIntent = new Intent(this, AudioService.class);
        bindService(bindServiceIntent, mAudioServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        // Pause audio when pause activity.
        pauseAudio();

        // Remove service when stop activity.
        if (mAudioService != null && isBound) {
            mAudioService.setPlayAudioCallback(null);
            unbindService(mAudioServiceConnection);
            isBound = false;
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        try {
            closeRealm();

            // Remove service when stop activity.
            if (mAudioService != null && isBound) {
                mAudioService.setPlayAudioCallback(null);
                unbindService(mAudioServiceConnection);
                isBound = false;
            }

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
     * Init View.
     */
    private void initView() {
        mRecyclerViewKnowledgeList = (RecyclerView) findViewById(R.id.mRecyclerView);
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mTvTitleToolbar = (AppCompatTextView) mToolbar.findViewById(R.id.mTvTitleToolbar);
        mTvTitleList = (AppCompatTextView) findViewById(R.id.mTvTitleList);
        mHelperDialog = new HelperDialog(this, R.style.TransparentDialog, getString(getDataIntent().getCategory() == Category.UNIT_WORD ? R.string.common_help__s15_knowledge_list__word__title : R.string.common_help__s15_knowledge_list__sentence__title),
                getString(getDataIntent().getCategory() == Category.UNIT_WORD ? R.string.common_help__s15_knowledge_list__word__content : R.string.common_help__s15_knowledge_list__sentence__content));
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

                boolean isShowFirstOfScreen = prefs.getBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S14_FLASH_CASH, true);

                if (isShowFirstOfScreen) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S14_FLASH_CASH, false);
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
     */
    private void setData(Lesson lesson) {
        setTitleScreen(lesson);

        RealmResults<KnowledgeDao> knowledgeDaos = mRealm.where(KnowledgeDao.class)
                .equalTo(Definition.Database.Knowledge.KNOWLEDGE_FIELD_LESSON_NUMBER, lesson.getNumber())
                .equalTo(Definition.Database.Lesson.LESSON_FIELD_LEVEL, lesson.getLevel())
                .equalTo(Definition.Database.Lesson.LESSON_FIELD_CATEGORY, lesson.getCategory())
                .findAll();

        if (!knowledgeDaos.isEmpty()) {
            List<KnowledgeDao> mListKnowledge = new ArrayList<>();

            mListKnowledge.addAll(knowledgeDaos);

            // Khởi tạo adapter và set data cho adapter.
            S15KnowledgeList_Adapter s15KnowledgeList_adapter = new S15KnowledgeList_Adapter(S15KnowledgeList_Activity.this, lesson, mListKnowledge);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            mRecyclerViewKnowledgeList.setLayoutManager(layoutManager);
            mRecyclerViewKnowledgeList.setHasFixedSize(true);
            mRecyclerViewKnowledgeList.setAdapter(s15KnowledgeList_adapter);
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
        mTvTitleList.setText(BreadcrumbUtil.getBreadcrumb(this, lesson, getString(R.string.common_module__knowledge)));
    }

    /**
     * Get data through Intent
     */

    private Lesson getDataIntent() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getParcelable(S15_LESSON_PARCELABLE_OBJECT);
        }

        return null;
    }

    /**
     * Open public method using call it from {@link S15KnowledgeList_Adapter}.
     *
     * @param position Vị trí hiện tại của view xảy ra sự kiện click.
     */
    public void showDetailKnowledge(int position) {
        if (getDataIntent() != null) {
            Intent intent = new Intent(this, S23KnowledgeDetail_Activity.class);
            intent.putExtra(S23KnowledgeDetail_Activity.S23_LESSON_PARCELABLE_OBJECT, getDataIntent());
            intent.putExtra(S23KnowledgeDetail_Activity.S23_POSITION_CURRENT, position);
            intent.putExtra(S23KnowledgeDetail_Activity.S23_GO_TO_FROM_SCREEN, 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }
    }

    /**
     * Check file is Exits.
     *
     * @return File.
     */
    public File getFileVoice(KnowledgeDao knowledgeDao) {
        UserModel userModel = LocalAppUtil.getLastLoginUserInfo(this);

        if (knowledgeDao == null || TextUtils.isEmpty(knowledgeDao.voice_file) || getDataIntent() == null || userModel == null) {
            return null;
        }

        return MediaUtil.audioIsPrepare(this, getDataIntent(), userModel, knowledgeDao.category, knowledgeDao.voice_file);
    }

    /**
     * Play audio with file path.
     *
     * @param position Value.
     */
    public void playAudioWithPath(int position) {
        S15KnowledgeList_Adapter s15KnowledgeList_adapter = (S15KnowledgeList_Adapter) mRecyclerViewKnowledgeList.getAdapter();

        if (s15KnowledgeList_adapter != null && s15KnowledgeList_adapter.getItem(position) != null) {
            File fileAudio = getFileVoice(s15KnowledgeList_adapter.getItem(position));

            // Kiểm tra file có tồn tại hay không.
            if (fileAudio != null && fileAudio.exists()) {
                if (mAudioService != null && isBound) {
                    mAudioService.playAudioWithPath(fileAudio.getPath());
                }
            }
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

    @Override
    public void onPlayAudioFinish() {
        Log.d(LOG_TAG, "onPlayAudioFinish");
    }

    @Override
    public void onPlayAudioCompletion() {
        Log.d(LOG_TAG, "onPlayAudioCompletion");
    }
}
