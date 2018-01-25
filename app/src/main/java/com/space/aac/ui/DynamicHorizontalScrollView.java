package com.space.aac.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * Created by space on 19/7/16.
 */
public class DynamicHorizontalScrollView extends HorizontalScrollView {

    public DynamicHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public DynamicHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
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

    @SuppressLint("WrongCall")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mListener != null)
            mListener.onLayout();
    }
}