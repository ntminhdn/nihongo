package com.honkidenihongo.pre.dac.dao;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Data Access Object ánh xạ với table: knowledges.
 * Todo: Hiện giờ để đơn giản thì không dùng set, get mà để các thuộc tính dạng public.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class KnowledgeDao extends RealmObject {
    /**
     * Khóa chính: The Knowledge Id.
     */
    @PrimaryKey
    @Required
    public Long id;

    /**
     * The Number.
     */
    public Integer number;

    /**
     * The Level.
     */
    public Integer level;

    /**
     * The Category.
     */
    public Integer category;

    /**
     * The Subject Kana.
     */
    @NonNull
    public String subject_kana = "";

    /**
     * The Subject Kanji
     */
    @NonNull
    public String subject_kanji = "";

    /**
     * The Subject Romaji.
     */
    @NonNull
    public String subject_romaji = "";

    /**
     * The Meaning in Vietnamese.
     */
    @NonNull
    public String meaning_vi = "";

    /**
     * The Meaning in English.
     */
    @NonNull
    public String meaning_en = "";

    /**
     * The Voice File.
     */
    @NonNull
    public String voice_file = "";

    /**
     * The Picture File (add it at database version 2 and remove field lesson_id).
     */
    @NonNull
    public String picture_file = "";

    /**
     * The Lesson Number (add it at database version 2 and remove field lesson_id).
     */
    public Integer lesson_number;
}
