package com.ynaito.mediaplayer_sample;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {
    private String TAG = MainActivity.class.getSimpleName();

    private Context mContext;
    private String uriString = "http://domain/path/content.mp4";
//    private String uriString = "https://tungsten.aaplimg.com/VOD/bipbop_adv_example_hevc/master.m3u8";
//    private String uriString = "https://tungsten.aaplimg.com/VOD/bipbop_adv_example_hevc/master.m3u8";
//    private String uriString = "https://tungsten.aaplimg.com/VOD/bipbop_adv_example_hevc/v13/prog_index.m3u8";
//    private String uriString = "https://tungsten.aaplimg.com/VOD/bipbop_adv_example_hevc/v10/prog_index.m3u8";
    private MediaPlayer mMediaPlayer = null;
    private SurfaceView mSurfaceView = null;
    private RelativeLayout mPlayerLayout = null;
//    private MySurfaceView mSurfaceView = null;

    private ImageView mPlayPauseImage = null;
    private Button mButton1 = null;
    private Button mButton2 = null;

    private SeekBar mSeekbar = null;
    private Handler mHandler = null;
    private MonitorTask monitorTask = null;
    private long POST_DELAY_MONITOR_TIME = 1000;
    private long POST_DELAY_HIDDEN_CONTROLLER_TIME = 3000;

    private GestureDetector gesture;

    private boolean visibleController = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        mContext = this;
        mHandler = new Handler();
//        mHandler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                MainActivity.handleMessage(msg);
//
//            }
//        };
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mPlayerLayout = (RelativeLayout) findViewById(R.id.playerLayout);
        mPlayPauseImage = (ImageView) findViewById(R.id.play_button_image);
        mSurfaceView.getHolder().addCallback(this);
        monitorTask = new MonitorTask();


        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer == null || !mMediaPlayer.isPlaying()) {
                    return;
                }
                //playing時のみshow/hide controllerを行う。
                if (!visibleController) {
                    Log.d(TAG, "controller is invisible ");
                    showController();
                    mHandler.removeCallbacks(r);
                    mHandler.postDelayed(r, POST_DELAY_HIDDEN_CONTROLLER_TIME);
                } else {
                    Log.d(TAG, "controller is visible ");

                }
            }
        });


        mPlayPauseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mPlayPauseImage.setImageResource(R.mipmap.exo_controls_play);
                    mHandler.removeCallbacks(r);
                    showController();
                } else if ((mMediaPlayer != null && !mMediaPlayer.isPlaying())) {
                    mMediaPlayer.start();
                    mPlayPauseImage.setImageResource(R.mipmap.exo_controls_pause);
                    //todo 以下をmethod化する
                    mHandler.removeCallbacks(r);
                    mHandler.postDelayed(r, POST_DELAY_HIDDEN_CONTROLLER_TIME);
                }
            }
        });

        mButton1 = (Button) findViewById(R.id.seek_backward);

        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    Log.d(TAG, "seek to backward to " + (mMediaPlayer.getCurrentPosition() - 10000));
                    mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() - 10000);
                }
            }
        });

        mButton2 = (Button) findViewById(R.id.seek_forward);

        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    Log.d(TAG, "seek to forward to " + (mMediaPlayer.getCurrentPosition() + 10000));
                    mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() + 10000);
                }
            }
        });

        mSeekbar = (SeekBar) findViewById(R.id.seekbar);
        mSeekbar.setMax(10000);
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean isPlayingWhenStartToSeek = false;
            private boolean isTouchSeekbar = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int progress = seekBar.getProgress();
                Log.d(TAG, "onProgressChanged" + "progress : " + progress);
                if (!isTouchSeekbar) {
                    return;
                }
                if (mMediaPlayer != null) {
                    int seekPosition = (int) (mMediaPlayer.getDuration() * ((double) progress / (double) 10000));
                    mMediaPlayer.seekTo(seekPosition);
                    Log.d(TAG, "seekPosition : " + seekPosition);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStartTrackingTouch");
                isTouchSeekbar = true;
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    isPlayingWhenStartToSeek = true;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch");
                isTouchSeekbar = false;
                if (mMediaPlayer != null && isPlayingWhenStartToSeek) {
                    mMediaPlayer.start();
                }
            }
        });
//        gesture = new GestureDetector(this,gestureListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(null);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
        if (mHandler != null) {
            mHandler.postDelayed(monitorTask, POST_DELAY_MONITOR_TIME);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = null;
        if (mHandler != null) {
            mHandler.removeCallbacks(null);
        }
        mHandler = null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated");
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(this, Uri.parse(uriString));
            mMediaPlayer.setDisplay(surfaceHolder);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            Log.d(TAG, "Stream read error : " + e);
            mMediaPlayer.release();
            return;
        }
        setVideoLayoutParams();
        mMediaPlayer.start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            for (MediaPlayer.TrackInfo trackInfo : mMediaPlayer.getTrackInfo()) {
                Log.d(TAG, "mediaPlayer.getTrackInfo" + trackInfo);
            }
        }
        mHandler.postDelayed(monitorTask, POST_DELAY_MONITOR_TIME);
        mHandler.removeCallbacks(r);
        mHandler.postDelayed(r, POST_DELAY_HIDDEN_CONTROLLER_TIME);
    }

    private void setVideoLayoutParams() {
        if (mMediaPlayer == null || mSurfaceView == null) {
            return;
        }
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_main);
        int layoutWidth = layout.getWidth();
        int videoWidth = mMediaPlayer.getVideoWidth();
        int videoHeight = mMediaPlayer.getVideoHeight();
        int surfaceHeight = (int) (layoutWidth * (1.0 * videoHeight / videoWidth));
        Log.d(TAG, "display.getHeight" + surfaceHeight);
        mPlayerLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, surfaceHeight));
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d(TAG, "surfaceChanged");

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceDestroyed");
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = null;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.d(TAG, "onError i " + i + " i1 " + i1);
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        Log.d(TAG, "onInfo i " + i + " i1 " + i1);
        return false;
    }


    private void monitorPlayer() {
        if (mMediaPlayer == null) {
            return;
        }
        long currentPosition = mMediaPlayer.getCurrentPosition();
        long duration = mMediaPlayer.getDuration();
        Log.d(TAG, "currentPosition : " + currentPosition + " duration : " + duration);
        if (currentPosition >= duration) {
            Log.d(TAG, "playback complete ");
//            return;
        }
        syncSeekbar(currentPosition, duration);
    }

    private void syncSeekbar(long currentPosition, long duration) {
        if (mSeekbar == null) {
            return;
        }
        if (currentPosition < 0 || duration < 0) {
            return;
        }
        int progress = 0;
        if (currentPosition != 0 && duration != 0) {
            progress = (int) (10000 * ((double) currentPosition / (double) duration));
        }
        mSeekbar.setProgress(progress);
    }

    private class MonitorTask implements Runnable {
        @Override
        public void run() {
            monitorPlayer();
            mHandler.postDelayed(monitorTask, POST_DELAY_MONITOR_TIME);
        }
    }

    final Runnable r = new Runnable() {
        @Override
        public void run() {
            hideController();
        }
    };

    private void showController() {
        mPlayPauseImage.setVisibility(View.VISIBLE);
        visibleController = true;
    }

    private void hideController() {
        mPlayPauseImage.setVisibility(View.GONE);
        visibleController = false;
    }

//    private static void handleMessage(Message msg) {
//        switch (msg.what) {
//            case SHOW_CONTROLLER:
//                break;
//            case HIDE_CONTROLLER:
//                break;
//            case MONITOR_PLAYER:
//            default:
//                break;
//        }
//    }
//    }
//
//    private final GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
//
//        @Override
//        public boolean onSingleTapUp(MotionEvent e) {
//            Log.d(TAG, "onSingleTapUp");
//            return super.onSingleTapUp(e);
//        }
//
//        @Override
//        public void onLongPress(MotionEvent e) {
//            Log.d(TAG, "onLongPress");
//            super.onLongPress(e);
//        }
//
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            Log.d(TAG, "onFling");
//            return super.onFling(e1, e2, velocityX, velocityY);
//        }
//
//        @Override
//        public boolean onDoubleTap(MotionEvent e) {
//            Log.d(TAG, "onDoubleTap");
//
//            return super.onDoubleTap(e);
//        }
//
//        @Override
//        public boolean onDoubleTapEvent(MotionEvent e) {
//            Log.d(TAG, "onDoubleTapEvent");
//            return super.onDoubleTapEvent(e);
//        }
//    };
//
//    private class MySurfaceView extends SurfaceView {
//
//        public MySurfaceView(Context context) {
//            super(context);
//        }
//
//        public MySurfaceView(Context context, AttributeSet attrs) {
//            super(context, attrs);
//        }
//
//        public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
//            super(context, attrs, defStyleAttr);
//        }
//
//        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//        public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//            super(context, attrs, defStyleAttr, defStyleRes);
//        }
//
//        @Override
//        public boolean onTouchEvent(MotionEvent event) {
//            if (gesture == null) {
//                return super.onTouchEvent(event);
//            }
//            return gesture.onTouchEvent(event);
//        }
}

