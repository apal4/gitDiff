package com.capozio.flightbag.data.model;

import com.google.gson.annotations.SerializedName;
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
 * Created by Ying Zhang on 10/12/16.
 */

/** Used to construct json to move a signed waiver to the Couch database. */
public class PDFWrapper {
    @SerializedName("_id")
    private String _id;
    private String type;
    private String StreetAddress;
    private String City;
    private String State;
    private String Resident;
    private String Longitude;
    private String Latitude;
    private String LocalTimestamp;
    private String UTCTimestamp;

    public PDFWrapper(String _id, String type, String streetAddress, String city, String state, String resident,
                      String longitude, String latitude, String localTimestamp, String UTCTimestamp) {
        this._id = _id;
        City = city;
        Latitude = latitude;
        LocalTimestamp = localTimestamp;
        Longitude = longitude;
        Resident = resident;
        State = state;
        StreetAddress = streetAddress;
        this.type = type;
        this.UTCTimestamp = UTCTimestamp;
    }

    public String get_id() {
        return _id;
    }

    public String getResident() {
        return Resident;
    }

    public String getCity() {
        return City;
    }

    public String getState() {
        return State;
    }

    public String getStreetAddress() {
        return StreetAddress;
    }

    public String getLocalTimestamp() {
        return LocalTimestamp;
    }

    public String getLatitude() {
        return Latitude;
    }

    public String getLongitude() {
        return Longitude;
    }
}