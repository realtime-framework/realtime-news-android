package co.realtime.realtimenews.storage;

import co.realtime.realtimenews.R;
import co.realtime.realtimenews.config.Config;
import co.realtime.realtimenews.domains.ContentResponse;
import co.realtime.realtimenews.domains.TagResponse;
import co.realtime.realtimenews.interfaces.OnMoreDataLoaded;
import co.realtime.realtimenews.notifications.OrtcManager;
import co.realtime.realtimenews.preferences.PreferencesManager;
import co.realtime.realtimenews.util.DateHelper;
import co.realtime.storage.ItemAttribute;
import co.realtime.storage.ItemSnapshot;
import co.realtime.storage.StorageRef;
import co.realtime.storage.TableRef;
import co.realtime.storage.ext.OnConnected;
import co.realtime.storage.ext.OnError;
import co.realtime.storage.ext.OnItemSnapshot;
import co.realtime.storage.ext.OnReconnected;
import co.realtime.storage.ext.OnReconnecting;
import co.realtime.storage.ext.StorageException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class StorageManager {

    private static StorageManager storageManager;

    private Context context;
    private StorageRef storageRef;
    private ArrayList<ContentResponse> contentsList;
    private boolean isConnected;
    private boolean reconnecting;

    final public static String STORAGE_TAG = "STORAGE";

    private  StorageManager(){

    }

    public static StorageManager getInstance(Context context){
        if (storageManager == null){
            storageManager = new StorageManager();
            storageManager.context = context;
        }
        return storageManager;
    }

    public void init(){
        String token = PreferencesManager.getInstance(context).loadToken();
        try {
            storageRef = new StorageRef(Config.APPKEY, token);
            storageRef.onConnected(new OnConnected() {
                @Override
                public void run(StorageRef storageRef) {
                    Log.d(STORAGE_TAG,"Connected to storage");
                    isConnected = true;
                    sendBroadCast(Config.STORAGE_CONNECTED);
                    getTags();
                }
            });

            storageRef.onReconnecting(new OnReconnecting() {
                @Override
                public void run(StorageRef storageRef) {
                    Log.d(STORAGE_TAG,"Reconnecting...");
                    isConnected = false;
                    if (!reconnecting) {
                        reconnecting = true;
                        sendBroadCast(Config.STORAGE_RECONNECTING);
                        loadOfflineData();
                    }
                }
            });

            storageRef.onReconnected(new OnReconnected() {
                @Override
                public void run(StorageRef storageRef) {
                    Log.d(STORAGE_TAG,"Reconnected...");
                    isConnected = true;
                    reconnecting = false;
                    sendBroadCast(Config.STORAGE_RECONNECTED);
                    getContents();
                }
            });

        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

    private void loadOfflineData(){
        LinkedHashMap<String, ContentResponse> linkedHashMap = PreferencesManager.getInstance(context).loadCachedNews();
        if (linkedHashMap != null && linkedHashMap.size() > 0 ){
            contentsList = new ArrayList<ContentResponse>();
            for (Map.Entry<String, ContentResponse> entry : linkedHashMap.entrySet()) {
                ContentResponse value = entry.getValue();
                if(PreferencesManager.getInstance(context).loadCachedNews() != null && PreferencesManager.getInstance(context).loadCachedNews().containsKey(value.getTimestamp())){
                    value.setSaveBtnResource((R.drawable.saved));
                    value.setClickable(false);
                }else{
                    value.setSaveBtnResource((R.drawable.download));
                    value.setClickable(true);
                }
                contentsList.add(value);
            }
            sendBroadCast(Config.STORAGE_REFRESH);
        }else{
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "You have no offline records", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void getTags(){
        final ArrayList<LinkedHashMap<String,ItemAttribute>> tagsItems = new ArrayList<LinkedHashMap<String, ItemAttribute>>();

        TableRef tableRef = storageRef.table(Config.TABLE_TAGS);
        tableRef.getItems(new OnItemSnapshot() {
            @Override
            public void run(ItemSnapshot itemSnapshot) {
                if (itemSnapshot != null) {
                    Log.d(STORAGE_TAG, String.format("Item retrieved: %s", itemSnapshot.val()));
                    tagsItems.add(itemSnapshot.val());
                } else {
                    Log.d(STORAGE_TAG, "No more items in table");

                    ArrayList<TagResponse> tagsList = new ArrayList<TagResponse>();
                    for (int i = 0; i < tagsItems.size(); i++) {
                        String tag = tagsItems.get(i).get(Config.ITEM_PROPERTY_TAG).toString();
                        String type = tagsItems.get(i).get(Config.ITEM_PROPERTY_TYPE).toString();
                        TagResponse tagResponse = new TagResponse();
                        tagResponse.setTag(tag);
                        tagResponse.setType(type);
                        tagsList.add(tagResponse);
                    }

                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<TagResponse>>() {}.getType();
                    String tagsListJson = gson.toJson(tagsList, type);
                    getContentsInit(tagsListJson);

                }
            }
        }, new OnError() {
            @Override
            public void run(Integer integer, String s) {
                Log.e(STORAGE_TAG, s);
            }
        });
    }

    private void getContentsInit(final String tagsListJson){
        contentsList = new ArrayList<ContentResponse>();
        getContentsByMonthYearInit(DateHelper.getCurrentDate(), tagsListJson);
    }

    private void getContentsByMonthYearInit(final String currentDate, final String tagsListJson){
        final TableRef tableRef = storageRef.table(Config.TABLE_CONTENTS);
        long itemsMaxLimit = Config.ITEMS_MAX - contentsList.size();
        tableRef.desc().limit(itemsMaxLimit).equals(Config.ITEM_PROPERTY_MONTHYEAR, new ItemAttribute(currentDate)).getItems(new OnItemSnapshot() {
            @Override
            public void run(ItemSnapshot itemSnapshot) {
                if (itemSnapshot != null) {
                    Log.d(STORAGE_TAG, String.format("Item retrieved: %s", itemSnapshot.val()));
                    ContentResponse contentResponse = buildContentResponseFromSnapshot(itemSnapshot);
                    if (PreferencesManager.getInstance(context).loadCachedNews() != null && PreferencesManager.getInstance(context).loadCachedNews().containsKey(contentResponse.getTimestamp())) {
                        contentResponse.setSaveBtnResource((R.drawable.saved));
                        contentResponse.setClickable(false);
                    } else {
                        contentResponse.setSaveBtnResource((R.drawable.download));
                        contentResponse.setClickable(true);
                    }

                    contentsList.add(contentResponse);
                } else {
                    Log.d(STORAGE_TAG, "No more items in table");
                    if (contentsList.size() >= Config.ITEMS_MAX || DateHelper.isDateBeyondLimit(context, currentDate)) {
                        Intent intent = new Intent(Config.BROADCAST_EVENT);
                        intent.putExtra(Config.BROADCAST_MESSAGE, Config.STORAGE_INIT);
                        intent.putExtra("tagsList", tagsListJson);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        startEventsListener(tableRef);
                    } else {
                        getContentsByMonthYearInit(DateHelper.getPreviousMonth(currentDate), tagsListJson);
                    }
                }
            }
        }, new OnError() {
            @Override
            public void run(Integer integer, String s) {
                Log.e(STORAGE_TAG, s);
            }
        });
    }

    private void getContentsByMonthYear(final String currentDate){
        TableRef tableRef = storageRef.table(Config.TABLE_CONTENTS);
        long itemsMaxLimit = Config.ITEMS_MAX - contentsList.size();
        tableRef.desc().limit(itemsMaxLimit).equals(Config.ITEM_PROPERTY_MONTHYEAR, new ItemAttribute(currentDate)).getItems(new OnItemSnapshot() {
            @Override
            public void run(ItemSnapshot itemSnapshot) {
                if (itemSnapshot != null) {
                    //Log.d(STORAGE_TAG,String.format("Item retrieved: %s", itemSnapshot.val()));
                    ContentResponse contentResponse = buildContentResponseFromSnapshot(itemSnapshot);
                    if (PreferencesManager.getInstance(context).loadCachedNews() != null && PreferencesManager.getInstance(context).loadCachedNews().containsKey(contentResponse.getTimestamp())) {
                        contentResponse.setSaveBtnResource((R.drawable.saved));
                        contentResponse.setClickable(false);
                    } else {
                        contentResponse.setSaveBtnResource((R.drawable.download));
                        contentResponse.setClickable(true);
                    }

                    contentsList.add(contentResponse);
                } else {
                    Log.d(STORAGE_TAG, "No more items in table");
                    if (contentsList.size() >= Config.ITEMS_MAX || DateHelper.isDateBeyondLimit(context, currentDate)) {
                        sendBroadCast(Config.STORAGE_REFRESH_RECONNECTED);
                    } else {
                        getContentsByMonthYear(DateHelper.getPreviousMonth(currentDate));
                    }
                }
            }
        }, new OnError() {
            @Override
            public void run(Integer integer, String s) {
                Log.e(STORAGE_TAG, s);
            }
        });
    }

    private void getContents(){
        contentsList = new ArrayList<ContentResponse>();
        getContentsByMonthYear(DateHelper.getCurrentDate());
    }

    public void loadMoreData(final String lastMonthYear, final String lastTimestamp, final OnMoreDataLoaded callback){
        loadMoreData(new ArrayList<ContentResponse>(),lastMonthYear,lastTimestamp,callback);

    }

    private void loadMoreData(final ArrayList<ContentResponse> loadMoreDataList, final String lastMonthYear, final String lastTimestamp, final OnMoreDataLoaded callback){

        TableRef tableRef = storageRef.table(Config.TABLE_CONTENTS);
        tableRef.desc().limit(Config.ITEMS_MAX).equals(Config.ITEM_PROPERTY_MONTHYEAR, new ItemAttribute(lastMonthYear)).lessThan(Config.ITEM_PROPERTY_TIMESTAMP, new ItemAttribute(lastTimestamp)).getItems(new OnItemSnapshot() {
            @Override
            public void run(ItemSnapshot itemSnapshot) {
                if (itemSnapshot != null) {
                    //Log.d(STORAGE_TAG,String.format("Item retrieved: %s", itemSnapshot.val()));
                    ContentResponse contentResponse = buildContentResponseFromSnapshot(itemSnapshot);
                    if (PreferencesManager.getInstance(context).loadCachedNews() != null && PreferencesManager.getInstance(context).loadCachedNews().containsKey(contentResponse.getTimestamp())) {
                        contentResponse.setSaveBtnResource((R.drawable.saved));
                        contentResponse.setClickable(false);
                    } else {
                        contentResponse.setSaveBtnResource((R.drawable.download));
                        contentResponse.setClickable(true);
                    }

                    loadMoreDataList.add(contentResponse);
                } else {
                    Log.d(STORAGE_TAG, "No more items in table");
                    if (loadMoreDataList.size() >= Config.ITEMS_MAX || DateHelper.isDateBeyondLimit(context, lastMonthYear)) {
                        callback.onMoreDataLoaded(loadMoreDataList);
                    } else {
                        loadMoreData(loadMoreDataList,DateHelper.getPreviousMonth(lastMonthYear),lastTimestamp,callback);
                    }
                }
            }
        }, new OnError() {
            @Override
            public void run(Integer integer, String s) {
                Log.e(STORAGE_TAG, s);
            }
        });
    }

    private void startEventsListener(TableRef tableRef){
        // Be notified in realtime when an item is updated in SomeTable
        tableRef.on(StorageRef.StorageEvent.UPDATE, new OnItemSnapshot(){
            @Override
            public void run(ItemSnapshot itemSnapshot) {
                if (itemSnapshot != null) {
                    Log.d(STORAGE_TAG, String.format("Item updated: %s", itemSnapshot.val()));
                    ContentResponse updatedContent = buildContentResponseFromSnapshot(itemSnapshot);
                    checkNewOrUpdated(updatedContent);
                }
            }
        });

        tableRef.on(StorageRef.StorageEvent.DELETE, new OnItemSnapshot() {
            @Override
            public void run(ItemSnapshot itemSnapshot) {
                if (itemSnapshot != null){
                    ContentResponse deletedContent = buildContentResponseFromSnapshot(itemSnapshot);
                    checkDeleted(deletedContent);
                }
            }
        });

        OrtcManager.getInstance(context).init();
    }

    private void checkNewOrUpdated(ContentResponse newContent){
        for (ContentResponse c: contentsList){
            if (c.getTimestamp().equalsIgnoreCase(newContent.getTimestamp())){
                newContent.setUpdated(true);
                newContent.setStateVisibility(View.VISIBLE);
                newContent.setStateText(context.getString(R.string.updateStr));
                contentsList.remove(c);
                if(PreferencesManager.getInstance(context).loadCachedNews() != null && PreferencesManager.getInstance(context).loadCachedNews().containsKey(newContent.getTimestamp())){
                    newContent.setSaveBtnResource((R.drawable.saved));
                    newContent.setClickable(false);
                }
                else{
                    newContent.setSaveBtnResource((R.drawable.download));
                    newContent.setClickable(true);
                }
                contentsList.add(0,newContent);
                //updateCache(newContent);
                sendBroadCast(Config.STORAGE_UPDATE, newContent.getType(), newContent.getTag());
                return;
            }
        }

        newContent.setNew(true);
        newContent.setStateVisibility(View.VISIBLE);
        newContent.setStateText(context.getString(R.string.newStr));
        if(PreferencesManager.getInstance(context).loadCachedNews() != null && PreferencesManager.getInstance(context).loadCachedNews().containsKey(newContent.getTimestamp())){
            newContent.setSaveBtnResource((R.drawable.saved));
            newContent.setClickable(false);
        }
        else{
            newContent.setSaveBtnResource((R.drawable.download));
            newContent.setClickable(true);
        }
        contentsList.add(0,newContent);
        sendBroadCast(Config.STORAGE_UPDATE, newContent.getType(),newContent.getTag());
    }

    private void checkDeleted(ContentResponse deletedContent){
        for (ContentResponse c: contentsList){
            if (c.getTimestamp().equalsIgnoreCase(deletedContent.getTimestamp())){
                contentsList.remove(c);
                //removefromCache(c);
                sendBroadCast(Config.STORAGE_DELETE, c.getType(),c.getTag());
                return;
            }
        }
    }

    private void updateCache(ContentResponse updatedContent){
        if(PreferencesManager.getInstance(context).loadCachedNews() != null && PreferencesManager.getInstance(context).loadCachedNews().containsKey(updatedContent.getTimestamp())){
            LinkedHashMap<String, ContentResponse> cachedNews = PreferencesManager.getInstance(context).loadCachedNews();
            cachedNews.put(updatedContent.getTimestamp(),updatedContent);
            PreferencesManager.getInstance(context).saveCachedNews(cachedNews);
        }
    }

    private void removefromCache(ContentResponse deletedContent){
        if(PreferencesManager.getInstance(context).loadCachedNews() != null && PreferencesManager.getInstance(context).loadCachedNews().containsKey(deletedContent.getTimestamp())){
            LinkedHashMap<String, ContentResponse> cachedNews = PreferencesManager.getInstance(context).loadCachedNews();
            cachedNews.remove(deletedContent.getTimestamp());
            PreferencesManager.getInstance(context).saveCachedNews(cachedNews);
        }
    }

    private ContentResponse buildContentResponseFromSnapshot(ItemSnapshot itemSnapshot){

        ContentResponse contentResponse = new ContentResponse();

        if(itemSnapshot.val().containsKey(Config.ITEM_PROPERTY_MONTHYEAR)) {
            String monthYear = itemSnapshot.val().get(Config.ITEM_PROPERTY_MONTHYEAR).toString();
            contentResponse.setMonthYear(monthYear);
        }

        String type = itemSnapshot.val().get(Config.ITEM_PROPERTY_TYPE).toString();

        if (itemSnapshot.val().containsKey(Config.ITEM_PROPERTY_URL)) {
            String url = itemSnapshot.val().get(Config.ITEM_PROPERTY_URL).toString();
            contentResponse.setUrl(url);
        }

        String tag = itemSnapshot.val().get(Config.ITEM_PROPERTY_TAG).toString();
        String title = itemSnapshot.val().get(Config.ITEM_PROPERTY_TITLE).toString();
        String img = itemSnapshot.val().get(Config.ITEM_PROPERTY_IMG).toString();
        String description = itemSnapshot.val().get(Config.ITEM_PROPERTY_DESCRIPTION).toString();
        String timestamp = itemSnapshot.val().get(Config.ITEM_PROPERTY_TIMESTAMP).toString();

        if (itemSnapshot.val().containsKey(Config.ITEM_PROPERTY_BODY)) {
            String body = itemSnapshot.val().get(Config.ITEM_PROPERTY_BODY).toString();
            contentResponse.setBody(body);
        }

        contentResponse.setType(type);
        contentResponse.setTag(tag);
        contentResponse.setTitle(title);
        contentResponse.setImg(img);
        contentResponse.setDescription(description);
        contentResponse.setTimestamp(timestamp);

        String date = DateHelper.convertTimestampToDate(timestamp);
        contentResponse.setTimestampText(date);


        return contentResponse;
    }

    private void sendBroadCast(int event, String type, String tag){
        Intent intent = new Intent(Config.BROADCAST_EVENT);
        intent.putExtra(Config.BROADCAST_MESSAGE, event);
        intent.putExtra("type",type);
        intent.putExtra("tag",tag);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void sendBroadCast(int event){
        Intent intent = new Intent(Config.BROADCAST_EVENT);
        intent.putExtra(Config.BROADCAST_MESSAGE, event);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public ArrayList<ContentResponse> getContentsList() {
        return contentsList;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
