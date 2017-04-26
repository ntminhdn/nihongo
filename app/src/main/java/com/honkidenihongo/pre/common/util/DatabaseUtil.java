package com.honkidenihongo.pre.common.util;

import android.content.Context;
import android.util.Log;

import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.dac.DatabaseMigration;
import com.honkidenihongo.pre.model.UserModel;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmException;

/**
 * Class tiện ích support database.
 *
 * @author binh.dt.
 * @since 24-Dec-2016.
 */
public class DatabaseUtil {
    private static final String LOG_TAG = DatabaseUtil.class.getName();

    /**
     * The private constructor.
     */
    private DatabaseUtil() {
    }

    /**
     * Cấu hình file database realm theo user đăng nhập.
     *
     * @param context Value context.
     */
    public static void configRealmDatabase(Context context) {
        UserModel lastUserModel = LocalAppUtil.getLastLoginUserInfo(context);

        // Nếu tồn tại user đăng nhập thì Configuration realm Database theo user id.
        if (lastUserModel != null) {
            // Set Default Configuration database theo user login.
            String databaseOfUser = String.format("%s%s%s", AppConfig.SharedPreferencesKey.USER_INFO_PREFIX, lastUserModel.id, Definition.Database.REALM);

            RealmConfiguration config = new RealmConfiguration.Builder()
                    .name(databaseOfUser)
                    .schemaVersion(AppConfig.getDatabaseVersion())
                    .migration(new DatabaseMigration())
                    .build();

            // Fix bug RealmMigration must be provided.
            // http://stackoverflow.com/questions/33940233/realm-migration-needed-exception-in-android-while-retriving-values-from-realm.
            try {
                Realm.setDefaultConfiguration(config);
                Realm.getInstance(config);
            } catch (RealmException ex) {
                // If error , realm file has been deleted.
                Realm.deleteRealm(config);
                Log.e(LOG_TAG, ex.getMessage());
            }
        }
    }
}
