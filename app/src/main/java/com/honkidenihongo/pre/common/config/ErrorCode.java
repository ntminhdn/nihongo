package com.honkidenihongo.pre.common.config;

/**
 * Hằng số thể hiện danh sách các hằng số về mã lỗi.
 *
 * @author long.tt.
 * @since 01-Dec-2016.
 */
public final class ErrorCode {
    /**
     * The private constructor to prevent creating new object.
     */
    private ErrorCode() {
    }

    public static class S06LessonList {
        /**
         * The private constructor to prevent creating new object.
         */
        private S06LessonList() {
        }

        public static final int ERROR_DOWNLOAD_LESSON = 4001;
        public static final int ERROR_UNZIP_LESSON = 4002;
        public static final int ERROR_IMPORT_LESSON = 4003;
    }

}
