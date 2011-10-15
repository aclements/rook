package edu.mit.csail.awakening.rook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;

public class PageView extends View
{
    private static final String TAG = "PageView";

    private RookFile file;
    private int page;
    private Bitmap pageImage;
    private int offset;

    public PageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        N2EpdController.setGL16Mode(2);
    }

    public void setFile(RookFile f)
    {
        file = f;
        page = -1;
        setPage(0);
    }

    public void setPage(int p)
    {
        setPage(p, false);
    }

    private void setPage(int p, boolean bottom)
    {
        int max = file.getNumPages();
        if (p >= max) {
            p = max - 1;
            bottom = true;
        }
        if (p < 0) {
            p = 0;
            bottom = false;
        }

        if (p != page) {
            page = p;
            pageImage = null;
            if (max != 0) {
                try {
                    pageImage = file.getPage(page);
                } catch (IOException e) {
                    // XXX
                }
            }
        }

        offset = 0;
        if (bottom && pageImage != null)
            offset = pageImage.getHeight() - getHeight();
        if (offset < 0)
            offset = 0;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        if (pageImage == null)
            return;

        Log.d(TAG, "page " + page + " offset " + offset);
        // XXX Stops working if we switch to another activity (e.g.,
        // the screensaver)
        N2EpdController.setGL16Mode(0);
        canvas.drawBitmap(pageImage, 0, -offset, new Paint());
    }

    // XXX Batch refreshes if the user is flipping through pages
    // quickly.

    public void next()
    {
        // Might be worth tweaking the overlap toward 100% if we're
        // really close but not quite at the edge.
        addToOffset(getHeight() * 95 / 100);
    }

    public void prev()
    {
        addToOffset(-getHeight() * 95 / 100);
    }

    private void addToOffset(int delta)
    {
        if (delta < 0 && offset == 0) {
            setPage(page - 1, true);
            return;
        }

        if (delta > 0 && offset + getHeight() >= pageImage.getHeight()) {
            setPage(page + 1);
            return;
        }

        offset += delta;
        if (offset + getHeight() > pageImage.getHeight())
            offset = pageImage.getHeight() - getHeight();
        if (offset < 0)
            offset = 0;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        // XXX Trackball also generates ACTION_UP
        if (e.getAction() != e.ACTION_UP)
            return true;

        float y = e.getY() / getHeight();
        // Log.d(TAG, "y="+e.getY()+" yp="+e.getYPrecision()+" h="+getHeight()+" y="+y);
        if (y < 0.33) {
            prev();
        } else if (y > 0.66) {
            next();
        }


        // // XXX Scroll the page
        // if (offset == 0) {
        //     offset = 580;
        //     invalidate();
        // } else
        //     setPage(page + 1);
        return true;
    }

    // http://forum.xda-developers.com/showpost.php?p=16475010&postcount=17
    // BTW, nook reader make full refresh on pages with images and
    // next one. Wise decision, because partial refreshing lead to
    // ghostings and artifacts in such cases.
}
