package com.capozio.flightbag.feature.plan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.models.status.AirMapStatusAdvisory;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.capozio.flightbag.Communication;
import com.capozio.flightbag.R;
import com.capozio.flightbag.data.local.UserSettings;
import com.capozio.flightbag.util.ToastUtil;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.Constants;
import com.mapbox.services.android.geocoder.ui.GeocoderAutoCompleteView;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v5.DirectionsCriteria;
import com.mapbox.services.directions.v5.MapboxDirections;
import com.mapbox.services.directions.v5.models.DirectionsResponse;
import com.mapbox.services.directions.v5.models.DirectionsRoute;
import com.mapbox.services.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.geocoding.v5.models.CarmenFeature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
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
 */

// MapBox has its own locations services, which might be superior to Google Play.
// The code is currently using both, for historical reasons.

// Requires an internet connection.

/**
 *   Fragment for the Plan screen.
 */
public class planFragment extends Fragment implements OnMapReadyCallback {

    private static final int PERMISSION_ACCESS_CODE = 11;
    private MapboxMap mMap;
    private MapView mapView;
//    private GoogleApiHelper mGoogleApiHelper;
//    private Location myLocation;

    // the map layers the user currently selected
    private List<MappingService.AirMapLayerType> mapLayers;
    // For drawing a route on the map. The start and end-points of the route.
    private static Position fromPosition;
    private static Position toPosition;

    private com.mapbox.mapboxsdk.location.LocationServices locationServices;
    // currentTheme has 3 states: Standard, Satellite, null(for the VFR Raster Map)
    private MappingService.AirMapMapTheme currentTheme = MappingService.AirMapMapTheme.Standard;
    //
    private static Map<MappingService.AirMapLayerType, Integer> id2layers;
    private String ACCESS_TOKEN;
    private static final int MODE_SETLOCATION = 1;
    private static final int MODE_GETLOCATION = 2;
    private static int locationMode = 1;
    private static boolean isCheckAll = false;
    private static boolean getLocationOnly = false; // when set to true, will force a location update when user resumes the plan fragment
    private Marker advisoryMarker;
    private UserSettings.MapType mapType;


    /**
     * This autoCompleteView can record the location only when user clicks one of the provided options
     */
    private static GeocoderAutoCompleteView.OnFeatureListener fromAddrListener
            = new GeocoderAutoCompleteView.OnFeatureListener() {
        @Override
        public void OnFeatureClick(CarmenFeature feature) {
            fromPosition = feature.asPosition();
        }
    };

    private static GeocoderAutoCompleteView.OnFeatureListener toAddrListener
            = new GeocoderAutoCompleteView.OnFeatureListener() {
        @Override
        public void OnFeatureClick(CarmenFeature feature) {
            toPosition = feature.asPosition();
        }
    };

    public planFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
    // Allows us to use MapBox.
        ACCESS_TOKEN = getString(R.string.access_token);

        // provides the mapping from checkbox id to the map layer type
        id2layers = ((Communication)getActivity()).getIDMap();

        locationServices = com.mapbox.mapboxsdk.location.LocationServices.getLocationServices(getContext());

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_plan, container, false);
        mapView = (MapView) rootView.findViewById(R.id.map_fragment);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

//        MapsInitializer.initialize(getContext());

        // the async callback is "OnMapReady"
        // Initialize the callback that ...
        mapView.getMapAsync(this);

//        if(mGoogleApiHelper == null)
//        mGoogleApiHelper = App.getGoogleApiHelper();

        return rootView;
    }


    // TODO: Use a geocoder in case the user didn't select the autocompleted address
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setRetainInstance(true);

        // The location buttong is a circle with four line segments in the upper right.
        // Pushing it centers the map on the current location, using setMyLocation() below.
        ImageButton locationButton = (ImageButton) getActivity().findViewById(R.id.button_mylocation);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMyLocation();
            }
        });

        // when the "layer" button is clicked, it shows a dialog that allows user to add and remove layers.
        // The layer button looks like a stack of papers.
        ImageButton layerButton = (ImageButton) getActivity().findViewById(R.id.button_layers);
        layerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Bring up the dialog with a list of layers.
                final AlertDialog layerDialog = new AlertDialog.Builder(getContext()).setView(R.layout.dialog_layers).setCancelable(false).create();

                layerDialog.show();
                // set checkbox to true for the layers user previously selected
                for (MappingService.AirMapLayerType layer: mapLayers) {
                    int resID = id2layers.get(layer);
                    ((CheckBox)layerDialog.findViewById(resID)).setChecked(true);
                }

                ImageButton layerButton = (ImageButton) layerDialog.findViewById(R.id.button_layer_done);
                layerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (currentTheme == null)
                            // This is the case where the user choses "Airspace" from  lower-left.
                            // Null means user our own map.
                            // Our own map is if the theme is a VFR map, there is no need to have layers on it
                            // We are currently the free version.
                            // If we have our own server, we would replace this with our own url.

                            // This was made by hand by Ying, and contains a VFR for Chicago area only, superimposed on
                            // a bare-bones map of the coastline.
                            // The map was made using mapbox studio (/www.mapbox.com/studio)
                            // There is limit to the size of map that they will host for free on their website.
                            mMap.setStyleUrl("mapbox://styles/pliottrainingsystem/ciwxya19b000n2qpkasrd95od");
                        else
                            // This is the case where the user selects Satellite or Street from the lower-right.
                            // Set the layers and the theme using Mapbox technology.
                            mMap.setStyleUrl(AirMap.getTileSourceUrl(mapLayers, currentTheme));
                        layerDialog.dismiss();
                    }
                });

                // select all the checkboxes if user previously selected the "selectall" checkbox
                CheckBox selectAll = (CheckBox) layerDialog.findViewById(R.id.check_selectall);
                selectAll.setChecked(isCheckAll);

                // when the "selectAll" button is selected/deselected, need to check/uncheck all the checkboxes
                // and add/remove all the map layers.
                final LinearLayout linearLayout = (LinearLayout) layerDialog.findViewById(R.id.checkBoxlist);
                selectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int count = linearLayout.getChildCount();
                        if(isChecked) {
                            for(int i = 1; i < count; i++) {
                                View view = linearLayout.getChildAt(i);
                                if (view instanceof CheckBox)
                                    ((CheckBox) view).setChecked(true);
                            }
                            isCheckAll = true;
                            mapLayers.addAll(id2layers.keySet());
                        } else {
                            for(int i = 1; i < count; i++) {
                                View view = linearLayout.getChildAt(i);
                                if (view instanceof CheckBox)
                                    ((CheckBox) view).setChecked(false);
                            }
                            isCheckAll = false;
                            mapLayers.clear();
                        }
                    }
                });

                // Each row of the Layer dialog has its own checkbox. This code adds or removes the
                // the corresponding layer from the list of layers.
                // add/remove certain map layer when the corresponding checkbox is selected/deselected.
                ((CheckBox)layerDialog.findViewById(R.id.check_commercial)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) mapLayers.add(MappingService.AirMapLayerType.AirportsCommercial);
                        else          mapLayers.remove(MappingService.AirMapLayerType.AirportsCommercial);
                    }
                });

                ((CheckBox)layerDialog.findViewById(R.id.check_pa)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) mapLayers.add(MappingService.AirMapLayerType.AirportsCommercialPrivate);
                        else          mapLayers.remove(MappingService.AirMapLayerType.AirportsCommercialPrivate);
                    }
                });

                ((CheckBox)layerDialog.findViewById(R.id.check_class_b)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) mapLayers.add(MappingService.AirMapLayerType.ClassB);
                        else          mapLayers.remove(MappingService.AirMapLayerType.ClassB);
                    }
                });

                ((CheckBox)layerDialog.findViewById(R.id.check_class_c)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) mapLayers.add(MappingService.AirMapLayerType.ClassC);
                        else          mapLayers.remove(MappingService.AirMapLayerType.ClassC);
                    }
                });

                ((CheckBox)layerDialog.findViewById(R.id.check_class_d)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) mapLayers.add(MappingService.AirMapLayerType.ClassD);
                        else          mapLayers.remove(MappingService.AirMapLayerType.ClassD);
                    }
                });

                ((CheckBox)layerDialog.findViewById(R.id.check_class_e0)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) mapLayers.add(MappingService.AirMapLayerType.ClassE);
                        else          mapLayers.remove(MappingService.AirMapLayerType.ClassE);
                    }
                });

                ((CheckBox)layerDialog.findViewById(R.id.check_psua)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) mapLayers.add(MappingService.AirMapLayerType.Prohibited);
                        else          mapLayers.remove(MappingService.AirMapLayerType.Prohibited);
                    }
                });

                ((CheckBox)layerDialog.findViewById(R.id.check_rsua)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) mapLayers.add(MappingService.AirMapLayerType.Restricted);
                        else          mapLayers.remove(MappingService.AirMapLayerType.Restricted);
                    }
                });

                ((CheckBox)layerDialog.findViewById(R.id.check_np)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) mapLayers.add(MappingService.AirMapLayerType.NationalParks);
                        else          mapLayers.remove(MappingService.AirMapLayerType.NationalParks);
                    }
                });

                ((CheckBox)layerDialog.findViewById(R.id.check_noaa)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) mapLayers.add(MappingService.AirMapLayerType.NOAA);
                        else          mapLayers.remove(MappingService.AirMapLayerType.NOAA);
                    }
                });

                ((CheckBox)layerDialog.findViewById(R.id.check_hos)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) mapLayers.add(MappingService.AirMapLayerType.Hospitals);
                        else          mapLayers.remove(MappingService.AirMapLayerType.Hospitals);
                    }
                });

                ((CheckBox)layerDialog.findViewById(R.id.check_sch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) mapLayers.add(MappingService.AirMapLayerType.Schools);
                        else          mapLayers.remove(MappingService.AirMapLayerType.Schools);
                    }
                });

                ((CheckBox)layerDialog.findViewById(R.id.check_heli)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) mapLayers.add(MappingService.AirMapLayerType.Heliports);
                        else          mapLayers.remove(MappingService.AirMapLayerType.Heliports);
                    }
                });

                ((CheckBox)layerDialog.findViewById(R.id.check_pp)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) mapLayers.add(MappingService.AirMapLayerType.PowerPlants);
                        else          mapLayers.remove(MappingService.AirMapLayerType.PowerPlants);
                    }
                });

                ((CheckBox)layerDialog.findViewById(R.id.check_tfrs)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) mapLayers.add(MappingService.AirMapLayerType.TFRS);
                        else          mapLayers.remove(MappingService.AirMapLayerType.TFRS);
                    }
                });

                ((CheckBox)layerDialog.findViewById(R.id.check_wild)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) mapLayers.add(MappingService.AirMapLayerType.Wildfires);
                        else          mapLayers.remove(MappingService.AirMapLayerType.Wildfires);
                    }
                });
            }
        });

        // Add a callback to the "From" textbox to allow the user to lookup partial addresses using Mapbox.
        final GeocoderAutoCompleteView fromAddrView = (GeocoderAutoCompleteView) view.findViewById(R.id.edit_from_addr);
        fromAddrView.setAccessToken(ACCESS_TOKEN);
        fromAddrView.setType(GeocodingCriteria.TYPE_ADDRESS);
        fromAddrView.setOnFeatureListener(fromAddrListener);
        // clear the position when user clears the address
        fromAddrView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fromAddrView.getText().toString().isEmpty());
                    fromPosition = null;
            }
        });

        // Add a callback to the "To." textbox to allow the user to lookup partial addresses using Mapbox.
        final GeocoderAutoCompleteView toAddrView = (GeocoderAutoCompleteView) view.findViewById(R.id.edit_to_addr);
        toAddrView.setAccessToken(ACCESS_TOKEN);
        toAddrView.setType(GeocodingCriteria.TYPE_ADDRESS);
        toAddrView.setOnFeatureListener(toAddrListener);
        // clear the position when user clears the address text
        toAddrView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toAddrView.getText().toString().isEmpty());
                    toPosition = null;
            }
        });


         //   The direction button is the little car inside a circle.
        // Set the callback that handles this button.
        // Clear the map, and call the function that handles the case.

        final FloatingActionButton directionButton  = (FloatingActionButton) view.findViewById(R.id.button_drivingdir);
        directionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
//                Log.d("TAG", fromPosition + ", "+ toPosition);
                directionOnClick(fromAddrView, toAddrView);
            }
        });

        // Set the callbacks for the radiogroup on the bottom left with [street, satellite, airspace]
        // add map layer when user clicks each checkbox
        RadioGroup radioGroup = (RadioGroup) getActivity().findViewById(R.id.radiogroup_maptype);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            /* Default styles
                mapbox://styles/mapbox/streets-v9
                mapbox://styles/mapbox/outdoors-v9
                mapbox://styles/mapbox/light-v9
                mapbox://styles/mapbox/dark-v9
                mapbox://styles/mapbox/satellite-v9
                mapbox://styles/mapbox/satellite-streets-v9
            */
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                UserSettings.MapType checkedIdMapType = convertInttoMapType(checkedId);
                setupMapTypes(checkedIdMapType);
            }
        });

    }

    public UserSettings.MapType convertInttoMapType(int Rid)
    {
        if(Arrays.asList(UserSettings.ResourceLookup).contains(Rid))
        {
            int index = java.util.Arrays.binarySearch(UserSettings.ResourceLookup,Rid);
            UserSettings.MapType indexedType = UserSettings.MapType.getMapType(index);
            return indexedType;
        }
        else
        {
            UserSettings.MapType mapDefault = UserSettings.MapType.AIRSPACE;
            return mapDefault;
        }
    }

    /**
     * Handles the case where the user clicks on the radiogroup on the
     * bottom-left with [street, satellite, airspace]
     * @param checkedId
     */

    //TODO: checkedID should be an enumeration of 3 val
    private void setupMapTypes(UserSettings.MapType checkedId) {
        switch (checkedId) {
            // Street button
            case STREET:
                //this is case "street"
//                        mMap.setStyleUrl("mapbox://styles/mapbox/streets-v9");
                currentTheme = MappingService.AirMapMapTheme.Standard;
                mapType = UserSettings.MapType.STREET;
                mMap.setStyleUrl(AirMap.getTileSourceUrl(mapLayers, currentTheme));
                break;
            case SATELLITE:

//                        mMap.setStyleUrl("mapbox://styles/mapbox/satellite-streets-v9");
                currentTheme = MappingService.AirMapMapTheme.Satellite;
                mapType = UserSettings.MapType.SATELLITE;
                mMap.setStyleUrl(AirMap.getTileSourceUrl(mapLayers,currentTheme));
                break;
            case AIRSPACE:
                // VFR map doesn't need a theme
                currentTheme = null;
                mapType = UserSettings.MapType.AIRSPACE;
                mMap.setStyleUrl("mapbox://styles/pliottrainingsystem/ciwxya19b000n2qpkasrd95od");
//                        mMap.setStyleUrl("mapbox://styles/mapbox/light-v9");
//                        mMap.setMapType(mMap.MAP_TYPE_TERRAIN);
//                        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(new GeotiffTileProvider()));
//                        if(myLocation != null) {
//                            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 8);
//                            mMap.moveCamera(update);
//                        }
                break;
        }
    }
        //TODO: fix edge case in statement to handle none of the above options. Should be impossible
        //but lets cover our bases.
    /**
     *  When the Direction Button is Clicked:
     *  1) clear the map if both addresses are empty
     *  2) if only the "toAddress" is empty, then draw a marker showing the current location
     *  3) if only the "fromAddress" is empty, then draw a route from current location to the specified destination
     *  4) if neither of the addresses is empty, then draw a route between those two locations
     */
    private void directionOnClick(GeocoderAutoCompleteView fromAddrView, GeocoderAutoCompleteView toAddrView) {
        if(fromAddrView.hasFocus()) fromAddrView.clearFocus();
        if(toAddrView.hasFocus()) toAddrView.clearFocus();

        if(fromPosition != null && !fromAddrView.getText().toString().isEmpty() && toPosition == null) {
            // if only the "fromAddress" is filled, then add a marker on this location
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(fromPosition.getLatitude(), fromPosition.getLongitude()))
                    .title("Current Location"));
        } else if (fromPosition == null && toPosition != null) {
            // if only the "toAddress" is filled, then draw a route from current location to the specified destination
            getCurrentPositionThenDrawRoute();
        } else if(fromPosition != null && toPosition != null)
            // if both addrsses are filled, draw a route between two locations
            drawRouteBetweenTwoPositions();
        else
            ToastUtil.makeLongToast(getContext(), "No routes found");
    }

    /**
     * Calls MapBox to draw a path between two points.
     * Draw a Mapbox marker on the destination.
     * A MapBox marker is an inverted red teardrop with a white circle in the middle.
     * The path between is called a route in Mapbox.
     */
    private void drawRouteBetweenTwoPositions() {
        // Add origin and destination to the map
//        mMap.addMarker(new MarkerOptions()
//                .position(new LatLng(fromPosition.getLatitude(), fromPosition.getLongitude()))
//                .title("Origin"));
//                        .snippet("Alhambra"));
        // Draw the marker at the destination.
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(toPosition.getLatitude(), toPosition.getLongitude()))
                .title("Destination"));
//                        .snippet("Plaza del Triunfo"));

        // Get route, and draw it on the screen.
        try {
            getRoute(fromPosition, toPosition);
        } catch (ServicesException servicesException) {
            servicesException.printStackTrace();
        }
    }

    /** Called when the circle-with-four-segements button on the upper-left is pusshed.
     *  Centers the map on the current location of the android device.
     */
    private void setMyLocation() {
        mMap.clear();
        // If we don't already permssion to lookup our location,
        // ask the user for permission.
        if(!locationServices.areLocationPermissionsGranted()) {
            locationMode = MODE_SETLOCATION;
            requestPermissions(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, PERMISSION_ACCESS_CODE);
        } else {
            enableLocation();
        }
    }

    /**
     * Looks up the current location of the android device.
     * Moves the camera so that this location is centered on the map.
     */
    private void enableLocation() {
        Location lastLocation = locationServices.getLastLocation();
        if(mMap != null) {
            if (lastLocation != null && !getLocationOnly)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 11));
            else {
                getLocationOnly = false;
                locationServices.toggleGPS(true);
                // Called when we get a location update.
                // Moves the camera so that the new location is in the center of the map.
                locationServices.addLocationListener(new com.mapbox.mapboxsdk.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.d("TAG", location.getLatitude() + "|" + location.getLongitude());
                        if (location != null) {
                            fromPosition = Position.fromCoordinates(location.getLongitude(), location.getLatitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 11));
                            // Set the blue dot that indicates our current location.
                            mMap.setMyLocationEnabled(true);
                            locationServices.removeLocationListener(this);
                        }
                    }
                });
            }
        }
    }

    /**
     *   If both the "from" address and the "to" Address is filled, then draw a route from current location to the specified destination.
     */
    private void getCurrentPositionThenDrawRoute() {
        if(!locationServices.areLocationPermissionsGranted()) {
            locationMode = MODE_GETLOCATION;
            requestPermissions( new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, PERMISSION_ACCESS_CODE);
        } else {
            getPositionThenDrawRoute();
        }
    }
    /**
     *   If both the "from" address and the "to" Address is filled, then draw a route from current location to the specified destination.
     */
    private void getPositionThenDrawRoute() {
        Location lastLocation = locationServices.getLastLocation();
        if(lastLocation != null) {
            fromPosition = Position.fromCoordinates(lastLocation.getLongitude(), lastLocation.getLatitude());
            drawRouteBetweenTwoPositions();
        } else {
            locationServices.addLocationListener(new com.mapbox.mapboxsdk.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if(location != null) {
                        fromPosition =  Position.fromCoordinates(location.getLongitude(), location.getLatitude());
                        drawRouteBetweenTwoPositions();
                        locationServices.removeLocationListener(this);
                    }
                }
            });
        }
    }

    /**
     * Process the result of a request for location permissions.
     * If permissions are not available, pops up a toast indicating that permission are required.
     *
     * @param requestCode  - defined above.
     * @param permissions  - ignored.
     * @param grantResults - Determines if permissions were granted by Android.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_ACCESS_CODE) {
            // If no permission, pop up a toast.
            if (grantResults.length > 0 && (grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
                ToastUtil.makeLongToast(getContext(), "Location is Required to perform action!");
            } else if (locationMode == MODE_SETLOCATION) {
                // If we have permission, look up the current location of the android device.
                // Moves the camera so that this location is centered on the map.
                enableLocation();
            } else
                getPositionThenDrawRoute();
        }
    }

    /**
     * Called when MapBox does it initialization.
     * Happens once each time the user switches to "Plan".
     *
     * @param mapboxMap
     */

    // You can think of MapboxMap as the controller class for your map and MapView as a more traditional View class.
    // MapboxMap is now home to the methods you’ll use to set and move camera position,
    // add markers, configure user interactions, draw shapes, customize infoWindows, and more.
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        final String RED_TITLE = getContext().getString(R.string.flight_strictly_regulated);
        final String YELLOW_TITLE = getContext().getString(R.string.advisories);
        final String GREEN_TITLE = getContext().getString(R.string.informational);

        mMap = mapboxMap;

        mapLayers = ((Communication)getActivity()).getMapLayers();

        // get the map type the user last selected. Maptype can be [street, satellite, airspace]
        mapType = ((Communication)getActivity()).getMapType();
        //mapType should be an enumeration
        setupMapTypes(mapType);

        // check the radio button of the current map type
        // RadioGroup radioGroup = (RadioGroup) getActivity().findViewById(R.id.radiogroup_maptype);
        UserSettings.MapType[] vals = UserSettings.MapType.values();
        int mapTypeInt = mapType.ordinal();
        int resourceId = UserSettings.ResourceLookup[mapTypeInt];

        //TODO: fix whatever is going wrong here
        ((RadioButton)getActivity().findViewById(resourceId)).setChecked(true);

        //  ensure that every time user opens up the map, it will record the user's current location
        mMap.setMyLocationEnabled(true);
        setMyLocation();

        // remove the previous marker.
        mMap.setOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng point) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(advisoryMarker != null)
                            mMap.removeMarker(advisoryMarker);
                    }
                });
            }
        });

        // Set a callback that will
        // show advisory information when user clicks on the map.
        // Taken from AirMap github repository.
        mMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull final LatLng point) {
                Coordinate coordinate = new Coordinate(point);
                AirMap.checkCoordinate(coordinate, .01, null, null, false, new Date(), new AirMapCallback<AirMapStatus>() {
                    @Override
                    public void onSuccess(final AirMapStatus response) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // remove the previous marker and add a new marker at the new position
                                if(advisoryMarker != null)
                                    mMap.removeMarker(advisoryMarker);
                                advisoryMarker = mMap.addMarker(new MarkerOptions().position(point));
                                // show the advisory info
                                mMap.selectMarker(advisoryMarker);
                            }
                        });

                        /**
                         * The REST call response will give you a list of all the advisories,
                         * then needs to group the list by advisory color creating a mapping between
                         * each color with a list of advisories that all has the specified color.
                         * This Hashmap is then used to populate the recyclerView.
                         */
                        mMap.setInfoWindowAdapter(new MapboxMap.InfoWindowAdapter() {
                            @Nullable
                            @Override
                            public View getInfoWindow(@NonNull Marker marker) {
                                // Parent is the Rectangle that pops up, giving a list of location that have some sort of advisory.
                                View parent = LayoutInflater.from(getContext()).inflate(R.layout.advisorysheet, null);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

                                RecyclerView recyclerView = (RecyclerView)parent.findViewById(R.id.recycler_advisory);
                                recyclerView.setLayoutManager(linearLayoutManager);
                                Map<String ,List<AirMapStatusAdvisory>> data = new HashMap<>();
                                // This determines the colors of the bars that appear to the left of each advisory.
                                List<AirMapStatusAdvisory> statusList = response.getAdvisories();

                                // Get the colors the will go with each advisory.
                                for (AirMapStatusAdvisory airmapStatus: statusList) {
                                    AirMapStatus.StatusColor color = airmapStatus.getColor();
                                    String colorTitle = "";
                                    switch (color) {
                                        case Green:
                                            colorTitle = GREEN_TITLE;
                                            break;
                                        case Yellow:
                                            colorTitle = YELLOW_TITLE;
                                            break;
                                        case Red:
                                            colorTitle = RED_TITLE;
                                            break;
                                    }

                                    List<AirMapStatusAdvisory> mlist = data.get(colorTitle);
                                    if(mlist == null) {
                                        mlist = new ArrayList<>();
                                        data.put(colorTitle, mlist);
                                    }

                                    mlist.add(airmapStatus);
                                }

                                AdvisoriesBottomSheetAdapter adapter = new AdvisoriesBottomSheetAdapter(getContext(), data);
                                recyclerView.setAdapter(adapter);

                                return parent;
                            }
                        });

                    }

                    @Override
                    public void onError(AirMapException e) {
                        Log.d("TAG", "ERROR: "+e.toString());
                    }
                });
            }
        });


//        Log.d("TAG",mGoogleApiHelper.isConnected()+"~~~");

//        myLocation = mGoogleApiHelper.getGoogleLastLocation();
//        if(myLocation != null) {
//
//
//
//            LatLng mLatlng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
//            // create marker
////            MarkerOptions marker = new MarkerOptions().position(
////                    mLatlng).title("Hello Maps");
//
////            // Changing marker icon
////            marker.icon(BitmapDescriptorFactory
////                    .defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
//
//            // adding marker
////            googleMap.addMarker(marker);
//            CameraPosition cameraPosition = new CameraPosition.Builder()
//                    .target(mLatlng)
//                    .zoom(11)
//                    .build();
//            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//        } else {
//            ToastUtil.makeLongToast(getContext(), "Please make sure the location service is turned on!");
//        }
    }

    /**
     * Draw a route on the screen. MapBox does not provide a set up directions like Google Map.
     * @param origin  - The position where the route begins.
     * @param destination  The position where the route ends.
     * @throws ServicesException
     */
    private void getRoute(Position origin, Position destination) throws ServicesException {
        // Build the object that we use to request a route.
        MapboxDirections client = new MapboxDirections.Builder()
                .setOrigin(origin)
                .setDestination(destination)
                .setProfile(DirectionsCriteria.PROFILE_DRIVING)
                .setAccessToken(ACCESS_TOKEN)
                .build();

        //  Make a REST call to the MapBox server.
        // This code lifted from MapBox.
        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response
                Log.d(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().getRoutes().size() < 1) {
                    ToastUtil.makeLongToast(getContext(), "No routes found");
                    return;
                }

                // Print some info about the route
                DirectionsRoute currentRoute = response.body().getRoutes().get(0);
                String distance = String.format("%.1f", currentRoute.getDistance()*0.000621371192);
                Toast.makeText(
                        getContext(),
                        "Route is " + distance + " miles long.",
                        Toast.LENGTH_LONG).show();

                // Draw the route on the map
                drawRoute(currentRoute);

            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Log.e(TAG, "Error: " + throwable.getMessage());
                Toast.makeText(getContext(), "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Draws a route on top of a map.
     * Lifted from MapBox.
     * @param route
     */
    private void drawRoute(DirectionsRoute route) {
        // Convert LineString coordinates into LatLng[]
        LineString lineString = LineString.fromPolyline(route.getGeometry(), Constants.OSRM_PRECISION_V5);
        List<Position> coordinates = lineString.getCoordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).getLatitude(),
                    coordinates.get(i).getLongitude());
        }

        // Draw Points on MapView
        mMap.addPolyline(new PolylineOptions()
                .add(points)
                .color(Color.parseColor("#009688"))
                .width(5));
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        getLocationOnly = true;
        enableLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        ((Communication)getActivity()).setMapType(mapType);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
