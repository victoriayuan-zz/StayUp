package com.example.other.stayup;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;

/**
 * Created by Jinesh Patel on 2015-08-15.
 */
public class SendAlert implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    public ArrayList<Contact> contacts = new ArrayList<Contact>();

    private Context context;

    public SendAlert(Context context) {
        this.context = context;
        buildGoogleApiClient(context);
    }

    public void connect() {
        mGoogleApiClient.connect();
    }

    protected synchronized void buildGoogleApiClient(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void stop() {
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        //Create URL
        if (mLastLocation != null) {
            String url = "http://maps.google.com/?q=" + String.valueOf(mLastLocation.getLatitude()) + "," + String.valueOf(mLastLocation.getLongitude());

            sendSMS(url);
        }
    }

    protected void sendSMS(String myLoc) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            for (Contact contact : contacts) {
                smsManager.sendTextMessage(contact.number, null, "I have an emergency at: " + myLoc, null, null);
            }

            Toast.makeText(context, "Your emergency contacts have been notified via SMS!", Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            Toast.makeText(context,"SMS failed, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
