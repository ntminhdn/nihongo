package com.honkidenihongo.pre.gui.listener;

import com.honkidenihongo.pre.model.Lesson;

/**
 * Created by BinhDT on 07/11/2016.
 */

public interface OnMainActivityListener {
    /**
     * Set value title for screen Main.
     *
     * @param title value name title.
     */
    void setTitleScreen(String title);

    /**
     * Goto screen Lesson.
     */
    void goToScreen_S06LessonList();

    /**
     * Goto screen of Lesson.
     *
     * @param lesson value object.
     */
    void goToScreen_S06LessonList_Item(Lesson lesson);

    /**
     * Goto screen LessonCategory.
     *
     * @param lesson Value method.
     */
    void goToScreenS19LessonCategory(Lesson lesson);

    /**
     * Goto screen S18Setting.
     */
    void goToScreen_S18Setting();

    /**
     * Goto screen S27GrammarList.
     */
    void goToScreenS27GrammarList(Lesson lesson);
}
