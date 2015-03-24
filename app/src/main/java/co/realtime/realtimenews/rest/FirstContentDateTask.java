package co.realtime.realtimenews.rest;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import co.realtime.realtimenews.config.Config;
import co.realtime.realtimenews.interfaces.FirstMonthYearTaskCompleted;
import co.realtime.realtimenews.preferences.PreferencesManager;

public class FirstContentDateTask extends AsyncTask<Void, Void, Void> {

    private final Context mContext;
    private final FirstMonthYearTaskCompleted mListener;

    public FirstContentDateTask(Context context, FirstMonthYearTaskCompleted listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        HttpClient httpClient = new DefaultHttpClient();
        String url = String.format(Config.CODEHOSTING_FIRST_CONTENT_DATE,Config.APPKEY);
        HttpGet httpGet = new HttpGet(url);

        try {
            HttpResponse response = httpClient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200){
                HttpEntity entity = response.getEntity();
                String retSrc = EntityUtils.toString(entity);
                JSONObject result = new JSONObject(retSrc);
                String firstMonthYear = result.getString("firstMonthYear");
                PreferencesManager.getInstance(mContext).saveFirstMonthYear(firstMonthYear);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        mListener.onFirstMonthYearTaskCompleted();
    }

}
