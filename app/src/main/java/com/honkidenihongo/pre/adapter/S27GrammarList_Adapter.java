package com.honkidenihongo.pre.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.config.FontsConfig;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.gui.grammar.S27GrammarList_Fragment;
import com.honkidenihongo.pre.model.Grammar;

import java.util.List;

/**
 * Class S27GrammarList_Adapter dùng để hiện thị data cho class {@link S27GrammarList_Fragment}.
 *
 * @author binh.dt.
 * @since 10-Nov-2016.
 */
public class S27GrammarList_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private S27GrammarList_Fragment mS27GrammarListFragment;
    private List<Grammar> mGrammars;

    // Biến dùng để xác định item list được chọn mở trước đó.
    private int mItemOldSelected;
    private Context mContext;

    /**
     * Constructor of Class.
     *
     * @param context                Giá trị context của màn hình hiện tại.
     * @param grammars               list danh sách Grammar.
     * @param s27GrammarListFragment Fragment {@link S27GrammarList_Fragment}.
     */
    public S27GrammarList_Adapter(Context context, List<Grammar> grammars, S27GrammarList_Fragment s27GrammarListFragment) {
        mContext = context;
        mGrammars = grammars;
        mS27GrammarListFragment = s27GrammarListFragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.s27_grammar_list_item_list, parent, false);

        final GrammarHolder grammarHolder = new GrammarHolder(view);

        grammarHolder.mTvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Grammar grammarOldSelected = mGrammars.get(mItemOldSelected);

                // Đóng view chi tiết của item list được mở trước đó.
                if (grammarOldSelected.is_the_opend && mItemOldSelected != grammarHolder.getLayoutPosition()) {
                    grammarOldSelected.is_the_opend = false;
                    notifyItemChanged(mItemOldSelected);
                }

                Grammar grammarSelected = mGrammars.get(grammarHolder.getLayoutPosition());

                // Update value data.
                grammarSelected.is_the_opend = !grammarSelected.is_the_opend;

                boolean isOpened = grammarSelected.is_the_opend;

                grammarHolder.mTvContent.setVisibility(isOpened ? View.VISIBLE : View.GONE);

                if (isOpened) {
                    // grammarHolder.starAnimation(); // Remove animation at version current.
                    mItemOldSelected = grammarHolder.getLayoutPosition();
                    mS27GrammarListFragment.moveItemToTop(grammarHolder.getLayoutPosition());
                }
            }
        });

        return grammarHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        GrammarHolder grammarHolder = (GrammarHolder) holder;
        Grammar grammar = mGrammars.get(position);

        // Lấy title and content theo ngôn ngữ.
        String title = "";
        String content = "";

        content = grammar.content_vi;
        title = grammar.title_vi;

        // Todo ở version hiện tại thì nội dung luôn luôn là tiếng việt.
//        if (LocaleHelper.getLanguage(mContext).equals(Definition.LanguageCode.ENGLISH)) {
//             content = grammar.content_en;
//            title = grammar.title_en;
//        } else {
//            content = grammar.content_vi;
//            title = grammar.title_vi;
//        }

        grammarHolder.mTvTitle.setText(title);
        grammarHolder.mTvContent.setText(content);

        boolean isOpened = grammar.is_the_opend;
        grammarHolder.mTvContent.setVisibility(isOpened ? View.VISIBLE : View.GONE);
    }

    /**
     * Open public method using call it inside fragment.
     *
     * @return List GrammarDao.
     */
    public List<Grammar> getData() {
        return mGrammars;
    }

    @Override
    public int getItemCount() {
        return mGrammars == null ? 0 : mGrammars.size();
    }

    /**
     * Class tạo ViewHolder cho list danh sách.
     */
    private static final class GrammarHolder extends RecyclerView.ViewHolder {
        private final AppCompatTextView mTvTitle;
        private final AppCompatTextView mTvContent;
        private final Animation mAnimShow;

        /**
         * Constructor of Class.
         *
         * @param itemView Giá trị ItemView get từ layout truyền vào.
         */
        private GrammarHolder(View itemView) {
            super(itemView);

            // Hiện tại ở version này chưa dùng đến animation để show view.
            mAnimShow = AnimationUtils.loadAnimation(itemView.getContext(), android.R.anim.slide_in_left);
            mTvTitle = (AppCompatTextView) itemView.findViewById(R.id.mTvTitle);
            mTvContent = (AppCompatTextView) itemView.findViewById(R.id.mTvContent);
        }

        /**
         * Chạy hiệu ứn show view.
         */
        private void starAnimation() {
            mTvContent.startAnimation(mAnimShow);
        }
    }
}
