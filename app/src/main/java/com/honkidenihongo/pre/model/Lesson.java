package com.honkidenihongo.pre.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.honkidenihongo.pre.model.constant.LessonStatus;

/**
 * @author BinhDT.
 */

public class Lesson implements Parcelable {
    private long id;
    private int type;
    private int number;
    private int version;
    private String description;
    private String title_vi;
    private String title_en;
    private String title_ja;

    private String created_at;
    private String updated_at;
    private int category;
    private int level;

    public int status = LessonStatus.UN_DOWNLOADED;

    /**
     * Constructor of Class.
     */
    public Lesson() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getTitle_vi() {
        return title_vi;
    }

    public void setTitle_vi(String title_vi) {
        this.title_vi = title_vi;
    }

    public String getTitle_en() {
        return title_en;
    }

    public void setTitle_en(String title_en) {
        this.title_en = title_en;
    }

    public String getTitle_ja() {
        return title_ja;
    }

    public void setTitle_ja(String title_ja) {
        this.title_ja = title_ja;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.type);
        dest.writeInt(this.number);
        dest.writeInt(this.version);
        dest.writeString(this.description);
        dest.writeString(this.title_vi);
        dest.writeString(this.title_en);
        dest.writeString(this.title_ja);
        dest.writeString(this.created_at);
        dest.writeString(this.updated_at);
        dest.writeInt(this.category);
        dest.writeInt(this.level);
        dest.writeInt(this.status);
    }

    protected Lesson(Parcel in) {
        this.id = in.readLong();
        this.type = in.readInt();
        this.number = in.readInt();
        this.version = in.readInt();
        this.description = in.readString();
        this.title_vi = in.readString();
        this.title_en = in.readString();
        this.title_ja = in.readString();
        this.created_at = in.readString();
        this.updated_at = in.readString();
        this.category = in.readInt();
        this.level = in.readInt();
        this.status = in.readInt();
    }

    public static final Creator<Lesson> CREATOR = new Creator<Lesson>() {
        @Override
        public Lesson createFromParcel(Parcel source) {
            return new Lesson(source);
        }

        @Override
        public Lesson[] newArray(int size) {
            return new Lesson[size];
        }
    };
}
