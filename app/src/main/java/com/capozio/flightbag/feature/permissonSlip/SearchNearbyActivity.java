package com.capozio.flightbag.feature.permissonSlip;

import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.capozio.flightbag.R;
import com.capozio.flightbag.data.model.PDFWrapper;
import com.capozio.flightbag.data.model.PermissonSearchResult;
import com.capozio.flightbag.data.model.PermissonWrapper;
import com.capozio.flightbag.util.ToastUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;
import static com.capozio.flightbag.util.KeyBoardUtil.hideKeyBoard;

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
 * Created by Ying Zhang on 11/15/16.
 */

/**
 *  Core logic for the SearchNearby screen.
 *  Controls how addresses are entered into the Address TextBox, and how we search for the nearby
 *  addresses where a signed permission waiver exists.  
 *
 */
public class SearchNearbyActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, LocationListener{
    /**
     *  Manages the  process where Android updates the device location.
     */
    public static LocationRequest locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(1 * 1000)// Polling interval. Time in milliseconds between each poll. Polling too often drains battery.
            .setFastestInterval(1 * 1000); // 1 second, in milliseconds
    private GoogleApiClient mGoogleApiClient;
//    private File externalDir = new File(Environment.getExternalStorageDirectory(), Configs.WAIVER_PATH);
//    private RestInterface restService;
//    private final static int REQUEST_CHECK_SETTINGS = 25;
//    private final static int PERMISSION_ACCESS_FINE_LOCATION = 10;
    private Location mLocation;
    private ProgressDialog mProgressDialog;
    private PermissonSlipRecyclerlistAdapter adapter;
//    private List<PDFWrapper> metalist;
//    private List<String> pdfPath;
    private PermissonSearchResult permissonResult;
    private List<PermissonWrapper> resultList;
    private RecyclerView recyclerView;
    private boolean isSaved = false;
//    private Type typeToken =  new TypeToken<List<PermissonWrapper>>() {}.getType();

//    private Subscription geo_sub;
//    private static final String GEOCODER_BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=";
//    private static final String APIKEY = "AIzaSyCyTZbt0spEFUYeeEbbwZOEarvcLthP7H8";

    public SearchNearbyActivity() {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        isSaved = true;
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(!isSaved)
            getCurrentLocation();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        initPermissonSlip(mLocation);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  The circular arrows graphic while we wait for activity to start.
        mProgressDialog = new ProgressDialog(this, R.style.SignatureProgressDialog);
        setContentView(R.layout.activity_searchnearby);

        // Initialize the list of homes with signed waivers.
        recyclerView = (RecyclerView) findViewById(R.id.permissonlistview);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        // Get objects where user enters the search radius and search address.
        final EditText radiusEdit =  ((EditText) findViewById(R.id.edit_radius));
        final EditText addressEdit = (EditText) findViewById(R.id.edit_address_search);
        FloatingActionButton searchButton = (FloatingActionButton) findViewById(R.id.button_search);
        // The button with a little magnifying glass.  User clicks this to search.
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog.setMessage("Searching...");
                mProgressDialog.show();
//                final EditText addressText = (EditText) findViewById(R.id.edit_address_search);
//                String addressStr = addressText.getText().toString();
                final String radiusStr = radiusEdit.getText().toString().trim();
                if(mProgressDialog != null)
                    mProgressDialog.dismiss();

                // Get the json object corresponding to target address that the user selected.
                PDFWrapper pdfWrapper = adapter.getCurrentlist();
                // Get the lat and long from that json, and use it to set the target location.
                if(pdfWrapper != null) {
                    double lon = Double.parseDouble(pdfWrapper.getLongitude());
                    double lat = Double.parseDouble(pdfWrapper.getLatitude());
                    Location addressLocation = new Location("");
                    addressLocation.setLongitude(lon);
                    addressLocation.setLatitude(lat);
                    // Search for all nearby waiver locations. This changes the dataset.
                    resultList = permissonResult.searchByRadius(radiusStr, addressLocation);
                } else
                    resultList = permissonResult.searchByRadius(radiusStr);
                // Tell the adapter about this change, to draw the nearby waiver addresses on the screen.
                adapter.notifyDataSetChanged();
                hideKeyBoard(view);

                // Show a toast with the number of nearby addresses.
                ToastUtil.makeLongToast(getBaseContext(), resultList.size()+" Record"+ ((resultList.size() <= 1) ? "":"s") + " Found!");
            }
        });


        /**
         * Called when focus shifts to the address textbox. Clears the radius text
         * when user edits the address text.
         */
        addressEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && !radiusEdit.getText().toString().trim().isEmpty())
                    radiusEdit.setText("");
            }
        });


        /** Called with each keystroke the user types an address into the address textbox.
         * As user types the address, it dynamically shows all the results that starts with the typed address and sorted
         * based on the least distance between the address and the current location
         */
        addressEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            /**
             *  Searches for all nearby waiver locations when the user types a key.
             * @param s - The text in the textbox.
             * @param start - ignored.
             * @param before - ignored.
             * @param count - ignored.
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resultList = permissonResult.searchByAddress(s.toString());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // The image that user clicks when he wants to exit the screen. Current an "x".
        ImageButton doneButton = (ImageButton) findViewById(R.id.button_done_search);
        //  When the user exits, hide the keyboard, and finish the SearchNearbyActivity.
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(geo_sub != null)
//                    geo_sub.unsubscribe();
                hideKeyBoard(view);
                finish();
            }
        });
    }

    /**
     * Initializes the SearchNearby screen.
     * @param myLocation - The location of the Android device.
     */
    private void initPermissonSlip(Location myLocation) {
        // Read all json files that have information about waiver locations.
        // These are found in the SignedWaiver directory.
        permissonResult  = new PermissonSearchResult(myLocation);
        // Get the textboxes where the user types the address and radius.
        EditText addressEdit = (EditText) findViewById(R.id.edit_address_search);
        EditText radiusEdit = (EditText) findViewById(R.id.edit_radius);
        // Set up the rest of the screen.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        resultList = permissonResult.getMetaResult();
        adapter = new PermissonSlipRecyclerlistAdapter(resultList, addressEdit, radiusEdit);
        recyclerView.setAdapter(adapter);

        // Draw a toast showing the number of signed waivers found.
        ToastUtil.makeShortToast(getBaseContext(), resultList.size()+" Record" + ((resultList.size() <= 1) ? "":"s")+ " Found within 0.25 mile.");
    }

    /**
     * Gets the current location of the android device.
     * This will fail if the locations permissions are turned off.
     * This should never happen, because we ask for permission in
     * GoogleApiHelper class.
     */
    private void getCurrentLocation() {
        mLocation =LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLocation == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        } else
            initPermissonSlip(mLocation);
    }


    /**
     *  Connects with the Google API.
     */
    @Override
    public void onStart() {
        super.onStart();
//        Log.d("TAG", "ONSTART!");
        mGoogleApiClient.connect();
    }

    /**
     *  Disconnects from the Google API.
     */
    @Override
    public void onStop() {
        super.onStop();
//        Log.d("TAG", "ONSTOP!");
        mGoogleApiClient.disconnect();
    }

    /**
     * Resolves issue with keyboard not hiding when clicking outside a textbox.
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                hideKeyBoard(v);
                v.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
