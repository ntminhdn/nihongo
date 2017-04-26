package com.honkidenihongo.pre.common.util;

import com.honkidenihongo.pre.common.config.Definition;

/**
 * Util class related to common operations of String.
 *
 * @author long.tt.
 * @since 17-Nov-2016.
 */
public class StringUtil {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = StringUtil.class.getName();

    /**
     * The private constructor to prevent creating object.
     */
    private StringUtil() {
    }

    /**
     * Todo using later.
     * Kiểm tra 1 string có null hoặc empty hay không?
     *
     * @param string String cần kiểm tra.
     * @return Nếu string là null hoặc empty thì trả về true, còn nếu không (khác null và không empty) thì trả về false.
     */
//    public static boolean isNullOrEmpty(String string) {
//        if (string != null && !"".equals(string)) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    /**
     * Method is used for checking valid email id format.
     *
     * @return Boolean true for valid false for invalid.
     */
    public static boolean isValidEmail(String email) {
        return email.matches(Definition.Valid.EMAIL);
    }

    /**
     * Method is used for checking valid username id format.
     *
     * @return Boolean true for valid false for invalid.
     */
    public static boolean isValidUserName(String username) {
        return username.matches(Definition.Valid.USER_NAME);
    }
}
