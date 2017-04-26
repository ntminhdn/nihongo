package com.honkidenihongo.pre.model.constant;

/**
 * Hằng số thể hiện các kiểu ngôn ngữ (Japanese, English, Vietnamese) dưới dạng số.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public final class LanguageNumberCode {
    /**
     * The private constructor to prevent creating new object.
     */
    private LanguageNumberCode() {
    }

    /**
     * Ngôn ngữ là Tiếng Nhật.
     */
    public static final int JAPANESE = 0;

    /**
     * Ngôn ngữ là Tiếng Việt.
     */
    public static final int VIETNAMESE = 1;

    /**
     * Ngôn ngữ là Tiếng Anh.
     */
    public static final int ENGLISH = 2;

}
