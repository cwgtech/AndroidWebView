package com.cwgtech.unity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by colin on 2/4/18.
 */

public class MyPlugin {
    private static final MyPlugin ourInstance = new MyPlugin();

    protected static final String LOGTAG = "CWGTech";

    public static MyPlugin getInstance() {
        return ourInstance;
    }

    public static Activity mainActivity;

    public interface AlertViewCallback {
        public void onButtonTapped(int id);
    }

    public interface ShareImageCallback {
        public void onShareComplete(int result);
    }

    private long startTime;
    private LinearLayout webLayout;
    private TextView webTextView;
    private WebView webView;

    private MyPlugin() {
        Log.i(LOGTAG,"Created MyPlugin V3");
        startTime = System.currentTimeMillis();
    }

    public double getElapsedTime()
    {
        return (System.currentTimeMillis()-startTime)/1000.0f;
    }

    public void showAlertView(final String[] strings, final AlertViewCallback callback) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (strings.length < 3) {
                    Log.i(LOGTAG, "Error - expected at least 3 strings, got " + strings.length);
                    return;
                }
                DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        dialogInterface.dismiss();
                        Log.i(LOGTAG, "Tapped: " + id);
                        callback.onButtonTapped(id);
                    }
                };

                AlertDialog alertDialog = new AlertDialog.Builder(mainActivity)
                        .setTitle(strings[0])
                        .setMessage(strings[1])
                        .setCancelable(false)
                        .create();
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, strings[2], myClickListener);
                if (strings.length > 3)
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, strings[3], myClickListener);
                if (strings.length > 4)
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, strings[4], myClickListener);
                alertDialog.show();

            }
        });
    }

    public void showWebView(final String webURL, final int pixelSpace)
    {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(LOGTAG,"Want to open webview for " + webURL);
                if (webTextView==null)
                    webTextView = new TextView(mainActivity);
                webTextView.setText("");
                if (webLayout==null)
                    webLayout = new LinearLayout(mainActivity);
                webLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                mainActivity.addContentView(webLayout,layoutParams);
                if (webView==null)
                    webView = new WebView(mainActivity);
                webView.setWebViewClient(new WebViewClient());
                layoutParams.weight = 1.0f;
                webView.setLayoutParams(layoutParams);
                webView.loadUrl(webURL);
                webLayout.addView(webTextView);
                webLayout.addView(webView);
                if (pixelSpace>0)
                    webTextView.setHeight(pixelSpace);
            }
        });
    }

    public void closeWebView(final ShareImageCallback callback)
    {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (webLayout!=null)
                {
                    webLayout.removeAllViews();
                    webLayout.setVisibility(View.GONE);
                    webLayout = null;
                    webView = null;
                    webTextView = null;
                    callback.onShareComplete(1);
                }
                else
                    callback.onShareComplete(0);
            }
        });
    }

    public void shareImage(final byte[] imagePNG, final String caption, final ShareImageCallback callback)
    {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                File imageFile = new File(mainActivity.getFilesDir(),"screengrab.png");
                FileOutputStream imageStream;
                try {
                    Log.i(LOGTAG,"writing image to " + imageFile.getAbsolutePath());
                    imageStream = new FileOutputStream(imageFile);
                    imageStream.write(imagePNG);
                    imageStream.close();
                    Uri contentUri;
                    try {
                        contentUri = FileProvider.getUriForFile(mainActivity,"com.cwgtech.unity.fileprovider",imageFile);
                        if (contentUri!=null)
                        {
                            Log.i(LOGTAG,"Got Uri: " + contentUri);
                            try {
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.setDataAndType(contentUri,mainActivity.getContentResolver().getType(contentUri));
                                shareIntent.putExtra(Intent.EXTRA_STREAM,contentUri);
                                if (caption!=null)
                                    shareIntent.putExtra(Intent.EXTRA_TEXT,caption);
                                shareIntent.setClass(mainActivity,OnResultCallback.class);
                                OnResultCallback.shareImageCallback = callback;
                                mainActivity.startActivity(shareIntent);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                Log.i(LOGTAG,"error sharing intent: " + e);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(LOGTAG,"Error getting Uri: " + e);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(LOGTAG,"Error writing file: " + e);
                }
            }
        });
    }


}
