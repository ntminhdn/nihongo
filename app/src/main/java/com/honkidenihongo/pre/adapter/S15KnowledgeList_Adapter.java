package com.honkidenihongo.pre.adapter;

import android.content.Context;
import android.graphics.Typeface;
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
import com.honkidenihongo.pre.dac.dao.KnowledgeDao;
import com.honkidenihongo.pre.model.constant.Category;
import com.honkidenihongo.pre.gui.knowledge.S15KnowledgeList_Activity;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.common.util.LocaleHelper;

import java.io.File;
import java.util.List;

/**
 * Adapter của màn hình {@link S15KnowledgeList_Activity}.
 *
 * @author binh.tt.
 * @since 08-Nov-2016.
 */
public class S15KnowledgeList_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<KnowledgeDao> mKnowledgeDetailDaos;
    private Lesson mLesson;
    private Context mContext;

    /**
     * Constructor of Class.
     *
     * @param context       value context của màn hình hiện tại.
     * @param knowledgeList list đối tượng truyền vào.
     */
    public S15KnowledgeList_Adapter(Context context, Lesson lesson, List<KnowledgeDao> knowledgeList) {
        mContext = context;
        mLesson = lesson;
        mKnowledgeDetailDaos = knowledgeList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.s15_knowlege_list_item_list, parent, false);
        final KnowledgeViewHolder knowledgeViewHolder = new KnowledgeViewHolder(view);

        knowledgeViewHolder.mRlKnowledgeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((S15KnowledgeList_Activity) mContext).showDetailKnowledge(knowledgeViewHolder.getLayoutPosition());
            }
        });

        knowledgeViewHolder.mIbSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((S15KnowledgeList_Activity) mContext).playAudioWithPath(knowledgeViewHolder.getLayoutPosition());
            }
        });

        return knowledgeViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        KnowledgeViewHolder knowledgeViewHolder = (KnowledgeViewHolder) holder;
        KnowledgeDao knowledge = mKnowledgeDetailDaos.get(position);
        knowledgeViewHolder.mTvSubjectRomaji.setText(knowledge.subject_kana);

        // Nếu đó là bài mở đầu thì hiện thị theo cấu trúc sau.
        if (mLesson.getCategory() == Category.PRE_HIRAGANA || mLesson.getCategory() == Category.PRE_KATAKANA) {
            knowledgeViewHolder.mTvSubjectRomaji.setText(mKnowledgeDetailDaos.get(position).subject_kana);
            knowledgeViewHolder.mTvSubjectKana.setText(String.format("%s%s", ": ", mKnowledgeDetailDaos.get(position).subject_romaji));
            knowledgeViewHolder.mTvSubjectKana.setVisibility(View.VISIBLE);
        }

        Typeface typeface = FontsConfig.getInstance(mContext).getFont(FontsConfig.AppFont.KLEE);

        if (typeface != null) {
            knowledgeViewHolder.mTvSubjectRomaji.setTypeface(typeface);
        }

        // Todo hiện tại nội dung bài học tất cả đều lấy tiếng việt.
        // Kiểm tra ngôn ngữ hiện thị lên text mTvVietnamese.
        String meaning = "";
//        if (LocaleHelper.getLanguage(mContext).equals(Definition.LanguageCode.ENGLISH)) {
//            meaning = mKnowledgeDetailDaos.get(position).meaning_en;
//        } else {
//            meaning = mKnowledgeDetailDaos.get(position).meaning_vi;
//        }

        meaning = mKnowledgeDetailDaos.get(position).meaning_vi;

        knowledgeViewHolder.mTvVietnamese.setText(meaning);

        knowledgeViewHolder.mTvVietnamese.setVisibility(mLesson.getCategory() == Category.PRE_HIRAGANA || mLesson.getCategory() == Category.PRE_KATAKANA ? View.INVISIBLE : View.VISIBLE);

        // Kiểm tra làm mờ image âm thanh.
        if (mContext instanceof S15KnowledgeList_Activity) {
            File fileAudio = ((S15KnowledgeList_Activity) mContext).getFileVoice(mKnowledgeDetailDaos.get(position));

            // Kiểm tra file audio có tồn tại hay không if tồn tại thì show , không tồn tại thì làm mờ nó đi và không cho click.
            if (fileAudio != null && fileAudio.exists()) {
                knowledgeViewHolder.mIbSound.setAlpha(Definition.Graphic.LIMPIDITY);
                knowledgeViewHolder.mIbSound.setEnabled(true);
            } else {
                knowledgeViewHolder.mIbSound.setAlpha(Definition.Graphic.BLEAR);
                knowledgeViewHolder.mIbSound.setEnabled(false);
            }
        }
    }

    /**
     * Open public method.
     * Lấy đối tượng ở vị trí position.
     *
     * @param position Value.
     * @return KnowledgeDao.
     */
    public KnowledgeDao getItem(int position) {
        if (mKnowledgeDetailDaos != null && !mKnowledgeDetailDaos.isEmpty()) {
            return mKnowledgeDetailDaos.get(position);
        }

        return null;
    }

    @Override
    public int getItemCount() {
        return mKnowledgeDetailDaos == null ? 0 : mKnowledgeDetailDaos.size();
    }

    /**
     * Class KnowledgeViewHolder.
     */
    private static final class KnowledgeViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageButton mIbSound;
        private final AppCompatTextView mTvSubjectRomaji;
        private final AppCompatTextView mTvSubjectKana;
        private final AppCompatTextView mTvVietnamese;
        private final RelativeLayout mRlKnowledgeItem;

        /**
         * Private Constructor.
         *
         * @param itemView View layout.
         */
        private KnowledgeViewHolder(View itemView) {
            super(itemView);

            mIbSound = (AppCompatImageButton) itemView.findViewById(R.id.ib_sound);
            mTvSubjectRomaji = (AppCompatTextView) itemView.findViewById(R.id.tv_subject_romaji);
            mTvVietnamese = (AppCompatTextView) itemView.findViewById(R.id.tv_vietnamese);
            mRlKnowledgeItem = (RelativeLayout) itemView.findViewById(R.id.rl_knowledge_item);
            mTvSubjectKana = (AppCompatTextView) itemView.findViewById(R.id.tv_subject_kana);
        }
    }
}
