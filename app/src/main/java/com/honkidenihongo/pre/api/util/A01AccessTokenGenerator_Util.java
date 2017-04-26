package com.honkidenihongo.pre.api.util;

import android.support.annotation.Nullable;
import android.util.Log;

import com.honkidenihongo.pre.MainApplication;
import com.honkidenihongo.pre.api.json.A01AccessTokenGenerator_JsonData;
import com.honkidenihongo.pre.api.json.A01AccessTokenGenerator_JsonResult;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * Class tiện ích để parse json data lấy từ API: A01AccessTokenGenerator.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class A01AccessTokenGenerator_Util {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = MainApplication.class.getName();

    /**
     * The private constructor to prevent creating new object.
     */
    private A01AccessTokenGenerator_Util() {
    }

    /**
     * Parse để lấy thông tin A01AccessTokenGenerator_JsonResult từ Json String.
     * //Chú ý: Thời gian từ API Server sẽ được convert thành thời gian theo TimeZone ở client.
     *
     * @param jsonString The json data in String.
     * @return The object after parsing if OK, else return null.
     */
    @Nullable
    private static A01AccessTokenGenerator_JsonResult parseResult(String jsonString) {
        try {
            // Build Moshi object.
            Moshi moshi = new Moshi.Builder()
                    //.add(new JsonAdapterDate(AppConfig.SERVER_TIME_ZONE))
                    .build();

            // Create json adapter, prepare parsing.
            JsonAdapter<A01AccessTokenGenerator_JsonResult> jsonAdapter = moshi.adapter(A01AccessTokenGenerator_JsonResult.class);

            // Parse json data, return the object.
            return jsonAdapter.fromJson(jsonString);
        } catch (Exception ex) {
            // Show the log in development environment.
            Log.e(LOG_TAG, "parseResult(): " + ex.getMessage());

            return null;
        }
    }

    /**
     * Parse để lấy thông tin A01AccessTokenGenerator_JsonData từ Json String.<br/>
     * Chỉ trả về trong trường hợp các thuộc tính có dữ liệu hợp lệ.
     *
     * @param jsonString The json data in String.
     * @return The A01AccessTokenGenerator_JsonData object.
     */
    @Nullable
    public static A01AccessTokenGenerator_JsonData parseData(String jsonString) {
        // Parse the json result.
        A01AccessTokenGenerator_JsonResult jsonResult = parseResult(jsonString);

        if (jsonResult != null && jsonResult.is_success != null && jsonResult.is_success) {
            A01AccessTokenGenerator_JsonData jsonData = jsonResult.data;

            if (jsonData != null
                    && jsonData.access_token != null
                    && jsonData.refresh_token != null
                    && jsonData.expires_in != null
                    && jsonData.expires_in > 0) {
                // Trả về dữ liệu hợp lệ.
                return jsonData;
            }
        }

        return null;
    }
}
