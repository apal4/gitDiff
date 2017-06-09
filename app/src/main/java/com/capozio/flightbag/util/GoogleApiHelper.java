package com.capozio.flightbag.util;

import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

/*** ***********************************************************************
 * <p>
 * Pilot Training System CONFIDENTIAL
 * __________________
 * <p>
 * [2015] - [2017] Pilot Training System
 * All Rights Reserved.
 * <p>
 * NOTICE:  All information contained herein is, and remains
 * the property of Pilot Training System,
 * The intellectual and technical concepts contained
 * herein are proprietary to Pilot Training System
 * and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Pilot Training System.
 *
 * Created by Ying Zhang on 11/30/16.
 */

/**
 *  Wraps Google location services.
 */
public class GoogleApiHelper implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{
    private final static String TAG = GoogleApiHelper.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 10000;
    private final static int PERMISSION_ACCESS_FINE_LOCATION = 10;
    // Create the LocationRequest object
    public static LocationRequest locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(1 * 1000)// 10 seconds, in milliseconds
            .setFastestInterval(1 * 1000); // 1 second, in milliseconds

    public static LocationRequest longLocationReqeust = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(60*1000)
            .setFastestInterval(1 * 1000);

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    public GoogleApiHelper(Context context) {
        this.mContext = context;
        buildGoogleApiClient();
        connect();
    }

    public void connect() {
        if(mGoogleApiClient != null && !mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();

    }

    public void disconnect() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @SuppressWarnings("ResourceType")
    public void requestLocationUpdate() {
//        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions((AppCompatActivity)mContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    PERMISSION_ACCESS_FINE_LOCATION);
//        }
        mLastLocation = null;
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    @SuppressWarnings("ResourceType")
    public void requestLongLocationUpdate() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, longLocationReqeust, this);
    }

    public void removeLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public Location getLastLocation() {
        return mLastLocation;
    }

    @SuppressWarnings("ResourceType")
    public Location getGoogleLastLocation() {return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);}

    public PendingResult<LocationSettingsResult> getLocationSettingResult() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);
        return LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED)
            ToastUtil.makeLongToast(mContext,  "Disconnected. Please re-connect.");
        else if (i == CAUSE_NETWORK_LOST)
            ToastUtil.makeLongToast(mContext,  "Network lost. Please re-connect.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult((AppCompatActivity)mContext, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                Log.d(TAG, "Connection failed!");
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            ToastUtil.makeLongToast(mContext, "Make sure the location service is enabled.");
            Log.d(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        Log.d(TAG, location.getLongitude()+"|"+location.getLatitude());
    }

    public boolean isConnected() {
        return mGoogleApiClient.isConnected();
    }
}
