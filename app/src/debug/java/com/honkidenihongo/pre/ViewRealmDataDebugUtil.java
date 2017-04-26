package com.honkidenihongo.pre;

import android.content.Context;

/**
 * Class ViewRealmDataDebugUtil, for view Realm data in Debug mode.
 *
 * @author long.tt.
 * @since 14-Jan-2017.
 */
public class ViewRealmDataDebugUtil {
    /**
     * The private constructor.
     */
    private ViewRealmDataDebugUtil() {
    }

    public static void initialize(Context context) {
        if (com.honkidenihongo.pre.BuildConfig.DEBUG) {
            // Todo: View Realm data util in debug mode.
            // com.facebook.stetho.Stetho.initializeWithDefaults(this);
            com.facebook.stetho.Stetho.initialize(
                    com.facebook.stetho.Stetho.newInitializerBuilder(context)
                            .enableDumpapp(com.facebook.stetho.Stetho.defaultDumperPluginsProvider(context))
                            .enableWebKitInspector(com.uphyca.stetho_realm.RealmInspectorModulesProvider.builder(context).build())
                            .build());
        }

    }
}
