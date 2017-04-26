package com.honkidenihongo.pre.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by datpt on 7/7/16.
 */
public class Course extends RealmObject {

    @PrimaryKey
    public long id;
    public String name;
    public String version;
    public String short_name;
    public String icon;
    public String data_directory;
    public RealmList<Unit> units;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getData_directory() {
        return data_directory;
    }

    public void setData_directory(String data_directory) {
        this.data_directory = data_directory;
    }

    public RealmList<Unit> getUnits() {
        return units;
    }

    public void setUnits(RealmList<Unit> units) {
        this.units = units;
    }

    public void addUnit(Unit unit) {
        if (unit == null) {
            return;
        }
        if (units == null) {
            units = new RealmList<>();
        }
        units.add(unit);
    }

}
