package edu.mit.csail.awakening.rook;

import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

import java.io.InputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class RookFile
{
    private final ZipFile zip;
    private final List<ZipEntry> pages;

    public final String path;

    public RookFile(String path)
        throws IOException
    {
        this.path = path;

        // XXX See android.os.Environment for how to find files
        zip = new ZipFile(path);

        // Get entries
        List<? extends ZipEntry> ents = Collections.list(zip.entries());
        Collections.sort(ents, new Comparator<ZipEntry>() {
                public int compare(ZipEntry a, ZipEntry b)
                {
                    return a.getName().compareTo(b.getName());
                }
            });

        // Get pages
        pages = new ArrayList<ZipEntry>();
        for (ZipEntry ent : ents) {
            if (ent.getName().matches("page-[0-9]+\\.png"))
                pages.add(ent);
        }
    }

    public int getNumPages()
    {
        return pages.size();
    }

    public Bitmap getPage(int n)
        throws IOException
    {
        // XXX It might be desirable to cache these bitmaps in memory.
        // It depends how expensive this is.  See also
        // BitmapFactory.Options for how to make bitmaps purgable,
        // which would essentially let the system handle caching.
        // Probably also want to create images with a smaller
        // inPreferredConfig.
        InputStream is = zip.getInputStream(pages.get(n));
        return BitmapFactory.decodeStream(is);
    }

    public String getPageLabel(int n)
    {
        return "" + (n+1);
    }
}
