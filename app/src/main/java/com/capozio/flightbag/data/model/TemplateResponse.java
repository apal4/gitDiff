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
 * Created by Ying Zhang on 9/13/16.
 */


/** Manages the response from reading a Checklist template from the Couch database.
 *
 */
public class TemplateResponse {
    private String total_rows; // Cloudant semantics
    private String offset; // Cloudant semantics
    private List<TemplateWrapper> rows; // query results. Each row corresponds to a single template.

    public TemplateResponse(String total_rows, String offset, List<TemplateWrapper> rows) {
        this.total_rows = total_rows;  // Never used?
        this.offset = offset;          // Never used?
        this.rows = rows;
    }

    public List<TemplateWrapper> getRows() {
        return rows;
    }
}
