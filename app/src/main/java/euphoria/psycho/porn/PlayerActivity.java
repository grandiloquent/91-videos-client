package euphoria.psycho.porn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.TimedMetaData;
import android.opengl.GLES20;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import euphoria.psycho.porn.Shared.Listener;
import euphoria.psycho.porn.TimeBar.OnScrubListener;

import static euphoria.psycho.porn.PlayerUtils.hideSystemUI;
import static euphoria.psycho.porn.Shared.getActionBarHeight;
import static euphoria.psycho.porn.Shared.getNavigationBarHeight;
import static euphoria.psycho.porn.Shared.getStringForTime;

public class PlayerActivity extends Activity implements OnTouchListener {

    public static final int DEFAULT_HIDE_TIME_DELAY = 5000;
    public static final String KEY_VIDEO_FILE = "VideoFile";
    private static final int TOUCH_IGNORE = -1;
    private static final int TOUCH_NONE = 0;
    private final Handler mHandler = new Handler();
    private final StringBuilder mStringBuilder = new StringBuilder();
    private final Formatter mFormatter = new Formatter(mStringBuilder);
    private TextureView mTextureView;
    private MediaPlayer mMediaPlayer;
    private Surface mSurface;
    private FrameLayout mRoot;
    private boolean mLayout = false;
    private FrameLayout mBottomBar;
    private TextView mDuration;
    private SimpleTimeBar mTimeBar;
    private TextView mPosition;
    private LinearLayout mCenterControls;
    private final Runnable mHideAction = this::hiddenControls;
    private List<String> mPlayList;
    private int mPlayIndex;
    private ImageButton mPlayPause;
    private int mScaledTouchSlop;
    private int mDelta = 0;
    private int mCurrentPosition;
    private float mLastFocusX;
    private int mLastSystemUiVis;
    private PlayerSizeInformation mPlayerSizeInformation;
    private boolean mShuffle;


    public static void launchActivity(Context context, File videoFile, int sort) {
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra(KEY_VIDEO_FILE, videoFile.getAbsolutePath());
        intent.putExtra("sort", sort);
        context.startActivity(intent);
    }

    static int calculateScreenOrientation(Activity activity) {
        int displayRotation = getDisplayRotation(activity);
        boolean standard = displayRotation < 180;
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (standard)
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            else return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        } else {
            if (displayRotation == 90 || displayRotation == 270) {
                standard = !standard;
            }
            return standard ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        }
    }

    static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    static File[] listVideoFiles(String dir) {
        File directory = new File(dir);
        Pattern pattern = Pattern.compile("\\.(?:mp4|vm|crdownload)$");
        File[] files = directory.listFiles(file ->
                file.isFile() && pattern.matcher(file.getName()).find());
        if (files == null || files.length == 0) return null;
        Arrays.sort(files, (o1, o2) -> {
            final long result = o2.lastModified() - o1.lastModified();
            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            } else {
                return 0;
            }
        });
        return files;
    }

    private void bindingDeleteVideoEvent() {
        findViewById(R.id.action_file_download)
                .setOnClickListener(v -> new AlertDialog.Builder(PlayerActivity.this)
                        .setTitle("询问")
                        .setMessage("确定要删除 \"" + Shared.substringAfterLast(mPlayList.get(mPlayIndex), "/") + "\" 视频吗？")
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            deleteVideo();
                            dialog.dismiss();
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show());
    }

    private void bindingFullScreenEvent() {
        findViewById(R.id.action_fullscreen)
                .setOnClickListener(v -> fullScreen());

    }

    private void clearSurface() {
        if (mSurface == null) {
            return;
        }
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        egl.eglInitialize(display, null);
        int[] attribList = {
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_RENDERABLE_TYPE, EGL10.EGL_WINDOW_BIT,
                EGL10.EGL_NONE, 0,      // placeholder for recordable [@-3]
                EGL10.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        egl.eglChooseConfig(display, attribList, configs, configs.length, numConfigs);
        EGLConfig config = configs[0];
        EGLContext context = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, new int[]{
                12440, 2, EGL10.EGL_NONE
        });
        EGLSurface eglSurface = egl.eglCreateWindowSurface(display, config, mSurface, new int[]{
                EGL10.EGL_NONE
        });
        egl.eglMakeCurrent(display, eglSurface, eglSurface, context);
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        egl.eglSwapBuffers(display, eglSurface);
        egl.eglDestroySurface(display, eglSurface);
        egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroyContext(display, context);
        egl.eglTerminate(display);
    }

    private void deleteVideo() {
        mMediaPlayer.reset();
        File videoFile = new File(mPlayList.get(mPlayIndex));
        videoFile.delete();
        loadPlaylist(videoFile.getParentFile().getAbsolutePath());
        if (mPlayList.size() > 0) {
            if (mPlayList.size() - 1 < mPlayIndex) mPlayIndex = 0;
            try {
                play();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void fullScreen() {
        int orientation = calculateScreenOrientation(this);
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            int videoWidth = mMediaPlayer.getVideoWidth();
            int videoHeight = mMediaPlayer.getVideoHeight();
            double ratio = mRoot.getMeasuredWidth() / (videoHeight * 1.0);
            int width = (int) (ratio * videoWidth);
            int left = (mRoot.getMeasuredHeight() - width) >> 1;
            FrameLayout.LayoutParams layoutParams = new LayoutParams(width, getResources().getDisplayMetrics().widthPixels);
            layoutParams.leftMargin = left;
            mTextureView.setLayoutParams(layoutParams);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            int videoWidth = mMediaPlayer.getVideoWidth();
            int videoHeight = mMediaPlayer.getVideoHeight();
            double ratio = mRoot.getMeasuredHeight() / (videoWidth * 1.0);
            int height = (int) (ratio * videoHeight);
            int top = (mRoot.getMeasuredWidth() - height) >> 1;
            FrameLayout.LayoutParams layoutParams = new LayoutParams(mRoot.getMeasuredHeight(), height);
            layoutParams.topMargin = top;
            mTextureView.setLayoutParams(layoutParams);
        }
    }

    private void hiddenControls() {
        mTimeBar.setVisibility(View.GONE);
        mBottomBar.setVisibility(View.GONE);
        mCenterControls.setVisibility(View.GONE);
        hideSystemUI(this);
        zoomIn();
    }


    private void initializePlayer() {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setOnBufferingUpdateListener(this::onBufferingUpdate);
            mMediaPlayer.setOnCompletionListener(this::onCompletion);
            mMediaPlayer.setOnErrorListener(this::onError);
            mMediaPlayer.setOnInfoListener(this::onInfo);
            mMediaPlayer.setOnPreparedListener(this::onPrepared);
            mMediaPlayer.setOnSeekCompleteListener(this::onSeekComplete);
            mMediaPlayer.setOnTimedMetaDataAvailableListener(this::onTimedMetaDataAvailable);
            mMediaPlayer.setOnVideoSizeChangedListener(this::onVideoSizeChanged);
            mMediaPlayer.setSurface(mSurface);
            play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPlaylist(String folder) {
        File fileDirectory = new File(folder);
        if (!fileDirectory.isDirectory()) return;
        File[] files = listVideoFiles(folder);
        if (files == null) {
            return;
        }
        int sort = getIntent().getIntExtra("sort", 2);
        int direction = (sort & 1) == 0 ? -1 : 1;
        Arrays.sort(files, (o1, o2) -> {
            if ((sort & 2) == 2) {
                final long result = o2.lastModified() - o1.lastModified();
                if (result < 0) {
                    return -1 * direction;
                }
                if (result > 0) {
                    return 1 * direction;
                }
            }
            if ((sort & 4) == 4) {
                final long result = o2.length() - o1.length();
                if (result < 0) {
                    return -1 * direction;
                }
                if (result > 0) {
                    return 1 * direction;
                }
            }
            return 0;
        });
        mPlayList = new ArrayList<>();
        for (File file : files) {
            mPlayList.add(file.getAbsolutePath());
        }
        if (mShuffle) {
            Collections.shuffle(mPlayList);
        }
    }

    private void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        mTimeBar.setBufferedPosition(i);
    }

    private void onCompletion(MediaPlayer mediaPlayer) {
        mMediaPlayer.reset();
        try {
            play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return true;
    }

    private boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return true;
    }

    private void onNext(View view) {
        if (mPlayList.size() < 2) return;
        mLayout = false;
        if (mPlayIndex + 1 < mPlayList.size()) {
            mPlayIndex++;
        } else {
            mPlayIndex = 0;
        }
        mMediaPlayer.reset();
        try {
            play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onPlayPause(View view) {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mPlayPause.setBackgroundDrawable(getResources().getDrawable(R.drawable.exo_ic_play_circle_filled));
        } else {
            mMediaPlayer.start();
            mPlayPause.setBackgroundDrawable(getResources().getDrawable(R.drawable.exo_ic_pause_circle_filled));
        }
    }

    private void onPrepared(MediaPlayer mediaPlayer) {
        mDuration.setText(getStringForTime(mStringBuilder, mFormatter, mediaPlayer.getDuration()));
        mTimeBar.setDuration(mediaPlayer.getDuration());
        mMediaPlayer.start();
        mPlayPause.setBackgroundDrawable(getResources().getDrawable(R.drawable.exo_ic_pause_circle_filled));
        updateProgress();
        hiddenControls();
    }

    private void onPrev(View view) {
        if (mPlayList.size() < 2) return;
        mLayout = false;
        if (mPlayIndex - 1 > -1) {
            mPlayIndex--;
        } else {
            mPlayIndex = 0;
        }
        mMediaPlayer.reset();
        try {
            play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onSeekComplete(MediaPlayer mediaPlayer) {
    }

    private void onTimedMetaDataAvailable(MediaPlayer mediaPlayer, TimedMetaData timedMetaData) {
    }

    private void onVideoSizeChanged(MediaPlayer mediaPlayer, int videoWidth, int videoHeight) {
        zoomIn();
    }

    private void play() throws IOException {
        getActionBar().setTitle(Shared.substringAfterLast(mPlayList.get(mPlayIndex), "/"));
        mMediaPlayer.setDataSource(mPlayList.get(mPlayIndex));
        mMediaPlayer.prepareAsync();
    }

    private void scheduleHideControls() {
        mHandler.removeCallbacks(mHideAction);
        mHandler.postDelayed(mHideAction, DEFAULT_HIDE_TIME_DELAY);
    }

    private void setOnSystemUiVisibilityChangeListener() {
        // When the user touches the screen or uses some hard key, the framework
        // will change system ui visibility from invisible to visible. We show
        // the media control and enable system UI (e.g. ActionBar) to be visible at this point
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {

                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        int diff = mLastSystemUiVis ^ visibility;
                        mLastSystemUiVis = visibility;
                        if ((diff & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0
                                && (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                            showControls();
                            getActionBar().show();
                            scheduleHideControls();
                            zoomOut();
                        }
                    }
                });
    }

    private void showControls() {
        mTimeBar.setVisibility(View.VISIBLE);
        mBottomBar.setVisibility(View.VISIBLE);
        mCenterControls.setVisibility(View.VISIBLE);
        updateProgress();
    }

    private void updateProgress() {
        if (mMediaPlayer == null || mBottomBar.getVisibility() != View.VISIBLE) {
            return;
        }
        mTimeBar.setPosition(mMediaPlayer.getCurrentPosition());
        mPosition.setText(getStringForTime(mStringBuilder, mFormatter, mMediaPlayer.getCurrentPosition()));
        mHandler.postDelayed(this::updateProgress, 1000);
    }

    private void zoomIn() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mPlayerSizeInformation == null) {
            mPlayerSizeInformation = new PlayerSizeInformation(this, mRoot, mBottomBar, mTimeBar);
        }
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == 1) {
            int videoHeight = mMediaPlayer.getVideoHeight();
            int videoWidth = mMediaPlayer.getVideoWidth();
            double ratio = mRoot.getMeasuredWidth() / (videoWidth * 1.0);
            int height = (int) (ratio * videoHeight);
            int top = (mRoot.getMeasuredHeight() - height) >> 1;
            FrameLayout.LayoutParams layoutParams = new LayoutParams(mRoot.getMeasuredWidth(), height);
            layoutParams.topMargin = top;
            mTextureView.setLayoutParams(layoutParams);
        } else {
            int videoHeight = mMediaPlayer.getVideoHeight();
            int videoWidth = mMediaPlayer.getVideoWidth();
            float x = ((float) mPlayerSizeInformation.getAvailableHeight()) / videoWidth;
            float y = ((float) mPlayerSizeInformation.getAvailableWidth()) / videoHeight;
            x = Math.min(x, y);
            int screenWidth = (int) (videoWidth * x);
            int screenHeight = (int) (videoHeight * x);
            FrameLayout.LayoutParams layoutParams = new LayoutParams(screenWidth, screenHeight);
            layoutParams.topMargin = (mPlayerSizeInformation.getAvailableWidth() - screenHeight) >> 1;
            layoutParams.leftMargin = (mPlayerSizeInformation.getAvailableHeight() - screenWidth) >> 1;
            mTextureView.setLayoutParams(layoutParams);
        }

    }

    private void zoomOut() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mPlayerSizeInformation == null) {
            mPlayerSizeInformation = new PlayerSizeInformation(this, mRoot, mBottomBar, mTimeBar);
        }
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == 1) {
            mPlayerSizeInformation = new PlayerSizeInformation(this, mRoot, mBottomBar, mTimeBar);
            int videoHeight = mMediaPlayer.getVideoHeight();
            int videoWidth = mMediaPlayer.getVideoWidth();
            float x = ((float) mPlayerSizeInformation.getAvailableWidth()) / videoWidth;
            float y = ((float) mPlayerSizeInformation.getPortraitHeight()) / videoHeight;
            x = Math.min(x, y);
            int screenWidth = (int) (videoWidth * x);
            int screenHeight = (int) (videoHeight * x);
            FrameLayout.LayoutParams layoutParams = new LayoutParams(screenWidth, screenHeight);
            layoutParams.topMargin = (mPlayerSizeInformation.getPortraitHeight() - screenHeight) >> 1;
            layoutParams.leftMargin = (mPlayerSizeInformation.getAvailableWidth() - screenWidth) >> 1;
            mTextureView.setLayoutParams(layoutParams);
        } else {
            int videoHeight = mMediaPlayer.getVideoHeight();
            int videoWidth = mMediaPlayer.getVideoWidth();
            float x = ((float) mPlayerSizeInformation.getAvailableHeight()) / videoWidth;
            float y = ((float) mPlayerSizeInformation.getLandscapeHeight()) / videoHeight;
            x = Math.min(x, y);
            int screenWidth = (int) (videoWidth * x);
            int screenHeight = (int) (videoHeight * x);
            FrameLayout.LayoutParams layoutParams = new LayoutParams(screenWidth, screenHeight);
            layoutParams.topMargin = (mPlayerSizeInformation.getLandscapeHeight() - screenHeight) >> 1;
            layoutParams.leftMargin = (mPlayerSizeInformation.getAvailableHeight() - screenWidth - mPlayerSizeInformation.getNavigationBarLanscapeHeight()) >> 1;
            mTextureView.setLayoutParams(layoutParams);
            //Log.e("B5aOx2", String.format("zoomOut, %s", mPlayerSizeInformation.toString()));
        }


    }

    public static final String KEY_SHUFFLE = "shuffle";

    private float mSpeed = 1f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);
        bindingDeleteVideoEvent();
        bindingFullScreenEvent();
        mRoot = findViewById(R.id.root);
//        mRoot.setOnClickListener(v -> {
//            showSystemUi(true);
//            showControls();
//            scheduleHideControls();
//
//        });
        setOnSystemUiVisibilityChangeListener();
        hideSystemUI(this);
//        View decorView = getWindow().getDecorView();
//        decorView.setOnSystemUiVisibilityChangeListener
//                (visibility -> {
//                    // Note that system bars will only be "visible" if none of the
//                    // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
//                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
//                        // TODO: The system bars are visible. Make any desired
//                        // adjustments to your UI, such as showing the action bar or
//                        // other navigational controls.
//                        mHandler.postDelayed(this::hideSystemUI, DEFAULT_HIDE_TIME_DELAY);
//                    } else {
//                        // TODO: The system bars are NOT visible. Make any desired
//                        // adjustments to your UI, such as hiding the action bar or
//                        // other navigational controls.
//                    }
//                });
        mCenterControls = findViewById(R.id.exo_center_controls);
        mTextureView = findViewById(R.id.texture_view);
        mPosition = findViewById(R.id.position);
        mBottomBar = findViewById(R.id.exo_bottom_bar);
        mDuration = findViewById(R.id.duration);
        mTextureView.setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mSurface = new Surface(surface);
                initializePlayer();
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
        mTimeBar = findViewById(R.id.timebar);
        mTimeBar.addListener(new OnScrubListener() {
            @Override
            public void onScrubMove(TimeBar timeBar, long position) {
                mPosition.setText(getStringForTime(mStringBuilder, mFormatter, position));
            }

            @Override
            public void onScrubStart(TimeBar timeBar, long position) {
                mHandler.removeCallbacks(mHideAction);
                mPosition.setText(getStringForTime(mStringBuilder, mFormatter, position));
            }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                mMediaPlayer.seekTo((int) position);
                updateProgress();
                scheduleHideControls();
            }

        });
        Button rewWithAmount = findViewById(R.id.exo_rew_with_amount);
        rewWithAmount.setText("2");
        rewWithAmount.setOnClickListener(v -> {
//            int dif = mMediaPlayer.getCurrentPosition() - 10000;
//            if (dif < 0) {
//                dif = 0;
//            }
//            if (VERSION.SDK_INT >= VERSION_CODES.O) {
//                mMediaPlayer.seekTo(dif);
//            } else {
//                mMediaPlayer.seekTo(dif);
//            }
            PlaybackParams playbackParams = new PlaybackParams();
            mSpeed /= 2;
            playbackParams.setSpeed(mSpeed);
            mMediaPlayer.setPlaybackParams(playbackParams);
            scheduleHideControls();
            updateProgress();
        });
        Button ffwdWithAmount = findViewById(R.id.exo_ffwd_with_amount);
        ffwdWithAmount.setText("2");
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            Typeface typeface = null;
            typeface = getResources().getFont(R.font.roboto_medium_numbers);
            rewWithAmount.setTypeface(typeface);
            ffwdWithAmount.setTypeface(typeface);
        }
        ffwdWithAmount.setOnClickListener(v -> {
//            int dif = mMediaPlayer.getCurrentPosition() + 10000;
//            if (dif > mMediaPlayer.getDuration()) {
//                dif = mMediaPlayer.getDuration();
//            }
//            if (VERSION.SDK_INT >= VERSION_CODES.O) {
//                mMediaPlayer.seekTo(dif);
//
//            } else {
//                mMediaPlayer.seekTo(dif);
//            }
            PlaybackParams playbackParams = new PlaybackParams();
            mSpeed *= 2;
            if (mSpeed >= 6) {
                mSpeed = 6;
            }
            Log.e("B5aOx2", String.format("onCreate, %s", mSpeed));
            playbackParams.setSpeed(mSpeed);
            mMediaPlayer.setPlaybackParams(playbackParams);
            scheduleHideControls();
            updateProgress();
        });
        mPlayPause = findViewById(R.id.play_pause);
        mPlayPause.setOnClickListener(this::onPlayPause);
        mScaledTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        //mTextureView.setOnTouchListener(this);
        ImageButton prev = findViewById(R.id.prev);
        ImageButton next = findViewById(R.id.next);
        mShuffle = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(KEY_SHUFFLE, false);
        findViewById(R.id.action_shuffle).setOnClickListener(v -> {
            if (!mShuffle) {
                Collections.shuffle(mPlayList);
                mPlayIndex = mPlayList.indexOf(mPlayList.get(mPlayIndex));
            }
            mShuffle = !mShuffle;
            PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean(KEY_SHUFFLE, mShuffle)
                    .apply();

        });
        String videoFile = getIntent().getStringExtra(KEY_VIDEO_FILE);
        findViewById(R.id.action_speed).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Shared.openTextContentDialog(PlayerActivity.this, "跳转", value -> {
                    Pattern pattern = Pattern.compile("\\b(\\d+ )+\\d+\\b");
                    Matcher matcher = pattern.matcher(value);
                    if (matcher.matches()) {
                        String[] pieces = value.split(" ");
                        int total = 0;
                        for (int i = pieces.length - 1, j = 0; i > -1; i--, j++) {
                            total += Integer.parseInt(pieces[i]) * Math.pow(60, j);
                        }
                        total *= 1000;
                        if (VERSION.SDK_INT >= VERSION_CODES.O) {
                            mMediaPlayer.seekTo(total, MediaPlayer.SEEK_CLOSEST);
                        } else {
                            mMediaPlayer.seekTo(total);
                        }
                    }
                });
            }
        });
        if (videoFile != null) {
            loadPlaylist(new File(videoFile).getParentFile().getAbsolutePath());
            if (mPlayList.size() < 2) {
                prev.setAlpha(75);
                next.setAlpha(75);
                mPlayIndex = 0;
            } else {
                prev.setOnClickListener(this::onPrev);
                next.setOnClickListener(this::onNext);
                mPlayIndex = mPlayList.indexOf(videoFile);
            }
            return;
        }


    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mSurface != null)
            initializePlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacks(null);
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        clearSurface();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mLastFocusX = event.getX();
                showControls();
                mMediaPlayer.pause();
                mCurrentPosition = mMediaPlayer.getCurrentPosition();
                mDelta = 0;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final float scrollX = mLastFocusX - event.getX();
                if (Math.abs(scrollX) > 1) {
                    if (scrollX < 0) {
                        mDelta++;
                    } else {
                        mDelta--;
                    }
                }
                mPosition.setText(getStringForTime(mStringBuilder, mFormatter, mCurrentPosition + mDelta * 1000));
                mLastFocusX = event.getX();
                break;
            }
            case MotionEvent.ACTION_UP: {
                mMediaPlayer.seekTo(mCurrentPosition + mDelta * 1000);
                mMediaPlayer.start();
                scheduleHideControls();
            }
        }
        return true;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private int mScaledTouchSlop;
        private int mCurrentTime;

        public GestureListener() {
            mScaledTouchSlop = ViewConfiguration.get(PlayerActivity.this).getScaledTouchSlop();
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDown(MotionEvent event) {
            // don't return false here or else none of the other
            // gestures will work
            mCurrentTime = mMediaPlayer.getCurrentPosition();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    // && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
                    if (Math.abs(diffX) > mScaledTouchSlop) {
                        if (diffX > 0) {
                            mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() + 1);
                        } else {
                        }
                    }
                }
//                else {
//                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
//                        if (diffY > 0) {
//                        } else {
//                        }
//                    }
//                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            showControls();
            scheduleHideControls();
            return true;
        }
    }

}
