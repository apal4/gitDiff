package com.capozio.flightbag.data.model;

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
 * Created by Ying Zhang on 11/11/16.
 */
/** Cloudant specific queries for finding Waivers in database. */
public class PermissonSlipMetaDataResponse {
    private String total_rows; // Cloudant semantics
    private String offset;     // Cloudant semantics
    private List<PermissonData> rows; // query results

    public PermissonSlipMetaDataResponse(String total_rows, String offset,
                                         List<PermissonData> rows) {
        this.total_rows = total_rows;
        this.offset = offset;
        this.rows = rows;
    }

    public List<PermissonData> getRows() {
        return rows;
    }
}
