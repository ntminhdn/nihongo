package com.honkidenihongo.pre.common.util;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Class ToolbarUtil support for setup toolbar.
 *
 * @author binh.dt.
 * @since 05-Jan-2017.
 */
public class ToolbarUtil {

    /**
     * The private constructor.
     */
    private ToolbarUtil() {
    }

    /**
     * Method init toolbar.
     *
     * @param toolbar  Toolbar inside activity current.
     * @param activity Value activity current.
     */
    public static void initToolbar(Toolbar toolbar, AppCompatActivity activity) {
        if (toolbar == null || activity == null) {
            return;
        }

        toolbar.collapseActionView();
        activity.setSupportActionBar(toolbar);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
            activity.getSupportActionBar().setHomeButtonEnabled(true);
        }
    }
}
