package co.realtime.realtimenews.notifications;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

import co.realtime.realtimenews.config.Config;

import ibt.ortc.api.Ortc;
import ibt.ortc.extensibility.OnConnected;
import ibt.ortc.extensibility.OnDisconnected;
import ibt.ortc.extensibility.OnException;
import ibt.ortc.extensibility.OnMessage;
import ibt.ortc.extensibility.OnSubscribed;
import ibt.ortc.extensibility.OrtcClient;
import ibt.ortc.extensibility.OrtcFactory;

public class OrtcManager {

   private static OrtcManager ortcManager;
   private Context context;

   private OrtcManager(){}

   public static OrtcManager getInstance(Context context){
       if (ortcManager == null){
           ortcManager = new OrtcManager();
           ortcManager.context = context;
       }
       return ortcManager;
   }

   public void init(){
       AuthNotificationTask task = new AuthNotificationTask(context);
       String url = String.format(Config.CODEHOSTING_NOTIFICATIONS_URL,Config.APPKEY,Config.TOKEN);
       task.execute(url);
   }

   public class AuthNotificationTask extends AsyncTask<String, Void, Boolean> {

        private final Context mContext;

        public AuthNotificationTask(Context context) {
            mContext = context;
        }


        @Override
        protected Boolean doInBackground(String... params) {
            Boolean result = false;
            String url = params[0];
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            try {
                HttpResponse response = httpClient.execute(httpPost);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200){
                    result = true;
                }else{
                    result = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Boolean result) {

            if(result){
                Ortc ortc = new Ortc();
                try {
                    OrtcFactory factory = ortc.loadOrtcFactory("IbtRealtimeSJ");
                    final OrtcClient client = factory.createClient();
                    client.setApplicationContext(mContext.getApplicationContext());
                    client.setGoogleProjectId(Config.PROJECT_ID);

                    client.onConnected = new OnConnected() {
                        @Override
                        public void run(OrtcClient ortcClient) {
                            Log.d("OrtcManager","Connected to " + ortcClient.getUrl());
                            client.subscribeWithNotifications(Config.NOTIFICATIONS_CHANNEL,true, new OnMessage() {
                                @Override
                                public void run(OrtcClient ortcClient, String s, String s2) {

                                }
                            });
                        }
                    };

                    client.onSubscribed = new OnSubscribed() {
                        @Override
                        public void run(OrtcClient ortcClient, String channel) {
                            Log.d("OrtcManager","Channel subscribed: " +  channel);
                            client.disconnect();
                        }
                    };

                    client.onDisconnected = new OnDisconnected() {
                        @Override
                        public void run(OrtcClient ortcClient) {
                            Log.d("OrtcManager","Disconnected");
                        }
                    };

                    client.onException = new OnException() {
                        @Override
                        public void run(OrtcClient ortcClient, Exception e) {
                            Log.d("OrtcManager",e.toString());
                        }
                    };

                    client.setClusterUrl(Config.CLUSTER_URL);
                    client.connect(Config.APPKEY,Config.TOKEN);

                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }


            }else {
                Toast.makeText(mContext, "Unable to authenticate for notifications", Toast.LENGTH_LONG).show();
            }
        }
    }

}


