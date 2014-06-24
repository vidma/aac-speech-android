package com.epfl.android.aac_speech.ui;

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
		int sortedColumnIndex = c.getColumnIndex("word");
		alphaIndexer = new AlphabetIndexer(c, sortedColumnIndex, " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
	}

	@Override
	public int getPositionForSection(int section) {
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

}
