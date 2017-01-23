package com.space.aac.data.models;

import android.net.Uri;

import com.space.aac.cont_providers.IconsProvider;

/**
 * Created by space on 21/7/16.
 */
public class Icon {

    public static final String TABLE = "icon_meanings";

    public static final String PATH_STR = TABLE + "_listing_full";
    public static final String URI_STR = IconsProvider.URI_AUTHORITY + PATH_STR;
    public static final Uri CONTENT_URI = Uri.parse(URI_STR);

    public static final String COL_ID = "_id";
    public static final String COL_USE_COUNT = "use_count";

    public static final String COL_WORD = "word";
    public static final String COL_WORD_ASCII = "word_ascii_only";
    public static final String COL_PART_OF_SPEECH = "part_of_speech";
    public static final String COL_SPC_COLOR = "spc_color";
    public static final String COL_ICON_PATH = "icon_path";
    public static final String COL_LANG = "lang";
    public static final String COL_MAIN_CATEGORY = "main_category_id";
    public static final String COL_OFFENSIVE = "is_offensive";
    public static final String COL_GUID = "guid";
}
