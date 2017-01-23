package com.space.aac.cont_providers;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.space.aac.TTSButtonActivity;
import com.space.aac.data.LowLevelDatabaseHelper;
import com.space.aac.data.models.Category;
import com.space.aac.data.models.Icon;
import com.space.aac.data.models.PhraseHistory;


import java.util.HashMap;
import java.util.List;

/**
 * Created by space on 21/7/16.
 */
public class IconsProvider extends ContentProvider {
    static final String AUTHORITY = "com.space.aac.cont_providers.IconsProvider";

    private LowLevelDatabaseHelper mOpenHelper;


    private static final String TAG = "PhraseProviderDB";

    // TODO: sort order -- shall that be the real collumn or the one in AS
    private static final String GESTURE_SEARCH_DEFAULT_SUGGESTION_SORT_ORDER = "word ASC";

    public static final String URI_AUTHORITY = "content://" + AUTHORITY + "/";

    /* CONSTANTS DEFINING PATHS AND URIS */
    public static final String ICON_LISTING_BY_CATEGORY_PATH_STR = Icon.TABLE + "_by_category";

    public static final String GESTURE_ICON_LISTING_BY_CATEGORY_PATH_STR = Icon.TABLE + "_by_category"
            + "_gesture";

    public static final String GESTURE_SEARCH_PATH_STR = Icon.TABLE + "_gesture_search";

    public static final Uri GESTURE_SEARCH_CONTENT_URI = Uri.parse(URI_AUTHORITY + GESTURE_SEARCH_PATH_STR);

    public static final Uri GESTURE_SEARCH_BY_CATEGORY_CONTENT_URI = Uri.parse(URI_AUTHORITY
            + GESTURE_ICON_LISTING_BY_CATEGORY_PATH_STR);

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.space.acc.icons";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.space.acc.icons";

    /* PROJECTION MAP FOR GESTURE SEARCH */
    private static HashMap<String, String> gestureSearchSuggestionProjectionMap;
    static {
        gestureSearchSuggestionProjectionMap = new HashMap<String, String>();
        gestureSearchSuggestionProjectionMap.put(BaseColumns._ID, "_id");
        gestureSearchSuggestionProjectionMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, "word_ascii_only AS "
                + SearchManager.SUGGEST_COLUMN_TEXT_1);
        gestureSearchSuggestionProjectionMap.put(SearchManager.SUGGEST_COLUMN_TEXT_2, "word AS "
                + SearchManager.SUGGEST_COLUMN_TEXT_2);
        //TODO: add assets content provider addr
        gestureSearchSuggestionProjectionMap.put(SearchManager.SUGGEST_COLUMN_ICON_1,
                String.format("icon_path AS %s", SearchManager.SUGGEST_COLUMN_ICON_1));
        //String.format("REPLACE(icon_path, '%s', '%s') AS %s",
        //			  // TODO: now we use Real files
        //			  LowLevelDatabaseHelper.ASSETS_DIR, AssetsProvider.URI, SearchManager.SUGGEST_COLUMN_ICON_1)
        //		);
    }

    /* URI MATCHER */
    private static final int URI_MATCH_GESTURE_SUGGESTION = 0;
    private static final int URI_MATCH_SUGGESTION_ID = 10;
    private static final int URI_MATCH_ICON_LISTING_BY_CATID_FULL = 100;
    private static final int URI_MATCH_GESTURE_BYCATEGORY = 1000;
    private static final int URI_MATCH_ICON_HISTORY = 10000;
    private static final int URI_MATCH_CATEGORY_ICON_HISTORY = 100000;

    private static final int URI_MATCH_CATEGORY_INFO = 1000000;

    private static final UriMatcher sUriMatcher;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, GESTURE_SEARCH_PATH_STR, URI_MATCH_GESTURE_SUGGESTION);
        sUriMatcher.addURI(AUTHORITY, GESTURE_SEARCH_PATH_STR + "/#", URI_MATCH_SUGGESTION_ID);
        sUriMatcher.addURI(AUTHORITY, Icon.PATH_STR + "/#", URI_MATCH_ICON_LISTING_BY_CATID_FULL);
		/* param: category_id */
        sUriMatcher.addURI(AUTHORITY, GESTURE_ICON_LISTING_BY_CATEGORY_PATH_STR + "/#", URI_MATCH_GESTURE_BYCATEGORY);
        sUriMatcher.addURI(AUTHORITY, PhraseHistory.HISTORY_PATH_STR, URI_MATCH_ICON_HISTORY);
        sUriMatcher.addURI(AUTHORITY, Category.PATH_STR, URI_MATCH_CATEGORY_INFO);

    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new LowLevelDatabaseHelper(getContext());
         Log.d("Phrase provider", "context" + getContext().toString());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		/*
		 * qb.appendWhere do no add AND automatically, so we have to append in
		 * manually
		 */
        int whereChunkCount = 0;
        int match = match_uri(uri);

		/* Set Projection for Gesture search */
        String orderBy = sortOrder;
        if (match == URI_MATCH_GESTURE_SUGGESTION || match == URI_MATCH_SUGGESTION_ID
                || match == URI_MATCH_GESTURE_BYCATEGORY) {
            qb.setProjectionMap(gestureSearchSuggestionProjectionMap);
            if (TextUtils.isEmpty(orderBy)) {
                orderBy = GESTURE_SEARCH_DEFAULT_SUGGESTION_SORT_ORDER;
            }
        }

		/* Set category filter if any */
        if (match == URI_MATCH_GESTURE_BYCATEGORY) {

            List<String> uri_segments = uri.getPathSegments();
            String category_id = uri_segments.get(uri_segments.size() - 1);

            // TODO: allow icons to belong to multiple categories (there are not many such yet, only ~30 in 5000)
            CharSequence whereChunk = "main_category_id=" + category_id;
            appendWhereChunkSmart(qb, whereChunkCount++, whereChunk);

        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        // easiest way to do language filtering is to append it into the query right here
        if (match == URI_MATCH_ICON_HISTORY) {
			/* TODO: language filtering for History !!! */
            qb.setTables(PhraseHistory.TABLE);
            appendWhereChunkSmart(qb, whereChunkCount++,
                    PhraseHistory.COL_LANGUAGE + " = '" + TTSButtonActivity.getPreferedLanguage() + "'");
        } else if (match == URI_MATCH_CATEGORY_INFO) {
            qb.setTables(Category.TABLE);
            appendWhereChunkSmart(qb, whereChunkCount++,
                    Category.COL_LANGUAGE + " = '" + TTSButtonActivity.getPreferedLanguage() + "'");

        } else {
            qb.setTables(Icon.TABLE);
            appendWhereChunkSmart(qb, whereChunkCount++, Icon.COL_LANG + " = '" + TTSButtonActivity.getPreferedLanguage()
                    + "'");

        }
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    /**
     * @param qb
     * @param whereChunkCount
     * @param whereChunk
     */
    private static void appendWhereChunkSmart(SQLiteQueryBuilder qb, int whereChunkCount, CharSequence whereChunk) {
        whereChunk = ((whereChunkCount > 0) ? "AND" : "") + " (" + whereChunk + ") ";
        qb.appendWhere(whereChunk);
    }

    @Override
    public String getType(Uri url) {
        int match = sUriMatcher.match(url);
        switch (match) {

            case URI_MATCH_GESTURE_BYCATEGORY:
            case URI_MATCH_GESTURE_SUGGESTION:
                return CONTENT_TYPE;

            case URI_MATCH_SUGGESTION_ID:
                return CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: is this used?
        String tableName = get_matched_tablename(uri);

        /** Append LANGUAGE to PhraseHistory */
        if (tableName == PhraseHistory.TABLE) {
            if (!values.containsKey(PhraseHistory.COL_LANGUAGE)) {
                values.put(PhraseHistory.COL_LANGUAGE, TTSButtonActivity.getPreferedLanguage());

            }
        }

        if (tableName != null) {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            // Performs the insert and returns the ID of the new note.
            db.insert(tableName, null, values);
            db.close();
            return null;
        }
        // If the insert didn't succeed, then the rowID is <= 0. Throws an
        // exception.
        throw new SQLException("Failed to insert row into " + uri);
    }

    /**
     * @param uri
     * @return
     */
    private int match_uri(Uri uri) {
        int match = sUriMatcher.match(uri);
        if (match == -1) {
            throw new IllegalArgumentException(uri + " -- Unknown URL");
        }
        return match;
    }

    @Override
    public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
        // TODO: is this in use?
        String tableName = get_matched_tablename(uri);
        if (tableName != null) {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            db.update(tableName, values, whereClause, whereArgs);
            db.close();
        }
        return 0;
    }

    /**
     * @param uri
     * @return
     */
    private String get_matched_tablename(Uri uri) {
        int match = match_uri(uri);
        String tableName = null;

        if (match == URI_MATCH_ICON_HISTORY) {
            tableName = PhraseHistory.TABLE;
        }
        if (match == URI_MATCH_CATEGORY_ICON_HISTORY) {
            tableName = Icon.TABLE;
        }
        return tableName;
    }
}
