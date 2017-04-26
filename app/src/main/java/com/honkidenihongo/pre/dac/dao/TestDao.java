package com.honkidenihongo.pre.dac.dao;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Data Access Object ánh xạ với table: tests.
 * Todo: Hiện giờ để đơn giản thì không dùng set, get mà để các thuộc tính dạng public.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class TestDao extends RealmObject {
    /**
     * Khóa chính: The Test Id.
     */
    @PrimaryKey
    @Required
    public Long id;

    /**
     * The Lesson Id.
     */
    @Required
    public Long lesson_id;

    /**
     * The Description in Vietnamese.
     */
    @NonNull
    public String description_vi = "";

    /**
     * The Description in English.
     */
    @NonNull
    public String description_en = "";

    /**
     * The Description in Japanese.
     */
    @NonNull
    public String description_ja = "";

}
