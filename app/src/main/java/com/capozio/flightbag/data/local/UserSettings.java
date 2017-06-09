package com.capozio.flightbag.data.local;

import com.airmap.airmapsdk.networking.services.MappingService;
import com.capozio.flightbag.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

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
 * Created by Ying Zhang on 9/23/16.
 */

/** Allows persistence of things for a single user.
 * Currently allows user to see the last  checklist used,
 * The last map layers and types displayed in Plan.
 *
 */
public class UserSettings {
    private int checklistSelectedIdx; // the checklist last selected by the user
    private List<MappingService.AirMapLayerType> mapLayers; // the map layers last selected by the user
    public enum MapType {
        STREET(0), SATELLITE(1), AIRSPACE(2);

        private int mapType;

        private static Map<Integer, MapType> map = new HashMap<>();

        static {
            for(MapType mapEnum : MapType.values()) {
                map.put(mapEnum.mapType, mapEnum);
            }
        }

        private MapType(int map){ mapType = map;}

        public static MapType getMapType(int mapType) {
            return map.get(mapType);
        }
    }


    public static Integer[] ResourceLookup = {R.id.radio_normal,R.id.radio_satellite,R.id.radio_airspace};

    //TODO: Delete this
    //int temp = R.id.radio_normal;
    MapType mSelectedMapType = MapType.STREET;
    /** Contructor.  Single argument uniquely identifies checklist. */
    public UserSettings(int checklistSelectedIdx) {
        this.checklistSelectedIdx = checklistSelectedIdx;
    }

    public UserSettings() {
        mapLayers = new ArrayList<>();
    }

    /** returns the int that uniquely identifies checklist. */
    public int getChecklistSelectedIdx() {
        return checklistSelectedIdx;
    }

    /** sets the int that uniquely identifies checklist. */
    public void setChecklistSelectedIdx(int checklistSelectedIdx) {
        this.checklistSelectedIdx = checklistSelectedIdx;
    }

    /**
     * @return Returns the user's current list of map layers.
     */
    public List<MappingService.AirMapLayerType> getMapLayers() {
        return mapLayers;
    }

    /** Returns the user's maptype, which can be street, satellite, or airspace. */
    public MapType getMapTypeSelected() {
        return mSelectedMapType;
    }

     /**  Sets the user's maptype, which can be street, satellite, or airspace. */
    public void setMapTypeSelected(MapType mapType) {
        this.mSelectedMapType = mapType;
    }


    // Todo. Make mapTypeSelected an enumeration.
}
