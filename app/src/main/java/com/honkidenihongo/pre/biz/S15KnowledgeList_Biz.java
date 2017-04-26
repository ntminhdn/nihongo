package com.honkidenihongo.pre.biz;

import com.honkidenihongo.pre.model.Knowledge;

import java.util.ArrayList;
import java.util.List;

/**
 * Class S15KnowledgeList_Biz cung cấp data cho screen {@link com.honkidenihongo.gui.S15KnowledgeList_Activity}.
 *
 * @author binh.tt.
 * @since 09-Nov-2016.
 */
public class S15KnowledgeList_Biz {
    private List<Knowledge> knowledgeList = new ArrayList<>();

    /**
     * Trả về một mảng knowledge.
     *
     * @return KnowledgeList.
     */
    public List<Knowledge> getKnowledgeList() {
        for (int i = 0; i < 100; i++) {
            Knowledge knowledge = new Knowledge();
            knowledge.setData(String.valueOf(i) + ".はじめまして。");
            knowledge.setFormat(String.valueOf(i) + ".Rất vui được.");
            knowledgeList.add(knowledge);
        }

        return knowledgeList;
    }
}
