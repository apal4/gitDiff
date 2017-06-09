package com.capozio.flightbag.feature.preflight;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.capozio.flightbag.Communication;
import com.capozio.flightbag.R;
import com.capozio.flightbag.data.model.CheckListData;
import com.capozio.flightbag.data.model.ChecklistWrapper;
import com.capozio.flightbag.data.model.Entry;
import com.capozio.flightbag.data.model.PDFWrapper;
import com.capozio.flightbag.data.model.PermissonData;
import com.capozio.flightbag.data.model.PermissonSlipMetaDataResponse;
import com.capozio.flightbag.data.model.PilotDataResponse;
import com.capozio.flightbag.feature.permissonSlip.SearchNearbyActivity;
import com.capozio.flightbag.feature.permissonSlip.SignatureActivity;
import com.capozio.flightbag.rest.DataConnector;
import com.capozio.flightbag.rest.RestClient;
import com.capozio.flightbag.rest.RestInterface;
import com.capozio.flightbag.rest.RestService;
import com.capozio.flightbag.util.Configs;
import com.capozio.flightbag.util.SendEmailUtil;
import com.capozio.flightbag.util.StorageUtil;
import com.capozio.flightbag.util.ToastUtil;
import com.cloudant.sync.documentstore.AttachmentException;
import com.cloudant.sync.documentstore.ConflictException;
import com.cloudant.sync.documentstore.DocumentStoreException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import javax.mail.MessagingException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.capozio.flightbag.util.Configs.CHECKLISTS_FILE;
import static com.capozio.flightbag.util.Configs.DEFAULT_TEMPLATE_FILE;
import static com.capozio.flightbag.util.Configs.PDF_FILENAME;
import static com.capozio.flightbag.util.Configs.SHAREDPREF_KEY;
import static com.capozio.flightbag.util.Configs.TAG_CHECKLIST_EDIT;
import static com.capozio.flightbag.util.Configs.TAG_TEMPLATE_EDIT;
import static com.capozio.flightbag.util.Configs.TIME_FORMAT_LOCAL;
import static com.capozio.flightbag.util.Configs.TYPE_CHECKLIST;
import static com.capozio.flightbag.util.DialogFactory.createEditDialog;

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
 *

 */

/**
 * This fragment will display the checklist that the user last chose.
 * User can check the checkBoxes, modify notes, submit the current checklist,
 * modify template or checklist, add new checklist based on the template and view all other checklists the user has.
 */
public class PreFlightFragment extends android.support.v4.app.Fragment {
    public static final String PERMISSON_TEXT = "permisson_text";
    //   List<Entry> listItems;
    //   ViewListAdapter viewListAdapter;
    //   ListView listView;
    //   int counter;
    // TODO: hard-coded
    private final String templateName = "Template_1";
    private final String userid = "db0";

    private final static String TAG = "TAG";
    private AppCompatActivity activity;
    private RecyclerListAdapter adapter; // adapter for rendering the checklists
    private Communication cm;
    private List<CheckListData> checklists; // all the checklists the user have.
    private int menuSelectIdx; // current checklist being selected
    private CheckListData currentList;
    private TextView checklistName;
    private RestInterface restService;
    private SharedPreferences sharedPref; // retrive data from local storage.
    private Subscription email_sub;
    private Subscription syncFromCloud_sub;
    private boolean isSyncSuccess;
    private ProgressDialog progressDialog;
    private File externalDir;
    private final int PERMISSON_CODE = 3;

    private static final int REQUESTSUBMIT_CODE = 1;

    public PreFlightFragment() {
    }

    private CheckListData initTemplate() {
        CheckListData template;
//        Log.d("TAG", "NO_TEMPLATE_FOUND!");
        String templateJson = sharedPref.getString(DEFAULT_TEMPLATE_FILE, null);

        if (templateJson == null || templateJson.equals("null")) {
            // if no template is found in local storage (mostly happens on the very first time the app is launched),
            // create a pre-defined template for the user.
            List<Entry> listEntries = new ArrayList<>();
            listEntries.add(new Entry("Airspace", ""));
            listEntries.add(new Entry("Permissions", ""));
            listEntries.add(new Entry("Weather", ""));
            listEntries.add(new Entry("Batteries", ""));
            listEntries.add(new Entry("Motors/Blades", ""));
            listEntries.add(new Entry("Hardware", ""));
            listEntries.add(new Entry("Software", ""));
            listEntries.add(new Entry("Property Inspection", ""));
            listEntries.add(new Entry("Risk Assessment", ""));
            listEntries.add(new Entry("Barriers & Signage", ""));
            listEntries.add(new Entry("Personal Protection", ""));
            listEntries.add(new Entry("Crew Briefing", ""));

            // set this list to be the default template
            template = new CheckListData("<NAME>", listEntries, null);

        } else {
            // convert json into java object
            template = new Gson().fromJson(templateJson, new TypeToken<CheckListData>() {
            }.getType());
        }
        return template;
    }

    private List<CheckListData> initChecklist(CheckListData template) {
        List<CheckListData> checkLists;

        // Get a json string from that file.
        String jsonStr = sharedPref.getString(CHECKLISTS_FILE, null);

        //  Deserialize jsonStr to get a java object with all the checklist data.
        Type collectionType = new TypeToken<List<CheckListData>>() {
        }.getType();

        // If there is no checklist in the file, create a default checklist based on the template.
        if (jsonStr == null || jsonStr.equals("null")) {
            checkLists = new ArrayList<>();
            checkLists.add(template.newInstance());
        } else { // convert json into java object
            checkLists = new Gson().fromJson(jsonStr, collectionType);
            if (checkLists == null) throw new NullPointerException();
        }
        return checkLists;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        restService = RestClient.getInstance().create(RestInterface.class);

        externalDir = new File(Environment.getExternalStorageDirectory(), Configs.WAIVER_PATH);
        if (!externalDir.exists() && !externalDir.mkdir())
            Log.e(TAG, "Directory not created");

        cm = (Communication) getActivity();
        // get the checklist data from MainActivity.
        checklists = cm.getChecklists();

        // Get a key-value map with the default checklist.  This gets a reference to
        // a file.
        sharedPref = getActivity().getSharedPreferences(SHAREDPREF_KEY, Context.MODE_PRIVATE);

        if (checklists == null) {
            // Initialize the checklist data.  If there an existing
            // sharedPreference file, we use that. Otherwise, create a default template.
            CheckListData template = cm.getTemplate();
            if (template == null) {
                template = initTemplate();
                cm.sendTemplate(template);
            }

            // send template and checklists to MainActivity.
            checklists = initChecklist(template);
            cm.sendCheckLists(checklists);
        }

        // get the checklist the user selected last time.
        menuSelectIdx = cm.getChecklistIdx();
        if (menuSelectIdx >= checklists.size())
            menuSelectIdx = 0;
        currentList = checklists.get(menuSelectIdx);

//        TextView toolbarTitle = (TextView) getActivity().findViewById(R.id.toolbar_title);
//        toolbarTitle.setText(R.string.toolbar_title_preflight);

        View rootview = inflater.inflate(R.layout.fragment_preflight, container, false);

        PilotDataResponse.PilotData pilotData = cm.getPilotData();
        ((TextView)rootview.findViewById(R.id.pilot_name)).setText(pilotData.getFirstName()+" "+pilotData.getLastName());
        ((TextView)rootview.findViewById(R.id.pilot_id)).setText(pilotData.getPilot_ID());
        ((TextView)rootview.findViewById(R.id.pilot_cert)).setText(pilotData.getRPIC_Cert());

/*
        EditText editText = (EditText) rootview.findViewById(R.id.editText);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                System.out.println(getActivity()+","+b);
                if(!b)
                    hideKeyBoard(view);
            }
        });
*/

/*
       // ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(rootview.getContext(), R.layout.checklist_item, test);
        viewListAdapter = new ViewListAdapter(rootview.getContext(), listItems, false);

        listView = (ListView) rootview.findViewById(R.id.checklistview);
        //listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // test for resolving auto deselect of the checkbox in listview
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                CheckBox checkBox = (CheckBox) view.getTag(R.id.checkBox);
//                String s = checkBox.isChecked() ? "is checked":"is not checked";
//                Toast.makeText(view.getContext(), s, Toast.LENGTH_LONG).show();
//            }
//        });

        // try to fix the bug that causes "java.lang.IllegalArgumentException: parameter must be a descendant of this view"
        // the bug might get triggered when scrolling the listview while the keyboard is being displayed
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if(SCROLL_STATE_TOUCH_SCROLL == i) {
                    View currentFocus = getActivity().getCurrentFocus();
                    if(currentFocus != null)
                        currentFocus.clearFocus();
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            }
        });

        listView.setAdapter(viewListAdapter);
        */
        return rootview;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSON_CODE);


        // inflate the recyclerView and bind adapter to it
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.checklistview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
//        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new RecyclerListAdapter(getActivity().getSupportFragmentManager(), currentList.getChecklist(), false);
        recyclerView.setAdapter(adapter);

        progressDialog = new ProgressDialog(getContext(), R.style.SignatureProgressDialog);

//        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter, false);
//        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
//        touchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = ((AppCompatActivity) getActivity());


        checklistName = (TextView) activity.findViewById(R.id.toolbar_mytitle);
        checklistName.setVisibility(View.VISIBLE);
        checklistName.setText(currentList.getChecklistName());

        final Button signButton = (Button) activity.findViewById(R.id.button_sign);
        final Button submitButton = (Button) activity.findViewById(R.id.button_submit);
        final Button searchButton = (Button) activity.findViewById(R.id.button_display);

        /* toolbar elements */
        // shows all the checklists the user have in a popup menu.
        ImageButton menuButton = (ImageButton) activity.findViewById(R.id.button_menus);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(activity, view);
                Menu menu = popupMenu.getMenu();
                // populate the menu.
                for (CheckListData checkListData : checklists) {
                    menu.add(checkListData.getChecklistName());
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        // find which item is selected, render the fragment with the selected checklist
                        // and record its index
                        for (int i = 0; i < checklists.size(); i++)
                            if (menuItem.getTitle().equals(checklists.get(i).getChecklistName())) {
                                menuSelectIdx = i;
                                currentList = checklists.get(i);
                                adapter.setList(currentList.getChecklist());
                                adapter.notifyDataSetChanged();
                                checklistName.setText(currentList.getChecklistName());
                                cm.sendCheckListIdx(menuSelectIdx);

                                // when another checklist is selected,
                                // the "submit" button has to be adjusted based on
                                // whether a permisson slip has been signed yet,
                                // and it must show a "sign" button if there is a permisson slip.
                                if (currentList.hasPermissonSlip()) {
                                    signButton.setVisibility(View.VISIBLE);
                                    searchButton.setVisibility(View.VISIBLE);
//                                    if (currentList.isSigned()) {
//                                        submitButton.setEnabled(true);
//                                        submitButton.setAlpha(1f);
//
//                                    } else {
//                                        submitButton.setEnabled(false);
//                                        submitButton.setAlpha(0.5f);
//
//                                    }
                                } else {
                                    signButton.setVisibility(View.INVISIBLE);
                                    searchButton.setVisibility(View.INVISIBLE);
//                                    submitButton.setEnabled(true);
//                                    submitButton.setAlpha(1f);
                                }
                                // write to sharedPreference
//                                sharedPref.edit().putString(USERSETTING_FILE,
//                                        new Gson().toJson(new UserSettings(menuSelectIdx))).commit();
                                return true;
                            }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        ImageButton addChecklist = (ImageButton) activity.findViewById(R.id.button_add_checklist);
        // when user wants to add a new checklist,
        // create a new list in our dataset and show the edit mode for this cheklist in the background,
        // in the foreground, prompt user a dialog for the name of this new checklist
        addChecklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                currentList = cm.getTemplate().newInstance();

                checklists.add(currentList);
                cm.sendCheckListIdx(checklists.size() - 1);
                cm.notifyEditMode(true);

                createEditDialog(activity, checklists).show();
                goEditMode(TAG_CHECKLIST_EDIT);
            }
        });

        // when user wants to edit the template,
        // go to edit mode and render the template list.
        ImageButton editTemplate = (ImageButton) activity.findViewById(R.id.button_edit_template);
        editTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cm.notifyEditMode(false);
                goEditMode(TAG_TEMPLATE_EDIT);
            }
        });

        // when user wants to edit the checklist, go to edit mode and render the current checklist.
        ImageButton editButton = (ImageButton) activity.findViewById(R.id.button_edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cm.notifyEditMode(true);
                goEditMode(TAG_CHECKLIST_EDIT);
            }
        });


//       /* ------- DISPLAY BUTTON ------- */
////        final AppCompatActivity activity = this;
//        Button displayButton = (Button) activity.findViewById(R.id.button_display);
//        displayButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });

        /* ------- SUBMIT BUTTON ------- */
        // when user submits the checklist, if the internet is connected,
        // wrap the checklist data with meta-data and send it to the cloud and
        // clear all the checked boxes.
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View msg = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm_submit_checklist, null);
                new AlertDialog.Builder(getContext())
                        .setCancelable(false)
                        .setView(msg)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final CheckListData curlist = checklists.get(menuSelectIdx);

                                // get local time and UTC time
                                SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT_LOCAL);
//                    DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
                                Date curDate = new Date();
                                String localDate = dateFormat.format(curDate);
                                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                //make a new map to upload with dataConnector
                                Map<String,Object> map=new HashMap<String,Object>();
                                map.put("localTimestamp",localDate);
                                map.put("TemplateName",templateName);
                                map.put("UTCTimestamp",dateFormat.format(curDate) + "Z");
                                map.put("checklistData",curlist);
                                map.put("type",TYPE_CHECKLIST);
                                map.put("FlightID",cm.getFlightID());
                                DataConnector dataConnector = DataConnector.getInstance(getContext());

                                try{
                                    dataConnector.writeMap(map, getContext());
                                    dataConnector.syncWithThread();
                                    Toast.makeText(activity, "Checklist Submitted!", Toast.LENGTH_LONG).show();
                                }catch (RuntimeException e){

                                    Toast.makeText(activity, "Checklist Fail!", Toast.LENGTH_LONG).show();

                                }





                             /*   Call<ResponseBody> mCall = restService.uploadChecklist(userid,
                                        new ChecklistWrapper(localDate, dateFormat.format(curDate) + "Z", curlist, TYPE_CHECKLIST, templateName, cm.getFlightID().toString()));
*/
                  /*              mCall.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        Log.d("TAG", "UPLOAD_SUCCESS");
                                        Toast.makeText(activity, "Checklist Submitted!", Toast.LENGTH_LONG).show();
                                        // clear the focus of the checkboxes
                                        adapter.clearNotesAndCheckBoxes();
                                        adapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        Log.e("TAG", "UPLOAD_FAILED:" + t.toString());
                                        Toast.makeText(activity, "Submit Failed! Check Your Internet Connection.", Toast.LENGTH_LONG).show();
                                    }
                                });
                                dialog.dismiss();*/
                            }
                        })
                     .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                     }).create().show();



            }
        });

        // ensure that the checklist can be submitted only after the permisson has been signed
        if (currentList.hasPermissonSlip()) {
            signButton.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.VISIBLE);
//            if (currentList.isSigned()) {
//                submitButton.setEnabled(true);
//                submitButton.setAlpha(1f);
//            } else {
//                submitButton.setEnabled(false);
//                submitButton.setAlpha(0.5f);
//            }
        } else {
            signButton.setVisibility(View.INVISIBLE);
            searchButton.setVisibility(View.INVISIBLE);
        }

        /* ------- SIGN BUTTON ------- */
        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SignatureDialogFragment signatureDialogFragment = new SignatureDialogFragment();
//                signatureDialogFragment.show(getFragmentManager(), null);
                //createSignatureDialog(activity, R.string.dialog_title_signature).show();

                Intent intent = new Intent(activity, SignatureActivity.class);
                startActivityForResult(intent, REQUESTSUBMIT_CODE);

                    /*
                    Call<PermissonSlipResponse> mCall = restService.getPermissionText(userid);
                    mCall.enqueue(new Callback<PermissonSlipResponse>() {
                        @Override
                        public void onResponse(Call<PermissonSlipResponse> call, Response<PermissonSlipResponse> response) {
                            Log.d("TAG", "Get Permisson Slip Success!");

                            Intent intent = new Intent(activity, SignatureActivity.class);
                            intent.putExtra(PERMISSON_TEXT, response.body().getContent());
                            startActivityForResult(intent, REQUEST_CODE);
                        }

                        @Override
                        public void onFailure(Call<PermissonSlipResponse> call, Throwable t) {
                            Toast.makeText(activity, "Get Permisson Slip Failed! Check Your Internet Connection.", Toast.LENGTH_LONG).show();
                        }
                    });
                    */
            }
        });

        /* ------- SEARCH BUTTON ------- */
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchNearbyActivity.class);
                startActivity(intent);
            }
        });

        /* ------- SYNC BUTTON ------- */
        ImageButton syncButton = (ImageButton) activity.findViewById(R.id.button_sync);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSyncSuccess = true;
                progressDialog.setMessage("Syncing...");
                progressDialog.show();

                // Sync files from the cloud first
                final List<String> idlist = new ArrayList<>();




                //Syncing list of permission data to local storage, want to replace by DataConnector writeMap
                syncFromCloud_sub = restService.getPermissonSlipMetaData(Configs.PERMISSONDBID)
                        .flatMap(new Func1<PermissonSlipMetaDataResponse, Observable<Void>>() {
                            @Override
                            public Observable<Void> call(PermissonSlipMetaDataResponse response) {
                                for (PermissonData permissonData : response.getRows()) {
                                    idlist.add(permissonData.getId());
                                    File dir = new File(externalDir, permissonData.getId());
                                    if (!dir.exists())
                                        dir.mkdir();
                                    StorageUtil.writeJson2File(new File(dir, Configs.META_FILENAME), new Gson().toJson(permissonData.getKey()));
                                    StorageUtil.writeStatus2File(new File(dir, Configs.STAT_FILENAME), "", true, true);
                                }
                                return Observable.from(idlist)
                                        .flatMap(new Func1<String, Observable<Void>>() {
                                            @Override
                                            public Observable<Void> call(final String s) {
                                                return restService.getAttachment(Configs.PERMISSONDBID, s)
                                                        .flatMap(new Func1<ResponseBody, Observable<Void>>() {
                                                            @Override
                                                            public Observable<Void> call(ResponseBody responseBody) {
                                                                File dir = new File(externalDir, s);
                                                                if (!dir.exists())
                                                                    dir.mkdir();
                                                                StorageUtil.writeResponseBody2File(responseBody, externalDir + "/" + s + "/" + PDF_FILENAME);
                                                                return null;
                                                            }
                                                        });
                                            }
                                        });
                            }
                        }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Void>() {
                            @Override
                            public void onCompleted() {
                                Log.d(TAG, "Completed!!!");
                                upload2Cloud();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "ERROR!");
                                isSyncSuccess = false;
                                upload2Cloud();
                            }

                            @Override
                            public void onNext(Void aVoid) {
                                Log.d(TAG, "NEXT");
                            }
                        });
            }
        });
    }

    private void upload2Cloud() {
        // read the status file
        email_sub = Observable.from(new File(externalDir.toString()).listFiles())
                .flatMap(new Func1<File, Observable<List<String>>>() {
                    @Override
                    public Observable<List<String>> call(File file) {
                        final List<String> ret = new ArrayList<>();
                        final String uuid = file.getName();
                        final File currentDir = new File(externalDir, uuid);
                        String statStr = StorageUtil.readFile(new File(currentDir, Configs.STAT_FILENAME));
                        JSONObject reader;
                        ret.add(uuid);
                        try {
                            reader = new JSONObject(statStr);
                            final boolean stat_email = reader.getBoolean(Configs.STATUS_EMAIL);
                            final boolean stat_pdf = reader.getBoolean(Configs.STATUS_PDF);
                            ret.add(stat_pdf ? "1" : "0");
                            // if the email address is empty, "emailAddr" will be a string "null";
                            final String emailAddr = reader.getString(Configs.EMAIL_ADDR);
                            ret.add(emailAddr);

                            // if the email has not been sent, send it
                            if (!stat_email) {
                                return Observable.fromCallable(new Callable<List<String>>() {
                                    @Override
                                    public List<String> call() throws MessagingException {
                                        ret.add(SendEmailUtil.sendEmail(emailAddr, new File(externalDir, uuid + "/" + Configs.PDF_FILENAME)) ? "1" : "0");
                                        return ret;
                                    }
                                }).onErrorReturn(new Func1<Throwable, List<String>>() {
                                    @Override
                                    public List<String> call(Throwable throwable) {
                                        Log.d(TAG, throwable.getMessage());
                                        ret.add("0");
                                        return ret;
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ret.add("1");
                        return Observable.just(ret);
                    }
                }).flatMap(new Func1<List<String>, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(List<String> strlist) {
                        String uuid = strlist.get(0);
                        boolean stat_email = strlist.get(3).equals("1");
                        if (!stat_email)
                            isSyncSuccess = false;
                        String emailAddr = strlist.get(2);
                        if (!(strlist.get(1).equals("1"))) {
                            RestService.RESTuploadPDF(getPDFMetaData(uuid), new File(externalDir, uuid + "/" + PDF_FILENAME),
                                    uuid, new File(externalDir, uuid), emailAddr, stat_email, progressDialog, getContext(), true, isSyncSuccess);
//                                    if(progressDialog != null)
//                                        progressDialog.dismiss();
                        } else {
                            File statusFile = new File(externalDir, uuid + "/" + Configs.STAT_FILENAME);
                            StorageUtil.writeStatus2File(statusFile, emailAddr, stat_email, true);
                        }
                        return Observable.empty();
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "completed!");
                                if(progressDialog != null) {
                                    progressDialog.dismiss();
                                }
                        if(isSyncSuccess)
                            ToastUtil.makeShortToast(getContext(), "Synced!");
                        else
                            ToastUtil.makeShortToast(getContext(), "Sync Failed!");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (progressDialog != null)
                            progressDialog.dismiss();
                        ToastUtil.makeShortToast(getContext(), "Sync Failed!");
                    }

                    @Override
                    public void onNext(Void aVoid) {}
                });
    }


    private PDFWrapper getPDFMetaData(String uuid) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(
                    new FileReader(externalDir + "/" + uuid + "/" + Configs.META_FILENAME));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new Gson().fromJson(br, PDFWrapper.class);
    }


//    private void delayBeforeDismiss(int delay, final ProgressDialog progressDialog) {
//        new Handler().postDelayed(new Runnable() {
//            public void run() {
//                progressDialog.dismiss();
//            }
//        }, delay);
//    }

    /**
     * go to PreflightEditMode fragment
     */
    private void goEditMode(String whichEditMode) {
        LinearLayout linearLayout = (LinearLayout) activity.findViewById(R.id.menu_container);
        linearLayout.setVisibility(View.INVISIBLE);

        PreFlightEditModeFragment editmodeFragment = new PreFlightEditModeFragment();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, editmodeFragment, whichEditMode)
                //                   .addToBackStack(null)
                .commit();
    }

    // After the signed permisson has been submitted,
    // user now is allowd to submit the checklist.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.d("TAG", (requestCode == Activity.RESULT_OK) + "");
        if (requestCode == REQUESTSUBMIT_CODE) {
//            if (resultCode == Activity.RESULT_OK) {
                if(data != null && data.getBooleanExtra("Signed", true));
                currentList.setSigned();
                Button submitButton = (Button) activity.findViewById(R.id.button_submit);
                submitButton.setEnabled(true);
                submitButton.setAlpha(1f);
//            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(email_sub  != null)
            email_sub.unsubscribe();
        if(syncFromCloud_sub != null)
            syncFromCloud_sub.unsubscribe();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSON_CODE) {
            if(grantResults.length > 0){
               if(grantResults[0] != PackageManager.PERMISSION_GRANTED )
                   ToastUtil.makeLongToast(getContext(), "Storage is required to perform action!\nTap the \"Preflight\" tag to grant the permisson again.");
                if(grantResults[1] != PackageManager.PERMISSION_GRANTED)
                    ToastUtil.makeLongToast(getContext(), "Location is required to perform action!\nTap the \"Preflight\" tag to grant the permisson again.");
            }
        }
    }
}
