package com.honkidenihongo.pre.gui.flashcard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.adapter.S14Flashcard_Note_Adapter;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.BreadcrumbUtil;
import com.honkidenihongo.pre.common.util.LocalAppUtil;
import com.honkidenihongo.pre.common.util.MediaUtil;
import com.honkidenihongo.pre.dac.dao.KnowledgeDao;
import com.honkidenihongo.pre.gui.widget.FlipAnimation;
import com.honkidenihongo.pre.gui.widget.SwipeStack;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.LogKnowledgeDaoRemember;
import com.honkidenihongo.pre.model.UserModel;
import com.honkidenihongo.pre.model.constant.Category;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Screen display with Flashcard.
 *
 * @author binh.dt.
 * @since 25-Nov-2016.
 */
public class S14Flashcard_Note_Fragment extends Fragment implements SwipeStack.SwipeStackListener, SwipeStack.SwipeProgressListener {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S14Flashcard_Note_Fragment.class.getName();
    private static final String PATH = "/";
    private static final int MSG = 1;

    private final List<KnowledgeDao> mKnowledgeDaoList = new ArrayList<>();
    private final List<KnowledgeDao> mKnowledgeDaoRemember = new ArrayList<>();

    private static final long MIN_CLICK_INTERVAL = 500;
    private long mLastClickTime;
    private Realm mRealm;

    // Dùng biến toàn cục để reset data.

    // For view.
    private SwipeStack mSwipeStack;
    private AppCompatImageView mImgLater;
    private AppCompatImageView mImgRemember;
    private AppCompatTextView mTvLater;
    private AppCompatTextView mTvRemember;
    private AppCompatTextView mTvPosition;
    private AppCompatImageButton mImgRefresh;
    private AppCompatTextView mTvTitle;
    private AppCompatTextView mTvTotal;

    private Context mContext;

    // Thread play audio.
    private Handler mHandlerAutoPlayAudio = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            synchronized (S14Flashcard_Note_Fragment.this) {
                if (msg.what == MSG) {
                    playAudio();
                }
            }
        }
    };

    /**
     * Method receive data.
     *
     * @param lesson Value.
     */
    public static S14Flashcard_Note_Fragment newInstance(Lesson lesson) {
        S14Flashcard_Note_Fragment fragment = new S14Flashcard_Note_Fragment();

        Bundle arguments = new Bundle();
        arguments.putParcelable(S14Flashcard_Activity.S14_LESSON_PARCELABLE_OBJECT, lesson);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo đối tượng realm.
        mRealm = Realm.getDefaultInstance();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.s14_flashcard_note_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        /**
         * Init View for layout.
         */
        initView(view);

        /**
         * Set data for View.
         */
        if (getDataLesson2() != null) {
            setData(getDataLesson2());
        }

        /**
         * Set event for View.
         */
        setEvent();
    }

    @Override
    public void onStop() {
        Log.e(LOG_TAG, "onStop");

        closeRealm();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        mHandlerAutoPlayAudio.removeMessages(MSG);

        super.onDestroy();
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
     * Mở public phương thức để gọi nó từ adapter {@link S14Flashcard_Note_Fragment}.
     * <p>
     * Nhận data từ activity truyền vào.
     *
     * @return Lesson.
     */
    public Lesson getDataLesson2() {
        Bundle arguments = getArguments();

        if (arguments != null) {
            return arguments.getParcelable(S14Flashcard_Activity.S14_LESSON_PARCELABLE_OBJECT);
        }

        return null;
    }

    /**
     * Get KnowledgeDao with lesson.
     *
     * @param lesson Value Lesson.
     * @return KnowledgeDao.
     */
    private RealmResults<KnowledgeDao> getKnowledgeDao(Lesson lesson) {
        return mRealm.where(KnowledgeDao.class)
                .equalTo(Definition.Database.Knowledge.KNOWLEDGE_FIELD_LESSON_NUMBER, lesson.getNumber())
                .equalTo(Definition.Database.Lesson.LESSON_FIELD_LEVEL, lesson.getLevel())
                .equalTo(Definition.Database.Lesson.LESSON_FIELD_CATEGORY, lesson.getCategory())
                .findAll();
    }

    /**
     * Get list KnowledgeDetailDao.
     *
     * @return List KnowledgeDetailDao.
     */
    private List<KnowledgeDao> getKnowledgeDetailDaos(Lesson mLesson) {
        List<KnowledgeDao> listResult = new ArrayList<>();

        List<KnowledgeDao> listCheck = new ArrayList<>();
        List<LogKnowledgeDaoRemember> listRemember = new ArrayList<>();

        listCheck.addAll(getKnowledgeDao(mLesson));
        listRemember.addAll(LogKnowledgeDaoRemember.getAllRemember(mLesson));

        if (listCheck.size() == listRemember.size()) {
            // Delete all mRemember and load all.
            LogKnowledgeDaoRemember.deleteAll(mLesson);

            listResult.addAll(listCheck);
        } else {
            for (KnowledgeDao knowledgeDetailDao : listCheck) {
                if (!LogKnowledgeDaoRemember.exitsValue(mRealm, knowledgeDetailDao.id)) {
                    listResult.add(knowledgeDetailDao);
                }
            }
        }

        return listResult;
    }

    /**
     * Set Data for View.
     */
    private void setData(Lesson lesson) {
        setTitleScreen(lesson);

        resetData();
    }

    /**
     * Lấy dữ liệu mới từ database lên UI.
     */
    private void resetData() {
        mKnowledgeDaoList.clear();
        mKnowledgeDaoRemember.clear();

        mKnowledgeDaoList.addAll(getKnowledgeDetailDaos(getDataLesson2()));

        S14Flashcard_Note_Adapter mS14Flashcard_note_adapter = new S14Flashcard_Note_Adapter(mContext, mKnowledgeDaoList, S14Flashcard_Note_Fragment.this);

        mTvPosition.setText("1");
        String total = PATH + String.valueOf(mKnowledgeDaoList.size() - mKnowledgeDaoRemember.size());
        mTvTotal.setText(total);

        mSwipeStack.setAdapter(mS14Flashcard_note_adapter);
        mSwipeStack.resetStack();

        // Kiểm tra data có hay không show view.
        mTvPosition.setVisibility(mKnowledgeDaoList.isEmpty() ? View.GONE : View.VISIBLE);
        mTvTotal.setVisibility(mKnowledgeDaoList.isEmpty() ? View.GONE : View.VISIBLE);
        mImgRemember.setVisibility(mKnowledgeDaoList.isEmpty() ? View.GONE : View.VISIBLE);
        mImgLater.setVisibility(mKnowledgeDaoList.isEmpty() ? View.GONE : View.VISIBLE);
        mTvLater.setVisibility(mKnowledgeDaoList.isEmpty() ? View.GONE : View.VISIBLE);
        mTvRemember.setVisibility(mKnowledgeDaoList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /**
     * Set title for screen.
     */
    private void setTitleScreen(Lesson lesson) {
        mTvTitle.setText(BreadcrumbUtil.getBreadcrumb(mContext, lesson, getString(R.string.common_module__flashcard)));
    }

    /**
     * Init View.
     */
    private void initView(View view) {
        mSwipeStack = (SwipeStack) view.findViewById(R.id.mSwipeStack);
        mImgLater = (AppCompatImageView) view.findViewById(R.id.mImgLater);
        mImgRemember = (AppCompatImageView) view.findViewById(R.id.mImgRemember);
        mTvLater = (AppCompatTextView) view.findViewById(R.id.mTvLater);
        mTvRemember = (AppCompatTextView) view.findViewById(R.id.mTvRemember);
        mTvPosition = (AppCompatTextView) view.findViewById(R.id.mTvPosition);
        mImgRefresh = (AppCompatImageButton) view.findViewById(R.id.mImgRefresh);
        mTvTitle = (AppCompatTextView) view.findViewById(R.id.mTvTitle);
        mTvTotal = (AppCompatTextView) view.findViewById(R.id.mTvTotal);
    }

    /**
     * Method set event for View.
     */
    private void setEvent() {
        mSwipeStack.setListener(this);
        mSwipeStack.setSwipeProgressListener(this);

        mSwipeStack.setChildClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                onChildClick(view);
            }
        });

        mImgLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getDataLesson2()==null)
                {
                    return;
                }

                // Tạo độ trễ press button đối với flashcard có photo svg.
                if(getDataLesson2().getCategory()== Category.UNIT_WORD)
                {
                    long currentClickTime = SystemClock.uptimeMillis();
                    long elapsedTime = currentClickTime - mLastClickTime;

                    mLastClickTime = currentClickTime;

                    if (elapsedTime <= MIN_CLICK_INTERVAL) {
                        return;
                    }
                }

                if (mImgRefresh.getVisibility() == View.GONE) {
                    mImgLater.setImageResource(R.drawable.s14_flash_card_ic_next_press);
                    mTvLater.setTextColor(Color.RED);

                    mSwipeStack.swipeTopViewToRight();
                }
            }
        });

        mImgRemember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getDataLesson2()==null)
                {
                    return;
                }

                // Tạo độ trễ press button đối với flashcard có photo svg.
                if(getDataLesson2().getCategory()== Category.UNIT_WORD)
                {
                    long currentClickTime = SystemClock.uptimeMillis();
                    long elapsedTime = currentClickTime - mLastClickTime;

                    mLastClickTime = currentClickTime;

                    if (elapsedTime <= MIN_CLICK_INTERVAL) {
                        return;
                    }
                }

                if (mImgRefresh.getVisibility() == View.GONE) {
                    mImgRemember.setImageResource(R.drawable.s14_flash_card_ic_pre_press);
                    mTvRemember.setTextColor(Color.BLUE);

                    mSwipeStack.swipeTopViewToLeft();
                }
            }
        });

        // Lắng nghe sự kiên view top swipe qua trái hay qua phải.
        mSwipeStack.setViewTopSwipeListener(new SwipeStack.ViewTopSwipeCallback() {
            @Override
            public void onViewTopSwipeRight() {
                mImgLater.setImageResource(R.drawable.s14_flash_card_ic_next_press);
                mTvLater.setTextColor(Color.RED);

                mImgRemember.setImageResource(R.drawable.s14_flash_card_ic_pre_normal);
                mTvRemember.setTextColor(Color.BLACK);
            }

            @Override
            public void onViewTopSwipeLeft() {
                mImgRemember.setImageResource(R.drawable.s14_flash_card_ic_pre_press);
                mTvRemember.setTextColor(Color.BLUE);

                mImgLater.setImageResource(R.drawable.s14_flash_card_ic_next_normal);
                mTvLater.setTextColor(Color.BLACK);
            }
        });

        mImgRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mKnowledgeDaoRemember.isEmpty()) {
                    if (mKnowledgeDaoRemember.size() == mKnowledgeDaoList.size()) {
                        // Nếu size mảng đã nhớ == size mảng tổng , we sẽ reset lại data từ đầu.
                        resetData();
                    } else {
                        // Ngược lại mảng tổng sẽ remove các phần tử đã nhớ ra khỏi list, kết quả trả về sẽ là mảng còn lại chưa nhớ, set mới lại adapter cho switch view.
                        for (KnowledgeDao knowledgeDaoRemember : mKnowledgeDaoRemember) {
                            if (mKnowledgeDaoList.contains(knowledgeDaoRemember)) {
                                mKnowledgeDaoList.remove(knowledgeDaoRemember);
                            }
                        }

                        // Xóa lại mảng đếm và set lại data với mảng dữ liệu sau khi remove.
                        mKnowledgeDaoRemember.clear();

                        S14Flashcard_Note_Adapter s14Flashcard_note_adapter = new S14Flashcard_Note_Adapter(mContext, mKnowledgeDaoList, S14Flashcard_Note_Fragment.this);
                        mTvPosition.setText("1");
                        String total = PATH + String.valueOf(mKnowledgeDaoList.size() - mKnowledgeDaoRemember.size());
                        mTvTotal.setText(total);

                        mSwipeStack.setAdapter(s14Flashcard_note_adapter);
                        mSwipeStack.resetStack();
                    }
                } else {
                    // Nếu mảng đã nhớ là rỗng ta cũng thực hiện lấy mới data như ban đầu.
                    resetData();
                }

                // Ẩn hiện view.
                mSwipeStack.setVisibility(View.VISIBLE);
                mTvPosition.setVisibility(View.VISIBLE);
                mTvTotal.setVisibility(View.VISIBLE);

                mImgRefresh.setVisibility(View.GONE);
            }
        });
    }
    /**
     * Open public using call it from adapter of class.
     * Method using get Uri of file picture.
     *
     * @param position Value position current.
     * @return Uri.
     */
    public File getFileSvgOfLesson(int position) {
        UserModel userModel = LocalAppUtil.getLastLoginUserInfo(mContext);

        if (userModel == null || getDataLesson2() == null || mKnowledgeDaoList.isEmpty() || position > mKnowledgeDaoList.size()) {
            return null;
        }

        return MediaUtil.getFilePicture(mContext, userModel, getDataLesson2(), mKnowledgeDaoList.get(position).picture_file);
    }

    /**
     * Click on CardView children.
     *
     * @param view Value item.
     */
    private void onChildClick(final View view) {
        // Disable sự kiện click của view trước khi chạy hiệu ứng.
        mSwipeStack.setEnabled(false);

        final LinearLayout cardFaceView = (LinearLayout) view.findViewById(R.id.mRlViewFace);
        final RelativeLayout cardBackView = (RelativeLayout) view.findViewById(R.id.mRlViewBack);

        FlipAnimation flipAnimation = new FlipAnimation(cardFaceView, cardBackView);

        if (cardFaceView.getVisibility() == View.GONE) {
            flipAnimation.reverse();
        }

        flipAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardFaceView.setAlpha(0.6f);
                cardBackView.setAlpha(0.6f);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardFaceView.setAlpha(1.0f);
                cardBackView.setAlpha(1.0f);

                // Mở lại cho người dùng tương tác view.
                mSwipeStack.setEnabled(true);

                // Chỉ chạy audio khi mặt trái được hiện thị.
                if (cardBackView.getVisibility() == View.VISIBLE) {
                    mHandlerAutoPlayAudio.removeMessages(MSG);
                    Activity activity = (Activity) mContext;

                    if (activity != null && !activity.isFinishing() && activity instanceof S14Flashcard_Activity) {
                        ((S14Flashcard_Activity) activity).pauseAudio();
                    }

                    mHandlerAutoPlayAudio.sendEmptyMessageDelayed(MSG, 500);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        view.startAnimation(flipAnimation);
    }

    /**
     * Play audio when view child click.
     */
    private void playAudio() {
        Activity activity = (Activity) mContext;

        if (activity != null && !activity.isFinishing() && activity instanceof S14Flashcard_Activity) {
            int position = mSwipeStack.getCurrentPosition();

            if (position < mKnowledgeDaoList.size()) {
                ((S14Flashcard_Activity) activity).playAudioWithPath(mKnowledgeDaoList.get(position));
            }
        }
    }

    /**
     * Open public method.
     * Method implement Adapter item click.
     */
    public void onImageSoundAdapterClick(int position) {
        if (mContext instanceof S14Flashcard_Activity && position < mKnowledgeDaoList.size()) {
            if (!TextUtils.isEmpty(mKnowledgeDaoList.get(position).voice_file)) {
                ((S14Flashcard_Activity) mContext).playAudioWithPath(mKnowledgeDaoList.get(position));
            }
        }
    }

    @Override
    public void onViewSwipedToLeft(final int position) {
        // Dừng audio if đang chạy audio nào trước đó.
        mHandlerAutoPlayAudio.removeMessages(MSG);

        mImgLater.setImageResource(R.drawable.s14_flash_card_ic_next_normal);
        mImgRemember.setImageResource(R.drawable.s14_flash_card_ic_pre_normal);

        // Set màu default lại cho 2 text.
        mTvLater.setTextColor(Color.BLACK);
        mTvRemember.setTextColor(Color.BLACK);

        if (getDataLesson2() != null && position < mKnowledgeDaoList.size()) {
            Lesson mLesson = getDataLesson2();
            // Update it is Remembered and save it into database.
            final KnowledgeDao knowledgeDao = mKnowledgeDaoList.get(position);

            final LogKnowledgeDaoRemember logKnowledgeDaoRemember = new LogKnowledgeDaoRemember();
            logKnowledgeDaoRemember.id = knowledgeDao.id;
            logKnowledgeDaoRemember.lesson_id = mLesson.getId();
            logKnowledgeDaoRemember.category = mLesson.getCategory();
            logKnowledgeDaoRemember.level = mLesson.getLevel();

            logKnowledgeDaoRemember.saveOrUpdate();

            // Nếu phần tử đó chưa có trong mảng remember thì add nó vào.
            if (!mKnowledgeDaoRemember.contains(knowledgeDao)) {
                mKnowledgeDaoRemember.add(knowledgeDao);
            }

            String total = PATH + String.valueOf(mKnowledgeDaoList.size() - mKnowledgeDaoRemember.size());
            mTvTotal.setText(total);
        }
    }

    @Override
    public void onViewSwipedToRight(int position) {
        // Dừng audio if đang chạy audio nào trước đó.
        mHandlerAutoPlayAudio.removeMessages(MSG);

        mImgLater.setImageResource(R.drawable.s14_flash_card_ic_next_normal);
        mImgRemember.setImageResource(R.drawable.s14_flash_card_ic_pre_normal);

        // Set màu default lại cho 2 text.
        mTvLater.setTextColor(Color.BLACK);
        mTvRemember.setTextColor(Color.BLACK);

        // Lấy position hiện tại đang được set trên text view position.
        if (!TextUtils.isEmpty(mTvPosition.getText().toString())) {
            int current = Integer.parseInt(mTvPosition.getText().toString().trim());
            mTvPosition.setText(String.valueOf(current + 1));
            String total = PATH + String.valueOf(mKnowledgeDaoList.size() - mKnowledgeDaoRemember.size());
            mTvTotal.setText(total);
        }
    }

    @Override
    public void onStackEmpty() {
        mImgLater.setImageResource(R.drawable.s14_flash_card_ic_next_normal);
        mImgRemember.setImageResource(R.drawable.s14_flash_card_ic_pre_normal);

        // Set màu default lại cho 2 text.
        mTvLater.setTextColor(Color.BLACK);
        mTvRemember.setTextColor(Color.BLACK);

        // Khi switch đến trang cuối cùng thì hiện nút refresh lên đồng thời ẩn các View liên quan.
        mImgRefresh.setVisibility(View.VISIBLE);
        mSwipeStack.setVisibility(View.GONE);

        // Set lại các chỉ số cho text View position là 0 & tổng số bài còn lại chưa nhớ sẽ là giá trị mảng thực tại - đi mảng đã nhớ.
        mTvPosition.setText("0");
        String total = PATH + String.valueOf(mKnowledgeDaoList.size() - mKnowledgeDaoRemember.size());
        mTvTotal.setText(total);
    }

    @Override
    public void onSwipeStart(int position) {
    }

    @Override
    public void onSwipeProgress(int position, float progress) {
    }

    @Override
    public void onSwipeEnd(int position) {
        // Khi người dùng không tương tác với view top nữa thì set các drawable của 2 nút qua trái phải về default.
        mImgLater.setImageResource(R.drawable.s14_flash_card_ic_next_normal);
        mImgRemember.setImageResource(R.drawable.s14_flash_card_ic_pre_normal);

        // Set màu default lại cho 2 text.
        mTvLater.setTextColor(Color.BLACK);
        mTvRemember.setTextColor(Color.BLACK);
    }
}
