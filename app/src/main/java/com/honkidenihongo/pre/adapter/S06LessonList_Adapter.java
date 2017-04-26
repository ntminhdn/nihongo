package com.honkidenihongo.pre.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.model.constant.LanguageNumberCode;
import com.honkidenihongo.pre.gui.lesson.S06LessonList_Fragment;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.common.util.LessonNameUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.model.constant.LessonStatus;

import java.util.List;

/**
 * Màn hình hiển thị danh sách lesson đã hoặc chưa download.
 *
 * @author binh.tt.
 * @since 08-Nov-2016.
 */
public class S06LessonList_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private S06LessonList_Fragment mS06LessonListFragment;
    private final List<Lesson> mLessons;
    private Context mContext;

    /**
     * Khởi tạo Constructor cho class.
     *
     * @param context  Value context of screen current.
     * @param lessons  Mảng danh sách đối tượng.
     * @param fragment Fragment hiện tại chứa list.
     */
    public S06LessonList_Adapter(Context context, List<Lesson> lessons, S06LessonList_Fragment fragment) {
        mContext = context;
        mLessons = lessons;
        mS06LessonListFragment = fragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.s06_lesson_list_item, parent, false);
        final LessonViewHolder lessonViewHolder = new LessonViewHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Kiểm tra đối tượng fragment bằng null thì return.
                if (mS06LessonListFragment == null) {
                    return;
                }

                mS06LessonListFragment.onItemLessonsClick(lessonViewHolder.getLayoutPosition());
            }
        });

        lessonViewHolder.imgLessonStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra đối tượng fragment bằng null thì return.
                if (mS06LessonListFragment == null) {
                    return;
                }

                mS06LessonListFragment.onItemUpdateLessonClick(lessonViewHolder.getLayoutPosition());
            }
        });

        return lessonViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Ép kiểu về LessonViewHolder.
       final LessonViewHolder lessonViewHolder = (LessonViewHolder) holder;

        // Lấy đối tượng Lesson dựa trên position.
        Lesson lesson = mLessons.get(position);

        /* Dựa trên dữ liệu của đối tượng Lesson mà set view tương ứng. */
        // Lấy tên bài học dựa theo ngôn ngữ.
        String lessonName = "";
        String lessonTitle = "";

        if (LocaleHelper.getLanguage(mContext).equals(Definition.LanguageCode.ENGLISH)) {
            lessonName = LessonNameUtil.getLessonName(lesson, LanguageNumberCode.ENGLISH);
            lessonTitle = lesson.getTitle_en();
        } else {
            lessonName = LessonNameUtil.getLessonName(lesson, LanguageNumberCode.VIETNAMESE);
            lessonTitle = lesson.getTitle_vi();
        }

        lessonViewHolder.lblLessonName.setText(lessonName);
        lessonViewHolder.lblLessonTitle.setText(lessonTitle);

        // Icon.
        int iconResourceId = R.drawable.s06_lesson_list_item_ic_un_downloaded;
        String lessonStatusString = "";
        int lessonStatus = lesson.status;

        // Clear animation for view.
        lessonViewHolder.imgLessonStatus.clearAnimation();

        switch (lessonStatus) {
            default:
            case LessonStatus.UN_DOWNLOADED:
                lessonViewHolder.lessonItemView.setEnabled(true);

                iconResourceId = R.drawable.s06_lesson_list_item_ic_un_downloaded;
                lessonStatusString = mContext.getString(R.string.s06_lesson_list__lbl_lesson_status__un_downloaded);

                break;
            case LessonStatus.WAITING:
                lessonViewHolder.lessonItemView.setEnabled(false);

                iconResourceId = R.drawable.s06_lesson_list_item_ic_un_downloaded;
                lessonStatusString = mContext.getString(R.string.s06_lesson_list__lbl_lesson_status__waiting_for_downloading);

                break;
            case LessonStatus.DOWNLOADING:
                lessonViewHolder.lessonItemView.setEnabled(false);

                iconResourceId = R.drawable.s06_lesson_list_item_ic_downloading;
                lessonStatusString = mContext.getString(R.string.s06_lesson_list__lbl_lesson_status__downloading);

                break;
            case LessonStatus.DOWNLOAD_ERROR:
                lessonViewHolder.lessonItemView.setEnabled(true);

                iconResourceId = R.drawable.s06_lesson_list_item_ic_un_downloaded;
                lessonStatusString = mContext.getString(R.string.s06_lesson_list__lbl_lesson_status__download_error);

                break;
            case LessonStatus.DOWNLOADED:
                lessonViewHolder.lessonItemView.setEnabled(true);

                iconResourceId = R.drawable.s06_lesson_list_item_ic_downloaded;
                lessonStatusString = mContext.getString(R.string.s06_lesson_list__lbl_lesson_status__downloaded);

                break;
            case LessonStatus.LEARN_COMPLETED:
                lessonViewHolder.lessonItemView.setEnabled(true);

                iconResourceId = R.drawable.s06_lesson_list_item_ic_learn_completed;
                lessonStatusString = mContext.getString(R.string.s06_lesson_list__lbl_lesson_status__learn_completed);

                break;

            case LessonStatus.WAITING_UPDATE:
                lessonViewHolder.lessonItemView.setEnabled(false);

                iconResourceId = R.drawable.s06_lesson_list_item_ic_un_downloaded;
                lessonStatusString = mContext.getString(R.string.s06_lesson_list__lbl_lesson_status__waiting_for_updating);

                break;
            case LessonStatus.UPDATE:
                lessonViewHolder.lessonItemView.setEnabled(true);

                iconResourceId = R.drawable.s06_lesson_list_ic_update;
                lessonStatusString = mContext.getString(R.string.s06_lesson_list__lbl_lesson_status__update);

                break;
            case LessonStatus.UPDATING:
                lessonViewHolder.lessonItemView.setEnabled(false);

                iconResourceId = R.drawable.s06_lesson_list_ic_update;
                lessonStatusString = mContext.getString(R.string.s06_lesson_list__lbl_lesson_status__updating);

                // Set animation for view updating.
                RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(1000);
                rotate.setInterpolator(new LinearInterpolator());

                rotate.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        lessonViewHolder.imgLessonStatus.startAnimation(animation);
                    }
                });

                lessonViewHolder.imgLessonStatus.startAnimation(rotate);

                break;
        }

        lessonViewHolder.imgLessonStatus.setImageResource(iconResourceId);
        lessonViewHolder.lblLessonStatus.setText(lessonStatusString);
    }

    public Lesson getItem(int position) {
        if (mLessons != null && !mLessons.isEmpty()) {
            return mLessons.get(position);
        }

        return null;
    }

    public List<Lesson> getData() {
        return mLessons;
    }

    /**
     * Tính tổng số con lesson đã được tải so với list danh sách hiển thị.
     *
     * @return
     */
    public int countTotalLessonDownloaded() {
        int count = 0;

        if (mLessons == null || mLessons.isEmpty()) {
            return count;
        } else {
            for (Lesson lesson : mLessons) {
                if (lesson.status != LessonStatus.DOWNLOADED) {
                    count++;
                }
            }
        }

        return count;
    }

    @Override
    public int getItemCount() {
        return mLessons == null ? 0 : mLessons.size();
    }

    /**
     * Class LessonViewHolder.
     */
    public static final class LessonViewHolder extends RecyclerView.ViewHolder {
        public View lessonItemView;

        private final AppCompatTextView lblLessonName;
        private final AppCompatTextView lblLessonTitle;
        private final AppCompatImageView imgLessonStatus;
        private final AppCompatTextView lblLessonStatus;

        /**
         * The private constructor.
         *
         * @param itemView The item view.
         */
        private LessonViewHolder(View itemView) {
            super(itemView);

            this.lessonItemView = itemView;

            lblLessonName = (AppCompatTextView) itemView.findViewById(R.id.s06_lesson_list_lbl_lesson_name);
            lblLessonTitle = (AppCompatTextView) itemView.findViewById(R.id.s06_lesson_list_lbl_lesson_title);
            imgLessonStatus = (AppCompatImageView) itemView.findViewById(R.id.s06_lesson_list_item_img_lesson_status);
            lblLessonStatus = (AppCompatTextView) itemView.findViewById(R.id.s06_lesson_list_item_lbl_lesson_status);
        }
    }
}
