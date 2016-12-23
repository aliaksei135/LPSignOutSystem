/*
 * com.aliakseipilko.signoutsystem.Helpers.CalendarRemoteFetch was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2016.  All Rights Reserved.
 *
 * Last modified 23/12/16 13:12
 */

package com.aliakseipilko.signoutsystem.Helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.Keep;

import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.text.ICalReader;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

@Keep
public class CalendarRemoteFetch {

    private static final String CALENDAR_URL = "http://www.leightonpark.com/media/calendar/ical/Calendar";
    private static Long calendarLastUpdateTimeMillis;
    private static Long currentFileHash;
    private static List<CalendarEvent> events;
    private static CalendarRemoteFetch ourInstance;

    private CalendarRemoteFetch() {
    }

    public static CalendarRemoteFetch getInstance() {
        if (ourInstance == null) {
            ourInstance = new CalendarRemoteFetch();
            return ourInstance;
        } else {
            return ourInstance;
        }
    }

    public List<CalendarEvent> getParsedCalData() {

        if (calendarLastUpdateTimeMillis == null
                || (System.currentTimeMillis() - calendarLastUpdateTimeMillis) >= 14400000 //4 Hours
                || events == null) {
            events = convertCalData(getRawCalData());
            return events;
        } else {
            return events;
        }
    }

    private List<CalendarEvent> convertCalData(List<VEvent> rawCalData) {

        List<CalendarEvent> result = new ArrayList<>();
        SimpleDateFormat startFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.UK);
        SimpleDateFormat endFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.UK);
        try {
            for (VEvent event : rawCalData) {
                //This is bound to have something wrong with it
                startFormat.parse(event.getDateStart().getValue().toString());
                Calendar startCal = startFormat.getCalendar();
                endFormat.parse(event.getDateEnd().getValue().toString());
                Calendar endCal = endFormat.getCalendar();

                CalendarEvent calEvent = new BaseCalendarEvent(
                        event.hashCode(), //Use hash of event as Unique ID
                        android.graphics.Color.DKGRAY,
                        event.getSummary().getValue(), //Actually the title
                        " ", //Empty description
                        " ", //Empty Location
                        startCal.getTime().getTime(), //Start time/date
                        endCal.getTime().getTime(), //End time/date
                        1, //Events are all day
                        null);

                result.add(calEvent);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<VEvent> getRawCalData() {

        List<VEvent> result = new ArrayList<>();
        ICalReader reader;
        try {
            File iCalFile = getiCalFile();
            assert iCalFile != null;
            reader = new ICalReader(iCalFile);
            ICalendar ical;
            while ((ical = reader.readNext()) != null) {
                for (VEvent event : ical.getEvents()) {
                    result.add(event);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private File getiCalFile() {
        try {
            return new DownloadCalTask().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isEventsUpToDate(Context context) {
        File compFile = getiCalFile();
        int retryCount = 0;

        while (compFile == null) {
            if (retryCount > 9) {
                return false;
            }
            retryCount++;
        }

        return compFile.hashCode() == currentFileHash;
    }


    private class DownloadCalTask extends AsyncTask<Void, Void, File> {

        URL CAL_URL;

        DownloadCalTask() {

            try {
                CAL_URL = new URL(CalendarRemoteFetch.CALENDAR_URL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                cancel(true);
            }
        }

        @Override
        protected File doInBackground(Void... params) {

            try {
                return fetchiCalFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private File fetchiCalFile() throws IOException {
            File resultCalFile = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath(), "LPCalendar.ical");
            resultCalFile.createNewFile();
            resultCalFile.setWritable(true);
            URLConnection connection = CAL_URL.openConnection();
            connection.connect();
            InputStream is = connection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            OutputStream os = new FileOutputStream(resultCalFile);
            byte[] data = new byte[32768]; //32 kilobytes...sorry, I mean Kibibytes -_-
            int count;
            long total = 0;
            while ((count = bis.read(data)) != -1) {
                total += count;
                os.write(data, 0, count);
            }

            //Close streams
            os.flush();
            os.close();
            is.close();
            bis.close();

            return resultCalFile;
        }
    }
}
