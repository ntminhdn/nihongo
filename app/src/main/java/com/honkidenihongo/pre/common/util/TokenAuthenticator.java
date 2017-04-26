package com.honkidenihongo.pre.common.util;

import android.content.Context;

import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.model.UserModel;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Class using refresh token.
 *
 * @author BinhDT.
 */
public class TokenAuthenticator implements Authenticator {
    private static final String LOG_TAG = TokenAuthenticator.class.getName();
    private Context mContext;

    /**
     * Constructor of class.
     *
     * @param context Value context of Screen current.
     */
    public TokenAuthenticator(Context context) {
        mContext = context;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            // Refresh your access_token using a synchronous api request.
            UserModel userModel = LocalAppUtil.getLastLoginUserInfo(mContext);

            if (userModel != null && userModel.tokenInfo != null) {
                String refreshToken = userModel.tokenInfo.refresh_token;

                TokenUtil.refreshToken(mContext, refreshToken);

                UserModel userModelNew = LocalAppUtil.getLastLoginUserInfo(mContext);

                if (userModelNew != null && userModelNew.tokenInfo != null) {
                    String authHeader = String.format("%s %s", Definition.Request.HEADER_BEARER2, userModelNew.tokenInfo.access_token);

                    // Add new header to rejected request and retry it.
                    return response.request().newBuilder()
                            .header(Definition.Request.HEADER_AUTHORIZATION, authHeader)
                            .build();
                }
            }
        }

        return null;
    }
}
