package edu.mit.csail.awakening.rook;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.IOException;

public class RookReaderActivity extends Activity
{
    private RookFile file;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.main);

        try {
            file = new RookFile();
            //((ImageView)findViewById(R.id.img)).setImageBitmap(file.getPage(0));
            ((PageView)findViewById(R.id.page)).setFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
