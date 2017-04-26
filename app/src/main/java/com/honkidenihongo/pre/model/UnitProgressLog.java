package com.honkidenihongo.pre.model;

import com.honkidenihongo.pre.common.config.Definition;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by datpt on 9/9/16.
 */
public class UnitProgressLog extends RealmObject implements Comparable<UnitProgressLog> {

    @PrimaryKey
    public long id;
    public long unit_id;
    public double progress;
    public boolean is_send;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUnit_id() {
        return unit_id;
    }

    public void setUnit_id(long unit_id) {
        this.unit_id = unit_id;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
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
            jsonObject.put(Definition.JSON.UNIT_ID_KEY, unit_id);
            jsonObject.put(Definition.JSON.PROGRESS_KEY, progress);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public int compareTo(UnitProgressLog unitProgressLog) {
        int comp = (int) (unitProgressLog.getProgress() - progress);
        if (comp != 0) {
            return comp;
        } else {
            return (int) (id - unitProgressLog.getId());
        }
    }
}
