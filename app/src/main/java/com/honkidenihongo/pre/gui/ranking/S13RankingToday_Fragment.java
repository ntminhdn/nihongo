package com.honkidenihongo.pre.gui.ranking;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.adapter.S13RankingToday_Adapter;
import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.LocalAppUtil;
import com.honkidenihongo.pre.common.util.NetworkUtil;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.UserModel;
import com.honkidenihongo.pre.model.UserRanking;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Screen display list ranking follow to day.
 *
 * @author binh.dt modify.
 * @since 27-Nov-2016.
 */
public class S13RankingToday_Fragment extends Fragment {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S13RankingToday_Fragment.class.getName();
    private static final String S13_RANKING_LESSON_OBJECT = "S13_RANKING_LESSON_OBJECT";
    private Context mContext;

    // For View.
    private RecyclerView mRecyclerView;
    private RelativeLayout mViewContent;
    private AppCompatTextView mTvNoData;

    /**
     * Method receive object lesson.
     *
     * @param lesson Value.
     */
    public static S13RankingToday_Fragment newInstance(Lesson lesson) {
        S13RankingToday_Fragment fragment = new S13RankingToday_Fragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(S13_RANKING_LESSON_OBJECT, lesson);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.s13_ranking_fragment_today, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Init controls in the layout.
        initView(view);

        // Init data for displaying.
        if (getDataBundle() != null) {
            initData(getDataBundle());
        }
    }

    /**
     * Method using init View.
     */
    private void initView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.mRecyclerView);
        mViewContent = (RelativeLayout) view.findViewById(R.id.mViewContent);
        mTvNoData = (AppCompatTextView) view.findViewById(R.id.mTvNoData);

        mTvNoData.setText(getString(R.string.common_msg__content_info__have_no_data));
        mTvNoData.setVisibility(View.VISIBLE);
        mViewContent.setVisibility(View.GONE);
    }

    /**
     * Method using set data for View.
     */
    private void initData(Lesson lesson) {
        requestAsyncRankinTodayFromServer(lesson);
    }

    /**
     * Lấy data nhận được.
     *
     * @return Value Lesson.
     */
    private Lesson getDataBundle() {
        Bundle arguments = getArguments();

        if (arguments != null) {
            return arguments.getParcelable(S13_RANKING_LESSON_OBJECT);
        }

        return null;
    }

    /**
     * Request to server get ranking today.
     */
    private void requestAsyncRankinTodayFromServer(Lesson lesson) {  /* Bước 1: Bật hộp thoại chờ request lên server. */
        // Cẩn thận luôn khởi tạo mới.
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.common_msg__content_info__processing));
        progressDialog.show();

        // Lấy thông tin user hiện thời vừa đăng nhập để lấy Access-Token hợp lệ.
        UserModel userModel = LocalAppUtil.getLastLoginUserInfo(mContext);
        String accessToken = null;

        if (userModel != null && userModel.tokenInfo != null) {
            accessToken = userModel.tokenInfo.access_token;
        }

        /* Bước 2: Request lên API Server để lấy danh sách Lesson List. */
        // Chuẩn bị dữ kiện để gửi lên Server.
        String authHeader = String.format("%s %s", Definition.Request.HEADER_BEARER2, accessToken);
        Request getRankingWeekRequest = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + String.format(Locale.getDefault(), Definition.API.GET_RANKING_TODAY, lesson.getId()))
                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                .header(Definition.Request.HEADER_AUTHORIZATION, authHeader)
                .header(Definition.Request.PARAM_CLIENT_TOKEN, AppConfig.getClientToken())
                .build();

        // Get OkHttpClient object with default timeout configurations.
        OkHttpClient okHttpClient = NetworkUtil.getDefaultHttpClient(mContext);

        // Tiến hành gửi request bất đồng bộ (asynchronous) lên Server.
        okHttpClient.newCall(getRankingWeekRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressDialog.dismiss();
                mViewContent.setVisibility(View.GONE);
                mTvNoData.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                progressDialog.dismiss();

                if (response.code() == HttpURLConnection.HTTP_OK) {
                    final String responseString = response.body().string();
                    if (!TextUtils.isEmpty(responseString) && !handleRankingData(responseString).isEmpty())
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showData(handleRankingData(responseString));
                            }
                        });
                } else {
                    mViewContent.setVisibility(View.GONE);
                    mTvNoData.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * Show data to list view.
     *
     * @param userRankings List object.
     */
    private void showData(List<UserRanking> userRankings) {
        if (userRankings.isEmpty()) {
            mViewContent.setVisibility(View.GONE);
            mTvNoData.setVisibility(View.VISIBLE);
        } else {
            mViewContent.setVisibility(View.VISIBLE);
            mTvNoData.setVisibility(View.GONE);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setHasFixedSize(true);

            S13RankingToday_Adapter s13RankingToday_adapter = new S13RankingToday_Adapter(mContext, userRankings);
            mRecyclerView.setAdapter(s13RankingToday_adapter);
        }
    }

    /**
     * Method handle ranking.
     *
     * @param rankingData Value String.
     */
    private List<UserRanking> handleRankingData(String rankingData) {
        List<UserRanking> userRankings = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(rankingData);

            if (jsonObject.has(Definition.Response.DATA)) {
                JSONArray list = jsonObject.getJSONArray(Definition.Response.DATA);
                for (int i = 0; i < list.length(); i++) {
                    UserRanking userRanking = new UserRanking();
                    JSONObject json = list.getJSONObject(i);
                    userRanking.setDisplay_name(json.getString("display_name"));
                    userRanking.setPoint(Double.valueOf(json.getString("point")));
                    userRanking.setRanking(json.getString("ranking"));

                    userRankings.add(userRanking);
                }
            }
        } catch (JSONException e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        return userRankings;
    }
}
