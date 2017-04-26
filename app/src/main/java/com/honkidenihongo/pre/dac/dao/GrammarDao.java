package com.honkidenihongo.pre.dac.dao;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Data Access Object ánh xạ với table: grammars.
 * Todo: Hiện giờ để đơn giản thì không dùng set, get mà để các thuộc tính dạng public.
 *
 * @author binh.dt.
 * @since 04-Jan-2017.
 */
public class GrammarDao extends RealmObject {
    /**
     * Khóa chính: The GrammarDao Id.
     */
    @PrimaryKey
    @Required
    public Long id;

    /**
     * The title Vietnamese.
     */
    @NonNull
    public String title_vi = "";

    /**
     * The title English.
     */
    @NonNull
    public String title_en = "";

    /**
     * The content Vietnamese.
     */
    @NonNull
    public String content_vi = "";

    /**
     * The content English.
     */
    @NonNull
    public String content_en = "";

    /**
     * The Lesson Number (add at database version 2 and remove field lesson_id).
     */
    public Long lesson_number;

    /**
     * The display_order.
     */
    public Integer display_order;
}
