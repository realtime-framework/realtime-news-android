package co.realtime.realtimenews.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import co.realtime.realtimenews.HomeActivity;
import co.realtime.realtimenews.SplashcreenActivity;
import co.realtime.realtimenews.WebViewActivity;
import ibt.ortc.extensibility.GcmOrtcBroadcastReceiver;

public class GcmReceiver extends GcmOrtcBroadcastReceiver {

    private static final String TAG = "GcmReceiver";

    public GcmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received message");
        Bundle extras = intent.getExtras();
        if (extras != null && !HomeActivity.isInForeground() && !WebViewActivity.isInForeground()) {
              createNotification(context, extras);
        }
    }

    public void createNotification(Context context, Bundle extras)
    {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String appName = getAppName(context);

        Intent notificationIntent = new Intent(context, SplashcreenActivity.class);
        String payload = extras.getString("P");
        try {
            JSONObject payloadJsonObj = new JSONObject(payload);
            String type = payloadJsonObj.getString("Type");
            String timestamp = payloadJsonObj.getString("Timestamp");

            notificationIntent.putExtra("type",type);
            notificationIntent.putExtra("timestamp", timestamp);

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent contentIntent = PendingIntent.getActivity(context, 9999, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(context.getApplicationInfo().icon)
                            .setWhen(System.currentTimeMillis())
                            .setContentTitle(appName)
                            .setContentIntent(contentIntent)
                            .setAutoCancel(true);

            String message = extras.getString("message");
            mBuilder.setContentText(message);

            mNotificationManager.notify(appName, 9999, mBuilder.build());

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String getAppName(Context context)
    {
        CharSequence appName =
                context
                        .getPackageManager()
                        .getApplicationLabel(context.getApplicationInfo());

        return (String)appName;
    }

}
