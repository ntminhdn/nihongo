package com.honkidenihongo.pre.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.honkidenihongo.pre.R;

import java.io.IOException;

/**
 * Created by datpt on 7/19/16.
 */
public class AudioService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    /**
     * Create interface implement event.
     */
    public interface PlayAudioCallback {
        // Method lắng nghe file âm thanh từ device chạy xong.
        void onPlayAudioFinish();

        // Method lắng nghe 1 trong 2 file âm thanh đúng sai chạy xong rename later.
        void onPlayAudioCompletion();
    }

    private static final String LOG_TAG = AudioService.class.getSimpleName();
    private static final String WRONG_PATH = "WrongAudio";
    private static final String CORRECT_PATH = "CorrectAudio";

    private final IBinder mAudioBinder = new AudioBinder();

    private String mAudioPath;

    private boolean mIsPlaying = false;
    private boolean mIsPrepared = false;

    private MediaPlayer mMediaPlayer;
    private PlayAudioCallback mPlayAudioCallback;

    // Biến để check âm thanh và đúng/sai hay file âm thanh.
    private boolean mIsAudioEffect = false;

    /**
     * Set call back để lắng nghe âm thanh chạy xong hay chưa.
     *
     * @param callback Value PlayAudioCallback.
     */
    public void setPlayAudioCallback(PlayAudioCallback callback) {
        mPlayAudioCallback = callback;
    }

    /**
     * Class AudioBinder.
     */
    public class AudioBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAudioBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initAudioPlayer();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        release();

        mMediaPlayer = null;
        return false;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.e(LOG_TAG, "onError()");

        mIsPrepared = false;
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(LOG_TAG, "onPrepared()");

        mIsPrepared = true;

        play();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(LOG_TAG, "onCompletion");

        mIsPlaying = false;

        // Audio đang chạy là file audio đúng or sai.
        if (mIsAudioEffect) {
            if (mPlayAudioCallback != null) {
                mPlayAudioCallback.onPlayAudioCompletion();
            }
        } else { // File audio đang chạy là file đọc từ device.
            if (mPlayAudioCallback != null) {
                mPlayAudioCallback.onPlayAudioFinish();
            }
        }
    }

    /**
     * InitPlayer.
     */
    private void initAudioPlayer() {
        /*
         * Release the MediaPlayer objects if they are still valid.
		 */
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
            mMediaPlayer = new MediaPlayer();
        }

        // Set the mediaPlayers' stream sources.
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // Todo hiện tại không can thiệp vào hệ thống để set max cho file voice.
//        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        // Set listen for Mp3.
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
    }

    /**
     * Play audio là file.
     *
     * @param audioPath Url of file.
     * @param offset    Start.
     * @param duration  End.
     */
    public void playAudio(String audioPath, int offset, int duration) {
        Log.d(LOG_TAG, "playAudio( " + audioPath + ", " + offset + ", " + duration + ")");

        if (mIsPlaying) {
            return;
        }

        mIsAudioEffect = false;
        if (audioPath.equalsIgnoreCase(mAudioPath)) {
            if (mIsPrepared) {
                play();
            } else {
                open();
            }
        } else {
            mAudioPath = audioPath;
            reset();
            open();
        }
    }

    /**
     * Chạy file âm thanh với link path của nó.
     *
     * @param filePath Url of file.
     */
    public void playAudioWithPath(String filePath) {
        if (mIsPlaying) {
            return;
        }

        // Chạy file ấm thanh, trường hợp này chạy file đúng/sai sẽ bằng false.
        mIsAudioEffect = false;

        if (filePath.equalsIgnoreCase(mAudioPath)) {
            if (mIsPrepared) {
                play();
            } else {
                open();
            }
        } else {
            mAudioPath = filePath;

            reset();
            open();
        }
    }

    /**
     * Chạy audio đúng/sai.
     *
     * @param isCorrect Value/True hay false.
     */
    public void playEffectAudio(boolean isCorrect) {
        if (mIsPlaying) {
            return;
        }

        mIsAudioEffect = true;

        if (mAudioPath != null && ((mAudioPath.equalsIgnoreCase(WRONG_PATH) && !isCorrect) || (mAudioPath.equalsIgnoreCase(CORRECT_PATH) && isCorrect))) {

            if (mIsPrepared) {
                play();
            } else {
                openEffectAudio(isCorrect);
            }
        } else {
            if (isCorrect) {
                mAudioPath = CORRECT_PATH;
            } else {
                mAudioPath = WRONG_PATH;
            }

            openEffectAudio(isCorrect);
        }
    }

    /**
     * Mở file âm thanh đúng or sai.
     *
     * @param isCorrect Value true/false.
     */
    private void openEffectAudio(boolean isCorrect) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        }

        AssetFileDescriptor afd;
        if (isCorrect) {
            afd = getResources().openRawResourceFd(R.raw.correct);
        } else {
            afd = getResources().openRawResourceFd(R.raw.wrong);
        }

        try {
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mMediaPlayer.prepare();
        } catch (IOException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
    }

    /**
     * Mở file âm anh trong bộ nhớ mấy.
     */
    private void open() {
        try {
            mMediaPlayer.setDataSource(mAudioPath);
//            mMediaPlayer.setDataSource(this, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), mAudioPath)));
            mMediaPlayer.prepare();
        } catch (IOException e) {
            Log.d(LOG_TAG, e.getMessage());
            mIsPrepared = false;
            mIsPlaying = false;
        }
    }

    /**
     * Chạy file âm thanh.
     */
    private void play() {
        Log.d(LOG_TAG, "play()");

        if (mIsPlaying) {
            return;
        }

        try {
            mMediaPlayer.seekTo(0);
            mMediaPlayer.start();

            mIsPlaying = true;
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
            Log.d(LOG_TAG, "Play Audio error!");
        }
    }

    /**
     * Tạm dừng file âm thanh.
     */
    public void pause() {
        try {
            mMediaPlayer.pause();
            mIsPlaying = false;
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
            Log.d(LOG_TAG, "Pause Audio error!");
        }
    }

    /**
     * Reset file âm thanh.
     */
    private void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();

            mIsPrepared = false;
            mIsPlaying = false;
        }
    }

    /**
     * Dừng và khởi tạo lại.
     */
    private void release() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            } catch (Exception e) {
                Log.d(LOG_TAG, e.getMessage());
                Log.d(LOG_TAG, "Release audio error!");
            }
        }
    }
}
