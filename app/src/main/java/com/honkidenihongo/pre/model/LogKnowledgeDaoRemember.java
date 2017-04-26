package com.honkidenihongo.pre.model;

import com.honkidenihongo.pre.common.config.Definition;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Class LogKnowledgeDaoRemember using remember Knowledge.
 *
 * @author BinhDT.
 */
public class LogKnowledgeDaoRemember extends RealmObject {

    /**
     * The Knowledge Id.
     */
    @PrimaryKey
    @Required
    public Long id;

    /**
     * The Lesson Id.
     */
    @Required
    public Long lesson_id;

    /**
     * The Level.
     */
    public Integer level;

    /**
     * The Category.
     */
    public Integer category;

    /**
     * Method using save data to Realm.
     */
    public void saveOrUpdate() {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(LogKnowledgeDaoRemember.this);
                }
            });
        } finally {
            if (realm != null && !realm.isClosed()) {
                realm.close();
            }
        }
    }

    /**
     * Get all list remember from Lesson.
     *
     * @return List LogKnowledgeDaoRemember.
     */
    public static List<LogKnowledgeDaoRemember> getAllRemember(final Lesson lesson) {
        final List<LogKnowledgeDaoRemember> logKnowledgeDaoRemembers = new ArrayList<>();

        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    // Tìm tất cả những đối tượng mà người dùng đã nhớ.
                    logKnowledgeDaoRemembers.addAll(Realm.getDefaultInstance().where(LogKnowledgeDaoRemember.class)
                            .equalTo(Definition.Database.Lesson.LESSON_FIELD_LESSON_ID, lesson.getId())
                            .equalTo(Definition.Database.Lesson.LESSON_FIELD_LEVEL, lesson.getLevel())
                            .equalTo(Definition.Database.Lesson.LESSON_FIELD_CATEGORY, lesson.getCategory())
                            .findAll());
                }
            });
        } finally {
            if (realm != null && !realm.isClosed()) {
                realm.close();
            }
        }

        return logKnowledgeDaoRemembers;
    }

    /**
     * Delete all list remember from Lesson.
     *
     * @param lesson Value.
     */
    public static void deleteAll(final Lesson lesson) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<LogKnowledgeDaoRemember> logKnowledgeDaoRemembers = realm.where(LogKnowledgeDaoRemember.class)
                            .equalTo(Definition.Database.Lesson.LESSON_FIELD_LESSON_ID, lesson.getId())
                            .equalTo(Definition.Database.Lesson.LESSON_FIELD_LEVEL, lesson.getLevel())
                            .equalTo(Definition.Database.Knowledge.KNOWLEDGE_FIELD_CATEGORY, lesson.getCategory())
                            .findAll();

                    if (!logKnowledgeDaoRemembers.isEmpty()) {
                        logKnowledgeDaoRemembers.deleteAllFromRealm();
                    }
                }
            });
        } finally {
            if (realm != null && !realm.isClosed()) {
                realm.close();
            }
        }
    }

    /**
     * Method using check object is exit to array.
     */
    public static boolean exitsValue(Realm realm, Long id) {
        RealmResults<LogKnowledgeDaoRemember> logKnowledgeDetailDaos = realm.where(LogKnowledgeDaoRemember.class).findAll();

        if (!logKnowledgeDetailDaos.isEmpty()) {
            for (LogKnowledgeDaoRemember logKnowledgeDetailDao : logKnowledgeDetailDaos) {
                if (logKnowledgeDetailDao.id.equals(id)) {
                    return true;
                }
            }
        }

        return false;
    }
}
