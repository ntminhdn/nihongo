package com.honkidenihongo.pre.dac.dao;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Data Access Object ánh xạ với table: practices.
 * Todo: Hiện giờ để đơn giản thì không dùng set, get mà để các thuộc tính dạng public.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class PracticeDao extends RealmObject {
    /**
     * Khóa chính: The Practice Id.
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
     * The Level.
     */
    public Integer level;

    /**
     * The Category.
     */
    public Integer category;
}
