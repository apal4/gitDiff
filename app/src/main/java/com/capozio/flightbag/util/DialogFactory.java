package com.capozio.flightbag.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.capozio.flightbag.Communication;
import com.capozio.flightbag.R;
import com.capozio.flightbag.data.model.CheckListData;
import com.capozio.flightbag.feature.preflight.PreFlightFragment;

import java.util.List;

import static com.capozio.flightbag.util.Configs.TAG_PREFLIGHT;
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
 * Created by Ying Zhang on 9/20/16.
 */

/**
 *  Create a number of dialogs alerting the user about various things.
 */
public final class DialogFactory {

    // simple dialog that has only one button
    private static AlertDialog.Builder createSimpleOkDialogBuilder(Context context, @StringRes int message) {
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_simpleok, null);
        TextView msg = (TextView) view.findViewById(R.id.dialog_msg);
        msg.setText(message);

        //msg.setGravity(Gravity.CENTER);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
//                .setTitle(R.string.dialog_title_err)
//                .setIcon(R.drawable.ic_info)
//                .setView(msg)
                .setView(view)
                .setCancelable(false)
                .setNeutralButton(R.string.dialog_action_ok, null);
        return alertDialog;
    }

    public static AlertDialog createSimpleOkDialog(Context context, @StringRes int message) {
        return createSimpleOkDialogBuilder(context, message).create();
    }


    // Specifically for prompting the user for the checklist name when the "Add checklist" button is pressed
    public static AlertDialog createEditDialog(final Context context, final List<CheckListData> checklists) {
        // inflate custom layout
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_newchecklist, null);

        final AppCompatActivity activity = (AppCompatActivity) context;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setView(view) // bind the custom view
                .setCancelable(false) // dismissing dialog by clicking outside the dialog is not allowed
                .setPositiveButton(R.string.dialog_action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userinput = ((EditText) view.findViewById(R.id.dialog_checklist_name)).getText().toString().trim();
                        // update the checklist name in the editmode toolbar with the user input
                        EditText tbedit = (EditText) activity.findViewById(R.id.toolbar_title_edit);

                        checklists.get(checklists.size() - 1).setChecklistName(userinput);
                        tbedit.setText(userinput);
                        hideKeyBoard(view);

                        DialogFactory.createGetPermissonSlipDialog(activity,
                                R.string.dialog_isPermissonNeeded).show();
                    }
                })
                .setNegativeButton(R.string.dialog_action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // delete the newly created checklist
                        checklists.remove(checklists.size() - 1);
                        ((Communication) context).sendCheckListIdx(checklists.size() - 1);

                        // Exit edit mode to where user can check boxes to make
                        // an actual Checklist.

                        hideKeyBoard(view);  // Make sure the keyboard is hidden when user arrives on the preflight screen.

                        // The menu container holds the list of the buttons across the bottom of (most) screens.
                        // Make it visible in preflight.
                        LinearLayout linearLayout = (LinearLayout) activity.findViewById(R.id.menu_container);
                        linearLayout.setVisibility(View.VISIBLE);

                        TextView title = (TextView) activity.findViewById(R.id.check_text);
                        title.setText(context.getString(R.string.checklist));

                        // Transition to the preflight screen.
                        PreFlightFragment preflightfrag = new PreFlightFragment();
                        activity.getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.container, preflightfrag, TAG_PREFLIGHT)
                                .commit();
                    }
                });

        return alertDialog.create();
    }

    // create a dialog with two buttons
    public static AlertDialog.Builder createYesNoDialog(Context context, @StringRes int message) {
        TextView msg = new TextView(context);
        msg.setText(message);
        msg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        msg.setGravity(Gravity.CENTER);
        msg.setHeight(250);

        return new AlertDialog.Builder(context)
                .setCancelable(false)
                .setView(msg);
    }


    // prompt asking if a permisson slip is required
    public static Dialog createGetPermissonSlipDialog(final Context context, @StringRes int message) {
        // TODO: hard-coded
        final String userid = "db0";

        AlertDialog.Builder builder = createYesNoDialog(context, message);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {

                // inflate and adjust layout to take up 90% of the width of the screen
                final View view = LayoutInflater.from(context).inflate(R.layout.dialog_view_permissonslip, null);
                TextView textView = (TextView) view.findViewById(R.id.dialog_textview_permisson);
                textView.setText(Html.fromHtml(Configs.permissonText));

                // view the permisson text.

                createViewAndSetPermissonSlipDialog(context, Configs.permissonText, view, 0.9f).show();

/*   USED TO GET PERMISSON TEXT FROM THE CLOUDANT SERVER
                final RestInterface restService = RestClient.getInstance().create(RestInterface.class);

                Call<PermissonSlipResponse> mCall = restService.getPermissionText(userid);
                mCall.enqueue(new Callback<PermissonSlipResponse>() {
                    @Override
                    public void onResponse(Call<PermissonSlipResponse> call, final Response<PermissonSlipResponse> response) {
                        Log.d("TAG", "Get Permisson Slip Success!");
                        // get the psermisson slip text
                        final String content = response.body().getContent();

                        // if the permisson is required,
                        // then there will be a "permisson" button appearing in the toolbar
                        // which allows user to view the permisson text.

//                        permissonButton.setVisibility(View.VISIBLE);

                        // inflate and adjust layout to take up 90% of the width of the screen
                        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_view_permissonslip, null);
                        TextView textView = (TextView) view.findViewById(R.id.dialog_textview_permisson);
                        textView.setText(Html.fromHtml(content));

                        // view the permisson text.

                        createViewAndSetPermissonSlipDialog(context, content, view, 0.9f).show();
                    }

                    @Override
                    public void onFailure(Call<PermissonSlipResponse> call, Throwable t) {
                        Toast.makeText(context, "Get Permisson Slip Failed! Check Your Internet Connection.", Toast.LENGTH_LONG).show();
                    }
                });*/
//                                File pdfFile = download(response, fileName);
                //                               Uri path = Uri.fromFile(pdfFile);
//                                Intent intent = new Intent(Intent.ACTION_VIEW);
//                                intent.setDataAndType(path, "application/pdf");
//
//                                context.startActivity(intent);
            }
        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                permissonButton.setVisibility(View.INVISIBLE);
            }
        });
        return builder.create();
    }

    // Generic dialog builder for viewing the permisson text
    private static AlertDialog.Builder singleViewBuilder(final Context context, View view, float ratio) {
        // retrieve display dimensions
        Rect displayRect = new Rect();
        ((AppCompatActivity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(displayRect);

        // inflate and adjust layout to take up 90% of the width of the screen
        view.setMinimumWidth((int) (displayRect.width() * ratio));

        AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(view);
        return builder;
    }

    public static AlertDialog createViewPermissonSlipDialog(final Context context, View view, float ratio) {
        return singleViewBuilder(context, view, ratio)
                .setPositiveButton(R.string.dialog_action_ok, null)
                .create();
    }

    // If the user clicks "back", it will go back to the dialog that asks whether a permisson slip is required.
    public static AlertDialog createViewAndSetPermissonSlipDialog(final Context context,
                                                                  final String content, View view, float ratio) {
        return singleViewBuilder(context, view, ratio)
                .setPositiveButton(R.string.dialog_action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Communication cm = (Communication) context;
                        cm.getChecklists().get(cm.getChecklistIdx()).setPermissonSlip(content);
                    }
                })
                .setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DialogFactory.createGetPermissonSlipDialog(context, R.string.dialog_isPermissonNeeded).show();
                    }
                }).create();
    }

}

