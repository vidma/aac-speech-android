package com.epfl.android.aac_speech.data.models;

import android.net.Uri;

import com.epfl.android.aac_speech.data.PhraseProviderDB;

public class Category {
	public static final String PATH_STR = "categories";
	public static final Uri CONTENT_URI = Uri.parse(PhraseProviderDB.URI_AUTHORITY + PATH_STR);

	public static final String TABLE_NAME = "categories";

	/*
	 * TODO: (_id INTEGER PRIMARY KEY AUTOINCREMENT, phrase TEXT,
	 * phrase_items_serialized TEXT);
	 */

	public static final String COL_PK = "_id";

	/* we may have multiple languages, therefore category id is not unique */
	public static final String COL_CATEGORY_ID = "category_id";

	public static final String COL_TITLE = "title";

	/* short title for home screen */
	public static final String COL_TITLE_SHORT = "title_short";

	public static final String COL_ICON_PATH = "icon_path";
	public static final String COL_ORDER = "cat_order";

	public static final String COL_LANGUAGE = "lang";
}
