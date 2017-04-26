package com.honkidenihongo.pre.model;

import android.content.Context;

import com.honkidenihongo.pre.common.config.DateFormatString;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.LocalAppUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Class object realm TimeLog.
 * <p>
 * Modify by Binh.dt.
 *
 * @author BinhDT.
 */
public class TimeLog extends RealmObject {

    @PrimaryKey
    public long id;
    public long course_id;
    public long unit_id;
    public Date start;
    public Date end;
    public String type;
    public boolean is_send;
    public String shortDay;

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

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean is_send() {
        return is_send;
    }

    public void setIs_send(boolean is_send) {
        this.is_send = is_send;
    }

    public String getShortDay() {
        return shortDay;
    }

    public void setShortDay(String shortDay) {
        this.shortDay = shortDay;
    }

    public String getStringStart() {
        DateFormat dateFormat = new SimpleDateFormat(DateFormatString.YYYY_MM_DD__HH_MM_SS, Locale.getDefault());
        return dateFormat.format(start);
    }

    public String getStringEnd() {
        DateFormat dateFormat = new SimpleDateFormat(DateFormatString.YYYY_MM_DD__HH_MM_SS, Locale.getDefault());
        return dateFormat.format(end);
    }

    /**
     * Lấy thời gian cho mỗi phiên làm việc của user.
     *
     * @return Value time count with s.
     */
    public float getDuration() {
        if (start != null && end != null) {
            return (end.getTime() - start.getTime()) / 1000;
        }

        return 0;
    }

    public JSONObject toJsonObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            long duration = (long) getDuration();
            jsonObject.put(Definition.JSON.START_KEY, getStringStart());
            jsonObject.put(Definition.JSON.END_KEY, getStringEnd());
            jsonObject.put(Definition.JSON.DURATION_KEY, duration);
            if (type.equalsIgnoreCase(Definition.Constants.TYPE_IMPROVE_KNOWLEDGE)) {
                jsonObject.put(Definition.JSON.COURSE_ID_KEY, course_id);
                jsonObject.put(Definition.JSON.UNIT_ID_KEY, unit_id);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * Method using save data to Realm.
     */
    public void saveOrUpdate() {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(TimeLog.this);
            }
        });
    }

    /**
     * Method update value end of object.
     *
     * @param id      Value of object.
     * @param context Value context of screen current.
     */
    public void editTimeLog(long id, Context context) {
        Realm realm = Realm.getDefaultInstance();
        TimeLog objectEdit = realm.where(TimeLog.class).equalTo(Definition.Database.TimeLog.TIME_LOG_FIELD_ID, id).findFirst();

        // Nếu tìm thấy đối tượng thì update giờ end cho nó với điều kiện đối tượng cho phép update thời gian end bởi thuộc tính is_send=true.
        if (objectEdit != null && is_send) {
            realm.beginTransaction();
            objectEdit.setEnd(new Date());

            // Nếu user đã log out thì giá trị thời gian online cuối cùng của nó không được update field end lại nữa khi user login trở lại.
            if (LocalAppUtil.getLastLoginUserInfo(context) == null) {
                objectEdit.setIs_send(false);
            }

            realm.commitTransaction();
            realm.close();
        }
    }
}
