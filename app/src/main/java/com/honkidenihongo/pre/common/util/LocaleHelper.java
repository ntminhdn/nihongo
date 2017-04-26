package com.honkidenihongo.pre.common.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import java.util.Locale;

/**
 * Class support setting language for application.
 * Modify by binh.dt.
 * Reference: http://gunhansancar.com/change-language-programmatically-in-android/.
 *
 * @author binh.dt.
 * @since 15-Jan-2017.
 */
public class LocaleHelper {
    // Using save key value language current.
    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.LanguageCode";

    /**
     * Method onCreate.
     *
     * @param context Value context of screen current.
     */
    public static Context onAttach(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        return setLocale(context, lang);
    }

    /**
     * Method onCreate.
     *
     * @param context         Value context of screen current.
     * @param defaultLanguage Value default language.
     */
    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang);
    }

    /**
     * Method using get language current displaying.
     *
     * @param context Value context of screen current.
     * @return Value name language.
     */
    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    /**
     * Method setting language language when user choose setting.
     *
     * @param context  Value context of screen current.
     * @param language Value string name of language.
     */
    public static Context setLocale(Context context, String language) {
        persist(context, language);
        return updateResources(context, language);
    }

    /**
     * Method getPersistedData.
     *
     * @param context         Value context of screen current.
     * @param defaultLanguage Value  default string name of language.
     * @return Name language displaying.
     */
    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    /**
     * Method persist.
     *
     * @param context  Value context of screen current.
     * @param language Value string name of language.
     */
    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    /**
     * Phương thức thay đổi resource.
     *
     * @param context  Value context of screen current.
     * @param language Value name of language.
     */
    private static Context updateResources(Context context, String language) {
        // Android Platform Version 7.0 up.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResourcesFromApi24(context, language);
        }

        return updateResourcesBeforeApi24(context, language);
    }

    /**
     * Phương thức update value of resources đối với Android Platform Version 7.0 trở lên.
     * Build.VERSION_CODES.N = 24.
     *
     * @param context  Value context of screen current.
     * @param language Value name language.
     */
    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResourcesFromApi24(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);

        return context.createConfigurationContext(configuration);
    }

    /**
     * Method dùng để update resource khi api version hiện tại < 24.
     *
     * @param context  Value context of screen current.
     * @param language Value language.
     */
    private static Context updateResourcesBeforeApi24(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }

    /**
     * Todo phương thức này gây ra lỗi ngôn ngữ.
     * Phương thức dùng để get nội dung của string theo ngôn ngữ được set và value id.
     *
     * @param context   Value context of screen current.
     * @param language  Value Language.
     * @param stringRes Value id of string.
     * @return Value string follow to language.
     */
    public static String getStringByLocale(Context context, String language, int stringRes) {
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale savedLocale = conf.locale;

        conf.locale = new Locale(language); // Whatever you want here.
        res.updateConfiguration(conf, null); // Second arg null means don't change.

        // Retrieve resources from desired locale.
        String str = res.getString(stringRes);

        // Restore original locale.
        conf.locale = savedLocale;
        res.updateConfiguration(conf, null);

        return str;
    }
}
