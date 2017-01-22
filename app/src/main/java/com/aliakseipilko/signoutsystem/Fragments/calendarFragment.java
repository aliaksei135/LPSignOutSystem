/*
 * com.aliakseipilko.signoutsystem.Fragments.calendarFragment was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 22/01/17 12:42
 */

package com.aliakseipilko.signoutsystem.Fragments;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliakseipilko.signoutsystem.Helpers.CalendarRemoteFetch;
import com.aliakseipilko.signoutsystem.R;
import com.github.tibolte.agendacalendarview.AgendaCalendarView;
import com.github.tibolte.agendacalendarview.CalendarPickerController;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;
import com.github.tibolte.agendacalendarview.models.DayItem;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class calendarFragment extends Fragment implements CalendarPickerController {

    private static AgendaCalendarView agendaCalendarView;
    private List<CalendarEvent> events;

    public calendarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        agendaCalendarView = (AgendaCalendarView) getView().findViewById(R.id.agenda_calendar_view);
    }

    public void populateCalendar() {

        final Calendar minDate = Calendar.getInstance();
        final Calendar maxDate = Calendar.getInstance();

        minDate.add(Calendar.MONTH, 0);

        maxDate.add(Calendar.MONTH, 1);

        CalendarRemoteFetch calFetch = CalendarRemoteFetch.getInstance();

        List<CalendarEvent> newEvents = calFetch.getParsedCalData();
        if (events != null) {
            if (events.size() == newEvents.size()) {
                return;
            } else {
                events = newEvents;
            }
        } else {
            events = newEvents;
        }

        if (agendaCalendarView != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    agendaCalendarView.init(events, minDate, maxDate, Locale.UK, calendarFragment.this);
                }
            });

        }
    }

    @Override
    public void onDaySelected(DayItem dayItem) {
        //Do nothing
    }

    @Override
    public void onEventSelected(CalendarEvent event) {
        //Do nothing
    }

    @Override
    public void onScrollToDate(Calendar calendar) {
        //Do nothing
    }

}
