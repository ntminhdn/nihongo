package com.honkidenihongo.pre.model;

import com.honkidenihongo.pre.common.config.Definition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by datpt on 7/21/16.
 */
public class ExamLog extends RealmObject {

    public long id;
    public long course_id;
    public long unit_id;
    public long module_id;
    public long user_id;
    public String type;
    public Date create_at;
    public boolean is_send;
    public RealmList<Result> questions;
    public double correctness_ratio;
    public float total_duration;
    public int question_count;
    public float score;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCourse_id() {
        return course_id;
    }

    public void setCourse_id(long course_id) {
        this.course_id = course_id;
    }

    public long getUnit_id() {
        return unit_id;
    }

    public void setUnit_id(long unit_id) {
        this.unit_id = unit_id;
    }

    public long getModule_id() {
        return module_id;
    }

    public void setModule_id(long module_id) {
        this.module_id = module_id;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreate_at() {
        return create_at;
    }

    public void setCreate_at(Date create_at) {
        this.create_at = create_at;
    }

    public boolean is_send() {
        return is_send;
    }

    public void setIs_send(boolean is_send) {
        this.is_send = is_send;
    }

    public RealmList<Result> getQuestions() {
        return questions;
    }

    public void setQuestions(RealmList<Result> questions) {
        this.questions = questions;
    }

    public double getCorrectness_ratio() {
        return correctness_ratio;
    }

    public void setCorrectness_ratio(float correctness_ratio) {
        this.correctness_ratio = correctness_ratio;
    }

    public float getTotal_duration() {
        return total_duration;
    }

    public void setTotal_duration(float total_duration) {
        this.total_duration = total_duration;
    }

    public int getQuestion_count() {
        return question_count;
    }

    public void setQuestion_count(int question_count) {
        this.question_count = question_count;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getStringDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(create_at);
    }

    public JSONObject toJsonObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONArray questionArray = new JSONArray();
            jsonObject.put(Definition.JSON.DATE_KEY, getStringDate());
            jsonObject.put(Definition.JSON.COURSE_ID_KEY, course_id);
            jsonObject.put(Definition.JSON.UNIT_ID_KEY, unit_id);
            jsonObject.put(Definition.JSON.MODULE_ID_KEY, module_id);
            if (type.equalsIgnoreCase(Definition.General.TEST)) {
                jsonObject.put(Definition.JSON.SCORE_KEY, score);
            }
            jsonObject.put(Definition.JSON.QUESTION_COUNT_KEY, questions.size());
            jsonObject.put(Definition.JSON.CORRECTNESS_RATIO_KEY, correctness_ratio);
            jsonObject.put(Definition.JSON.TOTAL_DURATION_KEY, total_duration);
            jsonObject.put(Definition.JSON.RESULT_DETAILS_KEY, questionArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /*public float getDuration() {
        float duration = 0f;

        for (Result result : questions) {
            duration += result.getTime_complete();
        }

        return duration;
    }

    public float getScore() {
        int incorrect_count = 0;
        for (Result result : questions) {
            if (!result.is_correct()) {
                incorrect_count++;
            }
        }
        return (getDuration() + incorrect_count * 3);
    }*/
}
