package com.honkidenihongo.pre.common.util;

import com.honkidenihongo.pre.model.constant.LanguageNumberCode;
import com.honkidenihongo.pre.model.constant.LessonNameFormat;
import com.honkidenihongo.pre.model.constant.LessonType;
import com.honkidenihongo.pre.model.Lesson;

/**
 * Class tiện ích để lấy Lesson Name theo Lesson Number.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class LessonNameUtil {
    /**
     * The private constructor to prevent creating new object.
     */
    private LessonNameUtil() {
    }

    public static String getLessonName(Lesson lesson, int language) {
        if (lesson.getType() == LessonType.PRE_LESSON) {
            switch (language) {
                case LanguageNumberCode.ENGLISH:
                    return LessonNameFormat.PRE_LESSON_NAME_EN;
                case LanguageNumberCode.JAPANESE:
                    return LessonNameFormat.PRE_LESSON_NAME_JA;
                default:
                    return LessonNameFormat.PRE_LESSON_NAME_VI;
            }
        } else {
            switch (language) {
                case LanguageNumberCode.ENGLISH:
                    return String.format(LessonNameFormat.LESSON_NAME_EN, lesson.getNumber());
                case LanguageNumberCode.JAPANESE:
                    return String.format(LessonNameFormat.LESSON_NAME_JA, lesson.getNumber());
                default:
                    return String.format(LessonNameFormat.LESSON_NAME_VI, lesson.getNumber());
            }
        }
    }
}
