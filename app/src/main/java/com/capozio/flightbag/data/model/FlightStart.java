package com.capozio.flightbag.data.model;

/**
 * Created by PTC on 2/26/2017.
 */

/** Captures the time, position, pilot data, etc at the start of a flight,
 * using data captured from the android device.
 *
 */
public class FlightStart {
    private String FlightID;
    private String type = "FlightStart";
    private String Pilot_ID;
    private String RPIC_Cert;
    private String EmailAddress;
    private String LocalTimestamp;
    private String UTCTimestamp;


    public FlightStart(String flightID, String pilot_ID, String RPIC_Cert, String emailAddress, String localTimestamp, String UTCTimestamp) {
        FlightID = flightID;
        Pilot_ID = pilot_ID;
        this.RPIC_Cert = RPIC_Cert;
        EmailAddress = emailAddress;
        LocalTimestamp = localTimestamp;
        this.UTCTimestamp = UTCTimestamp;
    }
}
