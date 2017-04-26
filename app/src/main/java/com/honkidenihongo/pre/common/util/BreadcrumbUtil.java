package com.honkidenihongo.pre.common.util;

import android.content.Context;
import android.text.TextUtils;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.constant.Category;
import com.honkidenihongo.pre.model.constant.LessonType;
import com.honkidenihongo.pre.model.constant.Level;

/**
 * Class support lấy đường dẫn cho title breadcrumb cho màn hình hiện tại.
 *
 * @author binh.dt.
 * @since 05-Jan-2017.
 */
public class BreadcrumbUtil {
    /**
     * The private constructor.
     */
    private BreadcrumbUtil() {
    }

    /**
     * Method get content of breadcrumb for screen current.
     *
     * @param context Value context of screen current.
     * @param lesson  Object lesson.
     * @param module  Name screen current.
     * @return Value breadcrumb.
     */
    public static String getBreadcrumb(Context context, Lesson lesson, String module) {
        String contentBreadcrumb = "";

        if (lesson == null || TextUtils.isEmpty(module)) {
            return contentBreadcrumb;
        }

        String category = "";
        String level = "";

        // Compare type of lesson.
        if (lesson.getType() == LessonType.PRE_LESSON) {
            switch (lesson.getCategory()) {
                case Category.PRE_HIRAGANA:
                    category = context.getString(R.string.common_app__category__pre_hiragana);

                    break;
                case Category.PRE_KATAKANA:
                    category = context.getString(R.string.common_app__category__pre_katakna);

                    break;
                case Category.PRE_NUMBER:
                    category = context.getString(R.string.common_app__category__pre_number);

                    break;
                case Category.PRE_COMMON:
                    category = context.getString(R.string.common_app__category__pre_common);

                    break;
            }
        } else {
            switch (lesson.getCategory()) {
                case Category.UNIT_WORD:
                    category = context.getString(R.string.common_app__category__unit_word);

                    break;

                case Category.UNIT_SENTENCE:
                    category = context.getString(R.string.common_app__category__unit_sentence);

                    break;
            }

            switch (lesson.getLevel()) {
                case Level.BASIC:
                    level = context.getString(R.string.common_app__level__basic);

                    break;
                case Level.ADVANCE:
                    level = context.getString(R.string.common_app__level__advance);

                    break;
            }

        }

        if (TextUtils.isEmpty(level)) {
            contentBreadcrumb = String.format("%s%s%s", category, Definition.General.BREADCRUMB_SEPARATOR, module);
        } else {
            contentBreadcrumb = String.format("%s%s%s%s%s", level, Definition.General.BREADCRUMB_SEPARATOR, category, Definition.General.BREADCRUMB_SEPARATOR, module);
        }

        return contentBreadcrumb;
    }
}
