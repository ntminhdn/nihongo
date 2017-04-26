package com.honkidenihongo.pre.model;

/**
 * Created by datpt on 7/21/16.
 */
public class Answer {

    private String content;
    private boolean is_correct;

    public Answer(String content, boolean is_correct) {
        this.content = content;
        this.is_correct = is_correct;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean is_correct() {
        return is_correct;
    }

    public void setIs_correct(boolean is_correct) {
        this.is_correct = is_correct;
    }
}
