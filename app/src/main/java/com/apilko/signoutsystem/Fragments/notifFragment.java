package com.apilko.signoutsystem.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.apilko.signoutsystem.Activities.MainActivity;
import com.apilko.signoutsystem.DataHandlers.GoogleSheetsHandler;
import com.apilko.signoutsystem.R;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class notifFragment extends Fragment {

    private Long notifLastUpdateTimeMillis;
    private TextSwitcher notifSwitcher;
    private List<String> notifList;
    private int index;
    private GoogleSheetsHandler sheetsHandler;

    public notifFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        sheetsHandler = GoogleSheetsHandler.getInstance(getContext());

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notif, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        notifSwitcher = (TextSwitcher) getView().findViewById(R.id.notifTextSwitcher);
        notifSwitcher.setInAnimation(getContext(), android.R.anim.slide_in_left);
        notifSwitcher.setOutAnimation(getContext(), android.R.anim.slide_out_right);

        ViewSwitcher.ViewFactory viewFactory = new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {

                TextView t = new TextView(getContext());
                t.setGravity(Gravity.CENTER);
                t.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
                return t;
            }
        };
        notifSwitcher.setFactory(viewFactory);
        displayNotifsView();
    }

    public void displayNotifsView() {

        //Update Notifs if old
        if (notifList == null ||
                notifLastUpdateTimeMillis == null
                || (System.currentTimeMillis() - notifLastUpdateTimeMillis) >= 600000) {
            Log.d("NotifFragment", "Notifications List is Updated");
            notifList = getNotifList();
        }

        if (index >= notifList.size()) {
            index = 0;
        } else {
            notifSwitcher.setText(notifList.get(index));
            index++;
        }
    }

    private List<String> getNotifList() {

        try {
            return sheetsHandler.getLatestNotifs(new MainActivity().getServerAuthCode());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

}
