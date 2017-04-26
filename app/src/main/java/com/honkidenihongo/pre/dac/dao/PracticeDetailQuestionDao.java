package com.honkidenihongo.pre.dac.dao;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Data Access Object ánh xạ với table: practice_detail_questions.
 * Todo: Hiện giờ để đơn giản thì không dùng set, get mà để các thuộc tính dạng public.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class PracticeDetailQuestionDao extends RealmObject {
    /**
     * Khóa chính: The PracticeDetailQuestion Id.
     */
    @PrimaryKey
    @Required
    public Long id;

    /**
     * The PracticeDetail Id.
     */
    @Required
    public Long practice_detail_id;

    /**
     * The Question Id.
     */
    @Required
    public Long question_id;

}
