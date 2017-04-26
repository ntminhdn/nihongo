package com.honkidenihongo.pre.api.json;

/**
 * Base class thể hiện kết quả json từ API.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public abstract class BaseJsonResult {
    /**
     * Kết quả từ API trả về true hay false hoặc null nếu không lấy được.
     */
    public Boolean is_success;

}
