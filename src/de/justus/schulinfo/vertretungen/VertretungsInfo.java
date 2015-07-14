package de.justus.schulinfo.vertretungen;

import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.ViewFlipper;
import de.justus.schulinfo.Downloader;
import de.justus.schulinfo.MainActivity;
import de.justus.schulinfo.R;

public class VertretungsInfo extends View {
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
	JSONObject selectedObj = null;
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

	Rect[] links = null;

	ViewFlipper viewflipper;

	HashMap<String, String> infoscreenValues = new HashMap<String, String>();

	String[] infoscreenOrder = { "Datum", "Klasse", "Stunde", "Raum", "Lehrer", "Fach" };

	private Paint selectedPaint = new Paint();

	Bitmap arrow_back;

	Typeface font = Typeface.create("Roboto", Typeface.NORMAL);
	Typeface topic = Typeface.create("Consolas", Typeface.BOLD);

	boolean debug = false;

	public VertretungsInfo(Context context) {
		super(context);
		doAfterConstruct();
	}

	public VertretungsInfo(Context context, AttributeSet attrs) {
		super(context, attrs);
		doAfterConstruct();
	}

	public VertretungsInfo(Context context, AttributeSet attrs, int defStyle) {
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
		setFocusable(true);
		setFocusableInTouchMode(true);
		arrow_back = BitmapFactory.decodeResource(MainActivity.context.getResources(), R.drawable.arrow_back_32);
		debug = prefs.getBoolean("debug", false);
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
	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas canvas) {
		Log.d("Method", "VertretungsInfo:onDraw");
		// TODO Else hinzufügen, selectedObj aber auch in if festlegen.
		if (SelectedObject.exists() && !selectedObj.equals(SelectedObject.get())) {
			requestLayout();
		}
		screen_w = canvas.getWidth();
		paint.setStrokeWidth(2);
		selectedObj = SelectedObject.get();
		paint.setTypeface(font);
		paint.setTextSize(30);
		if (scrollview == null) {
			scrollview = (ScrollView) getParent();
		}
		if (viewflipper == null) {
			viewflipper = (ViewFlipper) scrollview.getParent();
		}
		Log.d("selected", "selectedObj != null: " + (selectedObj != null));
		if (SelectedObject.exists()) {
			int y = 50;
			selectedPaint.setTextSize(25);
			selectedPaint.setTypeface(font);
			selectedPaint.setStrokeWidth(1);
			selectedPaint.setStyle(Paint.Style.FILL);
			try {
				String artText = "";
				if (selectedObj.getString("art") != null) {
					String art = selectedObj.getString("art");
					if (art.equals("C")) {
						artText = "Entfall";
					} else if (art.equals("R")) {
						artText = "Raumvertretung";
					} else if (art.equals("E")) {
						artText = "Klausur";
					} else {
						artText = "Vertretung";
					}
				} else {
					artText = "Vertretung";
				}
				drawMultilineText(artText, 0, y, screen_w, 50, true, canvas, paint, false);
				y += 50;
				for (int i = 0; i < infoscreenOrder.length; i++) {
					if (selectedObj.get(infoscreenValues.get(infoscreenOrder[i])) != null) {
						if (selectedObj.get(infoscreenValues.get(infoscreenOrder[i])) instanceof String && !selectedObj.get(infoscreenValues.get(infoscreenOrder[i])).equals("")) {
							canvas.drawText(String.valueOf(infoscreenOrder[i]), 60, y, selectedPaint);
							canvas.drawText(String.valueOf(selectedObj.get(infoscreenValues.get(infoscreenOrder[i]))).replace('|', '-'),
									screen_w - 60 - selectedPaint.measureText(String.valueOf(selectedObj.get(infoscreenValues.get(infoscreenOrder[i]))).replace('|', '-')), y, selectedPaint);
							y += 30;
						}
					}
				}
				y += 15;
				if (!selectedObj.getString("kommentar").equals("")) {
					Log.d("html", "VertretungsInfo:" + selectedObj.getString("kommentar"));
					if (!(selectedObj.getString("kommentar").equals("#!#") || selectedObj.getString("kommentar").equals("#!!#"))) {
						canvas.drawText("Kommentar", ((screen_w - 60) - paint.measureText("Kommentar")) / 2 + 30, y + 10, paint);
						y += 45;
						y = drawHTML(selectedObj.getString("kommentar"), 30, y, screen_w - 60, 30, canvas, selectedPaint, false);
						y += 15;
					}
				}
				if ((selectedObj.has("materialfiles") && selectedObj.getJSONArray("materialfiles").length() != 0) || selectedObj.has("materialkommentar") && !selectedObj.getString("materialkommentar").equals("")) {
					canvas.drawText("Materialien", ((screen_w - 60) - paint.measureText("Materialien")) / 2 + 30, y + 10, paint);
					y += 45;
					if (selectedObj.has("materialkommentar") && !selectedObj.getString("materialkommentar").equals("")) {
						y = drawHTML(selectedObj.getString("materialkommentar"), 30, y, screen_w - 60, 30, canvas, selectedPaint, false);
						y += 15;
					}
					JSONArray materialfiles = selectedObj.getJSONArray("materialfiles");
					links = new Rect[materialfiles.length()];
					for (int i = 0; i < materialfiles.length(); i++) {
						int y2 = drawLink(materialfiles.getJSONObject(i).getString("file"), 30, y, screen_w - 100, 30, canvas, selectedPaint, false);
						links[i] = new Rect(25, y - (int) paint.getTextSize() + 6, screen_w - 70, y2 - (int) paint.getTextSize() + 8);
						canvas.drawText(String.valueOf(materialfiles.getJSONObject(i).getInt("count")), screen_w - 60, y, selectedPaint);
						y = y2;
						Log.d("box", "i: " + i);
						Log.d("box", "links: " + links);
						Log.d("box", "top: " + links[i].top);
						selectedPaint.setStyle(Paint.Style.STROKE);
						if (debug) {
							canvas.drawRect(links[i], selectedPaint);
						}
						selectedPaint.setStyle(Paint.Style.FILL);
					}
					if (clicked_rect != null) {
						paint.setColor(Color.BLUE);
						paint.setAlpha(100);
						canvas.drawRect(clicked_rect, paint);
						paint.setColor(Color.BLACK);
						paint.setAlpha(255);
					}
				}
				Log.d("draw:height", "VertretungsInfo:" + String.valueOf(y));
				selectedPaint.setStrokeWidth(2);
				canvas.drawLine(0, height - 80, screen_w, height - 80, paint);
				canvas.drawBitmap(arrow_back, (float) (screen_w * 0.5 - 32), height - 64, selectedPaint);
			} catch (JSONException e) {
				Log.e("error", "VertretungsInfo:onDraw", e);
			}
		}
	}

	public int drawMultilineText(String text, float x, float y, float width, float lineheight, boolean center, Canvas canvas, Paint paint, boolean onlymeasure) {
		String[] words = text.split(" ");
		Log.d("Method", "VertretungsInfo:DrawMultilineText");
		int wordpos = 0;
		String lineText = "";
		while (wordpos < words.length) {
			while (wordpos < words.length && paint.measureText(lineText + " " + words[wordpos]) < width) {
				lineText += " " + words[wordpos];
				wordpos++;
			}
			if (!onlymeasure) {
				if (center) {
					canvas.drawText(lineText, (float) (x + (width - paint.measureText(lineText)) * 0.5), y, paint);
				} else {
					canvas.drawText(lineText, x, y, paint);
				}
				lineText = "";
				y += lineheight;
			}
		}
		return (int) y;
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
			Log.e("error", "VertretungsInfo:drawTextNotNull", e);
		}
	}

	@SuppressLint("DrawAllocation")
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d("Method", "VertretungsInfo:onMeasure");
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		int statusbar_height = 0;
		if (resourceId > 0) {
			statusbar_height = getResources().getDimensionPixelSize(resourceId);
		}
		screen_h = getContext().getResources().getDisplayMetrics().heightPixels - (statusbar_height + MainActivity.actionbar.getHeight());
		screen_w = getContext().getResources().getDisplayMetrics().widthPixels;
		if (infoscreenValues.isEmpty()) {
			setupInfoscreenOrder();
		}
		Log.d("measure", "VertretungsInfo:SelectedObj.exists(): " + SelectedObject.exists());
		try {
			if (SelectedObject.exists()) {
				selectedObj = SelectedObject.get();
				int y = 100;
				Log.d("measure", "VertretungsInfo:1: " + y);
				paint.setTypeface(font);
				paint.setTextSize(30);
				selectedPaint.setTextSize(25);
				selectedPaint.setTypeface(font);
				selectedPaint.setStrokeWidth(1);
				selectedPaint.setStyle(Paint.Style.FILL);
				for (int i = 0; i < infoscreenOrder.length; i++) {
					if (selectedObj.get(infoscreenValues.get(infoscreenOrder[i])) != null) {
						if (selectedObj.get(infoscreenValues.get(infoscreenOrder[i])) instanceof String && !selectedObj.get(infoscreenValues.get(infoscreenOrder[i])).equals("")) {
							y += 30;
						}
					}
				}
				y += 15;
				Log.d("measure", "VertretungsInfo:2: " + y);
				if (!selectedObj.getString("kommentar").equals("")) {
					Log.d("html", selectedObj.getString("kommentar"));
					if (!(selectedObj.getString("kommentar").equals("#!#") || selectedObj.getString("kommentar").equals("#!!#"))) {
						y += 45;
						y = drawHTML(selectedObj.getString("kommentar"), 30, y, screen_w - 60, 30, null, selectedPaint, true);
						y += 15;
					}
				}
				Log.d("measure", "VertretungsInfo:3: " + y);
				if ((selectedObj.has("materialfiles") && selectedObj.getJSONArray("materialfiles").length() != 0) || selectedObj.has("materialkommentar") && !selectedObj.getString("materialkommentar").equals("")) {
					y += 45;
					if (selectedObj.has("materialkommentar") && !selectedObj.getString("materialkommentar").equals("")) {
						y = drawHTML(selectedObj.getString("materialkommentar"), 30, y, screen_w - 60, 30, null, selectedPaint, true);
						y += 15;
					}
					Log.d("measure", "VertretungsInfo:4: " + y);
					JSONArray materialfiles = selectedObj.getJSONArray("materialfiles");
					for (int i = 0; i < materialfiles.length(); i++) {
						int y2 = drawLink(materialfiles.getJSONObject(i).getString("file"), 30, y, screen_w - 100, 30, null, selectedPaint, true);
						y = y2;
						y += 10;
					}
				}
				y += 50;
				Log.d("measure", "VertretungsInfo:5: " + y);
				height = y;
				Log.d("measure", String.valueOf(y));
			}
		} catch (JSONException e) {
			Log.e("error", "VertretungsInfo:onMeasure", e);
		}
		if (height < screen_h) {
			height = screen_h;
		}
		setMeasuredDimension(screen_w, height);
		Log.d("measure", "VertretungsInfo: w: " + screen_w + ", h: " + height);
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
		Log.d("Click", "x: " + x + ", y: " + y);
		if (y > height - 80) {
			SelectedObject.set(null);
			viewflipper.showPrevious();
		} else {
			if (links != null) {
				out: for (int i = 0; i < links.length; i++) {
					if (links[i].contains(x, y)) {
						try {

							openInBrowser(selectedObj.getJSONArray("materialfiles").getJSONObject(i).getString("file"));
							final int i2 = i;
							new Thread(new Runnable() {
								@Override
								public synchronized void run() {
									clicked_rect = links[i2];
									postInvalidate();
									try {
										wait(300);
									} catch (InterruptedException e) {
										Log.e("error", "VertretungsInfo:onClick", e);
									}
									clicked_rect = null;
									postInvalidate();
								}
							}).start();
						} catch (JSONException e) {
							Log.e("error", "VertretungsInfo:onClick", e);
						}
						break out;
					}
				}
			}
		}
	}

	/**
	 * Ein Listener, der überprüft, ob die BACK-Taste gedrückt wird, um das Infofenster der ausgewählten Vertretung zu schließen.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d("Touch", "VertretungsInfo:" + KeyEvent.keyCodeToString(keyCode));
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.d("Touch", "VertretungsInfo: Button_BACK");
			SelectedObject.set(null);
			viewflipper.showPrevious();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public int drawHTML(String htmltext, float x, float y, float width, float lineheight, Canvas canvas, Paint paint, boolean onlymeasure) {
		Log.d("Method", "VertretungsInfo:drawHTML");
		float x2 = x;
		String[] raute = htmltext.split("#");
		int posTags = 0;
		String[] tags = null;
		if (raute.length == 3) {
			if (!raute[2].equals("")) {
				tags = raute[2].split("<|>");
			}
		} else if (raute.length == 1) {
			tags = raute[0].split("<|>");
		} else {
			for (int i = 0; i < raute.length; i++) {
				Log.d("error", raute[i]);
			}
			Log.wtf("raute", raute.length + "");
		}
		boolean isTag = false;
		boolean isBold = false;
		boolean isUnderlined = false;
		while (posTags < tags.length) {
			if (isTag) {
				if (tags[posTags].equals("b")) {
					isBold = true;
				} else if (tags[posTags].equals("/b")) {
					isBold = false;
				} else if (tags[posTags].equals("u")) {
					isUnderlined = true;
				} else if (tags[posTags].equals("/u")) {
					isUnderlined = false;
				} else if (tags[posTags].equals("br") || tags[posTags].equals("/br")) {
					y += lineheight;
					x2 = x;
				}
			} else {
				if (!tags[posTags].equals("")) {
					String[] text = tags[posTags].split(" ");
					int posWord = 0;
					paint.setFakeBoldText(isBold);
					paint.setUnderlineText(isUnderlined);
					while (posWord < text.length) {
						while (posWord < text.length && x2 + paint.measureText(text[posWord]) < width) {
							if (!onlymeasure) {
								canvas.drawText(text[posWord], x2, y, paint);
							}
							x2 += paint.measureText(text[posWord] + " ");
							posWord++;
						}
						if (posWord < text.length) {
							y += lineheight;
							x2 = x;
						}
					}
				}
			}
			isTag = !isTag;
			posTags++;
		}
		paint.setUnderlineText(false);
		paint.setFakeBoldText(false);
		return (int) (y + lineheight);
	}

	public int drawLink(String link, float x, float y, float width, float lineheight, Canvas canvas, Paint paint, boolean onlymeasure) {
		Log.d("Method", "VertretungsInfo:DrawMultilineText");
		paint.setUnderlineText(true);
		paint.setColor(Color.rgb(0, 99, 236));
		int charpos = 0;
		String lineText = "";
		while (charpos < link.length()) {
			while (charpos < link.length() && paint.measureText(lineText + " " + link.charAt(charpos)) < width) {
				lineText += link.charAt(charpos);
				charpos++;
			}
			if (!onlymeasure) {
				canvas.drawText(lineText, x, y, paint);
			}
			lineText = "";
			y += lineheight;
		}
		paint.setUnderlineText(false);
		paint.setColor(Color.BLACK);
		return (int) y;
	}

	public void openInBrowser(String filename) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + prefs.getString("url", "") + "/images/vertretungsmaterial/" + filename));
		getContext().startActivity(browserIntent);
	}
}
