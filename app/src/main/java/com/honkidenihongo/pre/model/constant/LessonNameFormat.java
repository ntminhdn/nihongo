package com.honkidenihongo.pre.model.constant;

/**
 * Hằng số thể hiện định dạng tên bài học theo số bài và ngôn ngữ.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public final class LessonNameFormat {
    /**
     * The private constructor to prevent creating new object.
     */
    private LessonNameFormat() {
    }

    /**
     * Định dạng tên bài học theo tiếng Nhật: 第%d課.
     */
    public static final String LESSON_NAME_JA = "第%d課";

    /**
     * Định dạng tên bài học theo tiếng Việt: Bài %d.
     */
    public static final String LESSON_NAME_VI = "Bài %d";

    /**
     * Định dạng tên bài học theo tiếng Anh: Unit %d.
     */
    public static final String LESSON_NAME_EN = "Unit %d";

    /**
     * Định dạng tên bài học theo tiếng Việt, trường hợp đặc biệt: Bài mở đầu.
     */
    public static final String PRE_LESSON_NAME_VI = "Bài mở đầu";

    /**
     * Định dạng tên bài học theo tiếng Anh, trường hợp đặc biệt: Pre-Lesson.
     */
    public static final String PRE_LESSON_NAME_EN = "Pre-Lesson";

    /**
     * Định dạng tên bài học theo tiếng Nhật, trường hợp đặc biệt: プレ レッスン.
     */
    public static final String PRE_LESSON_NAME_JA = "プレ レッスン";

}
