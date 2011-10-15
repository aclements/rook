package edu.mit.csail.awakening.rook;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.IOException;

public class ThumbAdapter extends BaseAdapter implements AbsListView.RecyclerListener
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
        if (convertView == null) {
            view = inflater.inflate(R.layout.thumb, parent, false);
        } else {
            view = convertView;
        }
        ImageView imageView = (ImageView)view.findViewById(R.id.image);
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

    @Override
    public void onMovedToScrapHeap(View view) 
    {
        Log.d(TAG, "Scrapping " + view);

        // We're going to replace the bitmap in this view, so there's
        // no point in keeping it around on the scrap heap.
        ImageView imageView = (ImageView)view.findViewById(R.id.image);
        imageView.setImageBitmap(null);
    }
}
