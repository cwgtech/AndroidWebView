package com.cwgtech.unity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class OnResultCallback extends Activity {

    public static final String LOGTAG = MyPlugin.LOGTAG + "_OnResult";
    public static MyPlugin.ShareImageCallback shareImageCallback;
    String caption;
    Uri imageUri;

    void myFinish(int result)
    {
        if (shareImageCallback!=null)
            shareImageCallback.onShareComplete(result);
        shareImageCallback = null;
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOGTAG, "onCreateBundle");
        Intent intent = getIntent();
        if (intent != null) {
            caption = intent.getStringExtra(Intent.EXTRA_TEXT);
            imageUri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
            Log.i(LOGTAG, "Uri: " + imageUri);
        }
        if (intent == null || imageUri == null) {
            myFinish(1);
            return;
        }

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setDataAndType(intent.getData(), intent.getType());
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            if (caption != null)
                shareIntent.putExtra(Intent.EXTRA_TEXT, caption);
            startActivityForResult(Intent.createChooser(shareIntent, "share with..."), 1);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(LOGTAG, "Error: " + e.getLocalizedMessage());
            myFinish(2);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(LOGTAG,"onActivityResult: " + requestCode + ", " + resultCode + ", " + data);
        myFinish(resultCode);
    }
}
