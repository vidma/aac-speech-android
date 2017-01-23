package com.space.aac.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import com.space.aac.ui.util.Scale;

/**
 * Created by space on 30/7/16.
 */
public class ScalingLinearLayout extends LinearLayout {
    int baseWidth;
    int baseHeight;
    boolean alreadyScaled;
    float scale;
    int expectedWidth;
    int expectedHeight;

    public void cleanup(){
        this.alreadyScaled = false;
        //this.scale = 1;
    }

    public ScalingLinearLayout(Context context) {
        super(context);

        Log.d("notcloud.view", "ScalingLinearLayout: width=" + this.getWidth() + ", height=" + this.getHeight());
        this.alreadyScaled = false;
    }

    public ScalingLinearLayout(Context context, AttributeSet attributes) {
        super(context, attributes);

        Log.d("notcloud.view", "ScalingLinearLayout: width=" + this.getWidth() + ", height=" + this.getHeight());
        this.alreadyScaled = false;
    }

    public void onFinishInflate() {
        updateMeasures();

        if (this.alreadyScaled) {
            Scale.scaleViewAndChildren((LinearLayout) this, this.scale, 0);
        }
    }

    private void updateMeasures() {
        Log.d("notcloud.view", "ScalingLinearLayout::onFinishInflate: 1 width=" + this.getWidth() + ", height=" + this.getHeight());

        // Do an initial measurement of this layout with no major restrictions on size.
        // This will allow us to figure out what the original desired width and height are.
        this.measure(2500, 2000); // TODO: Adjust this up if necessary.
        this.baseWidth = this.getMeasuredWidth();
        this.baseHeight = this.getMeasuredHeight();
        Log.d("notcloud.view", "ScalingLinearLayout::onFinishInflate: 2 width=" + this.getWidth() + ", height=" + this.getHeight());
        Log.d("notcloud.view", "ScalingLinearLayout::onFinishInflate: 2 basewidth=" + this.baseWidth + ", baseheight=" + this.baseHeight);

        Log.d("notcloud.view", "ScalingLinearLayout::onFinishInflate: alreadyScaled=" + this.alreadyScaled);
        Log.d("notcloud.view", "ScalingLinearLayout::onFinishInflate: scale=" + this.scale);
    }

    public void draw(Canvas canvas) {
        Log.d("notcloud.view", "ScalingLinearLayout::draw_start");

        // Get the current width and height.
        int width = this.getWidth();
        int height = this.getHeight();

        // Figure out if we need to scale the layout.
        // We may need to scale if:
        //    1. We haven't scaled it before.
        //    2. The width has changed.
        //    3. The height has changed.
        if(!this.alreadyScaled || width != this.expectedWidth || height != this.expectedHeight) {
            Log.d("notcloud.view", "ScalingLinearLayout::scalling again");

            updateMeasures();

            // Figure out the x-scaling.
            float xScale = (float)width / this.baseWidth;
            if(this.alreadyScaled && width != this.expectedWidth) {
                xScale = (float)width / this.expectedWidth;
            }
            // Figure out the y-scaling.
            float yScale = (float)height / this.baseHeight;
            if(this.alreadyScaled && height != this.expectedHeight) {
                yScale = (float)height / this.expectedHeight;
            }

            // Scale the layout.
            this.scale = Math.min(xScale, yScale) * (float)3; // add some extra to make sure the contents fit..
            // TODO
            //this.scale = (float) 0.5;

            // do not allow zero scaling as it might mess the things up (e.g. when Layout is empty)
            if (this.scale < 0.05 || this.baseWidth <= 20) {
                //this.scale = (float) 1.0;
                // the element is probably not yet properly initialized, lets wait...
                return; //prevent super.draw()
            }
            else {
                Log.d("notcloud.view", "ScalingLinearLayout::onLayout: Scaling! scale=" + this.scale);
                Scale.scaleViewAndChildren((LinearLayout)this, this.scale, 0);

                // Mark that we've already scaled this layout, and what
                // the width and height were when we did so.
                this.alreadyScaled = true;
                this.expectedWidth = width;
                this.expectedHeight = height;
            }

            // Finally, return.
            return;
        }
        Log.d("notcloud.view", "ScalingLinearLayout::draw_end_pre");

        super.draw(canvas);

        Log.d("notcloud.view", "ScalingLinearLayout::draw_end");
    }
}