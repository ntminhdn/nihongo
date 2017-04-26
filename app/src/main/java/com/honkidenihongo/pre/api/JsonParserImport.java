package com.honkidenihongo.pre.api;

import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.dac.dao.ChoiceDao;
import com.honkidenihongo.pre.dac.dao.GrammarDao;
import com.honkidenihongo.pre.dac.dao.KnowledgeDao;
import com.honkidenihongo.pre.dac.dao.KnowledgeDetailDao;
import com.honkidenihongo.pre.dac.dao.LessonDao;
import com.honkidenihongo.pre.dac.dao.PracticeDao;
import com.honkidenihongo.pre.dac.dao.PracticeDetailDao;
import com.honkidenihongo.pre.dac.dao.PracticeDetailQuestionDao;
import com.honkidenihongo.pre.dac.dao.QuestionDao;
import com.honkidenihongo.pre.dac.dao.TestDao;
import com.honkidenihongo.pre.dac.dao.TestQuestionDao;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.util.List;

/**
 * Class tiện ích dùng trong quá trình Import file json vào Database: parse nội dung json thành kiểu đối tượng.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class JsonParserImport {
    /**
     * The private constructor to prevent creating new object.
     */
    private JsonParserImport() {
    }

    /**
     * Parse choices.json file.
     * Chú ý: Thời gian từ API Server sẽ được convert thành thời gian theo TimeZone ở client.
     *
     * @param jsonData The json data in String.
     * @return The object after parsing if OK, else return null.
     */
    public static List<ChoiceDao> parse_Choices(String jsonData) {
        try {
            // Build Moshi object.
            Moshi moshi = new Moshi.Builder()
                    .add(new JsonAdapterDate())
                    .build();

            // Create json adapter, prepare parsing.
            JsonAdapter<List<ChoiceDao>> jsonAdapter = moshi.adapter(Types.newParameterizedType(List.class, ChoiceDao.class));

            // Parse json data, return the object.
            return jsonAdapter.fromJson(jsonData);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Parse knowledge_details.json file.
     * Chú ý: Thời gian từ API Server sẽ được convert thành thời gian theo TimeZone ở client.
     *
     * @param jsonData The json data in String.
     * @return The object after parsing if OK, else return null.
     */
    public static List<KnowledgeDetailDao> parse_KnowledgeDetails(String jsonData) {
        try {
            // Build Moshi object.
            Moshi moshi = new Moshi.Builder()
                    .add(new JsonAdapterDate())
                    .build();

            // Create json adapter, prepare parsing.
            JsonAdapter<List<KnowledgeDetailDao>> jsonAdapter = moshi.adapter(Types.newParameterizedType(List.class, KnowledgeDetailDao.class));

            // Parse json data, return the object.
            return jsonAdapter.fromJson(jsonData);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Parse knowledges.json file.
     * Chú ý: Thời gian từ API Server sẽ được convert thành thời gian theo TimeZone ở client.
     *
     * @param jsonData The json data in String.
     * @return The object after parsing if OK, else return null.
     */
    public static List<KnowledgeDao> parse_Knowledges(String jsonData) {
        try {
            // Build Moshi object.
            Moshi moshi = new Moshi.Builder()
                    .add(new JsonAdapterDate())
                    .build();

            // Create json adapter, prepare parsing.
            JsonAdapter<List<KnowledgeDao>> jsonAdapter = moshi.adapter(Types.newParameterizedType(List.class, KnowledgeDao.class));

            // Parse json data, return the object.
            return jsonAdapter.fromJson(jsonData);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Parse lesson.json file.
     * Chú ý: Thời gian từ API Server sẽ được convert thành thời gian theo TimeZone ở client.
     *
     * @param jsonData The json data in String.
     * @return The object after parsing if OK, else return null.
     */
    public static LessonDao parse_Lesson(String jsonData) {
        try {
            // Build Moshi object.
            Moshi moshi = new Moshi.Builder()
                    .add(new JsonAdapterDate())
                    .build();

            // Create json adapter, prepare parsing.
            JsonAdapter<LessonDao> jsonAdapter = moshi.adapter(LessonDao.class);

            // Parse json data, return the object.
            return jsonAdapter.fromJson(jsonData);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Parse practice_detail_questions.json file.
     * Chú ý: Thời gian từ API Server sẽ được convert thành thời gian theo TimeZone ở client.
     *
     * @param jsonData The json data in String.
     * @return The object after parsing if OK, else return null.
     */
    public static List<PracticeDetailQuestionDao> parse_PracticeDetailQuestions(String jsonData) {
        try {
            // Build Moshi object.
            Moshi moshi = new Moshi.Builder()
                    .add(new JsonAdapterDate())
                    .build();

            // Create json adapter, prepare parsing.
            JsonAdapter<List<PracticeDetailQuestionDao>> jsonAdapter = moshi.adapter(Types.newParameterizedType(List.class, PracticeDetailQuestionDao.class));

            // Parse json data, return the object.
            return jsonAdapter.fromJson(jsonData);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Parse practice_details.json file.
     * Chú ý: Thời gian từ API Server sẽ được convert thành thời gian theo TimeZone ở client.
     *
     * @param jsonData The json data in String.
     * @return The object after parsing if OK, else return null.
     */
    public static List<PracticeDetailDao> parse_PracticeDetails(String jsonData) {
        try {
            // Build Moshi object.
            Moshi moshi = new Moshi.Builder()
                    .add(new JsonAdapterDate())
                    .build();

            // Create json adapter, prepare parsing.
            JsonAdapter<List<PracticeDetailDao>> jsonAdapter = moshi.adapter(Types.newParameterizedType(List.class, PracticeDetailDao.class));

            // Parse json data, return the object.
            return jsonAdapter.fromJson(jsonData);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Parse practices.json file.
     * Chú ý: Thời gian từ API Server sẽ được convert thành thời gian theo TimeZone ở client.
     *
     * @param jsonData The json data in String.
     * @return The object after parsing if OK, else return null.
     */
    public static List<PracticeDao> parse_Practices(String jsonData) {
        try {
            // Build Moshi object.
            Moshi moshi = new Moshi.Builder()
                    .add(new JsonAdapterDate())
                    .build();

            // Create json adapter, prepare parsing.
            JsonAdapter<List<PracticeDao>> jsonAdapter = moshi.adapter(Types.newParameterizedType(List.class, PracticeDao.class));

            // Parse json data, return the object.
            return jsonAdapter.fromJson(jsonData);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Parse questions.json file.
     * Chú ý: Thời gian từ API Server sẽ được convert thành thời gian theo TimeZone ở client.
     *
     * @param jsonData The json data in String.
     * @return The object after parsing if OK, else return null.
     */
    public static List<QuestionDao> parse_Questions(String jsonData) {
        try {
            // Build Moshi object.
            Moshi moshi = new Moshi.Builder()
                    .add(new JsonAdapterDate())
                    .build();

            // Create json adapter, prepare parsing.
            JsonAdapter<List<QuestionDao>> jsonAdapter = moshi.adapter(Types.newParameterizedType(List.class, QuestionDao.class));

            // Parse json data, return the object.
            return jsonAdapter.fromJson(jsonData);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Parse tests.json file.
     * Chú ý: Thời gian từ API Server sẽ được convert thành thời gian theo TimeZone ở client.
     *
     * @param jsonData The json data in String.
     * @return The object after parsing if OK, else return null.
     */
    public static List<TestDao> parse_Tests(String jsonData) {
        try {
            // Build Moshi object.
            Moshi moshi = new Moshi.Builder()
                    .add(new JsonAdapterDate())
                    .build();

            // Create json adapter, prepare parsing.
            JsonAdapter<List<TestDao>> jsonAdapter = moshi.adapter(Types.newParameterizedType(List.class, TestDao.class));

            // Parse json data, return the object.
            return jsonAdapter.fromJson(jsonData);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Parse tests_questions.json file.
     * Chú ý: Thời gian từ API Server sẽ được convert thành thời gian theo TimeZone ở client.
     *
     * @param jsonData The json data in String.
     * @return The object after parsing if OK, else return null.
     */
    public static List<TestQuestionDao> parse_TestsQuestions(String jsonData) {
        try {
            // Build Moshi object.
            Moshi moshi = new Moshi.Builder()
                    .add(new JsonAdapterDate())
                    .build();

            // Create json adapter, prepare parsing.
            JsonAdapter<List<TestQuestionDao>> jsonAdapter = moshi.adapter(Types.newParameterizedType(List.class, TestQuestionDao.class));

            // Parse json data, return the object.
            return jsonAdapter.fromJson(jsonData);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Parse grammar.json file.
     * Chú ý: Thời gian từ API Server sẽ được convert thành thời gian theo TimeZone ở client.
     *
     * @param jsonData The json data in String.
     * @return The object after parsing if OK, else return null.
     */
    public static List<GrammarDao> parse_Grammars(String jsonData) {
        try {
            // Build Moshi object.
            Moshi moshi = new Moshi.Builder()
                    .add(new JsonAdapterDate())
                    .build();

            // Create json adapter, prepare parsing.
            JsonAdapter<List<GrammarDao>> jsonAdapter = moshi.adapter(Types.newParameterizedType(List.class, GrammarDao.class));

            // Parse json data, return the object.
            return jsonAdapter.fromJson(jsonData);
        } catch (Exception ex) {
            return null;
        }
    }

}
