package com.epfl.android.aac_speech.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * Allows scrolling the scroller after layout updates, as simple scroller would
 * scroll to the earlier element
 * 
 * The issue was described here: http
 * ://stackoverflow.com/questions/4720469/horizontalscrollview-auto-scroll-to
 * -end-when-new-views-are-added
 * 
 * @author vidma
 * 
 */
public class DynamicHorizontalScrollView extends HorizontalScrollView {

	public DynamicHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public DynamicHorizontalScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public DynamicHorizontalScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	private OnLayoutListener mListener;

	// /...
	private interface OnLayoutListener {
		void onLayout();
	}

	public void fullScrollOnLayout(final int direction) {
		mListener = new OnLayoutListener() {
			@Override
			public void onLayout() {
				fullScroll(direction);
				mListener = null;
			}
		};
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mListener != null)
			mListener.onLayout();
	}
}