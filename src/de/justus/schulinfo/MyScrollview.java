package de.justus.schulinfo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class MyScrollview extends ScrollView {

	private boolean enableScrolling = true;

	public boolean isEnableScrolling() {
		return enableScrolling;
	}

	public void setEnableScrolling(boolean enableScrolling) {
		this.enableScrolling = enableScrolling;
	}

	public MyScrollview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MyScrollview(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyScrollview(Context context) {
		super(context);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		if (isEnableScrolling()) {
			return super.onInterceptTouchEvent(ev);
		} else {
			return false;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isEnableScrolling()) {
			return super.onInterceptTouchEvent(ev);
		} else {
			return false;
		}
	}
}