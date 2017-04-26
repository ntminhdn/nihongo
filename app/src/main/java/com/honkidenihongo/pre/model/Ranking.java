package com.honkidenihongo.pre.model;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

/**
 * Class model Ranking.
 *
 * @author Minh.nt.
 * @since 16-Mar-2017.
 */
public class Ranking extends RealmObject implements Parcelable {
    private int lesson_number;
    private int level;
    private int category;
    private int lesson_type;
    private int question_type;
    private int armorial;
    private String time;

    /**
     * Constructor of Class.
     */
    public Ranking() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.lesson_number);
        dest.writeInt(this.level);
        dest.writeInt(this.category);
        dest.writeInt(this.lesson_type);
        dest.writeInt(this.question_type);
        dest.writeInt(this.armorial);
        dest.writeString(this.time);
    }

    protected Ranking(Parcel in) {
        this.lesson_number = in.readInt();
        this.level = in.readInt();
        this.category = in.readInt();
        this.lesson_type = in.readInt();
        this.question_type = in.readInt();
        this.armorial = in.readInt();
        this.time = in.readString();
    }

    public static final Creator<Ranking> CREATOR = new Creator<Ranking>() {
        @Override
        public Ranking createFromParcel(Parcel source) {
            return new Ranking(source);
        }

        @Override
        public Ranking[] newArray(int size) {
            return new Ranking[size];
        }
    };
}

