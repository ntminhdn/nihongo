package com.honkidenihongo.pre.common.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Class using support show/hide keyboard when open window.
 *
 * @author binh.dt.
 * @since 10-Nov-2016.
 */
public class KeyboardUtil {

    /**
     * Private Constructor of Class.
     */
    private KeyboardUtil() {
    }

    /**
     * Dùng để ẩn bàn phím vật lý khi màn hình của view Fragment chứa View con là EditText.
     *
     * @param view    View màn hình hiện tại.
     * @param context Giá trị context của màn hình hiện tại.
     */
    public static void hideKeyboard(View view, Context context) {
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * Dùng để hiện bàn phím vật lý.
     *
     * @param view    View màn hình hiện tại.
     * @param context Giá trị context của màn hình hiện tại.
     */
    public static void showKeyboard(View view, Context context) {
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
