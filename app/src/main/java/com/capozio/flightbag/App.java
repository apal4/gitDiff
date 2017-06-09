package com.capozio.flightbag;

import android.app.Application;
import android.content.Context;

import com.capozio.flightbag.util.GoogleApiHelper;

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

/**
 * Singleton class. Should be doing initialization.
 */

public class App extends Application{
    private static GoogleApiHelper mGoogleApiHelper;
    private static Context mContext;

    // TODO: setHTML() deprecated
    // TODO: add a field "mapboxStreetAddress" in json

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();
        mGoogleApiHelper = new GoogleApiHelper(this);

    }

    public static GoogleApiHelper getGoogleApiHelper() {
        if(mGoogleApiHelper != null)
            return mGoogleApiHelper;
        else
            return new GoogleApiHelper(mContext);
    }



}
