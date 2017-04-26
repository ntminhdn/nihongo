package com.honkidenihongo.pre.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.config.FontsConfig;
import com.honkidenihongo.pre.common.util.MathUtil;
import com.honkidenihongo.pre.gui.practice.S22PracticeResult_Activity;
import com.honkidenihongo.pre.model.constant.QuestionType;
import com.honkidenihongo.pre.model.Result;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình hiển thị danh sách kết quả câu trả lời.
 *
 * @author binh.dt.
 * @since 27-Nov-2016.
 */
public class S22PracticeResult_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Result> mResults;
    private Context mContext;
    private int mTypeQuestion;

    /**
     * Khởi tạo Constructor cho class.
     *
     * @param context      Value context of screen current.
     * @param mResults     Mảng danh sách đối tượng.
     * @param typeQuestion Loại câu hỏi là âm thanh hay text.
     */
    public S22PracticeResult_Adapter(Context context, List<Result> mResults, int typeQuestion) {
        mContext = context;
        this.mResults = mResults;
        mTypeQuestion = typeQuestion;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.s22_practice_result_item_list, parent, false);
        final ResultViewHolder resultViewHolder = new ResultViewHolder(view);

        resultViewHolder.mViewPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContext instanceof S22PracticeResult_Activity) {
                    // Call method on item click on Activity S22PracticeResult_Activity.
                    ((S22PracticeResult_Activity) mContext).onAdapterItemClick(resultViewHolder.getLayoutPosition());
                }
            }
        });

        // Click vào nút chạy âm thanh.
        resultViewHolder.mImgSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call play audio.
                ((S22PracticeResult_Activity) mContext).onImgSoundItemClick(resultViewHolder.getLayoutPosition());
            }
        });

        return resultViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ResultViewHolder resultViewHolder = (ResultViewHolder) holder;
        Result result = mResults.get(position);

        Typeface typeface = FontsConfig.getInstance(mContext).getFont(FontsConfig.AppFont.KLEE);

        resultViewHolder.mTvQuestion.setText(result.getQuestion());
        resultViewHolder.mTvAnswer.setText(result.getAnswer());

        // Đối với trường hợp loại câu hỏi có nội dung câu hỏi là tiếng Nhật thì set font tiếng Nhật cho nó.
        if (typeface != null && mTypeQuestion == QuestionType.TEXT_ROMAJI_KANA || mTypeQuestion == QuestionType.TEXT_JA_NLANG || mTypeQuestion == QuestionType.TEXT_KANA_ROMAJI) {
            resultViewHolder.mTvQuestion.setTypeface(typeface);
        }

        // Tương tự câu lệnh trên cho câu trả lời là tiếng Nhật.
        if (typeface != null && mTypeQuestion == QuestionType.VOICE_JA_JA || mTypeQuestion == QuestionType.VOICE_KANA_KANA || mTypeQuestion == QuestionType.TEXT_NLANG_JA || mTypeQuestion == QuestionType.TEXT_ROMAJI_KANA) {
            resultViewHolder.mTvAnswer.setTypeface(typeface);
        }

        // Sử dụng string format.
        resultViewHolder.mTvTime.setText(roundToTimeForDisplaying(result.getTime_complete()));
        resultViewHolder.mViewContent.setBackgroundColor(ContextCompat.getColor(mContext, result.is_correct() ? R.color.s22_practice_result_color_item_correct : R.color.s22_practice_result_color_item_fail));

        switch (mTypeQuestion) {
            case QuestionType.VOICE_JA_NLANG:
            case QuestionType.VOICE_JA_JA:
                resultViewHolder.mImgSound.setVisibility(View.VISIBLE);
                resultViewHolder.mTvQuestion.setVisibility(View.GONE);

                // Kiểm tra làm mờ image âm thanh.
                if (mContext instanceof S22PracticeResult_Activity && result.getCategory() != null) {
                    File fileAudio = ((S22PracticeResult_Activity) mContext).getFileVoice(result.getCategory(), result.getAudio_data());

                    // Kiểm tra file audio có tồn tại hay không if tồn tại thì show , không tồn tại thì làm mờ nó đi và không cho click.
                    if (fileAudio != null && fileAudio.exists()) {
                        resultViewHolder.mImgSound.setAlpha(Definition.Graphic.LIMPIDITY);
                        resultViewHolder.mImgSound.setEnabled(true);
                    } else {
                        resultViewHolder.mImgSound.setAlpha(Definition.Graphic.BLEAR);
                        resultViewHolder.mImgSound.setEnabled(false);
                    }
                }

                break;

            default:
                resultViewHolder.mImgSound.setVisibility(View.GONE);
                resultViewHolder.mTvQuestion.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Tính thời gian hoàn thành câu trả lời của user theo đơn vị giây, phút, giờ, ngày.
     *
     * @param seconds Thời gian hoàn thành một câu hỏi của user tính theo đơn vị giây.
     * @return Giá trị hiện thị theo nội dung ngày, giờ, phút, giây.
     */
    private String roundToTimeForDisplaying(double seconds) {
        double minutes = MathUtil.round((seconds / 60), 2);
        double hours = MathUtil.round((minutes / 60), 2);
        double days = MathUtil.round((hours / 24), 2);

        String friendly = "";

        if (days >= 1) {
            friendly = String.format(Locale.US, Definition.Result.RESULT_FORMAT_TIME_COMPLETED, days) + Definition.Result.SUFFIX_DAY;
        } else if (hours >= 1) {
            friendly = String.format(Locale.US, Definition.Result.RESULT_FORMAT_TIME_COMPLETED, hours) + Definition.Result.SUFFIX_HOURS;
        } else {
            if (minutes >= 1) {
                friendly = String.format(Locale.US, Definition.Result.RESULT_FORMAT_TIME_COMPLETED, minutes) + Definition.Result.SUFFIX_MINUTE;
            } else {
                friendly = String.format(Locale.US, Definition.Result.RESULT_FORMAT_TIME_COMPLETED, seconds) + Definition.Result.SUFFIX_SECOND;
            }
        }

        return friendly;
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
     * Class ResultViewHolder.
     */
    private static final class ResultViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageButton mImgSound;
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
        private ResultViewHolder(View itemView) {
            super(itemView);

            mImgSound = (AppCompatImageButton) itemView.findViewById(R.id.mImgSound);
            mTvQuestion = (AppCompatTextView) itemView.findViewById(R.id.mTvQuestion);
            mTvAnswer = (AppCompatTextView) itemView.findViewById(R.id.mTvAnswer);
            mTvTime = (AppCompatTextView) itemView.findViewById(R.id.mTvTime);
            mViewContent = (RelativeLayout) itemView.findViewById(R.id.mViewContent);
            mViewPress = (RelativeLayout) itemView.findViewById(R.id.mViewPress);
        }
    }
}
