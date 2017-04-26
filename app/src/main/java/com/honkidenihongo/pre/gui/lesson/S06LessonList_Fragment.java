package com.honkidenihongo.pre.gui.lesson;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.adapter.S06LessonList_Adapter;
import com.honkidenihongo.pre.api.JsonParserApi;
import com.honkidenihongo.pre.api.json.A25LessonList_JsonResult;
import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.IoUtil;
import com.honkidenihongo.pre.common.util.LocalAppUtil;
import com.honkidenihongo.pre.common.util.MessageDialogUtil;
import com.honkidenihongo.pre.common.util.NetworkUtil;
import com.honkidenihongo.pre.dac.S06LessonList_Dac;
import com.honkidenihongo.pre.dac.dao.LessonDao;
import com.honkidenihongo.pre.gui.MainActivity;
import com.honkidenihongo.pre.gui.listener.NetworkConnectionCallback;
import com.honkidenihongo.pre.gui.listener.OnMainActivityListener;
import com.honkidenihongo.pre.model.Lesson;
import com.honkidenihongo.pre.model.UserModel;
import com.honkidenihongo.pre.model.constant.LessonStatus;
import com.honkidenihongo.pre.service.DownloadLessonBroadcastReceiver;
import com.honkidenihongo.pre.service.DownloadLessonService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Screen display lesson list: S06LessonList.
 *
 * @author binh.tt.
 * @since 08-Nov-2016.
 */
public class S06LessonList_Fragment extends Fragment {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S06LessonList_Fragment.class.getName();

    public Context mContext;

    // For view.
    private RelativeLayout mRlDownloadAll;
    private RecyclerView mRecyclerViewLesson;
    private AppCompatTextView mTvEmptyData;
    private LinearLayout mViewContent;
    private ProgressDialog mProgressDialog;

    private OnMainActivityListener mActivityListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = getActivity();

        if (mContext instanceof MainActivity) {
            mActivityListener = (MainActivity) mContext;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.s06_lesson_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Init controls in the layout.
        initView(view);

        // Init data for displaying.
        initData();

        // Set events.
        setEvent();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mActivityListener != null) {
            mActivityListener.setTitleScreen(getString(R.string.common_module__lesson_list));
        }

        /**
         * Khởi tạo BroadcastReceiver lắng nghe dữ liệu gởi về.
         */
        intBroadCast();
    }

    /**
     * Receive update prefecture list
     */
    private BroadcastReceiver mDownloadLessonReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Tiến hành update ui khi tải lesson thành công hoặc error.
            int position = intent.getIntExtra(DownloadLessonBroadcastReceiver.ARG_POSITION, 0);
            int statusLesson = intent.getIntExtra(DownloadLessonBroadcastReceiver.ARG_STATUS_DOWNLOAD_LESSON, LessonStatus.UN_DOWNLOADED);

            Log.d(LOG_TAG, "onReceive: position== " + position);

            S06LessonList_Adapter mS06LessonListAdapter = (S06LessonList_Adapter) mRecyclerViewLesson.getAdapter();

            if (mS06LessonListAdapter == null) {
                return;
            }

            // Kiểm tra vị trí trả về phải bé hơn list data.
            if (position < mS06LessonListAdapter.getItemCount()) {
                Lesson lesson = mS06LessonListAdapter.getItem(position);

                if (lesson == null) {
                    return;
                }

                // Kiểm tra trạng thái trả về của download là lỗi thiếu dung lượng bộ nhớ để download bài học hay không?
                if (statusLesson == IoUtil.ERROR_MEMORY) {
                    MessageDialogUtil.showNotificationDialog(mContext, mContext.getString(R.string.common_msg__title__warning), mContext.getString(R.string.common_msg__content_error__not_enough_memory_disk));

                    // Cập nhật trạng thái của lesson download trong trường hợp không đủ bộ nhớ sẽ là error.
                    lesson.status = LessonStatus.DOWNLOAD_ERROR;
                } else {
                    // Cập nhật trạng thái của lesson.
                    lesson.status = statusLesson;
                }

                mS06LessonListAdapter.notifyItemChanged(position);

                // Cập nhật vô lại SharedPreferences trạng thái mới nhất của list lesson.
                LocalAppUtil.saveLastLessonList(context, mS06LessonListAdapter.getData());

                // Ẩn text download all đi khi đã tải hết và ngược lại.
                mRlDownloadAll.setVisibility(mS06LessonListAdapter.countTotalLessonDownloaded() == 0 ? View.GONE : View.VISIBLE);
            }
        }
    };

    /**
     * Đăng ký nhận data from broadcast.
     */
    private void intBroadCast() {
        IntentFilter filter = new IntentFilter(DownloadLessonBroadcastReceiver.FILTER_DOWNLOAD_LESSON);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mDownloadLessonReceive, filter);
    }

    /**
     * Method using init View.
     */
    private void initView(View view) {
        // Xác định các controls trên GUI.
        mViewContent = (LinearLayout) view.findViewById(R.id.mViewContent);
        mRlDownloadAll = (RelativeLayout) view.findViewById(R.id.mRlDownloadAll);
        mRecyclerViewLesson = (RecyclerView) view.findViewById(R.id.mRecyclerView);
        mTvEmptyData = (AppCompatTextView) view.findViewById(R.id.mTvEmptyData);

        // Khởi tạo sẵn dialog.
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(getString(R.string.common_msg__content_info__processing));
        mProgressDialog.setCancelable(false);

        // Khởi tạo layoutManager cho recycleView.
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerViewLesson.setLayoutManager(layoutManager);
        mRecyclerViewLesson.setHasFixedSize(true);
    }

    /**
     * Method using set data for View.
     */
    private void initData() {
        // Nếu có kết nối mạng.
        if (NetworkUtil.isAvailable(mContext)) {
            // Lấy (asynchronous) LessonList mới nhất từ API Server.
            // Vì asynchronous nên việc set Adapter và GUI sẽ ở luôn trong method này.
            requestAsyncLessonListFromServer();
        } else {
            // Hiện thị data khi không có kết nối mạng.
            checkLessonListFromLocal();

//            // Tạo Adapter.
//            S06LessonList_Adapter mS06LessonListAdapter = new S06LessonList_Adapter(mContext, mLessons, this);
//            mRecyclerViewLesson.setAdapter(mS06LessonListAdapter);
//
//            // Cập nhật UI.
//            mS06LessonListAdapter.notifyDataSetChanged();
        }

    }

    /**
     * Method using set Event for View inside screen.
     */
    private void setEvent() {
        // Xử lý việc Download All: Sẽ download lần lượt theo vị trí từ trên xuống dưới.
        mRlDownloadAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Xử lý sự kiện click vào Download ALl.
                downloadAll_Clicked();
            }
        });
    }

    /**
     * Method using request api.
     */
    private void requestAsyncLessonListFromServer() {
          /* Bước 1: Bật hộp thoại chờ request lên server. */
        // Cẩn thận luôn khởi tạo mới.
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(getString(R.string.common_msg__content_info__processing));
        mProgressDialog.setCancelable(false);

        mProgressDialog.show();

        // Lấy thông tin user hiện thời vừa đăng nhập để lấy Access-Token hợp lệ.
        UserModel userModel = LocalAppUtil.getLastLoginUserInfo(mContext);
        String accessToken = null;
        if (userModel != null && userModel.tokenInfo != null) {
            accessToken = userModel.tokenInfo.access_token;
        }

        /* Bước 2: Request lên API Server để lấy danh sách Lesson List. */
        // Chuẩn bị dữ kiện để gửi lên Server.
        String authHeader = String.format("%s %s", Definition.Request.HEADER_BEARER2, accessToken);
        Request getLessonsRequest = new Request.Builder()
                .url(AppConfig.getApiBaseUrl() + Definition.API.GET_LESSONS)
                .header(Definition.Request.HEADER_ACCEPT, Definition.Constants.VALUE_ACCEPT)
                .header(Definition.Request.HEADER_AUTHORIZATION, authHeader)
                .header(Definition.Request.PARAM_CLIENT_TOKEN, AppConfig.getClientToken())
                .build();

        // Get OkHttpClient object with default timeout configurations.
        OkHttpClient okHttpClient = NetworkUtil.getDefaultHttpClient(mContext);

        // Tiến hành gửi request bất đồng bộ (asynchronous) lên Server.
        okHttpClient.newCall(getLessonsRequest).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Khởi tạo danh sách Lessons List (đang tìm danh sách Lesson mới nhất từ Server hoặc từ Local).
                List<Lesson> latestLessonList = new ArrayList<>();

                // Nếu có Response thành công thì parse để lấy dữ liệu.
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    final String responseString = response.body().string();

                    // Parser data.
                    A25LessonList_JsonResult jsonResult = JsonParserApi.parse_A25LessonList(responseString);

                    if (jsonResult != null && jsonResult.data != null && !jsonResult.data.isEmpty()) {
                        List<LessonDao> lessonDaoList = jsonResult.data;

                        for (LessonDao lessonDao : lessonDaoList) {
                            latestLessonList.add(convertObject(lessonDao));
                        }
                    }
                }

                // Nếu đã xác định được danh sách LessonList từ Server hoặc Local.
                if (!latestLessonList.isEmpty()) {
                    // Kết hợp với Database để biết được thông tin đã download hay chưa.
                    updateInfoWithDatabase(latestLessonList);

                    // Sau khi update với dữ liệu ở local xong thì tiến hành cập nhật vô lại SharedPreferences trạng thái mới nhất của list lesson.
                    LocalAppUtil.saveLastLessonList(mContext, latestLessonList);
                } else {
                    // Nếu không lấy LessonList được từ server thì lấy từ Local đã lưu lần gần nhất.
                    latestLessonList = LocalAppUtil.getLastLessonList(mContext);
                }

                /* Hiển thị cập nhật lên GUI. */
                processUiWithLessonList((Activity) mContext, latestLessonList);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // Khi request Failure tức không lấy LessonList được từ server thì lấy từ Local đã lưu lần gần nhất.
                List<Lesson> latestLessonList = LocalAppUtil.getLastLessonList(mContext);

                // Nếu đã xác định được danh sách LessonList từ Local.
                if (latestLessonList != null && !latestLessonList.isEmpty()) {
                    // Kết hợp với Database để biết được thông tin đã cài đặt hay chưa.
                    updateInfoWithDatabase(latestLessonList);
                }

                /* Hiển thị cập nhật lên GUI. */
                processUiWithLessonList((Activity) mContext, latestLessonList);
            }
        });
    }

    /**
     * Chạy trong UI Thread để xử lý khi cập nhật thông tin danh sách Lesson List.
     */
    private void processUiWithLessonList(Activity activity, final List<Lesson> finalLessonList) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Nếu danh sách Lesson rỗng.
                if (finalLessonList == null || finalLessonList.isEmpty()) {
                    mViewContent.setVisibility(View.GONE);
                    mTvEmptyData.setVisibility(View.VISIBLE);
                    mProgressDialog.dismiss();

                    if (isAdded() && !isDetached() && !isRemoving()) {
                        MessageDialogUtil.showNotificationDialog(mContext, mContext.getString(R.string.common_msg__title__error), mContext.getString(R.string.s06_lesson_list__content_error__get_lesson_list_failure));
                    }
                } else {
                    // Tạo Adapter.
                    S06LessonList_Adapter mS06LessonListAdapter = new S06LessonList_Adapter(mContext, finalLessonList, S06LessonList_Fragment.this);
                    mRecyclerViewLesson.setAdapter(mS06LessonListAdapter);

                    // Cập nhật UI.
                    mS06LessonListAdapter.notifyDataSetChanged();

                    // Ẩn text download all đi khi đã tải hết và ngược lại.
                    mRlDownloadAll.setVisibility(mS06LessonListAdapter.countTotalLessonDownloaded() == 0 ? View.GONE : View.VISIBLE);

                    // Tắt show dialog.
                    mProgressDialog.dismiss();

                    // Sau khi update với dữ liệu ở local xong thì tiến hành cập nhật vô lại SharedPreferences trạng thái mới nhất của list lesson.
                    LocalAppUtil.saveLastLessonList(mContext, finalLessonList);
                }
            }
        });
    }

    /**
     * Method using convert from LessonDao list to Lesson list.
     */
    private Lesson convertObject(LessonDao lessonDao) {
        Lesson lesson = new Lesson();

        lesson.setId(lessonDao.id);
        lesson.setType(lessonDao.type);
        lesson.setNumber(lessonDao.number);
        lesson.setTitle_vi(lessonDao.title_vi);
        lesson.setTitle_en(lessonDao.title_en);
        lesson.setTitle_ja(lessonDao.title_ja);

        if (lessonDao.status != null) {
            System.out.println(lesson.status + "Trước đó +++++++++++");
            lesson.status = lessonDao.status;
            System.out.println(lesson.status + "Sau đó +++++++++++");
        }

        lesson.setDescription(lessonDao.description);
        lesson.setVersion(lessonDao.version);

        return lesson;
    }

    /**
     * Kết hợp với danh sách S06LessonList_Dac(Realm.getDefaultInstance()).getLessonIdList();Lesson List từ Database để biết được Lesson nào đã Download và Install OK rồi.
     */
    private void updateInfoWithDatabase(List<Lesson> lessonList) {
        List<Lesson> lessonsDatabase = new S06LessonList_Dac(Realm.getDefaultInstance()).readLessonList();

        // Dữ liệu từ server và local phải có dữ liệu mới thực hiện so sánh.
        if (!lessonsDatabase.isEmpty() && !lessonList.isEmpty()) {
            for (Lesson lessonServer : lessonList) {

                for (Lesson lessonDatabase : lessonsDatabase) {
                    if (lessonServer.getId() == lessonDatabase.getId()) {

                        // Kiểm tra version của lesson từ json trả về và version của lesson đã được cài đặt.
                        if (lessonDatabase.getVersion() < lessonServer.getVersion()) {
                            lessonServer.status = LessonStatus.UPDATE;
                        } else {
                            lessonServer.status = LessonStatus.DOWNLOADED;
                        }

                        // Dừng ở đây và tiếp tục chạy vòng for ngoài.
                        break;
                    }
                }
            }
        }

        //       // Kiểm tra danh sách đối tượng từ API.
//        if (!lessonList.isEmpty()) {
//            // Cập nhật trạng thái mới nhất của lesson list vào shared preferences file.
//            LocalAppUtil.saveLastLessonList(mContext, lessonList);
//

//            // Compare lesson list từ API và lesson list từ database.
//            if (!lessonListFromDatabase.isEmpty()) {
//                // Lấy chuẩn từ API , cập nhật lại tình trạng của lesson (Nếu DB thừa thì xóa, DB thiếu thì hiện thị trạng thái chưa download).
//                for (Lesson lesson2API : lessonList) {
//                    for (Lesson lesson2DB : lessonListFromDatabase) {
//                        if (lesson2DB.getId() == lesson2API.getId()) {
//                            lesson2API.setInstalled(true);
//                        }
//                    }
//                }
//
//                mLessons.addAll(lessonList);
//            } else {
//                // Lấy danh sách theo api và hiện thị trạng thái chưa download.
//                mLessons.addAll(lessonList);
//            }
//        } else { // List data từ API get về là rỗng.
//            if (lessonListFromDatabase.isEmpty()) {
//                mTvEmptyData.setVisibility(View.VISIBLE);
//                mViewContent.setVisibility(View.GONE);
//            } else {
//                // Lấy list lesson từ database.
//                mLessons.addAll(lessonListFromDatabase);
//            }
//        }

        // Đồng thời kiểm tra danh sách từ sharedPreferences để biết lesson ở trạng thái downloading hoặc waiting cập nhập lên ui khi mở lại màn hình.
        List<Lesson> lessonListPreferences = LocalAppUtil.getLastLessonList(mContext);

        if (lessonListPreferences != null && !lessonListPreferences.isEmpty()) {
            for (Lesson lesson : lessonList) {
                // So sánh con lesson từ server và lesson lưu dưới sharedPreferences phải cùng id.
                for (Lesson lessonLocal : lessonListPreferences) {
                    if (lesson.getId() == lessonLocal.getId()) {
                        // Điều kiện lesson từ server trả về trạng thái khác downloaded.
                        if (lesson.status != LessonStatus.DOWNLOADED && lessonLocal.status == LessonStatus.WAITING_UPDATE || lessonLocal.status == LessonStatus.UPDATING || lessonLocal.status == LessonStatus.WAITING || lessonLocal.status == LessonStatus.DOWNLOADING) {
                            lesson.status = lessonLocal.status;
                        }
                    }
                }
            }
        }
    }

    /**
     * Methoad hiển thị data khi không có kết nối mạng lên giao diện người dùng.
     */
    private void checkLessonListFromLocal() {
        // Lấy danh sách lesson từ share preferences.
        List<Lesson> lessonListPreferences = LocalAppUtil.getLastLessonList(mContext);

        // Lấy danh sách lesson từ Database.
        List<Lesson> lessonListFromDataBase = new S06LessonList_Dac(Realm.getDefaultInstance()).readLessonList();

        // Danh sách lesson trong Share preferences là khác rỗng.
        if (lessonListPreferences != null && !lessonListPreferences.isEmpty() && !lessonListFromDataBase.isEmpty()) {

            // Dữ liệu từ server và local phải có dữ liệu mới thực hiện so sánh.
            for (Lesson lessonPreference : lessonListPreferences) {

                for (Lesson lessonDatabase : lessonListFromDataBase) {
                    if (lessonPreference.getId() == lessonDatabase.getId()) {

                        // Kiểm tra version của lesson từ Preference trả về và version của lesson đã được cài đặt.
                        if (lessonDatabase.getVersion() < lessonPreference.getVersion()) {
                            lessonPreference.status = LessonStatus.UPDATE;
                        } else {
                            lessonPreference.status = LessonStatus.DOWNLOADED;
                        }

                        // Dừng ở đây và tiếp tục chạy vòng for ngoài.
                        break;
                    }
                }
            }

//            if (lessonListFromDataBase.isEmpty()) {
//                // Lấy all lesson trong Share preferences.
//                mLessons.addAll(lessonListPreferences);
//            } else {
//                // Lấy chuẩn từ SharedPreferencesKey, cập nhật lại tình trạng của lesson.
//                for (Lesson lesson2Pre : lessonListPreferences) {
//                    for (Lesson lesson2DB : lessonListFromDataBase) {
//                        if (lesson2DB.getId() == lesson2Pre.getId()) {
//                            lesson2Pre.setInstalled(true);
//                        }
//                    }
//                }
//
//                mLessons.addAll(lessonListPreferences);
//            }

            // Cập nhật lại UI.
            processUiWithLessonList((Activity) mContext, lessonListPreferences);
        } else {
            mTvEmptyData.setVisibility(View.VISIBLE);
            mViewContent.setVisibility(View.GONE);
        }
    }

    /**
     * Process when the Download All button is clicked.
     */
    private void downloadAll_Clicked() {
        if (NetworkUtil.isAvailable(mContext)) {
            S06LessonList_Adapter mS06LessonListAdapter = (S06LessonList_Adapter) mRecyclerViewLesson.getAdapter();

            // Nếu không có Adapter, chứng tỏ chưa lấy được Lesson List, nên không cho Download.
            if (mS06LessonListAdapter == null) {
                return;
            }

            List<Lesson> lessonList = mS06LessonListAdapter.getData();

            // Nếu không có Lesson List, không cho Download.
            if (lessonList == null || lessonList.isEmpty()) {
                return;
            }

            // Thực hiện download từng lesson.
            UserModel lastLoginUserInfo = LocalAppUtil.getLastLoginUserInfo(mContext);
//            mRlDownloadAll.setEnabled(false); todo...

            int itemCount = mS06LessonListAdapter.getItemCount();

            for (int position = 0; position < itemCount; position++) {
                Lesson lesson = mS06LessonListAdapter.getItem(position);

                // Lấy trạng thái của lesson.
                int statusLesson = lesson.status;

                // Kiểm tra xem if trạng thái của nó là download bị lỗi or chưa download thì tái khởi động download.
                if (statusLesson == LessonStatus.DOWNLOAD_ERROR || statusLesson == LessonStatus.UN_DOWNLOADED || statusLesson == LessonStatus.UPDATE) {
                    // Update trạng thái của con lesson được chọn là trạng thái đang download or waiting upload.
                    if (lesson.status == LessonStatus.UPDATE) {
                        lesson.status = LessonStatus.WAITING_UPDATE;
                    } else {
                        lesson.status = LessonStatus.WAITING;
                    }

                    mS06LessonListAdapter.notifyItemChanged(position);

                    // Gởi data và start service download.
                    Intent intent = new Intent(mContext, DownloadLessonService.class);
                    intent.putExtra(DownloadLessonService.LESSON_PARCELABLE_OBJECT_DOWNLOAD, lesson);
                    intent.putExtra(DownloadLessonService.POSITION_LESSON_PARCELABLE_OBJECT_DOWNLOAD, position);

                    mContext.startService(intent);
                }
            }

            // Update trạng thái của lesson list này xuống dưới SharedPreferences để khi load mở lại màn hình này
            // update status của lesson is downloading trên view.
            LocalAppUtil.saveLastLessonList(mContext, mS06LessonListAdapter.getData());
        } else {
            MessageDialogUtil.showNetworkUnavailableDialog(
                    mContext,
                    new NetworkConnectionCallback() {
                        @Override
                        public void onTryAgain() {
                            downloadAll_Clicked();
                        }
                    }
            );
        }
    }

    /**
     * Open public method using call from adapter.
     *
     * @param position Vị trí position xảy ra sự kiện Click.
     */
    public void onItemUpdateLessonClick(final int position) {
        S06LessonList_Adapter mS06LessonListAdapter = (S06LessonList_Adapter) mRecyclerViewLesson.getAdapter();

        if (mS06LessonListAdapter == null) {
            return;
        }

        Lesson lesson = mS06LessonListAdapter.getItem(position);

        // Nếu trạng thái lesson đang tải thì cũng return tránh download 2 lần.
        if (lesson != null && lesson.status == LessonStatus.UPDATE) {
            // Kiểm tra kết nối mạng trước khi gọi tiến trình download .
            if (NetworkUtil.isAvailable(mContext)) {
                // Update trạng thái của con lesson được chọn là trạng thái đang download watting update khi luồng download khởi động download lesson ở position nào thì sẽ bắn về cập nhật lại trạng thái lesson đang download.
                lesson.status = LessonStatus.WAITING_UPDATE;
                mS06LessonListAdapter.notifyItemChanged(position);

                // Gởi data và start service download.
                Intent intent = new Intent(mContext, DownloadLessonService.class);
                intent.putExtra(DownloadLessonService.LESSON_PARCELABLE_OBJECT_DOWNLOAD, lesson);
                intent.putExtra(DownloadLessonService.POSITION_LESSON_PARCELABLE_OBJECT_DOWNLOAD, position);

                mContext.startService(intent);

                // Update trạng thái của lesson list này xuống dưới SharedPreferences để khi load mở lại màn hình này
                // update status của lesson is downloading trên view.
                LocalAppUtil.saveLastLessonList(mContext, mS06LessonListAdapter.getData());
            } else {
                MessageDialogUtil.showNetworkUnavailableDialog(mContext, new NetworkConnectionCallback() {
                            @Override
                            public void onTryAgain() {
                                onItemUpdateLessonClick(position);
                            }
                        }
                );
            }
        }
    }

    /**
     * Method using call from adapter {@link S06LessonList_Adapter}.
     *
     * @param position Vị trí position xảy ra sự kiện Click.
     */
    public void onItemLessonsClick(final int position) {
        S06LessonList_Adapter mS06LessonListAdapter = (S06LessonList_Adapter) mRecyclerViewLesson.getAdapter();

        if (mS06LessonListAdapter == null) {
            return;
        }

        Lesson lesson = mS06LessonListAdapter.getItem(position);

        // Nếu trạng thái lesson đang tải thì cũng return tránh download 2 lần.
        if (lesson == null || lesson.status == LessonStatus.DOWNLOADING || lesson.status == LessonStatus.WAITING || lesson.status == LessonStatus.UPDATING) {
            return;
        }

        if (mActivityListener != null) {
            // Check lesson type.
            if (lesson.status == LessonStatus.DOWNLOADED || lesson.status == LessonStatus.LEARN_COMPLETED || lesson.status == LessonStatus.UPDATE) {
                mActivityListener.goToScreen_S06LessonList_Item(lesson);
            } else {
                // Kiểm tra kết nối mạng trước khi gọi tiến trình download.

                if (NetworkUtil.isAvailable(mContext)) {
                    // Update trạng thái của con lesson được chọn là trạng thái đang download watting khi luồng download khởi động download lesson ở position nào thì sẽ bắn về cập nhật lại trạng thái lesson đang download.
                    lesson.status = LessonStatus.WAITING;
                    mS06LessonListAdapter.notifyItemChanged(position);

                    // Gởi data và start service download.
                    Intent intent = new Intent(mContext, DownloadLessonService.class);
                    intent.putExtra(DownloadLessonService.LESSON_PARCELABLE_OBJECT_DOWNLOAD, lesson);
                    intent.putExtra(DownloadLessonService.POSITION_LESSON_PARCELABLE_OBJECT_DOWNLOAD, position);

                    mContext.startService(intent);

                    // Update trạng thái của lesson list này xuống dưới SharedPreferences để khi load mở lại màn hình này
                    // update status của lesson is downloading trên view.
                    LocalAppUtil.saveLastLessonList(mContext, mS06LessonListAdapter.getData());
                } else {
                    MessageDialogUtil.showNetworkUnavailableDialog(
                            mContext,
                            new NetworkConnectionCallback() {
                                @Override
                                public void onTryAgain() {
                                    onItemLessonsClick(position);
                                }
                            }
                    );
                }
            }
        }
    }

    // Todo hiện tại không dùng thread, dùng service để handler event download lesson.
//    /**
//     * Method dùng để gọi download lesson detail.
//     *
//     * @param userModel
//     * @param mRecyclerViewLesson
//     * @param position
//     */
//    private void downloadAndImportLesson(UserModel userModel, RecyclerView mRecyclerViewLesson, int position) {
//        S06LessonList_AsyncDownloadAndImporter_SingleLesson lessonListDownloadWorker = new S06LessonList_AsyncDownloadAndImporter_SingleLesson(mContext, userModel, mRecyclerViewLesson, position);
//        lessonListDownloadWorker.execute();
//    }
}
