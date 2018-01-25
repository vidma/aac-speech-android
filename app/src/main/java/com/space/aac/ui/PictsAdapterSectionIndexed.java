package com.space.aac.ui;
import android.content.Context;
import android.database.Cursor;
import android.widget.AlphabetIndexer;
import android.widget.SectionIndexer;
/**
 * based on: http://stackoverflow.com/questions/4115920/alphabetindexer-with-custom-adapter
 * @author vidma
 *
 */
public class PictsAdapterSectionIndexed extends PictogramCursorAdapter implements SectionIndexer {
    AlphabetIndexer alphaIndexer;
    public PictsAdapterSectionIndexed(Context context, int layout, Cursor c, String[] from, int[] to,
                                      boolean pref_uppercase) {
        super(context, layout, c, from, to, pref_uppercase);
        alphaIndexer = new AlphabetIndexer(c, getColIdx(c), " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        alphaIndexer.setCursor(c);
    }
    private int getColIdx(Cursor c) {
        int sortedColumnIndex = c.getColumnIndex("word_clean");
        return sortedColumnIndex;
    }
    @Override
    public int getPositionForSection(int section) {
// TODO: can the cursor be empty/invalid ? but now all semms fine..
        return alphaIndexer.getPositionForSection(section);
    }
    @Override
    public int getSectionForPosition(int position) {
        return alphaIndexer.getSectionForPosition(position);
    }
    @Override
    public Object[] getSections() {
        return alphaIndexer.getSections();
    }
    @Override
    public void changeCursor(Cursor c) {
        super.changeCursor(c);
        alphaIndexer.setCursor(c);
    }
    public Cursor swapCursor(Cursor c) {
// recreate the indexer
        if (c != null) {
            alphaIndexer = new AlphabetIndexer(c, getColIdx(c), " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        }
        return super.swapCursor(c);
    }
}