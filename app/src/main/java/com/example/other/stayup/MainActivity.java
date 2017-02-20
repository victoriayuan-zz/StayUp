package com.example.other.stayup;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import static android.provider.ContactsContract.CommonDataKinds.Phone.*;

public class MainActivity extends AppCompatActivity {

    android.support.v7.widget.Toolbar toolbar;
    SendAlert alert;

    MediaPlayer player;

    FallDetector detector;
    CountDownTimer timer;

    ImageButton b1;
    ImageButton b2;
    ImageButton b3;

    TextView t1;
    TextView t2;
    TextView t3;

    Switch sw;

    LinearLayout l1;
    LinearLayout l2;
    LinearLayout l3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        l1 = (LinearLayout)findViewById(R.id.l1);
        l2 = (LinearLayout)findViewById(R.id.l2);
        l3 = (LinearLayout)findViewById(R.id.l3);

        b1 = (ImageButton)findViewById(R.id.b1);
        b2 = (ImageButton)findViewById(R.id.b2);
        b3 = (ImageButton)findViewById(R.id.b3);

        sw = (Switch)findViewById(R.id.switch1);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContact(l1);
            }
        });b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContact(l2);
            }
        });b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContact(l3);
            }
        });

        l1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContact(l1);
            }
        });l2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addContact(l2);
            }
        });l3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addContact(l3);
            }
        });

        player = MediaPlayer.create(MainActivity.this, R.raw.alarm);


        t1 = (TextView)findViewById(R.id.t1);
        t2 = (TextView)findViewById(R.id.t2);
        t3 = (TextView)findViewById(R.id.t3);

        alert = new SendAlert(getApplicationContext());

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String name = sharedPref.getString("nam" + Integer.valueOf(R.id.l1), "Add");
        String number = sharedPref.getString("num" + Integer.valueOf(R.id.l1), "000");
        alert.contacts.add(new Contact(number, name));
        t1.setText(name);
        name = sharedPref.getString("nam" + Integer.valueOf(R.id.l2), "Add");
        number = sharedPref.getString("num" + Integer.valueOf(R.id.l2), "000");

        alert.contacts.add(new Contact(number, name));
        t2.setText(name);
        name = sharedPref.getString("nam" + Integer.valueOf(R.id.l3), "Add");
        number = sharedPref.getString("num" + Integer.valueOf(R.id.l3), "000");
        alert.contacts.add(new Contact(number, name));
        t3.setText(name);

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.stop();
                player = MediaPlayer.create(MainActivity.this, R.raw.alarm);
                timer.cancel();
                detector.resetFall();
                detector.reset();
            }
        });

        timer = new CountDownTimer(10000, 10000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                player.stop();
                player = MediaPlayer.create(MainActivity.this, R.raw.alarm);
                alert.connect();
                timer.cancel();
                detector.resetFall();
                detector.reset();
            }
        };

        detector = new FallDetector(this) {
            @Override
            public void onFall() {
                Toast.makeText(activity, "Fall Detected", Toast.LENGTH_SHORT).show();
                timer.start();
                detector.reset();
                if (sw.isChecked()) {
                    player.start();
                }
            }
        };
    }

    static final int PICK_CONTACT_REQUEST = 1;  // The request code

    private void pickContact() {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        pickContactIntent.setType(Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    TextView tview;
    ViewGroup view;

    private void addContact(ViewGroup v) {
        tview = (TextView)v.getChildAt(1);
        view = v;
        pickContact();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected contact
                Uri contactUri = data.getData();
                // We only need the NUMBER column, because there will be only one row in the result
                String[] projection = {Phone.NUMBER, Phone.DISPLAY_NAME};

                // Perform the query on the contact to get the NUMBER column
                // We don't need a selection or sort order (there's only one result for the given URI)
                // CAUTION: The query() method should be called from a separate thread to avoid blocking
                // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
                // Consider using CursorLoader to perform the query.
                Cursor cursor = getContentResolver()
                        .query(contactUri, projection, null, null, null);
                cursor.moveToFirst();

                // Retrieve the phone number from the NUMBER column
                int column = cursor.getColumnIndex(Phone.NUMBER);

                String number = cursor.getString(column);

                column = cursor.getColumnIndex(Phone.DISPLAY_NAME);

                String name = cursor.getString(column);

                tview.setText(name);

                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("num" + Integer.valueOf(view.getId()), number);
                editor.putString("nam" + Integer.valueOf(view.getId()), name);
                editor.commit();
            }
        }
    }
}
