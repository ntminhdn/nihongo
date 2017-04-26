package com.honkidenihongo.pre.gui.knowledge;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.caverock.androidsvg.SVG;
import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.adapter.S15KnowledgeList_Adapter;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.config.FontsConfig;
import com.honkidenihongo.pre.common.util.BreadcrumbUtil;
import com.honkidenihongo.pre.common.util.LocalAppUtil;
import com.honkidenihongo.pre.common.util.MediaUtil;
import com.honkidenihongo.pre.gui.widget.AutoResizeTextView;
import com.honkidenihongo.pre.gui.widget.LoopingViewPager;
import com.honkidenihongo.pre.common.util.ToolbarUtil;
import com.honkidenihongo.pre.gui.widget.svg.SvgDecoder;
import com.honkidenihongo.pre.gui.widget.svg.SvgDrawableTranscoder;
import com.honkidenihongo.pre.gui.widget.svg.SvgSoftwareLayerSetter;
import com.honkidenihongo.pre.model.UserModel;
import com.honkidenihongo.pre.model.constant.Category;
import com.honkidenihongo.pre.model.constant.LanguageNumberCode;
import com.honkidenihongo.pre.dac.dao.KnowledgeDao;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.common.util.LessonNameUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.service.AudioService;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Screen hiển thị danh sách Knowledge detail.
 * breadcrumb.
 *
 * @author binh.tt.
 * @since 08-Nov-2016.
 */
public class S23KnowledgeDetail_Activity extends AppCompatActivity implements AudioService.PlayAudioCallback {
    /**
     * The Tag for logging.
     */
    public static final String LOG_TAG = S23KnowledgeDetail_Activity.class.getName();
    public static final String S23_LESSON_PARCELABLE_OBJECT = "S23_LESSON_PARCELABLE_OBJECT";
    public static final String S23_POSITION_CURRENT = "S23_POSITION_CURRENT";
    /**
     * Biến dùng để biết tôi đến detail từ màn hình nào S15Knowledge list hay từ màn hình kết quả tôi qua.
     */
    public static final String S23_GO_TO_FROM_SCREEN = "S23_GO_TO_FROM_SCREEN";
    private static final int MSG = 1;
    private Realm mRealm;

    // Remove animation.
//    private static final long MIN_CLICK_INTERVAL = 600;
//    private long mLastClickTime;

    // For View.
    private RelativeLayout mViewDetail;
    private AppCompatTextView mTvTitleDetail;
    private Toolbar mToolbar;
    private AppCompatTextView mTvTitleToolbar;
    private AppCompatImageButton mImgPrevious;
    private AppCompatImageButton mImgNext;
    private LoopingViewPager mViewPager;
    private AppCompatTextView mTvPosition;

    // Define variable about play audio service
    private AudioService mAudioService;
    private boolean isBound = false;

    // Connection to bind a service that play special audio
    private ServiceConnection mAudioServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(LOG_TAG, "onServiceConnected");

            AudioService.AudioBinder binder = (AudioService.AudioBinder) iBinder;
            mAudioService = binder.getService();
            isBound = true;
            mAudioService.setPlayAudioCallback(S23KnowledgeDetail_Activity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
            mAudioService.setPlayAudioCallback(S23KnowledgeDetail_Activity.this);

            Log.d(LOG_TAG, "Connect Service error!");
        }
    };

    /**
     * Thread play audio at position.
     */
    private Handler mHandlerAutoPlayAudio = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            synchronized (S23KnowledgeDetail_Activity.this) {
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

        if (getDataIntent() == null) {
            finish();
            return;
        }

        // Khởi tạo đối tượng realm.
        mRealm = Realm.getDefaultInstance();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Get layout for window.
        setContentView(R.layout.s23_knowledge_list_detail_activity);

        // Init View.
        initView();

        // Init toolbar.
        ToolbarUtil.initToolbar(mToolbar, this);

        // Set data.
        setData(getDataIntent());

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

    /**
     * Init View.
     */
    private void initView() {
        mViewDetail = (RelativeLayout) findViewById(R.id.mViewDetail);
        mTvTitleDetail = (AppCompatTextView) findViewById(R.id.mTvTitleDetail);
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mTvTitleToolbar = (AppCompatTextView) mToolbar.findViewById(R.id.mTvTitleToolbar);
        mViewPager = (LoopingViewPager) findViewById(R.id.mViewPager);
        mImgPrevious = (AppCompatImageButton) findViewById(R.id.mImgPrevious);
        mImgNext = (AppCompatImageButton) findViewById(R.id.mImgNext);
        mTvPosition = (AppCompatTextView) findViewById(R.id.mTvPosition);
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
            mViewPager.setAdapter(new S23KnowledgeListMyPagerAdapter(this, mListKnowledge));
            // Tại mỗi thời điểm chỉ set limit 3 trang cho viewPager.
            mViewPager.setOffscreenPageLimit(3);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showDetailKnowledge(getDataIntentCurrentPosition());
                }
            }, 100);
        }
    }

    /**
     * Method set sự kiện for View.
     */
    protected void setEvent() {
        // Tất cả các sự kiện scroll to position đều bỏ qua hiện ứng chạy trang của view pager thông qua value false.
        mImgNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewPager.getCurrentItem() < mViewPager.getAdapter().getCount() - 1) {
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                } else {
                    // Set lại trang đầu tiên cho viewPager.
                    mViewPager.setCurrentItem(0, false);
                }
            }
        });

        mImgPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewPager.getCurrentItem() > 0) {
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
                } else {
                    // Set trang cuối cùng cho viewPager.
                    mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() - 1, false);
                }
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                S23KnowledgeListMyPagerAdapter s23KnowledgeListMyPagerAdapter = (S23KnowledgeListMyPagerAdapter) mViewPager.getAdapter();

                if (s23KnowledgeListMyPagerAdapter != null && s23KnowledgeListMyPagerAdapter.getItem(position) != null) {
                    mTvPosition.setText(String.valueOf(position + 1) + "/" + String.valueOf(s23KnowledgeListMyPagerAdapter.getCount()));

                    // Chạy audio khi người dùng lướt danh sách.
                    pauseAudio();

                    mHandlerAutoPlayAudio.removeMessages(MSG);
                    Message msg = new Message();
                    msg.what = MSG;
                    msg.arg1 = position;

                    mHandlerAutoPlayAudio.sendMessageDelayed(msg, 200);
                }
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
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
        S23KnowledgeListMyPagerAdapter s23KnowledgeListMyPagerAdapter = (S23KnowledgeListMyPagerAdapter) mViewPager.getAdapter();

        if (s23KnowledgeListMyPagerAdapter != null && s23KnowledgeListMyPagerAdapter.getItem(position) != null) {
            File fileAudio = getFileVoice(s23KnowledgeListMyPagerAdapter.getItem(position));

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
        mTvTitleDetail.setText(BreadcrumbUtil.getBreadcrumb(this, lesson, getString(R.string.common_module__knowledge)));
    }

    /**
     * Open public method using call it from {@link S15KnowledgeList_Adapter}.
     *
     * @param position Vị trí hiện tại của view xảy ra sự kiện click.
     */
    public void showDetailKnowledge(int position) {
        if (position < mViewPager.getAdapter().getCount()) {
            mViewPager.setCurrentItem(position, false);
            mViewDetail.setVisibility(View.VISIBLE);
            mTvPosition.setText(String.valueOf(position + 1) + "/" + String.valueOf(mViewPager.getAdapter().getCount()));

            // Nếu tôi đến từ màn hình kết quả thì disable scroll của view pager và disable 2 nút qua lại.
            if (gotoFromScreen() != 0) {
                mImgNext.setEnabled(false);
                mImgNext.setAlpha(Definition.Graphic.BLEAR);
                mImgPrevious.setEnabled(false);
                mImgPrevious.setAlpha(Definition.Graphic.BLEAR);
                mViewPager.setLocked(true);
            }
            // Remove animation.
//            Animation openAnim = AnimationUtils.loadAnimation(S23KnowledgeDetail_Activity.this, R.anim.open_animation);
//            mViewDetail.startAnimation(openAnim);
        }
    }

    /**
     * Open public method using call it from {@link S15KnowledgeList_Adapter}.
     * <p>
     * Trở vể màn hình danh sách list knowledge.
     */
    public void hideDetailKnowledge() {
        Animation closeAnim = AnimationUtils.loadAnimation(this, R.anim.close_animation);

        closeAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewDetail.setVisibility(View.GONE);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mViewDetail.startAnimation(closeAnim);
    }

    /**
     * Get data through Intent.
     */
    private Lesson getDataIntent() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getParcelable(S23_LESSON_PARCELABLE_OBJECT);
        }

        return null;
    }

    /**
     * Get data through Intent.
     */
    private int getDataIntentCurrentPosition() {
        if (getIntent().getExtras() != null) {
            return getIntent().getIntExtra(S23_POSITION_CURRENT, 0);
        }

        return 0;
    }

    /**
     * Get data through Intent.
     *
     * @return If result= 0 from S15KnowledgeList_Activity else =1 from S22PracticeResult_Activity, S17TrialTestResult_Activity.
     */
    private int gotoFromScreen() {
        if (getIntent().getExtras() != null) {
            return getIntent().getIntExtra(S23_GO_TO_FROM_SCREEN, 0);
        }

        return 0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Disable click double.
        // Remove animation.
//        long currentClickTime = SystemClock.uptimeMillis();
//        long elapsedTime = currentClickTime - mLastClickTime;
//
//        mLastClickTime = currentClickTime;
//
//        if (elapsedTime <= MIN_CLICK_INTERVAL) {
//            return;
//        }
//
//        hideDetailKnowledge();
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

    /**
     * Custom Adapter for ViewPager của Activity hiện tại.
     */
    private class S23KnowledgeListMyPagerAdapter extends PagerAdapter {
        private Context mContext;
        private List<KnowledgeDao> mListKnowledge;

        /**
         * Private constructor.
         *
         * @param mContext Value context của màn hình hiện tại.
         */
        private S23KnowledgeListMyPagerAdapter(Context mContext, List<KnowledgeDao> knowledgeDaos) {
            this.mContext = mContext;
            this.mListKnowledge = knowledgeDaos;
        }

        /**
         * Open public method.
         * Lấy đối tượng ở vị trí position.
         *
         * @param position Value.
         * @return KnowledgeDao.
         */
        public KnowledgeDao getItem(int position) {
            if (mListKnowledge != null && !mListKnowledge.isEmpty()) {
                return mListKnowledge.get(position);
            }

            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.s23_knowledge_item_detail, container, false);

            RelativeLayout mViewContentKana = (RelativeLayout) view.findViewById(R.id.mViewContentKana);
            AppCompatImageView mImgDescription = (AppCompatImageView) view.findViewById(R.id.mImgDescription);

            // Needed because of image accelaration in some devices such as samsung.
            mImgDescription.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            AutoResizeTextView mTvKana = (AutoResizeTextView) view.findViewById(R.id.mTvKana);
            AppCompatTextView mTvRomaji = (AppCompatTextView) view.findViewById(R.id.mTvRomaji);
            AppCompatTextView mTvMean = (AppCompatTextView) view.findViewById(R.id.mTvMeaning);
            AppCompatTextView mTvKanji = (AppCompatTextView) view.findViewById(R.id.mTvKanji);
            AppCompatImageButton mImgClose = (AppCompatImageButton) view.findViewById(R.id.mImgClose);
            AppCompatImageButton mImgSound = (AppCompatImageButton) view.findViewById(R.id.mImgSound);

            Typeface fontDisplay = FontsConfig.getInstance(mContext).getFont(FontsConfig.AppFont.KLEE);

            mTvKana.setText(mListKnowledge.get(position).subject_kana);

            if (fontDisplay != null) {
                mTvKana.setTypeface(fontDisplay);
            }

            mTvRomaji.setText(mListKnowledge.get(position).subject_romaji);

            // Lấy mean theo ngôn ngữ.
            String mean = "";

            // Todo hiện tại nội dung bài học tất cả đều lấy tiếng việt.
//            if (LocaleHelper.getLanguage(mContext).equals(Definition.LanguageCode.ENGLISH)) {
//                mean = mListKnowledge.get(position).meaning_en;
//            } else {
//                mean = mListKnowledge.get(position).meaning_vi;
//            }

            mean = mListKnowledge.get(position).meaning_vi;
            mTvMean.setText(mean);

            // Ẩn mean đi nếu category là hiragana or katakana.
            if (getDataIntent() != null) {
                mTvMean.setVisibility(getDataIntent().getCategory() == Category.PRE_HIRAGANA || getDataIntent().getCategory() == Category.PRE_KATAKANA ? View.INVISIBLE : View.VISIBLE);
            }

            mTvKanji.setText(mListKnowledge.get(position).subject_kanji);

            if (fontDisplay != null) {
                mTvKanji.setTypeface(fontDisplay);
            }

            mImgClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });

            File fileAudio = getFileVoice(mListKnowledge.get(position));

            // Kiểm tra file audio có tồn tại hay không if tồn tại thì show , không tồn tại thì làm mờ nó đi và không cho click.
            if (fileAudio != null && fileAudio.exists()) {
                mImgSound.setAlpha(Definition.Graphic.LIMPIDITY);
                mImgSound.setEnabled(true);
            } else {
                mImgSound.setAlpha(Definition.Graphic.BLEAR);
                mImgSound.setEnabled(false);
            }

            mImgSound.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playAudioWithPath(position);
                }
            });

            /**
             * Thực hiện load ảnh svg của item.
             */
            GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable>
                    requestBuilder = Glide.with(mContext)
                    .using(Glide.buildStreamModelLoader(Uri.class, mContext), InputStream.class)
                    .from(Uri.class)
                    .as(SVG.class)
                    .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                    .sourceEncoder(new StreamEncoder())
                    .cacheDecoder(new FileToStreamDecoder<>(new SvgDecoder()))
                    .decoder(new SvgDecoder())
                    .placeholder(null)
                    .error(null)
                    .animate(android.R.anim.fade_in)
                    .listener(new SvgSoftwareLayerSetter<Uri>());

            clearCache(position, requestBuilder, mImgDescription, mViewContentKana);

            container.addView(view);

            return view;
        }

        /**
         * Open public using call it from adapter of class.
         * Method using get Uri of file picture.
         *
         * @param position Value position current.
         * @return Uri.
         */
        private File getFileSvgOfLesson(int position) {
            UserModel userModel = LocalAppUtil.getLastLoginUserInfo(S23KnowledgeDetail_Activity.this);

            if (userModel == null || getDataIntent() == null || mListKnowledge == null || mListKnowledge.isEmpty() || position > mListKnowledge.size()) {
                return null;
            }

            return MediaUtil.getFilePicture(mContext, userModel, getDataIntent(), mListKnowledge.get(position).picture_file);
        }

        /**
         * Method using clear memory cash.
         *
         * @param requestBuilder     Value object GenericRequestBuilder.
         * @param appCompatImageView Photo display inside imageView.
         */
        private void clearCache(int position, GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder, AppCompatImageView appCompatImageView, RelativeLayout relativeLayout) {
            Glide.clear(appCompatImageView);
            Glide.get(mContext).clearMemory();
            File cacheDir = Glide.getPhotoCacheDir(mContext);

            if (cacheDir.isDirectory()) {
                for (File child : cacheDir.listFiles()) {
                    if (!child.delete()) {
                        Log.w(LOG_TAG, "cannot delete: " + child);
                    }
                }
            }

            loadFromRes(position, requestBuilder, appCompatImageView, relativeLayout);
        }

        /**
         * Load photo from local.
         *
         * @param requestBuilder     Value object GenericRequestBuilder.
         * @param appCompatImageView Photo display inside imageView.
         */
        private void loadFromRes(int position, GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder, AppCompatImageView appCompatImageView, RelativeLayout relativeLayoutContent) {
            File fileSvg = getFileSvgOfLesson(position);

            if (fileSvg != null) {
                appCompatImageView.setVisibility(View.VISIBLE);

                // Trường hợp này set layout cha chứa nội dung chữ kana và romaji có weight là bằng 0.5 cho vừa device màn hình nhỏ.
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.5f);
                relativeLayoutContent.setLayoutParams(param);

                requestBuilder.diskCacheStrategy(DiskCacheStrategy.NONE)
                        // SVG cannot be serialized so it's not worth to cache it.
                        // and the getResources() should be fast enough when acquiring the InputStream.
                        .load(Uri.fromFile(fileSvg))
                        .into(appCompatImageView);
            } else {
                appCompatImageView.setVisibility(View.GONE);
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }

        @Override
        public int getCount() {
            return mListKnowledge.size();
        }
    }
}

