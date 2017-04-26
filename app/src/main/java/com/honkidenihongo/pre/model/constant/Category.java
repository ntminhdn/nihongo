package com.honkidenihongo.pre.model.constant;

/**
 * Hằng số thể hiện các kiểu Category như: Pre-Lesson (Hiragana), Unit (Word), Unit (Sentence)...
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public final class Category {
    /**
     * The private constructor to prevent creating new object.
     */
    private Category() {
    }

    /**
     * Category is Pre-Lesson (Hiragana).
     */
    public static final int PRE_HIRAGANA = 11;

    /**
     * Category is Pre-Lesson (Katakana).
     */
    public static final int PRE_KATAKANA = 12;

    /**
     * Category is Pre-Lesson (Number).
     */
    public static final int PRE_NUMBER = 13;

    /**
     * Category is Pre-Lesson (Common Greeting Sentence).
     */
    public static final int PRE_COMMON = 14;

    /**
     * Category is Unit (Word).
     */
    public static final int UNIT_WORD = 21;

    /**
     * Category is Unit (Sentence).
     */
    public static final int UNIT_SENTENCE = 22;

}
