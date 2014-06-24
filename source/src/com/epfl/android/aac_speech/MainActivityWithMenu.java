package com.epfl.android.aac_speech;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ViewFlipper;

import com.epfl.android.aac_speech.cont_providers.IconsProvider;
import com.epfl.android.aac_speech.data.Pictogram;
import com.epfl.android.aac_speech.data.models.PhraseHistory;
import com.epfl.android.aac_speech.ui.PictogramCursorAdapter;

/**
 * This file adds a Menu to the Main Activity, and the related functions which
 * are accessed only through the menu.
 * 
 * TODO: Usage of scala like mixins would be nicier, but for now we have to live
 * with such flat inheritance.
 *   
 * @author vidma
 *
 */
public class MainActivityWithMenu extends MainActivity {
	protected  static final String TAG = "AACWithMenu";

	/* Menu */
	private static final int MENU_GESTURE_SEARCH_ID = 1;
	private static final int MENU_ABOUT = 3;
	private static final int MENU_PREFS = 4;
	private static final int MENU_HISTORY = 5;

	/*
	 * Interaction with Gesture Search
	 * 
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

	private void gestureSearch() {
		try {
			Intent intent = new Intent();
			intent.setAction("com.google.android.apps.gesturesearch.SEARCH");

			// TODO: optionally pass part of speech or category parameter (as URI)
			Uri content_uri = IconsProvider.GESTURE_SEARCH_CONTENT_URI;
			if (currentCategoryId != 0) {
				content_uri = ContentUris.withAppendedId(IconsProvider.GESTURE_SEARCH_BY_CATEGORY_CONTENT_URI,
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
		gestureSearch();
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
			gestureSearch();
			return true;

		case MENU_ABOUT:
			Intent intent = new Intent(MainActivityWithMenu.this, AboutActivity.class);
			startActivity(intent);
			return true;

		case MENU_PREFS:
			Intent intent1 = new Intent(MainActivityWithMenu.this, PreferencesActivity.class);
			startActivityForResult(intent1, MENU_PREFS);
			return true;

		case MENU_HISTORY:
			showHistory();
			return true;

		}
		return super.onOptionsItemSelected(item);

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
					switchFlipperScreenTo(MainActivityWithMenu.FLIPPER_VIEW_HOME);
				}

			}
			break;

		case MENU_PREFS:
			// TODO: clean
			// if new data was installed then we restart this activity
			//if (resultCode == PreferencesActivity.RESULT_DATA_UPDATED) {
			//	restartActivity(0);
			//}
			break;
		}

	}
	
	
	//TODO
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



	private void performListViewSearch() {
		// TODO: this listview search is currently crap because of issues with Android Keyboard
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


	

}
