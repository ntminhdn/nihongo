package com.honkidenihongo.pre.dac;

import android.util.Log;

import com.honkidenihongo.pre.dac.dao.ChoiceDao;
import com.honkidenihongo.pre.dac.dao.GrammarDao;
import com.honkidenihongo.pre.dac.dao.KnowledgeDao;
import com.honkidenihongo.pre.dac.dao.LessonDao;
import com.honkidenihongo.pre.dac.dao.QuestionDao;
import com.honkidenihongo.pre.model.Lesson;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by datpt on 7/6/16.
 */
public class S06LessonList_Dac {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S06LessonList_Dac.class.getName();

    private Realm realm;

    /**
     * The constructor.
     *
     * @param realm The Realm object.
     */
    public S06LessonList_Dac(Realm realm) {
        this.realm = realm;
    }

    //region Create Tables.

    public void createChoice(ChoiceDao choiceDao) {
        // Bắt đầu Transaction.
        realm.beginTransaction();

        try {
            // Lưu thử vào trong Database.
            realm.copyToRealmOrUpdate(choiceDao);

            // Nếu không vấn đề gì thì Commit Transaction.
            realm.commitTransaction();
        } catch (Exception ex) {
            // Lỗi có lỗi thì roll-back Transaction.
            Log.d(LOG_TAG, ex.getMessage());
            realm.cancelTransaction();
        }
    }

    public void createKnowledge(KnowledgeDao knowledgeDao) {
        // Bắt đầu Transaction.
        realm.beginTransaction();

        try {
            // Lưu thử vào trong Database.
            realm.copyToRealmOrUpdate(knowledgeDao);

            // Nếu không vấn đề gì thì Commit Transaction.
            realm.commitTransaction();
        } catch (Exception ex) {
            // Lỗi có lỗi thì roll-back Transaction.
            Log.d(LOG_TAG, ex.getMessage());
            realm.cancelTransaction();
        }
    }

    public void createGrammar(GrammarDao grammarDao) {
        // Bắt đầu Transaction.
        realm.beginTransaction();

        try {
            // Lưu thử vào trong Database.
            realm.copyToRealmOrUpdate(grammarDao);

            // Nếu không vấn đề gì thì Commit Transaction.
            realm.commitTransaction();
        } catch (Exception ex) {
            // Lỗi có lỗi thì roll-back Transaction.
            Log.d(LOG_TAG, ex.getMessage());
            realm.cancelTransaction();
        }
    }

    public void createLesson(LessonDao lessonDao) {
        // Bắt đầu Transaction.
        realm.beginTransaction();

        try {
            // Lưu thử vào trong Database.
            realm.copyToRealmOrUpdate(lessonDao);

            // Nếu không vấn đề gì thì Commit Transaction.
            realm.commitTransaction();
        } catch (Exception ex) {
            // Lỗi có lỗi thì roll-back Transaction.
            Log.d(LOG_TAG, ex.getMessage());
            realm.cancelTransaction();
        }
    }

    public void createQuestion(QuestionDao questionDao) {
        // Bắt đầu Transaction.
        realm.beginTransaction();

        try {
            // Lưu thử vào trong Database.
            realm.copyToRealmOrUpdate(questionDao);

            // Nếu không vấn đề gì thì Commit Transaction.
            realm.commitTransaction();
        } catch (Exception ex) {
            // Lỗi có lỗi thì roll-back Transaction.
            Log.d(LOG_TAG, ex.getMessage());
            realm.cancelTransaction();
        }
    }

    //endregion

    /**
     * Phương thức convert object from LessonDao to Lesson.
     *
     * @return List Lesson.
     */
    public List<Lesson> readLessonList() {
        // Khởi tạo.
        final List<Lesson> lessonList = new ArrayList<>();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<LessonDao> realmLessonDaos = realm.where(LessonDao.class).findAll();

                    for (LessonDao lessonDao : realmLessonDaos) {
                        Lesson lesson = new Lesson();
                        lesson.setId(lessonDao.id);
                        lesson.setType(lessonDao.type);
                        lesson.setNumber(lessonDao.number);
                        lesson.setDescription(lessonDao.description);
                        lesson.setVersion(lessonDao.version);
                        lesson.setTitle_vi(lessonDao.title_vi);
                        lesson.setTitle_en(lessonDao.title_en);
                        lesson.setTitle_ja(lessonDao.title_ja);

                        lessonList.add(lesson);
                    }
                }
            });
        } finally {
            if (realm != null && !realm.isClosed()) {
                realm.close();
            }
        }

        return lessonList;
    }

    /**
     * Lấy danh sách Lesson Id List từ Database.
     *
     * @return Danh sách Lesson Id List.
     */
    public List<Long> getLessonIdList() {
        // Khởi tạo.
        final List<Long> lessonIdList = new ArrayList<>();

        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    // Lấy danh sách đầy đủ Lesson List từ Database.
                    RealmResults<LessonDao> realmLessonDaos = realm.where(LessonDao.class).findAll();

                    // Lọc chỉ lấy Lesson Id.
                    for (LessonDao lessonDao : realmLessonDaos) {
                        lessonIdList.add(lessonDao.id);
                    }
                }
            });
        } finally {
            if (realm != null && !realm.isClosed()) {
                realm.close();
            }
        }

        return lessonIdList;
    }
}
