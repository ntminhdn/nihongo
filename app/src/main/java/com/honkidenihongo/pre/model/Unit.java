package com.honkidenihongo.pre.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by datpt on 7/7/16.
 */
public class Unit extends RealmObject {

    @PrimaryKey
    public long id;
    public long course_id;
    public long unit_id;
    public String name;
    public RealmList<UnitData> datas;

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
    }

    public RealmList<UnitData> getDatas() {
        return datas;
    }

    public void setDatas(RealmList<UnitData> datas) {
        this.datas = datas;
    }

    public void addData(UnitData data) {
        if (data == null) {
            return;
        }
        if (datas == null) {
            datas = new RealmList<>();
        }
        datas.add(data);
    }
}
