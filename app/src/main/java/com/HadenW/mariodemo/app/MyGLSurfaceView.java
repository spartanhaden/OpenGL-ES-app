package com.HadenW.mariodemo.app;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Haden on 4/17/14.
 */
class MyGLSurfaceView extends GLSurfaceView {
	final MyGLRenderer mRenderer;
	private float mPreviousX = 0;
	private float mPreviousY = 0;

	public MyGLSurfaceView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		setEGLContextClientVersion(2);
		mRenderer = new MyGLRenderer(context);
		setRenderer(mRenderer);
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		final float x = e.getX() / 20f;
		final float y = e.getY() / 20f;

		final float rawX = e.getX();
		final float rawY = e.getY();

		float DPadX = 0f;
		float DPadY = 0f;

		float PedoX = 0f;
		float PedoY = 0f;

		final float boxSize = 500f;

		final float duller = 2f;

		if (e.getAction() == MotionEvent.ACTION_MOVE || e.getAction() == MotionEvent.ACTION_DOWN) {
			if (rawX < boxSize && rawY < boxSize) {
				DPadX = (rawX - boxSize / 2f) / boxSize;
				DPadY = (rawY - boxSize / 2f) / boxSize;
			} else if (rawX > super.getWidth() - boxSize && rawY < boxSize) {
				PedoX = (rawX - super.getWidth() + boxSize / 2f) / boxSize;
				PedoY = (rawY - boxSize / 2f) / boxSize;
			} else if (e.getAction() == MotionEvent.ACTION_MOVE) {
				mRenderer.changeAngle(mPreviousX - x, mPreviousY - y);
			}
		} else if (e.getAction() == MotionEvent.ACTION_UP) {
			DPadX = 0;
			DPadY = 0;
		}
		mRenderer.changeX(-DPadX / duller);
		mRenderer.changeY(DPadY / duller);
		mRenderer.moveP(PedoX / duller, -PedoY / duller);
		mPreviousX = x;
		mPreviousY = y;
		return true;
	}
}
