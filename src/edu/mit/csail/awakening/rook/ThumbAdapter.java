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
import java.io.IOException;

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "Getting view for " + position + " (converting " + convertView + ")");

        View view;
        ImageView imageView;
        if (convertView == null) {
            view = inflater.inflate(R.layout.thumb, parent, false);
            imageView = (ImageView)view.findViewById(R.id.image);
            view.setTag(imageView);
        } else {
            view = convertView;
            imageView = (ImageView)view.getTag();
        }
        TextView labelView = (TextView)view.findViewById(R.id.label);

        labelView.setText(file.getPageLabel(position));

        try {
            // XXX Scaling should be generic and cached.  Maybe it
            // should be part of RookFile, since the format might be
            // able to provide scaled images another way.
            // XXX Resizing should be done in a background thread
            Bitmap page = file.getPage(position);
            // XXX Aspect ratio
            int w = width, h = height;
            // if (page.getWidth() > page.getHeight()) {
            //     w = width;
            //     h = page.getHeight() * w / page.getWidth();
            // } else {
            //     h = width;
            //     w = page.getWidth() * h / page.getHeight();
            // }
            Bitmap scaled = page.createScaledBitmap(page, w, h, true);
            imageView.setImageBitmap(scaled);
        } catch (IOException e) {
            // XXX
        }

        return view;
    }

    /*
     * Recycle listener
     */

    @Override
    public void onMovedToScrapHeap(View view) 
    {
        Log.d(TAG, "Scrapping " + view);

        // We're going to replace the bitmap in this view, so there's
        // no point in keeping it around on the scrap heap.
        ImageView imageView = (ImageView)view.getTag();
        imageView.setImageBitmap(null);
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
            ImageView imageView = (ImageView)view.getTag();
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
