package com.honkidenihongo.pre.gui.lesson;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.MutableChar;

import com.honkidenihongo.pre.adapter.S06LessonList_Adapter;
import com.honkidenihongo.pre.api.JsonParserImport;
import com.honkidenihongo.pre.common.OperationResultInfo;
import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.config.DownloadFileConfig;
import com.honkidenihongo.pre.common.config.ErrorCode;
import com.honkidenihongo.pre.common.util.IoUtil;
import com.honkidenihongo.pre.common.util.NetworkUtil;
import com.honkidenihongo.pre.common.util.ZipUtil;
import com.honkidenihongo.pre.dac.S06LessonList_Dac;
import com.honkidenihongo.pre.dac.dao.ChoiceDao;
import com.honkidenihongo.pre.dac.dao.KnowledgeDao;
import com.honkidenihongo.pre.dac.dao.LessonDao;
import com.honkidenihongo.pre.dac.dao.QuestionDao;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.UserModel;
import com.honkidenihongo.pre.model.constant.LessonStatus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import io.realm.Realm;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Class using download a Lesson from the Server.
 */
public class S06LessonList_AsyncDownloadAndImporter_SingleLesson extends AsyncTask<String, Void, OperationResultInfo> {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S06LessonList_AsyncDownloadAndImporter_SingleLesson.class.getName();

    private Context context;
    private UserModel userModel;
    private int position;

    private S06LessonList_Adapter adapter;

    /**
     * The constructor.
     *
     * @param context
     * @param userModel
     * @param lessonListRecyclerView
     * @param position
     */
    S06LessonList_AsyncDownloadAndImporter_SingleLesson(Context context, UserModel userModel, RecyclerView lessonListRecyclerView, int position) {
        this.context = context;
        this.userModel = userModel;
        this.position = position;

        this.adapter = (S06LessonList_Adapter) lessonListRecyclerView.getAdapter();
    }

    @Override
    protected void onPreExecute() {
        // Hiển thị thông tin trạng thái.
        Lesson lesson = adapter.getItem(position);
        lesson.status = LessonStatus.DOWNLOADING;
        adapter.notifyItemChanged(position);
    }

    @Override
    protected OperationResultInfo doInBackground(String... params) {
        Lesson lesson = adapter.getItem(position);

        // Task 1: Download Lesson.
        File downloadedZipFile = task1_DownloadLesson(userModel, lesson);

        // Nếu việc Download không thành công thì trả về thông tin lỗi.
        if (downloadedZipFile == null) {
            OperationResultInfo operationResultInfo = new OperationResultInfo();
            operationResultInfo.isSuccess = false;
            operationResultInfo.errorCode = ErrorCode.S06LessonList.ERROR_DOWNLOAD_LESSON;

            return operationResultInfo;
        }

        // Task 2: Unzip file đã download.
        File userUnzipDir = task2_UnzipLesson(userModel, lesson, downloadedZipFile);

        // Nếu việc Unzip không thành công thì trả về thông tin lỗi.
        if (userUnzipDir == null) {
            OperationResultInfo operationResultInfo = new OperationResultInfo();
            operationResultInfo.isSuccess = false;
            operationResultInfo.errorCode = ErrorCode.S06LessonList.ERROR_UNZIP_LESSON;

            return operationResultInfo;
        }

        // Âm thầm xóa file zip gốc đã download để tiết kiệm dung lượng.
        try {
            downloadedZipFile.delete();
        } catch (Exception ex) {
            // Show the log in development environment.
            Log.d(LOG_TAG, "doInBackground(): " + ex.getMessage());
        }

        // Task 3: Import tài nguyên (voices, json,...) vào Database hay thư mục cần thiết.
        boolean importResult = task3_ImportData(userUnzipDir);

        // Nếu việc Import không thành công thì trả về thông tin lỗi.
        if (!importResult) {
            OperationResultInfo operationResultInfo = new OperationResultInfo();
            operationResultInfo.isSuccess = false;
            operationResultInfo.errorCode = ErrorCode.S06LessonList.ERROR_IMPORT_LESSON;

            return operationResultInfo;
        }

        // Đến đây, tất cả các thao tác đã thành công, trả về thông tin thành công.
        OperationResultInfo operationResultInfo = new OperationResultInfo();
        operationResultInfo.isSuccess = true;

        return operationResultInfo;
    }

    @Override
    protected void onProgressUpdate(Void... values) {

//        if (mRecyclerViewLesson != null && isAdded()) {
//            View view = mRecyclerViewLesson.getLayoutManager().findViewByPosition(getPositionOfLessonInsideList(mLessonId));
//
//            if (view != null) {
//                TextView mTvProcessing = (TextView) view.findViewById(R.id.lblLessonStatus);
//                TextView mTvLoading = (TextView) view.findViewById(R.id.mTvDownLoading);
//                TextView mTvNumber = (TextView) view.findViewById(R.id.mTvNumber);
//                ImageView imgLessonStatus = (ImageView) view.findViewById(R.id.imgLessonStatus);
//
//                imgLessonStatus.setImageResource(R.drawable.s06_lesson_list_ic_downloading);
//                mTvProcessing.setText(String.valueOf(values[0]));
//
//                if (mTvLoading.getVisibility() != View.VISIBLE) {
//                    mTvLoading.setVisibility(View.VISIBLE);
//                }
//
//                if (mTvNumber.getVisibility() != View.VISIBLE) {
//                    mTvNumber.setVisibility(View.VISIBLE);
//                }
//            }
//        }
    }

    @Override
    protected void onPostExecute(OperationResultInfo operationResultInfo) {
        /* Dựa vào kết quả trả về mà cập nhật thông tin trên giao diện. */
        Lesson lesson = adapter.getItem(position);

        // Nếu thành công.
        if (operationResultInfo.isSuccess) {
            Log.d(LOG_TAG, "onPostExecute(): isSuccess.");

            // 1. Hiển thị thông tin Downloaded OK ở Lesson Item.
            // 2. Cho phép click vào Lesson Item.
            // 3. Check nếu đã download xong tất cả thì phải disable button Download All.
            // todo: việc disable button này có thể cần xem xét thực hiện ở ngoài hàm gọi hàm này.
            // Hide text View download All.
//            if (checkInstallAll()) {
//                RelativeLayout mRlDownloadAll = (RelativeLayout) mRootView.findViewById(R.id.mRlDownloadAll);
//                mRlDownloadAll.setVisibility(View.GONE);
//            }

            lesson.status = LessonStatus.DOWNLOADED;
        } else {
//            // Nếu không thành công thì hiển thị thông báo lỗi tùy theo mã lỗi. Todo...
//            switch (operationResultInfo.errorCode) {
//                default:
//                case ErrorCode.S06LessonList.ERROR_DOWNLOAD_LESSON:
//                    Log.e(LOG_TAG, "ERROR_DOWNLOAD_LESSON");
//                    // Nhớ multi-language.
//                    lessonViewHolder.lblLessonStatus.setText("Download Error");
//
//                    break;
//                case ErrorCode.S06LessonList.ERROR_UNZIP_LESSON:
//                    Log.e(LOG_TAG, "ERROR_UNZIP_LESSON");
//                    lessonViewHolder.lblLessonStatus.setText("Unzip Error");
//
//                    break;
//                case ErrorCode.S06LessonList.ERROR_IMPORT_LESSON:
//                    Log.e(LOG_TAG, "ERROR_IMPORT_LESSON");
//                    lessonViewHolder.lblLessonStatus.setText("Import Error");
//
//                    break;
//            }

            lesson.status = LessonStatus.DOWNLOAD_ERROR;
        }

        adapter.notifyItemChanged(position);
    }

    /**
     * Đây là bước 1/3 trong cả quá trình Download -> Unzip -> Import danh sách Lesson, tham khảo {@link S06LessonList_AsyncDownloadAndImporter_SingleLesson}.
     *
     * @param userModel Thông tin của User bao gồm Access-Token.
     * @param lesson    Thông tin của Lesson.
     * @return Trả về đối tượng File: thể hiện file đã được download thành công, nếu không thành công thì trả về null.
     */
    @Nullable
    private File task1_DownloadLesson(UserModel userModel, Lesson lesson) {
        // Thư mục Cache của App.
        File cacheDir = context.getCacheDir();

        // Thư mục Cache của User sẽ sử dụng để download file zip.
        String cacheUserDirString = String.format("%s%s", AppConfig.SharedPreferencesKey.USER_INFO_PREFIX, userModel.id);
        File userCacheDir = new File(cacheDir, cacheUserDirString);

        // Nếu thư mục User Cache này không tồn tại thì tạo. Nếu tạo không thành công thì trả về null.
        if (!userCacheDir.exists()) {
            try {
                boolean createDirResult = userCacheDir.mkdirs();

                if (!createDirResult) {
                    // Show the log in development environment.
                    Log.d(LOG_TAG, "task1_DownloadLesson(): Cannot create directory.");

                    return null;
                }
            } catch (Exception ex) {
                // Show the log in development environment.
                Log.e(LOG_TAG, "task1_DownloadLesson(): " + ex.getMessage());

                return null;
            }
        }

        // Tên file zip sẽ download về.
        String userDownloadedZipFileName = String.format("%s%s.%s", DownloadFileConfig.DOWNLOAD_FILE_PREFIX_LESSON, lesson.getId(), DownloadFileConfig.DOWNLOAD_FILE_TYPE_ZIP);

        // Đối tượng file zip sẽ download về.
        File userDownloadedZipFile = null;

        // Temp object for download zip file.
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;

        try {
            userDownloadedZipFile = new File(userCacheDir, userDownloadedZipFileName);
            // userDownloadedZipFile = File.createTempFile(userDownloadedZipFileName, null, userCacheDir);

            String authHeader = String.format("%s %s", Definition.Request.HEADER_BEARER2, userModel.tokenInfo.access_token);

            // Todo: Cache okhttp request.
            Request downloadLessonRequest = new Request.Builder()
                    .url(AppConfig.getApiBaseUrl() + Definition.API.DOWNLOAD_LESSONS_LIST + "/" + lesson.getId())
                    .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                    .addHeader(Definition.Request.HEADER_AUTHORIZATION, authHeader)
                    .build();

            // Get OkHttpClient object with default timeout configurations.
            OkHttpClient okHttpClient = NetworkUtil.getDefaultHttpClient(context);

            // Tiến hành gửi request đồng bộ (synchronous) lên Server.
            Response response = okHttpClient.newCall(downloadLessonRequest).execute();

            // Check thông tin dung lượng. Cần dung lượng trống gấp đôi file download.
            //final long availableSpace = IoUtil.availableSpace(userCacheDir);
            final long availableSpace = userCacheDir.getFreeSpace();

            if (availableSpace < 2 * response.body().contentLength()) {
                // Show the log in development environment.
                Log.d(LOG_TAG, "task1_DownloadLesson(): The space not enough.");

                return null;
            }

            // Thực hiện download Stream đến file trên local.
            inputStream = response.body().byteStream();
            fileOutputStream = new FileOutputStream(userDownloadedZipFile);
            IoUtil.copy(inputStream, fileOutputStream);
        } catch (Exception ex) {
            // Show the log in development environment.
            Log.e(LOG_TAG, "task1_DownloadLesson(): " + ex.getMessage());

            return null;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception ex) {
                    // Show the log in development environment.
                    Log.e(LOG_TAG, "task1_DownloadLesson(): " + ex.getMessage());
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ex) {
                    Log.e(LOG_TAG, "task1_DownloadLesson(): " + ex.getMessage());
                }
            }
        }

        return userDownloadedZipFile;
    }

    /**
     * Đây là bước 2/3 trong cả quá trình Download-Unzip-Import Lesson List: {@link S06LessonList_AsyncDownloadAndImporter_SingleLesson}.<br>
     * Unzip file zip đã download để tạo ra thư mục có cùng tên và ở cùng nơi với file zip đã download.
     *
     * @param zipFile File zip đã download được ở bước 1: {@link #task1_DownloadLesson(UserModel, Lesson)}.
     * @return Trả về đối tượng File: thể hiện thư mục đã được unzip thành công, nếu không thành công thì trả về null.
     */
    @Nullable
    private File task2_UnzipLesson(UserModel userModel, Lesson lesson, File zipFile) {
        // Thư mục Files của App.
        File filesDir = context.getFilesDir();

        // Thư mục theo User.
        String userDirString = String.format("%s%s", AppConfig.SharedPreferencesKey.USER_INFO_PREFIX, userModel.id);

        // Thư mục theo Lesson.
        String lessonDirString = String.format("%s%s", DownloadFileConfig.DOWNLOAD_FILE_PREFIX_LESSON, lesson.getId());

        // Thư mục tương đối của User theo Lesson sẽ sử dụng để giải nén file zip.
        String userLessonFilesDirString = String.format("%s%s%s", userDirString, File.separator, lessonDirString);

        // Thư mục tuyệt đối sử dụng để chứa các tài nguyên được giải nén.
        File userLessonFilesDir = new File(filesDir, userLessonFilesDirString);

        // Nếu thư mục User Files này không tồn tại thì tạo. Nếu tạo không thành công thì trả về null.
        if (!userLessonFilesDir.exists()) {
            try {
                boolean createDirResult = userLessonFilesDir.mkdirs();

                if (!createDirResult) {
                    // Show the log in development environment.
                    Log.d(LOG_TAG, "task2_UnzipLesson(): Cannot create directory.");

                    return null;
                }
            } catch (Exception ex) {
                // Show the log in development environment.
                Log.e(LOG_TAG, "task2_UnzipLesson(): " + ex.getMessage());

                return null;
            }
        }

        return ZipUtil.unzipFile(zipFile, userLessonFilesDir);
    }

    /**
     * Đây là bước 3/3 trong cả quá trình Download-Unzip-Import Lesson List: {@link S06LessonList_AsyncDownloadAndImporter_SingleLesson}.<br>
     * Tiến hành Import các file dữ liệu (*.json, voices file,...) vào Database hoặc thư mục của ứng dụng.
     *
     * @param baseDataDir Thư mục gốc chứa các file dữ liệu (*.json, voices file,...) đã được unzip ở bước 2: {@link #task2_UnzipLesson(UserModel, Lesson, File)}.
     * @return Nếu việc import dữ liệu thành công thì trả về true, không thành công thì trả về false.
     */
    private boolean task3_ImportData(File baseDataDir) {
        // 1. Giữ nguyên nơi chứa voices file, nên không cần làm gì cả với voice files.

        // 2. Import *.json file to the Database.
        return importJsonToDatabase(baseDataDir);
    }

    /**
     * Method using import database.
     *
     * @param baseDataDir Base Data Directory.
     * @return true or false.
     */
    private boolean importJsonToDatabase(File baseDataDir) {
        String baseDataDirPath = baseDataDir.getAbsolutePath();

        try {
            // 1. Import lesson.json.
            String filePath_lesson = baseDataDirPath + "/" + DownloadFileConfig.FILE_LESSON_JSON;
            File fileLesson = new File(filePath_lesson);

            if (fileLesson.exists()) {
                if (getStringFromFile(filePath_lesson) != null) {
                    importLesson(getStringFromFile(filePath_lesson));
                }
            }

            // 2. Import knowledges.json.
            String filePath_knowledges = baseDataDirPath + "/" + DownloadFileConfig.FILE_KNOWLEDGES_JSON;
            File fileKnowledge = new File(filePath_knowledges);

            if (fileKnowledge.exists()) {
                if (getStringFromFile(filePath_knowledges) != null) {
                    importKnowledge(getStringFromFile(filePath_knowledges));
                }
            }

            // 3. Import questions.json.
            String filePath_questions = baseDataDirPath + "/" + DownloadFileConfig.FILE_QUESTIONS_JSON;
            File fileQuestion = new File(filePath_questions);

            if (fileQuestion.exists()) {
                if (getStringFromFile(filePath_questions) != null) {
                    importQuestions(getStringFromFile(filePath_questions));
                }
            }

            // 4. Import choices.json.
            String filePath_choices = baseDataDirPath + "/" + DownloadFileConfig.FILE_CHOICES_JSON;
            File fileChoice = new File(filePath_choices);

            if (fileChoice.exists()) {
                if (getStringFromFile(filePath_choices) != null) {
                    importChoices(getStringFromFile(filePath_choices));
                }
            }

            return true;
        } catch (Exception ex) {
            // Show the log in development environment.
            Log.e(LOG_TAG, "" + ex.getMessage());

            return false;
        }
    }

    /**
     * Find position of Lesson using update UI.
     *
     * @param lessonId Value Id of Lesson.
     * @return Position.
     */
    private int getPositionOfLessonInsideList(long lessonId) {
//            for (int i = 0; i < mLessons.size(); i++) {
//                if (mLessons.get(i).getId() == lessonId) {
//                    return i;
//                }
//            }

        return 0;
    }

    /**
     * Method check lessons installed.
     *
     * @return Is true is all installed or false if one lesson not installed.
     */
    private boolean checkInstallAll() {
//            for (Lesson lesson2 : mLessons) {
//                if (!lesson2.isInstalled()) {
//                    return false;
//                }
//            }

        return true;
    }

    /**
     * Method using convertStreamToString.
     *
     * @param is Input.
     * @return Nội dung trong file.
     * @throws Exception....
     */
    @NonNull
    private String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        reader.close();

        return sb.toString();
    }

    /**
     * Method using get string from file.
     *
     * @param filePath Link path file into device.
     * @return Nội dung của file dưới dạng chuỗi string.
     * @throws Exception...
     */
    private String getStringFromFile(String filePath) throws Exception {
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        String result = convertStreamToString(fileInputStream);

        // Make sure you close all streams.
        fileInputStream.close();

        return result;
    }

    /**
     * Method dùng để import Lesson vô database.
     *
     * @param jsonData Json of object Lesson.
     */
    private void importLesson(String jsonData) {
        // Convert json data thành đối tượng.
        LessonDao lessonDao = JsonParserImport.parse_Lesson(jsonData);

        if (lessonDao != null) {
            lessonDao.status = LessonStatus.DOWNLOADED;

            // Create Realm object.
            Realm realm = Realm.getDefaultInstance();

            // Insert into DB.
            new S06LessonList_Dac(realm).createLesson(lessonDao);
        }
    }

    /**
     * Method dùng để import Knowledge vô database.
     *
     * @param jsonData Json of object Knowledge.
     */
    private void importKnowledge(String jsonData) {
        // Convert json data thành đối tượng.
        List<KnowledgeDao> knowledgeDaos = JsonParserImport.parse_Knowledges(jsonData);

        if (knowledgeDaos != null && !knowledgeDaos.isEmpty()) {
            // Create Realm object.
            Realm realm = Realm.getDefaultInstance();

            // Insert into DB.
            S06LessonList_Dac s06LessonList_Dac = new S06LessonList_Dac(realm);
            for (KnowledgeDao knowledgeDao : knowledgeDaos) {
                s06LessonList_Dac.createKnowledge(knowledgeDao);
            }
        }
    }

    /**
     * Method dùng để import Questions vô database.
     *
     * @param jsonData Json of object Question.
     */
    private void importQuestions(String jsonData) {
        // Convert json data thành đối tượng.
        List<QuestionDao> questionDaos = JsonParserImport.parse_Questions(jsonData);

        if (questionDaos != null && !questionDaos.isEmpty()) {
            // Create Realm object.
            Realm realm = Realm.getDefaultInstance();

            // Insert into DB.
            S06LessonList_Dac s06LessonList_Dac = new S06LessonList_Dac(realm);
            for (QuestionDao questionDao : questionDaos) {
                s06LessonList_Dac.createQuestion(questionDao);
            }
        }
    }

    /**
     * Method dùng để import Choices vô database.
     *
     * @param jsonData Json of object Choices.
     */
    private void importChoices(String jsonData) {
        // Convert json data thành đối tượng.
        List<ChoiceDao> choiceDaoList = JsonParserImport.parse_Choices(jsonData);

        if (choiceDaoList != null && !choiceDaoList.isEmpty()) {
            // Create Realm object.
            Realm realm = Realm.getDefaultInstance();

            // Insert into DB.
            S06LessonList_Dac s06LessonList_Dac = new S06LessonList_Dac(realm);
            for (ChoiceDao choiceDao : choiceDaoList) {
                s06LessonList_Dac.createChoice(choiceDao);
            }
        }
    }
}