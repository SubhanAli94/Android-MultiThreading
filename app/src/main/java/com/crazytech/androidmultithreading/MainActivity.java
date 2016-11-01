package com.crazytech.androidmultithreading;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private EditText mEditTextUrl;
    private String[] mUrlsList;
    private ListView mListView;
    private LinearLayout mLoadingSection = null;
    private ImageView mImageView;
    private String mImageUrl;
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerXML();
    }

    private void registerXML() {
        mEditTextUrl = (EditText) findViewById(R.id.main_activity_eTxt_url);
        mListView = (ListView) findViewById(R.id.main_activity_listview);
        mLoadingSection = (LinearLayout) findViewById(R.id.main_activity_linearLayout);
        mUrlsList = getResources().getStringArray(R.array.listitems);
        mImageView = (ImageView) findViewById(R.id.main_activity_imageView);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mEditTextUrl.setText(mUrlsList[i]);
        mImageUrl = mUrlsList[i];
    }

    public void downloadImage(View view) {
        Thread myThread = new Thread(new DownloadImageThread());
        myThread.start();
    }

    public boolean downloadImageUsingThread(String url) {

        boolean isSuccessful = false;
        URL downloadUrl = null;
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        File file = null;
        try {
            downloadUrl = new URL(url);
            httpURLConnection = (HttpURLConnection) downloadUrl.openConnection();
            inputStream = httpURLConnection.getInputStream();

            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsoluteFile() + "/" + Uri.parse(url).getLastPathSegment());
            Log.v(TAG, file.getAbsolutePath());
            fileOutputStream = new FileOutputStream(file);
            int read = -1;
            byte[] buffer = new byte[1024];
            while ((read = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, read);
                Log.v(TAG, " " + read);
            }

            isSuccessful = true;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }

            final File finalFile = file;
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoadingSection.setVisibility(View.GONE);

                    Bitmap bitmap = BitmapFactory.decodeFile(finalFile.getAbsolutePath());
                    mImageView.setImageBitmap(bitmap);
                }
            });

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return isSuccessful;
    }

    private class DownloadImageThread implements Runnable {

        @Override
        public void run() {

            if (isNetworkAvailable()) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingSection.setVisibility(View.VISIBLE);
                    }
                });
                downloadImageUsingThread(mImageUrl);
            } else {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Please check your internet connectivity", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
