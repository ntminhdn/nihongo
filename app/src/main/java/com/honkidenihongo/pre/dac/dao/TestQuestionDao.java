package com.honkidenihongo.pre.dac.dao;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Data Access Object ánh xạ với table: tests_questions.
 * Todo: Hiện giờ để đơn giản thì không dùng set, get mà để các thuộc tính dạng public.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class TestQuestionDao extends RealmObject {
    /**
     * Khóa chính: The TestQuestion Id.
     */
    @PrimaryKey
    @Required
    public Long id;

    /**
     * The Test Id.
     */
    @Required
    public Long test_id;

    /**
     * The Question Id.
     */
    @Required
    public Long question_id;

}
