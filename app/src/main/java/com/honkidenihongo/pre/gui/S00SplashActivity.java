package com.honkidenihongo.pre.gui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.caverock.androidsvg.SVG;
import com.honkidenihongo.pre.R;
import com.honkidenihongo.pre.common.config.AppConfig;
import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.common.util.ApplicationUpdateUtil;
import com.honkidenihongo.pre.common.util.LocalAppUtil;
import com.honkidenihongo.pre.common.util.LoginUtil;
import com.honkidenihongo.pre.common.util.LogoutUtil;
import com.honkidenihongo.pre.gui.auth.S01Login_Activity;
import com.honkidenihongo.pre.gui.widget.svg.SvgDecoder;
import com.honkidenihongo.pre.gui.widget.svg.SvgDrawableTranscoder;
import com.honkidenihongo.pre.gui.widget.svg.SvgSoftwareLayerSetter;
import com.honkidenihongo.pre.model.UserModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.realm.Realm;

/**
 * Màn hình splash hiển thị logo của app.
 *
 * @author binh.dt.
 * @since 12-Nov-2016.
 */
public class S00SplashActivity extends AppCompatActivity {
    /**
     * The Tag for logging.
     */
    private static final String LOG_TAG = S00SplashActivity.class.getName();
    private static final int TIME_DELAY = 2000;
    private static final String SLASH_FILE = "/";

    private AppCompatImageView mImgLogo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.s00_splash_activity);

        /**
         * Method init view
         */
        initView();

        // Nếu phát hiện có dấu vết đã Login trước đó thì đến thẳng màn hình chính, nếu không thì đến màn hình Login.
        if (existLastLoginUser()) {
            // Thực hiện logout đối với app lên store có version code khác với app version current được update.
            if (AppConfig.getVersionCode() != LoginUtil.getVersionCodeCurrent(S00SplashActivity.this)) {
                LogoutUtil.handleLogoutUser(S00SplashActivity.this);

                // Logout and return for user login again.
                return;
            }

            // Show the log in development environment.
            Log.i(LOG_TAG, "onCreate(): Exist last Login User -> goto Main Screen.");

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Processing update data of folder.
                    ApplicationUpdateUtil.updateFolder(S00SplashActivity.this);

                    Intent mainIntent = new Intent(S00SplashActivity.this, MainActivity.class);
                    startActivity(mainIntent);

                    // Đóng màn hình Splash này.
                    finish();
                }
            }, TIME_DELAY);
        } else {
            // Show the log in development environment.
            Log.i(LOG_TAG, "onCreate(): Not exist last Login User -> goto Login Screen.");


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent loginIntent = new Intent(S00SplashActivity.this, S01Login_Activity.class);
                    startActivity(loginIntent);

                    // Đóng màn hình Splash này.
                    finish();
                }
            }, TIME_DELAY);
        }
    }

    /**
     * Method int view for window.
     */
    private void initView() {
        mImgLogo = (AppCompatImageView) findViewById(R.id.mImgLogo);

        // Needed because of image accelaration in some devices such as samsung.
        mImgLogo.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        loadPhotoLogo();
    }

    /**
     * Method using load photo logo app.
     */
    private void loadPhotoLogo() {
        GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable>
                requestBuilder = Glide.with(S00SplashActivity.this)
                .using(Glide.buildStreamModelLoader(Uri.class, S00SplashActivity.this), InputStream.class)
                .from(Uri.class)
                .as(SVG.class)
                .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                .sourceEncoder(new StreamEncoder())
                .cacheDecoder(new FileToStreamDecoder<>(new SvgDecoder()))
                .decoder(new SvgDecoder())
                .placeholder(null)
                .error(null)
                .animate(android.R.anim.fade_in)
                .listener(new SvgSoftwareLayerSetter<Uri>());

        clearCache(requestBuilder);
    }

    /**
     * Method using clear memory cash.
     *
     * @param requestBuilder Value object GenericRequestBuilder.
     */
    private void clearCache(GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder) {
        Glide.clear(mImgLogo);
        Glide.get(S00SplashActivity.this).clearMemory();
        File cacheDir = Glide.getPhotoCacheDir(S00SplashActivity.this);

        if (cacheDir.isDirectory()) {
            for (File child : cacheDir.listFiles()) {
                if (!child.delete()) {
                    Log.w(LOG_TAG, "cannot delete: " + child);
                }
            }
        }

        // Load file svg.
        requestBuilder.diskCacheStrategy(DiskCacheStrategy.NONE)
                // SVG cannot be serialized so it's not worth to cache it.
                // and the getResources() should be fast enough when acquiring the InputStream.
                .load(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + ":" + SLASH_FILE + SLASH_FILE + this.getPackageName() + SLASH_FILE + R.raw.ic_s00_splash))
                .into(mImgLogo);
    }

    /**
     * Method kiểm tra xem đã có người đăng nhập trước đó hay chưa?
     *
     * @return Nếu phát hiện có dấu vết đã Login trước đó thì trả về true, nếu không thì trả về false.
     */
    private boolean existLastLoginUser() {
        UserModel lastUserModel = LocalAppUtil.getLastLoginUserInfo(S00SplashActivity.this);

        // Nếu có thông tin User Id hợp lệ đã từng ở Local (SharedPreferences) thì trả về true, nếu không thì trả về false.
        return lastUserModel != null && lastUserModel.id > 0;

    }
}
