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
 * Created by Ying Zhang on 9/22/16.
 */

/** Manages the uploading of checklist templates to Couch database. */
public class TemplateWrapper {
    private String id; // Cloudant semantics
    private String key; // Cloudant semantics
    private ChecklistWrapper value; // payload

    public TemplateWrapper(String id, String key, ChecklistWrapper value) {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    public ChecklistWrapper getValue() {
        return value;
    }
}
