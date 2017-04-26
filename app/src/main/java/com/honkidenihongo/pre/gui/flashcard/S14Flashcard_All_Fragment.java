package com.honkidenihongo.pre.gui.flashcard;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.adapter.S14Flashcard_All_Adapter;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.BreadcrumbUtil;
import com.honkidenihongo.pre.common.util.LocalAppUtil;
import com.honkidenihongo.pre.common.util.MediaUtil;
import com.honkidenihongo.pre.dac.dao.KnowledgeDao;
import com.honkidenihongo.pre.gui.widget.FlipAnimation;
import com.honkidenihongo.pre.gui.widget.SwipeStack;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.UserModel;

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
public class S14Flashcard_All_Fragment extends Fragment implements SwipeStack.SwipeStackListener, SwipeStack.SwipeProgressListener {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S14Flashcard_All_Fragment.class.getName();
    private static final String PATH = "/";

    private static final int MSG = 1;

    private final List<KnowledgeDao> mKnowledgeDetailDaoList = new ArrayList<>();
    private Context mContext;
    private Realm mRealm;

    // For view.
    private SwipeStack mSwipeStack;
    private AppCompatTextView mTvPosition;
    private AppCompatTextView mTvTitle;

    private Handler mHandlerAutoPlayAudio;

    {
        mHandlerAutoPlayAudio = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                synchronized (S14Flashcard_All_Fragment.this) {
                    if (msg.what == MSG) {
                        playAudio();
                    }
                }
            }
        };
    }

    /**
     * Method using setup data when class call.
     */
    public static S14Flashcard_All_Fragment newInstance(Lesson lesson) {
        S14Flashcard_All_Fragment fragment = new S14Flashcard_All_Fragment();

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
        return inflater.inflate(R.layout.s14_flashcard_all_fragment, container, false);

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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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
     * Mở public phương thức để gọi nó từ adapter {@link S14Flashcard_All_Adapter}.
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
     * Set Data for View.
     */
    private void setData(Lesson lesson) {
        setTitleScreen(lesson);

        S14Flashcard_All_Adapter s14Flashcard_all_adapter = new S14Flashcard_All_Adapter(mContext, mKnowledgeDetailDaoList, this);
        mSwipeStack.setAdapter(s14Flashcard_all_adapter);

        RealmResults<KnowledgeDao> knowledgeDaos = mRealm.where(KnowledgeDao.class)
                .equalTo(Definition.Database.Knowledge.KNOWLEDGE_FIELD_LESSON_NUMBER, lesson.getNumber())
                .equalTo(Definition.Database.Lesson.LESSON_FIELD_LEVEL, lesson.getLevel())
                .equalTo(Definition.Database.Lesson.LESSON_FIELD_CATEGORY, lesson.getCategory())
                .findAll();

        if (!knowledgeDaos.isEmpty()) {
            mKnowledgeDetailDaoList.addAll(knowledgeDaos);
            s14Flashcard_all_adapter.notifyDataSetChanged();
            mTvPosition.setText(String.valueOf("1") + PATH + String.valueOf(mKnowledgeDetailDaoList.size()));
        }
    }

    /**
     * Set title for screen.
     */
    private void setTitleScreen(Lesson lesson) {
        mTvTitle.setText(BreadcrumbUtil.getBreadcrumb(mContext, lesson, getString(R.string.common_module__flashcard)));
    }

    /**
     * Open public method.
     * Method implement Adapter item click.
     */
    public void onImageSoundAdapterClick(int position) {
        if (mContext instanceof S14Flashcard_Activity && position < mKnowledgeDetailDaoList.size()) {
            ((S14Flashcard_Activity) mContext).playAudioWithPath(mKnowledgeDetailDaoList.get(position));
        }
    }

    /**
     * Init View.
     */
    private void initView(View view) {
        mSwipeStack = (SwipeStack) view.findViewById(R.id.mSwipeStack);
        mTvPosition = (AppCompatTextView) view.findViewById(R.id.mTvPosition);
        mTvTitle = (AppCompatTextView) view.findViewById(R.id.mTvTitle);
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

            if (position < mKnowledgeDetailDaoList.size()) {
                ((S14Flashcard_Activity) activity).playAudioWithPath(mKnowledgeDetailDaoList.get(position));
            }
        }
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

        if (userModel == null || getDataLesson2() == null || mKnowledgeDetailDaoList.isEmpty() || position > mKnowledgeDetailDaoList.size()) {
            return null;
        }

        return MediaUtil.getFilePicture(mContext, userModel, getDataLesson2(), mKnowledgeDetailDaoList.get(position).picture_file);
    }

    @Override
    public void onViewSwipedToLeft(final int position) {
        // Dừng audio if đang chạy audio nào trước đó.
        mHandlerAutoPlayAudio.removeMessages(MSG);

        if (position == mKnowledgeDetailDaoList.size() - 1) {
            mTvPosition.setText("");
        } else {
            mTvPosition.setText(String.valueOf(position + 2) + PATH + String.valueOf(mKnowledgeDetailDaoList.size()));
        }
    }

    @Override
    public void onViewSwipedToRight(int position) {
        // Dừng audio if đang chạy audio nào trước đó.
        mHandlerAutoPlayAudio.removeMessages(MSG);

        if (position == mKnowledgeDetailDaoList.size() - 1) {
            mTvPosition.setText("");
        } else {
            mTvPosition.setText(String.valueOf(position + 2) + PATH + String.valueOf(mKnowledgeDetailDaoList.size()));
        }
    }

    @Override
    public void onStackEmpty() {
        mSwipeStack.resetStack();
        mTvPosition.setText("1" + PATH + String.valueOf(mKnowledgeDetailDaoList.size()));
    }

    @Override
    public void onSwipeStart(int position) {
    }

    @Override
    public void onSwipeProgress(int position, float progress) {
    }

    @Override
    public void onSwipeEnd(int position) {
    }
}
