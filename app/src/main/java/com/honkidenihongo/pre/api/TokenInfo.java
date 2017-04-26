package com.honkidenihongo.pre.api;

/**
 * Lưu trữ thông tin về Token.
 * Todo: Hiện giờ để đơn giản thì không dùng set, get mà để các thuộc tính dạng public.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class TokenInfo {
    /**
     * The Access-Token.
     */
    public String accessToken;

    /**
     * The Refresh-Token.
     */
    public String refreshToken;

    /**
     * Todo...
     */
    public Long expires;
}