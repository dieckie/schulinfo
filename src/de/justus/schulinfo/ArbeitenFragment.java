package de.justus.schulinfo;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class ArbeitenFragment extends Fragment {

	public ArbeitenFragment() {

	}

	private class DrawView extends View {
		int x = 0;
		int y = 0;
		boolean touched = false;
		Paint paint = new Paint();

		@Override
		public boolean onTouchEvent(MotionEvent ev) {
			x = (int) ev.getX();
			y = (int) ev.getY();
			touched = true;
			invalidate();
			if (ev.getAction() == MotionEvent.ACTION_UP) {
				performClick();
			}
			return true;
		}

		@Override
		public boolean performClick() {
			return super.performClick();
		}

		public DrawView(Context context) {
			super(context);
			paint.setColor(Color.BLACK);
			paint.setTextSize(20);
		}

		public DrawView(Context context, AttributeSet attrs) {
			super(context, attrs);
			paint.setColor(Color.BLACK);
			paint.setTextSize(20);
		}

		public DrawView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			paint.setColor(Color.BLACK);
			paint.setTextSize(20);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			paint.setColor(Color.BLACK);
			canvas.drawText(x + " * 0,53 = " + (int) (x * 0.53125) + ", " + y + " * 0,31 = " + (int) (y * 0.31875), 20, 100, paint);
			paint.setARGB(255, (int) (x * 0.53125), 0, (int) (y * 0.31875));
			if (touched) {
				canvas.drawCircle(x, y, 90, paint);
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);

			int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
			int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
			this.setMeasuredDimension(parentWidth, parentHeight);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		return new DrawView(this.getActivity());
	}
}