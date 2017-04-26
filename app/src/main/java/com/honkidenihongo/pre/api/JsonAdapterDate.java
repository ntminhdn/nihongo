package com.honkidenihongo.pre.api;

import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.DateFormatString;
import com.honkidenihongo.pre.common.util.DateUtil;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.util.Date;
import java.util.TimeZone;

/**
 * Json Adapter class for parsing Date string.
 *
 * @author long.tt.
 * @since 18-Nov-2016.
 */
public class JsonAdapterDate {
    /**
     * The TimeZone.
     */
    private TimeZone timeZone;

    /**
     * The default constructor with default timezone from the Server config.
     */
    public JsonAdapterDate() {
        this.timeZone = AppConfig.getServerTimezone();
    }

    /**
     * The constructor with parameter.
     *
     * @param timeZone The TimeZone used for converting the Date to string and vice versa.
     */
    public JsonAdapterDate(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @ToJson
    public String toJson(Date date) {
        // Convert the date to string.
        return DateUtil.dateToString(date, DateFormatString.YYYY_MM_DD__HH_MM_SS, timeZone);
    }

    @FromJson
    public Date fromJson(String dateString) {
        // Convert the string to date.
        return DateUtil.stringToDate(dateString, DateFormatString.YYYY_MM_DD__HH_MM_SS, timeZone);
    }
}