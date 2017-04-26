package com.honkidenihongo.pre.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.model.UserRanking;

import java.util.List;
import java.util.Locale;

/**
 * Adapter of class {@link com.honkidenihongo.pre.gui.ranking.S13RankingToday_Fragment}.
 *
 * @author binh.dt.
 * @since 27-Nov-2016.
 */
public class S13RankingToday_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<UserRanking> mListUserRankings;
    private Context mContext;

    /**
     * Constructor of class.
     *
     * @param mContext          Value of screen current.
     * @param mListUserRankings List data.
     */
    public S13RankingToday_Adapter(Context mContext, List<UserRanking> mListUserRankings) {
        this.mListUserRankings = mListUserRankings;
        this.mContext = mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new S13RankingTodayViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.s13_ranking_today_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        S13RankingTodayViewHolder s13RankingTodayViewHolder = (S13RankingTodayViewHolder) holder;

        UserRanking userRanking = mListUserRankings.get(position);

        s13RankingTodayViewHolder.mTvNameUser.setText(userRanking.getDisplay_name());
        s13RankingTodayViewHolder.mTvPosition.setText(String.valueOf(position + 1));
        s13RankingTodayViewHolder.mTvScore.setText(String.format(Locale.US, Definition.Result.RESULT_FORMAT_TIME_POINT, userRanking.getPoint()));

        switch (position) {
            case 0:
                s13RankingTodayViewHolder.mImgPlace.setImageResource(R.drawable.s13_ranking_one);
                s13RankingTodayViewHolder.mTvScore.setTextColor(ContextCompat.getColor(mContext, R.color.s13_ranking_color_item_list_one));

                break;
            case 1:
                s13RankingTodayViewHolder.mImgPlace.setImageResource(R.drawable.s13_ranking_two);

                break;
            case 2:
                s13RankingTodayViewHolder.mImgPlace.setImageResource(R.drawable.s13_ranking_three);

                break;

            default:

                s13RankingTodayViewHolder.mImgPlace.setImageResource(0);

                break;
        }
    }

    @Override
    public int getItemCount() {
        return mListUserRankings == null ? 0 : mListUserRankings.size();
    }

    /**
     * Class S13RankingWeekViewHolder.
     */
    private static final class S13RankingTodayViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageView mImgPlace;
        private final AppCompatTextView mTvPosition;
        private final AppCompatTextView mTvNameUser;
        private final AppCompatTextView mTvScore;

        /**
         * Private Constructor.
         *
         * @param itemView View layout.
         */
        private S13RankingTodayViewHolder(View itemView) {
            super(itemView);

            mImgPlace = (AppCompatImageView) itemView.findViewById(R.id.mImgPlace);
            mTvPosition = (AppCompatTextView) itemView.findViewById(R.id.mTvPosition);
            mTvNameUser = (AppCompatTextView) itemView.findViewById(R.id.mTvNameUser);
            mTvScore = (AppCompatTextView) itemView.findViewById(R.id.mTvScore);
        }
    }
}
