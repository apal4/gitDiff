package com.capozio.flightbag.feature.permissonSlip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
/*** ***********************************************************************
 * <p>
 * Pilot Training System CONFIDENTIAL
 * __________________
 * <p>
 * [2015] - [2017] Pilot Training System
 * All Rights Reserved.
 * <p>
 * NOTICE:  All information contained herein is, and remains
 * the property of Pilot Training System,
 * The intellectual and technical concepts contained
 * herein are proprietary to Pilot Training System
 * and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Pilot Training System.
 *
 * Created by Ying Zhang on 10/7/16.
 */

/**
 * The view on the bottom of the waiver signing screen used to
 * actually capture the signature of the homeowner.
 * This is just signature part of the screen.
 */
public class SignatureView extends View {
    private static final float STROKE_WIDTH = 5f; // Width of "pen" used for signature.
    // Need to track this so the dirty region can accommodate the stroke.
    private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

    private Paint paint = new Paint();
    private Path path = new Path();
    private boolean toClear = false;

    // optimizes painting by invalidating the smallest possible area.
    private float lastTouchX;
    private float lastTouchY;
    private RectF dirtyRect = new RectF();

    public SignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(STROKE_WIDTH);
    }

    /**
     * Erases the signature.
     */
    public void clearSignature() {
//        path.reset();
//        Paint clearPaint = new Paint();
//        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//        if(canvas != null)
//        canvas.drawRect(0, 0, 0, 0, clearPaint);

        toClear = true;
        // Repaint the entire view.
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        if(this.canvas == null)
//            this.canvas = canvas;
        if (toClear) {
            path = new Path();
            Paint clearPaint = new Paint();
            clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST));
            canvas.drawPaint(clearPaint);
            toClear = false;
        }
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(eventX, eventY);
                lastTouchX = eventX;
                lastTouchY = eventY;
                // there is no end point yet, so don't waste cycles invalidating.
                return true;

            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                // start tracking the dirty region.
                resetDirtyRect(eventX, eventY);

                // when the hardware tracks events faster than they are delivered,
                // the event will contain a history of those skipped points.
                int historySize = event.getHistorySize();
                for (int i = 0; i < historySize; i++) {
                    float historicalX = event.getHistoricalX(i);
                    float historicalY = event.getHistoricalY(i);
                    expandDirtyRect(historicalX, historicalY);
                    path.lineTo(historicalX, historicalY);
                }
                // after replaying history, connect the line to the touch point.
                path.lineTo(eventX, eventY);
                break;
            default:
                return false;
        }

        // include half the stroke width to avoid clipping.
        invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

        return true;
    }

    /**
     * Called when replaying history to ensure the dirty region includes all points.
     */
    private void expandDirtyRect(float historicalX, float historicalY) {
        if (historicalX < dirtyRect.left)
            dirtyRect.left = historicalX;
        else if (historicalX > dirtyRect.right)
            dirtyRect.right = historicalX;

        if (historicalY < dirtyRect.top)
            dirtyRect.top = historicalY;
        else if (historicalY > dirtyRect.bottom)
            dirtyRect.bottom = historicalY;
    }

    /**
     * Resets the dirty region when the motion event occurs.
     */
    private void resetDirtyRect(float eventX, float eventY) {
        // The lastTouchX and lastTouchY were set when the ACTION_DOWN motion event occurred.
        dirtyRect.left = Math.min(lastTouchX, eventX);
        dirtyRect.right = Math.max(lastTouchX, eventX);
        dirtyRect.top = Math.min(lastTouchY, eventY);
        dirtyRect.bottom = Math.max(lastTouchY, eventY);
    }
}
