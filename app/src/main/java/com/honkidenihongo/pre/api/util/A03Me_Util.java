package com.honkidenihongo.pre.api.util;

import android.support.annotation.Nullable;

import com.honkidenihongo.pre.api.json.A03Me_JsonData;
import com.honkidenihongo.pre.api.json.A03Me_JsonResult;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * Class tiện ích để parse json data lấy từ API cho toàn ứng dụng.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class A03Me_Util {
    /**
     * The private constructor to prevent creating new object.
     */
    private A03Me_Util() {
    }

    /**
     * Parse để lấy thông tin A03Me_JsonResult từ Json String.
     * //Chú ý: Thời gian từ API Server sẽ được convert thành thời gian theo TimeZone ở client.
     *
     * @param jsonString The json data in String.
     * @return The object after parsing if OK, else return null.
     */
    @Nullable
    private static A03Me_JsonResult parseResult(String jsonString) {
        try {
            // Build Moshi object.
            Moshi moshi = new Moshi.Builder()
                    //.add(new JsonAdapterDate(AppConfig.SERVER_TIME_ZONE))
                    .build();

            // Create json adapter, prepare parsing.
            JsonAdapter<A03Me_JsonResult> jsonAdapter = moshi.adapter(A03Me_JsonResult.class);

            // Parse json data, return the object.
            return jsonAdapter.fromJson(jsonString);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Parse để lấy thông tin A03Me_JsonData từ Json String.<br/>
     * Chỉ trả về trong trường hợp các thuộc tính có dữ liệu hợp lệ.
     *
     * @param jsonString The json data in String.
     * @return The A03Me_JsonData object.
     */
    @Nullable
    public static A03Me_JsonData parseData(String jsonString) {
        // Parse the json result.
        A03Me_JsonResult jsonResult = parseResult(jsonString);

        if (jsonResult != null && jsonResult.is_success != null && jsonResult.is_success) {
            A03Me_JsonData jsonData = jsonResult.data;

            if (jsonData != null
                    && jsonData.id != null
                    && jsonData.id > 0
                    && jsonData.has_password != null
                    && jsonData.facebook_connected != null) {
                // Trả về dữ liệu hợp lệ.
                return jsonData;
            }
        }

        return null;
    }

}
