package com.honkidenihongo.pre.model;

/**
 * Class Model TypeQuestion.
 *
 * @author binh.dt.
 * @since 06-Dec-2016.
 */
public class TypeQuestion {
    private String name;
    private int typeQuestion;

    /**
     * The constructor of class.
     *
     * @param name         Value name.
     * @param typeQuestion Value typeQuestion.
     */
    public TypeQuestion(String name, int typeQuestion) {
        this.name = name;
        this.typeQuestion = typeQuestion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTypeQuestion() {
        return typeQuestion;
    }

    public void setTypeQuestion(int typeQuestion) {
        this.typeQuestion = typeQuestion;
    }
}
