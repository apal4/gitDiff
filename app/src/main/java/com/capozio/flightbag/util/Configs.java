package com.capozio.flightbag.util;

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
 * Created by Ying Zhang on 9/20/16.
 *
 *
 */

/**
 * Configuration settings used within the system, not exposed to user.
 */
public class Configs {

    private Configs() {}

    // Verbage for an unsigned permissions waiver. Should be moved to database eventually.
    public static final String  permissonText = "<br><p>To property owner,</p><p>American Family Mutual Insurance Company (American Family) has received permission from the Federal Aviation Administration (FAA) to evaluate use of Unmanned Aircraft Systems (UAS) in our business, including collection of aerial photography and videography data.  We are currently evaluating opportunities to improve the roof-inspection process in an attempt to enhance the claim experience of our customers. </p><p>We request permission to fly over your property. </p><p>" +
            "All safety and flight regulations will be followed in compliance with the FAA, state, and local guidelines.</p><p>" +
            "We intend to limit all imagery and data collection to your property.  </p><p>" +
            "Our intention is to be unobtrusive and conduct the flight operation as quickly as possible.</p><p>" +
            "Thank you in advance for your cooperation.</p><h1>Authorization and Release</h1><p>" +
            "By signing below, I authorize American Family to conduct Unmanned Aircraft Systems (UAS) flights over my dwelling and/or business.  I hereby release and discharge American Family and its subsidiaries, affiliates, agents, officers, directors, principals and employees from any and all liability, claims, grievances or demands, known or unknown, arising from, related to or associated with UAS research, operations or video and/or photographic recordings of American Family client’s property obtained in accordance with this authorization.</p><p>" +
            "If any provision of this authorization is found to be unenforceable in any respect by a court, it is my intention and understanding that this authorization shall nonetheless be enforced to the maximum extent to which it is found by the court to be legally enforceable. To the extent permitted by applicable law, I hereby waive the benefit of any provisions of any statute or other law that might adversely affect the rights of American Family under this authorization and release.</p><p>" +
            "This release of liability does not apply to any real or personal property damage or bodily injury caused directly by American Family’s use of a UAS.</p><p>" +
            "This authorization shall be governed by the laws of the State of Wisconsin without reference to its choice of law rules. I irrevocably consent to the exclusive jurisdiction and venue of the federal and state courts located in Dane County, Wisconsin with respect to any claim or suit arising out of or in connection with this authorization and agree not to commence or prosecute any such claim or suit other than in the aforementioned courts.</p><p>" +
            "This release of liability applies to the undersigned and his or her heirs, assigns, personal representatives and estate.</p><p>" +
            "I have read this release of liability before signing below, and I fully understand the contents.</p><p>" +
            "I authorize American Family to conduct UAS flight research of my dwelling and/or business.</p>";

    public static final String TAG = "TAG";

    // Specifies the type used in Cloudant.
    public static final String TYPE_TEMPLATE = "ChecklistTemplate";
    public static final String TYPE_CHECKLIST = "Checklist";
    public static final String TYPE_PERMISSONWAIVER = "HomeOwner";
    //    public static final String TIME_FORMAT_UTC = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    // Standard format for storing local times in Cloudant.
    public static final String TIME_FORMAT_LOCAL = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String CHECKLIST_EDIT_MODE = "CHECKLIST EDITOR";
    public static final String TEMPLATE_EDIT_MODE = "CHECKLIST TEMPLATE EDITOR";

    /* TAGs for identifying fragments*/
    public static final String TAG_PREFLIGHT = "tag_preflight";
    public static final String TAG_CHECKLIST_EDIT = "tag_checklist_edit";
    public static final String TAG_TEMPLATE_EDIT = "tag_template_edit";

    /* File names for local storage */
    public static final String DEFAULT_TEMPLATE_FILE = "default_template_file";
    public static final String CHECKLISTS_FILE = "checklists_file";
    public static final String SHAREDPREF_KEY = "sharedpref_key";
    public static final String USERSETTING_FILE = "usersetting_file";

    /* Email Services */
    public static final String EMAIL = "pts.test0@gmail.com";  // The sender of waiver emails.
    public static final String PASSWORD = "PTS_1234"; // Password to senders account.
    public static final String DEFAULT_SUBJECT = "Authorization and Release";
    public static final String DEFAULT_MSG = "";
//    public static final String BCC_EMAIL = "info@pilottrainingsystem.com";
//    public static final String BCC_EMAIL = "thorsage@amfam.com";

    public static final String BCC_EMAIL = "capozio25@hotmail.com";

    public static final String STATUS_EMAIL = "email_sent";
    public static final String EMAIL_ADDR = "email_address";
    public static final String STATUS_PDF = "pdf_sent";
    // Directory on device where all waiver pdfs are stored.
    public static final String WAIVER_PATH = "SignedWaiver";

    public static final String PDF_FILENAME = "signedWaiver.pdf";
    public static final String STAT_FILENAME = "status.json";
    public static final String META_FILENAME = "meta-data.json";

    // The Cloudant database where waivers are stored.
    public static final String PERMISSONDBID = "demo1";
    // Cloudant DB where all of other data is stored.
    public static final String DATADBID = "db0";
    //used for testing the DataConnnector

    private static String mDataStoreName = "my_datastore3";
    private static String mDatabaseName = "test8";


}
