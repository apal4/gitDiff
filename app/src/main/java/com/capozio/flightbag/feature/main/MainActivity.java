package com.capozio.flightbag.feature.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.capozio.flightbag.Communication;
import com.capozio.flightbag.R;
import com.capozio.flightbag.data.local.UserSettings;
import com.capozio.flightbag.data.model.CheckListData;
import com.capozio.flightbag.data.model.ChecklistWrapper;
import com.capozio.flightbag.data.model.Entry;
import com.capozio.flightbag.data.model.FlightStart;
import com.capozio.flightbag.data.model.PilotDataResponse;
import com.capozio.flightbag.data.model.TemplateResponse;
import com.capozio.flightbag.data.model.TemplateWrapper;
import com.capozio.flightbag.feature.learn.LearnFragment;
import com.capozio.flightbag.feature.plan.planFragment;
import com.capozio.flightbag.feature.preflight.PreFlightFragment;
import com.capozio.flightbag.rest.RestClient;
import com.capozio.flightbag.rest.RestInterface;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.mapboxsdk.MapboxAccountManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.observers.Subscribers;
import rx.schedulers.Schedulers;

import static com.capozio.flightbag.util.Configs.CHECKLISTS_FILE;
import static com.capozio.flightbag.util.Configs.DATADBID;
import static com.capozio.flightbag.util.Configs.DEFAULT_TEMPLATE_FILE;
import static com.capozio.flightbag.util.Configs.SHAREDPREF_KEY;
import static com.capozio.flightbag.util.Configs.TAG_PREFLIGHT;
import static com.capozio.flightbag.util.Configs.TIME_FORMAT_LOCAL;
import static com.capozio.flightbag.util.Configs.USERSETTING_FILE;
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
 * Created by Ying Zhang on 8/17/16.
 */
/*
     prompt a dialog for checklist name - DONE!
     TODO: auto focus on the newly added checklist item
     keep track of the last checklist the user clicks - DONE!
     clear the checkbox once submitted on success - DONE!
      use custom view to inflate the toolbars - DONE!
     LayoutInflater mInflater=LayoutInflater.from(context);
View mCustomView = mInflater.inflate(R.layout.toolbar_custom_view, null);
toolbar.addView(mCustomView);
*/

/**
 * Main class for the FlightBag.  This controls the buttons on the bottom of the screen,
 * which are (almost) always visible.  Each button corresponds to a fragment, and this
 * class manages the transition between fragments.
 * This class also manages the checklist templates.
 */
public class MainActivity extends AppCompatActivity implements Communication {

    private List<CheckListData> checkLists;  // List of objects that manage user's checklist.
    private UserSettings userSettings; // store all the user settings
    private Entry mEntry;
    private CheckListData template;  // the template for checklist
    private boolean isChecklistEditMode; // to tell if a checklist or a template is being edited
    private PilotDataResponse.PilotData mPilotData;
    //    private GoogleApiHelper mGoogleApiHelper;

    // Identifies the DJI Fly App, which is launched from the FLY button.
    private final String DJI_PACKAGE_NAME = "dji.pilot";
    private static final Map<MappingService.AirMapLayerType, Integer> id2layer = new HashMap<>();
    //    private static final int FLY_REQUESTCODE = 10;
    // Used to tie together all data from a drone flight.
    private UUID flightID;
    // TODO: hard-coded
    //    private final String db = "db0";
    private final String mtemplateName = "Template_1";


    /**
     * Called before any view is drawn.
     * Sets the content of the view with layout information.
     * Inflate() is called indirectly somewhere in this method.
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init Mapbox
        MapboxAccountManager.start(this,  getString(R.string.access_token));

        // init Airmap
        AirMap.init(MainActivity.this);

        // get the pilot info from the Login activity
        PilotDataResponse response =  new Gson().fromJson(getIntent().getStringExtra("pilot"), new TypeToken<PilotDataResponse>(){}.getType());
        mPilotData = response.getPilotData();

        // generate an UUID representing a new flight
        flightID = UUID.randomUUID();

        // tie each airmap layer type to the corresponding checkbox
        id2layer.put(MappingService.AirMapLayerType.AirportsCommercial, R.id.check_commercial);
        id2layer.put(MappingService.AirMapLayerType.AirportsCommercialPrivate, R.id.check_pa);
        id2layer.put(MappingService.AirMapLayerType.ClassB, R.id.check_class_b);
        id2layer.put(MappingService.AirMapLayerType.ClassC, R.id.check_class_c);
        id2layer.put(MappingService.AirMapLayerType.ClassD, R.id.check_class_d);
        id2layer.put(MappingService.AirMapLayerType.ClassE, R.id.check_class_e0);
        id2layer.put(MappingService.AirMapLayerType.Prohibited, R.id.check_psua);
        id2layer.put(MappingService.AirMapLayerType.Restricted, R.id.check_rsua);
        id2layer.put(MappingService.AirMapLayerType.NationalParks, R.id.check_np);
        id2layer.put(MappingService.AirMapLayerType.NOAA, R.id.check_noaa);
        id2layer.put(MappingService.AirMapLayerType.Hospitals, R.id.check_hos);
        id2layer.put(MappingService.AirMapLayerType.Schools, R.id.check_sch);
        id2layer.put(MappingService.AirMapLayerType.Heliports, R.id.check_heli);
        id2layer.put(MappingService.AirMapLayerType.PowerPlants, R.id.check_pp);
        id2layer.put(MappingService.AirMapLayerType.TFRS, R.id.check_tfrs);
        id2layer.put(MappingService.AirMapLayerType.Wildfires, R.id.check_wild);

        setContentView(R.layout.activity_main);

        // retrieve template from cloud first
        getTemplateFromCloud();

        // read user settings from local storage.
        // We currently treat this as a single user machine,
        // so these are set by the last person using the android device,
        // regardless of user id.
        final SharedPreferences sharedPref =
                getSharedPreferences(SHAREDPREF_KEY, Context.MODE_PRIVATE);
        String settingStr = sharedPref.getString(USERSETTING_FILE, null);

        if (settingStr == null) { // if setting not exist, init settings
            userSettings = new UserSettings();
            userSettings.setChecklistSelectedIdx(0);
            //this needs to be changed to the enumeration
            userSettings.setMapTypeSelected(UserSettings.MapType.STREET);
        } else { // if exsists, parse settings from the Json file
            userSettings = new Gson().fromJson(settingStr, new TypeToken<UserSettings>() {
            }.getType());
        }
 //        mGoogleApiHelper = App.getGoogleApiHelper();

        // setup default fragment.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new IndexMainFragment()).commit();
        }
    }

    /**
     * Reads the template if it exists.
     * This json file holds the checklist the user sees before a flight begins.
     * If no template exists, this method will create a default.
     */
    private void getTemplateFromCloud() {
        template = null;
        final RestInterface restService = RestClient.getInstance().create(RestInterface.class);

        Call<TemplateResponse> templateCall = restService.getCheckListTemplate(DATADBID, "\"" + mtemplateName + "\"");
        templateCall.enqueue(new Callback<TemplateResponse>() {
            @Override
            public void onResponse(Call<TemplateResponse> call, Response<TemplateResponse> response) {
                Log.d("TAG", "GET_TEMPLATE_SUCCESS");
                List<TemplateWrapper> templateLists = response.body().getRows();
                // if the template from the cloud is valid, store the template as a variable
                if (!templateLists.isEmpty()) {
                    // assume there is only one template,
                    // if there are more than one, always take the first template.
                    ChecklistWrapper metadata = response.body().getRows().get(0).getValue();
                    template = metadata.getChecklistData();
                }
            }

            @Override
            public void onFailure(Call<TemplateResponse> call, Throwable t) {
                Log.d("TAG", "GET_TEMPLATE_FAILED");
            }
        });
    }

    // transition to Learn fragment
    public void learnClick(View view) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new LearnFragment()).commit();
    }

    // transition to Preflight fragment
    public void preflightClick(View view) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new PreFlightFragment(), TAG_PREFLIGHT).commit();
        //  hideKeyBoard(this);
    }

    // transition to Plan fragment
    public void planClick(View view) {
        Fragment mPlanFragment = new planFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mPlanFragment).commit();

        //TODO: get rid of this
        //FragmentTransaction  fragment_transaction = getSupportFragmentManager().beginTransaction();
        //FragmentTransaction transact = fragment_transaction.replace(R.id.container,mPlanFragment);
        //transact.commit();
    }

    // transition to Fly fragment
    public void flyClick(View view) {
//        getSupportFragmentManager().beginTransaction().replace(R.id.container, new IndexMainFragment()).commit();

        // get local time and UTC time
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT_LOCAL);
        Date curDate = new Date();
        String localDate = dateFormat.format(curDate);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        FlightStart flightStart = new FlightStart(flightID.toString(), mPilotData.getPilot_ID(), mPilotData.getRPIC_Cert(), mPilotData.getEmailAddress(), localDate, dateFormat.format(curDate) + "Z");
        final RestInterface restService = RestClient.getInstance().create(RestInterface.class);
        restService.uploadPilotData(DATADBID, flightStart)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Subscribers.empty());


        // once the user clicks the fly button, the current flightID is used to represent this flight
        // and a new flightID is generated for the next flight
        flightID = UUID.randomUUID();

        Intent dijIntent = getPackageManager().getLaunchIntentForPackage(DJI_PACKAGE_NAME);
        if(dijIntent == null) {
            dijIntent.setData(Uri.parse("market://details?id="+ DJI_PACKAGE_NAME));
        }
        startActivity(dijIntent);
 //        PackageManager pm = getPackageManager();
 //        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
 //        for(ApplicationInfo ai : packages)
 //            Log.d("TAG", ai.packageName);
    }

    // transition to Log fragment
    public void logClick(View view) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new IndexMainFragment()).commit();
    }

    // transition to Manage fragment
    public void manageClick(View view) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new IndexMainFragment()).commit();
    }

    public void adminClick(View view) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new LearnFragment()).commit();
    }




    /**
     *  Communcation Interface implementations
     *  for transferring data between fragments and this Main activity
     *
     */
    @Override
    public void sendCheckLists(List<CheckListData> checklists) {
        this.checkLists = checklists;
    }

    @Override
    public List<CheckListData> getChecklists() {
        return checkLists;
    }

    @Override
    public int getChecklistIdx() {
        return userSettings.getChecklistSelectedIdx();
    }

    @Override
    public void sendCheckListIdx(int idx) {
        userSettings.setChecklistSelectedIdx(idx);
    }

    @Override
    public Entry getNotes() {
        return mEntry;
    }

    @Override
    public void sendNotes(Entry entry) {
        this.mEntry = entry;
    }

    @Override
    public void sendTemplate(CheckListData template) {
        this.template = template;
    }

    @Override
    public CheckListData getTemplate() {
        return template;
    }

    @Override
    public boolean getEditMode() {
        return isChecklistEditMode;
    }

    @Override
    public void notifyEditMode(boolean isChecklistEditMode) {
        this.isChecklistEditMode = isChecklistEditMode;
    }

    @Override
    public List<MappingService.AirMapLayerType> getMapLayers() {
        return userSettings.getMapLayers();
    }

    @Override
    public Map<MappingService.AirMapLayerType, Integer> getIDMap() {
        return id2layer;
    }


    @Override
    public void setMapType(UserSettings.MapType mapType) {
        userSettings.setMapTypeSelected(mapType);
    }

    @Override
    public UserSettings.MapType getMapType() {
        return userSettings.getMapTypeSelected();
    }

    @Override
    public UUID getFlightID() {return flightID;}


    @Override
    public PilotDataResponse.PilotData getPilotData() {
        return mPilotData;
    }

    // resolve issue with keyboard not hiding when clicking outside an EditText
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                hideKeyBoard(v);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        mGoogleApiHelper.connect();
    }

    /**
     * Save User settings and Checklist data locally when the application is stopped
     */
    @Override
    protected void onStop() {
        super.onStop();
//        mGoogleApiHelper.disconnect();
//        Log.d("TAG","MAIN: onSTOP!");

        SharedPreferences sharedPref = getSharedPreferences(SHAREDPREF_KEY, Context.MODE_PRIVATE);

        // save user preferences
        sharedPref.edit().putString(USERSETTING_FILE,
                new Gson().toJson(userSettings)).commit();

        // save template
        sharedPref.edit()
                .putString(DEFAULT_TEMPLATE_FILE, new Gson().toJson(template))
                .commit();

        // save checklists
        sharedPref.edit()
                .putString(CHECKLISTS_FILE, new Gson().toJson(checkLists))
                .commit();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        Log.d("TAG", "MAIN: onRESTART!");
    }
}
