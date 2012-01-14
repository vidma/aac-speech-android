package com.epfl.android.aac_speech.data;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.epfl.android.aac_speech.data.PicWordAction;
import com.epfl.android.aac_speech.data.models.Category;
import com.epfl.android.aac_speech.data.models.IndividualIcons;
import com.epfl.android.aac_speech.data.models.PhraseHistory;

/**
 * Higher level helper to access content on database
 * 
 * @author vidma
 * 
 */
public class DBHelper {
	private static final boolean DEBUG = false;
	private static final String TAG = "DBHelper";

	private static final int CATEGORY_RECENT_ITEMS_LIMIT = 9;

	private static ContentResolver cr = null;

	public DBHelper(ContentResolver contentResolver) {
		cr = contentResolver;
	}

	/**
	 * Resets the data in DB, e.g. after downloading new icons
	 * 
	 * TODO: once got user data (user icons), run updates, but not delete
	 * everything (in LowLevelDatabaseHelper)
	 * 
	 * @param context
	 */
	public void forceUpdateDatabase(Context context) {
		LowLevelDatabaseHelper mOpenHelper = new LowLevelDatabaseHelper(context);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		mOpenHelper.dropTables(db);
		mOpenHelper.onCreate(db);
		db.close();
	}

	/*
	 * ==== HISTORY ===
	 */

	public void updateIconHistory(ArrayList<PicWordAction> phrase,
			String serialized, String phrase_txt) {
		ContentValues values = new ContentValues();
		values.put(PhraseHistory.COL_PHRASE, phrase_txt);
		values.put(PhraseHistory.COL_PHRASE_SERIALISED, serialized);
		cr.insert(PhraseHistory.CONTENT_URI, values);
	}

	/**
	 * @param phrase
	 */
	public void update_icon_use_count(ArrayList<PicWordAction> phrase,
			Context context) {
		LowLevelDatabaseHelper mOpenHelper = new LowLevelDatabaseHelper(context);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (PicWordAction word : phrase) {
			if (word.wordID > 0) {
				if (i++ > 0) {
					sb.append(",");
				}
				sb.append(word.wordID);
			}
		}

		String id_list = sb.toString();
		String sql = "UPDATE " + IndividualIcons.TABLE_NAME + " SET "
				+ IndividualIcons.COL_USE_COUNT + "=  "
				+ IndividualIcons.COL_USE_COUNT + " + 1 WHERE "
				+ IndividualIcons.COL_ID + " IN (" + id_list + ") ";
		Log.d(TAG, "upd sql:" + sql);
		db.execSQL(sql);
		db.close();
	}

	/**
	 * returns the serialized phrase from DB given it's ID
	 * 
	 * @param phraseID
	 * @return
	 */
	public String getSerializedPhraseById(long phraseID) {
		Cursor cur = cr.query(PhraseHistory.CONTENT_URI, null,
				PhraseHistory.COL_ID + " = " + phraseID, null, null);

		String result = null;

		if (cur.moveToFirst()) {
			result = cur
					.getString(cur
							.getColumnIndexOrThrow(PhraseHistory.COL_PHRASE_SERIALISED));
		}

		cur.close();
		return result;
	}

	/**
	 * @param categoryId
	 * @return
	 */
	public Cursor getPhraseHistoryCursor(String search_text) {
		Uri uri = PhraseHistory.CONTENT_URI;

		String selection = null;
		String[] selectionArgs = null;

		if (search_text != null && !search_text.equals("")) {
			selection = ((selection != null) ? selection + " AND " : "") + "( "
					+ PhraseHistory.COL_PHRASE + " LIKE ? )";
			selectionArgs = new String[] { "%" + search_text + "%", };
		}

		// TODO: here it may return most used on top
		Cursor cur = cr.query(uri, null, selection, selectionArgs,
				PhraseHistory.COL_ID + " DESC");
		// Log.d("a", cur.toString());
		return cur;
	}

	/*
	 * ==== ICONS ===
	 */

	public PicWordAction getIconById(long itemId) {
		PicWordAction newWord = null;
		Uri uri = Uri.parse(IndividualIcons.URI_STR + "/" + itemId);

		Cursor cur = cr.query(uri, null, BaseColumns._ID + " = " + itemId,
				null, null);

		if (cur.moveToFirst()) {
			// Log.d("columns", arrayToString(cur.getColumnNames()));
			newWord = DB_Cursor_to_WordIcon(cur);
			cur.close();
		}
		return newWord;
	}

	public PicWordAction DB_Cursor_to_WordIcon(Cursor cur) {
		PicWordAction newWord;
		String word = cur.getString(cur.getColumnIndexOrThrow("word"));
		String part_of_speech = cur.getString(cur
				.getColumnIndexOrThrow("part_of_speech"));
		int spc_color = cur.getInt(cur.getColumnIndexOrThrow("spc_color"));
		String icon_path = cur
				.getString(cur.getColumnIndexOrThrow("icon_path"));
		int use_count = cur.getInt(cur
				.getColumnIndexOrThrow(IndividualIcons.COL_USE_COUNT));

		newWord = new PicWordAction(word, part_of_speech, icon_path, spc_color);
		newWord.wordID = cur.getInt(cur.getColumnIndexOrThrow("_id"));
		// newWord.use_count = use_count;

		if (DEBUG) {
			Log.d("got an item from db", "icon:" + word + "part of:"
					+ part_of_speech + "path:  " + icon_path);
			Log.d("the wordButton:", newWord.toDebugString());
		}

		return newWord;
	}

	/*
	 * not used anymore: public ArrayList<PicWordAction> getIconsByCategory(long
	 * categoryId) { PicWordAction newWord = null;
	 * 
	 * ArrayList<PicWordAction> icon_list = new ArrayList<PicWordAction>();
	 * 
	 * Cursor cur = getIconsCursorByCategory(categoryId, null);
	 * 
	 * if (cur.moveToFirst()) do { newWord = this.DB_Cursor_to_WordIcon(cur);
	 * 
	 * icon_list.add(newWord); } while (cur.moveToNext()); cur.close();
	 * 
	 * return icon_list; }
	 */

	/**
	 * @param categoryId
	 * @return
	 */
	public Cursor getIconsCursorByCategory(long categoryId, String search_text) {
		Uri uri = Uri.parse(IndividualIcons.URI_STR + "/" + categoryId);

		String selection = null;
		String[] selectionArgs = null;

		if (categoryId != 0) {
			selection = "(main_category_id = " + categoryId + ")";
		}

		if (search_text != null && !search_text.equals("")) {
			selection = ((selection != null) ? selection + " AND " : "")
					+ "( word LIKE ? OR word_ascii_only LIKE ?)";
			selectionArgs = new String[] { search_text + "%", search_text + "%" };
		}
		// TODO: here it may return most used on top. and then by alphabet?
		Cursor cur = cr.query(uri, new String[] { "*", "0 AS is_recent" }, selection, selectionArgs, "word ASC");
		// Log.d("a", cur.toString());
		return cur;
	}

	public Cursor getRecentIconsCursorByCategory(long categoryId) {
		Uri uri = Uri.parse(IndividualIcons.URI_STR + "/" + categoryId);

		String selection = null;
		String[] selectionArgs = null;

		selection = "(main_category_id = " + categoryId + ") AND ("
				+ IndividualIcons.COL_USE_COUNT + " > 0 )";

		// TODO: here it may return most used on top. and then maybe order by
		// alphabet or just by use count
		Cursor cur = cr.query(uri, new String[] { "*", "1 AS is_recent" },
				selection, selectionArgs, IndividualIcons.COL_USE_COUNT
						+ " DESC LIMIT " + CATEGORY_RECENT_ITEMS_LIMIT);

		// Log.d("a", cur.toString());
		return cur;
	}

	/* Category info */
	public String getCategoryInfoTitle(long categoryId) {
		try {
			Cursor cur = cr.query(Category.CONTENT_URI, null,
					Category.COL_CATEGORY_ID + " = " + categoryId, null, null);
			cur.moveToFirst();
			return cur.getString(cur.getColumnIndex(Category.COL_TITLE));

		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, e.toString());
			return "";
		}

		// Log.d("a", cur.toString());
	}

}
