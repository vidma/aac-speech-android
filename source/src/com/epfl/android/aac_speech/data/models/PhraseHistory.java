package com.epfl.android.aac_speech.data.models;

import android.net.Uri;

import com.epfl.android.aac_speech.data.PhraseProviderDB;

/* Model definitions */
public class PhraseHistory {
	public static final String HISTORY_PATH_STR = "phrase_history";

	public static final Uri CONTENT_URI = Uri.parse(PhraseProviderDB.URI_AUTHORITY + HISTORY_PATH_STR);

	public static final String TABLE_NAME = "phrase_history";

	/*
	 * TODO: (_id INTEGER PRIMARY KEY AUTOINCREMENT, phrase TEXT,
	 * phrase_items_serialized TEXT);
	 */

	public static final String COL_ID = "_id";
	public static final String COL_PHRASE = "phrase";
	public static final String COL_PHRASE_SERIALISED = "phrase_items_serialized";
	public static final String COL_DATETIME = "timeused";
	public static final String COL_LANGUAGE = "lang";

	// TODO: we may have another column of more completely serialized items
}
