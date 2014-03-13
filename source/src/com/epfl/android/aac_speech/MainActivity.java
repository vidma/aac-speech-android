package com.epfl.android.aac_speech;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.lang.Math;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.epfl.android.aac_speech.data.DBHelper;
import com.epfl.android.aac_speech.data.LowLevelDatabaseHelper;
import com.epfl.android.aac_speech.data.PhraseProviderDB;
import com.epfl.android.aac_speech.data.Pictogram;
import com.epfl.android.aac_speech.data.PictogramFactory;
import com.epfl.android.aac_speech.data.models.PhraseHistory;
import com.epfl.android.aac_speech.nlg.Pic2NLG;
import com.epfl.android.aac_speech.nlg.Pic2NLG.ActionType;
import com.epfl.android.aac_speech.ui.DynamicHorizontalScrollView;
import com.epfl.android.aac_speech.ui.HomeFeatureLayout;
import com.epfl.android.aac_speech.ui.PictogramCursorAdapter;
import com.epfl.android.aac_speech.ui.ScalingLinearLayout;
import com.epfl.android.aac_speech.ui.UIFactory;

public class MainActivity extends TTSButtonActivity implements UncaughtExceptionHandler {

	DBHelper dbHelper = null;
	private PictogramFactory pictogramFactory = null;
	private UIFactory uiFactory = null;
	public static Pic2NLG nlgConverter = null;

	// initialized in onCreate
	private LayoutInflater inflater;
	Resources res;

	public static ArrayList<Pictogram> phrase_list = new ArrayList<Pictogram>();

	public static final boolean DEBUG = false;

	public static final String APP_CONTENT_FILE_DOWN_URL = "https://github.com/vidma/aac-speech-android/releases/download/v1.2beta/aac_speech_data.zip";

	public static boolean isTablet = false;

	public static Boolean nlg_state_subject_selected = false;

	// our GUI
	private int currentCategoryId = 0;
	static final int PROGRESS_DIALOG = 0;
	ProgressDialog progressDialog;

	// default Preferences

	/* Default preferences */
	private final static boolean PREF_UPERCASE_DEFAULT = false;
	private final static boolean PREF_HIDE_SPC_DEFAULT = false;
	private final boolean PREF_CLEAR_PHRASE_AFTER_SPEAK_DEFAULT = false;
	private final String PREF_GENDER_DEFAULT = "MALE";

	private static boolean pref_uppercase = PREF_UPERCASE_DEFAULT;
	private static boolean pref_hide_spc_color = PREF_HIDE_SPC_DEFAULT;

	private String pref_gender = PREF_GENDER_DEFAULT;
	private boolean pref_hide_offensive = true;
	private boolean pref_clear_phrase_after_speak = PREF_CLEAR_PHRASE_AFTER_SPEAK_DEFAULT;

	private boolean pref_switch_back_to_main_screen = true;

	private String nlg_text;
	private PendingIntent restart_intent;

	static TextView wordsToSpeak = null;

	private static final String TAG = "AAC";

	/* Menu */
	private static final int MENU_GESTURE_SEARCH_ID = 1;
	private static final int MENU_ABOUT = 3;
	private static final int MENU_PREFS = 4;
	private static final int MENU_HISTORY = 5;

	/* Screens */
	protected static final int FLIPPER_VIEW_HOME = 0;
	protected static final int FLIPPER_VIEW_CATEGORY_LISTING = 1;
	protected static final int FLIPPER_VIEW_LISTVIEW_SEARCH = 2;

	/*
	 * Interaction with Gesture Search
	 */
	/**
	 * Optionally, specify what should be shown when launching Gesture Search.
	 * If this is not specified, SHOW_HISTORY will be used as a default value.
	 */
	private static String SHOW_MODE = "show";
	/** Possible values for invoking mode */
	// Show the visited items
	private static final int SHOW_HISTORY = 0;
	// Show nothing (a blank screen)
	private static final int SHOW_NONE = 1;
	// Show all of date items
	private static final int SHOW_ALL = 2;

	/**
	 * The theme of Gesture Search can be light or dark
	 */
	private static final String THEME = "theme";
	private static final int THEME_LIGHT = 0;
	private static final int THEME_DARK = 1;

	/** Keys for results returned by Gesture Search */
	private static final String SELECTED_ITEM_ID = "selected_item_id";
	private static final String SAVED_INST_PHRASE_KEY = "phrase_list";

	private void GestureSearch() {
		try {
			Intent intent = new Intent();
			intent.setAction("com.google.android.apps.gesturesearch.SEARCH");

			// TODO: optionally pass part of speech or category parameter (as URI)
			Uri content_uri = PhraseProviderDB.GESTURE_SEARCH_CONTENT_URI;
			if (currentCategoryId != 0) {
				content_uri = ContentUris.withAppendedId(PhraseProviderDB.GESTURE_SEARCH_BY_CATEGORY_CONTENT_URI,
						currentCategoryId);
			}
			intent.setData(content_uri);
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			// TODO: show history or all. but she nows the first letter. this
			// may be an option!!! :)

			// TODO: Hmmm. History shows only the last one

			intent.putExtra(SHOW_MODE, SHOW_ALL);
			intent.putExtra(THEME, THEME_DARK);
			startActivityForResult(intent, MENU_GESTURE_SEARCH_ID);
		} catch (ActivityNotFoundException e) {
			Log.e("GestureSearchExample", "Gesture Search is not installed", e);
			// e.printStackTrace();
			Log.i("ListSearch", "Falling back to homemade listview search...");

			// Falling back to homemade listview search
			performListViewSearch();
		}
	}

	@Override
	public boolean onSearchRequested() {
		Log.e("act", "Search Requested");
		GestureSearch();
		return true;
	}

	// END OF GESTURE SEARCH STUFF

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_GESTURE_SEARCH_ID, 0, R.string.menu_gesture_search).setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, MENU_HISTORY, 0, R.string.menu_history).setIcon(android.R.drawable.ic_menu_recent_history);
		menu.add(0, MENU_ABOUT, 0, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(0, MENU_PREFS, 0, R.string.menu_preferences).setIcon(android.R.drawable.ic_menu_preferences);		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_GESTURE_SEARCH_ID:
			GestureSearch();
			return true;

		case MENU_ABOUT:
			Intent intent = new Intent(MainActivity.this, AboutActivity.class);
			startActivity(intent);
			return true;

		case MENU_PREFS:
			Intent intent1 = new Intent(MainActivity.this, PreferencesActivity.class);
			startActivityForResult(intent1, MENU_PREFS);
			return true;

		case MENU_HISTORY:
			showHistory();
			return true;

		}
		return super.onOptionsItemSelected(item);

	}

	/**
	 * 
	 */
	private void showHistory() {
		// build a Cursor
		Cursor c = dbHelper.getPhraseHistoryCursor(null);

		// feed the cursor into adapter
		String[] columns = new String[] { PhraseHistory.COL_PHRASE, };
		/* R.id.search_list_entry_icon, */
		int[] to = new int[] { R.id.search_list_entry_icon_text };

		final PictogramCursorAdapter adapter = new PictogramCursorAdapter(this, R.layout.history_list_entry, c,
				columns, to, pref_uppercase);

		// TODO: we now hide the search as it doesn't look good on all Mobiles
		LinearLayout l = (LinearLayout) findViewById(R.id.listview_search_layout_cont);
		l.setVisibility(View.INVISIBLE);

		ListView listview = (ListView) findViewById(R.id.search_results_listview);
		listview.setAdapter(adapter);
		listview.setTextFilterEnabled(false);

		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long phrase_id) {
				// Recreate the phrase
				String serialized = dbHelper.getSerializedPhraseById(phrase_id);
				phrase_list = pictogramFactory.createFromSerialized(serialized);
				updatePhraseDisplay();

				// Go back to default view
				ViewFlipper switcher = (ViewFlipper) findViewById(R.id.view_switcher);

				// try to hide the keyboard
				InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				EditText search_q = (EditText) findViewById(R.id.listview_search_text);
				mgr.hideSoftInputFromWindow(search_q.getWindowToken(), 0);

				// select the home screen again
				switcher.setDisplayedChild(FLIPPER_VIEW_HOME);

			}

		});

		ViewFlipper switcher = (ViewFlipper) findViewById(R.id.view_switcher);
		switcher.setDisplayedChild(FLIPPER_VIEW_LISTVIEW_SEARCH);
	}

	@Override
	protected void onResume() {
		super.onStart();

		getPreferences();

		// if preferences changed, update DBHelper with global preferences?
		this.dbHelper.pref_hide_offensive = this.pref_hide_offensive;

		// if preferences changed, we need to re-render the text. no need to
		// do so if nlg not loaded yet, as everything is initialized
		// afterwards
		if (nlgConverter != null) {
			// reload NLG functions again that depend on settings
			// that includes the pictogramFactory
			onNLGload_initGUI();

			// a special case is that gender may have affected the phrase_list
			// easiest work-around is to serialize and de-serialize it again
			phrase_list = pictogramFactory.createFromSerialized(pictogramFactory.getSerialized(phrase_list));

			// repaint the current phrase as it may have changed
			updatePhraseDisplay();
		}
	}

	/**
	 * 
	 */
	private void getPreferences() {
		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		pref_uppercase = prefs.getBoolean("pref_uppercase", PREF_UPERCASE_DEFAULT);
		pref_clear_phrase_after_speak = prefs.getBoolean("pref_clear_phrase_after_speak",
				PREF_CLEAR_PHRASE_AFTER_SPEAK_DEFAULT);
		pref_gender = prefs.getString("pref_gender", PREF_GENDER_DEFAULT);
		pref_hide_spc_color = prefs.getBoolean("pref_hide_spc_color", PREF_HIDE_SPC_DEFAULT);
		pref_hide_offensive = prefs.getBoolean("pref_hide_offensive", true);

	}

	public static boolean getPrefHideSPCColor() {
		return MainActivity.pref_hide_spc_color;
	}

	/**
	 * Updates current phrase displayed once it has changed
	 * 
	 * Prerequisite: NLG loaded
	 */
	private final void updatePhraseDisplay() {
		nlg_text = "";
		if (phrase_list.size() > 0)
			nlg_text = nlgConverter.convertPhrasesToNLG(phrase_list);

		Boolean is_subject_selected = nlgConverter.hasSubjectBeenSelected(phrase_list);
		if (is_subject_selected != nlg_state_subject_selected) {
			nlg_state_subject_selected = is_subject_selected;
			createImageButtons();
		}
		wordsToSpeak.setText(getText(nlg_text));
		drawCurrentIcons();
	}

	private final void addWord(final Pictogram currentButton) {
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(100);
		phrase_list.add(currentButton);
		Log.d("addWord", currentButton.data + " " + currentButton.type + " " + currentButton.element);
		updatePhraseDisplay();
	}

	public static String getNaturalLanguageText() {
		String result = "";
		if (phrase_list.size() > 0) {
			nlgConverter.convertPhrasesToNLG(phrase_list);
		}
		return result;
	}

	private void loadNLG(Bundle savedInstanceState) {
		final ProgressDialog nlg_wait = ProgressDialog.show(MainActivity.this, "", res.getString(R.string.loading_nlg),
				true);

		Log.d(TAG, "loadNLG instState=" + savedInstanceState);
		final Bundle fsavedInstanceState = savedInstanceState;
		
		// Define the Handler that receives messages from the thread and update
		// the GUI then NLG is loaded
		// GUI has to be manipulated within the same thread
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				onNLGload_initGUI();
				Log.d(TAG, "loadNLG simpeNLG loading done instState=" + fsavedInstanceState);
				restoreInstanceState(fsavedInstanceState);
				nlg_wait.dismiss();
			}
		};

		Thread init_nlg = new Thread() {
			@Override
			public void run() {
				Log.d(TAG, "creating NLG for lang=" + getPreferedLanguage());

				MainActivity.nlgConverter = new Pic2NLG(getPreferedLanguage());
				Message msg = handler.obtainMessage();
				// msg.arg1 = total;
				handler.sendMessage(msg);
			}
		};

		init_nlg.start();

	}

	private void drawCurrentIcons() {
		// TODO: move out to UI factory?
		LinearLayout icon_list = (LinearLayout) findViewById(R.id.icon_history_layout);
		DynamicHorizontalScrollView scroller = (DynamicHorizontalScrollView) findViewById(R.id.icon_history_scrollview);
		/*
		 * as we are about to change the listing of icons in case of
		 * "inteligent guesses", we have to rebuild the whole listing
		 */
		icon_list.removeAllViews();

		int index = 0;

		/* Add icons */
		for (Pictogram currentButton : phrase_list) {
			View view = uiFactory.createImageButton(icon_list, currentButton,
					R.layout.top_status_current_phrase_imagebutton);
			ImageButton img = (ImageButton) view.findViewById(R.id.icons_imgButton);

			final int currentIndex = index;
			img.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.i("onCLik @history", "onclick");

					View layout = inflater.inflate(R.layout.popup_icon_settings, null, false);
					// create a 300px width and 470px height PopupWindow
					final PopupWindow pw = new PopupWindow(layout, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT,
							true);
					pw.setBackgroundDrawable(new BitmapDrawable());
					pw.setOutsideTouchable(true);

					// TODO: PopupWindow#setOutsideTouchable(true)
					// display the popup in the center
					Button cancel_btn = (Button) layout.findViewById(R.id.popup_cancel);
					cancel_btn.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							pw.dismiss();
						}
					});

					Button remove_btn = (Button) layout.findViewById(R.id.popup_remove_icon);
					remove_btn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// remove the icon and redraw the current icon list
							phrase_list.remove(currentIndex);
							updatePhraseDisplay();
							pw.dismiss();
						}
					});
					pw.showAtLocation(findViewById(R.id.main), Gravity.CENTER, 0, 0);

				}
			});

			icon_list.addView(view);
			index++;
		}

		/*
		 * scroll to the end of the list to see the most recent items if there
		 * are more than it fits
		 */
		scroller.fullScrollOnLayout(View.FOCUS_RIGHT);

	}

	private boolean isTablet() {
		// TODO: make sure large mobiles are not too small !!!
		boolean is_tablet = false;
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int height = metrics.heightPixels;
		int width = metrics.widthPixels;
		
		int layout = getResources().getConfiguration().screenLayout;
		if ((layout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
		    // on a large screen device ...
			Log.d(TAG, "large");
			is_tablet = true; //TODO: shall this allow landscape or not!?
		}
		if ((layout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
		    // on an xlarge screen - certainly tablet...
			Log.d(TAG, "is tablet; xlarge");
			is_tablet = true;
		}
		if ((layout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
		    // on an xlarge screen - certainly tablet...
			Log.d(TAG, "is tablet; normal");
			//is_tablet = true;
		}		
		Log.d(TAG, "is tablet=" + is_tablet);
		return is_tablet;
	}

	/**
	 * Choses between two different layouts, one for tablet other for mobile
	 * 
	 * we use the "super horizontal scroller" only for mobiles, not for tablets
	 */
	private void createImageButtons() {
		// TODO: image buttons could be reused afterwards, only changing the text/action...
		Log.d(TAG, "createImageButtons:start");
		boolean is_tablet = isTablet();
		ViewGroup home_screen_layout = (LinearLayout) findViewById(R.id.home_screen);
		home_screen_layout.removeAllViews();

		/* TODO: persist UIFActory */
		uiFactory.nlg_state_subject_selected = nlg_state_subject_selected;

		/* We shall have tablet, so we could fit everything into one page */
		if (is_tablet) {
			TableLayout tl = (TableLayout) inflater.inflate(R.layout.tablelayout, home_screen_layout, false);
			uiFactory.createHomePictogramTable(tl);
			TableLayout tl1 = uiFactory.createImageButtonsCategoriesRight(home_screen_layout);

			home_screen_layout.addView(tl);
			home_screen_layout.addView(tl1);

			// in landscape mode we show both screens side-by-side
			if (home_screen_layout instanceof ScalingLinearLayout){
				tl1.setPadding(20, 0, 0, 0); // add spacing between main and secondary column
				ScalingLinearLayout l = (ScalingLinearLayout)home_screen_layout;
				// TODO: some of the later steps might be optional
				l.cleanup(); // we're reusing old element right now ...				
				l.invalidate();
				l.requestLayout(); // refresh view and recalculate the size
				//l.refreshDrawableState();
			}

		} else {
			// on a smaller device, we use a scroller to switch between the two views
			// TODO: Android standard tools might be also good or even better
			HomeFeatureLayout super_scroller = 
					(HomeFeatureLayout) inflater.inflate(R.layout.horizontal_flip_layout, home_screen_layout, false);
			super_scroller.init();

			ViewGroup parent = super_scroller.internalWrapper;
			TableLayout tl = (TableLayout) inflater.inflate(R.layout.tablelayout, parent, false);

			uiFactory.createHomePictogramTable(tl);
			TableLayout tl1 = uiFactory.createImageButtonsCategoriesRight(parent);

			ArrayList<View> items = new ArrayList<View>();
			items.add(tl);
			items.add(tl1);
			
			// Set Layout size to match the screen, it's not automatically resized within scrollable thing			
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			int height = metrics.heightPixels;
			int width = metrics.widthPixels;
			for (View item : items) {
				item.setLayoutParams(new LayoutParams(width, height));
			}

			super_scroller.setFeatureItems(items);
			home_screen_layout.addView(super_scroller);
		}

		Log.d(TAG, "createImageButtons:end");
	}

	private void showCategory(int category_id) {
		// get icons in category
		Cursor c = dbHelper.getIconsCursorByCategory(category_id, null);
		Cursor c_recents = dbHelper.getRecentIconsCursorByCategory(category_id);
		MergeCursor merged_cursor = new MergeCursor(new Cursor[] { c_recents, c });		

		// Draw the grid
		GridView gv = (GridView) findViewById(R.id.category_gridView);

		String[] map_from = new String[] { "icon_path", "word" };
		int[] map_to = new int[] { R.id.search_list_entry_icon, R.id.search_list_entry_icon_text };
		PictogramCursorAdapter adapter = new PictogramCursorAdapter(this, R.layout.gridview_icon_entry, merged_cursor,
				map_from, map_to, pref_uppercase);
		gv.setAdapter(adapter);
		gv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Pictogram selected_icon = dbHelper.getIconById(id);
				addWord(selected_icon);
				ViewFlipper switcher = (ViewFlipper) findViewById(R.id.view_switcher);
				// select the home screen again
				switcher.setDisplayedChild(FLIPPER_VIEW_HOME);
			}
		});

		// TODO: we now hide the search in History as it doesn't look good on all Mobiles
		LinearLayout l = (LinearLayout) findViewById(R.id.listview_search_layout_cont);
		l.setVisibility(View.VISIBLE);

		// display category title
		TextView category_title = (TextView) findViewById(R.id.category_title);
		category_title.setText(dbHelper.getCategoryTitle(category_id));

		currentCategoryId = category_id;

		// switch to the Category view
		ViewFlipper switcher = (ViewFlipper) findViewById(R.id.view_switcher);
		switcher.setDisplayedChild(FLIPPER_VIEW_CATEGORY_LISTING);
	}

	private void performListViewSearch() {
		// build a Cursor
		Cursor c = dbHelper.getIconsCursorByCategory(currentCategoryId, null);

		// feed the cursor into adapter
		String[] map_from = new String[] { "icon_path", "word" };
		int[] map_to = new int[] { R.id.search_list_entry_icon, R.id.search_list_entry_icon_text };

		final PictogramCursorAdapter adapter = new PictogramCursorAdapter(this, R.layout.search_list_entry, c, map_from,
				map_to, pref_uppercase);

		adapter.setFilterQueryProvider(new FilterQueryProvider() {
			@Override
			public Cursor runQuery(CharSequence constraint) {
				ViewFlipper switcher = (ViewFlipper) findViewById(R.id.view_switcher);
				// make sure the results are visible
				if (switcher.getDisplayedChild() != FLIPPER_VIEW_LISTVIEW_SEARCH)
					switcher.setDisplayedChild(FLIPPER_VIEW_LISTVIEW_SEARCH);
				
				return dbHelper.getIconsCursorByCategory(currentCategoryId, (String) constraint);
			}
		});

		// set up the ListView and add search capability
		EditText search_q = (EditText) findViewById(R.id.listview_search_text);
		search_q.setText("");

		ListView listview = (ListView) findViewById(R.id.search_results_listview);
		listview.setAdapter(adapter);
		listview.setTextFilterEnabled(true);

		View parent = findViewById(R.id.listview_search_layout);

		search_q.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable search_query) {
				adapter.getFilter().filter(search_query);
			}
		});

		listview.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Pictogram selected_icon = dbHelper.getIconById(id);
				addWord(selected_icon);
				ViewFlipper switcher = (ViewFlipper) findViewById(R.id.view_switcher);

				// try to hide the keyboard
				InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				EditText search_q = (EditText) findViewById(R.id.listview_search_text);
				mgr.hideSoftInputFromWindow(search_q.getWindowToken(), 0);

				// select the home screen again
				switcher.setDisplayedChild(FLIPPER_VIEW_HOME);
			}

		});

		ViewFlipper switcher = (ViewFlipper) findViewById(R.id.view_switcher);
		switcher.setDisplayedChild(FLIPPER_VIEW_LISTVIEW_SEARCH);

		// TODO: requesting/displaying the onscreenkeyboard programatically is
		// sufficiently messy on android and do not seem to work. may it help
		// calling this with a delay (once the view was displayed)
		search_q.requestFocus();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.dbHelper = null;
		this.pictogramFactory = null;
		this.uiFactory = null;
		MainActivity.nlgConverter = null;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		// serialize the current phrase
		if (this.pictogramFactory != null && MainActivity.phrase_list != null)
			outState.putString(SAVED_INST_PHRASE_KEY, this.pictogramFactory.getSerialized(phrase_list));
	}

	/**
	 * restores the current phrase from its serialized form saved in Bundle
	 * 
	 * @param savedInstState
	 */
	protected void restoreInstanceState(Bundle savedInstState) {
		Log.d(TAG, "restoreInstanceState from bundle=" + savedInstState + " picFactory=" + pictogramFactory
				+ " nlgConv=" + nlgConverter);
		if (savedInstState != null && savedInstState.containsKey(SAVED_INST_PHRASE_KEY) && pictogramFactory != null) {
			phrase_list = pictogramFactory.createFromSerialized(savedInstState.getString(SAVED_INST_PHRASE_KEY));
			Log.d(TAG, "restoredInstanceState from: " + savedInstState.getString(SAVED_INST_PHRASE_KEY));
			updatePhraseDisplay();
		}
	}
	
	/*
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		Log.d(TAG, "onConfigurationChanged");
		res = getResources();
		inflater = getLayoutInflater();
		setContentView(R.layout.main);
		updatePhraseDisplay();
		this.onResume();
	}*/

	/**
	 * Returns the text either in uppercase or normal, according to prefs
	 * 
	 * @param text
	 * @return
	 */
	public static String getText(String text) {
		if (pref_uppercase)
			return text.toUpperCase();
		return text;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "Starting. onCreate. nlgConverter=" + nlgConverter + " nlgText = " + nlg_text);

		// remove title (label bar) to save space
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		// To further save space on MobilePhones: Remove notification bar
		MainActivity.isTablet = isTablet();
		if (!isTablet()) {
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			// force portrait orientation for non-tablets
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		setContentView(R.layout.main);

		// Allow restarting the activity
		restart_intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(getIntent()), getIntent().getFlags());

		// Disabled: In production, override any unexpected (non-handled) exceptions with restart of application
		if (!MainActivity.DEBUG) {
			// TODO: send trace to server; Internet may be not available
			// TODO: Thread.setDefaultUncaughtExceptionHandler(this);
		}

		/* initialize activity level variables */
		getPreferences();
		dbHelper = new DBHelper(getContentResolver(), this.pref_hide_offensive);
		inflater = getLayoutInflater();
		res = getResources();

		wordsToSpeak = (TextView) findViewById(R.id.wordsToSpeak);

		/* category view - go back button */
		Button category_back = (Button) findViewById(R.id.category_go_back);
		Button search_back = (Button) findViewById(R.id.listview_search_go_back);

		OnClickListener back_to_home_handler = new OnClickListener() {
			@Override
			public void onClick(View v) {
				returnToMainScreen();
			}
		};

		category_back.setOnClickListener(back_to_home_handler);
		search_back.setOnClickListener(back_to_home_handler);

		/*
		 * initialise NLG and Application
		 */

		if (ensureDataInstalledOrQuit()) {
			// There is no use in loading the slow simpleNLG is no data is installed
			loadNLG(savedInstanceState);
		}

		// after everything is loaded, enable speaking button
		ui_enable_tts();
		Log.d(TAG, "on create end");
	}

	/**
	 * @return
	 * 
	 */
	private boolean ensureDataInstalledOrQuit() {
		/*
		 * even before loading NLG check if categories and icons data have been
		 * a) downloaded (TODO: what if download have failed earlier!)
		 * b) database was successfully created
		 * 
		 * DB would be created only if download finished succesfully
		 */

		if (!LowLevelDatabaseHelper.checkDataFileExistance(getApplicationContext())) {
			String msg = getResources().getString(R.string.no_datafiles_found_question);
			String yes = getResources().getString(R.string.no_datafiles_found_question_yes);
			String no = getResources().getString(R.string.no_datafiles_found_question_no);

			AlertDialog.Builder alert_dlg = new AlertDialog.Builder(this);			
			alert_dlg.setMessage(msg).setCancelable(false).setPositiveButton(yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// download icons
					dialog.cancel();
					PreferencesActivity.update_pictograms(MainActivity.this);
				}
			}).setNegativeButton(no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					// show the message
					String cancel_msg = getResources().getString(R.string.no_datafiles_found_msg_quit);
					Toast.makeText(MainActivity.this, cancel_msg, Toast.LENGTH_SHORT).show();
					// close the application
					MainActivity.this.finish();
				}
			});
			alert_dlg.create().show();
			return false;
		}
		// TODO: check database
		return true;
	}

	/**
	 * Switch back to the main View from from the flipper
	 */
	public boolean returnToMainScreen() {
		if (currentCategoryId != 0) {
			currentCategoryId = 0;
		}

		/* allow default implementation of back button */
		ViewFlipper switcher = (ViewFlipper) findViewById(R.id.view_switcher);
		if (switcher.getDisplayedChild() == MainActivity.FLIPPER_VIEW_HOME)
			return false;
		
		switchFlipperScreenTo(MainActivity.FLIPPER_VIEW_HOME);
		return true;
	}

	/**
	 * @param view
	 */
	private void switchFlipperScreenTo(int view) {
		/* Stay in the same screen if such preference is set */
		if (view == FLIPPER_VIEW_HOME && !pref_switch_back_to_main_screen)
			return;

		ViewFlipper switcher = (ViewFlipper) findViewById(R.id.view_switcher);
		switcher.setDisplayedChild(view);
	}

	@Override
	public void onBackPressed() {
		if (!returnToMainScreen()) {
			super.onBackPressed();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// TODO: these two thing shall be better separated
		switch (requestCode) {
		case MENU_GESTURE_SEARCH_ID:
			if (resultCode == Activity.RESULT_OK) {
				long selectedItemId = data.getLongExtra(SELECTED_ITEM_ID, -1);

				// Get the complete Icon+word data
				Pictogram newWord = null;
				newWord = dbHelper.getIconById(selectedItemId);
				if (newWord != null) {
					addWord(newWord);
					// switch back to the main screen
					switchFlipperScreenTo(MainActivity.FLIPPER_VIEW_HOME);
				}

			}
			break;

		case MENU_PREFS:
			// if new data was installed then we restart this activity
			if (resultCode == PreferencesActivity.RESULT_DATA_UPDATED) {
				restartActivity(0);
			}
			break;
		}

	}

	private void historyAdd(String text, ArrayList<Pictogram> phrase) {
		// TODO: store main icons on DB too?
		String serialized = pictogramFactory.getSerialized(phrase);
		Log.d(TAG, "history serialized:" + serialized);
		dbHelper.updateIconHistory(phrase, serialized, text);
		dbHelper.update_icon_use_count(phrase, getApplicationContext());
	}

	/**
	 * 
	 */
	@Override
	protected void onSpeakButtonClicked() {
		historyAdd(nlg_text, phrase_list);
		this.speak(nlg_text);
		if (pref_clear_phrase_after_speak) {
			wordsToSpeak.setText("");
			phrase_list.clear();
		}
		updatePhraseDisplay();
	}

	/**
	 * 
	 */
	private void onNLGload_initGUI() {
		/*
		 * create the image buttons: with current implementation UI buttons can
		 * not be created before NLG is loaded
		 */
		pictogramFactory = new PictogramFactory(dbHelper, pref_gender, res);
		OnClickListener items_onclick_listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Pictogram currentButton = (Pictogram) v.getTag();
				if (currentButton != null) {
					if (currentButton.type == ActionType.CATEGORY) {
						try {
							int catId = Integer.parseInt(currentButton.data);
							showCategory(catId);
						} catch (NumberFormatException e) {
						}

					} else {
						addWord(currentButton);
						/* immediately return to main window */
						if (currentCategoryId != 0) {
							returnToMainScreen();
						}
					}
				}

			}
		};

		uiFactory = new UIFactory(inflater, getApplicationContext(), pictogramFactory, items_onclick_listener, dbHelper);
		createImageButtons();
		updatePhraseDisplay();

		/* activate long-click of backspace as delete all phrase */
		ImageButton btn_backspace = (ImageButton) findViewById(R.id.delete);
		btn_backspace.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				phrase_list.clear();
				updatePhraseDisplay();
				return true;
			}
		});
		
		/* regular-click of backspace to delete the last icon */
		btn_backspace.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: shall I add confirmation here as it's close to the speak button ?
				if (phrase_list.size() > 0)
					phrase_list.remove(phrase_list.size() - 1);
				updatePhraseDisplay();
			}
		});
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e(TAG, "uncaughtException" + ex.toString(), ex);
		ex.printStackTrace();
		restartActivity(2);
	}

	/**
	 * Restart the activity.
	 * 
	 * uses restart_activity_intent initialized in onCreate
	 */
	protected void restartActivity(int code) {
		if (restart_intent != null) {
			AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, restart_intent);
			System.exit(code);
		}
	}

}
