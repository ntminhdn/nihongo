package com.honkidenihongo.pre.common.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.ConditionVariable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.gui.auth.S01Login_Activity;
import com.honkidenihongo.pre.model.UserModel;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Util class related to the Network.
 *
 * @author long.tt.
 * @since 17-Nov-2016.
 */
public class NetworkUtil {
    /**
     * The network connection timeout: 30 seconds.
     */
    private static final long CONNECT_TIMEOUT = 2;

    /**
     * The network read timeout: 30 seconds.
     */
    private static final long READ_TIMEOUT = 30;

    /**
     * The network write timeout: 30 seconds.
     */
    private static final long WRITE_TIMEOUT = 30;

    /**
     * The private constructor to prevent creating object.
     */
    private NetworkUtil() {
    }

    public static boolean isAvailable(Context context) {
        // Init default return result.
        boolean connected = false;

        // Get ConnectivityManager object.
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connManager != null) {
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

            if (networkInfo != null) {
                connected = networkInfo.isConnected();
            }
        }

        return connected;
    }

    /**
     * Get OkHttpClient object with default timeout configurations.
     *
     * @return The OkHttpClient object.
     */
    public static OkHttpClient getDefaultHttpClient(Context context) {
        // Create OkHttpClient Builder.
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);

        // Todo có thể dùng 1 trong 2 dòng lệnh phía dưới để check refresh token nên dùng dòng lệnh thứ 2 tránh lỗi trường hợp dialog dismiss khi restart app.
//        clientBuilder.authenticator(new TokenAuthenticator(context));
        clientBuilder.addInterceptor(new SignedRequestInterceptor(context));

        // Return OkHttpClient object.
        return clientBuilder.build();
    }

    /**
     * Reference: http://stackoverflow.com/questions/31021725/android-okhttp-refresh-expired-token.
     * This class has two tasks:
     * 1) sign requests with the auth token, when available
     * 2) try to refresh a new token
     */
    private static class SignedRequestInterceptor implements Interceptor {
        private Context mContext;

        /**
         * Constructor of class.
         *
         * @param context Value context of screen current.
         */
        private SignedRequestInterceptor(Context context) {
            this.mContext = context;
        }

        // These two static variables serve for the pattern to refresh a token.
        private final static ConditionVariable LOCK = new ConditionVariable(true);
        private static final AtomicBoolean mIsRefreshing = new AtomicBoolean(false);

        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            // 1. Sign this request.
            Request request = chain.request();

            // 2. Proceed with the request.
            Response response = chain.proceed(request);

            // 3. Check the response: have we got a 401?
            if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {

                UserModel userModel = LocalAppUtil.getLastLoginUserInfo(mContext);

                if (userModel != null && userModel.tokenInfo != null) {
                /*
                *  Because we send out multiple HTTP requests in parallel, they might all list a 401 at the same time.
                *  Only one of them should refresh the token, because otherwise we'd refresh the same token multiple times
                *  and that is bad. Therefore we have these two static objects, a ConditionVariable and a boolean. The
                *  first thread that gets here closes the ConditionVariable and changes the boolean flag.
                */
                    if (mIsRefreshing.compareAndSet(false, true)) {
                        LOCK.close();

                        // We're the first here. let's refresh this token.
                        // it looks like our token isn't valid anymore.

                        // Do we have an access token to refresh?

                        String refreshToken = userModel.tokenInfo.refresh_token;

                        if (!TextUtils.isEmpty(refreshToken)) {
                            TokenUtil.refreshToken(mContext, refreshToken);
                        }

                        LOCK.open();

                        mIsRefreshing.set(false);

                    } else {
                        // Another thread is refreshing the token for us, let's wait for it.
                        boolean conditionOpened = LOCK.block(READ_TIMEOUT);

                        // If the next check is false, it means that the timeout expired, that is - the refresh
                        // stuff has failed. The thread in charge of refreshing the token has taken care of
                        // Redirecting the user to the login activity.
                        if (conditionOpened) {

                            // Another thread has refreshed this for us! thanks!

                            // Sign the request with the new token and proceed.

                            // Return the outcome of the newly signed request.

                            UserModel userModelNew = LocalAppUtil.getLastLoginUserInfo(mContext);

                            if (userModelNew != null && userModelNew.tokenInfo != null) {
                                String authHeader = String.format("%s %s", Definition.Request.HEADER_BEARER2, userModelNew.tokenInfo.access_token);

                                Request newRequest = chain.request().newBuilder()
                                        .header(Definition.Request.HEADER_AUTHORIZATION, authHeader)
                                        .build();

                                response = chain.proceed(newRequest);
                            }
                        }
                    }
                }
            }

//            // Check if still unauthorized (i.e. refresh failed).
//            if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
//                // Clean your access token and prompt user for login again.
//                LogoutUtil.handleLogoutUser(mContext);
//            }

            // Returning the response to the original request.
            return response;
        }
    }
}
