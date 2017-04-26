package com.honkidenihongo.pre.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by datpt on 7/7/16.
 */
public class Knowledge extends RealmObject {

    @PrimaryKey
    public long id;
    public int knowledge_id;
    public String format;
    public String data;
    public boolean is_remember;
    public Course course;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getKnowledge_id() {
        return knowledge_id;
    }

    public void setKnowledge_id(int knowledge_id) {
        this.knowledge_id = knowledge_id;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean is_remember() {
        return is_remember;
    }

    public void setIs_remember(boolean is_remember) {
        this.is_remember = is_remember;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public ArrayList<String> parserData() {
        if (data == null || data.isEmpty()) {
            return null;
        }
        ArrayList<String> datas = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(data);
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    datas.add(jsonArray.getString(i));
                }
            }
            int lastPos = datas.size() - 1;
            String audioData = datas.get(lastPos);
            JSONObject jsonObject = new JSONObject(audioData);
            Iterator<String> keys = jsonObject.keys();
            if (keys.hasNext()) {
                String audioFileName = keys.next();
                datas.set(lastPos, audioFileName);
                JSONArray audioArray = jsonObject.getJSONArray(audioFileName);
                String startPosition = audioArray.getString(0);
                datas.add(startPosition);
                String endPosition = audioArray.getString(1);
                datas.add(endPosition);
            }
            return datas;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
