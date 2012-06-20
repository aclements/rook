package edu.mit.csail.awakening.rook;

import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

import java.io.InputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class RookFile
{
    public class Page
    {
        private final String label;
        private final ZipEntry ent;
        private final int w, h;

        private Page(int pagenum, ZipEntry ent, int w, int h)
        {
            this.label = "" + pagenum;
            this.ent = ent;
            this.w = w;
            this.h = h;
        }

        public Bitmap render()
            throws IOException
        {
            // XXX It might be desirable to cache these bitmaps in memory.
            // It depends how expensive this is.  See also
            // BitmapFactory.Options for how to make bitmaps purgable,
            // which would essentially let the system handle caching.
            // Probably also want to create images with a smaller
            // inPreferredConfig.
            synchronized (zip) {
                InputStream is = zip.getInputStream(ent);
                return BitmapFactory.decodeStream(is);
            }
        }

        public String getLabel()
        {
            return label;
        }
    }

    private final ZipFile zip;
    private final ArrayList<Page> pages;

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

        // Prepare to get page dimensions
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTempStorage = new byte[16*1024];
        opts.inJustDecodeBounds = true;

        // Get pages
        // XXX This could take a while.  This should be done
        // asynchronously in any case.  Perhaps this information
        // should be in a metadata file, or we should cache the
        // results.
        int pageno = 1;
        pages = new ArrayList<Page>();
        for (ZipEntry ent : ents) {
            if (ent.getName().matches("page-[0-9]+\\.png")) {
                InputStream is = zip.getInputStream(ent);
                BitmapFactory.decodeStream(is, null, opts);
                pages.add(new Page(pageno, ent, opts.outWidth, opts.outHeight));
                pageno++;
            }
        }

        if (pages.size() == 0)
            throw new IOException("Not a Rook file");
    }

    public int getNumPages()
    {
        return pages.size();
    }

    public Page getPage(int n)
    {
        return pages.get(n);
    }
}
