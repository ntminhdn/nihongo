package com.honkidenihongo.pre.common.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.model.UserModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Class call api refresh token.
 *
 * @author binh.dt.
 * @since 20-Feb-2017.
 */
public class TokenUtil {
    private static final String LOG_TAG = TokenUtil.class.getSimpleName();

    /**
     * Method using call api get accessToken new.
     *
     * @param context      Value context of screen current.
     * @param refreshToken Vale refreshToken.
     */
    public static void refreshToken(final Context context, final String refreshToken) {
        String refreshTokenUrl = AppConfig.getApiBaseUrl() + Definition.API.GET_ACCESS_TOKEN;

        Log.d(LOG_TAG, "RefreshTokenURL: " + refreshTokenUrl);
        Log.d(LOG_TAG, "RefreshTokenURL: " + refreshToken);

        OkHttpClient client = NetworkUtil.getDefaultHttpClient(context);

        RequestBody formBody = new FormBody.Builder()
                .add(Definition.Request.PARAM_GRANT_TYPE, Definition.Constants.REFRESH_TOKEN)
                .add(Definition.Request.PARAM_REFRESH_TOKEN, refreshToken)
                .add(Definition.Request.PARAM_CLIENT_ID, AppConfig.getClientId())
                .add(Definition.Request.PARAM_CLIENT_SECRET, AppConfig.getClientSecret())
                .build();

        Request request = new Request.Builder()
                .url(refreshTokenUrl)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(LOG_TAG, "onFailure()");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Chú ý không nên gọi response.body() quá 1 lần tránh crash app.
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    // Read data on the worker thread.
                    handlerRefreshTokenData(context, response.body().string());

                } else {
                    // Khi gọi api này mà lỗi nữa thì thực hiện logout.
                    LogoutUtil.handleLogoutUser(context);
                }
            }
        });
    }

    /**
     * Method handler data from api refresh token.
     *
     * @param context      Value context of screen current.
     * @param responseData Value json data.
     */
    private static void handlerRefreshTokenData(Context context, String responseData) {
        try {
            JSONObject userDataJsonObject = new JSONObject(responseData);

            boolean isSuccess = userDataJsonObject.getBoolean(Definition.Response.SUCCESS);

            if (isSuccess) {
                JSONObject accessTokenJson = userDataJsonObject.getJSONObject(Definition.Response.DATA);

                String accessToken = accessTokenJson.getString(Definition.Response.ACCESS_TOKEN);
                String refreshToken = accessTokenJson.getString(Definition.Response.REFRESH_TOKEN);
                long expires_in = accessTokenJson.getLong(Definition.Response.EXPIRES_IN) * 1000 + System.currentTimeMillis();

//                SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//
//                editor.putString(Definition.SharedPreferencesKey.ACCESS_TOKEN, accessToken);
//                editor.putString(Definition.SharedPreferencesKey.REFRESH_TOKEN, refreshToken);
//                editor.putLong(Definition.SharedPreferencesKey.EXPIRES_IN, expires);
//                editor.apply();

                // Cập nhật các thông tin access token mới cho user login.
                UserModel userModel = LocalAppUtil.getLastLoginUserInfo(context);

                if (userModel != null && userModel.tokenInfo != null) {
                    userModel.tokenInfo.access_token = accessToken;
                    userModel.tokenInfo.refresh_token = refreshToken;
                    userModel.tokenInfo.expires_in = expires_in;

                    // Cập nhật vào data.
                    LocalAppUtil.saveLastLoginUserInfo(context, userModel);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }
}
