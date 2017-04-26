package com.honkidenihongo.pre.model;

import android.support.annotation.NonNull;

import com.honkidenihongo.pre.common.config.Definition;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by datpt on 9/9/16.
 */
public class UnitDataProgressLog extends RealmObject implements Comparable<UnitDataProgressLog> {

    @PrimaryKey
    public long id;
    public long unit_id;
    public String type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public double getUnitProgress() {
        double unitProgress;
        switch (type) {
            case Definition.General.KNOWLEDGE:
                unitProgress = progress / 10;
                break;
            default:
                unitProgress = (progress * 3) / 10;
                break;
        }

        return unitProgress;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            int content_type = 1;
            if (type.equalsIgnoreCase(Definition.General.KNOWLEDGE)) {
                content_type = 1;
            } else if (type.equalsIgnoreCase(Definition.General.PRACTICE)) {
                content_type = 2;
            } else if (type.equalsIgnoreCase(Definition.General.TEST)) {
                content_type = 3;
            } else if (type.equalsIgnoreCase(Definition.General.FLASHCARD)) {
                content_type = 4;
            }
            jsonObject.put(Definition.JSON.UNIT_ID_KEY, unit_id);
            jsonObject.put(Definition.JSON.PROGRESS_KEY, progress);
            jsonObject.put(Definition.JSON.CONTENT_TYPE_KEY, content_type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public int compareTo(@NonNull UnitDataProgressLog unitDataProgressLog) {
        int comp = (int) (unitDataProgressLog.getProgress() - progress);
        if (comp != 0) {
            return comp;
        } else {
            return (int) (id - unitDataProgressLog.getId());
        }
    }
}
