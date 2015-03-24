package co.realtime.realtimenews.util;

import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import co.realtime.realtimenews.config.Config;
import co.realtime.realtimenews.preferences.PreferencesManager;

public class DateHelper {

    private final static DateTimeFormatter dtf = DateTimeFormat.forPattern(Config.MONTHYEAR_DATE_FORMAT);

    public static String convertTimestampToDate(String timestamp){
        Long currentTime = Long.valueOf(timestamp);
        DateTime dateTime = new DateTime(currentTime);
        return dateTime.toString(Config.DATE_FORMAT);
    }

    public static String getCurrentDate(){
        return new DateTime().toString(Config.MONTHYEAR_DATE_FORMAT);
    }

    public static String getPreviousMonth(String currentDate){
        DateTime dt = dtf.parseDateTime(currentDate);
        return dt.minusMonths(1).toString(Config.MONTHYEAR_DATE_FORMAT);
    }

    public static boolean isDateBeyondLimit(Context ctx, String currentDate){
        DateTime newDate = dtf.parseDateTime(currentDate);
        String maxDate = PreferencesManager.getInstance(ctx).loadFirstMonthYear();
        DateTime dateLimit = dtf.parseDateTime(maxDate);
        return newDate.getMillis() <= dateLimit.getMillis();
    }

}
