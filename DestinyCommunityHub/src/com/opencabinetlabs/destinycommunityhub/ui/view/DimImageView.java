package com.opencabinetlabs.destinycommunityhub.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by John on 2/5/14.
 */
public class DimImageView extends ImageView {

    private boolean mIsSelected;

    private double mHeightRatio;

    public DimImageView(Context context) {
        super(context);
        init();
    }

    public DimImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DimImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mIsSelected = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && !mIsSelected) {
            setColorFilter(0x99000000);
            mIsSelected = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL && mIsSelected) {
            setColorFilter(Color.TRANSPARENT);
            mIsSelected = false;
        }

        return super.onTouchEvent(event);
    }


    public void setHeightRatio(double ratio) {
        if (ratio != mHeightRatio) {
            mHeightRatio = ratio;
            requestLayout();
        }
    }

    public double getHeightRatio() {
        return mHeightRatio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mHeightRatio > 0.0) {
            // set the image views size
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = (int) (width * mHeightRatio);
            setMeasuredDimension(width, height);
        }
        else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
