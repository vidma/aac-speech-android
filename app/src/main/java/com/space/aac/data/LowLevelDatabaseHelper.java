package com.space.aac.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Log;

import com.space.aac.data.models.Category;
import com.space.aac.data.models.Icon;
import com.space.aac.data.models.PhraseHistory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Locale;

/**
 * Created by space on 21/7/16.
 */
public class LowLevelDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "icons.db";
    /**
     * guid added in v5
     */
    private static final int DATABASE_VERSION = 9;
    private static final String TAG = "LowLevelDatabaseHelper";
    public static final String ICONS_DATAFILE = "icon_meanings.data";
    private static final String CATEGORIES_DATAFILE = "categories.data";
    public static final String ASSETS_DIR = "file:///android_asset/";
    private static final String ICONS_IN_ASSETS_DIR = ASSETS_DIR + "icons-data/";

    // TODO: is it OK storing a ref to context? at least non static. use Weakref?
    Context context;

    public LowLevelDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    /**
     * @param context
     */
    public static File getDatafilesStorageDirectory(Context context) {
        return context.getExternalFilesDir(null);
    }



    // TODO: not used?
    public static File getDataFile(Context context, String fileName) {
        File file = new File(getDatafilesStorageDirectory(context), fileName);
        return file;
    }

    public static InputStream openAssetStream(Context context, String uri) throws IOException {
        uri = uri.replace("file:///android_asset/", "");
        Uri tempuri = Uri.parse(uri);
        AssetManager am = context.getAssets();
        return am.open(uri);
    }


    class CsvDatafileReader {

        private BufferedReader br;
        private SimpleStringSplitter splitter;
        public boolean isValid = false;
        private InputStreamReader reader;
        public String storage_dir;

        public CsvDatafileReader(Context context, String fileName) {
            try {
                storage_dir = ICONS_IN_ASSETS_DIR;
                //LowLevelDatabaseHelper.getDatafilesStorageDirectory(context);
                Log.d("Phrase provider", "dir: " + storage_dir);

                InputStream istream = openAssetStream(context, storage_dir + fileName); // new FileInputStream(new File(storage_dir, fileName));
                reader = new InputStreamReader(istream, "UTF-8");
                br = new BufferedReader(reader, 8192);

                splitter = new TextUtils.SimpleStringSplitter('|');
                isValid = true;

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        Iterator<String> getNextLineItemsIterator() {
            if (!isValid)
                return null;
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line != "") {
                        splitter.setString(line);
                        Iterator<String> it = splitter.iterator();
                        return it;
                    }

                }
            } catch (IOException e) {
                Log.d(TAG, e.toString());
                return null;
            }
            return null;
        }

        void close() {
            try {
                br.close();
            } catch (IOException e) {
                Log.d(TAG, e.toString());
            }

            try {
                reader.close();
            } catch (IOException e) {
                Log.d(TAG, e.toString());
            }
        }

    };

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.setLocale(new Locale("fr", "FR"));
        }
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        createDatabase(db);
    }

    /**
     * @param db
     */
    @SuppressLint("SdCardPath")
    protected void createDatabase(SQLiteDatabase db) {
		/* TODO: create table categories */

        Log.d(TAG, "createDatabase starting");

        db.execSQL("CREATE TABLE icon_meanings"
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, word TEXT, word_ascii_only TEXT, part_of_speech TEXT, spc_color INT,"
                + " icon_path TEXT, lang TEXT,  main_category_id INT, " + Icon.COL_USE_COUNT + " INT,"
                + Icon.COL_OFFENSIVE + " INT, guid CHAR(36) );");
        db.execSQL("CREATE INDEX icon_meanings_main_category_idx ON " + Icon.TABLE + "(main_category_id);");
        db.execSQL("CREATE INDEX icon_meanings_lang_idx ON " + Icon.TABLE + "(lang);");
        db.execSQL("CREATE INDEX icon_meanings_count_idx ON " + Icon.TABLE + "(" + Icon.COL_USE_COUNT + ");");
        db.execSQL(String.format("CREATE INDEX icon_meanings_guid ON %s (guid);", Icon.TABLE));
        Log.d(TAG, Icon.TABLE + " OK");

        db.execSQL("CREATE TABLE " + PhraseHistory.TABLE
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, phrase TEXT, phrase_items TEXT, phrase_items_serialized TEXT, "
                + PhraseHistory.COL_DATETIME + " DATETIME, " + PhraseHistory.COL_LANGUAGE + " TEXT);");
        db.execSQL("CREATE INDEX phrasehistory_lang_idx ON " + PhraseHistory.TABLE + " ("
                + PhraseHistory.COL_LANGUAGE + ");");
        Log.d(TAG, PhraseHistory.TABLE + " OK");

        db.execSQL("CREATE TABLE phrase_lists"
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, phrase TEXT, phrase_items TEXT);");

        db.execSQL("CREATE TABLE " + Category.TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Category.COL_CATEGORY_ID + " INT, " + Category.COL_TITLE + " TEXT, " + Category.COL_TITLE_SHORT
                + " TEXT, " + Category.COL_ICON_PATH + " TEXT, " + Category.COL_LANGUAGE + " TEXT, "
                + Category.COL_ORDER + " INT );");

        db.execSQL("CREATE INDEX categories_catid_idx ON " + Category.TABLE + " (" + Category.COL_CATEGORY_ID 	+ ");");
        Log.d(TAG, Category.TABLE + " created OK");

        // SQL file is stored in "preferred" storage (sdcard, "internal sdcard") as assets are too small (limit of 1MB)

        // import icons
        CsvDatafileReader csvReader = new CsvDatafileReader(context, ICONS_DATAFILE);
        CharSequence iconsStorageDir;
        CharSequence my_path;
        Iterator<String> it;

		/*
		 * starting transaction explicitly shall improve the performance
		 * (otherwise each insert() call would create a separate transaction).
		 *
		 * TODO: use DatabaseUtils.InsertHelper
		 */
        db.beginTransaction();
        try {
            while ((it = csvReader.getNextLineItemsIterator()) != null) {
                ContentValues values = new ContentValues();
                /**
                 * The field order in CSV is: word, part_of_speech, spc_color,
                 * icon_path, lang, main_category_id, categories, is_offensive
                 *
                 */
                String word = it.next();
                values.put(Icon.COL_WORD, word);
				/*
				 * ascii representation to work-around google gesture search bug
				 * for now
				 */
                values.put(Icon.COL_WORD_ASCII, it.next());
                values.put(Icon.COL_PART_OF_SPEECH, it.next());
                values.put(Icon.COL_SPC_COLOR, it.next());

                String icon_path = it.next();
                /// newly added path
                my_path = Environment.getExternalStorageDirectory()+"/";
                Log.d("my_path"," my custom path is" + my_path);

                iconsStorageDir=my_path+"Android/data/com.space.aac/files/";


                //iconsStorageDir="/storage/emulated/0/Android/data/com.space.aac/files/";
                icon_path = icon_path.replace(ASSETS_DIR, iconsStorageDir);

                Log.d("IconP", "icon_path" + icon_path);

                values.put(Icon.COL_ICON_PATH, icon_path);

                values.put(Icon.COL_LANG, it.next());
                values.put(Icon.COL_MAIN_CATEGORY, it.next());
                // categories not used
                String categories = it.next();
                values.put(Icon.COL_OFFENSIVE, it.next());
                values.put(Icon.COL_USE_COUNT, 0);
                values.put(Icon.COL_GUID, it.next());

                long id = db.insert(Icon.TABLE, null, values);
                if (id % 1000 == 0) {
                    Log.d(TAG, "inserted icon with local id:" + id);
                }
            }

            db.setTransactionSuccessful();
        }

        catch (SQLException e) {
            Log.e(TAG, e.toString());
            Log.e(TAG, e.getStackTrace().toString());

        } finally {
            db.endTransaction();
        }
        csvReader.close();
        Log.d(TAG, Icon.TABLE + "loaded OK");

		/* import categories */
        csvReader = new CsvDatafileReader(context, CATEGORIES_DATAFILE);
        while ((it = csvReader.getNextLineItemsIterator()) != null) {
            ContentValues values = new ContentValues();

            String category_id = it.next();
            // TODO: add order
            String order = "0";
            String title_short = it.next(); // currently long title messes up the home screen
            String title_long = it.next();
            String icon_path = it.next();
            //icon_path = icon_path.replace(oldChar, newChar)
            String lang = it.next();

            values.put(Category.COL_CATEGORY_ID, category_id);
            values.put(Category.COL_ORDER, order);
            values.put(Category.COL_TITLE, title_long);
            values.put(Category.COL_TITLE_SHORT, title_short);
            // TODO: category iconpath is not used at the moment, as categories are not fully DB driven (partially predefined in code)...
            values.put(Category.COL_ICON_PATH, icon_path);
            values.put(Category.COL_LANGUAGE, lang);

            db.insert(Category.TABLE, null, values);
        }
        Log.d(TAG, Category.TABLE + "loaded OK");

    }

    public static void itemCallback() {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        dropTables(db);
        onCreate(db);
        // TODO: keep track of migration SQL between versions and preserve the history!!!
    }

    /**
     * @param db
     */
    public void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + Icon.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Category.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PhraseHistory.TABLE);
        db.execSQL("DROP TABLE IF EXISTS phrase_lists");
    }
}
