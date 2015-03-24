package co.realtime.realtimenews.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;

import co.realtime.realtimenews.config.Config;
import co.realtime.realtimenews.domains.ContentResponse;

public class PreferencesManager {

	private SharedPreferences settings;

    public String TOKEN = "TOKEN";
    public String USERNAME = "USERNAME";
    public String PASSWORD = "PASSWORD";
    public String CACHED_NEWS = "CACHED_NEWS";
    public String FIRST_MONTH_YEAR = "FIRST_MONTH_YEAR";

	private static PreferencesManager preferencesManagerManager;
	
	private PreferencesManager(SharedPreferences sp) {
		settings = sp;
	}

	public static PreferencesManager getInstance(Context ctx) {
		if (preferencesManagerManager == null) {
		    preferencesManagerManager = new PreferencesManager(PreferenceManager.getDefaultSharedPreferences(ctx));
		}
		return preferencesManagerManager;
	}

    public String loadUsername(){
        return settings.getString(USERNAME,"");
    }

    public void saveUsername(String username){
        SharedPreferences.Editor e = settings.edit();
        e.putString(USERNAME, username);
        e.apply();
    }

    public String loadPassword(){
        return settings.getString(PASSWORD,"");
    }

    public void savePassword(String password){
        SharedPreferences.Editor e = settings.edit();
        e.putString(PASSWORD, password);
        e.apply();
    }

    public String loadToken(){
        return settings.getString(TOKEN, "");
    }

    public void saveToken(String token){
        SharedPreferences.Editor e = settings.edit();
        e.putString(TOKEN, token);
        e.apply();
    }

    public void saveCachedNews(LinkedHashMap<String, ContentResponse> contentResponsesList){
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedHashMap<String, ContentResponse>>(){}.getType();
        String contentList= gson.toJson(contentResponsesList,type);
        SharedPreferences.Editor e = settings.edit();
        e.putString(CACHED_NEWS, contentList);
        e.apply();
    }

    public LinkedHashMap<String, ContentResponse> loadCachedNews(){
        Gson gson = new Gson();
        Type typeToken = new TypeToken<LinkedHashMap<String, ContentResponse>>(){}.getType();
        String data = settings.getString(CACHED_NEWS, "");
        return gson.fromJson(data, typeToken);
    }

    public void saveFirstMonthYear(String firstMonthYear){
        SharedPreferences.Editor e = settings.edit();
        e.putString(FIRST_MONTH_YEAR, firstMonthYear);
        e.apply();
    }

    public String loadFirstMonthYear(){
        return settings.getString(FIRST_MONTH_YEAR, Config.DEFAULT_FIRST_CONTENT_DATE);
    }

}
