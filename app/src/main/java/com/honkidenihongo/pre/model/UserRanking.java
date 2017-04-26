package com.honkidenihongo.pre.model;

/**
 * Class Model User ranking.
 *
 * @author BinhDT.
 * @since 13-Dec-2016.
 */
public class UserRanking {
    private String display_name;
    private double point;
    private String ranking;

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public double getPoint() {
        return point;
    }

    public void setPoint(double point) {
        this.point = point;
    }

    public String getRanking() {
        return ranking;
    }

    public void setRanking(String ranking) {
        this.ranking = ranking;
    }
}
