package com.honkidenihongo.pre.api.json;

import com.honkidenihongo.pre.dac.dao.LessonDao;

import java.util.List;

/**
 * Class thể hiện kết quả của việc gọi API: A25LessonList.
 * Todo: Hiện giờ để đơn giản thì không dùng set, get mà để các thuộc tính dạng public.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class A25LessonList_JsonResult extends BaseJsonResult {

    /**
     * Dữ liệu json trả về.
     */
    public List<LessonDao> data;
}
