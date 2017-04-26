package com.honkidenihongo.pre.dac;

import android.content.Context;
import android.util.Log;

import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.IoUtil;
import com.honkidenihongo.pre.model.PracticeData;
import com.honkidenihongo.pre.model.Course;
import com.honkidenihongo.pre.model.ExamLog;
import com.honkidenihongo.pre.model.Knowledge;
import com.honkidenihongo.pre.model.PracticeCoin;
import com.honkidenihongo.pre.model.Question;
import com.honkidenihongo.pre.model.TimeLog;
import com.honkidenihongo.pre.model.Unit;
import com.honkidenihongo.pre.model.UnitData;
import com.honkidenihongo.pre.model.UnitDataProgressLog;
import com.honkidenihongo.pre.model.UnitProgressLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by datpt on 7/7/16.
 * <p/>
 * Class used to parser data from json to object
 */
public class DatabaseParser {

    private static final String LOG_TAG = DatabaseParser.class.getSimpleName();

    private String mDataDirectory;
    private Context mContext;

    public DatabaseParser(Context context, String dataDirectory) {
        mContext = context;
        if (dataDirectory.endsWith("/")) {
            mDataDirectory = dataDirectory;
        } else {
            mDataDirectory = dataDirectory + "/";
        }
    }

    /**
     * Parser question data from question.json file to List object
     *
     * @return List questions
     */
    public List<Question> parserQuestion() {
        // Get knowledge file path
        String questionFilePath = mDataDirectory + Definition.FileData.QUESTION_FILE;

        // Read file to string
        String questionJSONString = IoUtil.readFileFromInternalStorageToString(mContext, questionFilePath);
        Log.d(LOG_TAG, questionJSONString + "");

        if (questionJSONString == null || questionJSONString.isEmpty()) {
            return null;
        }

        // JSON parser
        try {
            JSONObject questionJSONObject = new JSONObject(questionJSONString);
            Iterator<?> keys = questionJSONObject.keys();
            List<Question> listQuestions = new ArrayList<>();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                JSONObject jsonObject = questionJSONObject.getJSONObject(key);
                Question newQuestion = new Question();
                newQuestion.setQuestion_id(Integer.parseInt(key));
                if (jsonObject.has(Definition.JSON.QUESTION_KEY)) {
                    newQuestion.setQuestion_content(jsonObject.getString(Definition.JSON.QUESTION_KEY));
                }
                if (jsonObject.has(Definition.JSON.AUDIO_KEY)) {
                    newQuestion.setAudio(jsonObject.getString(Definition.JSON.AUDIO_KEY));
                }
                if (jsonObject.has(Definition.JSON.ANSWERS_KEY)) {
                    newQuestion.setAnswers(jsonObject.getString(Definition.JSON.ANSWERS_KEY));
                }
                listQuestions.add(newQuestion);
            }
            return listQuestions;
        } catch (JSONException je) {
            je.printStackTrace();
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }

        return null;
    }

    /**
     * Parser knowledge data from knowledge.json file to List object
     *
     * @return List knowledge
     */
    public List<Knowledge> parserKnowledge() {
        // Get knowledge file path
        String knowledgeFilePath = mDataDirectory + Definition.FileData.KNOWLEDGE_FILE;

        // Read file to string
        String knowledgeJSONString = IoUtil.readFileFromInternalStorageToString(mContext, knowledgeFilePath);
        Log.d(LOG_TAG, knowledgeJSONString + "");

        if (knowledgeJSONString == null || knowledgeJSONString.isEmpty()) {
            return null;
        }

        // JSON parser
        try {
            JSONObject knowledgeJSONObject = new JSONObject(knowledgeJSONString);
            Iterator<?> keys = knowledgeJSONObject.keys();
            List<Knowledge> listKnowledges = new ArrayList<>();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                JSONObject jsonObject = knowledgeJSONObject.getJSONObject(key);
                Knowledge knowledge = new Knowledge();
                knowledge.setKnowledge_id(Integer.parseInt(key));
                if (jsonObject.has(Definition.JSON.DATA_KEY)) {
                    knowledge.setData(jsonObject.getString(Definition.JSON.DATA_KEY));
                }
                if (jsonObject.has(Definition.JSON.FORMAT_KEY)) {
                    knowledge.setFormat(jsonObject.getString(Definition.JSON.FORMAT_KEY));
                }
                listKnowledges.add(knowledge);
            }
            return listKnowledges;
        } catch (JSONException je) {
            je.printStackTrace();
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }

        return null;
    }

    /**
     * Parser course data from main.json and unit.json file to Course object
     *
     * @return Course object
     */
    public Course parserCourse(long course_id) {
        // Get json file path
        String mainFilePath = mDataDirectory + Definition.FileData.MAIN_FILE;
        String unitFilePath = mDataDirectory + Definition.FileData.UNIT_FILE;

        // Read json file to String
        String mainJSONString = IoUtil.readFileFromInternalStorageToString(mContext, mainFilePath);
        Log.d(LOG_TAG, mainJSONString + "");
        String unitJSONString = IoUtil.readFileFromInternalStorageToString(mContext, unitFilePath);
        Log.d(LOG_TAG, unitJSONString + "");

        if (mainJSONString == null || mainJSONString.isEmpty()) {
            return null;
        }

        // Parser all json
        try {
            JSONObject mainJSONObject = new JSONObject(mainJSONString);
            JSONObject unitJSONObject = new JSONObject(unitJSONString);

            // Created course object
            Course course = new Course();
            course.setId(course_id);
            course.setData_directory(mDataDirectory);

            // Parser Course
            if (mainJSONObject.has(Definition.JSON.NAME_KEY)) {
                course.setName(mainJSONObject.getString(Definition.JSON.NAME_KEY));
            }
            if (mainJSONObject.has(Definition.JSON.VERSION_KEY)) {
                course.setVersion(mainJSONObject.getString(Definition.JSON.VERSION_KEY));
            }
            if (mainJSONObject.has(Definition.JSON.SHORT_NAME_KEY)) {
                course.setShort_name(mainJSONObject.getString(Definition.JSON.SHORT_NAME_KEY));
            }
            if (mainJSONObject.has(Definition.JSON.ICON_KEY)) {
                course.setIcon(mDataDirectory + mainJSONObject.getString(Definition.JSON.ICON_KEY));
            }

            if (mainJSONObject.has(Definition.JSON.TYPE_KEY)) {
                Log.d("xxx", "parserCourse: " + mainJSONObject.toString());
                String type = mainJSONObject.getString(Definition.JSON.TYPE_KEY);
                if (type.equals(Definition.JSON.UNIT)) {
                    List<Unit> unitList = parseCourseUnit(unitJSONObject, course_id);
                    for (Unit unit : unitList) {
                        course.addUnit(unit);
                    }

                    return course;
                }
            }

            List<Unit> unitList = parseCourseCharacter(unitJSONObject, course_id);
            for (Unit unit : unitList) {
                course.addUnit(unit);
            }

            return course;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Unit> parseCourseCharacter(JSONObject unitJSONObject, long course_id) throws JSONException {
        List<Unit> arrayUnit = new ArrayList<>();
        Iterator<?> unitKeys = unitJSONObject.keys();
        while (unitKeys.hasNext()) {
            String unitId = (String) unitKeys.next();
            JSONObject unitJson = unitJSONObject.getJSONObject(unitId);
            String nameUnit = unitJson.getString(Definition.Database.FIELD_NAME);
            Unit unit = new Unit();
            unit.setUnit_id(Long.parseLong(unitId));
            unit.setName(nameUnit);
            unit.setCourse_id(course_id);
            Iterator<?> uniDatakeys = unitJson.keys();

            while (uniDatakeys.hasNext()) {
                String unitDatakey = (String) uniDatakeys.next();
                if (unitDatakey.equals(Definition.Database.FIELD_NAME))
                    continue;
                UnitData unitData = parseUnitData(unitJson.getJSONObject(unitDatakey), course_id, Long.parseLong(unitId), unitDatakey);
                unitData.setType(unitDatakey);
                unit.addData(unitData);
            }
            arrayUnit.add(unit);
        }
        return arrayUnit;
    }

    public List<Unit> parseCourseUnit(JSONObject unitJSONObject, long course_id) throws JSONException {

        List<Unit> arrayUnit = new ArrayList<>();
        Iterator<?> unitKeys = unitJSONObject.keys();
        //parse unit
        while (unitKeys.hasNext()) {
            String unitId = (String) unitKeys.next();
            JSONObject unitJson = unitJSONObject.getJSONObject(unitId);
            String nameUnit = unitJson.getString(Definition.Database.FIELD_NAME);
            Unit unit = new Unit();
            unit.setUnit_id(Long.parseLong(unitId));
            unit.setName(nameUnit);
            unit.setCourse_id(course_id);
            Iterator<?> lessonKeys = unitJson.keys();
            while (lessonKeys.hasNext()) {
                String lesson_number = (String) lessonKeys.next();
                if (lesson_number.equals(Definition.Database.FIELD_NAME))
                    continue;
                int lessonId = Integer.parseInt(lesson_number);
                JSONObject lessonObject = unitJson.getJSONObject(lesson_number);
                String type = "";
                try {
                    type = lessonObject.getString(Definition.Database.FIELD_TYPE_LESSON);

                } catch (Exception e) {
                    Log.e(LOG_TAG, "----");
                }
                if (type.equals(Definition.Database.FIELD_TEST)) {
                    UnitData unitData = parseUnitData(lessonObject, course_id, Long.parseLong(unitId), type, lessonId, "", "");
                    unitData.setType(type);
                    unitData.setName(type);
                    unit.addData(unitData);
                    continue;
                } else {
                    Iterator<?> levelKeys = lessonObject.keys();
                    String nameLesson = lessonObject.getString(Definition.Database.FIELD_NAME);
                    while (levelKeys.hasNext()) {
                        String level = (String) levelKeys.next();
                        if (level.equals(Definition.Database.FIELD_NAME))
                            continue;

                        JSONObject partJsonObject = lessonObject.getJSONObject(level);
                        Iterator<?> partkeys = partJsonObject.keys();
                        while (partkeys.hasNext()) {
                            String partkey = (String) partkeys.next();
                            if (partkey.equals(Definition.Database.FIELD_NAME))
                                continue;
                            UnitData unitData = parseUnitData(partJsonObject.getJSONObject(partkey), course_id, Long.parseLong(unitId), nameLesson, lessonId, partJsonObject.getString("name"), partkey);
                            unitData.setType(partkey);
                            unit.addData(unitData);
                        }
                    }
                }
            }
            arrayUnit.add(unit);
        }
        return arrayUnit;
    }

    public UnitData parseUnitData(JSONObject uniDataJson, long course_id, long unit_id, String title) throws JSONException {
        return parseUnitData(uniDataJson, course_id, unit_id, "", 0, "", title);
    }

    public UnitData parseUnitData(JSONObject uniDataJson, long course_id, long unit_id, String lessonName, int lessonNumber, String levelName, String title) throws JSONException {
        UnitData unitData = new UnitData();
        unitData.setCourse_id(course_id);
        //unitData.setName(nameUnitData);
        unitData.setUnit_id(unit_id);
        unitData.setLesson_name(lessonName);
        unitData.setLesson_number(lessonNumber);
        unitData.setLevel(levelName);
        unitData.setTitle(title);

        if (uniDataJson.has(Definition.JSON.CONTENTS_KEY)) {
            JSONObject contentDataJsonObject = uniDataJson.getJSONObject(Definition.JSON.CONTENTS_KEY);
            Iterator<?> contentKeys = contentDataJsonObject.keys();
            while (contentKeys.hasNext()) {
                String contentKey = (String) contentKeys.next();
                JSONObject data = contentDataJsonObject.getJSONObject(contentKey);
                PracticeData practiceData = new PracticeData();
                practiceData.setCourse_id(course_id);
                practiceData.setUnit_id(Long.parseLong(contentKey));
                practiceData.setModule_id(Long.parseLong(contentKey));
                if (data.has(Definition.JSON.MODULE_KEY)) {
                    practiceData.setModule(data.getString(Definition.JSON.MODULE_KEY));
                }
                if (data.has(Definition.JSON.RANGE_KEY)) {
                    practiceData.setRange(data.getString(Definition.JSON.RANGE_KEY));
                }
                unitData.addData(practiceData);
            }
        }
        return unitData;
    }

    public List<UnitProgressLog> parserUnitProgress() {
        // Get knowledge file path
        String unitProgressFilePath = mDataDirectory + Definition.FileData.UNIT_PROGRESS_FILE;

        // Read file to string
        String unitProgressJSONString = IoUtil.readFileFromInternalStorageToString(mContext, unitProgressFilePath);
        Log.d(LOG_TAG, "UnitProgressFilePath: " + unitProgressFilePath);
        Log.d(LOG_TAG, "UnitProgressLog: " + unitProgressJSONString);

        if (unitProgressJSONString == null || unitProgressJSONString.isEmpty()) {
            return null;
        }
        // JSON parser
        try {
            JSONArray unitProgressJSONArray = new JSONArray(unitProgressJSONString);
            List<UnitProgressLog> listUnitProgresLogs = new ArrayList<>();
            for (int i = 0; i < unitProgressJSONArray.length(); i++) {
                JSONObject jsonObject = unitProgressJSONArray.getJSONObject(i);
                UnitProgressLog newUnitProgressLog = new UnitProgressLog();
                newUnitProgressLog.setIs_send(true);
                if (jsonObject.has(Definition.JSON.UNIT_ID_KEY)) {
                    newUnitProgressLog.setUnit_id(jsonObject.getLong(Definition.JSON.UNIT_ID_KEY));
                }
                if (jsonObject.has(Definition.JSON.PROGRESS_KEY)) {
                    newUnitProgressLog.setProgress(jsonObject.getDouble(Definition.JSON.PROGRESS_KEY));
                }
                listUnitProgresLogs.add(newUnitProgressLog);
            }
            return listUnitProgresLogs;
        } catch (JSONException je) {
            je.printStackTrace();
        }

        return null;
    }

    public List<UnitDataProgressLog> parserUnitDataProgress() {
        // Get knowledge file path
        String unitDataProgressFilePath = mDataDirectory + Definition.FileData.UNIT_CONTENT_PROGRESS_FILE;

        // Read file to string
        String unitDataProgressJSONString = IoUtil.readFileFromInternalStorageToString(mContext, unitDataProgressFilePath);
        Log.d(LOG_TAG, "UnitDataProgressFilePath: " + unitDataProgressFilePath);
        Log.d(LOG_TAG, "UnitDataProgressLog: " + unitDataProgressJSONString);

        if (unitDataProgressJSONString == null || unitDataProgressJSONString.isEmpty()) {
            return null;
        }
        // JSON parser
        try {
            JSONArray unitDataProgressJSONArray = new JSONArray(unitDataProgressJSONString);
            List<UnitDataProgressLog> listUnitDataProgresLogs = new ArrayList<>();
            for (int i = 0; i < unitDataProgressJSONArray.length(); i++) {
                JSONObject jsonObject = unitDataProgressJSONArray.getJSONObject(i);
                UnitDataProgressLog newUnitDataProgressLog = new UnitDataProgressLog();
                newUnitDataProgressLog.setIs_send(true);
                if (jsonObject.has(Definition.JSON.UNIT_ID_KEY)) {
                    newUnitDataProgressLog.setUnit_id(jsonObject.getLong(Definition.JSON.UNIT_ID_KEY));
                }
                if (jsonObject.has(Definition.JSON.CONTENT_TYPE_KEY)) {
                    newUnitDataProgressLog.setType(getType(jsonObject.getInt(Definition.JSON.CONTENT_TYPE_KEY)));
                }
                if (jsonObject.has(Definition.JSON.PROGRESS_KEY)) {
                    newUnitDataProgressLog.setProgress(jsonObject.getDouble(Definition.JSON.PROGRESS_KEY));
                }
                listUnitDataProgresLogs.add(newUnitDataProgressLog);
            }
            return listUnitDataProgresLogs;
        } catch (JSONException je) {
            je.printStackTrace();
        }

        return null;
    }

    public List<ExamLog> parserExamLog() {
        // Get knowledge file path
        String practiceFilePath = mDataDirectory + Definition.FileData.PRACTICE_FILE;
        String testFilePath = mDataDirectory + Definition.FileData.TEST_FILE;

        // Read file to string
        String praticeJSONString = IoUtil.readFileFromInternalStorageToString(mContext, practiceFilePath);
        String testJSONString = IoUtil.readFileFromInternalStorageToString(mContext, testFilePath);
        Log.d(LOG_TAG, "TestLogFilePath: " + testFilePath);
        Log.d(LOG_TAG, "TestLog: " + testJSONString);
        Log.d(LOG_TAG, "PracticeLogFilePath: " + practiceFilePath);
        Log.d(LOG_TAG, "PracticeLog: " + praticeJSONString);

        if ((praticeJSONString == null || praticeJSONString.isEmpty())
                && (testJSONString == null || testJSONString.isEmpty())) {
            return null;
        }
        // JSON parser
        try {
            List<ExamLog> listExamLog = new ArrayList<>();
            JSONArray praticeJSONArray = new JSONArray(praticeJSONString);
            for (int i = 0; i < praticeJSONArray.length(); i++) {
                JSONObject jsonObject = praticeJSONArray.getJSONObject(i);
                ExamLog newExamLog = new ExamLog();
                newExamLog.setIs_send(true);
                if (jsonObject.has(Definition.JSON.DATE_KEY)) {
                    newExamLog.setCreate_at(getDate(jsonObject.getString(Definition.JSON.DATE_KEY)));
                }
                if (jsonObject.has(Definition.JSON.QUESTION_COUNT_KEY)) {
                    newExamLog.setQuestion_count(jsonObject.getInt(Definition.JSON.QUESTION_COUNT_KEY));
                }
                if (jsonObject.has(Definition.JSON.CORRECTNESS_RATIO_KEY)) {
                    newExamLog.setCorrectness_ratio((float) jsonObject.getDouble(Definition.JSON.CORRECTNESS_RATIO_KEY));
                }
                if (jsonObject.has(Definition.JSON.TOTAL_DURATION_KEY)) {
                    newExamLog.setTotal_duration((float) jsonObject.getDouble(Definition.JSON.TOTAL_DURATION_KEY));
                }
                if (jsonObject.has(Definition.JSON.TYPE_KEY)) {
                    newExamLog.setType(jsonObject.getString(Definition.JSON.TYPE_KEY));
                }
                if (jsonObject.has(Definition.JSON.COURSE_KEY)) {
                    newExamLog.setCourse_id(jsonObject.getJSONObject(Definition.JSON.COURSE_KEY).getLong(Definition.JSON.ID_KEY));
                }
                if (jsonObject.has(Definition.JSON.UNIT_KEY)) {
                    newExamLog.setUnit_id(jsonObject.getJSONObject(Definition.JSON.UNIT_KEY).getLong(Definition.JSON.ID_KEY));
                }
                if (jsonObject.has(Definition.JSON.MODULE_KEY)) {
                    newExamLog.setModule_id(jsonObject.getJSONObject(Definition.JSON.MODULE_KEY).getLong(Definition.JSON.ID_KEY));
                }
                listExamLog.add(newExamLog);
            }

            JSONArray testJSONArray = new JSONArray(testJSONString);
            for (int i = 0; i < testJSONArray.length(); i++) {
                JSONObject jsonObject = testJSONArray.getJSONObject(i);
                ExamLog newExamLog = new ExamLog();
                newExamLog.setIs_send(true);
                if (jsonObject.has(Definition.JSON.SCORE_KEY)) {
                    newExamLog.setScore((float) jsonObject.getDouble(Definition.JSON.SCORE_KEY));
                }
                if (jsonObject.has(Definition.JSON.DATE_KEY)) {
                    newExamLog.setCreate_at(getDate(jsonObject.getString(Definition.JSON.DATE_KEY)));
                }
                if (jsonObject.has(Definition.JSON.QUESTION_COUNT_KEY)) {
                    newExamLog.setQuestion_count(jsonObject.getInt(Definition.JSON.QUESTION_COUNT_KEY));
                }
                if (jsonObject.has(Definition.JSON.CORRECTNESS_RATIO_KEY)) {
                    newExamLog.setCorrectness_ratio((float) jsonObject.getDouble(Definition.JSON.CORRECTNESS_RATIO_KEY));
                }
                if (jsonObject.has(Definition.JSON.TOTAL_DURATION_KEY)) {
                    newExamLog.setTotal_duration((float) jsonObject.getDouble(Definition.JSON.TOTAL_DURATION_KEY));
                }
                if (jsonObject.has(Definition.JSON.TYPE_KEY)) {
                    newExamLog.setType(jsonObject.getString(Definition.JSON.TYPE_KEY));
                }
                if (jsonObject.has(Definition.JSON.COURSE_KEY)) {
                    newExamLog.setCourse_id(jsonObject.getJSONObject(Definition.JSON.COURSE_KEY).getLong(Definition.JSON.ID_KEY));
                }
                if (jsonObject.has(Definition.JSON.UNIT_KEY)) {
                    newExamLog.setUnit_id(jsonObject.getJSONObject(Definition.JSON.UNIT_KEY).getLong(Definition.JSON.ID_KEY));
                }
                if (jsonObject.has(Definition.JSON.MODULE_KEY)) {
                    newExamLog.setModule_id(jsonObject.getJSONObject(Definition.JSON.MODULE_KEY).getLong(Definition.JSON.ID_KEY));
                }
                listExamLog.add(newExamLog);
            }

            return listExamLog;
        } catch (JSONException je) {
            je.printStackTrace();
        }

        return null;
    }

    public List<TimeLog> parserTimeLog() {
        // Get knowledge file path
        String timeLogFilePath = mDataDirectory + Definition.FileData.KNOWLEDGE_FILE;
        // Read file to string
        String timeLogJSONString = IoUtil.readFileFromInternalStorageToString(mContext, timeLogFilePath);
        Log.d(LOG_TAG, "PracticeCoinFilePath: " + timeLogFilePath);
        Log.d(LOG_TAG, "PracticeCoinLog: " + timeLogJSONString);
        if (timeLogJSONString == null || timeLogJSONString.isEmpty()) {
            return null;
        }
        // JSON parser
        try {
            JSONArray timeLogJSONArray = new JSONArray(timeLogJSONString);
            List<TimeLog> listTimeLog = new ArrayList<>();
            for (int i = 0; i < timeLogJSONArray.length(); i++) {
                JSONObject jsonObject = timeLogJSONArray.getJSONObject(i);
                TimeLog timeLog = new TimeLog();
                timeLog.setIs_send(true);
                if (jsonObject.has(Definition.JSON.START_KEY)) {
                    timeLog.setStart(getDate(jsonObject.getString(Definition.JSON.START_KEY)));
                }
                if (jsonObject.has(Definition.JSON.END_KEY)) {
                    timeLog.setEnd(getDate(jsonObject.getString(Definition.JSON.END_KEY)));
                }
                if (jsonObject.has(Definition.JSON.TYPE_KEY)) {
                    timeLog.setType(jsonObject.getString(Definition.JSON.TYPE_KEY));
                }
                if (jsonObject.has(Definition.JSON.COURSE_KEY)) {
                    timeLog.setCourse_id(jsonObject.getJSONObject(Definition.JSON.COURSE_KEY).getLong(Definition.JSON.ID_KEY));
                }
                if (jsonObject.has(Definition.JSON.UNIT_KEY)) {
                    timeLog.setUnit_id(jsonObject.getJSONObject(Definition.JSON.UNIT_KEY).getLong(Definition.JSON.ID_KEY));
                }
                listTimeLog.add(timeLog);
            }
            return listTimeLog;
        } catch (JSONException je) {
            je.printStackTrace();
        }

        return null;
    }

    public List<PracticeCoin> parserRank() {
        // Get knowledge file path
        String practiceCoinFilePath = mDataDirectory + Definition.FileData.PRACTICE_RANKING_FILE;

        // Read file to string
        String practiceCoinJSONString = IoUtil.readFileFromInternalStorageToString(mContext, practiceCoinFilePath);
        Log.d(LOG_TAG, "KnowledgeLogFilePath: " + practiceCoinFilePath);
        Log.d(LOG_TAG, "KnowledgeLog: " + practiceCoinJSONString);

        if (practiceCoinJSONString == null || practiceCoinJSONString.isEmpty()) {
            return null;
        }

        // JSON parser
        try {
            JSONArray practiceCoinJSONArray = new JSONArray(practiceCoinJSONString);
            List<PracticeCoin> practiceCoinList = new ArrayList<>();
            for (int i = 0; i < practiceCoinJSONArray.length(); i++) {
                JSONObject jsonObject = practiceCoinJSONArray.getJSONObject(i);
                PracticeCoin practiceCoin = new PracticeCoin();
                practiceCoin.setIs_send(true);
                if (jsonObject.has(Definition.JSON.COURSE_ID_KEY)) {
                    practiceCoin.setCourse_id(jsonObject.getLong(Definition.JSON.COURSE_ID_KEY));
                }
                if (jsonObject.has(Definition.JSON.UNIT_ID_KEY)) {
                    practiceCoin.setUnit_id(jsonObject.getLong(Definition.JSON.UNIT_ID_KEY));
                }
                if (jsonObject.has(Definition.JSON.PRACTICE_ID_KEY)) {
                    practiceCoin.setModule_id(jsonObject.getLong(Definition.JSON.PRACTICE_ID_KEY));
                }
                if (jsonObject.has(Definition.JSON.COIN_KEY)) {
                    practiceCoin.setCoin(jsonObject.getInt(Definition.JSON.COIN_KEY));
                }
                if (jsonObject.has(Definition.JSON.PERFECTION_KEY)) {
                    practiceCoin.setPerfect_result(jsonObject.getInt(Definition.JSON.PERFECTION_KEY));
                }
                if (jsonObject.has(Definition.JSON.LAST_UP_COIN_KEY)) {
                    practiceCoin.setLast_up_coin(getDate(jsonObject.getString(Definition.JSON.LAST_UP_COIN_KEY)));
                }
                practiceCoinList.add(practiceCoin);
            }
            return practiceCoinList;
        } catch (JSONException je) {
            je.printStackTrace();
        }

        return null;
    }

    private String getType(int content_type) {
        String type = null;
        switch (content_type) {
            case 1:
                type = Definition.General.KNOWLEDGE;
                break;
            case 2:
                type = Definition.General.PRACTICE;
                break;
            case 3:
                type = Definition.General.TEST;
                break;
            case 4:
                type = Definition.General.FLASHCARD;
                break;
            default:
                break;
        }

        return type;
    }

    private Date getDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }
}
