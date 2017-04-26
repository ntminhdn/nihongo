package com.honkidenihongo.pre.model;

import com.honkidenihongo.pre.common.config.Definition;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by datpt on 9/9/16.
 */
public class PracticeCoin extends RealmObject implements Comparable<PracticeCoin> {

    @PrimaryKey
    public long id;
    public long course_id;
    public long unit_id;
    public long module_id;
    public int coin;
    public int perfect_result;
    public Date last_up_coin;
    public boolean is_send;

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

    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public int getPerfect_result() {
        return perfect_result;
    }

    public void setPerfect_result(int perfect_result) {
        this.perfect_result = perfect_result;
    }

    public Date getLast_up_coin() {
        return last_up_coin;
    }

    public void setLast_up_coin(Date last_up_coin) {
        this.last_up_coin = last_up_coin;
    }

    public boolean is_send() {
        return is_send;
    }

    public void setIs_send(boolean is_send) {
        this.is_send = is_send;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Definition.JSON.COURSE_ID_KEY, course_id);
            jsonObject.put(Definition.JSON.UNIT_ID_KEY, unit_id);
            jsonObject.put(Definition.JSON.PRACTICE_ID_KEY, module_id);
            jsonObject.put(Definition.JSON.COIN_KEY, coin);
            jsonObject.put(Definition.JSON.PERFECTION_KEY, perfect_result);
            jsonObject.put(Definition.JSON.LAST_UP_COIN_KEY, getStringTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public String getStringTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(last_up_coin);
    }

    @Override
    public int compareTo(PracticeCoin practiceCoin) {
        int comp = perfect_result - practiceCoin.getPerfect_result();
        if (comp != 0) {
            return comp;
        } else {
            return (int) (module_id - practiceCoin.getModule_id());
        }
    }
}
