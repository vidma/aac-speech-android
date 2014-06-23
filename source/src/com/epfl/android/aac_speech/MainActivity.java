package com.epfl.android.aac_speech;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.epfl.android.aac_speech.data.DBHelper;
import com.epfl.android.aac_speech.data.Pictogram;
import com.epfl.android.aac_speech.data.PictogramFactory;
import com.epfl.android.aac_speech.nlg.Pic2NLG;
import com.epfl.android.aac_speech.nlg.Pic2NLG.ActionType;
import com.epfl.android.aac_speech.ui.DynamicHorizontalScrollView;
import com.epfl.android.aac_speech.ui.FlipLayout;
import com.epfl.android.aac_speech.ui.PictogramCursorAdapter;
import com.epfl.android.aac_speech.ui.ScalingLinearLayout;
import com.epfl.android.aac_speech.ui.UIFactory;

public class MainActivity extends TTSButtonActivity implements UncaughtExceptionHandler {
	protected static final String TAG = "AAC";

	private static  final boolean RESTART_ON_EXCEPTION = true;
	public static final boolean DEBUG = false;
	public static final String APP_CONTENT_FILE_DOWN_URL = "https://github.com/vidma/aac-speech-android/releases/download/v1.2beta/aac_speech_data.zip";
	
	// initialized in onCreate	
	DBHelper dbHelper = null;
	PictogramFactory pictogramFactory = null;
	private UIFactory uiFactory = null;
	public static Pic2NLG nlgConverter = null;
	private LayoutInflater inflater;
	Resources res;
	public static ArrayList<Pictogram> phrase_list = new ArrayList<Pictogram>();
	public static boolean isTablet = false;
	public static Boolean nlg_state_subject_selected = false;
	private String nlg_text;
	private PendingIntent restart_intent;
	TextView wordsToSpeak = null;

	
	// our GUI
	protected int currentCategoryId = 0;
	static final int PROGRESS_DIALOG = 0;
	ProgressDialog progressDialog;

	// default Preferences TODO: clean this up
	private final static boolean PREF_UPERCASE_DEFAULT = false;
	private final static boolean PREF_HIDE_SPC_DEFAULT = false;
	private final boolean PREF_CLEAR_PHRASE_AFTER_SPEAK_DEFAULT = false;
	private final String PREF_GENDER_DEFAULT = "MALE";

	protected static boolean pref_uppercase = PREF_UPERCASE_DEFAULT;
	private static boolean pref_hide_spc_color = PREF_HIDE_SPC_DEFAULT;

	private String pref_gender = PREF_GENDER_DEFAULT;
	private boolean pref_hide_offensive = true;
	private boolean pref_clear_phrase_after_speak = PREF_CLEAR_PHRASE_AFTER_SPEAK_DEFAULT;

	private boolean pref_switch_back_to_main_screen = true;


	/* Screens */
	protected static final int FLIPPER_VIEW_HOME = 0;
	protected static final int FLIPPER_VIEW_CATEGORY_LISTING = 1;
	protected static final int FLIPPER_VIEW_LISTVIEW_SEARCH = 2;


	protected static final String SAVED_INST_PHRASE_KEY = "phrase_list";
	


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
	final void updatePhraseDisplay() {
		nlg_text = "";
		if (phrase_list.size() > 0)
			nlg_text = nlgConverter.convertPhrasesToNLG(phrase_list);

		// first perform the faster tasks
		wordsToSpeak.setText(getText(nlg_text));
		drawCurrentIcons();
		
		Boolean is_subject_selected = nlgConverter.hasSubjectBeenSelected(phrase_list);
		if (is_subject_selected != nlg_state_subject_selected) {
			nlg_state_subject_selected = is_subject_selected;
			createImageButtons(true);
		}
	}

	final void addWord(final Pictogram currentButton) {
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(100);
		phrase_list.add(currentButton);
		//Log.d("addWord", currentButton.data + " " + currentButton.type + " " + currentButton.element);
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
	private void createImageButtons(boolean update) {
		// TODO: image buttons could be reused afterwards, only changing the text/action...
		Log.d(TAG, "createImageButtons:start");
		uiFactory.nlg_state_subject_selected = nlg_state_subject_selected;

		boolean is_tablet = isTablet();		
		ViewGroup home_screen_layout = (LinearLayout) findViewById(R.id.home_screen);
		ViewGroup tl_container = home_screen_layout;
		
		if (!update){
			home_screen_layout.removeAllViews();
		}
		FlipLayout view_flipper = null;
		if (!is_tablet){
			if (!update){
				view_flipper = (FlipLayout) inflater.inflate(R.layout.flip_layout, home_screen_layout, false);
				view_flipper.init();
			} else {
				view_flipper = (FlipLayout) home_screen_layout.findViewById(R.id.home_flipper);
			}
			tl_container = view_flipper.internalWrapper;
		}
		
		TableLayout tl1, tl2;
		tl1 = uiFactory.createHomePictogramTable(tl_container, update);
		tl2 = uiFactory.createImageButtonsCategoriesRight(tl_container, update);
		
		if (update)
			return;
		
		// these operations create the views, and are run only the first time
		if (is_tablet) {
			// shall have tablet, so we could fit everything into one page
			home_screen_layout.addView(tl1);
			home_screen_layout.addView(tl2);

			// in landscape mode we show both screens side-by-side
			if (home_screen_layout instanceof ScalingLinearLayout) {
				tl2.setPadding(20, 0, 0, 0); // add spacing between main and
												// secondary column
				ScalingLinearLayout l = (ScalingLinearLayout) home_screen_layout;
				l.cleanup(); // we're reusing old element right now ...
				l.invalidate();
				l.requestLayout(); // refresh view and recalculate the size
			}
		} else {
			// on a smaller device, we use a scroller to switch between the two views
			// TODO: Android standard tools might be also good or even better
			view_flipper.addAndResizeItems(new View[] { tl1, tl2 });
			home_screen_layout.addView(view_flipper);
		}

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

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.dbHelper = null;
		this.pictogramFactory = null;
		this.uiFactory = null;
		this.inflater = null;
		this.res = null;
		this.wordsToSpeak = null;
		
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
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // force landscape on tablet for now
		}

		setContentView(R.layout.main);
		//
		int size = getResources().getConfiguration().screenLayout;
		Log.d(TAG, "Layout=" + size + 
			  " xlarge=" +((size & Configuration.SCREENLAYOUT_SIZE_XLARGE)==Configuration.SCREENLAYOUT_SIZE_XLARGE));


		// In production, override any unexpected (non-handled) exceptions with restart of application		
		restart_intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(getIntent()), getIntent().getFlags());
		if (MainActivity.RESTART_ON_EXCEPTION) {
			Thread.setDefaultUncaughtExceptionHandler(this);
		}

		// initialize activity level variables
		getPreferences();
		dbHelper = new DBHelper(getContentResolver(), this.pref_hide_offensive);
		inflater = getLayoutInflater();
		res = getResources();
		wordsToSpeak = (TextView) findViewById(R.id.wordsToSpeak);

		// category view - go back button
		ImageButton category_back = (ImageButton) findViewById(R.id.category_go_back);
		ImageButton search_back = (ImageButton) findViewById(R.id.listview_search_go_back);
		OnClickListener back_to_home_handler = new OnClickListener() {
			@Override
			public void onClick(View v) {
				returnToMainScreen();
			}
		};
		category_back.setOnClickListener(back_to_home_handler);
		search_back.setOnClickListener(back_to_home_handler);
		
		// initialise NLG and Application
		loadNLG(savedInstanceState);

		// once everything is loaded, enable speaking button
		ui_enable_tts();
		Log.d(TAG, "on create end");
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
	protected void switchFlipperScreenTo(int view) {
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
		pictogramFactory = new PictogramFactory(dbHelper, res);
		Pictogram.pref_my_gender = pref_gender;
		
		OnClickListener items_onclick_listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				//ImageButton img = (ImageButton) v.findViewById(R.id.icons_imgButton);
				//System.out.println("TAG:" + v.getTag(R.id.TAG_PICTOGRAM));
				Pictogram currentButton = (Pictogram) v.getTag(R.id.TAG_PICTOGRAM);
				if (currentButton != null) {
					if (currentButton.type == ActionType.CATEGORY) {
						try {
							int catId = Integer.parseInt(currentButton.data);
							showCategory(catId);
						} catch (NumberFormatException ignored) {
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
		createImageButtons(false);
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
