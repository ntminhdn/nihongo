package com.honkidenihongo.pre.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.gui.practice.S11PracticeList_Activity;
import com.honkidenihongo.pre.model.TypeQuestion;
import com.honkidenihongo.pre.model.constant.QuestionType;

import java.util.List;

/**
 * Adapter of class {@link S11PracticeList_Activity}.
 *
 * @author binh.dt.
 * @since 27-Nov-2016.
 */
public class S11PracticeList_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<TypeQuestion> mTypeQuestions;
    private Context mContext;

    /**
     * Constructor of Class.
     *
     * @param context Value of screen current.
     * @param list    List object.
     * @param type    Type value.
     */
    public S11PracticeList_Adapter(Context context, List<TypeQuestion> list, String type) {
        mContext = context;
        mTypeQuestions = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.s11_pratice_list_item, parent, false);

        final PracticeViewHolder practiceViewHolder = new PracticeViewHolder(view);

        practiceViewHolder.mViewContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContext instanceof S11PracticeList_Activity) {
                    ((S11PracticeList_Activity) mContext).onAdapterItemClick(practiceViewHolder.getLayoutPosition());
                }
            }
        });

        return practiceViewHolder;
    }

    /**
     * Method lấy đối tượng typeQuestion from position of View.
     *
     * @param position Value position.
     * @return TypeQuestion.
     */
    public TypeQuestion getItem(int position) {
        if (mTypeQuestions != null && !mTypeQuestions.isEmpty()) {
            return mTypeQuestions.get(position);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        PracticeViewHolder practiceViewHolder = (PracticeViewHolder) holder;
        TypeQuestion typeQuestion = mTypeQuestions.get(position);
        practiceViewHolder.mTvTitle.setText(typeQuestion.getName());

        switch (typeQuestion.getTypeQuestion()) {
            case QuestionType.TEXT_JA_NLANG:
            case QuestionType.TEXT_KANA_ROMAJI:
                practiceViewHolder.mImgIcon.setImageResource(R.drawable.s11_practice_list_ic_text_ja_nlang);

                break;
            case QuestionType.TEXT_NLANG_JA:
            case QuestionType.TEXT_ROMAJI_KANA:
                practiceViewHolder.mImgIcon.setImageResource(R.drawable.s11_practice_list_text_nlang_ja);

                break;
            case QuestionType.VOICE_JA_NLANG:
            case QuestionType.VOICE_KANA_ROMAJI:
                practiceViewHolder.mImgIcon.setImageResource(R.drawable.s11_practice_list_ic_voice_ja_nlang);

                break;

            case QuestionType.VOICE_JA_JA:
            case QuestionType.VOICE_KANA_KANA:
                practiceViewHolder.mImgIcon.setImageResource(R.drawable.s11_practice_list_ic_voice_ja_ja);

                break;
        }
    }

    @Override
    public int getItemCount() {
        return mTypeQuestions == null ? 0 : mTypeQuestions.size();
    }

    /**
     * Class PracticeViewHolder.
     */
    private static class PracticeViewHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout mViewContent;

        // Todo dùng để set icon cho mỗi item later.
        private final AppCompatImageView mImgIcon;
        private final AppCompatTextView mTvTitle;

        /**
         * Constructor of Class.
         *
         * @param itemView View.
         */
        private PracticeViewHolder(View itemView) {
            super(itemView);

            mViewContent = (RelativeLayout) itemView.findViewById(R.id.mViewContent);
            mImgIcon = (AppCompatImageView) itemView.findViewById(R.id.mImgIcon);
            mTvTitle = (AppCompatTextView) itemView.findViewById(R.id.mTvTitle);
        }
    }
}
