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
 * Created by Ying Zhang on 11/30/16.
 */
/** Used to manage signed Waivers that are already present in the database. */
// Todo. This can probably be merged with PDFWrapper in a single class.
public class PermissonData {
    private String id;    // CouchDB _id for the signed waiver.
    private PDFWrapper key;

    public PermissonData(PDFWrapper key, String id) {
        this.key = key;
        this.id = id;
    }

    public PDFWrapper getKey() {
        return key;
    }

    public String getId() {
        return id;
    }
}