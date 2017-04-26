package com.honkidenihongo.pre.api;

import com.honkidenihongo.pre.api.json.A25LessonList_JsonResult;
import com.honkidenihongo.pre.api.json.A28LessonById_JsonResult;
import com.honkidenihongo.pre.common.config.AppConfig;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * Class tiện ích để parse json data lấy từ API cho toàn ứng dụng.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class JsonParserApi {
    /**
     * The private constructor to prevent creating new object.
     */
    private JsonParserApi() {
    }

    /**
     * Parse json for API: A25LessonList.
     * Chú ý: Thời gian từ API Server sẽ được convert thành thời gian theo TimeZone ở client.
     *
     * @param jsonData The json data in String.
     * @return The object after parsing if OK, else return null.
     */
    public static A25LessonList_JsonResult parse_A25LessonList(String jsonData) {
        try {
            // Build Moshi object.
            Moshi moshi = new Moshi.Builder()
                    .add(new JsonAdapterDate())
                    .build();

            // Create json adapter, prepare parsing.
            JsonAdapter<A25LessonList_JsonResult> jsonAdapter = moshi.adapter(A25LessonList_JsonResult.class);

            // Parse json data, return the object.
            return jsonAdapter.fromJson(jsonData);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Parse json for API: A28LessonById.
     * Chú ý: Thời gian từ API Server sẽ được convert thành thời gian theo TimeZone ở client.
     *
     * @param jsonData The json data in String.
     * @return The object after parsing if OK, else return null.
     */
    public static A28LessonById_JsonResult parse_A28LessonById(String jsonData) {
        try {
            // Build Moshi object.
            Moshi moshi = new Moshi.Builder()
                    .add(new JsonAdapterDate())
                    .build();

            // Create json adapter, prepare parsing.
            JsonAdapter<A28LessonById_JsonResult> jsonAdapter = moshi.adapter(A28LessonById_JsonResult.class);

            // Parse json data, return the object.
            return jsonAdapter.fromJson(jsonData);
        } catch (Exception ex) {
            return null;
        }
    }
}
