package com.honkidenihongo.pre.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.config.FontsConfig;
import com.honkidenihongo.pre.gui.trialtest.S17TrialTestResult_Activity;
import com.honkidenihongo.pre.model.Result;
import com.honkidenihongo.pre.model.constant.QuestionType;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Custom adapter of class {@link com.honkidenihongo.pre.gui.trialtest.S17TrialTestResult_Activity}.
 *
 * @author binh.dt.
 * @since 01-Dec-2016.
 */
public class S17TrialTestResult_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Result> mResults;
    private Context mContext;

    /**
     * Khởi tạo Constructor cho class.
     *
     * @param context  Value context of screen current.
     * @param mResults Mảng danh sách đối tượng.
     */
    public S17TrialTestResult_Adapter(Context context, List<Result> mResults) {
        mContext = context;
        this.mResults = mResults;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.s17_trial_test_result_item, parent, false);
        final S17TrialResultViewHolder s17TrialResultViewHolder = new S17TrialResultViewHolder(view);

        s17TrialResultViewHolder.mViewPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContext instanceof S17TrialTestResult_Activity) {
                    ((S17TrialTestResult_Activity) mContext).gotoScreenS15KnowLedgeList(s17TrialResultViewHolder.getLayoutPosition());
                }
            }
        });

        // Click vào nút chạy âm thanh.
        s17TrialResultViewHolder.mImgSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call play audio.
                ((S17TrialTestResult_Activity) mContext).onImgSoundItemClick(s17TrialResultViewHolder.getLayoutPosition());
            }
        });

        return s17TrialResultViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        S17TrialResultViewHolder s17TrialResultViewHolder = (S17TrialResultViewHolder) holder;
        Result result = mResults.get(position);

        // Kiểm tra câu hỏi thuộc loại âm thanh hay kiểu text.
        int typeQuestion = result.typeQuestion;

        s17TrialResultViewHolder.mTvQuestion.setText(result.getQuestion());
        s17TrialResultViewHolder.mTvAnswer.setText(result.getAnswer());

        Typeface typeface = FontsConfig.getInstance(mContext).getFont(FontsConfig.AppFont.KLEE);

        // Đối với trường hợp loại câu hỏi có nội dung câu hỏi là tiếng Nhật thì set font tiếng Nhật cho nó.
        if (typeface != null && typeQuestion == QuestionType.TEXT_ROMAJI_KANA || typeQuestion == QuestionType.TEXT_JA_NLANG || typeQuestion == QuestionType.TEXT_KANA_ROMAJI) {
            s17TrialResultViewHolder.mTvQuestion.setTypeface(typeface);
        }

        // Tương tự câu lệnh trên cho câu trả lời là tiếng Nhật.
        if (typeface != null && typeQuestion == QuestionType.VOICE_JA_JA || typeQuestion == QuestionType.VOICE_KANA_KANA || typeQuestion == QuestionType.TEXT_NLANG_JA || typeQuestion == QuestionType.TEXT_ROMAJI_KANA) {
            s17TrialResultViewHolder.mTvAnswer.setTypeface(typeface);
        }

        // Để đảm bảo giá trị thời gian hoàn thành mỗi câu hỏi không được vượt quá 3s khi show cho người dùng nên cần phải if else chỗ này.
        if (result.getTime_complete() > Definition.Result.TIME_COMPLETED_ONE_QUESTION_MAX) {
            s17TrialResultViewHolder.mTvTime.setText((String.format(Locale.US, Definition.Result.RESULT_FORMAT_TIME_COMPLETED, Definition.Result.TIME_COMPLETED_ONE_QUESTION_MAX) + Definition.Result.SUFFIX_SECOND));
        } else {
            s17TrialResultViewHolder.mTvTime.setText((String.format(Locale.US, Definition.Result.RESULT_FORMAT_TIME_COMPLETED, result.getTime_complete()) + Definition.Result.SUFFIX_SECOND));
        }

        s17TrialResultViewHolder.mViewContent.setBackgroundColor(ContextCompat.getColor(mContext, result.is_correct() ? R.color.s17_trial_test_result_color_item_correct : R.color.s17_trial_test_result_color_item_fail));

        switch (typeQuestion) {
            case QuestionType.VOICE_JA_NLANG:
            case QuestionType.VOICE_JA_JA:
                s17TrialResultViewHolder.mImgSound.setVisibility(View.VISIBLE);
                s17TrialResultViewHolder.mTvQuestion.setVisibility(View.GONE);

                // Kiểm tra làm mờ image âm thanh.
                if (mContext instanceof S17TrialTestResult_Activity && result.getCategory() != null) {
                    File fileAudio = ((S17TrialTestResult_Activity) mContext).getFileVoice(result.getCategory(), result.getAudio_data());

                    // Kiểm tra file audio có tồn tại hay không if tồn tại thì show , không tồn tại thì làm mờ nó đi và không cho click.
                    if (fileAudio != null && fileAudio.exists()) {
                        s17TrialResultViewHolder.mImgSound.setAlpha(Definition.Graphic.LIMPIDITY);
                        s17TrialResultViewHolder.mImgSound.setEnabled(true);
                    } else {
                        s17TrialResultViewHolder.mImgSound.setAlpha(Definition.Graphic.BLEAR);
                        s17TrialResultViewHolder.mImgSound.setEnabled(false);
                    }
                }

                break;

            default:
                s17TrialResultViewHolder.mImgSound.setVisibility(View.GONE);
                s17TrialResultViewHolder.mTvQuestion.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Lấy đối tượng ở vị trí position.
     *
     * @param position Value.
     * @return Result.
     */
    public Result getItem(int position) {
        if (mResults != null && !mResults.isEmpty()) {
            return mResults.get(position);
        }

        return null;
    }

    @Override
    public int getItemCount() {
        return mResults == null ? 0 : mResults.size();
    }

    /**
     * Class S17TrialResultViewHolder.
     */
    private static final class S17TrialResultViewHolder extends RecyclerView.ViewHolder {
        private final ImageButton mImgSound;
        private final AppCompatTextView mTvQuestion;
        private final AppCompatTextView mTvAnswer;
        private final AppCompatTextView mTvTime;
        private final RelativeLayout mViewContent;
        private final RelativeLayout mViewPress;

        /**
         * Private Constructor.
         *
         * @param itemView View layout.
         */
        private S17TrialResultViewHolder(View itemView) {
            super(itemView);

            mImgSound = (ImageButton) itemView.findViewById(R.id.mImgSound);
            mTvQuestion = (AppCompatTextView) itemView.findViewById(R.id.mTvQuestion);
            mTvAnswer = (AppCompatTextView) itemView.findViewById(R.id.mTvAnswer);
            mTvTime = (AppCompatTextView) itemView.findViewById(R.id.mTvTime);
            mViewContent = (RelativeLayout) itemView.findViewById(R.id.mViewContent);
            mViewPress = (RelativeLayout) itemView.findViewById(R.id.mViewPress);
        }
    }

    // Todo sẽ remove all các biến và method cũ sau khi convert chức năng của class là ok.
//    private final RealmList<Result> mList;
//    private Context mContext;
//
//    public S17TrialTestResult_Adapter(Context context, RealmList<Result> list) {
//        mContext = context;
//        mList = list;
//    }
//
//    @Override
//    public S17TrialResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View lessonItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.s17_trial_test_result_item, parent, false);
//        return new S17TrialResultViewHolder(lessonItemView);
//    }
//
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        onBindResultHolder((S17TrialResultViewHolder) holder, position);
//    }
//
//    private void onBindResultHolder(S17TrialResultViewHolder holder, int position) {
//        if (getItemCount() == 0) {
//            return;
//        }
//        if (position == 0) {
//            holder.mIvResult.setVisibility(View.GONE);
//            holder.mIvSound.setVisibility(View.GONE);
//            holder.mTvTimeComplete.setVisibility(View.GONE);
//            holder.mTvQuestion.setVisibility(View.VISIBLE);
//            holder.mTvQuestion.setText(mContext.getResources().getString(R.string.question));
//            holder.mTvAnswer.setText(mContext.getResources().getString(R.string.answer));
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                holder.mTvQuestion.setTextColor(mContext.getColor(R.color.gray));
//                holder.mTvAnswer.setTextColor(mContext.getColor(R.color.gray));
//            } else {
//                holder.mTvQuestion.setTextColor(mContext.getResources().getColor(R.color.gray));
//                holder.mTvAnswer.setTextColor(mContext.getResources().getColor(R.color.gray));
//            }
//            holder.mTvAnswer.setVisibility(View.GONE);
//            holder.mLine.setVisibility(View.GONE);
//        } else {
//            final Result result = mList.get(position);
//            if (result.is_correct()) {
//                holder.mIvResult.setBackgroundResource(R.drawable.ic_correct);
//            } else {
//                holder.mIvResult.setBackgroundResource(R.drawable.ic_wrong);
//            }
//            holder.mTvAnswer.setText(result.getAnswer());
//            holder.mTvTimeComplete.setText(String.format(Locale.getDefault(), "%.2f", result.getTime_complete()) + "s");
//            if (result.is_audio) {
//                holder.mIvSound.setVisibility(View.VISIBLE);
//                holder.mTvQuestion.setVisibility(View.INVISIBLE);
//                holder.mIvSound.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        try {
//                            JSONObject audioJSONObject = new JSONObject(result.getAudio_data());
//                            Iterator<String> keys = audioJSONObject.keys();
//                            if (keys.hasNext()) {
//                                String audioName = keys.next();
//                                JSONArray array = audioJSONObject.getJSONArray(audioName);
//                                int audioStartAt = (int) (Float.parseFloat(array.getString(0)) * 1000);
//                                int audioDuration = (int) (Float.parseFloat(array.getString(1)) * 1000);
//                                if (mContext instanceof S17TrialTestResult_Activity) {
//                                    ((S17TrialTestResult_Activity) mContext).playAudio(audioName, audioStartAt, audioDuration);
//                                }
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        } catch (NumberFormatException nfe) {
//                            nfe.printStackTrace();
//                        }
//                    }
//                });
//            } else {
//                holder.mIvSound.setVisibility(View.INVISIBLE);
//                holder.mTvQuestion.setVisibility(View.VISIBLE);
//                holder.mTvQuestion.setText(result.getQuestion());
//            }
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return mList == null ? 0 : mList.size();
//    }
//
//    /**
//     * Class ManagerViewHolder
//     */
//    private class S17TrialResultViewHolder extends RecyclerView.ViewHolder {
//        private final ImageView mIvResult;
//        private final ImageView mIvSound;
//        private final TextView mTvQuestion;
//        private final TextView mTvAnswer;
//        private final TextView mTvTimeComplete;
//        private final View mLine;
//
//        S17TrialResultViewHolder(View lessonItemView) {
//            super(lessonItemView);
//            this.mIvResult = (ImageView) lessonItemView.findViewById(R.id.iv_result_icon);
//            this.mIvSound = (ImageView) lessonItemView.findViewById(R.id.iv_sound);
//            this.mTvQuestion = (TextView) lessonItemView.findViewById(R.id.tv_question);
//            this.mTvAnswer = (TextView) lessonItemView.findViewById(R.id.tv_correct_answer);
//            this.mTvTimeComplete = (TextView) lessonItemView.findViewById(R.id.tv_time_complete);
//            this.mLine = lessonItemView.findViewById(R.id.v_line);
//        }
//    }
}
