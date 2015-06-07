package de.justus.schulinfo.vertretungen;

import java.util.Calendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class DateChanger extends View {

	int screen_w;
	Typeface font = Typeface.create("Roboto", Typeface.NORMAL);
	Paint paint = new Paint();

	public DateChanger(Context context) {
		super(context);
		doAfterConstruct();
	}

	public DateChanger(Context context, AttributeSet attrs) {
		super(context, attrs);
		doAfterConstruct();
	}

	public DateChanger(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		doAfterConstruct();
	}

	/**
	 * Geh�rt mit zum Konstructor dazu.
	 */
	public void doAfterConstruct() {
		screen_w = getContext().getResources().getDisplayMetrics().widthPixels;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		paint.setStrokeWidth(1);
		canvas.drawLine(0, 89, screen_w, 89, paint);
		canvas.drawLine(screen_w / 5, 0, screen_w / 5, 90, paint);
		canvas.drawLine(screen_w - (screen_w / 5), 0, screen_w - (screen_w / 5), 90, paint);
		paint.setTypeface(font);
		paint.setTextSize(30);
		canvas.drawText("<<", screen_w / 10 - 15, 60, paint);
		canvas.drawText(">>", screen_w - (screen_w / 10 + 15), 60, paint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		screen_w = getContext().getResources().getDisplayMetrics().widthPixels;
		setMeasuredDimension(screen_w, 90);
	}

	int xBegin = 0;
	int yBegin = 0;

	/**
	 * Untersucht, ob ein Klick statt gefunden hat. Das bislang einzige Kriterium ist die Entfernung zwischen {@link MotionEvent#ACTION_DOWN} und {@link MotionEvent#ACTION_UP}. Diese muss kleiner als 20 Pixel sein, dann wird
	 * {@link #onClick(int, int)} aufgerufen.
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xBegin = x;
			yBegin = y;
			return true;
		case MotionEvent.ACTION_UP:
			if (Math.abs(xBegin - x) < 20 && Math.abs(yBegin - y) < 20) {
				onClick(x, y);
			} else {
				Log.d("Click", Math.abs(xBegin - x) + " / " + Math.abs(yBegin - y));
			}
		default:
			return false;
		}
	}

	/**
	 * 
	 * Wird bei einem von {@link #onTouchEvent(MotionEvent)} erkannten Klick aufgerufen.
	 * 
	 * @param x
	 *            Die X-Koordinate des Klicks
	 * @param y
	 *            Die Y-Koordinate des Klicks
	 */
	public void onClick(int x, int y) {
		Log.d("Click", "x: " + x + ", y: " + y);
		if (x < screen_w / 5) {
		} else if (x > screen_w - screen_w / 5) {
		} else {
		}
	}
}