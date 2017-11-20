package com.brucetoo.wifitransport.HotPot.video;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.brucetoo.wifitransport.R;
import com.brucetoo.wifitransport.HotPot.Utils;

import java.io.IOException;


/**
 * Created by Bruce Too
 * On 7/12/16.
 * At 16:13
 */
public class VideoPlayDialogActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, VideoControllerView.MediaPlayerControlListener, MediaPlayer.OnVideoSizeChangedListener {

    private ResizeSurfaceView mSurfaceView;
    private ProgressBar mLoadingView;
    private FrameLayout mVideoContainer;
    private View mRootView;
    VideoControllerView mControllerView;
    SurfaceHolder mSurfaceHolder;
    MediaPlayer mMediaPlayer;
    String mTitle;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_play_video);

        mSurfaceView = (ResizeSurfaceView) findViewById(R.id.video_surface);
        mLoadingView = (ProgressBar) findViewById(R.id.progress);
        mVideoContainer = (FrameLayout) findViewById(R.id.video_container);
        mRootView = findViewById(R.id.layout_root);

        mTitle = getIntent().getStringExtra("title");
        mControllerView = new VideoControllerView(this);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        mMediaPlayer = new MediaPlayer();
        mLoadingView.setVisibility(View.VISIBLE);
        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mControllerView.toggleControllerView();
                return false;
            }
        });

        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(this, Uri.parse(getIntent().getStringExtra("url")));
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);
        } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        mMediaPlayer.setDisplay(holder);
        mMediaPlayer.prepareAsync();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        resetPlayer();
    }

    private int mVideoWidth;
    private int mVideoHeight;

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        mVideoHeight = mp.getVideoHeight();
        mVideoWidth = mp.getVideoWidth();
        if (mVideoHeight > 0 && mVideoWidth > 0)
            mSurfaceView.adjustSize(mRootView.getWidth(), mRootView.getHeight(), mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mVideoWidth > 0 && mVideoHeight > 0)
            mSurfaceView.adjustSize(Utils.getDeviceWidth(this), Utils.getDeviceHeight(this), mVideoWidth, mVideoHeight);
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        mControllerView.setMediaPlayerControlListener(this);
        mLoadingView.setVisibility(View.GONE);
        mSurfaceView.setVisibility(View.VISIBLE);
        mControllerView.setAnchorView(mVideoContainer);
        mControllerView.setGestureListener();
        mMediaPlayer.start();
    }

    private void resetPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeek() {
        return true;
    }


    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (null != mMediaPlayer)
            return mMediaPlayer.getCurrentPosition();
        else
            return 0;
    }

    @Override
    public int getDuration() {
        if (null != mMediaPlayer)
            return mMediaPlayer.getDuration();
        else
            return 0;
    }

    @Override
    public boolean isPlaying() {
        if (null != mMediaPlayer)
            return mMediaPlayer.isPlaying();
        else
            return false;
    }

    @Override
    public void pause() {
        if (null != mMediaPlayer) {
            mMediaPlayer.pause();
        }

    }

    @Override
    public void seekTo(int position) {
        if (null != mMediaPlayer) {
            mMediaPlayer.seekTo(position);
        }
    }

    @Override
    public void start() {
        if (null != mMediaPlayer) {
            mMediaPlayer.start();
        }
    }

    @Override
    public boolean isFullScreen() {
        return getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }

    @Override
    public void toggleFullScreen() {
        if (isFullScreen()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void exit() {
        resetPlayer();
        finish();
    }

    @Override
    public String getTopTitle() {
        return mTitle;
    }
}
