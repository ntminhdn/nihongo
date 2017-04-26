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
 * Created by datpt on 9/23/16.
 */
public class CoinLog extends RealmObject {

    @PrimaryKey
    public long id;
    public long course_id;
    public long unit_id;
    public long module_id;
    public int coin;
    public String type;
    public Date time_up_coin;
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

    public void setCoin(int coin_number) {
        this.coin = coin_number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getTime_up_coin() {
        return time_up_coin;
    }

    public void setTime_up_coin(Date time_up_coin) {
        this.time_up_coin = time_up_coin;
    }

    public boolean is_send() {
        return is_send;
    }

    public void setIs_send(boolean is_send) {
        this.is_send = is_send;
    }

    private String getStringTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(time_up_coin);
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            int integerType = 2;
            if (type.equalsIgnoreCase(Definition.General.FLASHCARD)) {
                integerType = 4;
            }
            jsonObject.put(Definition.JSON.COURSE_ID_KEY, course_id);
            jsonObject.put(Definition.JSON.UNIT_ID_KEY, unit_id);
            jsonObject.put(Definition.JSON.MODULE_ID_KEY, module_id);
            jsonObject.put(Definition.JSON.TYPE_KEY, integerType);
            jsonObject.put(Definition.JSON.COINS_KEY, coin);
            jsonObject.put(Definition.JSON.DATE_KEY, getStringTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
