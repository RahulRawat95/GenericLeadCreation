package com.wings2aspirations.genericleadcreation.repository;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CalendarHelper {

    public static final int SET_FROM_DATE = 1;
    public static final int SET_TO_DATE = 2;

    private Context context;
    private Date fromDate, toDate;
    private String title, description, location, emails;
    private AlertDialog allowCalendarDialog;

    private CalendarCallback calendarCallback;

    public interface CalendarInstance {
        long getFromDate(Context context);

        long getToDate(Context context);

        String getTitle();

        String getDescription();

        String getLocation();

        String getEmails();

        String getSyncId();
    }

    public interface CalendarCallback {
        void callback(Boolean wasEventInserted, String eventInsertionString);
    }

    private boolean checkPermission() {
        try {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            return false;
        }
    }

    public CalendarHelper setContext(Context context) {
        this.context = context;
        return this;
    }

    public CalendarHelper setDates(Date fromDate, Date toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        return this;
    }

    public CalendarHelper setDates(Calendar fromDate, Calendar toDate) {
        this.fromDate = new Date(fromDate.getTimeInMillis());
        this.toDate = new Date(toDate.getTimeInMillis());
        return this;
    }

    public CalendarHelper setDate(int fromOrToDate, int year, int month, int date, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, date, hourOfDay, minute);
        switch (fromOrToDate) {
            case SET_FROM_DATE:
                this.fromDate = new Date(calendar.getTimeInMillis());
                break;
            case SET_TO_DATE:
                this.toDate = new Date(calendar.getTimeInMillis());
                break;
            default:
                return null;
        }
        return this;
    }

    public CalendarHelper setTitleAndDescription(String title, String description) {
        this.title = title;
        this.description = description;
        return this;
    }

    public CalendarHelper setLocationAndEmails(String location, String emails) {
        this.location = location;
        this.emails = emails;
        return this;
    }

    public void insertEvent() {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, fromDate.getTime())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, toDate.getTime())
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.DESCRIPTION, description)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                .putExtra(Intent.EXTRA_EMAIL, emails);
        context.startActivity(intent);
    }

    public CalendarHelper setCalendarCallback(CalendarCallback calendarCallback) {
        this.calendarCallback = calendarCallback;
        return this;
    }

    @SuppressWarnings({"MissingPermission"})
    private void logCalendars() {
        String[] projection = new String[]{CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, projection, null, null, null);
        if (cursor.moveToFirst()) {
            int i = cursor.getColumnIndex(CalendarContract.Calendars._ID), j = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME);
            do {
                Log.d("dexter", "logCalendars: " + cursor.getString(i) + "   " + cursor.getString(j));
            } while (cursor.moveToNext());
        }
    }

    @SuppressWarnings({"MissingPermission"})
    private long getCalendarId() {
        //logCalendars();
        String[] projection = new String[]{CalendarContract.Calendars._ID};
        Cursor cursor = context.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, projection, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getLong(0);
        }
        return -1;
    }

    @SuppressWarnings({"MissingPermission"})
    public void insertEventDirectly(final Activity activity, final int requestCodeForCalendar) {
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR}, requestCodeForCalendar);
            calendarCallback.callback(null, "Grant Permission First");
            return;
        }
        new AsyncTask<Void, Void, Void>() {
            private boolean wasEventInserted;
            private String eventInsertionString;

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    ContentResolver cr = context.getContentResolver();
                    ContentValues values;
                    values = new ContentValues();
                    values.put(CalendarContract.Events.DTSTART, fromDate.getTime());
                    values.put(CalendarContract.Events.DTEND, toDate.getTime());
                    values.put(CalendarContract.Events.TITLE, title);
                    values.put(CalendarContract.Events.DESCRIPTION, description);
                    values.put(CalendarContract.Events.CALENDAR_ID, getCalendarId());
                    values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getDisplayName());
                    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

                    long eventID = Long.parseLong(uri.getLastPathSegment());

                    values = new ContentValues();
                    values.put(CalendarContract.Reminders.EVENT_ID, eventID);
                    values.put(CalendarContract.Reminders.METHOD, 1);
                    values.put(CalendarContract.Reminders.MINUTES, (toDate.getTime() - fromDate.getTime()) / 60000);
                    cr.insert(CalendarContract.Reminders.CONTENT_URI, values);

                    wasEventInserted = true;
                    eventInsertionString = "Event added to Calendar";
                } catch (Exception e) {
                    wasEventInserted = false;
                    eventInsertionString = "Event not added to Calendar";
                    Log.d("dexter", "insertEventDirectly: ", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                calendarCallback.callback(wasEventInserted, eventInsertionString);
            }
        }.execute();
    }

    @SuppressWarnings({"MissingPermission"})
    public void insertEventDirectly(final Activity activity, final int requestCodeForCalendar, final List<? extends CalendarInstance> list) {
        if (list == null || list.size() <= 0)
            return;
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR}, requestCodeForCalendar);
            calendarCallback.callback(null, "Grant Permission First");
            return;
        }
        new AsyncTask<Void, Void, Void>() {
            private Boolean wasEventInserted;
            private String eventInsertionString;

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    ContentResolver cr = context.getContentResolver();
                    ContentValues values;
                    long calendarId = getCalendarId();
                    long eventID;
                    CalendarInstance instance;
                    int j = 0;
                    for (int i = 0; i < list.size(); i++) {
                        instance = list.get(i);

                        String[] proj = new String[]{CalendarContract.Instances._ID};
                        Cursor cursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI,
                                proj, CalendarContract.Events.UID_2445 + " = ?",
                                new String[]{instance.getSyncId()}, null);
                        if (cursor.getCount() > 0) {
                            continue;
                        }
                        j++;
                        values = new ContentValues();
                        values.put(CalendarContract.Events.DTSTART, instance.getFromDate(context));
                        values.put(CalendarContract.Events.UID_2445, instance.getSyncId());
                        values.put(CalendarContract.Events.DTEND, instance.getToDate(context));
                        values.put(CalendarContract.Events.TITLE, instance.getTitle());
                        values.put(CalendarContract.Events.DESCRIPTION, instance.getDescription());
                        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
                        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getDisplayName());
                        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

                        eventID = Long.parseLong(uri.getLastPathSegment());

                        values = new ContentValues();
                        values.put(CalendarContract.Reminders.EVENT_ID, eventID);
                        values.put(CalendarContract.Reminders.METHOD, 1);
                        values.put(CalendarContract.Reminders.MINUTES, 15);
                        cr.insert(CalendarContract.Reminders.CONTENT_URI, values);
                    }
                    if (j > 0) {
                        wasEventInserted = true;
                        eventInsertionString = "Event added to Calendar";
                    } else {
                        wasEventInserted = null;
                        eventInsertionString = "Doesn't Matter";
                    }
                } catch (Exception e) {
                    wasEventInserted = false;
                    eventInsertionString = "Event not added to Calendar";
                    Log.d("dexter", "insertEventDirectly: ", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                calendarCallback.callback(wasEventInserted, eventInsertionString);
            }
        }.execute();
    }
}
