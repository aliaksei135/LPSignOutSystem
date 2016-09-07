package com.apilko.signoutsystem.Helpers;

import android.content.Context;
import android.os.AsyncTask;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.text.ICalReader;

@Keep
public class CalendarRemoteFetch {

    private static final String CALENDAR_URL = "http://www.leightonpark.com/media/calendar/ical/Calendar";
    private static long currentFileHash;
    private static CalendarRemoteFetch ourInstance;
    private Context context;

    private CalendarRemoteFetch(Context context) {
        this.context = context;
    }

    public static CalendarRemoteFetch getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new CalendarRemoteFetch(context);
            return ourInstance;
        } else {
            return ourInstance;
        }
    }

    public List<CalendarEvent> getParsedCalData() {
        return convertCalData(getRawCalData());
    }

    private List<CalendarEvent> convertCalData(List<VEvent> rawCalData) {

        List<CalendarEvent> result = new ArrayList<>();
        SimpleDateFormat startFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.UK);
        SimpleDateFormat endFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.UK);
        for (VEvent event : rawCalData) {
            //This is bound to have something wrong with it
            try {
                Date startDate = startFormat.parse(event.getDateStart().getValue().toString());
                Calendar startCal = startFormat.getCalendar();

                Date endDate = endFormat.parse(event.getDateEnd().getValue().toString());
                Calendar endCal = endFormat.getCalendar();

                CalendarEvent calEvent = new BaseCalendarEvent(
                        event.hashCode(), //Use hash of event as Unique ID
                        android.graphics.Color.DKGRAY, //Use Cyan as the colour for all events
                        event.getSummary().getValue(), //Actually the title
                        " ", //Empty description
                        " ", //Empty Location
                        startCal.getTime().getTime(), //Start time/date
                        endCal.getTime().getTime(), //End time/date
                        1, //Events are all day
                        null);

                result.add(calEvent);
            } catch (ParseException e) {
                e.printStackTrace();
            }


        }
        return result;
    }

    private List<VEvent> getRawCalData() {

        List<VEvent> result = new ArrayList<>();
        ICalReader reader;
        try {
            File iCalFile = getiCalFile();
            assert iCalFile != null;
            currentFileHash = iCalFile.hashCode();
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
        this.context = context;
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

        public DownloadCalTask() {

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
            File resultCalFile = new File(context.getFilesDir(), "calendar");
            resultCalFile.createNewFile();
            resultCalFile.setWritable(true);
            URLConnection connection = CAL_URL.openConnection();
            connection.connect();
            InputStream is = connection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            OutputStream os = new FileOutputStream(resultCalFile);
            byte[] data = new byte[8092];
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
