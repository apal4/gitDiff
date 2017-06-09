package com.capozio.flightbag.data.model;

import android.location.Location;
import android.os.Environment;
import android.view.ViewGroup;

import com.capozio.flightbag.util.Configs;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
 * Created by Ying Zhang on 1/13/16.
 *
 */
 /**
 * Helps manage  search results for finding waivers for locations near a given location.
 */
public class PermissonSearchResult {
     private List<PermissonWrapper> metalist;
     //    private List<String> pdfPath;
     private File externalDir;
     private List<PermissonWrapper> metaResult;

     // The target location that all other distances are calculated from.
     private Location myLocation;
     private final float MILE2METER = 1.60934f;
//    private List<String> pdfPathResult;

     /**
      * Compares two PermissionWrappers, returning a negative int, zero, positive int.
      *
      */
    private Comparator<PermissonWrapper> locationCmp = new Comparator<PermissonWrapper>() {
        @Override
        public int compare(PermissonWrapper pw1, PermissonWrapper pw2) {
            float lat1 = Float.parseFloat(pw1.getMetaData().getLatitude());
            float lon1 = Float.parseFloat(pw1.getMetaData().getLongitude());
            float lat2 = Float.parseFloat(pw2.getMetaData().getLatitude());
            float lon2 = Float.parseFloat(pw2.getMetaData().getLongitude());

            // Find the distancesfrom pw1 to target location.
            float dis1 = getDistance(lat1, lon1);
            // ... and the distance from target to pw2.
            float dis2 = getDistance(lat2, lon2);
            // Then compare the distances.
            return Float.compare(dis1, dis2);
        }
    };

    private float getDistance(float lat, float lon) {
   return 0;
    }

     private float getHaversineDistance(float lat1, float lon1, float lat2, float lon2) {
         final int R = 6371; // Radious of the earth
         float latDistance = toRad(lat2-lat1);
         float lonDistance = toRad(lon2-lon1);
         float a = (float)(Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                 Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
                         Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2));
         float c = (float)(2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)));
         return R * c;
     }

     private float toRad(float value) {
         return (float)(value * Math.PI / 180);
     }

    /**
     * Constructor
     * @param myLocation the Target location. All distances are calculated from this point.
     */
    public PermissonSearchResult(Location myLocation) {
        // Directory where we find all existing signed waivers.
        externalDir = new File(Environment.getExternalStorageDirectory(), Configs.WAIVER_PATH);
        // The meta data for all existing signed waivers.
        metalist = new ArrayList<>();
        // An ordered list of signed waiver stuff, order by distance from the target location.
        metaResult = new ArrayList<>();
        // Target location.
        this.myLocation = myLocation;

        //  The signed waiver directory has a bunch of subdirectories, each named with a UUID.
        //  each subdirectory will have three files for a single signed waiver.
        //  1. The PDF itself.  signedWaiver.pdf (see Config.PDF_FILENAME
        //  2. The metadata, in json format.  meta-data.json (see Config.META_FILENAME)
        //  3. Status file - tells us if the email has been sent, or the file uploaded.
        //       This file is not used in this class.


        // iterate through all the permission slips and populate the data into lists.
        // Each file is a json doc, named with a UUID.

        File[] allFiles = new File(externalDir.toString()).listFiles();
        if (allFiles != null) {
            for (File uuidDir : allFiles) {
                String uuid = uuidDir.getName();
                try {
                    // Read the meta data json into a string.
                    BufferedReader br = new BufferedReader(
                            new FileReader(externalDir + "/" + uuid + "/" + Configs.META_FILENAME));

                    // Construct an instance of PDFWrapper holding the metadata.
                    PDFWrapper newMeta = new Gson().fromJson(br, PDFWrapper.class);
                    // The path to the pdf file.
                    String newpdf = externalDir + "/" + uuid + "/" + Configs.PDF_FILENAME;
                    // Make the list of metadata for all signed waivers.
                    metalist.add(new PermissonWrapper(newMeta, newpdf));
                    // Construct a location using the metadata for this location/pdfWaiver.
                    Location location = new Location("");
                    location.setLatitude(Float.parseFloat(newMeta.getLatitude()));
                    location.setLongitude(Float.parseFloat(newMeta.getLongitude()));

                    // Make an ordered list of all locations that are with .25 miles the target location.
                    // on startup, the result will only include permissons that are within 0.25 mile radius
                    if(location.distanceTo(myLocation)/1000 <= 0.25 * MILE2METER) {
                        metaResult.add(new PermissonWrapper(newMeta, newpdf));
                    }
                } catch (FileNotFoundException e) {}
            }
        }
    }

     /**
      *
      * @return an list of all metadata this will be displayed on the screen.
      * This list is not sorted.
      */
    public List<PermissonWrapper> getMetaResult() {
        return metaResult;
    }

    public List<PermissonWrapper> searchByRadius(String radiusString) {
        return searchByRadius(radiusString, myLocation);
    }


    /**
     * returns an ordered list by the distance from the target.
     * Only locations within the given radius will be returned.
     * @param radiusString
     * @param location  -- The target location for the search.
     * @return
     */
    public List<PermissonWrapper> searchByRadius(String radiusString, Location location) {
        // Clear the result of any locations in a previous search.
        metaResult.clear();

        // If there is no valid radius string, return a list with all locations.
        if(radiusString.isEmpty() || Float.parseFloat(radiusString) == 0) {
            metaResult.addAll(metalist);
        } else {
            // Otherwise, return only things within the radius.
            float radius = Float.parseFloat(radiusString)* MILE2METER;

            for(PermissonWrapper permission: metalist) {
                Location mLocation = new Location("");
                mLocation.setLatitude(Float.parseFloat(permission.metaData.getLatitude()));
                mLocation.setLongitude(Float.parseFloat(permission.metaData.getLongitude()));
                // Todo: Check with with all the rest of the math.
                if (mLocation.distanceTo(location) / 1000 <= radius)
                    metaResult.add(new PermissonWrapper(permission.metaData, permission.pdfPath));
            }
        }

        // sort by  distance from the target, closest locations at the front of list.
        Collections.sort(metaResult, locationCmp);
        return metaResult;
    }

    /**
     * Search for permissons that using an  address to generate the target location.
     * @param address
     * @return
     */
    public List<PermissonWrapper> searchByAddress(String address) {
        metaResult.clear();

        for(PermissonWrapper permission: metalist) {
            String addr = permission.metaData.getStreetAddress()+", "+permission.metaData.getCity()+", "+permission.metaData.getState();
            if(addr.startsWith(address))
                metaResult.add(new PermissonWrapper(permission.metaData, permission.pdfPath));
        }

        // sort by least distance
        Collections.sort(metaResult, locationCmp);
        return metaResult;
    }
    // Todo:  PermissionWrapper could be private to this class.
}
