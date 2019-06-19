package education.mahmoud.quranyapp.Util;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateOperation {

    private static final String TAG = "DateOperation";

    public static String getStringDate(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        StringBuilder stringBuilder = new StringBuilder();
        calendar.setTime(date);
        stringBuilder.append(calendar.get(Calendar.DAY_OF_MONTH));
        stringBuilder.append("-");
        stringBuilder.append(calendar.get(Calendar.MONTH) + 1); //here add 1 to display ( Jan is 0 )
        stringBuilder.append("-");
        stringBuilder.append(calendar.get(Calendar.YEAR));

        return stringBuilder.toString();
    }

    public static String getStringDate(long milis) {
        Date date = new Date();
        date.setTime(milis);
        return getStringDate(date);
    }


    public static Date getCurrentDate() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        int year = getCurrentYear();
        int month = getCurrentMonth();

        Log.d(TAG, "getCurrentDate: " + month);

        int day = getCurrentDay();
        calendar.set(year, month, day);

        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MILLISECOND, 0);


        return calendar.getTime();
    }

    public static String getCurrentDateAsString() {
        return getStringDate(getCurrentDate());
    }
    /**
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static Date getSpecificDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        //as month in calender start with 0
        calendar.set(year, month - 1, day);
        //to sure same day date and easy date query in database
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }


    public static int getCurrentYear() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        return calendar.get(Calendar.YEAR);
    }

    public static int getCurrentMonth() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        return calendar.get(Calendar.MONTH) + 1;
    }

    public static int getCurrentDay() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        return calendar.get(Calendar.DAY_OF_MONTH);
    }


    public static int getYear(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    public static int getMonth(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) + 1;
    }

    public static int getDay(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static int[] getYesterDayMetric(Date date) {
        int ints[] = new int[3];
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        ints[0] = getDay(calendar.getTime());
        ints[1] = getMonth(calendar.getTime());
        ints[2] = getYear(calendar.getTime());
        return ints;
    }

    public static Date getYesterday(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTime();
    }

    public static int[] getPreviousWeekMetric(Date date) {
        int ints[] = new int[3];
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        ints[0] = getDay(calendar.getTime());
        ints[1] = getMonth(calendar.getTime());
        ints[2] = getYear(calendar.getTime());

        return ints;
    }


    public static int[] getPrevMonthMetric(Date date) {
        int[] metric = new int[2];
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -1);
        metric[0] = getMonth(calendar.getTime());
        metric[1] = getYear(calendar.getTime());

        return metric;
    }
}
