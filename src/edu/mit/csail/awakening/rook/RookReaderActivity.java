package edu.mit.csail.awakening.rook;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import java.io.IOException;

public class RookReaderActivity extends Activity
{
    private static final String TAG = "RookReaderActivity";

    private static final int REQUEST_CODE_PICK_FILE = 1;

    private RookFile file;

    private SharedPreferences prefs;

    private ViewAnimator views;
    private PageView pageView;
    private GridView thumbs;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        views = (ViewAnimator)findViewById(R.id.views);
        pageView = (PageView)findViewById(R.id.page);
        thumbs = (GridView)findViewById(R.id.thumbs);

        // XXX This side-bar takes away 48 pixels of critical
        // horizontal space.  Maybe only show it on a middle tap?  For
        // fast jumping, could always show a dragable scrollbar in a
        // only a few pixels of space.  When dragging, it could
        // temporarily show thumbnails at the side or even scroll the
        // whole page (probably in A2 mode).

        ImageView openButton = (ImageView)findViewById(R.id.openButton);
        openButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
               startOpenFile();
            }
        });

        ImageView thumbsButton = (ImageView)findViewById(R.id.thumbsButton);
        thumbsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                thumbs.setSelection(pageView.getPage());
                views.setDisplayedChild(1);
            }
        });

        // XXX Remember this as part of the display state
        views.setDisplayedChild(1);

        // XXX Put file in saved bundle (maybe position, but that
        // should be in the DB anyway).  Maybe call startOpenFile if
        // there is no file in the bundle?

        // XXX Remember the per-file location, too
        prefs = getPreferences(MODE_WORLD_READABLE);
        String path = prefs.getString("path", null);
        if (path == null)
            startOpenFile();
        else
            openFile(path);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        N2EpdController.setGL16Mode(2);

        SharedPreferences.Editor ed = prefs.edit();
        if (file != null) {
            ed.putString("path", file.path);
        } else {
            ed.remove("path");
        }
        ed.commit();
    }

    private void startOpenFile()
    {
        Intent intent = new Intent("org.openintents.action.PICK_FILE");

        // Set fancy title and button (optional)
        intent.putExtra("org.openintents.extra.TITLE", "Open Rook zip");
        intent.putExtra("org.openintents.extra.BUTTON_TEXT", "Open");

        try {
            startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
        } catch (ActivityNotFoundException e) {
            // No compatible file manager was found.
            Toast.makeText(this, "No file manager installed",
                           Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
        case REQUEST_CODE_PICK_FILE:
            if (resultCode == RESULT_OK && data != null) {
                Uri fileUri = data.getData();
                if (fileUri != null) {
                    String filePath = fileUri.getPath();
                    if (filePath != null) {
                        openFile(filePath);
                    }
                }
            }
            break;
        }
    }

    private void openFile(String path)
    {
        try {
            file = new RookFile(path);
        } catch (IOException e) {
            Toast.makeText(this, "Failed to open: " + e,
                           Toast.LENGTH_SHORT).show();
            startOpenFile();
            return;
        }
        //((ImageView)findViewById(R.id.img)).setImageBitmap(file.getPage(0));
        pageView.setFile(file);
        // XXX Use g.setSelection to jump view to an item
        Resources res = getResources();
        float bound = res.getDimension(R.dimen.thumb_bound);
        // XXX Use the common aspect ratio of this file.
        float pageWidth = 8.5f, pageHeight = 11;
        int thumbWidth = (int)bound, thumbHeight = (int)bound;
        if (pageWidth > pageHeight) {
            thumbHeight = (int)(pageHeight * thumbWidth / pageWidth);
        } else {
            thumbWidth = (int)(pageWidth * thumbHeight / pageHeight);
        }
        Log.d(TAG, "thumbWidth " + thumbWidth + " thumbHeight " + thumbHeight);
        thumbs.setColumnWidth(thumbWidth);
        ThumbAdapter ta = new ThumbAdapter(this, file, thumbWidth, thumbHeight);
        thumbs.setAdapter(ta);
        thumbs.setRecyclerListener(ta);
        thumbs.setOnTouchListener(ta);
        thumbs.setOnItemClickListener(ta);

        ta.setOnSelectListener(new ThumbAdapter.OnSelectListener() {
                @Override
                public boolean onSelect(int page, float x, float y)
                {
                    Log.d(TAG, "Select page " + page + " at " + x + "," + y);
                    // XXX Handle position
                    pageView.setPage(page);
                    views.setDisplayedChild(0);
                    return true;
                }
            });
    }

    private final int KEYCODE_NOOK_LEFT_TOP = 92;
    private final int KEYCODE_NOOK_LEFT_BOTTOM = 93;
    private final int KEYCODE_NOOK_RIGHT_TOP = 94;
    private final int KEYCODE_NOOK_RIGHT_BOTTOM = 95;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KEYCODE_NOOK_LEFT_TOP ||
            keyCode == KEYCODE_NOOK_RIGHT_TOP) {
            pageView.next();
            return true;
        } else if (keyCode == KEYCODE_NOOK_LEFT_BOTTOM ||
                   keyCode == KEYCODE_NOOK_RIGHT_BOTTOM) {
            pageView.prev();
            return true;
        }
        return false;
    }
}
