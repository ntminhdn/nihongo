package com.honkidenihongo.pre.dac.dao;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Data Access Object ánh xạ với table: choices.
 * Todo: Hiện giờ để đơn giản thì không dùng set, get mà để các thuộc tính dạng public.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class ChoiceDao extends RealmObject {
    /**
     * Khóa chính: The Choice Id.
     */
    @PrimaryKey
    @Required
    public Long id;

    /**
     * The Question Id.
     */
    @Required
    public Long question_id;

    /**
     * The Choice Content in Vietnamese.
     */
    @NonNull
    public String content_vi = "";

    /**
     * The Choice Content in English.
     */
    @NonNull
    public String content_en = "";

    /**
     * The Choice Content in Japanese.
     */
    @NonNull
    public String content_ja = "";

    /**
     * The Choice is correct answer or not.
     */
    public Boolean is_correct;

}
