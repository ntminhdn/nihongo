package com.honkidenihongo.pre.gui.cropphoto;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.util.BitmapUtils;
import com.honkidenihongo.pre.common.util.LocaleHelper;
import com.honkidenihongo.pre.common.util.ToolbarUtil;
import com.honkidenihongo.pre.gui.widget.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Screen using crop list photo choose.
 *
 * @author BinhDT
 */
public class CropPhotoActivity extends AppCompatActivity {
    public static final String LOG_TAG = CropPhotoActivity.class.getName();
    public static final String PATH_PHOTO_RECEIVE = "PATH_PHOTO_RECEIVE";
    private String mPathPhotoCropped;

    // For View.
    private Toolbar mToolbar;
    private CropImageView mCropImageView;
    private AppCompatTextView mTvTitleToolbar;
    private AppCompatButton mBtnCrop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getDataIntent() == null) {
            finish();

            // Tắt màn hình hiện tại và return.
            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Get layout for window.
        setContentView(R.layout.activity_crop_image);

        // Init View.
        initView();

        // Init toolbar.
        ToolbarUtil.initToolbar(mToolbar, this);

        // Set data.
        setData();

        // Set event.
        setEvent();
    }

    /**
     * Phương thức dùng để lắng nghe sự thay đổi ngôn ngữ của app.
     *
     * @param base Value context.
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(0, 0);
    }

    /**
     * Lưu ảnh sau khi cắt.
     *
     * @param context Value context of screen current.
     * @return True if save success else.
     */
    private boolean saveBitmapToFolder(Context context) {
        if (mCropImageView.getCroppedBitmap() == null) {
            return false;
        }

        boolean fileCreated = false;
        boolean bitmapCompressed = false;
        boolean streamClosed = false;

        // Thư mục Cache của App.
        File cacheDir = context.getCacheDir();

        String filePath = cacheDir.getPath();

        File fileMake = new File(filePath);

        if (!fileMake.exists()) {
            fileMake.mkdir();
        }

        mPathPhotoCropped = filePath + "/" + "image" + System.currentTimeMillis() + ".png";

        File imageFile = new File(mPathPhotoCropped);

        if (imageFile.exists())
            if (!imageFile.delete()) {
                return false;
            }

        try {
            fileCreated = imageFile.createNewFile();

        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(imageFile);
            bitmapCompressed = mCropImageView.getCroppedBitmap().compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            bitmapCompressed = false;

        } finally {
            if (outputStream != null) {

                try {
                    outputStream.flush();
                    outputStream.close();
                    streamClosed = true;
                } catch (IOException e) {
                    streamClosed = false;
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        }

        return (fileCreated && bitmapCompressed && streamClosed);
    }

    /**
     * Get data through Intent.
     */
    private String getDataIntent() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getString(PATH_PHOTO_RECEIVE);
        }

        return null;
    }

    /**
     * Init View.
     */
    private void initView() {
        mCropImageView = (CropImageView) findViewById(R.id.mImgCrop);
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mBtnCrop = (AppCompatButton) mToolbar.findViewById(R.id.mBtnCrop);
        mTvTitleToolbar = (AppCompatTextView) mToolbar.findViewById(R.id.mTvTitleToolbar);
    }

    /**
     * Method set data to view.
     */
    private void setData() {
        mTvTitleToolbar.setText(getString(R.string.common_app__name));
        String path = getDataIntent();
        Uri uri = BitmapUtils.getImageUri(path);
        Bitmap bitmap = BitmapUtils.getBitmap(this, uri);

        if (!TextUtils.isEmpty(path) && uri != null && bitmap != null) {
            mCropImageView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, getString(R.string.common_image__content_error__photo_file_too_big), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Method using set event for view.
     */
    private void setEvent() {
        mBtnCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveBitmapToFolder(CropPhotoActivity.this)) {
                    Intent intent = getIntent();
                    intent.setData(BitmapUtils.getImageUri(mPathPhotoCropped));
                    setResult(RESULT_OK, intent);

                    finish();
                }
            }
        });
    }
}
