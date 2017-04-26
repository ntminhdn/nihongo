package com.honkidenihongo.pre.gui.grammar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.adapter.S27GrammarList_Adapter;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.ConvertDaoToModelUtil;
import com.honkidenihongo.pre.common.util.LessonNameUtil;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.common.util.MessageDialogUtil;
import com.honkidenihongo.pre.dac.dao.GrammarDao;
import com.honkidenihongo.pre.gui.MainActivity;
import com.honkidenihongo.pre.gui.listener.OnMainActivityListener;
import com.honkidenihongo.pre.gui.widget.HelperDialog;
import com.honkidenihongo.pre.gui.widget.LinearLayoutManagerWithSmoothScroller;
import com.honkidenihongo.pre.model.Grammar;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.constant.Category;
import com.honkidenihongo.pre.model.constant.LanguageNumberCode;
import com.honkidenihongo.pre.model.constant.LessonType;
import com.honkidenihongo.pre.model.constant.Level;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Class S27GrammarList_Fragment để hiển thị danh sách giải thích ngữ pháp.
 *
 * @author binh.dt.
 * @since 10-Nov-2016.
 */
public class S27GrammarList_Fragment extends Fragment {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S27GrammarList_Fragment.class.getName();
    public static final String S27_LESSON_PARCELABLE_OBJECT = "S27_LESSON_PARCELABLE_OBJECT";
    private OnMainActivityListener mActivityListener;

    private RecyclerView mRecyclerViewGrammar;
    private AppCompatTextView mTvTitle;

    private HelperDialog mHelperDialog;
    private Context mContext;
    private Realm mRealm;

    /**
     * Method receive object lesson.
     *
     * @param lesson Value.
     */
    public static S27GrammarList_Fragment newInstance(Lesson lesson) {
        S27GrammarList_Fragment fragment = new S27GrammarList_Fragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(S27_LESSON_PARCELABLE_OBJECT, lesson);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        mContext = getActivity();

        if (mContext instanceof MainActivity) {
            mActivityListener = (MainActivity) mContext;
        }

        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo đối tượng realm.
        mRealm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.s27_grammar_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Init controls in the layout.
        initView(view);

        // Init data for displaying.
        if (getLessonData() != null) {
            setData(getLessonData());
        }

        /**
         * Show dialog help.
         */
        showDialogHelp();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mActivityListener != null && getLessonData() != null) {
            setTitleScreen(getLessonData());
        }
    }

    @Override
    public void onStop() {
        Log.e(LOG_TAG, "onStop");

        closeRealm();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        try {
            if (mHelperDialog != null && mHelperDialog.isShowing()) {
                mHelperDialog.dismiss();
                mHelperDialog = null;
            }

        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        super.onDestroy();
    }

    /**
     * Close database.
     */
    private void closeRealm() {
        if (mRealm != null && !mRealm.isClosed()) {
            try {
                mRealm.close();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Couldn't close realm.");
            }

            mRealm = null;
        }
    }

    /**
     * Lấy đối tượng lesson nhận được.
     *
     * @return Lesson;
     */
    private Lesson getLessonData() {
        Bundle arguments = getArguments();

        if (arguments != null) {
            return arguments.getParcelable(S27_LESSON_PARCELABLE_OBJECT);
        }

        return null;
    }

    /**
     * Method using show dialog help.
     */
    private void showDialogHelp() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mContext == null || mHelperDialog == null) {
                    return;
                }

                boolean isShow = false;

                SharedPreferences prefs = mContext.getSharedPreferences(Definition.SettingApp.SETTING_APPLICATION, Context.MODE_PRIVATE);

                boolean isShowFirstOfScreen = prefs.getBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S27_GRAMMAR, true);

                if (isShowFirstOfScreen) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Definition.SettingApp.DialogHelp.DIALOG_HELP_S27_GRAMMAR, false);
                    editor.apply();

                    isShow = true;
                } else {
                    boolean isShowApplication = prefs.getBoolean(Definition.SettingApp.DialogHelp.SHOW_DIALOG_HELP_ALL_APPLICATION, false);

                    if (isShowApplication) {
                        isShow = true;
                    }
                }

                if (isShow && isAdded() && !((Activity) mContext).isFinishing() && !mHelperDialog.isShowing()) {
                    mHelperDialog.show();
                }
            }
        });
    }

    /**
     * Method using init View.
     */
    private void initView(View view) {
        mRecyclerViewGrammar = (RecyclerView) view.findViewById(R.id.mRecyclerView);
        mTvTitle = (AppCompatTextView) view.findViewById(R.id.mTvTitle);

        mHelperDialog = new HelperDialog(mContext, R.style.TransparentDialog, getString(R.string.common_help__s27_grammar_list__title), getString(R.string.common_help__s27_grammar_list__content));
    }

    /**
     * Method using set data for View.
     */
    private void setData(Lesson lesson) {
        RealmResults<GrammarDao> grammarDaos = mRealm.where(GrammarDao.class)
                .equalTo(Definition.Database.Grammar.GRAMMAR_FIELD_LESSON_NUMBER, lesson.getNumber())
                .findAll();

        if (!grammarDaos.isEmpty()) {
            List<Grammar> grammarList = new ArrayList<>();

            for (GrammarDao grammarDao : grammarDaos) {
                // Convert object Dao to model.
                grammarList.add(ConvertDaoToModelUtil.convertDaoToModel(grammarDao));
            }

            S27GrammarList_Adapter mS27GrammarListAdapter = new S27GrammarList_Adapter(mContext, grammarList, this);
            mRecyclerViewGrammar.setLayoutManager(new LinearLayoutManagerWithSmoothScroller(mContext));
            mRecyclerViewGrammar.setHasFixedSize(true);
            mRecyclerViewGrammar.setAdapter(mS27GrammarListAdapter);
        }
    }

    /**
     * Todo : Nghiên cứu remove phương thức này.
     * Move item open inside list to top layout.
     *
     * @param position Value position selected.
     */
    public void moveItemToTop(int position) {
        mRecyclerViewGrammar.smoothScrollToPosition(position);
    }

    /**
     * Set title for screen.
     *
     * @param lesson Value object lesson.
     */
    private void setTitleScreen(Lesson lesson) {
        // Lấy tên bài học dựa theo ngôn ngữ.
        String lessonName = "";
        if (LocaleHelper.getLanguage(mContext).equals(Definition.LanguageCode.ENGLISH)) {
            lessonName = LessonNameUtil.getLessonName(lesson, LanguageNumberCode.ENGLISH);
        } else {
            lessonName = LessonNameUtil.getLessonName(lesson, LanguageNumberCode.VIETNAMESE);
        }

        if (mActivityListener != null) {
            mActivityListener.setTitleScreen(lessonName);
        }

        // Set title for screen.
        if (lesson.getType() == LessonType.UNIT && lesson.getCategory() == Category.UNIT_SENTENCE) {
            switch (lesson.getLevel()) {
                case Level.BASIC:
                    mTvTitle.setText(String.format("%s" + Definition.General.BREADCRUMB_SEPARATOR + "%s" + Definition.General.BREADCRUMB_SEPARATOR + "%s", getString(R.string.common_app__level__basic), getString(R.string.common_app__category__unit_sentence), getString(R.string.common_module__grammar)));
                    break;

                case Level.ADVANCE:
                    mTvTitle.setText(String.format("%s" + Definition.General.BREADCRUMB_SEPARATOR + "%s" + Definition.General.BREADCRUMB_SEPARATOR + "%s", getString(R.string.common_app__level__advance), getString(R.string.common_app__category__unit_sentence), getString(R.string.common_module__grammar)));
                    break;
            }
        }
    }
}
