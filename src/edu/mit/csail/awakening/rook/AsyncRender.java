package edu.mit.csail.awakening.rook;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

class AsyncRender extends AsyncTask<Void, Void, Bitmap>
{
    private static final String TAG = "AsyncRender";

    private final RookFile file;
    private final int page, w, h;
    private LinkedList<Callback> callbacks;

    public AsyncRender(RookFile file, int page, int w, int h)
    {
        this.file = file;
        this.page = page;
        this.w = w;
        this.h = h;

        callbacks = new LinkedList<Callback>();
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

    public interface Callback
    {
        void onImageReady(Bitmap img);
    }

    public void addCallback(Callback callback)
    {
        if (getStatus() == Status.FINISHED) {
            try {
                callback.onImageReady(get());
            } catch (ExecutionException e) {
                // We've already complained
            } catch (InterruptedException e) {
                // Can't happen because we check the status.
            }
        } else {
            callbacks.add(callback);
        }
    }

    public void removeCallback(Callback callback)
    {
        callbacks.remove(callback);
    }

    public boolean hasCallbacks()
    {
        return callbacks.size() > 0;
    }

    protected void onPostExecute(Bitmap result)
    {
        for (Callback cb : callbacks)
            cb.onImageReady(result);
        callbacks.clear();
    }
}
