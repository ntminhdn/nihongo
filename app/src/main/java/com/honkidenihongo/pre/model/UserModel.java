package com.honkidenihongo.pre.model;

import com.honkidenihongo.pre.api.json.A01AccessTokenGenerator_JsonData;

import io.realm.annotations.Required;

/**
 * Lưu trữ thông tin, hồ sơ User sau khi login.
 * Todo: Hiện giờ để đơn giản thì không dùng set, get mà để các thuộc tính dạng public.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class UserModel {
    public long id;

    public int authType;
    public A01AccessTokenGenerator_JsonData tokenInfo;

    /**
     * System Server information.
     */
    public String code;
    public String email;
    public String username;
    public String fullName;
    public String urlSlug;
    public String avatarUrl;

    /**
     * Facebook information.
     */
    public String facebookId;
    public String facebookEmail;
    public String facebookUsername;
    public String facebookDisplayName;
    public String facebookAvatarUrl;

    public boolean hasPassword;
    public boolean facebookConnected;

    /**
     * The Lesson Id.
     */
    @Required
    public Long lesson_id;

}
