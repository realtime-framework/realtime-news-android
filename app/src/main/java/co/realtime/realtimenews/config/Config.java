package co.realtime.realtimenews.config;

public class Config {

	final public static String TOKEN = "YOUR_TOKEN";
	final public static String APPKEY = "YOUR_APPLICATION_KEY";
    final public static String PROJECT_ID = "YOUR_GOOGLE_PROJECT_NUMBER";

    final public static String TABLE_TAGS = "Tags";
    final public static String TABLE_CONTENTS = "Contents";

    final public static String ITEM_PROPERTY_MONTHYEAR= "MonthYear";
    final public static String ITEM_PROPERTY_TYPE= "Type";
    final public static String ITEM_PROPERTY_URL= "URL";
    final public static String ITEM_PROPERTY_TAG= "Tag";
    final public static String ITEM_PROPERTY_TITLE= "Title";
    final public static String ITEM_PROPERTY_IMG= "IMG";
    final public static String ITEM_PROPERTY_DESCRIPTION= "Description";
    final public static String ITEM_PROPERTY_TIMESTAMP= "Timestamp";
    final public static String ITEM_PROPERTY_BODY= "Body";

    final public static String BROADCAST_MESSAGE = "message";
    final public static String BROADCAST_EVENT = "realtime-storage-event";

    final public static int STORAGE_CONNECTED = 0;
    final public static int STORAGE_INIT = 1;
    final public static int STORAGE_REFRESH = 2;
    final public static int STORAGE_RECONNECTING = 3;
    final public static int STORAGE_RECONNECTED = 4;
    final public static int STORAGE_UPDATE = 5;
    final public static int STORAGE_DELETE = 6;
    final public static int STORAGE_REFRESH_RECONNECTED = 7;
    final public static long ITEMS_MAX = 5;

    final public static String DATE_FORMAT = "dd MMM yyyy HH:mm";
    final public static String MONTHYEAR_DATE_FORMAT = "MM/yyyy";
    final public static String DEFAULT_FIRST_CONTENT_DATE = "01/2015";

    final public static String CODEHOSTING_STORAGE_URL = "https://codehosting.realtime.co/%s/authenticate?user=%s&password=%s&role=%s";
    final public static String CODEHOSTING_NOTIFICATIONS_URL = "https://codehosting.realtime.co/%s/saveAuthentication?token=%s";
    final public static String CODEHOSTING_FIRST_CONTENT_DATE = "https://codehosting.realtime.co/%s/firstMonthYear";
    final public static String CLUSTER_URL = "http://ortc-developers.realtime.co/server/2.1/";

    final public static String ROLE = "iOSApp";

    final public static String NOTIFICATIONS_CHANNEL = "notifications";

}
