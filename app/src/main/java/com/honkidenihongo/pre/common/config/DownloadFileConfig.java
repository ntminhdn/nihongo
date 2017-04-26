package com.honkidenihongo.pre.common.config;

/**
 * Các cấu hình tên file, thư mục sau khi download, chuẩn bị import vào Database.
 *
 * @author long.tt.
 * @since 18-Nov-2016.
 */
public final class DownloadFileConfig {
    /**
     * The private constructor to prevent creating new object.
     */
    private DownloadFileConfig() {
    }

    /**
     * The download file name prefix: lesson_.
     */
    public static final String DOWNLOAD_FILE_PREFIX_LESSON = "lesson_";

    /**
     * The download file type: zip.
     */
    public static final String DOWNLOAD_FILE_TYPE_ZIP = "zip";

    /**
     * The file: choices.json.
     */
    public static final String FILE_CHOICES_JSON = "choices.json";

    /**
     * The file: knowledges.json.
     */
    public static final String FILE_KNOWLEDGES_JSON = "knowledges.json";

    /**
     * The file: lesson.json.
     */
    public static final String FILE_LESSON_JSON = "lesson.json";

    /**
     * The file: knowledge_details.json.
     */
    public static final String FILE_QUESTIONS_JSON = "questions.json";

    /**
     * The file: grammar.json.
     */
    public static final String FILE_GRAMMARS_JSON = "grammars.json";

}
