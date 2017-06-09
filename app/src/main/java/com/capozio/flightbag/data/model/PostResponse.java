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

/** Captures the results of posting a json doucment to CouchDB.
 *  Basically manages the _id and _rev that are returned.
 */
public class PostResponse {
    private String ok;
    @SerializedName("id")
    private String id;
    @SerializedName("rev")
    private String rev;

    public PostResponse(String rev, String id, String ok) {
        this.rev = rev;
        this.id = id;
        this.ok = ok;
    }

    public String getID() {
        return id;
    }

    public String getRev() {
        return rev;
    }
}
