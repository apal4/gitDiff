package com.capozio.flightbag;

import com.airmap.airmapsdk.networking.services.MappingService;
import com.capozio.flightbag.data.local.UserSettings;
import com.capozio.flightbag.data.model.CheckListData;
import com.capozio.flightbag.data.model.Entry;
import com.capozio.flightbag.data.model.PilotDataResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;



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
 * Created by Ying Zhang on 8/29/16.
 *
 *  Interface for data communication among fragments
 */

/**
 * This class is used for sending data between the activity and various fragments.
 */

//TODO: destroy this-- this interface is only implemented once and therefore is not needed
public interface Communication {

    List<CheckListData> getChecklists();

    void sendCheckLists(List<CheckListData> checklist);

    void sendTemplate(CheckListData template);

    CheckListData getTemplate();

    int getChecklistIdx();

    void sendCheckListIdx(int idx);

    // String checkLists2Json();

    // used by NoteDialogFragment
    Entry getNotes();

    void sendNotes(Entry entry);

    void notifyEditMode(boolean isChecklistEditMode);

    boolean getEditMode();

    List<MappingService.AirMapLayerType> getMapLayers();

    Map<MappingService.AirMapLayerType, Integer> getIDMap();

    UserSettings.MapType getMapType();

    void setMapType(UserSettings.MapType mapType);

    UUID getFlightID();

    PilotDataResponse.PilotData getPilotData();

}
