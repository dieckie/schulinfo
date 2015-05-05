package de.justus.schulinfo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

	public class DrawView extends View {
		Paint paint = new Paint();

		public DrawView(Context context) {
			super(context);
			paint.setColor(Color.BLACK);
		}

		public DrawView(Context context, AttributeSet attrs) {
			super(context, attrs);
			paint.setColor(Color.BLACK);
		}

		public DrawView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			paint.setColor(Color.BLACK);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			int w = canvas.getWidth();
			paint.setStrokeWidth(2);
			paint.setColor(Color.rgb(170, 170, 170));
			Typeface topic = Typeface.create("Consolas", Typeface.BOLD);
			Typeface font = Typeface.create("Arial", Typeface.NORMAL);
			paint.setTypeface(topic);
			paint.setTextSize(45);
			canvas.drawText("09D", 10, 43, paint);
			canvas.drawRect(12, 50, w - 50, 55, paint);
			paint.setTypeface(font);
			paint.setColor(Color.BLACK);
			paint.setTextSize(30);
			// Senkrechte Linien
			canvas.drawLine(20, 70, 20, 138, paint);
			canvas.drawLine(73, 70, 73, 138, paint);
			canvas.drawLine(160, 70, 160, 138, paint);
			canvas.drawLine(w - 130, 70, w - 130, 138, paint);
			canvas.drawLine(w - 20, 70, w - 20, 138, paint);
			// Horizontale Linien
			canvas.drawLine(20, 100, w - 20, 100, paint);
			// Kategorien
			canvas.drawText("Std", 21 + (50 - paint.measureText("Std")) / 2, 96, paint);
			canvas.drawText("Fach", 75 + (84 - paint.measureText("Fach")) / 2, 96, paint);
			canvas.drawText("Vertreter", 162 + ((w - 293) - paint.measureText("Vertreter")) / 2, 96, paint);
			canvas.drawText("Raum", w - (128 - (107 - paint.measureText("Raum")) / 2), 96, paint);
			// Inhalt
			canvas.drawText("3-4", 21 + (50 - paint.measureText("3-4")) / 2, 130, paint);
			canvas.drawText("DuG", 75 + (84 - paint.measureText("DuG")) / 2, 130, paint);
			canvas.drawText("Zarnitz", 162 + ((w - 293) - paint.measureText("Zarnitz")) / 2, 130, paint);
			canvas.drawText("Mus", w - (128 - (107 - paint.measureText("MuG")) / 2), 130, paint);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);

			int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
			int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
			this.setMeasuredDimension(parentWidth, parentHeight);
		}
	}