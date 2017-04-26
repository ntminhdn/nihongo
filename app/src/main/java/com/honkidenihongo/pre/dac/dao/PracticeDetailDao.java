package com.honkidenihongo.pre.dac.dao;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Data Access Object ánh xạ với table: practice_details.
 * Todo: Hiện giờ để đơn giản thì không dùng set, get mà để các thuộc tính dạng public.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class PracticeDetailDao extends RealmObject {
    /**
     * Khóa chính: The Practice Id.
     */
    @PrimaryKey
    @Required
    public Long id;

    /**
     * The Practice Id.
     */
    @Required
    public Long practice_id;

    /**
     * The Name in Vietnamese.
     */
    @NonNull
    public String name_vi = "";

    /**
     * The Name in English.
     */
    @NonNull
    public String name_en = "";

}
