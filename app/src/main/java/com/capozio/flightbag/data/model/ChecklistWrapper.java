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
 * Created by Ying Zhang on 8/29/16.
 */
/**
 * Wrapper is necessary for the reasons that
 * 1) allow additional meta data such as timeStamp to be added.
 * 2) couchDB semantics do not allow a json document to
 * start and end with square brackets (a list in json);
 * they need to be wrapped with curly brackets.
 */
public class ChecklistWrapper {
    private String _id; // for Cloudant
    private String _rev; // for Cloudant
    private CheckListData checklistData; // payload
    private String LocalTimestamp;
    private String UTCTimestamp;
    private String type; // two types: 1) "Checklist" for submitted chechlist 2) "Template" per user basis
    private String TemplateName; // used as a primary key to find the template doc in Cloudant
    private String FlightID;

    public ChecklistWrapper(String _id, String _rev, String LocalTimestamp, String UTCTimestamp, CheckListData checklistData, String type, String TemplateName, String FlightID) {
        this._id = _id;
        this._rev = _rev;
        this.UTCTimestamp = UTCTimestamp;
        this.checklistData = checklistData;
        this.LocalTimestamp = LocalTimestamp;
        this.type = type;
        this.TemplateName = TemplateName;
        this.FlightID = FlightID;
    }

    public ChecklistWrapper(String LocalTimestamp, String UTCTimestamp, CheckListData checklistData, String type, String TemplateName, String FlightID) {
        this._id = null;
        this._rev = null;
        this.UTCTimestamp = UTCTimestamp;
        this.checklistData = checklistData;
        this.LocalTimestamp = LocalTimestamp;
        this.type = type;
        this.TemplateName = TemplateName;
        this.FlightID = FlightID;
    }

    public String get_id() {
        return _id;
    }

    public String get_rev() {
        return _rev;
    }

    public CheckListData getChecklistData() {
        return checklistData;
    }
}
