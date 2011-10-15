package edu.mit.csail.awakening.rook;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import java.io.IOException;

public class ThumbAdapter extends BaseAdapter
{
    private final Context context;
    private final RookFile file;
    private final int width, height;

    public ThumbAdapter(Context context, RookFile file, int width, int height)
    {
        this.context = context;
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
        // XXX Draw a border around the image
        // XXX Put a page number under the image
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(width, height));
            imageView.setAdjustViewBounds(false);
            //            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        try {
            // XXX Scaling should be generic and cached.  Maybe it
            // should be part of RookFile, since the format might be
            // able to provide scaled images another way.
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
        return imageView;
    }
}
