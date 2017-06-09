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
 * Created by Ying Zhang on 11/23/16.
 */

/**********    Not Used     ************/

/** Represents both the geo coordinates and postal addresses
 * of a single location.
 * Used to turn a  json string into a proper class. */

 public class GeoResponse {
        public class GeoLocation {
        private InnerGeoLocation geometry;
        private String formatted_address;

        public GeoLocation(String formatted_address, InnerGeoLocation geometry) {
            this.formatted_address = formatted_address;
            this.geometry = geometry;
        }

        public InnerGeoLocation getGeometry() {
            return geometry;
        }

        public String getFormatted_address() {
            return formatted_address;
        }
    }

    public class InnerGeoLocation {
        private Coordinates location;

        public InnerGeoLocation(Coordinates location) {
            this.location = location;
        }

        public Coordinates getLocation() {
            return location;
        }
    }

    public class Coordinates {
        private String lat;
        private String lng;

        public Coordinates(String lat, String lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public String getLat() {
            return lat;
        }

        public String getLng() {
            return lng;
        }
    }

    private List<GeoLocation> results;
    private String status;

    public GeoResponse(List<GeoLocation> results, String status) {
        this.results = results;
        this.status = status;
    }

    public List<GeoLocation> getResults() {
        return results;
    }

    public String getStatus() {
        return status;
    }
}
