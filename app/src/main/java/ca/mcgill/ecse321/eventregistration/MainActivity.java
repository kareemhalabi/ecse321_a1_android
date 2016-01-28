package ca.mcgill.ecse321.eventregistration;

import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import ca.mcgill.ecse321.eventregistration.controller.EventRegistrationController;
import ca.mcgill.ecse321.eventregistration.controller.InvalidInputException;
import ca.mcgill.ecse321.eventregistration.model.Event;
import ca.mcgill.ecse321.eventregistration.model.Participant;
import ca.mcgill.ecse321.eventregistration.model.RegistrationManager;
import ca.mcgill.ecse321.eventregistration.persistence.PersistenceEventRegistration;
import ca.mcgill.ecse321.eventregistration.persistence.PersistenceXStream;

public class MainActivity extends AppCompatActivity {

    // data elements
    private HashMap<Integer, Participant> participants;
    private HashMap<Integer, Event> events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        PersistenceEventRegistration.setFileName(
                Environment.getExternalStorageDirectory().getPath() + "/eventregistration.xml");
        PersistenceEventRegistration.loadEventRegistrationModel();
        refreshData();

    }

    private void refreshData() {

        RegistrationManager rm = RegistrationManager.getInstance();

        TextView newParticipant = (TextView) findViewById(R.id.newparticipant_name);
        newParticipant.setText("");

        TextView newEvent = (TextView) findViewById(R.id.newevent_name);
        newEvent.setText("");

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
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


    //TODO Register Button


    public void addParticipant(View v) {
        TextView newParticipant = (TextView) findViewById(R.id.newparticipant_name);
        EventRegistrationController erc = new EventRegistrationController();
        try {
            erc.createParticipant(newParticipant.getText().toString());
        } catch (InvalidInputException e) {
            //TODO handle error
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

        EventRegistrationController erc = new EventRegistrationController();

        TextView eventName = (TextView) findViewById(R.id.newevent_name);
        TextView eventDate = (TextView) findViewById(R.id.newevent_date);

        Calendar calendar = Calendar.getInstance();
        //calendar.setTime((Date) );

        //TODO finish arguments
        //erc.createEvent(eventName.getText(), );
    }


}
