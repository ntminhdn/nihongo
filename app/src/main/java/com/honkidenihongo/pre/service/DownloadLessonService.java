package com.honkidenihongo.pre.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.honkidenihongo.pre.api.JsonParserImport;
import com.honkidenihongo.pre.common.OperationResultInfo;
import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.config.DownloadFileConfig;
import com.honkidenihongo.pre.common.config.ErrorCode;
import com.honkidenihongo.pre.common.util.IoUtil;
import com.honkidenihongo.pre.common.util.LocalAppUtil;
import com.honkidenihongo.pre.common.util.NetworkUtil;
import com.honkidenihongo.pre.common.util.ZipUtil;
import com.honkidenihongo.pre.dac.S06LessonList_Dac;
import com.honkidenihongo.pre.dac.dao.ChoiceDao;
import com.honkidenihongo.pre.dac.dao.GrammarDao;
import com.honkidenihongo.pre.dac.dao.KnowledgeDao;
import com.honkidenihongo.pre.dac.dao.LessonDao;
import com.honkidenihongo.pre.dac.dao.QuestionDao;
import com.honkidenihongo.pre.gui.lesson.S06LessonList_AsyncDownloadAndImporter_SingleLesson;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.UserModel;
import com.honkidenihongo.pre.model.constant.LessonStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Class custom service download lesson.
 *
 * @author BinhDT.
 */
public class DownloadLessonService extends IntentService {
    /**
     * Log tag.
     */
    private static final String LOG_TAG = DownloadLessonService.class.getSimpleName();
    public static final String POSITION_LESSON_PARCELABLE_OBJECT_DOWNLOAD = "position";
    public static final String LESSON_PARCELABLE_OBJECT_DOWNLOAD = "lesson";
    private static final String SLASH_FILE = "/";

    /**
     * Các biến dùng để lắng nghe sự kiện người dùng logout tài khoản để stop service download lesson.
     */
    public static final String ACTION_STOP_DOWNLOAD_LESSON = "ACTION_STOP_DOWNLOAD_LESSON";
    private boolean mStop = false;

    public DownloadLessonService() {
        super(LOG_TAG);
    }

    /**
     * Receive stop download lesson when user logout.
     */
    private BroadcastReceiver mStopDownloadLessonReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive");

            mStop = true;
        }
    };

    @Override
    protected void onHandleIntent(Intent intent) {
        /**
         * Khi một service được khởi chạy ta sẽ đăng ký lắng nghe sự kiện mStop download lesson.
         */
        IntentFilter filter = new IntentFilter(ACTION_STOP_DOWNLOAD_LESSON);
        registerReceiver(mStopDownloadLessonReceive, filter);

        Lesson lesson = intent.getParcelableExtra(LESSON_PARCELABLE_OBJECT_DOWNLOAD);
        int position = intent.getIntExtra(POSITION_LESSON_PARCELABLE_OBJECT_DOWNLOAD, 0);

        Context context = getApplicationContext();

        if (context != null && lesson != null) {
            UserModel userModel = LocalAppUtil.getLastLoginUserInfo(context);

            if (userModel != null) {

                // Lấy thư mục dùng để lưu data.
                File filesDir = context.getFilesDir();

                // Tính toán dung lượng còn trống là bao nhiêu theo giá trị MB.
                long availableSpace = (IoUtil.availableSpace(filesDir) / IoUtil.SIZE_MB);

                Log.e(LOG_TAG, "AvailableSpace==" + availableSpace);

                // Nếu nhỏ hơn 60Mb thì trả về error không đủ dung lượng bộ nhớ cho tiến trình thực hiện.
                if (availableSpace < IoUtil.VALUE_MEMORY_DOWNLOAD_MIN) {
                    DownloadLessonBroadcastReceiver.sendUpdateStatusDownloadLesson(getApplication(), IoUtil.ERROR_MEMORY, position);

                    // Khi tác vụ này bị dừng thì hủy đăng kí tại lúc này.
                    unregisterReceiver(mStopDownloadLessonReceive);

                    return;
                }

                // Khi khởi động luồng download thì push về trạng thái của lesson là downloading cho lesson list cập nhật ui.
                DownloadLessonBroadcastReceiver.sendUpdateStatusDownloadLesson(getApplication(), lesson.status == LessonStatus.WAITING_UPDATE ? LessonStatus.UPDATING : LessonStatus.DOWNLOADING, position);

                // When I call stopSelf() in onHandleIntent(Intent ..) all Intents which are waiting in the IntentService queue are removed.
                // Http://stackoverflow.com/questions/10250745/proper-way-to-stop-intentService.
                if (mStop) {
                    Log.d(LOG_TAG, "stopAtTask0");

                    // Hủy đăng ký dịch vụ và dùng all luồng download.
                    unregisterReceiver(mStopDownloadLessonReceive);
                    stopSelf();

                    return;
                }

                // Trước khi thực hiện download lesson ta gọi api lấy url để download bài học.
                String url = getLinkDownloadLesson(context, lesson);

                if (TextUtils.isEmpty(url)) {
                    OperationResultInfo operationResultInfo = new OperationResultInfo();
                    operationResultInfo.isSuccess = false;
                    operationResultInfo.errorCode = ErrorCode.S06LessonList.ERROR_DOWNLOAD_LESSON;

                    DownloadLessonBroadcastReceiver.sendUpdateStatusDownloadLesson(getApplication(), LessonStatus.DOWNLOAD_ERROR, position);

                    // Khi tác vụ này bị dừng thì hủy đăng kí tại lúc này.
                    unregisterReceiver(mStopDownloadLessonReceive);

                    return;
                }

                // When I call stopSelf() in onHandleIntent(Intent ..) all Intents which are waiting in the IntentService queue are removed.
                // Http://stackoverflow.com/questions/10250745/proper-way-to-stop-intentService.
                if (mStop) {
                    Log.d(LOG_TAG, "stopAtTask1");

                    // Hủy đăng ký dịch vụ và dùng all luồng download.
                    unregisterReceiver(mStopDownloadLessonReceive);
                    stopSelf();

                    return;
                }

                // Task 1: Download Lesson.
                File downloadedZipFile = task1_DownloadLesson(context, userModel, lesson, url);

                // Nếu việc Download không thành công thì trả về thông tin lỗi.
                if (downloadedZipFile == null) {
                    OperationResultInfo operationResultInfo = new OperationResultInfo();
                    operationResultInfo.isSuccess = false;
                    operationResultInfo.errorCode = ErrorCode.S06LessonList.ERROR_DOWNLOAD_LESSON;

                    DownloadLessonBroadcastReceiver.sendUpdateStatusDownloadLesson(getApplication(), LessonStatus.DOWNLOAD_ERROR, position);

                    // Khi tác vụ này bị dừng thì hủy đăng kí tại lúc này.
                    unregisterReceiver(mStopDownloadLessonReceive);

                    return;
                }

                // When I call stopSelf() in onHandleIntent(Intent ..) all Intents which are waiting in the IntentService queue are removed.
                // Http://stackoverflow.com/questions/10250745/proper-way-to-stop-intentService.
                if (mStop) {
                    Log.d(LOG_TAG, "stopAtTask2");

                    // Hủy đăng ký dịch vụ và dùng all luồng download.
                    unregisterReceiver(mStopDownloadLessonReceive);
                    stopSelf();

                    return;
                }

                // Task 2: Unzip file đã download.
                File userUnzipDir = task2_UnzipLesson(context, userModel, lesson, downloadedZipFile);

                // Âm thầm xóa file zip gốc đã download để tiết kiệm dung lượng.
                try {
                    downloadedZipFile.delete();
                } catch (Exception ex) {
                    // Show the log in development environment.
                    Log.d(LOG_TAG, "doInBackground(): " + ex.getMessage());
                }

                // Nếu việc Unzip không thành công thì trả về thông tin lỗi.
                if (userUnzipDir == null) {
                    OperationResultInfo operationResultInfo = new OperationResultInfo();
                    operationResultInfo.isSuccess = false;
                    operationResultInfo.errorCode = ErrorCode.S06LessonList.ERROR_UNZIP_LESSON;

                    DownloadLessonBroadcastReceiver.sendUpdateStatusDownloadLesson(getApplication(), LessonStatus.DOWNLOAD_ERROR, position);

                    // Khi tác vụ này bị dừng thì hủy đăng kí tại lúc này.
                    unregisterReceiver(mStopDownloadLessonReceive);

                    return;
                }

                // When I call stopSelf() in onHandleIntent(Intent ..) all Intents which are waiting in the IntentService queue are removed.
                // Http://stackoverflow.com/questions/10250745/proper-way-to-stop-intentService.
                if (mStop) {
                    Log.d(LOG_TAG, "stopAtTask3");

                    // Hủy đăng ký dịch vụ và dùng all luồng download.
                    unregisterReceiver(mStopDownloadLessonReceive);
                    stopSelf();

                    return;
                }

                // Task 3: Import tài nguyên (voices, json,...) vào Database hay thư mục cần thiết.
                boolean importResult = task3_ImportData(userUnzipDir);

                // Nếu việc Import không thành công thì trả về thông tin lỗi.
                if (!importResult) {
                    OperationResultInfo operationResultInfo = new OperationResultInfo();
                    operationResultInfo.isSuccess = false;
                    operationResultInfo.errorCode = ErrorCode.S06LessonList.ERROR_IMPORT_LESSON;

                    DownloadLessonBroadcastReceiver.sendUpdateStatusDownloadLesson(getApplication(), LessonStatus.DOWNLOAD_ERROR, position);

                    // Khi tác vụ này bị dừng thì hủy đăng kí tại lúc này.
                    unregisterReceiver(mStopDownloadLessonReceive);

                    return;
                }

                // Đến đây, tất cả các thao tác đã thành công, trả về thông tin thành công.
                OperationResultInfo operationResultInfo = new OperationResultInfo();
                operationResultInfo.isSuccess = true;

                DownloadLessonBroadcastReceiver.sendUpdateStatusDownloadLesson(getApplication(), LessonStatus.DOWNLOADED, position);

                // Khi tác vụ này bị dừng thì hủy đăng kí tại lúc này.
                unregisterReceiver(mStopDownloadLessonReceive);
            }
        } else {// Trả thông báo về download error.
            DownloadLessonBroadcastReceiver.sendUpdateStatusDownloadLesson(getApplication(), LessonStatus.DOWNLOAD_ERROR, position);

            // Khi tác vụ này bị dừng thì hủy đăng kí tại lúc này.
            unregisterReceiver(mStopDownloadLessonReceive);
        }
    }

    /**
     * Run api using get link url download lesson.
     *
     * @param context Value context of screen current.
     * @param lesson  Value Lesson be download.
     */
    private String getLinkDownloadLesson(Context context, final Lesson lesson) {
        // Lấy thông tin user hiện thời vừa đăng nhập để lấy Access-Token hợp lệ.
        UserModel userModel = LocalAppUtil.getLastLoginUserInfo(context);

        String accessToken = null;
        if (userModel != null && userModel.tokenInfo != null) {
            accessToken = userModel.tokenInfo.access_token;
        }

        /* Bước 2: Request lên API Server để lấy danh sách Lesson List. */
        // Chuẩn bị dữ kiện để gửi lên Server.
        String authHeader = String.format("%s %s", Definition.Request.HEADER_BEARER2, accessToken);
        Request getLinkDownloadLessonsRequest = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + String.format(Locale.getDefault(), Definition.API.GET_URL_DOWNLOAD_LESSON, lesson.getNumber()))
                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                .header(Definition.Request.HEADER_AUTHORIZATION, authHeader)
                .header(Definition.Request.PARAM_CLIENT_TOKEN, AppConfig.getClientToken())
                .build();

        // Get OkHttpClient object with default timeout configurations.
        OkHttpClient okHttpClient = NetworkUtil.getDefaultHttpClient(context);

        Response response = null;
        try {
            response = okHttpClient.newCall(getLinkDownloadLessonsRequest).execute();

            // Request to server get data is ok.
            if (response.code() == HttpURLConnection.HTTP_OK) {
                return handlerProcessDownloadLesson(response.body().string());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        return null;
    }

    /**
     * Handler process download lesson.
     *
     * @param json Value string data from server.
     */
    private String handlerProcessDownloadLesson(final String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has(Definition.Response.DATA)) {
                boolean isSuccess = jsonObject.getBoolean(Definition.Response.SUCCESS);

                if (isSuccess) {
                    JSONObject data = jsonObject.getJSONObject(Definition.Response.DATA);
                    return data.getString(Definition.JSON.DOWNLOAD_URL);
                }
            }
        } catch (JSONException e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        return null;
    }

    /**
     * Đây là bước 1/3 trong cả quá trình Download -> Unzip -> Import danh sách Lesson, tham khảo {@link S06LessonList_AsyncDownloadAndImporter_SingleLesson}.
     *
     * @param userModel Thông tin của User bao gồm Access-Token.
     * @param lesson    Thông tin của Lesson.
     * @return Trả về đối tượng File: thể hiện file đã được download thành công, nếu không thành công thì trả về null.
     */
    @Nullable
    private File task1_DownloadLesson(Context context, UserModel userModel, Lesson lesson, String url) {
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
                    .url(url)
                    .addHeader(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                    .addHeader(Definition.Request.HEADER_AUTHORIZATION, authHeader)
                    .header(Definition.Request.PARAM_CLIENT_TOKEN, AppConfig.getClientToken())
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

            // Kiểm tra có download lesson thành công hay không?
            if (!response.isSuccessful()) {
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
     * @param zipFile File zip đã download được ở bước 1: {@link #task1_DownloadLesson(Context, UserModel, Lesson, String)}.
     * @return Trả về đối tượng File: thể hiện thư mục đã được unzip thành công, nếu không thành công thì trả về null.
     */
    @Nullable
    private File task2_UnzipLesson(Context context, UserModel userModel, Lesson lesson, File zipFile) {
        // Thư mục Files của App.
        File filesDir = context.getFilesDir();

        // Thư mục theo User.
        String userDirString = String.format("%s%s", AppConfig.SharedPreferencesKey.USER_INFO_PREFIX, userModel.id);

        // Thư mục theo Lesson.
        String lessonDirString = String.format(Locale.US, "%s%02d", DownloadFileConfig.DOWNLOAD_FILE_PREFIX_LESSON, lesson.getNumber());

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
     * @param baseDataDir Thư mục gốc chứa các file dữ liệu (*.json, voices file,...) đã được unzip ở bước 2: {@link #task2_UnzipLesson(Context, UserModel, Lesson, File)}.
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
            String filePath_lesson = baseDataDirPath + SLASH_FILE + DownloadFileConfig.FILE_LESSON_JSON;
            File fileLesson = new File(filePath_lesson);

            if (fileLesson.exists()) {
                if (getStringFromFile(filePath_lesson) != null) {
                    importLesson(getStringFromFile(filePath_lesson));

                    // Tiến hành xoá các file chứa nội dung string.
                    try {
                        fileLesson.delete();
                    } catch (Exception ex) {
                        // Show the log in development environment.
                        Log.d(LOG_TAG, "doInBackground(): " + ex.getMessage());
                    }
                }
            }

            // 2. Import knowledges.json.
            String filePath_knowledges = baseDataDirPath + SLASH_FILE + DownloadFileConfig.FILE_KNOWLEDGES_JSON;
            File fileKnowledge = new File(filePath_knowledges);

            if (fileKnowledge.exists()) {
                if (getStringFromFile(filePath_knowledges) != null) {
                    importKnowledge(getStringFromFile(filePath_knowledges));

                    // Tiến hành xoá các file chứa nội dung string.
                    try {
                        fileKnowledge.delete();
                    } catch (Exception ex) {
                        // Show the log in development environment.
                        Log.d(LOG_TAG, "doInBackground(): " + ex.getMessage());
                    }
                }
            }

            // 3. Import questions.json.
            String filePath_questions = baseDataDirPath + SLASH_FILE + DownloadFileConfig.FILE_QUESTIONS_JSON;
            File fileQuestion = new File(filePath_questions);

            if (fileQuestion.exists()) {
                if (getStringFromFile(filePath_questions) != null) {
                    importQuestions(getStringFromFile(filePath_questions));

                    // Tiến hành xoá các file chứa nội dung string.
                    try {
                        fileQuestion.delete();
                    } catch (Exception ex) {
                        // Show the log in development environment.
                        Log.d(LOG_TAG, "doInBackground(): " + ex.getMessage());
                    }
                }
            }

            // 4. Import choices.json.
            String filePath_choices = baseDataDirPath + SLASH_FILE + DownloadFileConfig.FILE_CHOICES_JSON;
            File fileChoice = new File(filePath_choices);

            if (fileChoice.exists()) {
                if (getStringFromFile(filePath_choices) != null) {
                    importChoices(getStringFromFile(filePath_choices));

                    // Tiến hành xoá các file chứa nội dung string.
                    try {
                        fileChoice.delete();
                    } catch (Exception ex) {
                        // Show the log in development environment.
                        Log.d(LOG_TAG, "doInBackground(): " + ex.getMessage());
                    }
                }
            }

            // 5. Import grammars.json.
            String filePath_grammars = baseDataDirPath + SLASH_FILE + DownloadFileConfig.FILE_GRAMMARS_JSON;
            File fileGrammar = new File(filePath_grammars);

            if (fileGrammar.exists()) {
                if (getStringFromFile(filePath_grammars) != null) {
                    importGrammars(getStringFromFile(filePath_grammars));

                    // Tiến hành xoá các file chứa nội dung string.
                    try {
                        fileGrammar.delete();
                    } catch (Exception ex) {
                        // Show the log in development environment.
                        Log.d(LOG_TAG, "doInBackground(): " + ex.getMessage());
                    }
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

            if (realm != null) {
                // Insert into DB.
                new S06LessonList_Dac(realm).createLesson(lessonDao);
            }
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

            if (realm != null) {
                // Insert into DB.
                S06LessonList_Dac s06LessonList_Dac = new S06LessonList_Dac(realm);
                for (KnowledgeDao knowledgeDao : knowledgeDaos) {
                    s06LessonList_Dac.createKnowledge(knowledgeDao);
                }
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

            if (realm != null) {
                // Insert into DB.
                S06LessonList_Dac s06LessonList_Dac = new S06LessonList_Dac(realm);
                for (QuestionDao questionDao : questionDaos) {
                    s06LessonList_Dac.createQuestion(questionDao);
                }
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

            if (realm != null) {
                // Insert into DB.
                S06LessonList_Dac s06LessonList_Dac = new S06LessonList_Dac(realm);
                for (ChoiceDao choiceDao : choiceDaoList) {
                    s06LessonList_Dac.createChoice(choiceDao);
                }
            }
        }
    }

    /**
     * Method dùng để import GrammarDao vô database.
     *
     * @param jsonData Json of object grammars.
     */
    private void importGrammars(String jsonData) {
        // Convert json data thành đối tượng.
        List<GrammarDao> grammarList = JsonParserImport.parse_Grammars(jsonData);

        if (grammarList != null && !grammarList.isEmpty()) {
            // Create Realm object.
            Realm realm = Realm.getDefaultInstance();

            if (realm != null) {
                // Insert into DB.
                S06LessonList_Dac s06LessonList_Dac = new S06LessonList_Dac(realm);
                for (GrammarDao grammarDao : grammarList) {
                    s06LessonList_Dac.createGrammar(grammarDao);
                }
            }
        }
    }
}
