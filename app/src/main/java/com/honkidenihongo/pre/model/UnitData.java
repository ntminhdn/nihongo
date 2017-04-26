package com.honkidenihongo.pre.model;

import com.honkidenihongo.pre.common.config.Definition;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by datpt on 7/7/16.
 */
public class UnitData extends RealmObject implements Comparable<UnitData> {

    @PrimaryKey
    public long id;
    public long course_id;
    public long unit_id;
    public String name;
    public String type;
    public String icon;
    public String title;
    public int lesson_number;
    public  String lesson_name;
    public String level;

    public String getLesson_name() {
        return lesson_name;
    }

    public void setLesson_name(String lesson_name) {
        this.lesson_name = lesson_name;
    }

    public int getLesson_number() {
        return lesson_number;
    }

    public void setLesson_number(int lesson_number) {
        this.lesson_number = lesson_number;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public RealmList<PracticeData> datas;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.title = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.name = title;
    }

    public RealmList<PracticeData> getDatas() {
        return datas;
    }

    public void setDatas(RealmList<PracticeData> datas) {
        this.datas = datas;
    }

    public void addData(PracticeData data) {
        if (data == null) {
            return;
        }
        if (datas == null) {
            datas = new RealmList<>();
        }
        datas.add(data);
    }

    private int getIntegerType() {
        int integerType;
        switch (name) {
            case Definition.General.KNOWLEDGE:
                integerType = 1;
                break;
            case Definition.General.FLASHCARD:
                integerType = 2;
                break;
            case Definition.General.PRACTICE:
                integerType = 3;
                break;
            case Definition.General.TEST:
                integerType = 4;
                break;
            default:
                integerType = 0;
                break;
        }

        return integerType;
    }

    @Override
    public int compareTo(UnitData o) {
        return (this.getIntegerType() - o.getIntegerType());
    }
}
