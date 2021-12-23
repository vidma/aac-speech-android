package com.epfl.android.aac_speech;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;

import com.epfl.android.aac_speech.cont_providers.IconsProvider;
import com.epfl.android.aac_speech.data.Pictogram;
import com.epfl.android.aac_speech.data.models.PhraseHistory;
import com.epfl.android.aac_speech.ui.PictogramCursorAdapter;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnCloseListener;

/**
 * This file adds a Menu to the Main Activity, and the related functions which
 * are accessed only through the menu.
 * 
 * TODO: Usage of scala like mixins would be nicier, but for now we have to live
 * with such flat inheritance.
 * 
 * Search functionality based on: 
 * [1] https://github.com/JakeWharton/ActionBarSherlock/blob/4.3.1/actionbarsherlock-samples/demos/src/com/actionbarsherlock/sample/demos/SearchViews.java
 * [2] http://www.coderzheaven.com/2013/06/01/create-searchview-filter-mode-listview-android/
 *   
 * @author vidma
 *
 */
public class MainActivityWithMenu extends MainActivity 
implements
    SearchView.OnQueryTextListener, 
	OnCloseListener,
	OnActionExpandListener, 
	ActionBar.TabListener {
	
	protected  static final String TAG = "AACWithMenu";
	
	/* Menu */
	private static final int MENU_GESTURE_SEARCH_ID = 1;
	private static final int MENU_ABOUT = 3;
	private static final int MENU_PREFS = 4;
	private static final int MENU_HISTORY = 5;
	private static final int MENU_DONATE = 6;
	

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
	
	
	/** Search View */
	private SearchView searchView; // TODO: robojuice
    private GridView grid_view; // TODO: robojuice

    /** MenuItems */
	private MenuItem menuSearch;
	private MenuItem menuInfo;
	private MenuItem menuDonate;
	private MenuItem menuPrefs; 
    
	
	
	/*** -------- TABS -----/
	 * 
	 */
	private void addCategoryTab(String key, int icon, int text){
		ActionBar.Tab tab = getSupportActionBar().newTab();
	    tab.setText(getString(text));
	    tab.setTag(key);
	    tab.setTabListener(this);
	    tab.setIcon(icon);
	    getSupportActionBar().addTab(tab);
	}
	
	private void addCategoryTabs(){
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		getSupportActionBar().removeAllTabs(); 
		addCategoryTab("all", R.drawable.infinity, R.string.cat_tab_all);
		addCategoryTab("recent", android.R.drawable.ic_menu_recent_history, R.string.cat_tab_recent);		
	}
	
	private void hideCategoryTabs(){
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		// select the default tab!
		catTabFilter = "all";
	}
	
	private void showCategoryTabs(){
		addCategoryTabs();
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

	}	
	
	@Override
    public void onTabReselected(Tab tab, FragmentTransaction transaction) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction transaction) {
        //mSelected.setText("Selected: " + tab.getText());
    	// TODO: update the filter!!!
    	String tabGroup = (String) tab.getTag();
    	this.catTabFilter = tabGroup;
    	// the simplest is to redraw the category
    	super.showCategory(currentCategoryId);
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction transaction) {
    }	
	
	// end of tabs
	
	
	private void addSearchAdapter(){
		//Create the search view
        searchView = new SearchView(getSupportActionBar().getThemedContext());
        searchView.setQueryHint("Search for icons…");
        searchView.setOnQueryTextListener(this);
		searchView.setSubmitButtonEnabled(false);
		searchView.setOnCloseListener(this);
		
		grid_view = (GridView) findViewById(R.id.category_gridView);
		grid_view.setTextFilterEnabled(true);
		// TODO: init searchview
	}
	

	@Override
	public boolean onClose() {
		Log.d(TAG, "Search:onClose");
		grid_view.setTextFilterEnabled(false);
		return true;
	}

	void toggleMenuIcons(Boolean active){
		MenuItem[] icons = new MenuItem[] { 
				menuDonate, menuInfo, menuPrefs };
		
		for (MenuItem icon: icons) {
			icon.setVisible(active);
		}
	}
	
	
	@Override	
	protected void showCategory(int category_id) {
		Log.d(TAG, "showCategory: cat="+ this.currentCategoryId);
		super.showCategory(category_id);		

		grid_view.setTextFilterEnabled(true);
		
		// cleanup the action bar
		ActionBar bar = getSupportActionBar();
		bar.setDisplayShowHomeEnabled(true);
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setDisplayShowTitleEnabled(true);
		
		// set the title, if any
		if (category_id != 0) {
			bar.setTitle(dbHelper.getCategoryTitle(category_id));
			bar.setIcon(uiFactory.getCategoryButtonDrawableId(category_id));
			
		}
		
		// show tabs
		showCategoryTabs();
		toggleMenuIcons(false);
	}
	
	@Override
	public boolean returnToMainScreen() {
		Log.d(TAG, "returnToMain in search");
		boolean r = super.returnToMainScreen();
		hideActionbarTitle();
		
		// hide search if any (e.g. after selecting icon while in search)
		menuSearch.collapseActionView();
    	searchView.setQuery("", false); // reset the query
    	searchView.clearFocus();
		Log.d(TAG, "returnToMain in search: collapsed");
		
		// hide category tabs, if any
		hideCategoryTabs();
		toggleMenuIcons(true);

		return r;
	}


	private void hideActionbarTitle() {
		ActionBar bar = getSupportActionBar();
		bar.setDisplayHomeAsUpEnabled(false);
		bar.setDisplayShowTitleEnabled(false);
		bar.setTitle("");
		bar.setIcon(R.drawable.aac_icon);
		bar.setDisplayShowHomeEnabled(false);
	}
	

	@Override
	public boolean onQueryTextSubmit(String query) {
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		Log.d(TAG, "onQueryTextChange: cat="+ this.currentCategoryId);	
		
		if (TextUtils.isEmpty(newText)) {
            grid_view.clearTextFilter();
        } else {
            grid_view.setFilterText(newText.toString());
        }
        return true;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		hideActionbarTitle();
		addSearchAdapter();
	}
	

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        // Do something when collapsed
        return true;  // Return true to collapse action view
    }
    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
    	Log.d(TAG, "onSearchExpanced. cat=" + currentCategoryId);
		// make sure the results are visible (as it could be called from home)
		ViewFlipper switcher = (ViewFlipper) findViewById(R.id.view_switcher);
		if (currentCategoryId == 0 && switcher.getDisplayedChild() != FLIPPER_VIEW_CATEGORY_LISTING) {
			switchFlipperScreenTo(FLIPPER_VIEW_CATEGORY_LISTING);
			showCategory(currentCategoryId);
		}
        // Do something when expanded
        return true;  // Return true to expand action view
    }
    	
	
	// -- end of keyboard based search (also see onCreateMenu callback)
	

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
			intent.putExtra(SHOW_MODE, SHOW_ALL);
			intent.putExtra(THEME, THEME_DARK);
			startActivityForResult(intent, MENU_GESTURE_SEARCH_ID);
		} catch (ActivityNotFoundException e) {
			Log.e("GestureSearchExample", "Gesture Search is not installed", e);
			// e.printStackTrace();
			Log.i("ListSearch", "Falling back to homemade listview search...");
			// Falling back to homemade keyboard search!!!
			menuSearch.expandActionView();
		}
	}

	@Override
	public boolean onSearchRequested() {
		// TODO: gesture search vs Keyboard
		Log.e("act", "Search Requested");
		gestureSearch();
		return true;
	}

	// END OF GESTURE SEARCH STUFF

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO: gesture search vs keyboard search...
		// TODO: gesture search is not nicely integrated, and it do not work well with assetsProvider & targetsdk>whatever
		super.onCreateOptionsMenu(menu);
		
		//menu.add(0, MENU_GESTURE_SEARCH_ID, 0, R.string.menu_gesture_search)
		//.setIcon(android.R.drawable.ic_menu_search);
		
		// TODO: History is broken now...
		//menu.add(0, MENU_HISTORY, 0, R.string.menu_history)
		//.setIcon(android.R.drawable.ic_menu_recent_history);
		//menu.add(0, MENU_ABOUT, 0, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);
		
		menuSearch = menu.add(R.string.menu_search)
        .setIcon(android.R.drawable.ic_menu_search) //TODO:R.drawable.abs__ic_search)
        .setActionView(searchView);
        menuSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        menuSearch.setOnActionExpandListener(this);
		
		menuInfo = menu.add(R.string.menu_about)
		.setIntent(new Intent(MainActivityWithMenu.this, AboutActivity.class))
        .setIcon(android.R.drawable.ic_menu_info_details);
        menuInfo.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		menuDonate = menu.add(0, MENU_DONATE, 0, R.string.donate) //R.string.menu_preferences
		.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("http://aacspeech.org/?donate#donate")))
		.setIcon(R.drawable.donate32);
		menuDonate.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
				
		menuPrefs = menu.add(0, MENU_PREFS, 0, R.string.menu_preferences)
		.setIcon(android.R.drawable.ic_menu_preferences);
		menuPrefs.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		// TODO: more button!
		// TODO: add icon, add favourite phrase...
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_GESTURE_SEARCH_ID:
			gestureSearch();
			return true;

		case MENU_PREFS:
			//ensure the actionbar is as expencted...
			Intent intent1 = new Intent(MainActivityWithMenu.this, PreferencesActivity.class);
			startActivityForResult(intent1, MENU_PREFS);
			return true;

		case MENU_HISTORY:
			showHistory();
			return true;

	    case android.R.id.home:
	        returnToMainScreen();
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
					returnToMainScreen();
				}

			}
			break;

		case MENU_PREFS:
			// TODO: clean
			// if new data was installed then we restart this activity
			//if (resultCode == PreferencesActivity.RESULT_DATA_UPDATED) {
			//	restartActivity(0);
			//}
			returnToMainScreen();
			break;
		}

	}
	
	
	/**
	 * @deprecated
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



}
