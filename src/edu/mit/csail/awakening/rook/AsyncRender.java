package edu.mit.csail.awakening.rook;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;

abstract class AsyncRender extends AsyncTask<Void, Void, Bitmap>
{
    private static final String TAG = "AsyncRender";

    private final RookFile file;
    private final int page, w, h;

    public AsyncRender(RookFile file, int page, int w, int h)
    {
        this.file = file;
        this.page = page;
        this.w = w;
        this.h = h;
    }

    @Override
    protected Bitmap doInBackground(Void... args)
    {
        Log.d(TAG, "Background rendering page " + page);

        if (isCancelled())
            return null;

        Bitmap orig;
        try {
            orig = file.getPage(page).render();
        } catch (IOException e) {
            Log.w(TAG, "Failed to render page: " + e);
            return null;
        }

        if (isCancelled())
            return null;

        // XXX Aspect ratio
        Bitmap scaled = orig.createScaledBitmap(orig, w, h, true);

        Log.d(TAG, "Done rendering page " + page);
        return scaled;
    }
}
