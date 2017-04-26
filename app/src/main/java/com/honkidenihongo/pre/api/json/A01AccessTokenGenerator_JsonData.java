package com.honkidenihongo.pre.api.json;

/**
 * Class thể hiện kết quả của trường "data" khi gọi API: A01AccessTokenGenerator.
 * Todo: Hiện giờ để đơn giản thì không dùng set, get mà để các thuộc tính dạng public.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class A01AccessTokenGenerator_JsonData {
    public String access_token;
    public String token_type;
    public Long expires_in;
    public String refresh_token;
}
