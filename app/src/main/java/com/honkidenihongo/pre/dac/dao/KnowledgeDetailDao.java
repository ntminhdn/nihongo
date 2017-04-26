package com.honkidenihongo.pre.dac.dao;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Data Access Object ánh xạ với table: knowledge_details.
 * Todo: Hiện giờ để đơn giản thì không dùng set, get mà để các thuộc tính dạng public.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class KnowledgeDetailDao extends RealmObject {
    /**
     * Khóa chính: The KnowledgeDetail Id.
     */
    @PrimaryKey
    @Required
    public Long id;

    /**
     * The Knowledge Id.
     */
    @Required
    public Long knowledge_id;

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
}
