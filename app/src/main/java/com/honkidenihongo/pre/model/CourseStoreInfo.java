package com.honkidenihongo.pre.model;

import java.io.Serializable;

/**
 * Created by datpt on 8/1/16.
 */
public class CourseStoreInfo implements Serializable {

    private long id;
    private String name;
    private String version;
    private boolean published;
    private String icon;
    private String url;
    private int unit_count;
    private int download_count;
    private String last_updated;
    private boolean is_exist;

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

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getUnit_count() {
        return unit_count;
    }

    public void setUnit_count(int unit_count) {
        this.unit_count = unit_count;
    }

    public int getDownload_count() {
        return download_count;
    }

    public void setDownload_count(int download_count) {
        this.download_count = download_count;
    }

    public String getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(String last_updated) {
        this.last_updated = last_updated;
    }

    public boolean is_exist() {
        return is_exist;
    }

    public void setIs_exist(boolean is_exist) {
        this.is_exist = is_exist;
    }
}
