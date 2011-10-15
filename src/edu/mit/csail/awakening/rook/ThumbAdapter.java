package edu.mit.csail.awakening.rook;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.IOException;

public class ThumbAdapter extends BaseAdapter
{
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
}
