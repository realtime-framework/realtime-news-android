package co.realtime.realtimenews.networking;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.LinkedHashMap;

import co.realtime.realtimenews.R;
import co.realtime.realtimenews.domains.ContentResponse;
import co.realtime.realtimenews.preferences.PreferencesManager;
import co.realtime.realtimenews.util.ProgressWheel;

public class DownloadNewsTask extends AsyncTask<Object, Integer, Boolean> {

    private final Activity mContext;
    private final ProgressWheel mProgressWheel;
    private final ImageView mIsCached;

    public DownloadNewsTask(Activity context, ProgressWheel progressWheel, ImageView isCached) {
        mContext = context;
        mProgressWheel = progressWheel;
        mIsCached = isCached;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressWheel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel(true);
                mProgressWheel.setVisibility(View.INVISIBLE);
                mProgressWheel.setOnClickListener(null);
                mIsCached.setVisibility(View.VISIBLE);
            }
        });
        mIsCached.setVisibility(View.INVISIBLE);
        mProgressWheel.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int progressValue = values[0];
        mProgressWheel.setProgress(progressValue);
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        ContentResponse contentResponse = (ContentResponse) params[0];
        final ImageView imgView = (ImageView) params[1];
        HttpClient httpClient = new DefaultHttpClient();
        Boolean result = false;

        if(contentResponse.getUrl() != null) {
            HttpGet httpGet = new HttpGet(contentResponse.getUrl());


            try {
                publishProgress(72);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {

                }

                HttpResponse response = httpClient.execute(httpGet);
                publishProgress(144);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {

                }
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    String retSrc = EntityUtils.toString(entity);
                    contentResponse.setBody(retSrc);
                    contentResponse.setNew(false);
                    contentResponse.setUpdated(false);
                    contentResponse.setStateText("");
                    contentResponse.setStateVisibility(View.INVISIBLE);
                    LinkedHashMap<String, ContentResponse> cachedList = PreferencesManager.getInstance(mContext).loadCachedNews();

                    publishProgress(216);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {

                    }
                    if (cachedList == null) {
                        cachedList = new LinkedHashMap<String, ContentResponse>();
                    }

                    cachedList.put(contentResponse.getTimestamp(), contentResponse);

                    publishProgress(360);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }

                    if (isCancelled()) {
                        return result;
                    }
                    PreferencesManager.getInstance(mContext).saveCachedNews(cachedList);
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imgView.setImageResource(R.drawable.saved);
                            imgView.setOnClickListener(null);
                        }
                    });

                    result = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            publishProgress(180);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }

            LinkedHashMap<String, ContentResponse> cachedList = PreferencesManager.getInstance(mContext).loadCachedNews();
            if (cachedList == null) {
                cachedList = new LinkedHashMap<String, ContentResponse>();
            }

            contentResponse.setNew(false);
            contentResponse.setUpdated(false);
            contentResponse.setStateText("");
            contentResponse.setStateVisibility(View.INVISIBLE);

            cachedList.put(contentResponse.getTimestamp(), contentResponse);

            publishProgress(360);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }

            if (isCancelled()) {
                return result;
            }
            PreferencesManager.getInstance(mContext).saveCachedNews(cachedList);
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imgView.setImageResource(R.drawable.saved);
                    imgView.setOnClickListener(null);
                }
            });

            result = true;
        }

        return result;
    }

    @Override
    protected void onCancelled(Boolean aBoolean) {
        super.onCancelled(aBoolean);
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        mProgressWheel.setOnClickListener(null);
        mProgressWheel.setVisibility(View.INVISIBLE);
        mIsCached.setVisibility(View.VISIBLE);
        if (result) {
            Toast.makeText(mContext, "News Saved!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext,"Unable to save news!",Toast.LENGTH_LONG).show();
        }
    }
}
