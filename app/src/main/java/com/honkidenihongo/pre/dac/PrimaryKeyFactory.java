package com.honkidenihongo.pre.dac;

import android.util.Log;

import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.model.CoinLog;
import com.honkidenihongo.pre.model.PracticeData;
import com.honkidenihongo.pre.model.Course;
import com.honkidenihongo.pre.model.ExamLog;
import com.honkidenihongo.pre.model.Knowledge;
import com.honkidenihongo.pre.model.Question;
import com.honkidenihongo.pre.model.PracticeCoin;
import com.honkidenihongo.pre.model.Result;
import com.honkidenihongo.pre.model.TimeLog;
import com.honkidenihongo.pre.model.Unit;
import com.honkidenihongo.pre.model.UnitData;
import com.honkidenihongo.pre.model.UnitDataProgressLog;
import com.honkidenihongo.pre.model.UnitProgressLog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import io.realm.Realm;
import io.realm.RealmObject;

import static java.lang.String.format;

/**
 * Created by datpt on 7/8/16.
 * <p/>
 * Class used to generate id (make id auto increment) for table of database
 */

public class PrimaryKeyFactory {

    /**
     * Singleton instance.
     */
    private final static PrimaryKeyFactory instance = new PrimaryKeyFactory();

    private static Class[] models = {PracticeData.class, Course.class, Knowledge.class,
            Question.class, Unit.class, UnitData.class, Result.class, ExamLog.class,
            TimeLog.class, UnitDataProgressLog.class, UnitProgressLog.class, PracticeCoin.class, CoinLog.class};

    /**
     * Maximum primary key values.
     */
    private Map<Class<? extends RealmObject>, AtomicLong> keys;

    /**
     * get the singleton instance
     *
     * @return singleton instance
     */
    public static PrimaryKeyFactory getInstance() {
        return instance;
    }

    /**
     * Initialize the factory. Must be called before any primary key is generated
     * - preferably from application class.
     */
    public synchronized void initialize(final Realm realm) {
        if (keys != null) {
            return;
            // throw new IllegalStateException("already initialized");
        }

        // keys field is used as an initialization flag at the same time
        keys = new HashMap<>();
        for (Class c : models) {
            Number keyValue = null;

            try {
                keyValue = realm.where(c).max(Definition.Database.FIELD_ID);
            } catch (ArrayIndexOutOfBoundsException ex) {
                Log.d(getClass().getSimpleName(), format("error while getting number primary key %s " +
                        " for %s", Definition.Database.FIELD_ID, c.getName()), ex);
            }

            if (keyValue == null) {
                Log.w(getClass().getSimpleName(), format("can't find number primary key %s " +
                        " for %s.", Definition.Database.FIELD_ID, c.getName()));
            } else {
                keys.put(c, new AtomicLong(keyValue.longValue()));
            }
        }
    }

    /**
     * Automatically create next key for a given class.
     */
    public synchronized long nextKey(final Class<? extends RealmObject> clazz) {
        if (keys == null) {
            throw new IllegalStateException("not initialized yet");
        }
        AtomicLong l = keys.get(clazz);
        if (l == null) {
            Log.i(getClass().getSimpleName(), "There was no primary keys for " + clazz.getName());
            //RealmConfiguration#getRealmObjectClasses() returns only classes with existing instances
            //so we need to store value for the first instance created
            l = new AtomicLong(0);
            keys.put(clazz, l);
        }
        return l.incrementAndGet();
    }

}