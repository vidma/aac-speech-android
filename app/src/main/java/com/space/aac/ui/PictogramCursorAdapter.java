package com.space.aac.ui;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.space.aac.MainActivity;
import com.space.aac.data.Pictogram.SpcColor;
import com.space.aac.lib.AsyncImageLoaderTask;

/**
 * Created by space on 29/7/16.
 * /*
 * Adapter to fill ListView or GridView with items containing icon + text
 *
 */
public class PictogramCursorAdapter extends SimpleCursorAdapter implements Filterable {

    private static final String TAG = "PictogramCursorAdapter";

    private boolean pref_uppercase = false;
    private Context context;

    private static final int COLUMN_INDEX_UNDEFINED = -2;

    private int COLUMN_INDEX_SPC_COLOR = COLUMN_INDEX_UNDEFINED;

    /*
     * Idea on executors
     * http://blog.chariotsolutions.com/2010/04/enhancing-performance
     * -on-android.html
     */
    public PictogramCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, boolean pref_uppercase) {
        super(context, layout, c, from, to);
        this.context = context;
        this.pref_uppercase = pref_uppercase;
    }

    @Override
    public void setViewText(TextView v, String text) {
        if (pref_uppercase) {
            text = text.toUpperCase();
        }

        //Cursor c = this.getCursor();
        // Only for category
        //		int is_recent_index;
        //		if ((is_recent_index = c.getColumnIndex("is_recent")) != -1) {
        //			int is_recent = c.getInt(is_recent_index);
        //			v.setTypeface(null, (is_recent == 1) ? Typeface.BOLD : Typeface.NORMAL);
        //		}
        super.setViewText(v, text);
    }

    /**
     * Given the bitmap URI from database, convert it into the Bitmap resource
     * TODO: set the icon color
     */
    @Override
    public void setViewImage(ImageView v, String imagePath) {
        if (MainActivity.DEBUG) {
            Log.d("PictogramCursorAdapter", "setting img to:" + imagePath);
        }

        // set the SPC color if it's not set to be hidden
        if (!MainActivity.getPrefHideSPCColor()) {
			/* get the current SPC color (from cursor) and set it as background */
            Cursor c = this.getCursor();
            if (COLUMN_INDEX_SPC_COLOR == COLUMN_INDEX_UNDEFINED) {
                COLUMN_INDEX_SPC_COLOR = c.getColumnIndex("spc_color");
            }
            int spc_color = c.getInt(COLUMN_INDEX_SPC_COLOR);
            v.setBackgroundColor(SpcColor.getColor(spc_color, false));
        }

        AsyncImageLoaderTask.AsycLoadImage(imagePath, v, context);
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (getFilterQueryProvider() != null) {
            return getFilterQueryProvider().runQuery(constraint);
        }
        return super.runQueryOnBackgroundThread(constraint);

    }
}