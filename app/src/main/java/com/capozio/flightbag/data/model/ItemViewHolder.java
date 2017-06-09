package com.capozio.flightbag.data.model;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.capozio.flightbag.R;


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
 * Created by Ying Zhang on 8/29/16.
 *
 *  Used by the RecyclerListAdapter
 *  Each viewholder represents one entry in the checklist
 */
/** Manages a collection of Entrys to display and entire checklist. */
public class ItemViewHolder extends RecyclerView.ViewHolder {
    public final EditText editText;
    public final CheckBox checkBox;
    public final TextView checklistmsg;
    public final Button notesButton;
    public final ImageButton deleteButton;

    public ItemViewHolder(View itemView, boolean isEditMode) {
        super(itemView);

        // There are two fragments involved.  One for editing the checklist, and one for
        // checking and annotating object in the checklist.
        // initialize different components depending on which fragment is currently running
        if (isEditMode) {
            editText = (EditText) itemView.findViewById(R.id.editmode_edittext);
            checkBox = null;
            checklistmsg = null;
            deleteButton = (ImageButton) itemView.findViewById(R.id.delete_item);
        } else {
            checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
            checklistmsg = (TextView) itemView.findViewById(R.id.checkBoxText);
            editText = null;
            deleteButton = null;
        }
        notesButton = (Button) itemView.findViewById(R.id.notesButton);
    }
}
