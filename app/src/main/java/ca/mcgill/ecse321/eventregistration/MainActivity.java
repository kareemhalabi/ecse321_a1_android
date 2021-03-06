package ca.mcgill.ecse321.eventregistration;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.sql.Date;
import java.util.HashMap;
import java.util.Iterator;

import ca.mcgill.ecse321.eventregistration.controller.EventRegistrationController;
import ca.mcgill.ecse321.eventregistration.controller.InvalidInputException;
import ca.mcgill.ecse321.eventregistration.model.Event;
import ca.mcgill.ecse321.eventregistration.model.Participant;
import ca.mcgill.ecse321.eventregistration.model.RegistrationManager;
import ca.mcgill.ecse321.eventregistration.persistence.PersistenceEventRegistration;

public class MainActivity extends AppCompatActivity {

    // data elements
    private HashMap<Integer, Participant> participants;
    private HashMap<Integer, Event> events;
    private String error = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Note that I needed to add the permission
        // <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
        // to the AndroidManifest file
        PersistenceEventRegistration.setFileName(
                Environment.getExternalStorageDirectory().getPath() + "/eventregistration.xml");
        PersistenceEventRegistration.loadEventRegistrationModel();
        refreshData();
    }

    private void refreshData() {

        RegistrationManager rm = RegistrationManager.getInstance();

        // error
        TextView errorMessage = (TextView) findViewById(R.id.errorMessage);
        errorMessage.setText(error);

        if (error == null || error.length() == 0) {
            // Initialize the data in the participant spinner
            Spinner participantSpinner = (Spinner) findViewById(R.id.participantspinner);
            ArrayAdapter<CharSequence> participantAdapter = new
                    ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
            participantAdapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
            this.participants = new HashMap<Integer, Participant>();

            int i = 0;
            for (Iterator<Participant> participants = rm.getParticipants().iterator();
                    participants.hasNext(); i++) {
                Participant p = participants.next();
                participantAdapter.add(p.getName());
                this.participants.put(i,p);
            }
            participantSpinner.setAdapter(participantAdapter);

            // Initialize the data in the event spinner
            Spinner eventSpinner = (Spinner) findViewById(R.id.eventspinner);
            ArrayAdapter<CharSequence> eventAdapter = new
                    ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
            eventAdapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
            this.events = new HashMap<Integer, Event>();

            i = 0;
            for (Iterator<Event> events = rm.getEvents().iterator();
                 events.hasNext(); i++) {
                Event e = events.next();
                eventAdapter.add(e.getName());
                this.events.put(i,e);
            }
            eventSpinner.setAdapter(eventAdapter);

            // Initialize participant text field
            TextView newParticipant = (TextView) findViewById(R.id.newparticipant_name);
            newParticipant.setText("");

            // Initialize event name text field
            TextView newEvent = (TextView) findViewById(R.id.newevent_name);
            newEvent.setText("");

            // Sets the event date to the current day and
            // the event start/end times to the current time
            Calendar c = Calendar.getInstance();
            setDate(R.id.newevent_date, c.get(Calendar.DAY_OF_MONTH),
                    c.get(Calendar.MONTH), c.get(Calendar.YEAR));
            setTime(R.id.newevent_starttime, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
            setTime(R.id.newevent_endtime, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
        }
    }

    public void register(View v) {
        error = null;
        EventRegistrationController erc = new EventRegistrationController();
        Spinner participantSpinner = (Spinner) findViewById(R.id.participantspinner);
        int selectedParticipant = participantSpinner.getSelectedItemPosition();
        Spinner eventSpinner = (Spinner) findViewById(R.id.eventspinner);
        int selectedEvent = eventSpinner.getSelectedItemPosition();
        try {
            erc.register(participants.get(selectedParticipant),events.get(selectedEvent));
        } catch (InvalidInputException e) {
            error = e.getMessage();
        }
        refreshData();
    }


    public void addParticipant(View v) {
        error = null;
        EventRegistrationController erc = new EventRegistrationController();
        TextView newParticipant = (TextView) findViewById(R.id.newparticipant_name);
        try {
            erc.createParticipant(newParticipant.getText().toString());
        } catch (InvalidInputException e) {
            error = e.getMessage();
        }

        refreshData();
    }

    public void showDatePickerDialog(View v) {
        TextView tf = (TextView) v;
        Bundle args = getDateFromLabel(tf.getText());
        args.putInt("id", v.getId());

        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void setDate(int id, int day, int month, int year) {
        TextView tv = (TextView) findViewById(id);
        tv.setText(String.format("%02d-%02d-%04d", day, month + 1, year));
    }

    private Bundle getDateFromLabel(CharSequence text) {
        Bundle rtn = new Bundle();
        String comps[] = text.toString().split("-");
        int day =1;
        int month=1;
        int year = 1;

        if (comps.length == 3) {
            day = Integer.parseInt(comps[0]);
            month = Integer.parseInt(comps[1]);
            year = Integer.parseInt(comps[2]);
        }

        rtn.putInt("day", day);
        rtn.putInt("month", month - 1);
        rtn.putInt("year", year);

        return rtn;
    }

    public void showTimePickerDialog(View v) {
        TextView tf = (TextView) v;
        Bundle args = getTimeFromLabel(tf.getText());
        args.putInt("id", v.getId());

        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void setTime(int id, int hourOfDay, int minute) {
        TextView tv = (TextView) findViewById(id);
        tv.setText(String.format("%02d:%02d", hourOfDay, minute));
    }

    private Bundle getTimeFromLabel(CharSequence text) {
        Bundle rtn = new Bundle();
        String comps[] = text.toString().split(":");
        int hour = 12;
        int minute = 0;

        if (comps.length == 2) {
            hour = Integer.parseInt(comps[0]);
            minute = Integer.parseInt(comps[1]);
        }

        rtn.putInt("hour", hour);
        rtn.putInt("minute", minute);

        return rtn;
    }

    public void addEvent(View v) {

        error = null;
        EventRegistrationController erc = new EventRegistrationController();

        TextView eventName = (TextView) findViewById(R.id.newevent_name);

        TextView eventDate = (TextView) findViewById(R.id.newevent_date);
        DateFormat dft = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = new Date(dft.parse(eventDate.getText().toString()).getTime());
        } catch (ParseException e) {
            error = "Date not formatted correctly!";
        }

        TextView startTime = (TextView) findViewById(R.id.newevent_starttime);
        TextView endTime = (TextView) findViewById(R.id.newevent_endtime);
        DateFormat tft = new SimpleDateFormat("HH:mm");
        Time start = null, end = null;
        try {
            start = new Time(tft.parse(startTime.getText().toString()).getTime());
            end = new Time(tft.parse(endTime.getText().toString()).getTime());
        } catch (ParseException e) {
            error = "Start/End time not formatted correctly!";
        }

        try {
            erc.createEvent(eventName.getText().toString(), date, start, end);
        } catch (InvalidInputException e) {
            error = e.getMessage();
        }
        refreshData();
    }
}