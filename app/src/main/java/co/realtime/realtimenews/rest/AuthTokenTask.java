package co.realtime.realtimenews.rest;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import co.realtime.realtimenews.R;
import co.realtime.realtimenews.config.Config;
import co.realtime.realtimenews.storage.StorageManager;
import co.realtime.realtimenews.preferences.PreferencesManager;
import co.realtime.realtimenews.util.MD5Hash;

public class AuthTokenTask extends AsyncTask<Void, Void, String> {

    private final Context mContext;

    public AuthTokenTask(Context context) {
        mContext = context;
    }


    @Override
    protected String doInBackground(Void... params) {
        String token = "";
        HttpClient httpClient = new DefaultHttpClient();
        String mUsername = PreferencesManager.getInstance(mContext).loadUsername();
        String mPassword = PreferencesManager.getInstance(mContext).loadPassword();
        String url = String.format(Config.CODEHOSTING_STORAGE_URL,Config.APPKEY, mUsername, MD5Hash.generate(mPassword), Config.ROLE);
        HttpPost httpPost = new HttpPost(url);

        try {
            HttpResponse response = httpClient.execute(httpPost);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200){
                HttpEntity entity = response.getEntity();
                String retSrc = EntityUtils.toString(entity);
                JSONObject result = new JSONObject(retSrc);
                token = result.getString("token");
                PreferencesManager.getInstance(mContext).saveToken(token);
            }else{
                return token;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return token;
    }

    @Override
    protected void onPostExecute(final String token) {

        if (!token.isEmpty()) {
            StorageManager.getInstance(mContext).init();
        } else {
            Activity activity = (Activity) mContext;
            ProgressBar storageConnectionProgressBar = (ProgressBar) activity.findViewById(R.id.progressBar1finite);
            storageConnectionProgressBar.setVisibility(View.GONE);
            Toast.makeText(mContext,"Unable to authenticate",Toast.LENGTH_LONG).show();
        }
    }
}
