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
 * Created by Ying Zhang.
 */

import java.util.List;

/** Captures data about the pilot. */

public class PilotDataResponse{
    List<PilotData> docs;

    PilotDataResponse(List<PilotData> docs) {
        this.docs = docs;
    }

    public PilotData getPilotData() {
        return docs.get(0);
    }

    public class PilotData {
        String _id;
        String type;
        String Pilot_ID;
        String RPIC_Cert;
        String FirstName;
        String LastName;
        String EmailAddress;

        public PilotData(String _id, String type, String pliot_ID, String RPIC_Cert, String firstName, String lastName, String emailAddress) {
            this._id = _id;
            this.type = type;
            Pilot_ID = pliot_ID;
            this.RPIC_Cert = RPIC_Cert;
            FirstName = firstName;
            LastName = lastName;
            EmailAddress = emailAddress;
        }

        public String getPilot_ID() {
            return Pilot_ID;
        }

        public String getRPIC_Cert() {
            return RPIC_Cert;
        }

        public String getFirstName() {
            return FirstName;
        }

        public String getLastName() {
            return LastName;
        }

        public String getEmailAddress() {
            return EmailAddress;
        }
    }
}
/*
"_id": "c251099d83dffdb1bad65e3e0cf2baf2",
        "_rev": "2-945e39e190c7510edc00c9ee0ce280c3",
        "type": "Pilot",
        "Pilot_ID": "007",
        "RPIC_Cert": "1233456",
        "FirstName": "Pilot",
        "LastName": "Training",
        "Username": "cjohnson",
        "PasswordToken": "be1d22baf69c4226bb38f77c68318afc",
        "EmailAddress": "a@a.com",
        "StreetAddress": "4620 Frey St",
        "City": "Madison",
        "State": "WI",
        "ZipCode": "53705",
        "EULA_Agreement": "1234",
        "EULA_Date": "2017-01-12T00:00:00Z",
        "EULA_Expiration": "2018-01-12T00:00:00Z",
        "PhoneNumber": "(608) 262-8451"
*/