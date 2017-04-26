package com.honkidenihongo.pre.dac.dao;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Data Access Object ánh xạ với table: lessons.
 * Todo: Hiện giờ để đơn giản thì không dùng set, get mà để các thuộc tính dạng public.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class LessonDao extends RealmObject {
    /**
     * Khóa chính: The Lesson Id.
     */
    @PrimaryKey
    @Required
    public Long id;

    /**
     * The Lesson Type (Pre-Lesson, Unit).
     */
    public Integer type;

    /**
     * The Lesson Number (0: Pre-Lesson, 1: Unit 1, 2: Unit 2,...).
     */
    public Integer number;

    /**
     * The Lesson Status.
     */
    public Integer status;

    /**
     * The version of Lesson.
     */
    public Integer version;

    /**
     * The description of Lesson.
     */
    public String description;

    /**
     * The title vietnamese.
     */
    @Required
    @NonNull
    public String title_vi = "";

    /**
     * The title english.
     */
    @Required
    @NonNull
    public String title_en = "";

    /**
     * The title japan.
     */
    @Required
    @NonNull
    public String title_ja = "";
}
