package com.wings2aspirations.genericleadcreation.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wings2aspirations.genericleadcreation.R;

import java.util.ArrayList;
import java.util.List;

public class MeetingSchedulerFragment extends Fragment {

    TextView  dateTV, timeTV, add_event;
    TextInputEditText eventTitleTIet, eventDescriptionTIet;


    public static MeetingSchedulerFragment newInstance() {

        Bundle args = new Bundle();

        MeetingSchedulerFragment fragment = new MeetingSchedulerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meeting_schedular, container, false);
        dateTV = view.findViewById(R.id.date_et);
        /*timeTV = view.findViewById(R.id.time_et);*/
        eventTitleTIet = view.findViewById(R.id.event_title_et);
        eventDescriptionTIet = view.findViewById(R.id.event_description_et);
        add_event = view.findViewById(R.id.add_event);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        add_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String d=dateTV.getText().toString();

                String t=timeTV.getText().toString();


                Intent calIntent = new Intent(Intent.ACTION_INSERT);
                calIntent.setData(CalendarContract.Events.CONTENT_URI);
                calIntent.putExtra(CalendarContract.Events.TITLE, "Google IO Afterparty");
                calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, "The W Hotel Bar on Third Street");
                calIntent.putExtra(CalendarContract.Events.DESCRIPTION, "Hang out after Google IO for a drink and geeky conversation.");
               /* Calendar startTime = Calendar.getInstance();
                startTime.set(years, months-1, days, hours, mins,seconds); // (Year-2013)(Day-Wed [i.e:-3rd DOW]) (Date-16th) 15:50
                Calendar endTime = Calendar.getInstance();
                endTime.set(years, months-1, days, hours, mins+30,seconds); // 18:30
                calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                        startTime.getTimeInMillis());
                calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                        endTime.getTimeInMillis());
                startActivity(calIntent);*/
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
