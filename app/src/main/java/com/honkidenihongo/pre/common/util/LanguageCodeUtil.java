package com.honkidenihongo.pre.common.util;

import android.content.Context;

import com.honkidenihongo.pre.common.config.Definition;

/**
 * Class support get language code.
 * Created by Binh.dt.
 * 3/9/17.
 */
public class LanguageCodeUtil {

    /**
     * The private constructor.
     */
    private LanguageCodeUtil() {
    }

    /**
     * Get language code.
     *
     * @param context Value context of screen current.
     * @return Value string.
     */
    public static String getLanguageCode(Context context) {
        String languageCode = "";

        if (context == null) {
            return languageCode;
        }

        if (LocaleHelper.getLanguage(context).equals(Definition.LanguageCode.ENGLISH)) {
            languageCode = Definition.LanguageCode.ENGLISH;
        }

        if (LocaleHelper.getLanguage(context).equals(Definition.LanguageCode.VIETNAMESE)) {
            languageCode = Definition.LanguageCode.VIETNAMESE;
        }

        if (LocaleHelper.getLanguage(context).equals(Definition.LanguageCode.JAPANESE)) {
            languageCode = Definition.LanguageCode.JAPANESE;
        }

        return languageCode;
    }
}
