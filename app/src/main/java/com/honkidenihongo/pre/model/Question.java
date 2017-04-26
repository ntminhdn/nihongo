package com.honkidenihongo.pre.model;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by datpt on 7/7/16.
 */
public class Question extends RealmObject {

    @PrimaryKey
    public long id;
    public int question_id;
    public String question_content;
    public String audio;
    public String answers;
    public Course course;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(int question_id) {
        this.question_id = question_id;
    }

    public String getQuestion_content() {
        return question_content;
    }

    public void setQuestion_content(String question_content) {
        this.question_content = question_content;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    private List<Answer> getListAnswers() {
        try {
            List<Answer> listAnswers = new ArrayList<>();
            JSONArray answerJSONArray = new JSONArray(answers);
            if (answerJSONArray.length() == 0) {
                return null;
            }
            for (int i = 0; i < answerJSONArray.length(); i++) {
                if (i == 0) {
                    listAnswers.add(new Answer(answerJSONArray.getString(i), true));
                } else {
                    listAnswers.add(new Answer(answerJSONArray.getString(i), false));
                }
            }
            return listAnswers;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Answer> getDisplayAnswers() {
        List<Answer> listAnswers = getListAnswers();
        if (listAnswers != null && listAnswers.size() > 0) {
            List<Answer> listDisplayAnswers = new ArrayList<>();
            listDisplayAnswers.add(listAnswers.get(0));
            listAnswers.remove(0);
            Random rand = new Random();
            for (int i = 0; i < 2; i++) {
                int randomValue = rand.nextInt(listAnswers.size());
                listDisplayAnswers.add(listAnswers.get(randomValue));
                listAnswers.remove(randomValue);
            }
            Collections.shuffle(listDisplayAnswers);
            listAnswers.clear();
            return listDisplayAnswers;
        }
        return null;
    }

    public String getCorrectAnswer() {
        List<Answer> listAnswers = getListAnswers();
        if (listAnswers != null && listAnswers.size() > 0) {
            String correctAnswer = null;
            for (Answer answer : listAnswers) {
                if (answer.is_correct()) {
                    correctAnswer = answer.getContent();
                    break;
                }
            }
            return correctAnswer;
        }

        return null;
    }
}
