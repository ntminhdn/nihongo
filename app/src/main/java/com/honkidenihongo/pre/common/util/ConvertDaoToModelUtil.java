package com.honkidenihongo.pre.common.util;

import com.honkidenihongo.pre.dac.dao.GrammarDao;
import com.honkidenihongo.pre.model.Grammar;

/**
 * Class ConvertDaoToModelUtil tiện ích convert đối tượng Dao sang đối tượng model.
 *
 * @author binh.dt.
 * @since 05-Jan-2017.
 */
public class ConvertDaoToModelUtil {

    /**
     * The private constructor.
     */
    private ConvertDaoToModelUtil() {
    }

    /**
     * Phương thức convert object Dao thành model Grammar.
     *
     * @param grammarDao Object Dao.
     */
    public static Grammar convertDaoToModel(GrammarDao grammarDao) {
        Grammar grammar = new Grammar();
        grammar.id = grammarDao.id;
        grammar.title_en = grammarDao.title_en;
        grammar.title_vi = grammarDao.title_vi;
        grammar.content_en = grammarDao.content_en;
        grammar.content_vi = grammarDao.content_vi;
        grammar.display_order = grammarDao.display_order;

        return grammar;
    }
}
