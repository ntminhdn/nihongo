package com.honkidenihongo.pre.common.config;

/**
 * Các hằng số string thể hiện định dạng của Date.
 *
 * @author long.tt.
 * @since 18-Nov-2016.
 */
public final class DateFormatString {
    /**
     * The private constructor to prevent creating new object.
     */
    private DateFormatString() {
    }

    /**
     * "yyyyMMdd"
     */
    public final static String YYYYMMDD = "yyyyMMdd";

    /**
     * "yyyy-MM-dd"
     */
    public final static String YYYY_MM_DD = "yyyy-MM-dd";

    /**
     * "HH:mm:ss"
     */
    public final static String HH_MM_SS = "HH:mm:ss";

    /**
     * "yyyy-MM-dd HH:mm:ss"
     */
    public final static String YYYY_MM_DD__HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * "yyyy/MM/dd HH:mm"
     */
    public final static String YYYY_MM_DD__HH_MM = "yyyy/MM/dd HH:mm";

    /**
     * "MM/dd HH:mm"
     */
    public final static String MM_DD__HH_MM = "MM/dd HH:mm";

    /**
     * "HH:mm"
     */
    public final static String HH_MM = "HH:mm";

    /**
     * "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
     */
    public final static String T_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

}
