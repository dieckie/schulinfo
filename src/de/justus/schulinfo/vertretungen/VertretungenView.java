package de.justus.schulinfo.vertretungen;

import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.ViewFlipper;
import de.justus.schulinfo.Downloader;
import de.justus.schulinfo.MainActivity;
import de.justus.schulinfo.R;

public class VertretungenView extends View {
	Paint paint = new Paint();
	/**
	 * Alle Vertretungen von einem Tag. Wird bei jedem Aufruf von {@link #readJSON()} neu definiert.
	 */
	JSONObject dateObj = null;
	/**
	 * Alle Vertretungen von einem Tag. Wird bei jedem Aufruf von {@link #readJSON()} neu definiert. Ist {@link #dateObj} in einem anderen Format und sortiert.
	 */
	JSONArray[] classArrays = null;
	/**
	 * Die Höhe dieses Views. Wird in {@link #onMeasure(int, int)} ausgerechnet.
	 */
	int height;

	/**
	 * Die Breite des Displays;
	 */
	int screen_w;
	/**
	 * Die Höhe, die dieser View auf dem Hardware-Display in Anspruch nimmt. Wird definiert durch die Höhe des gesamten Displays minus die ActionBar und die StatusBar;
	 */
	int screen_h;
	/**
	 * Sagt aus, ob {@link #dateObj} und {@link #classArrays} die Vertretungen des ausgewählten Datums beinhalten.
	 */
	boolean hasDate = false;
	/**
	 * Ist das Rechteck des Feldes, das geklickt wurde. Ist nur für wenige Millisekunden nach dem Klick definiert, sonst ist es <code>null</code>. Wenn es nicht null ist, wird es in {@link #onDraw(Canvas)} gezeichnet.
	 */
	Rect clicked_rect;
	/**
	 * Das ausgewählte Vertretungsobjekt.<br>
	 * <p>
	 * Wenn in {@link #onClick(int, int)} ein Klick auf ein Vertretungsfeld zurück getrackt wird, wird in {@link #selectedObj} das {@link JSONObject} der Vertretung gespeichert. Wenn {@link #onKeyDown(int, KeyEvent)} ein BACK-Event festgestellt wird,
	 * wird es wieder auf <code>null</code> gesetzt. Wenn es nicht null ist, wird in {@link #onDraw(Canvas)} die Infoanzeige eines Vertretungsobjektes gezeichnet.
	 * </p>
	 */

	int selectedTop = -1;

	/**
	 * Das ausgewählte Datum.<br>
	 * <p>
	 * Wenn mit dem Navigationmenü oben am Bildschirmrand das Datum geändert wird, wird {@link #hasDate} auf <code>false</code> gesetzt.
	 * </p>
	 */
	Calendar calendar = Calendar.getInstance(Locale.GERMANY);
	SharedPreferences prefs;
	/**
	 * Der Parent-ScrollView.
	 */
	ScrollView scrollview;

	ViewFlipper viewflipper;

	HashMap<String, String> infoscreenValues = new HashMap<String, String>();

	String[] infoscreenOrder = { "Datum", "Klasse", "Stunde", "Raum", "Lehrer", "Fach" };

	private HashSet<String> empty = new HashSet<String>();

	Bitmap arrow_back;

	Typeface font = Typeface.create("Roboto", Typeface.NORMAL);
	Typeface topic = Typeface.create("Consolas", Typeface.BOLD);

	public VertretungenView(Context context) {
		super(context);
		doAfterConstruct();
	}

	public VertretungenView(Context context, AttributeSet attrs) {
		super(context, attrs);
		doAfterConstruct();
	}

	public VertretungenView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		doAfterConstruct();
	}

	/**
	 * Gehört mit zum Konstructor dazu.
	 */
	public void doAfterConstruct() {
		prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		int statusbar_height = 0;
		if (resourceId > 0) {
			statusbar_height = getResources().getDimensionPixelSize(resourceId);
		}
		screen_h = getContext().getResources().getDisplayMetrics().heightPixels - (statusbar_height + MainActivity.actionbar.getHeight());
		screen_w = getContext().getResources().getDisplayMetrics().widthPixels;
		setFocusableInTouchMode(true);
		if (!Downloader.downloaded) {
			try {
				MainActivity.getDownloader().download();
			} catch (MalformedURLException e) {
				Log.e("error", "doAfterConstruct", e);
			}
		}
		readJSON();
		final GestureDetector gd = new GestureDetector(this.getContext(), new GestureListener());
		setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return gd.onTouchEvent(event);
			}
		});
		arrow_back = BitmapFactory.decodeResource(MainActivity.context.getResources(), R.drawable.arrow_back_32);
		setupInfoscreenOrder();
	}

	public void setupInfoscreenOrder() {
		infoscreenValues.put("Datum", "tag");
		infoscreenValues.put("Klasse", "klasse");
		infoscreenValues.put("Stunde", "stunden");
		infoscreenValues.put("Raum", "raum");
		infoscreenValues.put("Lehrer", "verlehrerkuerzel");
		infoscreenValues.put("Fach", "fach");
	}

	/**
	 * Malt den View.<br>
	 * <p>
	 * Fängt mit dem Navigationsmenü an und ruft dann in einer Schleife mit allen in den Einstellungen festgelegten Klassen {@link #drawClass(Canvas, String, JSONArray, int)} auf. Außerdem malt es, wenn nötig Fehlermeldungen, das Rechteck des
	 * geklickten Feldes und das Informationsfenster zur Ausgewählten Vertretung.
	 * </p>
	 */
	@Override
	public void onDraw(Canvas canvas) {
		Log.d("Method", "onDraw");
		screen_w = canvas.getWidth();
		paint.setStrokeWidth(1);
		paint.setTypeface(font);
		paint.setTextSize(25);
		if (scrollview == null) {
			scrollview = (ScrollView) getParent();
		}
		if (viewflipper == null) {
			viewflipper = (ViewFlipper) scrollview.getParent().getParent();
		}
		paint.setColor(Color.rgb(190, 190, 190));
		String error = Errorpanel.getError();
		Log.d("errorpanel", error);
		if (!error.equals("")) {
			drawMultilineText(error, 20, 160, screen_w - 40, 40, true, canvas, paint);
		} else {
			if (clicked_rect != null) {
				canvas.drawRect(clicked_rect, paint);
			}
			paint.setColor(Color.BLACK);
			Set<String> gewaehlteKlassen = prefs.getStringSet("class_select", empty);
			Log.d("Prefs", "länge: " + gewaehlteKlassen.size());
			boolean drewsth = false;
			if (classArrays != null) {
				int y = 0;
				try {
					for (int i = 0; i < classArrays.length; i++) {
						String className = classArrays[i].getJSONObject(0).getString("klasse");
						if (gewaehlteKlassen.contains(className)) {
							y = drawClass(canvas, className, classArrays[i], y);
							drewsth = true;
						} else if (className.equals("Jgst. Q1") && gewaehlteKlassen.contains("Q1")) {
							y = drawClass(canvas, className, classArrays[i], y);
							drewsth = true;
						} else if (className.equals("Jgst. Q2") && gewaehlteKlassen.contains("Q2")) {
							y = drawClass(canvas, className, classArrays[i], y);
							drewsth = true;
						}
					}
				} catch (JSONException e) {
					Log.e("error", "onDraw", e);
				}
			}
			if (gewaehlteKlassen.size() >= 1 && drewsth == false) {
				paint.setTextSize(25);
				drawMultilineText("Keine Eintragungen für diesen Tag!", 20, 160, screen_w - 40, 40, true, canvas, paint);
			}
		}
	}

	/**
	 * Malt eine Klasse auf dem Canvas.
	 * 
	 * @param canvas
	 *            Das {@link Canvas}, auf das gemalt werden soll.
	 * @param className
	 *            Der Klassenname, der oben über den Vertretungen steht.
	 * @param array
	 *            Das Array der Vertretungen der zu malenden Klasse.
	 * @param y
	 *            Die Höhe auf dem Canvas, ab der begonnen werden soll.
	 * @return Die Höhe auf dem Canvas, bei der weitergezeichnet werden kann.
	 */
	public int drawClass(Canvas canvas, String className, JSONArray array, int y) {
		paint.setStrokeWidth(2);
		paint.setColor(Color.rgb(170, 170, 170));
		paint.setTypeface(topic);
		paint.setTextSize(45);
		canvas.drawText(className, 10, 40 + y, paint);
		canvas.drawRect(12, 50 + y, screen_w - 50, 55 + y, paint);
		int size = array.length();
		paint.setTypeface(font);
		paint.setColor(Color.BLACK);
		paint.setTextSize(30);
		// Vertikale Linien
		canvas.drawLine(20, 70 + y, 20, 100 + y + (40 * size), paint);
		canvas.drawLine(73, 70 + y, 73, 100 + y + (40 * size), paint);
		canvas.drawLine(160, 70 + y, 160, 100 + y + (40 * size), paint);
		canvas.drawLine(screen_w - 130, 70 + y, screen_w - 130, 100 + y + (40 * size), paint);
		canvas.drawLine(screen_w - 20, 70 + y, screen_w - 20, 100 + y + (40 * size), paint);
		// Horizontale Linen
		canvas.drawLine(20, y + 100, screen_w - 20, y + 100, paint);
		for (int i = 0; i < size; i++) {
			paint.setColor(Color.BLACK);
			paint.setAlpha(255);
			canvas.drawLine(20, y + 140 + (i * 40), screen_w - 20, y + 140 + (i * 40), paint);
			JSONObject vertretungObj = array.optJSONObject(i);
			try {
				String art = vertretungObj.getString("art");
				if (!art.equals("")) {
					paint.setAlpha(150);
					Path path = new Path();
					path.moveTo(screen_w - 21, y + 119 + (i * 40));
					path.lineTo(screen_w - 21, y + 139 + (i * 40));
					path.lineTo(screen_w - 41, y + 139 + (i * 40));
					path.lineTo(screen_w - 21, y + 119 + (i * 40));
					path.close();
					if (art.equals("C")) {
						paint.setColor(Color.RED);
						canvas.drawPath(path, paint);
					} else if (art.equals("R")) {
						paint.setColor(Color.BLUE);
						canvas.drawPath(path, paint);
					} else {
						Log.w("onDraw", "There is another ART: " + art);
					}
				}
				String kommentar = vertretungObj.getString("kommentar");
				int color = -1;
				if (!kommentar.equals("")) {
					if (kommentar.startsWith("#!#")) {
						color = Color.rgb(255, 127, 0);
					} else if (kommentar.startsWith("#!!#")) {
						color = Color.rgb(255, 0, 0);
					}
				}
				if (color != -1) {
					paint.setColor(color);
					paint.setAlpha(150);
					canvas.drawRect(20, y + 100 + (i * 40), screen_w - 20, y + 140 + (i * 40), paint);
				}
			} catch (JSONException e) {
				Log.e("error", "drawClass", e);
			}
			// Inhalt
			paint.setColor(Color.BLACK);
			drawTextNotNull("stunden", vertretungObj, canvas, 21, 50, y + 130 + (i * 40));
			drawTextNotNull("fach", vertretungObj, canvas, 75, 84, y + 130 + (i * 40));
			drawTextNotNull("verlehrerkuerzel", vertretungObj, canvas, 162, screen_w - 293, y + 130 + (i * 40));
			drawTextNotNull("raum", vertretungObj, canvas, screen_w - 128, 107, y + 130 + (i * 40));
		}
		// Kategorien
		paint.setAlpha(255);
		paint.setColor(Color.BLACK);
		canvas.drawText("Std", 21 + (50 - paint.measureText("Std")) / 2, 94 + y, paint);
		canvas.drawText("Fach", 75 + (84 - paint.measureText("Fach")) / 2, 94 + y, paint);
		canvas.drawText("Vertreter", 162 + ((screen_w - 293) - paint.measureText("Vertreter")) / 2, 94 + y, paint);
		canvas.drawText("Raum", screen_w - (128 - (107 - paint.measureText("Raum")) / 2), 94 + y, paint);
		return (size * 40) + y + 110;
	}

	public void drawMultilineText(String text, float x, float y, float width, float lineheight, boolean center, Canvas canvas, Paint paint) {
		String[] words = text.split(" ");
		Log.d("Method", "DrawMultilineText");
		int wordpos = 0;
		String lineText = "";
		while (wordpos < words.length) {
			while (wordpos < words.length && paint.measureText(lineText + " " + words[wordpos]) < width) {
				lineText += " " + words[wordpos];
				wordpos++;
			}
			if (center) {
				canvas.drawText(lineText, (float) (x + (width - paint.measureText(lineText)) * 0.5), y, paint);
			} else {
				canvas.drawText(lineText, x, y, paint);
			}
			lineText = "";
			y += lineheight;
		}
	}

	/**
	 * Zeichnet einen übergebenen Text dann in das übergebene Feld, wenn er nicht <code>null</code> ist.
	 * 
	 * @param get
	 *            Der Key des Strings in dem <code>vertretungObj</code>.
	 * @param vertretungObj
	 *            Das vertretungsObj, in dem der String vorhanden ist.
	 * @param canvas
	 *            Das {@link Canvas} auf das gezeichnet werden soll.
	 * @param x
	 *            Der Anfang des ganzen Feldes, in das gezeichnet werden darf.
	 * @param widthfield
	 *            Die Breite des Feldes, in das gezeichnet werden darf.
	 * @param y
	 *            Die Höhe, in der der Text gezeichnet werden soll.
	 */
	public void drawTextNotNull(String get, JSONObject vertretungObj, Canvas canvas, int x, int widthfield, int y) {
		try {
			String text = vertretungObj.getString(get);
			if (text != null) {
				if (get.equals("stunden")) {
					text = text.replace("|", "-");
				}
				canvas.drawText(text, x + (widthfield - paint.measureText(text)) / 2, y, paint);
			}
		} catch (JSONException e) {
			Log.e("error", "drawTextNotNull", e);
		}
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d("Method", "onMeasure");
		Log.d("measure", "dateObj != null: " + String.valueOf(dateObj != null));
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		int statusbar_height = 0;
		if (resourceId > 0) {
			statusbar_height = getResources().getDimensionPixelSize(resourceId);
		}
		screen_h = getContext().getResources().getDisplayMetrics().heightPixels - (statusbar_height + MainActivity.actionbar.getHeight());
		screen_w = getContext().getResources().getDisplayMetrics().widthPixels;
		if (dateObj != null) {
			try {
				Set<String> gewaehlteKlassen = prefs.getStringSet("class_select", empty);
				height = 10;
				for (int i = 0; i < classArrays.length; i++) {
					String className = classArrays[i].getJSONObject(0).getString("klasse");
					if (gewaehlteKlassen.contains(className)) {
						height += 110 + classArrays[i].length() * 40;
					} else if (className.equals("Jgst. Q1") && gewaehlteKlassen.contains("Q1")) {
						height += 110 + classArrays[i].length() * 40;
					} else if (className.equals("Jgst. Q2") && gewaehlteKlassen.contains("Q2")) {
						height += 110 + classArrays[i].length() * 40;
					}
				}
				if (height < screen_h) {
					height = screen_h;
				}
				setMeasuredDimension(screen_w, height);
				Log.d("measure", "w: " + screen_w + ", h: " + height + ", 1");
			} catch (JSONException e) {
				Log.e("error", "onMeasure", e);
			}
		} else {
			height = screen_h;
			setMeasuredDimension(screen_w, screen_h);
			Log.d("measure", "w: " + screen_w + ", h: " + screen_h + ", 2");
			invalidate();
		}
		Log.d("Method", "onMeasure_Finished");
	}

	/**
	 * Gibt die übergebene Zahl <code>i</code> als zweistelligen {@link String} zurück. Wenn nötig, setzt es eine 0 davor.
	 */
	public static String zweistellig(int i) {
		if (String.valueOf(i).length() == 1) {
			return "0" + i;
		} else {
			return String.valueOf(i);
		}
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
		Log.d("Click", "VertretungenView: x: " + x + ", y: " + y);
		if (dateObj != null) {
			int y2 = 0;
			out: for (int i = 0; i < classArrays.length; i++) {
				try {
					boolean containsClass = false;
					String className = classArrays[i].getJSONObject(0).getString("klasse");
					Set<String> gewaehlteKlassen = prefs.getStringSet("class_select", empty);
					if (gewaehlteKlassen.contains(className)) {
						containsClass = true;
					} else if (className.equals("Jgst. Q1") && gewaehlteKlassen.contains("Q1")) {
						containsClass = true;
					} else if (className.equals("Jgst. Q2") && gewaehlteKlassen.contains("Q2")) {
						containsClass = true;
					}
					if (containsClass) {
						int y3 = y2 + classArrays[i].length() * 40 + 110;
						if (y < y3) {
							int y4 = y2 + 100;
							if (y >= y4) {
								for (int i2 = 0; i2 < classArrays[i].length(); i2++) {
									if (y < y4 + 40) {
										SelectedObject.set(classArrays[i].getJSONObject(i2));
										viewflipper.showNext();
										break out;
									} else {
										y4 += 40;
									}
								}
							}
							break out;
						} else {
							y2 = y3;
						}
					}
				} catch (JSONException e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	public void changeDate(int change) {
		switch (change) {
		case -1:
			calendar.add(Calendar.DATE, -1);
			hasDate = false;
			readJSON();
			break;
		case 0:
			calendar = Calendar.getInstance(Locale.GERMANY);
			hasDate = false;
			readJSON();
			break;
		case 1:
			calendar.add(Calendar.DATE, 1);
			hasDate = false;
			readJSON();
		default:
			break;
		}
	}

	/**
	 * Zu erst holt die Methode sich neu alle Vertretungen von {@link Downloader}. Dann definiert es {@link #dateObj},{@link #hasDate} und {@link #classArrays} und sortiert es. Als letztes ruft es {@link #requestLayout()} und {@link #invalidate()}
	 * auf.
	 */
	public void readJSON() {
		Log.d("Method", "readJSON");
		try {
			if (Downloader.downloaded) {
				JSONObject schuelervertretungen = Downloader.getVertretungsplan().getJSONObject("schuelervertretungen");
				String sDate = calendar.get(Calendar.YEAR) + "-" + zweistellig(calendar.get(Calendar.MONTH) + 1) + "-" + zweistellig(calendar.get(Calendar.DAY_OF_MONTH));
				if (schuelervertretungen.has(sDate)) {
					dateObj = schuelervertretungen.getJSONObject(sDate);
					hasDate = true;
					classArrays = new JSONArray[dateObj.length() - 1];
					int h = 0;
					for (@SuppressWarnings("unchecked")
					Iterator<String> i = dateObj.keys(); i.hasNext();) {
						String classname = i.next();
						if (!classname.equals("elementscount")) {
							classArrays[h] = dateObj.getJSONArray(classname);
							h++;
						}
					}
					for (int i = 0; i < classArrays.length; i++) {
						for (int j = 0; j < (classArrays.length - 1) - i; j++) {
							JSONArray temp;
							if (classArrays[j].getJSONObject(0).getString("klasse").compareTo(classArrays[j + 1].getJSONObject(0).getString("klasse")) > 0) {
								temp = classArrays[j];
								classArrays[j] = classArrays[j + 1];
								classArrays[j + 1] = temp;
							}
						}
					}
				} else {
					dateObj = null;
					classArrays = null;
				}
			}
		} catch (JSONException e) {
			Log.e("JSON", "error", e);
		}
		requestLayout();
	}

	private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			Log.d("fling", "A");
			try {
				int SWIPE_THRESHOLD = 100;
				int SWIPE_VELOCITY_THRESHOLD = 100;
				float diffY = e2.getY() - e1.getY();
				float diffX = e2.getX() - e1.getX();
				if (Math.abs(diffY) < 100) {
					if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
						if (diffX > 0) {
							calendar.add(Calendar.DATE, -1);
							hasDate = false;
							readJSON();
							return true;
						} else {
							calendar.add(Calendar.DATE, 1);
							hasDate = false;
							readJSON();
							return true;
						}
					} else {
						return false;
					}
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}
	}
}
