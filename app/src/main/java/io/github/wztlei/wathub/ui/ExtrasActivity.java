package io.github.wztlei.wathub.ui;

import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.utils.Px;

public class ExtrasActivity
    extends BaseActivity {

  private static final int STREAM = AudioManager.STREAM_MUSIC;

  private final Handler mHandler = new Handler(Looper.getMainLooper());
  private final Runnable mRumbleRunnable = new Runnable() {
    @Override
    public void run() {
      final float t = Px.fromDp(mImageView.isPressed() ? 20f : 5f);
      mImageView.setTranslationX((float) (Math.random() * t));
      mImageView.setTranslationY((float) (Math.random() * t));

      final int sign = (int) Math.signum(Math.random() - 0.5f);
      final float scaleFactor = mImageView.isPressed() ? .1f : 0.01f;
      final float scale = 1 + (sign * ((float) Math.random() * scaleFactor));
      mImageView.setScaleX(scale);
      mImageView.setScaleY(scale);

      mHandler.post(mRumbleRunnable);
    }
  };

  private ImageView mImageView;
  private MediaPlayer mMediaPlayer;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setVolumeControlStream(STREAM);
    getWindow().setStatusBarColor(Color.TRANSPARENT);
    getWindow().getDecorView().setSystemUiVisibility(
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

    // You can't see me...
    final View view = new View(this);
    view.setBackgroundColor(Color.BLACK);
    view.setLayoutParams(new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));
    setContentView(view);
  }

  private void setView() {
    // .. my time is now!
    setContentView(R.layout.activity_extras);

    mImageView = (ImageView) findViewById(R.id.extras_image_view);
    mImageView.setOnTouchListener((v, event) -> {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          mMediaPlayer.setVolume(1f, 1f);
          break;

        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
          mMediaPlayer.setVolume(0.5f, 0.5f);
          break;
      }
      return false;
    });
    mHandler.postDelayed(mRumbleRunnable, 1000L);
  }

  private void playSoundtrack() {
    if (!isDestroyed()) {
      mMediaPlayer = MediaPlayer.create(this, R.raw.extras_soundtrack);
      mMediaPlayer.setVolume(0.25f, 0.25f);
      mMediaPlayer.setAudioStreamType(STREAM);
      mMediaPlayer.setLooping(true);
      mMediaPlayer.start();

      mHandler.postDelayed(() -> {
        mMediaPlayer.setVolume(0.5f, 0.5f);
        setView();
      }, 1500L);
    }
  }

  @Override
  public void onBackPressed() {
    if (mImageView != null) {
      super.onBackPressed();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    mHandler.postDelayed(() -> playSoundtrack(), 1000L);
  }

  @Override
  protected void onPause() {
    super.onPause();

    if (mMediaPlayer != null) {
      mMediaPlayer.stop();
      mMediaPlayer.reset();
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
  }

  @Override
  protected void onDestroy() {
    mHandler.removeCallbacks(mRumbleRunnable);
    super.onDestroy();
  }
}
