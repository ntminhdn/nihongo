package com.honkidenihongo.pre.api.json;

/**
 * Class thể hiện kết quả của trường "data" khi gọi API: A03Me.
 * Todo: Hiện giờ để đơn giản thì không dùng set, get mà để các thuộc tính dạng public.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class A03Me_JsonData {
    public Long id;
    public String code;
    public String email;
    public String username;
    public String full_name;
    public String url_slug;
    public String avatar_url;

    public String facebook_id;
    public String facebook_email;
    public String facebook_username;
    public String facebook_display_name;
    public String facebook_avatar_url;

    public Boolean has_password;
    public Boolean facebook_connected;
}
