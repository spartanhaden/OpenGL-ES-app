package com.HadenW.mariodemo.app;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
	private MyGLSurfaceView glSurfaceView;
	MediaPlayer mediaPlayer;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		glSurfaceView = (MyGLSurfaceView) findViewById(R.id.MyGLSurfaceView);
		mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.theme_song);
	}

	@Override
	protected void onPause() {
		super.onPause();
		glSurfaceView.onPause();
		mediaPlayer.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		glSurfaceView.onResume();
		mediaPlayer.start();
	}

	public void left(final View view) {
		glSurfaceView.mRenderer.changeX(1f);
	}

	public void right(final View view) {
		glSurfaceView.mRenderer.changeX(-1f);
	}

	public void up(final View view) {
		glSurfaceView.mRenderer.changeY(-1f);
	}

	public void down(final View view) {
		glSurfaceView.mRenderer.changeY(1f);
	}
}
