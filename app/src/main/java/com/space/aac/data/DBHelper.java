package com.space.aac.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.space.aac.data.models.Category;
import com.space.aac.data.models.Icon;
import com.space.aac.data.models.PhraseHistory;
import com.space.aac.lib.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by space on 21/7/16.
 */
public class DBHelper {
    private static final boolean DEBUG = false;
    private static final String TAG = "DBHelper";

    private static final int CATEGORY_RECENT_ITEMS_LIMIT = 9;
    public boolean pref_hide_offensive = true;

    private ContentResolver cr = null;

    public DBHelper(ContentResolver contentResolver) {
        cr = contentResolver;
    }

    public DBHelper(ContentResolver contentResolver, boolean pref_hide_offensive) {
        cr = contentResolver;
        this.pref_hide_offensive = pref_hide_offensive;
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
        try {
            mOpenHelper.dropTables(db);
            mOpenHelper.onCreate(db);
        } finally {
            db.close();
        }
    }

	/*
	 * ==== HISTORY ===
	 */

    public void updateIconHistory(ArrayList<Pictogram> phrase, String serialized, String phrase_txt) {
        ContentValues values = new ContentValues();
        values.put(PhraseHistory.COL_PHRASE, phrase_txt);
        values.put(PhraseHistory.COL_PHRASE_SERIALISED, serialized);
        cr.insert(PhraseHistory.CONTENT_URI, values);
    }

    /**
     * @param phrase
     */
    public void update_icon_use_count(ArrayList<Pictogram> phrase, Context context) {
        LowLevelDatabaseHelper mOpenHelper = new LowLevelDatabaseHelper(context);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Pictogram word : phrase) {
            if (word.wordID > 0) {
                if (i++ > 0) {
                    sb.append(",");
                }
                sb.append(word.wordID);
            }
        }

        String id_list = sb.toString();
        String sql = "UPDATE " + Icon.TABLE + " SET " + Icon.COL_USE_COUNT + "=  " + Icon.COL_USE_COUNT
                + " + 1 WHERE " + Icon.COL_ID + " IN (" + id_list + ") ";
        Log.d(TAG, "upd sql:" + sql);
        try {
            db.execSQL(sql);
        } finally {
            db.close();
        }
    }

    /**
     * returns the serialized phrase from DB given it's ID
     *
     * @param phraseID
     * @return
     */
    public String getSerializedPhraseById(long phraseID) {
        Cursor cur = cr.query(PhraseHistory.CONTENT_URI, null, PhraseHistory.COL_ID + " = " + phraseID, null, null);

        String result = null;

        try {
            if (cur.moveToFirst()) {
                result = cur.getString(cur.getColumnIndexOrThrow(PhraseHistory.COL_PHRASE_SERIALISED));
            }
        } finally {
            cur.close();
        }
        return result;
    }

    /**
     * @paramcategoryId
     * @return
     */
    public Cursor getPhraseHistoryCursor(String search_text) {
        Uri uri = PhraseHistory.CONTENT_URI;

        String selection = null;
        String[] selectionArgs = null;

        if (search_text != null && !search_text.equals("")) {
            selection = ((selection != null) ? selection + " AND " : "") + "( " + PhraseHistory.COL_PHRASE
                    + " LIKE ? )";
            selectionArgs = new String[] { "%" + search_text + "%", };
        }

        // TODO: filter bad words out?
        Cursor cur = cr.query(uri, null, selection, selectionArgs, PhraseHistory.COL_ID + " DESC");
        // Log.d("a", cur.toString());
        return cur;
    }

    /*
     * ==== ICONS ===
     */
    public Pictogram getIconById(long itemId) {
        Pictogram newWord = null;
        Uri uri = Uri.parse(Icon.URI_STR + "/" + itemId);
        Cursor cur = cr.query(uri, null, BaseColumns._ID + " = " + itemId, null, null);
        if (cur.moveToFirst()) {
            // Log.d("columns", arrayToString(cur.getColumnNames()));
            newWord = cursorToIcon(cur);
        }
        cur.close();

        return newWord;
    }

    public Pictogram cursorToIcon(Cursor cur) {
        Pictogram newWord;
        String word = cur.getString(cur.getColumnIndexOrThrow("word"));
        String part_of_speech = cur.getString(cur.getColumnIndexOrThrow("part_of_speech"));
        int spc_color = cur.getInt(cur.getColumnIndexOrThrow("spc_color"));
        String icon_path = cur.getString(cur.getColumnIndexOrThrow("icon_path"));
        @SuppressWarnings("unused")
        int use_count = cur.getInt(cur.getColumnIndexOrThrow(Icon.COL_USE_COUNT));

        newWord = new Pictogram(word, part_of_speech, icon_path, spc_color);
        newWord.wordID = cur.getInt(cur.getColumnIndexOrThrow("_id"));

        if (DEBUG) {
            Log.d("got an item from db", "icon:" + word + "part of:" + part_of_speech + "path:  " + icon_path);
            Log.d("the wordButton:", newWord.toDebugString());
        }

        return newWord;
    }

    /**
     * get icons cursor, optionally filtered:
     * - categoryId
     * - search_text
     *
     * @param categoryId
     * @return
     */
    public Cursor getIconsCursorByCategory(long categoryId, String search_text, String filter) {
        Uri uri = Uri.parse(Icon.URI_STR + "/" + categoryId);

        List<String> projection = ArrayUtils.StrArr(new String[]{"*"});
        List<String> selections = new ArrayList<String>();
        String[] selectionArgs = null;
        String sortOrder = "word ASC";

        // filter bad words out?
        if (pref_hide_offensive) {
            selections.add("(" + Icon.COL_OFFENSIVE + " = 0)");
        }

        if (categoryId != 0) {
            selections.add("(main_category_id = " + categoryId + ")");
        }

        if (filter == "recent") {
            selections.add(String.format("( %s > 0)", Icon.COL_USE_COUNT));
        }

        if (search_text != null && !search_text.equals("")) {
            // TODO: match beginning of any word? is this fast enough?
            selections.add("( word LIKE ? OR word_ascii_only LIKE ? OR word LIKE ? OR word_ascii_only LIKE ?)");
            String s1 = search_text + "%";
            String s2 = "% "+ search_text + "%";
            selectionArgs = new String[] {s1, s1, s2, s2};
        }

        // in english language ignore the "to " prefix when sorting. this shall be in DB?
        projection.add("LOWER(REPLACE(word, 'to ', '')) AS word_clean");
        sortOrder = "word_clean ASC";

        Cursor cur = cr.query(uri, projection.toArray(new String[]{}),
                TextUtils.join(" AND ", selections), selectionArgs, sortOrder);
        return cur;
    }


    /* Category info */
    public String getCategoryTitle(long categoryId, boolean shorten) {
        Cursor cur = null;
        String result = "";

        try {
            cur = cr.query(Category.CONTENT_URI, null, Category.COL_CATEGORY_ID + " = " + categoryId, null, null);
            cur.moveToFirst();

            if (shorten)
                result = cur.getString(cur.getColumnIndex(Category.COL_TITLE_SHORT));
            else
                result = cur.getString(cur.getColumnIndex(Category.COL_TITLE));

        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG, e.toString());
        } finally {
            if (cur != null)
                cur.close();
        }

        return result;
        // Log.d("a", cur.toString());
    }

    public String getCategoryTitle(long categoryId) {
        return getCategoryTitle(categoryId, false);
    }

    public String getCategoryTitleShort(long categoryId) {
        return getCategoryTitle(categoryId, true);
    }

}
