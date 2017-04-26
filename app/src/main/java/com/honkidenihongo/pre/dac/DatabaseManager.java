package com.honkidenihongo.pre.dac;

import android.util.Log;

import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.model.PracticeData;
import com.honkidenihongo.pre.model.Course;
import com.honkidenihongo.pre.model.ExamLog;
import com.honkidenihongo.pre.model.Knowledge;
import com.honkidenihongo.pre.model.PracticeCoin;
import com.honkidenihongo.pre.model.Question;
import com.honkidenihongo.pre.model.Result;
import com.honkidenihongo.pre.model.TimeLog;
import com.honkidenihongo.pre.model.Unit;
import com.honkidenihongo.pre.model.UnitData;
import com.honkidenihongo.pre.model.UnitDataProgressLog;
import com.honkidenihongo.pre.model.UnitProgressLog;
import com.honkidenihongo.pre.common.util.MathUtil;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmAsyncTask;

/**
 * Created by datpt on 7/6/16.
 */
public class DatabaseManager {

    private static final String LOG_TAG = DatabaseManager.class.getSimpleName();

    public interface ChangeDatabaseCallback {
        void onSuccess(long id);

        void onError(Throwable error);
    }

    /**
     * Created database (course table, knowledge table and question table)
     *
     * @param course     Course to store database
     * @param knowledges List knowledge to store database
     * @param questions  List questions to store database
     * @return Return task to store database
     */
    public static RealmAsyncTask createdCourseDatabase(final Realm realm, final Course course, final List<Knowledge> knowledges
            , final List<Question> questions) {
        if (realm == null) {
            return null;
        }
        if (course != null) {
            return realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    // Create new course in course table
                    Course newCourse = realm.createObject(Course.class);
                    newCourse.name = course.getName();
                    newCourse.short_name = course.getShort_name();
                    newCourse.version = course.getVersion();
                    newCourse.icon = course.getIcon();
                    newCourse.data_directory = course.getData_directory();
                    newCourse.id = course.getId();
                    if (course.getUnits() != null) {
                        for (Unit unit : course.getUnits()) {
                            Unit newUnit = realm.createObject(Unit.class);
                            newUnit.id = PrimaryKeyFactory.getInstance().nextKey(Unit.class);
                            newUnit.course_id = course.getId();
                            newUnit.unit_id = unit.getUnit_id();
                            newUnit.name = unit.getName();
                            if (unit.getDatas() != null) {
                                for (UnitData data : unit.getDatas()) {
                                    UnitData newData = realm.createObject(UnitData.class);
                                    newData.id = PrimaryKeyFactory.getInstance().nextKey(UnitData.class);
                                    newData.course_id = course.getId();
                                    newData.unit_id = unit.getUnit_id();
                                    newData.name = data.getName();
                                    newData.level = data.getLevel();
                                    newData.lesson_number = data.getLesson_number();
                                    newData.lesson_name = data.getLesson_name();
                                    newData.icon = data.getIcon();
                                    newData.type = data.getType();
                                    newData.title = data.getTitle();
                                    if (data.getDatas() != null) {
                                        for (PracticeData practiceData : data.getDatas()) {
                                            PracticeData newPracticeData = realm.createObject(PracticeData.class);
                                            newPracticeData.id = PrimaryKeyFactory.getInstance().nextKey(PracticeData.class);
                                            newPracticeData.course_id = course.getId();
                                            newPracticeData.unit_id = unit.getUnit_id();
                                            newPracticeData.module_id = practiceData.getModule_id();
                                            newPracticeData.module = practiceData.getModule();
                                            newPracticeData.range = practiceData.getRange();
                                            newData.datas.add(newPracticeData);
                                        }
                                    }
                                    newUnit.datas.add(newData);
                                    if (data.getName().equalsIgnoreCase(Definition.Constants.TYPE_IMPROVE_KNOWLEDGE)) {
                                        UnitData flashCardData = realm.createObject(UnitData.class);
                                        flashCardData.id = PrimaryKeyFactory.getInstance().nextKey(UnitData.class);
                                        flashCardData.course_id = course.getId();
                                        flashCardData.unit_id = unit.getUnit_id();
                                        flashCardData.name = Definition.General.FLASHCARD;
                                        flashCardData.lesson_number = data.getLesson_number();
                                        flashCardData.lesson_name = data.getLesson_name();
                                        flashCardData.level = data.getLevel();
                                        flashCardData.icon = data.getIcon();
                                        flashCardData.type = data.getType();
                                        flashCardData.title = Definition.Constants.FLASHCARD;
                                        if (data.getDatas() != null) {
                                            for (PracticeData practiceData : data.getDatas()) {
                                                PracticeData newPracticeData = realm.createObject(PracticeData.class);
                                                newPracticeData.id = PrimaryKeyFactory.getInstance().nextKey(PracticeData.class);
                                                newPracticeData.course_id = course.getId();
                                                newPracticeData.unit_id = unit.getUnit_id();
                                                newPracticeData.module_id = practiceData.getModule_id();
                                                newPracticeData.module = practiceData.getModule();
                                                newPracticeData.range = practiceData.getRange();
                                                flashCardData.datas.add(newPracticeData);
                                            }
                                        }
                                        newUnit.datas.add(flashCardData);
                                    }
                                }
                            }
                            newCourse.units.add(newUnit);
                        }
                    }
                    createdKnowledgeTable(realm, knowledges, newCourse);
                    createdQuestionsTable(realm, questions, newCourse);
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Log.d(LOG_TAG, "Create data success!");
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    Log.e(LOG_TAG, "Create database error!");
                }
            });
        }

        return null;
    }

    /**
     * Create knowledge table from list Knowledge object
     *
     * @param realm      Realm object to interactive with database
     * @param knowledges List knowledge to store to Knowledge table in database
     * @param course     Course contain knowledge to store database
     */
    private static void createdKnowledgeTable(Realm realm, final List<Knowledge> knowledges, final Course course) {
        if (realm == null) {
            return;
        }
        if (knowledges != null && knowledges.size() > 0) {
            for (Knowledge knowledge : knowledges) {
                Knowledge newKnowledge = realm.createObject(Knowledge.class);
                newKnowledge.id = PrimaryKeyFactory.getInstance().nextKey(Knowledge.class);
                newKnowledge.knowledge_id = knowledge.getKnowledge_id();
                newKnowledge.format = knowledge.getFormat();
                newKnowledge.data = knowledge.getData();
                newKnowledge.is_remember = false;
                newKnowledge.course = course;
            }

        }
    }

    private static void createdQuestionsTable(Realm realm, final List<Question> questions, final Course course) {
        if (realm == null) {
            return;
        }
        if (questions == null || questions.size() == 0) {
            return;
        }

        for (Question question : questions) {
            Question newQuestion = realm.createObject(Question.class);
            newQuestion.id = PrimaryKeyFactory.getInstance().nextKey(Question.class);
            newQuestion.question_id = question.getQuestion_id();
            newQuestion.question_content = question.getQuestion_content();
            newQuestion.answers = question.getAnswers();
            newQuestion.audio = question.getAudio();
            newQuestion.course = course;
        }
    }

    public static RealmAsyncTask createHistoryTable(
            Realm realm, final long course_id, final long unit_id, final long module_id, final long user_id,
            final String type, final List<Result> listResult, final ChangeDatabaseCallback callback) {
        if (realm == null) {
            return null;
        }
        if (listResult != null && listResult.size() > 0) {
            final long id = PrimaryKeyFactory.getInstance().nextKey(ExamLog.class);
            return realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    int correct_count = 0;
                    float total_duration = 0;
                    ExamLog newExamLog = realm.createObject(ExamLog.class);
                    newExamLog.id = id;
                    newExamLog.course_id = course_id;
                    newExamLog.unit_id = unit_id;
                    newExamLog.module_id = module_id;
                    newExamLog.user_id = user_id;
                    newExamLog.type = type;
                    newExamLog.create_at = new Date();
                    newExamLog.is_send = false;
                    float alphat = (float) (300 / (2.9 * 2.9));
                    float correct_score = 0;
                    float bonus_score = 0;
                    for (Result result : listResult) {
                        if (result.is_correct()) {
                            correct_count++;
                            correct_score += 20;
                            bonus_score += alphat * (3 - result.getTime_complete()) * (3 - result.getTime_complete());
                        }
                        total_duration += result.getTime_complete();
                        Result newResult = realm.createObject(Result.class);
                        newResult.id = PrimaryKeyFactory.getInstance().nextKey(Result.class);
                        newResult.question_id = result.getQuestion_id();
                        newResult.question = result.getQuestion();
                        newResult.answer = result.getAnswer();
                        newResult.is_audio = result.is_audio();
                        newResult.audio_data = result.getAudio_data();
                        newResult.is_correct = result.is_correct();
                        newResult.time_complete = result.getTime_complete();
                        newResult.create_at = new Date();

                        newExamLog.questions.add(newResult);
                    }
                    newExamLog.correctness_ratio = MathUtil.round((((double) correct_count) / listResult.size()), 2);
                    newExamLog.total_duration = total_duration;
                    newExamLog.question_count = listResult.size();
                    newExamLog.score = correct_score + bonus_score;
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    if (callback != null) {
                        callback.onSuccess(id);
                    }
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    if (callback != null) {
                        callback.onError(error);
                    }
                }
            });
        }

        return null;
    }

    public static RealmAsyncTask createLogDatabase(
            Realm realm, final List<UnitProgressLog> unitProgressLogList, final List<UnitDataProgressLog> unitDataProgressLogs,
            final List<ExamLog> examLogList, final List<TimeLog> timeLogList, final List<PracticeCoin> practiceCoinList, final ChangeDatabaseCallback callback) {
        if (realm == null) {
            return null;
        }
        if ((unitDataProgressLogs == null || unitDataProgressLogs.size() == 0) && (examLogList == null || examLogList.size() == 0) &&
                (unitProgressLogList == null || unitProgressLogList.size() == 0) && (timeLogList == null || timeLogList.size() == 0) &&
                (practiceCoinList == null || practiceCoinList.size() == 0)) {
            return null;
        }

        return realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // Insert rows to UnitProgressLog table
                if (unitProgressLogList != null && unitProgressLogList.size() > 0) {
                    for (UnitProgressLog unitProgressLog : unitProgressLogList) {
                        UnitProgressLog newUnitProgressLog = realm.createObject(UnitProgressLog.class);
                        newUnitProgressLog.id = PrimaryKeyFactory.getInstance().nextKey(UnitProgressLog.class);
                        newUnitProgressLog.unit_id = unitProgressLog.getUnit_id();
                        newUnitProgressLog.progress = unitProgressLog.getProgress();
                        newUnitProgressLog.is_send = unitProgressLog.is_send();
                    }
                }

                // Insert rows to UnitDataProgressLog table
                if (unitDataProgressLogs != null && unitDataProgressLogs.size() > 0) {
                    for (UnitDataProgressLog unitDataProgressLog : unitDataProgressLogs) {
                        UnitDataProgressLog newUnitDataProgressLog = realm.createObject(UnitDataProgressLog.class);
                        newUnitDataProgressLog.id = PrimaryKeyFactory.getInstance().nextKey(UnitDataProgressLog.class);
                        newUnitDataProgressLog.unit_id = unitDataProgressLog.getUnit_id();
                        newUnitDataProgressLog.progress = unitDataProgressLog.getProgress();
                        newUnitDataProgressLog.type = unitDataProgressLog.getType();
                        newUnitDataProgressLog.is_send = unitDataProgressLog.is_send();
                    }
                }

                // Insert rows to ExamLog table
                if (examLogList != null && examLogList.size() > 0) {
                    for (ExamLog examLog : examLogList) {
                        ExamLog newExamLog = realm.createObject(ExamLog.class);
                        newExamLog.id = PrimaryKeyFactory.getInstance().nextKey(ExamLog.class);
                        newExamLog.course_id = examLog.getCourse_id();
                        newExamLog.unit_id = examLog.getUnit_id();
                        newExamLog.module_id = examLog.getModule_id();
                        newExamLog.type = examLog.getType();
                        newExamLog.create_at = examLog.getCreate_at();
                        newExamLog.correctness_ratio = examLog.getCorrectness_ratio();
                        newExamLog.total_duration = examLog.getTotal_duration();
                        newExamLog.question_count = examLog.getQuestion_count();
                        newExamLog.score = examLog.getScore();
                        newExamLog.is_send = examLog.is_send();
                    }
                }

                // Insert rows to TimeLog table
                if (timeLogList != null && timeLogList.size() > 0) {
                    for (TimeLog timeLog : timeLogList) {
                        TimeLog newTimeLog = realm.createObject(TimeLog.class);
                        newTimeLog.id = PrimaryKeyFactory.getInstance().nextKey(TimeLog.class);
                        newTimeLog.course_id = timeLog.getCourse_id();
                        newTimeLog.unit_id = timeLog.getUnit_id();
                        newTimeLog.type = timeLog.getType();
                        newTimeLog.start = timeLog.getStart();
                        newTimeLog.end = timeLog.getEnd();
                        newTimeLog.is_send = timeLog.is_send();
                    }
                }

                // Insert rows to PracticeCoin table
                if (practiceCoinList != null && practiceCoinList.size() > 0) {
                    for (PracticeCoin practiceCoin : practiceCoinList) {
                        PracticeCoin newPracticeCoin = realm.createObject(PracticeCoin.class);
                        newPracticeCoin.id = PrimaryKeyFactory.getInstance().nextKey(PracticeCoin.class);
                        newPracticeCoin.course_id = practiceCoin.getCourse_id();
                        newPracticeCoin.unit_id = practiceCoin.getUnit_id();
                        newPracticeCoin.module_id = practiceCoin.getModule_id();
                        newPracticeCoin.coin = practiceCoin.getCoin();
                        newPracticeCoin.perfect_result = practiceCoin.getPerfect_result();
                        newPracticeCoin.last_up_coin = practiceCoin.getLast_up_coin();
                        newPracticeCoin.is_send = practiceCoin.is_send();
                    }
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSuccess(0);
                }
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }
}
