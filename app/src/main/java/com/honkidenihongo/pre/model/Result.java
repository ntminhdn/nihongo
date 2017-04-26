package com.honkidenihongo.pre.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.honkidenihongo.pre.common.config.Definition;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by datpt on 7/21/16.
 */
public class Result extends RealmObject implements Parcelable {

    @PrimaryKey
    public long id;
    public long question_id;
    public String question;
    public String answer;
    public String audio_data;
    public boolean is_audio;
    public boolean is_correct;
    public double time_complete;
    public Date create_at;

    // 4 thuộc tính result set theo question của nó, dựa vào đó để tìm ra object knowledge detail.
    public Integer knowledge_number;
    public Integer level;
    public Integer category;
    public int typeQuestion;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(long question_id) {
        this.question_id = question_id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAudio_data() {
        return audio_data;
    }

    public void setAudio_data(String audio_data) {
        this.audio_data = audio_data;
    }

    public boolean is_audio() {
        return is_audio;
    }

    public void setIs_audio(boolean is_audio) {
        this.is_audio = is_audio;
    }

    public boolean is_correct() {
        return is_correct;
    }

    public void setIs_correct(boolean is_correct) {
        this.is_correct = is_correct;
    }

    public double getTime_complete() {
        return time_complete;
    }

    public void setTime_complete(double time_complete) {
        this.time_complete = time_complete;
    }

    public Date getCreate_at() {
        return create_at;
    }

    public void setCreate_at(Date create_at) {
        this.create_at = create_at;
    }

    public Integer getKnowledge_number() {
        return knowledge_number;
    }

    public void setKnowledge_number(Integer knowledge_number) {
        this.knowledge_number = knowledge_number;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public int getTypeQuestion() {
        return typeQuestion;
    }

    public void setTypeQuestion(int typeQuestion) {
        this.typeQuestion = typeQuestion;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Definition.JSON.QUESTION_ID_KEY, question_id);
            jsonObject.put(Definition.JSON.CORRECT_KEY, is_correct);
            jsonObject.put(Definition.JSON.DURATION_KEY, time_complete);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public Result() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.question_id);
        dest.writeString(this.question);
        dest.writeString(this.answer);
        dest.writeString(this.audio_data);
        dest.writeByte(this.is_audio ? (byte) 1 : (byte) 0);
        dest.writeByte(this.is_correct ? (byte) 1 : (byte) 0);
        dest.writeDouble(this.time_complete);
        dest.writeLong(this.create_at != null ? this.create_at.getTime() : -1);
        dest.writeValue(this.knowledge_number);
        dest.writeValue(this.level);
        dest.writeValue(this.category);
        dest.writeInt(this.typeQuestion);
    }

    protected Result(Parcel in) {
        this.id = in.readLong();
        this.question_id = in.readLong();
        this.question = in.readString();
        this.answer = in.readString();
        this.audio_data = in.readString();
        this.is_audio = in.readByte() != 0;
        this.is_correct = in.readByte() != 0;
        this.time_complete = in.readDouble();
        long tmpCreate_at = in.readLong();
        this.create_at = tmpCreate_at == -1 ? null : new Date(tmpCreate_at);
        this.knowledge_number = (Integer) in.readValue(Integer.class.getClassLoader());
        this.level = (Integer) in.readValue(Integer.class.getClassLoader());
        this.category = (Integer) in.readValue(Integer.class.getClassLoader());
        this.typeQuestion = in.readInt();
    }

    public static final Creator<Result> CREATOR = new Creator<Result>() {
        @Override
        public Result createFromParcel(Parcel source) {
            return new Result(source);
        }

        @Override
        public Result[] newArray(int size) {
            return new Result[size];
        }
    };
}
