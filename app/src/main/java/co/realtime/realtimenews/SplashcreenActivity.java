package co.realtime.realtimenews;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import co.realtime.realtimenews.interfaces.FirstMonthYearTaskCompleted;
import co.realtime.realtimenews.preferences.PreferencesManager;
import co.realtime.realtimenews.rest.FirstContentDateTask;


public class SplashcreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splashcreen);
        FirstContentDateTask task = new FirstContentDateTask(this, new FirstMonthYearTaskCompleted() {
            @Override
            public void onFirstMonthYearTaskCompleted() {
                final String type = getIntent().getStringExtra("type");
                final String timestamp = getIntent().getStringExtra("timestamp");

                int secondsDelayed = 1;
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        String token = PreferencesManager.getInstance(SplashcreenActivity.this).loadToken();
                        if (token.isEmpty()) {
                            Intent i = new Intent(SplashcreenActivity.this, LoginActivity.class);
                            if (type != null){
                                i.putExtra("type",type);
                            }
                            if(timestamp != null){
                                i.putExtra("timestamp",timestamp);
                            }
                            startActivity(i);
                        }else{
                            Intent i = new Intent(SplashcreenActivity.this, HomeActivity.class);
                            i.putExtra("doAuthentication",true);
                            if (type != null){
                                i.putExtra("type",type);
                            }
                            if(timestamp != null){
                                i.putExtra("timestamp",timestamp);
                            }
                            startActivity(i);

                        }
                        finish();
                    }
                }, secondsDelayed * 1000);
            }
        });

        task.execute();

    }
}
