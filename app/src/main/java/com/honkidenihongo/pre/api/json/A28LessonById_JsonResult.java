package com.honkidenihongo.pre.api.json;

import com.honkidenihongo.pre.dac.dao.LessonDao;

/**
 * Class thể hiện kết quả của việc gọi API: A28LessonById.
 * Todo: Hiện giờ để đơn giản thì không dùng set, get mà để các thuộc tính dạng public.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class A28LessonById_JsonResult extends BaseJsonResult {

    /**
     * Dữ liệu json trả về.
     */
    public LessonDao data;
}
