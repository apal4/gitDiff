package com.capozio.flightbag.data.model;

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
 * Created by Ying Zhang on 8/23/16.
 */
/** A single line of the checklist. */
public class Entry {
    private boolean isChecked; // checkbox status - have we checked this item.
    private String checklistmsg; // Title of the checklist item. e.g. Airspace, Batteries.
    private String notes;   // Comments that the user puts in about the item.
    private int severity;   // The severity of the risk pertaining to this checklist item.  Appears when the user adds a note.
    private int likelyhood; // The chance that the even will occur pertaining to this checklist item.
    private int risk_score; // Always the sum of severity and likelyhood.  Redundant.  Requested by C.Johnson.

    public Entry() {
        isChecked = false;
        checklistmsg = "";
        notes = "";
        severity = 1;
        likelyhood = 1;
        risk_score = 2;
    }


    public Entry(String checklist, String notes) {
        this.isChecked = false;
        this.checklistmsg = checklist;
        this.notes = notes;
        severity = 1;
        likelyhood = 1;
        risk_score = 2;
    }

    public Entry(Entry entry) {
        this.isChecked = entry.isChecked;
        this.checklistmsg = entry.checklistmsg;
        this.notes = entry.notes;
        this.severity = entry.severity;
        this.likelyhood = entry.likelyhood;
        this.risk_score = entry.risk_score;
    }

    public boolean getisChecked() {
        return isChecked;
    }

    public String getChecklistmsg() {
        return checklistmsg;
    }

    public String getNotes() {
        return notes;
    }

    public int getSeverity() {
        return severity;
    }

    public int getLikelyhood() {
        return likelyhood;
    }

    public int getRisk_score() {
        return risk_score;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public void setChecklistmsg(String checklistmsg) {
        this.checklistmsg = checklistmsg;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setRiskAssessement(int likelyhood, int severity) {
        this.severity = severity;
        this.likelyhood = likelyhood;
        risk_score = severity * likelyhood;
    }

    @Override
    public String toString() {
        return checklistmsg + ":" + notes;
    }
}
