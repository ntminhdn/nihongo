package com.honkidenihongo.pre.dac;

import android.content.Context;
import android.os.AsyncTask;

import com.honkidenihongo.pre.gui.listener.CreateDatabaseCallback;
import com.honkidenihongo.pre.model.Course;
import com.honkidenihongo.pre.model.Knowledge;
import com.honkidenihongo.pre.model.Question;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmAsyncTask;

/**
 * Created by datpt on 7/14/16.
 */
public class DatabaseStoreCourseTask extends AsyncTask<Void, Void, Void> {

    private long mCourseID;
    private String mDataDirectory;
    private Course mCourse;
    private List<Knowledge> mListKnowledge;
    private List<Question> mListQuestion;
    private DatabaseParser parser;
    private CreateDatabaseCallback mCallback;
    private Realm mRealm;

    public DatabaseStoreCourseTask(Context context, long course_id, String dataDirectory, Realm realm, CreateDatabaseCallback callback) {
        mCourseID = course_id;
        if (dataDirectory.endsWith("/")) {
            mDataDirectory = dataDirectory;
        } else {
            mDataDirectory = dataDirectory + "/";
        }
        parser = new DatabaseParser(context, mDataDirectory);
        mCallback = callback;
        mRealm = realm;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        mCourse = parser.parserCourse(mCourseID);
        mListKnowledge = parser.parserKnowledge();
        mListQuestion = parser.parserQuestion();
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mCallback != null) {
            mCallback.onStartCreated();
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        RealmAsyncTask transaction = DatabaseManager.createdCourseDatabase(mRealm, mCourse, mListKnowledge, mListQuestion);
        if (mCallback != null) {
            mCallback.onComplete(transaction);
        }
    }
}
