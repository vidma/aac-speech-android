package com.epfl.android.aac_speech.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.util.Iterator;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Log;

import com.epfl.android.aac_speech.data.PhraseProviderDB;
import com.epfl.android.aac_speech.data.models.Category;
import com.epfl.android.aac_speech.data.models.IndividualIcons;
import com.epfl.android.aac_speech.data.models.PhraseHistory;

/**
 * This class helps open, create, and upgrade the database file (low level
 * helper)
 */
public class LowLevelDatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "icons.db";
	private static final int DATABASE_VERSION = 2;

	private static final String TAG = "PhraseProviderDB: LowLevelDatabaseHelper";
	public static final String ICON_MEANINGS_DATAFILE = "icon_meanings.data";
	private static final String CATEGORIES_DATAFILE = "categories.data";

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

	public static File getDataFile(Context context, String fileName) {
		File file = new File(getDatafilesStorageDirectory(context), fileName);
		return file;
	}

	public static boolean checkDataFileExistance(Context context) {
		return getDataFile(context, ICON_MEANINGS_DATAFILE).exists()
				&& getDataFile(context, CATEGORIES_DATAFILE).exists();
	}

	class CsvDatafileReader {

		private BufferedReader br;
		private SimpleStringSplitter splitter;
		public boolean isValid = false;
		private InputStreamReader reader;
		public File storage_dir;

		public CsvDatafileReader(Context context, String fileName) {
			try {
				// reader = new InputStreamReader(, "UTF-8");

				storage_dir = LowLevelDatabaseHelper
						.getDatafilesStorageDirectory(context);
				Log.d("Phrase provider", "dir: " + storage_dir);

				/*
				 * reader = new InputStreamReader(new FileInputStream(
				 * "/sdcard/icon_meanings.data"), "UTF-8");
				 */
				reader = new InputStreamReader(new FileInputStream(new File(
						storage_dir, fileName)), "UTF-8");

				int BUFFER_SIZE = 8192;
				br = new BufferedReader(reader, BUFFER_SIZE);

				// TODO: do I get not complete lines?
				String line;
				splitter = new TextUtils.SimpleStringSplitter('|');
				isValid = true;

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// TODO: shall copy over the sqlite DB file instead?SQL seem to
			// be
			// more flexible in case of migration
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
						// TODO: call the appropriate callback
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
		// TODO Auto-generated method stub
		super.onOpen(db);
		if (!db.isReadOnly()) {
			db.setLocale(new Locale("fr", "FR"));
		}
	}

	/**
	 * TODO: just copy the database for now?
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		createDatabase(db);

	}

	/**
	 * @param db
	 */
	protected void createDatabase(SQLiteDatabase db) {
		/* TODO: create table categories */

		Log.d(TAG, "createDatabase starting");

		db.execSQL("CREATE TABLE icon_meanings"
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, word TEXT, word_ascii_only TEXT, part_of_speech TEXT, spc_color INT,"
				+ " icon_path TEXT, lang TEXT,  main_category_id INT, "
				+ IndividualIcons.COL_USE_COUNT + " INT);");

		db.execSQL("CREATE INDEX icon_meanings_main_category_idx ON "
				+ IndividualIcons.TABLE_NAME + "(main_category_id);");
		db.execSQL("CREATE INDEX icon_meanings_lang_idx ON "
				+ IndividualIcons.TABLE_NAME + "(lang);");
		db.execSQL("CREATE INDEX icon_meanings_count_idx ON "
				+ IndividualIcons.TABLE_NAME + "("
				+ IndividualIcons.COL_USE_COUNT + ");");
		Log.d(TAG, IndividualIcons.TABLE_NAME + " OK");

		db.execSQL("CREATE TABLE "
				+ PhraseHistory.TABLE_NAME
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, phrase TEXT, phrase_items TEXT, phrase_items_serialized TEXT, "
				+ PhraseHistory.COL_DATETIME + " DATETIME, "
				+ PhraseHistory.COL_LANGUAGE + " TEXT);");
		db.execSQL("CREATE INDEX phrasehistory_lang_idx ON "
				+ PhraseHistory.TABLE_NAME + " (" + PhraseHistory.COL_LANGUAGE
				+ ");");
		Log.d(TAG, PhraseHistory.TABLE_NAME + " OK");

		db.execSQL("CREATE TABLE phrase_lists"
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, phrase TEXT, phrase_items TEXT);");

		db.execSQL("CREATE TABLE " + Category.TABLE_NAME
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ Category.COL_CATEGORY_ID + " INT, " + Category.COL_TITLE
				+ " TEXT, " + Category.COL_TITLE_SHORT + " TEXT, "
				+ Category.COL_ICON_PATH + " TEXT, " + Category.COL_LANGUAGE
				+ " TEXT, " + Category.COL_ORDER + " INT );");

		db.execSQL("CREATE INDEX categories_catid_idx ON "
				+ Category.TABLE_NAME + " (" + Category.COL_CATEGORY_ID + ");");
		Log.d(TAG, Category.TABLE_NAME + " created OK");

		// we've put the SQL file to preferred storage place (sdcard,
		// tablets "internal sdcard") as assets are too small (limit of
		// 1MB)

		/* import icons */
		CsvDatafileReader csvReader = new CsvDatafileReader(context,
				ICON_MEANINGS_DATAFILE);
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
				 * icon_path, lang, main_category_id
				 * 
				 */
				String word = it.next();
				values.put(IndividualIcons.COL_WORD, word);
				/*
				 * ascii representation to work-around google gesture search bug
				 * for now
				 */
				values.put(IndividualIcons.COL_WORD_ASCI, it.next());
				values.put(IndividualIcons.COL_PART_OF_SPEECH, it.next());
				values.put(IndividualIcons.COL_SPC_COLOR, it.next());

				String icon_path = it.next();

				// TODO: gesture search seems to require absolute
				// paths... just a hack fix it for now
				icon_path = icon_path.replace("/sdcard",
						(CharSequence) csvReader.storage_dir.toString()); // .replace("file:///mnt/",
				// "file:///");
				values.put(IndividualIcons.COL_ICON_PATH, icon_path);

				values.put(IndividualIcons.COL_LANG, it.next());
				values.put(IndividualIcons.COL_MAIN_CATEGORY, it.next());
				values.put(IndividualIcons.COL_USE_COUNT, 0);

				long id = db.insert(IndividualIcons.TABLE_NAME, null, values);

				if (id % 1000 == 0)
					Log.d(TAG, "inserted icon with local id:" + id);

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

		Log.d(TAG, IndividualIcons.TABLE_NAME + "loaded OK");

		/* import categories */
		csvReader = new CsvDatafileReader(context, CATEGORIES_DATAFILE);
		while ((it = csvReader.getNextLineItemsIterator()) != null) {
			ContentValues values = new ContentValues();

			String category_id = it.next();
			// TODO: add order
			String order = "0";
			/*
			 * currently long title messes up the home screen
			 * 
			 * the TableLayout is real crap!
			 */
			String title_short = it.next();

			String title_long = it.next();
			String icon_path = it.next();
			String lang = it.next();

			values.put(Category.COL_CATEGORY_ID, category_id);
			values.put(Category.COL_ORDER, order);
			values.put(Category.COL_TITLE, title_long);
			values.put(Category.COL_TITLE_SHORT, title_short);

			values.put(Category.COL_ICON_PATH, icon_path);
			values.put(Category.COL_LANGUAGE, lang);

			long id = db.insert(Category.TABLE_NAME, null, values);
		}
		Log.d(TAG, Category.TABLE_NAME + "loaded OK");

	}

	public static void itemCallback() {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion);

		dropTables(db);
		onCreate(db);
		// TODO: keep track of migration SQL between versions!!!
	}

	/**
	 * @param db
	 */
	public void dropTables(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + IndividualIcons.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Category.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + PhraseHistory.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS phrase_lists");
	}
}
