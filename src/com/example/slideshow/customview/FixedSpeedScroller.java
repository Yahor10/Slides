package com.example.slideshow.customview;

import com.example.slideshow.app.Constants;
import com.example.slideshow.utils.PreferenceUtils;

import android.content.Context;
import android.util.Log;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class FixedSpeedScroller extends Scroller {

	private int mDuration = 1000;

	public FixedSpeedScroller(Context context) {
		super(context);
	}

	public FixedSpeedScroller(Context context, Interpolator interpolator) {
		super(context, interpolator);
		mDuration = PreferenceUtils.getInt(context,
				Constants.PREFERENCE_KEY_SPEED) * 1000;
		Log.v(Constants.TAG, "DURATION " + mDuration);
	}

	public FixedSpeedScroller(Context context, Interpolator interpolator,
			boolean flywheel) {
		super(context, interpolator, flywheel);
	}

	@Override
	public void startScroll(int startX, int startY, int dx, int dy, int duration) {
		// Ignore received duration, use fixed one instead
		// TODO get from preferences
		super.startScroll(startX, startY, dx, dy, mDuration);
	}

	@Override
	public void startScroll(int startX, int startY, int dx, int dy) {
		// Ignore received duration, use fixed one instead
		// TODO get from preferences mDuration
		super.startScroll(startX, startY, dx, dy, mDuration);
	}
}