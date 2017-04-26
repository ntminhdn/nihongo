package com.honkidenihongo.pre.common.util;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Class tiện ích để parse kiểu Date đến kiểu String và ngược lại: từ kiểu String đến kiểu Date.
 *
 * @author long.tt.
 * @since 16-Nov-2016.
 */
public class DateUtil {
    /**
     * The Tag for logging.
     */
    private final static String LOG_TAG = DateUtil.class.getName();

    /**
     * The private constructor to prevent creating new object.
     */
    private DateUtil() {
    }

    /**
     * Convert the Date to String with the default TimeZone
     * Chú ý: Locale sử dụng là Locale.US.
     *
     * @param date       The Date object.
     * @param dateFormat The date format in string.
     * @return The date in string if converting is OK, otherwise return null.
     */
    public static String dateToString(Date date, String dateFormat) {
        // Nếu giá trị date là null thì sẽ trả về null string.
        if (date == null) {
            return null;
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.US);

            return formatter.format(date);
        } catch (Exception ex) {
            // Logging the Exception.
            Log.e(LOG_TAG, "Error when convert the date to string: " + ex.getMessage());

            return null;
        }
    }

    /**
     * Convert the Date to String with the TimeZone.
     * Chú ý: Locale sử dụng là Locale.US.
     *
     * @param date       The Date object.
     * @param dateFormat The date format in string.
     * @param timeZone   The TimeZone used for converting.
     * @return The date in string if converting is OK, otherwise return null.
     */
    public static String dateToString(Date date, String dateFormat, TimeZone timeZone) {
        // Nếu giá trị date là null thì sẽ trả về null string.
        if (date == null) {
            return null;
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.US);
            formatter.setTimeZone(timeZone);

            return formatter.format(date);
        } catch (Exception ex) {
            // Logging the Exception.
            Log.e(LOG_TAG, "Error when convert the date to string: " + ex.getMessage());

            return null;
        }
    }

    /**
     * Convert the String to Date object with the default TimeZone.
     * Chú ý: Locale sử dụng là Locale.US.
     *
     * @param dateString The date in string.
     * @param dateFormat The date format in string.
     * @return The Date object if parsing is OK, otherwise return null.
     */
    public static Date stringToDate(String dateString, String dateFormat) {
        // Nếu giá trị Date String là null hoặc Empty thì sẽ trả về null Date object.
        if (dateString == null || "".equals(dateString)) {
            return null;
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.US);

            return formatter.parse(dateString);
        } catch (Exception ex) {
            // Logging the Exception.
            Log.e(LOG_TAG, "Error when parsing the string to date: " + ex.getMessage());

            return null;
        }
    }

    /**
     * Convert the String to Date object with the TimeZone.
     * Chú ý: Locale sử dụng là Locale.US.
     *
     * @param dateString The date in string.
     * @param dateFormat The date format in string.
     * @param timeZone   The TimeZone used for parsing.
     * @return The Date object if parsing is OK, otherwise return null.
     */
    public static Date stringToDate(String dateString, String dateFormat, TimeZone timeZone) {
        // Nếu giá trị Date String là null hoặc Empty thì sẽ trả về null Date object.
        if (dateString == null || "".equals(dateString)) {
            return null;
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.US);
            formatter.setTimeZone(timeZone);

            return formatter.parse(dateString);
        } catch (Exception ex) {
            // Logging the Exception.
            Log.e(LOG_TAG, "Error when parsing the string to date: " + ex.getMessage());

            return null;
        }
    }
}