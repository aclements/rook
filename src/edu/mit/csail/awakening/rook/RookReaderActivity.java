package edu.mit.csail.awakening.rook;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

public class RookReaderActivity extends Activity
{
    private static final int REQUEST_CODE_PICKED_FILE = 1;

    private static final int MENU_OPEN_ID = Menu.FIRST;

    private RookFile file;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.main);

        // XXX I think this can result in an endless loop of starting
        // the open file, then restarting this activity
        startOpenFile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_OPEN_ID, 0, R.string.menu_open);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_OPEN_ID:
            startOpenFile();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startOpenFile()
    {
        Intent intent = new Intent("org.openintents.action.PICK_FILE");

        // Set fancy title and button (optional)
        intent.putExtra("org.openintents.extra.TITLE", "Open Rook zip");
        intent.putExtra("org.openintents.extra.BUTTON_TEXT", "Open");

        try {
            startActivityForResult(intent, REQUEST_CODE_PICKED_FILE);
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
        case REQUEST_CODE_PICKED_FILE:
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
            //((ImageView)findViewById(R.id.img)).setImageBitmap(file.getPage(0));
            ((PageView)findViewById(R.id.page)).setFile(file);
        } catch (IOException e) {
            Toast.makeText(this, "Failed to open: " + e,
                           Toast.LENGTH_SHORT).show();
        }
    }
}
