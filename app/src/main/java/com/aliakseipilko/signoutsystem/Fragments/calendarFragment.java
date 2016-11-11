/*
 * com.aliakseipilko.signoutsystem.Fragments.calendarFragment was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2016.  All Rights Reserved.
 *
 * Last modified 11/11/16 20:11
 */

package com.aliakseipilko.signoutsystem.Fragments;


import android.os.Bundle;
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

        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();

        minDate.add(Calendar.MONTH, 0);

        maxDate.add(Calendar.MONTH, 2);

        CalendarRemoteFetch calFetch = CalendarRemoteFetch.getInstance();

        List<CalendarEvent> events = calFetch.getParsedCalData();

        if (agendaCalendarView != null) {
            agendaCalendarView.init(events, minDate, maxDate, Locale.UK, this);
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
