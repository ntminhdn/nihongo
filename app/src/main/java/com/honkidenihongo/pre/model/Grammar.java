package com.honkidenihongo.pre.model;

/**
 * Model Grammar ánh xạ theo class {@link com.honkidenihongo.pre.dac.dao.GrammarDao}.
 *
 * @author binh.dt.
 * @since 04-Jan-2017.
 */
public class Grammar {
    /**
     * The Grammar Id.
     */
    public Long id;

    /**
     * The title Vietnamese.
     */
    public String title_vi;

    /**
     * The title English.
     */
    public String title_en;

    /**
     * The content Vietnamese.
     */
    public String content_vi;

    /**
     * The content English.
     */
    public String content_en;

    /**
     * The Lesson Id.
     */
    public Long lesson_id;

    /**
     * The display_order.
     */
    public Integer display_order;

    /**
     * The is open.
     */
    public boolean is_the_opend;
}
