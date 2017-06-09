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
 * Created by Ying Zhang on 10/11/16.
 */

/**
 * Permission Slip = Waiver.
 * This gets the text for an unsigned waiver from the Cloudant database.
 * Not currently used.  We have a single string hardcoded somewhere.
 */
public class PermissonSlipResponse {
    //    private String _id;
    //    private String _rev;
    //    private String type;
    @SerializedName("waiver_string")
    private String waiver_string;

    //    public PermissonSlipResponse(String _id, String _rev, String type, String WaverString) {
    //
    //    }

    public PermissonSlipResponse(String waiver_string) {
        this.waiver_string = waiver_string;
    }

    public String getContent() {
        return waiver_string;
    }
}
