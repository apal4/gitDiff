package com.capozio.flightbag.feature.preflight;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.capozio.flightbag.Communication;
import com.capozio.flightbag.R;
import com.capozio.flightbag.data.model.CheckListData;
import com.capozio.flightbag.data.model.ChecklistWrapper;
import com.capozio.flightbag.data.model.TemplateResponse;
import com.capozio.flightbag.rest.RestClient;
import com.capozio.flightbag.rest.RestInterface;
import com.capozio.flightbag.util.DialogFactory;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.capozio.flightbag.util.Configs.CHECKLIST_EDIT_MODE;
import static com.capozio.flightbag.util.Configs.DEFAULT_TEMPLATE_FILE;
import static com.capozio.flightbag.util.Configs.SHAREDPREF_KEY;
import static com.capozio.flightbag.util.Configs.TAG_PREFLIGHT;
import static com.capozio.flightbag.util.Configs.TEMPLATE_EDIT_MODE;
import static com.capozio.flightbag.util.Configs.TIME_FORMAT_LOCAL;
import static com.capozio.flightbag.util.Configs.TYPE_TEMPLATE;
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
 * Created by Ying Zhang on 8/26/16.
 *

 */

/**
 *  EditMode for the preFlight Fragment.
 *  Launched by clicking on the pencil icon.
 *  Used for editing both checklist template and the checklist itself.
 */
public class PreFlightEditModeFragment extends android.support.v4.app.Fragment {
    // TODO: hard-coded
    private final String mtemplateName = "Template_1";
    private final String userid = "db0";

    // private ViewListAdapter viewListAdapter;
    //   private List<Entry> list;
    private RecyclerListAdapter adapter;
    private Communication cm;
    private List<CheckListData> checklists;
    private CheckListData curList;
    private AppCompatActivity activity;
    private SharedPreferences sharedPref;
    int curIdx;
    private boolean isChecklistEditMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_preflight_editmode, container, false);

        // initialize variables
        cm = (Communication) getActivity();
        // Get a list of all checklists - unclear how this list is populated.
        checklists = cm.getChecklists();
        // Index to the checklist that we are currently editing.
        curIdx = cm.getChecklistIdx();
        // A boolean.  True if we are editing a checklist.
        isChecklistEditMode = cm.getEditMode();
        // The current list is either a template or a checklist
        if(checklists.size() == 1)
            curIdx = 0;
        curList = isChecklistEditMode ? (checklists.get(curIdx) ): cm.getTemplate();

        return rootview;
    }
        /*
        List<String> content = getArguments().getStringArrayList("list_content");
        List<String> notes = getArguments().getStringArrayList("list_notes");
        if(content == null || notes == null )
            System.err.println("Bundle is NULL!");
        else {
            list = new ArrayList<>();
            for (int i = 0; i < content.size(); i++) {
                list.add(new Entry(content.get(i), notes.get(i)));
                //     System.out.println(content.get(i)+","+notes.get(i));
            }



/*
            viewListAdapter = new ViewListAdapter(getContext(), list, true);
            ListView listView = (ListView) rootview.findViewById(R.id.checklistview);


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

        }
    */


    /**
     * create recyclerView for the edit mode in preFlight fragment.
     * set "add" button here
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // render recyclerView and bind adapter to it.
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.checklistview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
//        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new RecyclerListAdapter(getActivity().getSupportFragmentManager(), curList.getChecklist(), true);
//        adapter.setTAGs(getString(R.string.TAG_PREFLIGHT), getString(R.string.TAG_EDITMODE));
        recyclerView.setAdapter(adapter);

//        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter, true);
//        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
//        touchHelper.attachToRecyclerView(recyclerView);


        // button for adding a new entry in the list.
        final ImageButton addButton = (ImageButton) view.findViewById(R.id.imagebutton_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  viewListAdapter.add(new Entry("Clicked: "+(+list.size()+1)));

                // resolve the bug when the focus is on the EditText "A" at the bottom of the list and
                // a new EditText "B" will be added at the bottom when the user press the "add" button
                // thus now the user should be able to swap "A" with "B"
                getActivity().findViewById(R.id.imageButton_movedown).setEnabled(true);

//                adapter.onItemAdd(new Entry("Clicked:" + (curList.getChecklist().size() + 1)));
                curList.addEntry();
                //Log.d("TAGB",curList.getChecklist().size()+"");
                adapter.notifyItemInserted(curList.getChecklist().size() - 1);
                // make sure the newly added entry is shown at bottom.
                recyclerView.scrollToPosition(curList.getChecklist().size() - 1);
            }
        });

        // whether a permisson slip is present
        /*
        if (isChecklistEditMode) {
            final Button permissonButton = (Button) getActivity().findViewById(R.id.button_permisson);
            final String permissonStr = curList.getPermissonSlip();
            if (permissonStr == null)
                permissonButton.setVisibility(View.INVISIBLE);
            else {
                permissonButton.setVisibility(View.VISIBLE);
                // inflate and adjust layout to take up 90% of the width of the screen
                final View myview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_view_permissonslip, null);
                TextView textView = (TextView) myview.findViewById(R.id.dialog_textview_permisson);
                textView.setText(Html.fromHtml(permissonStr));
                permissonButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFactory.createViewPermissonSlipDialog(getContext(), myview, 0.9f).show();
                    }
                });
            }
        }*/

    }

    /**
     * On edit mode, update toolbar and profileNAme to edit mode.
     * save the edited notes and handle the "done" button and "delete" button.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sharedPref = getActivity().
                getSharedPreferences(SHAREDPREF_KEY, Context.MODE_PRIVATE);

        // update toolbar to editmode
        activity = ((AppCompatActivity) getActivity());
//        tbpre = (Toolbar) activity.findViewById(R.id.toolbarpreflight);
//        tbedit = (Toolbar) activity.findViewById(R.id.toolbarpreflightedit);

//        activity.setSupportActionBar(tbedit);
//        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
//        tbpre.setVisibility(View.GONE);
//        tbedit.setVisibility(View.VISIBLE);

        // set the title based on the mode currently in.
        TextView title = (TextView) activity.findViewById(R.id.check_text);
        title.setText(isChecklistEditMode ? CHECKLIST_EDIT_MODE : TEMPLATE_EDIT_MODE);


        final EditText checklistName = (EditText) getActivity().findViewById(R.id.toolbar_title_edit);
        if (!isChecklistEditMode) {
            // each user will have only one template,
            // so there is no need to have a template name.
            checklistName.setVisibility(View.INVISIBLE);
        } else {
            // set checklist name
            checklistName.setVisibility(View.VISIBLE);
            checklistName.setText(curList.getChecklistName());
        }
//        Log.d("TAG", checkListData.getChecklistName());
        /*
        checklistName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    hideKeyBoard(view);
                    curList.setChecklistName(checklistName.getText().toString());
//                    System.out.println(checklistName.getText().toString());
                }
            }
        });
*/

//        final Button permissonButton = (Button) activity.findViewById(R.id.button_permisson);
//        permissonButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DialogFactory.createViewPermissonSlipDialog(getContext(), v, 0.9f).show();
//            }
//        });

        // when user is done editing,
        // 1) editing checklist: check if the name of the checklist user defined is valid and update the data in the memory
        // 2) editing template: upload the template to the cloud if internet is connected.
        ImageButton doneButton = (ImageButton) activity.findViewById(R.id.button_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (isChecklistEditMode) {
                    //Log.d("TAG", cm.getChecklists().size()+"");
                    // check if the checklist name is empty
                    final String curChecklistName = checklistName.getText().toString().trim();
                    if (curChecklistName.isEmpty()) {
                        DialogFactory.createSimpleOkDialog(getContext(), R.string.dialog_msg_emptyname).show();
                        return;
                    }


                    if (checklists == null || checklists.isEmpty())
                        throw new NullPointerException();

                    // check if the checklist name is duplicate
                    for (int i = 0; i < checklists.size(); i++) {
                        if (i != curIdx && checklists.get(i).getChecklistName().equals(curChecklistName)) {
                            final int index = i;
                            AlertDialog.Builder builder = DialogFactory.createYesNoDialog(activity, R.string.dialog_msg_dupname);
                            builder.setPositiveButton("Replace", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checklists.set(index, curList);
                                    checklists.remove(curIdx);
//                                    cm.sendCheckListIdx(curIdx < index ? index - 1 : index);
                                    cm.sendCheckListIdx(curIdx);
                                    curList.setChecklistName(curChecklistName);
                                    exitEditMode(view);
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checklistName.setText(curChecklistName);
                                }
                            });
                            builder.create().show();
                            return;
                            //DialogFactory.createSimpleOkDialog(getContext(), R.string.dialog_msg_dupname).show();

                        }
                    }

                    curList.setChecklistName(curChecklistName);
                    exitEditMode(view);
                    /*
                     Save a json string with the Checklists into a file.
                      sharedPres is a global android resource for storing user preference.
                     The pathname for that file is found in R.string.CHECKLISTS_FILE.
                    sharedPref.edit()
                            .putString(CHECKLISTS_FILE, cm.checkLists2Json())
                            .commit();
                     */
                } else {
                    // Save a json string with the Template into a file.
                    //  sharedPres is a global android resource for storing user preference.
                    // The pathname for that file is found in R.string.DEFAULT_TEMPLATE_FILE.
//                    sharedPref.edit()
//                            .putString(DEFAULT_TEMPLATE_FILE, new Gson().toJson(curList))
//                            .commit();

                    cm.sendTemplate(curList);

                    // upload the template onto Cloudant
                    final RestInterface restService = RestClient.getInstance().create(RestInterface.class);
                    Call<TemplateResponse> templateCall = restService.getCheckListTemplate(userid, "\"" + mtemplateName + "\"");
                    templateCall.enqueue(new Callback<TemplateResponse>() {
                        @Override
                        public void onResponse(Call<TemplateResponse> call, Response<TemplateResponse> response) {
                            Log.d("TAG", "GET_TEMPLATE_SUCCESS");
                            // get the template from cloud first
                            // if exists, we update the template to the cloud.
                            // otherwise, upload a new template to the cloud.
                            ChecklistWrapper metadata = null;

                            if (!response.body().getRows().isEmpty())
                                metadata = response.body().getRows().get(0).getValue();

                            // get local time and UTC time
                            SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT_LOCAL);
//                    DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
                            Date curDate = new Date();
                            String localDate = dateFormat.format(curDate);
                            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                            Call<ResponseBody> mCall = restService.uploadChecklist(userid,
                                    new ChecklistWrapper(metadata == null ? null : metadata.get_id(),
                                            metadata == null ? null : metadata.get_rev(),
                                            localDate,
                                            dateFormat.format(curDate) + "Z",
                                            curList,
                                            TYPE_TEMPLATE,
                                            mtemplateName,
                                            cm.getFlightID().toString()));

                            mCall.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    Log.d("TAG", "UPLOAD_SUCCESS");
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Log.e("TAG", "UPLOAD_FAILED:" + t.toString());
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<TemplateResponse> call, Throwable t) {
                            Log.e("TAG", "GET_TEMPLATE_FAILED:" + t.toString());
                        }
                    });
                    exitEditMode(view);
                }

            }
        });

        // if user wants to delete the checklist,
        // remove the checklist from the dataset and update index info
        ImageButton deleteButton = (ImageButton) activity.findViewById(R.id.button_delete);
        if (!isChecklistEditMode) {
            deleteButton.setVisibility(View.GONE);
        } else {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    AlertDialog.Builder builder = DialogFactory.createYesNoDialog(activity, R.string.dialog_delete_checklist);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (checklists.size() > 1) {
                                checklists.remove(curIdx);
                                if (curIdx > 0) {
                                    curIdx--;
//                            sharedPref.edit().putString(USERSETTING_FILE,
//                                    new Gson().toJson(new UserSettings(curIdx))).commit();
                                }
                                cm.sendCheckListIdx(curIdx);


                            } else {
                                // This is the case where the user deletes the last available checklist,
                                // then use the template to create a default checklist for the user.

                                // Type collectionType = new TypeToken<List<CheckListData>>() {}.getType();
                                CheckListData defaultTemplate = cm.getTemplate();
                                if (defaultTemplate == null) {
                                    defaultTemplate = new Gson().
                                            fromJson(sharedPref.getString(DEFAULT_TEMPLATE_FILE, null),
                                                    CheckListData.class);
                                }
                                Log.d("TAG", new Gson().toJson(defaultTemplate));
                                cm.sendCheckListIdx(0);
                                checklists.set(0, defaultTemplate.newInstance());
//                    Log.d("TAG",checklists.get(0)+"1");

//                    Log.d("TAG",checklists+"after");
//                    Log.d("TAG",checklists+","+cm.getChecklists());
                            }
                            exitEditMode(view);
                        }
                    }).setNegativeButton(R.string.no, null);
                    builder.create().show();
                }
            });
        }
    }

    /*
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == resultCode) {
                Object updatedNotes = data.getExtras().get("NOTES_REV");
                Object posObj = data.getExtras().get("POS_REV");
                if (updatedNotes != null && posObj != null) {
                    int pos = Integer.parseInt(posObj.toString());
                    curList.getChecklist().get(pos).notes = updatedNotes.toString();
                    adapter.notifyItemChanged(pos);
                    // viewListAdapter.notifyDataSetChanged();
                }
            }
        }
    */

    /**
     * Called when we leave edit mode in the Checklist screen.
     *
     */
    private void exitEditMode(View view) {
        // Transition to mode where user can check boxes to make
        // an actual Checklist.
//        activity.setSupportActionBar(tbpre); // tbpre is the preflight toolbar.
//        activity.getSupportActionBar().setDisplayShowTitleEnabled(false); // Disable the default title.
//        tbedit.setVisibility(View.GONE);  // Turn  off the edit toolbar.
//        tbpre.setVisibility(View.VISIBLE);  // Turn on the preflight toolbar.

        hideKeyBoard(view);  // Make sure the keyboard is hidden when user arrives on the preflight screen.

        // The menu container holds the list of the buttons across the bottom of (most) screens.
        // Make it visible in preflight.
        LinearLayout linearLayout = (LinearLayout) activity.findViewById(R.id.menu_container);
        linearLayout.setVisibility(View.VISIBLE);

        TextView title = (TextView) activity.findViewById(R.id.check_text);
        title.setText(getString(R.string.checklist_cap));

//        Log.d("gson", cm.checkLists2Json());


        // Transition to the preflight screen.
        PreFlightFragment preflightfrag = new PreFlightFragment();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, preflightfrag, TAG_PREFLIGHT)
                .commit();

    }

}
