package com.capozio.flightbag.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

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
 */

/** Manages the contents of checklist. */
public class CheckListData {
    // checklistName appears at the top of screen. Defaults to <NAME>
    // checklist is list containing the entries.
    // PermissionSlip is the text of the permission slip (a string)
    // It is not signed by the homeowner (yet).
    @SerializedName("checklistName")
    private String checklistName;
    @SerializedName("checklist")
    private List<Entry> checklist;
    @SerializedName("permissonSlip")
    private String permissonSlip;

    private transient boolean isSigned;

    /** Constructs a checklist, using the ChecklistName, checklist, and permissionSlip
    // in this function only the reference is passed into, thus no new checklist is created */
    public CheckListData(String checklistName, List<Entry> checklist, String permissonSlip) {
        this.checklistName = checklistName;
        this.checklist = checklist;
        this.permissonSlip = permissonSlip;
        this.isSigned = false;
    }


    /** Returns a clone the "CheckListData" Object */
    public CheckListData newInstance() {
        List<Entry> newlist = new ArrayList<>();
        for (Entry e : checklist)
            newlist.add(new Entry(e));

        return new CheckListData(checklistName, newlist, permissonSlip);
    }

    public List<Entry> getChecklist() {
        return checklist;
    }

    public String getChecklistName() {
        return checklistName;
    }

    public void setChecklistName(String checklistName) {
        this.checklistName = checklistName;
    }

    public void setChecklist(List<Entry> checklist) {
        this.checklist = checklist;
    }

    public String getPermissonSlip() {
        return permissonSlip;
    }

    public boolean hasPermissonSlip() {
        return permissonSlip != null;
    }

    public void setPermissonSlip(String permissonSlip) {
        this.permissonSlip = permissonSlip;
    }

    public void setSigned() {
        isSigned = true;
    }

    public boolean isSigned() {
        return isSigned;
    }

    /**
     * add a new entry, default with empty content
     */
    public void addEntry() {
        checklist.add(new Entry());
    }

}
