package com.honkidenihongo.pre.gui.common;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.common.util.NetworkUtil;
import com.honkidenihongo.pre.common.util.ToolbarUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Screen display content term of service.
 *
 * @author binh.tt.
 * @since 08-Nov-2016.
 */
public class S26TermsOfService_Activity extends AppCompatActivity {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S26TermsOfService_Activity.class.getName();

    // Gía trị nhận biết bạn đến màn hình này từ srceen S02Register hay là S29AboutUs?
    public static final String GO_TO_FROM_SCREEN = "GO_TO_FROM_SCREEN";

    // For view.
    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private AppCompatTextView mTxtContent;
    private AppCompatImageView mImgJapan;
    private AppCompatImageView mImgVietnamese;
    private AppCompatImageView mImgEnglish;
    private AppCompatButton mBtnAgree;
    private AppCompatTextView mTvTitleToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Set layout for window.
        setContentView(R.layout.s26_terms_of_service_activity);

        /**
         * Call method initView.
         */
        initView();

        /**
         * Init toolbar.
         */
        ToolbarUtil.initToolbar(mToolbar, this);

        /**
         * Set data into WebView.
         */
        initData();

        /**
         * Set event for View.
         */
        setEvent();
    }

    /**
     * Phương thức dùng để lắng nghe sự thay đổi ngôn ngữ của app.
     *
     * @param base Value context.
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(menuItem);
    }

    /**
     * Init View on layout.
     */
    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mProgressBar = (ProgressBar) findViewById(R.id.mProgressBar);
        mTxtContent = (AppCompatTextView) findViewById(R.id.mTxtContent);
        mImgJapan = (AppCompatImageView) findViewById(R.id.mImgJapan);
        mImgVietnamese = (AppCompatImageView) findViewById(R.id.mImgVietnamese);
        mImgEnglish = (AppCompatImageView) findViewById(R.id.mImgEnglish);
        mBtnAgree = (AppCompatButton) findViewById(R.id.mBtnAgree);
        mTvTitleToolbar = (AppCompatTextView) mToolbar.findViewById(R.id.mTvTitleToolbar);
        mTvTitleToolbar.setText(getString(R.string.common_app__name));

        // Nếu tôi đến từ màn hình 29AboutUs thì ẩn nút này đi.
        if (!TextUtils.isEmpty(getDataIntent()) && getDataIntent().equals(S29AboutUs_Fragment.class.getName())) {
            mBtnAgree.setVisibility(View.GONE);
        }
    }

    /**
     * Set data into View.
     */
    private void initData() {
        // Kiểm tra kết nối mạng.
        if (NetworkUtil.isAvailable(this)) {
            requestAsyncTermsOfService();
        } else {
            // Lấy thông tin dưới local theo ngôn ngữ để hiện thị.
            getLanguageCurrent();
        }
    }

    /**
     * Get data through Intent.
     */
    private String getDataIntent() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getString(GO_TO_FROM_SCREEN);
        }

        return null;
    }

    /**
     * Display content follow to language.
     */
    private void getLanguageCurrent() {
        if (LocaleHelper.getLanguage(this).equals(Definition.LanguageCode.ENGLISH)) {
            displayEnglish();
        } else {
            displayVietnamese();
        }
    }

    /**
     * Chạy API lấy thông tin mới nhất từ server  kết nối theo cơ chế Asynchronous.
     */
    private void requestAsyncTermsOfService() {
        mProgressBar.setVisibility(View.VISIBLE);

        // Chuẩn bị dữ kiện để gửi lên Server.
        Request termsOfServiceRequest = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + Definition.API.GET_TERMS_OF_SERVICE)
                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                .header(Definition.Request.PARAM_CLIENT_TOKEN, AppConfig.getClientToken())
                .build();

        OkHttpClient httpClient = NetworkUtil.getDefaultHttpClient(this);

        // Tiến hành gửi request bất đồng bộ (asynchronous) lên Server.
        httpClient.newCall(termsOfServiceRequest).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String body = response.body().string();

                if (response.code() == HttpURLConnection.HTTP_OK) {
                    // Hiển thị và save lại dưới local.
                    if (!TextUtils.isEmpty(body)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setVisibility(View.GONE);
                                handleTermsOfServiceData(body);
                            }
                        });
                    }
                } else {
                    //  Lỗi lấy nội dung dưới local hiển thị.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setVisibility(View.GONE);
                            getLanguageCurrent();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // Lỗi lấy nội dung dưới local hiển thị.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                        getLanguageCurrent();
                    }
                });
            }
        });
    }

    /**
     * Paser data of api.
     *
     * @param responseData Value data.
     */
    private void handleTermsOfServiceData(String responseData) {
        try {
            JSONObject termsOfServiceJson = new JSONObject(responseData);
            boolean isSuccess = termsOfServiceJson.getBoolean(Definition.Response.SUCCESS);

            if (isSuccess) {
                JSONObject data = termsOfServiceJson.getJSONObject(Definition.Response.DATA);

                // Lấy thông tin.
                String ja = data.getString(Definition.LanguageCode.JAPANESE);
                String vn = data.getString(Definition.LanguageCode.VIETNAMESE);
                String en = data.getString(Definition.LanguageCode.ENGLISH);

                // Save data.
                SharedPreferences sharedPreferences = this.getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Definition.LanguageCode.JAPANESE, ja);
                editor.putString(Definition.LanguageCode.VIETNAMESE, vn);
                editor.putString(Definition.LanguageCode.ENGLISH, en);
                editor.apply();

                // Hiện thị nội dung theo ngôn ngữ
                getLanguageCurrent();
            }
        } catch (JSONException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
    }

    /**
     * Hiện thị theo ngôn ngữ tiếng Nhật.
     */
    private void displayJapan() {
        mImgJapan.setEnabled(false);
        mImgEnglish.setEnabled(true);
        mImgVietnamese.setEnabled(true);

        mImgVietnamese.setAlpha(1.0f);
        mImgEnglish.setAlpha(1.0f);
        mImgJapan.setAlpha(0.3f);

        // Lấy thông tin hiển thị từ sharedPreferences.
        SharedPreferences sharedPreferences = this.getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        String contentJapan = sharedPreferences.getString(Definition.LanguageCode.JAPANESE, "");
        mTxtContent.setText(contentJapan);
    }

    /**
     * Hiện thị theo ngôn ngữ tiếng anh.
     */
    private void displayEnglish() {
        mImgEnglish.setEnabled(false);
        mImgJapan.setEnabled(true);
        mImgVietnamese.setEnabled(true);

        mImgJapan.setAlpha(1.0f);
        mImgVietnamese.setAlpha(1.0f);
        mImgEnglish.setAlpha(0.3f);

        // Lấy thông tin hiển thị từ sharedPreferences.
        SharedPreferences sharedPreferences = this.getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        String contentEnglish = sharedPreferences.getString(Definition.LanguageCode.ENGLISH, "");
        mTxtContent.setText(contentEnglish);
    }

    /**
     * Hiện thị theo ngôn ngữ tiếng việt.
     */
    private void displayVietnamese() {
        mImgVietnamese.setEnabled(false);
        mImgJapan.setEnabled(true);
        mImgEnglish.setEnabled(true);

        mImgJapan.setAlpha(1.0f);
        mImgEnglish.setAlpha(1.0f);
        mImgVietnamese.setAlpha(0.3f);

        // Lấy thông tin hiển thị từ sharedPreferences.
        SharedPreferences sharedPreferences = this.getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        String contentVietnamese = sharedPreferences.getString(Definition.LanguageCode.VIETNAMESE, "");
        mTxtContent.setText(contentVietnamese);
    }

    /**
     * Set event for view on layout.
     */
    private void setEvent() {
        mBtnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        mImgJapan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayJapan();
            }
        });

        mImgVietnamese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayVietnamese();
            }
        });

        mImgEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayEnglish();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(0, 0);
    }
}
