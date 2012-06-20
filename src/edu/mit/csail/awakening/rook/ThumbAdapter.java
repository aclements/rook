package edu.mit.csail.awakening.rook;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;

public class ThumbAdapter extends BaseAdapter
    implements AbsListView.RecyclerListener,
               View.OnTouchListener,
               AdapterView.OnItemClickListener
{
    private static final String TAG = "ThumbAdapter";

    private final Context context;
    private final LayoutInflater inflater;
    private final RookFile file;
    private final int width, height;

    public ThumbAdapter(Context context, RookFile file, int width, int height)
    {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.file = file;
        this.width = width;
        this.height = height;

        renderers = new HashMap<Integer, AsyncRender>();
    }

    /*
     * View adapter
     */

    @Override
    public int getCount()
    {
        return file.getNumPages();
    }

    @Override
    public Object getItem(int position) 
    {
        return position;
    }

    @Override
    public long getItemId(int position) 
    {
        return position;
    }

    // Cache of renderers, indexed by page number.  Android tends to
    // call getView for the same page several times with different
    // Views, so this ensures we only create one renderer per page.
    private HashMap<Integer, AsyncRender> renderers;

    private static class ViewMeta
    {
        int page;
        ImageView imageView;
        TextView labelView;
        AsyncRender render;
        AsyncRender.Callback callback;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int page = position;
        Log.d(TAG, "Getting view for " + page + " (converting " + convertView + ")");

        // XXX Should this use a PageView instead of an ImageView?
        View view;
        final ViewMeta meta;
        if (convertView == null) {
            view = inflater.inflate(R.layout.thumb, parent, false);
            meta = new ViewMeta();
            view.setTag(meta);
            meta.imageView = (ImageView)view.findViewById(R.id.image);
            meta.labelView = (TextView)view.findViewById(R.id.label);
        } else {
            view = convertView;
            meta = (ViewMeta)view.getTag();
            // Android tends to ask for the same position repeatedly,
            // passing in the view we just returned (and not
            // scrapping); avoid redoing our work when this happens.
            if (meta.page == page) {
                Log.d(TAG, "Reusing image");
                return view;
            }
        }

        meta.page = page;
        meta.labelView.setText(file.getPage(page).getLabel());

        // Make sure the image view is the right size while we go off
        // to load the real bitmap
        // XXX Should be actual page dimensions, once we're honoring
        // the aspect ratio
        meta.imageView.setMinimumWidth(width);
        meta.imageView.setMinimumHeight(height);

        AsyncRender r;
        if (renderers.containsKey(page)) {
            // We already have a renderer for this page
            Log.d(TAG, "Using existing renderer for " + page);
            r = renderers.get(page);
        } else {
            // Start a new renderer
            Log.d(TAG, "Starting renderer for " + page);
            // XXX The order these load in is really annoying, but maybe
            // caching will make that irrelevant
            r = new AsyncRender(file, position, width, height);
            r.execute();
            renderers.put(page, r);
        }

        meta.render = r;
        meta.callback = new AsyncRender.Callback() {
                public void onImageReady(Bitmap img)
                {
                    meta.imageView.setImageBitmap(img);
                    meta.callback = null;
                }
            };
        r.addCallback(meta.callback);

        return view;
    }

    /*
     * Recycle listener
     */

    @Override
    public void onMovedToScrapHeap(View view) 
    {
        ViewMeta meta = (ViewMeta)view.getTag();
        Log.d(TAG, "Scrapping page " + meta.page + " (" + view + ")");

        // Stop the async renderer if we have one
        if (meta.render != null) {
            if (meta.callback != null)
                meta.render.removeCallback(meta.callback);
            if (!meta.render.hasCallbacks()) {
                Log.d(TAG, "Stopping async render " + meta.page);
                meta.render.cancel(true);
                renderers.remove(meta.page);
            }
        }
        meta.render = null;
        meta.callback = null;

        // Mark this view as scrapped.
        meta.page = -1;

        // We're going to replace the bitmap in this view, so there's
        // no point in keeping it around on the scrap heap.
        meta.imageView.setImageBitmap(null);
    }

    /*
     * Touch handling
     */

    private OnSelectListener onSelectListener;
    private int touchX, touchY;

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            touchX = (int)event.getRawX();
            touchY = (int)event.getRawY();
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int page = position;

        if (!parent.isInTouchMode()) {
            onSelectListener.onSelect(page, -1, -1);
        } else {
            // Compute where our recorded touch event lies within the
            // image.
            ImageView imageView = ((ViewMeta)view.getTag()).imageView;
            int[] location = new int[2];
            imageView.getLocationOnScreen(location);
            Log.d(TAG, "onItemClick touch " + touchX + "," + touchY +
                  " image " + location[0] + "," + location[1] +
                  " " + imageView.getWidth() + "x" + imageView.getHeight());
            float x = (float)(touchX - location[0]) / imageView.getWidth(),
                y = (float)(touchY - location[1]) / imageView.getHeight();
            x = Math.max(0, Math.min(1, x));
            y = Math.max(0, Math.min(1, y));
            onSelectListener.onSelect(page, x, y);
        }
    }

    public interface OnSelectListener
    {
        boolean onSelect(int page, float x, float y);
    }

    public void setOnSelectListener(OnSelectListener l)
    {
        onSelectListener = l;
    }
}
