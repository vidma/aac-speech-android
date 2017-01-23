package com.space.aac;

import android.annotation.TargetApi;
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
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.space.aac.data.DBHelper;
import com.space.aac.data.Pictogram;
import com.space.aac.data.PictogramFactory;
import com.space.aac.data.ZippedDatafilesHelper;
import com.space.aac.nlg.Pic2NLG;
import com.space.aac.nlg.Pic2NLG.ActionType;
import com.space.aac.ui.DynamicHorizontalScrollView;
import com.space.aac.ui.FlipLayout;
import com.space.aac.ui.PictogramCursorAdapter;
import com.space.aac.ui.PictsAdapterSectionIndexed;
import com.space.aac.ui.ScalingLinearLayout;
import com.space.aac.ui.UIFactory;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, OnClickListener, Thread.UncaughtExceptionHandler, SearchView.OnQueryTextListener {


        protected static final String TAG = "AAC";
        private static final boolean RESTART_ON_EXCEPTION = false;
        ViewFlipper view_flipper;
        DBHelper dbHelper = null;
        Cursor icons_cursor;
        GridView gv;
        String newText;
        PictogramCursorAdapter adapter;
        private LayoutInflater inflater;
        TextView wordsToSpeak = null;
        ImageView speak = null;
        public int category_id;
        // DBHelper dbHelper = null;

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
        private boolean pref_read_each_word;

        public static final boolean DEBUG = false;
        // our GUI
        protected int currentCategoryId = 0;
        static final int PROGRESS_DIALOG = 0;
        ProgressDialog progressDialog;

    /* How do we filter the contents of the current category
     * to change, user has to choose an appropriate tab... */
        protected String catTabFilter = "all";


    /* Screens */
        protected static final int FLIPPER_VIEW_HOME = 0;
        protected static final int FLIPPER_VIEW_CATEGORY_LISTING = 1;
        protected static final int FLIPPER_VIEW_LISTVIEW_SEARCH = 2;


        protected static final String SAVED_INST_PHRASE_KEY = "phrase_list";

        public static Pic2NLG nlgConverter = null;
        protected UIFactory uiFactory = null;
        PictogramFactory pictogramFactory = null;
        Resources res;
        public static ArrayList<Pictogram> phrase_list = new ArrayList<Pictogram>();
        public static boolean isTablet = false;
        public static Boolean nlg_state_subject_selected = false;
        private String nlg_text;
        private ProgressDialog nlg_wait;
        private PendingIntent restart_intent;
        TTSButtonActivity tts;
        private TextToSpeech ttsNew;
        private ImageButton btnNew;


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

    private void getPreferences() {
        // Get the xml/preferences.xml preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        pref_uppercase = prefs.getBoolean("pref_uppercase", PREF_UPERCASE_DEFAULT);
        pref_clear_phrase_after_speak = prefs.getBoolean("pref_clear_phrase_after_speak",
                PREF_CLEAR_PHRASE_AFTER_SPEAK_DEFAULT);
        pref_gender = prefs.getString("pref_gender", PREF_GENDER_DEFAULT);
        pref_hide_spc_color = prefs.getBoolean("pref_hide_spc_color", PREF_HIDE_SPC_DEFAULT);
        pref_hide_offensive = prefs.getBoolean("pref_hide_offensive", true);
        pref_read_each_word = prefs.getBoolean("pref_readword", true);
    }

    public static boolean getPrefHideSPCColor() {
        // return MainActivity.pref_hide_spc_color;
        return Boolean.parseBoolean(null);
    }

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

    @NonNull
    private void loadNLG(Bundle savedInstanceState) {
        nlg_wait = ProgressDialog.show(MainActivity.this, "", res.getString(R.string.loading_nlg),
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
                if (nlg_wait != null) {
                    nlg_wait.dismiss();
                }
            }
        };

        Thread init_nlg = new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "creating NLG for lang=" + TTSButtonActivity.getPreferedLanguage());

                MainActivity.nlgConverter = new Pic2NLG(TTSButtonActivity.getPreferedLanguage());
                Message msg = handler.obtainMessage();
                // msg.arg1 = total;
                handler.sendMessage(msg);
            }
        };

        init_nlg.start();

    }

    /**
     *
     */
    @NonNull
    private void onNLGload_initGUI() {
        /*
         * create the image buttons: with current implementation UI buttons can
		 * not be created before NLG is loaded
		 */
        pictogramFactory = new PictogramFactory(dbHelper, res);
        Pictogram.pref_my_gender = pref_gender;

        OnClickListener items_onclick_listener = new OnClickListener() {


            @Override
            public void onClick(View view) {
                //ImageButton img = (ImageButton) v.findViewById(R.id.icons_imgButton);
                //System.out.println("TAG:" + v.getTag(R.id.TAG_PICTOGRAM));
                Pictogram currentButton = (Pictogram) view.getTag(R.id.TAG_PICTOGRAM);
                if (currentButton != null && currentButton.type != ActionType.EMPTY) {
                    if (currentButton.type == ActionType.CATEGORY) {
                        try {
                            int catId = Integer.parseInt(currentButton.data);
                            showCategory(catId, newText);
                            String catTitle = dbHelper.getCategoryTitle(catId);
                            // tts.speakOneWord(catTitle);
                            //  tts.speakOneWord(getString(R.string.folder_caption_onclick) + ":" + catTitle);
                        } catch (NumberFormatException ignored) {
                        } catch (NullPointerException npex) {
                            Toast.makeText(MainActivity.this, "nullpointer exception....", Toast.LENGTH_SHORT).show();
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

    /**
     * Draw the list of currently selected icons (displayed on top of screen)
     */

    private void drawCurrentIcons() {
        // TODO: move out to UI factory?
        LinearLayout icon_list = (LinearLayout) findViewById(R.id.icon_history_layout);
        DynamicHorizontalScrollView scroller = (DynamicHorizontalScrollView) findViewById(R.id.icon_history_scrollview);
        /*
         * as we are about to change the listing of icons i
         * n case of
		 * "intelligent guesses", we have to rebuild the whole listing
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


    protected void restoreInstanceState(Bundle savedInstState) {
        Log.d(TAG, "restoreInstanceState from bundle=" + savedInstState + " picFactory=" + pictogramFactory
                + " nlgConv=" + nlgConverter);
        if (savedInstState != null && savedInstState.containsKey(SAVED_INST_PHRASE_KEY) && pictogramFactory != null) {
            phrase_list = pictogramFactory.createFromSerialized(savedInstState.getString(SAVED_INST_PHRASE_KEY));
            Log.d(TAG, "restoredInstanceState from: " + savedInstState.getString(SAVED_INST_PHRASE_KEY));
            updatePhraseDisplay();
        }
    }

    protected void showCategory(int category_id, String newText) {
        // Prepare grid's data-source

        Cursor icons_cursor = dbHelper.getIconsCursorByCategory(category_id, newText, catTabFilter);

        String[] map_from = new String[]{"icon_path", "word"};
        int[] map_to = new int[]{R.id.search_list_entry_icon, R.id.search_list_entry_icon_text};

        PictogramCursorAdapter adapter = new PictsAdapterSectionIndexed(getApplicationContext(),
                R.layout.gridview_icon_entry, icons_cursor, map_from, map_to, pref_uppercase);

        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                // TODO: add other filters, e.g. tabs: recent, important, etc
                return dbHelper.getIconsCursorByCategory(currentCategoryId, (String) constraint, catTabFilter);
            }
        });
        // Draw the grid
        gv = (GridView) findViewById(R.id.category_gridView);
        gv.setFastScrollEnabled(false);
        gv.setAdapter(adapter);
        gv.setFastScrollEnabled(true);
        gridViewDecorate();


        gv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Pictogram selected_icon = dbHelper.getIconById(id);
                addWord(selected_icon);
                returnToMainScreen();
            }
        });

        currentCategoryId = category_id;

        // switch to the Category view
        switchFlipperScreenTo(FLIPPER_VIEW_CATEGORY_LISTING);
    }

    protected void gridViewDecorate() {
        int apiVer = android.os.Build.VERSION.SDK_INT;
        if (apiVer >= 11) {
            decorateGridViewApi11();
        } else {
            decorateGridViewApiOld();
        }

    }

    @TargetApi(11)
    protected void decorateGridViewApi11() {
        GridView gv = (GridView) findViewById(R.id.category_gridView);
        gv.setFastScrollAlwaysVisible(true);
    }

    protected void decorateGridViewApiOld() {
    }

    /**
     * @param view
     */
    /////////////////actual position of switchFlipperScreenTo() method
    public void switchFlipperScreenTo(int view) {
		/* Stay in the same screen if such preference is set */
        if (view == FLIPPER_VIEW_HOME && !pref_switch_back_to_main_screen)
            return;

        ViewFlipper switcher = (ViewFlipper) findViewById(R.id.view_switcher);
        switcher.setDisplayedChild(view);
    }


    final void addWord(final Pictogram currentButton) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(100);
        phrase_list.add(currentButton);
        Log.d("addWord", currentButton.data + " " + currentButton.type + " " + currentButton.element);
        updatePhraseDisplay();

        if (pref_read_each_word) {
            //  tts.speakOneWord(currentButton.display_text);
        }

    }

    /**
     * Choses between two different layouts, one for tablet other for mobile
     * <p/>
     * we use the "super horizontal scroller" only for mobiles, not for tablets
     */
    private void createImageButtons(boolean update) {
        // TODO: image buttons could be reused afterwards, only changing the text/action...
        Log.d(TAG, "createImageButtons:start");
        uiFactory.nlg_state_subject_selected = nlg_state_subject_selected;

        boolean is_tablet = isTablet();
        ViewGroup home_screen_layout = (LinearLayout) findViewById(R.id.home_screen);
        ViewGroup tl_container = home_screen_layout;

        if (!update) {
            home_screen_layout.removeAllViews();
        }
        FlipLayout view_flipper = null;
        if (!is_tablet) {
            if (!update) {
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

        tl1.computeScroll();
        tl2.computeScroll();

        if (update)
            return;

        // these operations create the views, and are run only the first time
        if (is_tablet) {
            // shall have tablet, so we could fit everything into one page
            home_screen_layout.addView(tl1);
            home_screen_layout.addView(tl2);

            // in landscape mode we show both screens side-by-side
            if (home_screen_layout instanceof ScalingLinearLayout) {
                // tl1.setColumnStretchable();
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
            view_flipper.addAndResizeItems(new View[]{tl1, tl2});
            home_screen_layout.addView(view_flipper);
        }

    }

    /**
     * Updates current phrase displayed once it has changed
     * <p/>
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int layout = getResources().getConfiguration().screenLayout;
        try {
            if ((layout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

        /*if ((layout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        }
*/
        } catch (NullPointerException e) {
            return;
        }


        ttsNew = new TextToSpeech(this, this);
        btnNew = (ImageButton) findViewById(R.id.speak);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //ZippedDatafilesHelper.ensureReadiness(this);

        // initialize activity level variables
        getPreferences();
        dbHelper = new DBHelper(getContentResolver(), this.pref_hide_offensive);
        icons_cursor = dbHelper.getIconsCursorByCategory(category_id, null, catTabFilter);
        String[] map_from = new String[]{"icon_path", "word"};
        int[] map_to = new int[]{R.id.search_list_entry_icon, R.id.search_list_entry_icon_text};
        adapter = new PictsAdapterSectionIndexed(getApplicationContext(),
                R.layout.gridview_icon_entry, icons_cursor, map_from, map_to, pref_uppercase);

        gv = (GridView) findViewById(R.id.category_gridView);
        gv.setFastScrollEnabled(false);
        gv.setAdapter(adapter);
        gv.setFastScrollEnabled(true);
        gridViewDecorate();


        inflater = getLayoutInflater();
        res = getResources();
        wordsToSpeak = (TextView) findViewById(R.id.wordsToSpeak);
        tts = new TTSButtonActivity();

        // category view - go back button
      /*  ImageButton category_back = (ImageButton) findViewById(R.id.category_go_back);
        ImageButton search_back = (ImageButton) findViewById(R.id.listview_search_go_back);
        OnClickListener back_to_home_handler = new OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToMainScreen();
            }
        };
        category_back.setOnClickListener(back_to_home_handler);
        search_back.setOnClickListener(back_to_home_handler);*/

        // ensure the data files are available (unzipped)
        // TOOD: context
        ZippedDatafilesHelper.ensureReadiness(this);


        this.isTablet = isTablet();
        if (!isTablet()) {
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
// force portrait orientation for non-tablets
            // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
// TODO at the moment the portrait don't look good on some tablets...
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // force landscape on tablet for now
        }

        int r = getIntent().getFlags();
        Intent in = new Intent(getIntent());
        restart_intent = PendingIntent.getActivity(getBaseContext(), 0, in, r);


        if (MainActivity.RESTART_ON_EXCEPTION) {
            Thread.setDefaultUncaughtExceptionHandler(this);
        }

        // once everything is loaded, enable speaking button
        speak = (ImageButton) findViewById(R.id.speak);

        // initialise NLG and Application
        loadNLG(savedInstanceState);
        // tts.ui_enable_tts(speak);
        Log.d(TAG, "on create end");


        btnNew.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                speakOut();
            }
        });


//      search view

        onPause();
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_INST_PHRASE_KEY) && pictogramFactory != null) {
            phrase_list = pictogramFactory.createFromSerialized(savedInstanceState.getString(SAVED_INST_PHRASE_KEY));
            Log.d(TAG, "restoredInstanceState from: " + savedInstanceState.getString(SAVED_INST_PHRASE_KEY));
            updatePhraseDisplay();
        }
    }


    @Override
    public void onBackPressed() {
        if (!returnToMainScreen()) {
            super.onBackPressed();
        }
    }


    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            int result = ttsNew.setLanguage(Locale.ENGLISH);
            if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                Log.e("TTS", "this lang is not supported");

            } else {
                btnNew.setEnabled(true);
                speakOut();

            }

        } else {
            Log.e("TTS", "tts initialiazation failed");
        }


    }

    private void speakOut() {

        String text = wordsToSpeak.getText().toString();
        ttsNew.speak(text, TextToSpeech.QUEUE_FLUSH, null);

    }


    @Override
    public void onPause() {
        super.onPause();

        if ((nlg_wait != null) && nlg_wait.isShowing())
            nlg_wait.dismiss();
        nlg_wait = null;
    }


  /*  public void onDestroy() {
        super.onDestroy();
        this.dbHelper = null;
        this.pictogramFactory = null;
        this.uiFactory = null;
        this.inflater = null;
        this.res = null;

        MainActivity.nlgConverter = null;
    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_search:

                break;
            // action with ID action_settings was selected
            case R.id.menu_about:

                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.donate:
                Toast.makeText(this, "Donate selected", Toast.LENGTH_SHORT)
                        .show();
                break;
            // action with ID action_settings was selected
            case R.id.menu_preferences:
                Intent i = new Intent(this, preferences.class);
                startActivity(i);
                break;
            case R.id.action_settings:
                intent = new Intent();
                intent.setAction("com.android.settings.TTS_SETTINGS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);

            default:
                break;
        }
        return true;
    }

    public boolean returnToMainScreen() {
        if (currentCategoryId != 0) {
            currentCategoryId = 0;
        }

        //* allow default implementation of back button *//
        ViewFlipper switcher = (ViewFlipper) findViewById(R.id.view_switcher);
        if (switcher.getDisplayedChild() == MainActivity.FLIPPER_VIEW_HOME)
            return false;

        switchFlipperScreenTo(MainActivity.FLIPPER_VIEW_HOME);
        return true;
    }


    ////// copy of view flipper


    ///////newly added fun
    @Override
    public void onClick(View view) {

        // int name = Integer.parseInt(view.getTag().toString());
        //switchFlipperScreenTo(name);


        Pictogram currentButton = (Pictogram) view.getTag(R.id.TAG_PICTOGRAM);
        if (currentButton != null) {
            if (currentButton.type == ActionType.CATEGORY) {
                try {
                    int catId = Integer.parseInt(currentButton.data);
                    showCategory(catId, newText);
                    String catTitle = dbHelper.getCategoryTitle(catId);
                    //  tts.speakOneWord(catTitle);
                    //  tts.speakOneWord(getString(R.string.folder_caption_onclick) + ":" + catTitle);
                } catch (NumberFormatException ignored) {
                } catch (NullPointerException npex) {
                    Toast.makeText(MainActivity.this, "nullpointer exception....", Toast.LENGTH_SHORT).show();
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


    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e(TAG, "uncaughtException" + ex.toString(), ex);
        ex.printStackTrace();
        restartActivity(2);

    }


    protected void restartActivity(int code) {
        if (restart_intent != null) {
            AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, restart_intent);
            System.exit(code);
        }

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        showCategory(category_id, query);


        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        showCategory(category_id, newText);

        return true;
    }

}




