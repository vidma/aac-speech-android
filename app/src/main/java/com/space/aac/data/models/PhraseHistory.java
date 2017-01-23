package com.space.aac.data.models;

import android.net.Uri;

import com.space.aac.cont_providers.IconsProvider;

/**
 * Created by space on 21/7/16.
 */
public class PhraseHistory {

    public static final String HISTORY_PATH_STR = "phrase_history";

    public static final Uri CONTENT_URI = Uri.parse(IconsProvider.URI_AUTHORITY + HISTORY_PATH_STR);

    public static final String TABLE = "phrase_history";

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
