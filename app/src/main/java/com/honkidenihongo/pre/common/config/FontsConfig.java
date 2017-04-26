package com.honkidenihongo.pre.common.config;

import android.content.Context;
import android.graphics.Typeface;
import android.icu.text.DateFormat;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Class tiện ích support set font for text view display content.
 *
 * @author Binh.dt.
 * @since 08-Jan-2016.
 */
public final class FontsConfig {
    private static final String LOG_TAG = FontsConfig.class.getSimpleName();

    public enum AppFont {
        KLEE;

        @Nullable
        public static AppFont getFontByString(String name) {
            if (Definition.Fonts.FONT_KLEE.equals(name)) {
                return KLEE;
            }

            return null;
        }
    }

    private static Typeface mTypeface;
    private static Context mContext;
    private static FontsConfig mInstance;

    /**
     * Private Constructor of Class.
     */
    private FontsConfig() {
    }

    /**
     * Method instance.
     *
     * @param context Value context of screen current.
     * @return FontsConfig.
     */
    public static FontsConfig getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FontsConfig();
            FontsConfig.mContext = context;
        }

        return mInstance;
    }

    /**
     * Method using get Typeface set font for textView.
     *
     * @param font Value of font.
     * @return Typeface.
     */
    public Typeface getFont(AppFont font) {
        if (font == null) {
            return null;
        }

        switch (font) {
            case KLEE:
                if (mTypeface == null) {
                    mTypeface = Typeface.createFromAsset(mContext.getResources().getAssets(), Definition.Fonts.PATH_FONT_KLEE);
                }

                return mTypeface;

            default:
                return null;
        }
    }

    /**
     * Using reflection to override default typeface.
     * NOTICE: DO NOT FORGET TO SET TYPEFACE FOR APP THEME AS DEFAULT TYPEFACE WHICH WILL BE OVERRIDDEN.
     *
     * @param context                    To work with assets.
     * @param defaultFontNameToOverride  For example "monospace".
     * @param customFontFileNameInAssets File name of the font from assets.
     */
    public static void overrideFont(Context context, String defaultFontNameToOverride, String customFontFileNameInAssets) {
        try {
            final Typeface customFontTypeface = Typeface.createFromAsset(context.getAssets(), customFontFileNameInAssets);

            final Field defaultFontTypefaceField = Typeface.class.getDeclaredField(defaultFontNameToOverride);
            defaultFontTypefaceField.setAccessible(true);
            defaultFontTypefaceField.set(null, customFontTypeface);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }
}