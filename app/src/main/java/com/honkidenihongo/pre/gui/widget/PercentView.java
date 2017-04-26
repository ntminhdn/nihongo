package com.honkidenihongo.pre.gui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.honkidenihongo.pre.R;

/**
 * One class custom draw circle ImageView.
 *
 * @author BinhDT.
 */
public class PercentView extends View {
    private Paint mPaint;
    private Paint mBgPaint;
    private RectF mRect;
    private double mPercentage = 0;

    /**
     * Constructor.
     *
     * @param context Value.
     */
    public PercentView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor.
     *
     * @param context Value.
     */
    public PercentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructor.
     *
     * @param context Value.
     */
    public PercentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        mBgPaint = new Paint();

        mBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.common_white));
        mBgPaint.setAntiAlias(true);
        mBgPaint.setStyle(Paint.Style.FILL);

        mRect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int left = 0;
        int width = getWidth();
        int top = 0;
        mRect.set(left, top, left + width, top + width);

        canvas.drawArc(mRect, -90, 360, true, mPaint);

        if (mPercentage != 0) {
            canvas.drawArc(mRect, -90, (float) (360 * (1 - mPercentage)), true, mBgPaint);
        }
    }

    /**
     * Set giá trị phần trăm sẽ vẽ.
     *
     * @param percentage Value.
     */
    public void setPercentage(double percentage) {
        if (percentage == 0) {
            // Nếu phần trăm của day là 0 thì set lại màu cho paint.
            mPaint.setColor(ContextCompat.getColor(getContext(), R.color.common_white));
        }

        this.mPercentage = percentage;
        invalidate();
    }
}

