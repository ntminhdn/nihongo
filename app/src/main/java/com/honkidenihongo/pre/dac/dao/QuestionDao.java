package com.honkidenihongo.pre.dac.dao;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Data Access Object ánh xạ với table: questions.
 * Todo: Hiện giờ để đơn giản thì không dùng set, get mà để các thuộc tính dạng public.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class QuestionDao extends RealmObject {
    /**
     * Khóa chính: The Question Id.
     */
    @PrimaryKey
    @Required
    public Long id;

    /**
     * The Level.
     */
    public Integer level;

    /**
     * The Category.
     */
    public Integer category;

    /**
     * The Question Type.
     */
    public Integer type;

    /**
     * The knowledge number.
     */
    @Required
    public Integer knowledge_number;

    /**
     * The Question Content in Vietnamese.
     */
    @NonNull
    public String content_vi = "";

    /**
     * The Question Content in English.
     */
    @NonNull
    public String content_en = "";

    /**
     * The Question Content in Japanese.
     */
    @NonNull
    public String content_ja = "";

    /**
     * The Voice File.
     */
    @NonNull
    public String voice_file = "";

    /**
     * The Lesson Number (add it at database version 2 and remove field lesson_id).
     */
    public Integer lesson_number;
}
