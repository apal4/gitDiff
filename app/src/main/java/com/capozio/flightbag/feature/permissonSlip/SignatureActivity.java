package com.capozio.flightbag.feature.permissonSlip;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.capozio.flightbag.App;
import com.capozio.flightbag.R;
import com.capozio.flightbag.data.model.PDFWrapper;
import com.capozio.flightbag.rest.RestService;
import com.capozio.flightbag.util.Configs;
import com.capozio.flightbag.util.GoogleApiHelper;
import com.capozio.flightbag.util.SendEmailUtil;
import com.capozio.flightbag.util.StorageUtil;
import com.capozio.flightbag.util.ToastUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.services.android.geocoder.ui.GeocoderAutoCompleteView;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.geocoding.v5.models.CarmenFeature;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.mail.MessagingException;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.capozio.flightbag.util.Configs.PDF_FILENAME;
import static com.capozio.flightbag.util.Configs.TIME_FORMAT_LOCAL;
import static com.capozio.flightbag.util.Configs.TYPE_PERMISSONWAIVER;
import static com.capozio.flightbag.util.KeyBoardUtil.hideKeyBoard;
import com.capozio.flightbag.rest.DataConnector;

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
 * Created by Ying Zhang on 10/6/16.
 *
 */

/**
 * * This activity is called when the user tries to sign the permission slip.
 *
 * It  displays on the screen the permission slip text  and
 * create a signature section for users to type in personal information and signature.
 *
 * If an email address is specified, an email attached with the PDF file containing the
 * signed document will be sent to the specified address, and a copy will be cc'ed to the
 * developer gmail account at the same time.
 *
 * Currently the permission text is hardcoded.  We intend to eventually
 * retrieve this text from the database.
 */
public class SignatureActivity extends AppCompatActivity{
    private final static String TAG = "TAG";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
//    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 10000;
//    private final static int PERMISSION_ACCESS_FINE_LOCATION = 10;
    private final static int PERMISSON_ACCESS_CODE = 11;
    private final static int REQUEST_CHECK_SETTINGS = 25;
    private static final int REQUESTSUBMIT_CODE = 1;
//    private final static int LOCATION_DELAY = 10000;
    private static GoogleApiHelper mGoogleApiHelper;
    // todo: Rename this MAPBOX_ACCESS_TOKEN.
    private final String ACCESS_TOKEN = MapboxAccountManager.getInstance().getAccessToken();
    //    private LocationRequest locationRequest;
    private File externalDir;
    // private File currentDir;
    private File curDir;
    private String docID;
    private EditText emailText;
    private Button submitButton;
    private Subscription email_sub;
    private Location lastLocation;
    private Position addressPosition;
//    private LocationListener mLocationListener;
//    private static boolean stat_metaData = false;
//    private static boolean stat_pdf = false;
//    private static boolean stat_email = false;

    private static ProgressDialog progressDialog;
    //    public static LocationRequest locationRequest = LocationRequest.create()
//            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//            .setInterval(1 * 1000)// 10 seconds, in milliseconds
//            .setFastestInterval(1 * 1000); // 1 second, in milliseconds

    /**
     *
     * @param savedInstanceState - ignored.
     */
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure Google Play services is online.
        // We need this to look up the location of the android device.
        // Todo: We need to understand how the system behaves when GooglePlay in
        // not available, and how to use GPS when there is no internet connection.
        checkPlayServices();

        // Basic screen layout.
        setContentView(R.layout.activity_pdf);

        // Get the path to the directory where we store signed waivers.
        externalDir = new File(Environment.getExternalStorageDirectory(), Configs.WAIVER_PATH);
        if(!externalDir.exists() && !externalDir.mkdir())
            Log.e(TAG, "Directory not created");

        // For communcation with the Google Play Services.
        mGoogleApiHelper = App.getGoogleApiHelper();

        // Make the UUID for the signed waiver.
        docID = UUID.randomUUID().toString();

        // The little spinner telling the user to wait on the application.
        progressDialog = new ProgressDialog(this, R.style.SignatureProgressDialog);

        // Screen where user signs permissions slip.
        TextView  textview= (TextView) findViewById(R.id.textview_permissonSlip);
        // The boilerplate text that user should agree to.  Currently hardwired.
        textview.setText(Html.fromHtml(Configs.permissonText));
        // Hide the keyboard when the user clicks on the area boilerplate text.
        textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBoard(v);
            }
        });

        // "Clear" button. Click this button to erase the signature.
        Button clearButton = (Button) findViewById(R.id.button_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignatureView signatureView = (SignatureView) findViewById(R.id.view_signature);
                signatureView.clearSignature();
            }
        });

        // Set the date field with the current date.
        TextView dateView = (TextView) findViewById(R.id.sign_date);
        Calendar calendar = Calendar.getInstance();
        dateView.setText((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.YEAR));

        // The homeowner's email.  If set, an email will be sent to the homeowner with signed waiver.
        emailText = (EditText) findViewById(R.id.edit_email);

        // handle the case where the user clicks "done" on the soft keyboard.
        // "Done" is a checkmark inside a circle.
        emailText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyBoard(v);
                    emailText.clearFocus();
                    return true;
                }
                return false;
            }
        });
        // MapBox library allowing us to autocomplete addresses.
        GeocoderAutoCompleteView addressEdit = (GeocoderAutoCompleteView)findViewById(R.id.edit_street_address);
        // Set the MapBox Access Token.
        addressEdit.setAccessToken(ACCESS_TOKEN);
        // Tell MapBox that we are searching for an address.
        addressEdit.setType(GeocodingCriteria.TYPE_ADDRESS);
        // Captures a  position of the selected address.
        addressEdit.setOnFeatureListener(new GeocoderAutoCompleteView.OnFeatureListener() {
            @Override
            public void OnFeatureClick(CarmenFeature feature) {
                addressPosition = feature.asPosition();
            }
        });


        /* ------- SUBMIT BUTTON ------- */
        // first generate a PDF and store it locally,
        // then upload it to the cloud,
        // then if there is an email address,
        // send an email with the attachment.

        submitButton = (Button) findViewById(R.id.button_submit_permissonslip);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Submitting...");
                progressDialog.show();

                // Android versions after 23 have stricter permission.
                // Make a call to check for these permissions.
                if(Build.VERSION.SDK_INT >= 23)
                    checkPermissons();

                // Make the button semi-transparent since we can click it.
                submitButton.setAlpha(0.5f);
                // Disable the submit button.
                submitButton.setEnabled(false);
                // If we haven't been able to get the homeowners address from
                // the MapBox, then try to get the position of using the Android device location.
                if(addressPosition == null)
                    // Get the location from location service. This will also submit the PDF.
                    enableLocationService();
                else
                    // Just submit the PDF.
                    submitPDF();

                // Currently does nothing.
                // The idea is that we make sure the user can submit a checklist
                // only after signing a waiver.
                Intent intent = new Intent();
                intent.putExtra("Signed", true);
                setResult(REQUESTSUBMIT_CODE, intent);
                hideKeyBoard(v);
            }
        });


        /* ------- CANCEL BUTTON ------- */
        ImageButton cancelButton = (ImageButton) findViewById(R.id.button_cancel_sign);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("Signed", false);
                setResult(REQUESTSUBMIT_CODE, intent);
                hideKeyBoard(view);
                finish();
            }
        });
    }

    /**
     *  Shuts down the location service, either because we found our current location,
     *  or we have given up trying.
     *  Also submits the signed PDF.
     */
    public void doStuffWithLocation() {
//        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, mLocationListener);
        mGoogleApiHelper.removeLocationUpdate();
        submitPDF();
    }


    /******* Activity Lifecycle Related *********/
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Start!");
        // Connect with the Google API.  We use this only to find the current device location.
        mGoogleApiHelper.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiHelper.disconnect();
        Log.d(TAG, "Stop");
    }

    /**
     *  Purpose uncertain.  May not be called at all.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    // Todo: Look at more closely.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made.
                        submitButton.setAlpha(1.0f);
                        submitButton.setEnabled(true);
                        ToastUtil.makeShortToast(getApplicationContext(), "Location is enabled.");
                        if(progressDialog != null)
                            progressDialog.dismiss();

                        break;
                    case Activity.RESULT_CANCELED:
                        submitButton.setAlpha(1.0f);
                        submitButton.setEnabled(true);
                        ToastUtil.makeLongToast(getApplicationContext(), "Location is required for submission!");
                        if(progressDialog != null)
                            progressDialog.dismiss();

                        break;
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(upload_sub != null && !upload_sub.isUnsubscribed())
//            upload_sub.unsubscribe();
//        if(attach_sub != null && !attach_sub.isUnsubscribed())
//            attach_sub.unsubscribe();
        // Shut down the email service to prevent a memory leak.
        if(email_sub != null && !email_sub.isUnsubscribed())
            email_sub.unsubscribe();
//        if(syncFromCloud_sub != null && !syncFromCloud_sub.isUnsubscribed())
//            syncFromCloud_sub.unsubscribe();
    }

    /**
     * Checks for all permission required to create a signed waiver.
     * Calls Android to check for permissions.
     */
    private void checkPermissons() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSON_ACCESS_CODE);
        }
    }

    /**
     * Callback called when Android tries to get permission for writing to local files
     * or to user the location mechanism.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSON_ACCESS_CODE) {
            if(grantResults.length > 0 && (grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
                ToastUtil.makeLongToast(this, "Storage access is required for submisson!");
                checkPermissons();
            } else if(grantResults.length >  0 && (grantResults[1] != PackageManager.PERMISSION_GRANTED)) {
                ToastUtil.makeLongToast(this, "Location is required for submisson!");
                checkPermissons();
            }
        }
    }

    /**
     * Saves the metadata for a signed permission to a local file.
     * @return
     */
    private Map<String,Object> saveMetaData() {
        // get local time and UTC time
        Map<String,Object> metamap=new HashMap<String,Object>();
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT_LOCAL);
        Date curDate = new Date();
        String localDate = dateFormat.format(curDate);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcDate = dateFormat.format(curDate) + "Z";
        metamap.put("LocalTimestamp",localDate);
        metamap.put("UTCTimestamp",utcDate);
        metamap.put("type","HOMEOWNER");
        String streetAddr = ((GeocoderAutoCompleteView) findViewById(R.id.edit_street_address)).getText().toString();
        metamap.put("StreetAddress",streetAddr);
        final String resident = ((EditText) findViewById(R.id.edit_name)).getText().toString();
        metamap.put("Resident",resident);

        double latitude = 0.0;
        double longitude = 0.0;

        if(addressPosition != null) {
            latitude = addressPosition.getLatitude();
            longitude = addressPosition.getLongitude();
        } else if(lastLocation != null) {
            latitude = lastLocation.getLatitude();
            longitude = lastLocation.getLongitude();
        }
        metamap.put("Latitude",latitude);
        metamap.put("Longitude",longitude);


        //LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        PDFWrapper metaData = new PDFWrapper(docID, TYPE_PERMISSONWAIVER, streetAddr,
                "", "", resident, longitude+"", latitude+"", localDate, utcDate);

        File metaFile = new File(curDir.toString(), Configs.META_FILENAME);

        StorageUtil.writeJson2File(metaFile, new Gson().toJson(metamap));

        return metamap;
    }


    /**
     * Returns a file with a PDF file with a signed waiver.
     * @return
     */
    private File generatePDF() {
        PrintAttributes printAttrs = new PrintAttributes.Builder().
                setColorMode(PrintAttributes.COLOR_MODE_COLOR).
                setMediaSize(PrintAttributes.MediaSize.ISO_A4). // specifies that the width to be 640px
//                setMediaSize(PrintAttributes.MediaSize.NA_LETTER).
                //setResolution(new PrintAttributes.Resolution("zooey", PRINT_SERVICE, 595, 842)).
                        setMinMargins(new PrintAttributes.Margins(10, 10, 0, 10)).
                        build();
        // create a new document
        PdfDocument document = new PrintedPdfDocument(this, printAttrs);

        // figure out the actual height of the PDF
        TextView content = (TextView) findViewById(R.id.textview_permissonSlip);
        //content.setScaleX(1f);
        //content.setScaleY(1f);
        int textHeight = content.getHeight();

        // The bottom of the screen, with the Name, Street address, Date,  signature, and email.
        TableLayout tableLayout = (TableLayout) findViewById(R.id.tablelayout_sign);
        int signAreaHeight = tableLayout.getHeight();

        // Make the "Clear" button invisible. Now user cannot clear the signature.
        findViewById(R.id.button_clear).setVisibility(View.INVISIBLE);

        // create a page description
//        PdfDocument.PageInfo pageInfo  new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        // set PDF page dimension.
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(content.getWidth() + 40, textHeight + signAreaHeight + 40, 1).create();

        // start a page
        PdfDocument.Page page = document.startPage(pageInfo);

        // draw something on the page

        //content.setScaleX(0.02f);
        //content.setScaleY(0.0f);

        //Log.d(TAG, content.getHeight() + "," + content.getWidth() + "|" + page.getCanvas().getWidth() + "," + signAreaHeight + "->" + PrintAttributes.MediaSize.ISO_A4.getWidthMils());

        Canvas canvas = page.getCanvas();
        // create some left margin
        canvas.translate(30, 0);

        // draw the bitmap with boilerplate text of the permission slip
        content.draw(canvas);

        // move down the canvas to draw the signature section at the bottom.
        canvas.translate(0, textHeight);
        tableLayout.draw(canvas);

        // finish the page
        document.finishPage(page);

        // add more pages

        // write the document content
        curDir = new File(externalDir, docID);
        if(!curDir.exists())
            if(!curDir.mkdir())
                Log.e(TAG, "Directory not created!");

        File newFile = new File(curDir.toString(), PDF_FILENAME);
//        File newFile = new File(externalDir, "SignedWaiver.pdf");
//        if (newFile.exists())
//            newFile.delete();
        try {
            FileOutputStream outputStream = new FileOutputStream(newFile);
            document.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // close the document
        document.close();
        return newFile;
    }

    /**
     * Save a local copy of the signed waiver.
     * Uploads to the cloudant database if connectivity exists.
     */
    private void submitPDF() {
        // Generate the PDF waiver using the content of
        // two Android views.
        final File pdfFile = generatePDF();
        if(pdfFile==null){
            Toast.makeText(this, "pdf not generate!", Toast.LENGTH_LONG).show();
        }
        //Christine using new dataconnector to upload pdf
       final Map<String,Object> map=saveMetaData();





        // Get the associated meta data.
     //   final PDFWrapper metaData = saveMetaData();

        // The email address of the homeowner.
        final String emailAddress = emailText.getText().toString().trim();

        // Sends an email to the homeowner.
        //  Always sends a bcc, so this should always suceed.

        email_sub = Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws MessagingException {
                return SendEmailUtil.sendEmail(emailAddress, pdfFile);
            }
        }).onErrorReturn(new Func1<Throwable, Boolean>() {
            @Override
            public Boolean call(Throwable throwable) {
                // If we fail to send the email, log a message.
                Log.d(TAG, throwable.getMessage());
                return false;
            }
        }).subscribeOn(Schedulers.io())   // Don't block the UI thread.
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
//                        Intent intent = new Intent();
//                        intent.putExtra("Signed", true);
//                        setResult(REQUEST_CODE, intent);
                        if(progressDialog != null)
                            progressDialog.dismiss();
                        finish();
                    }

                    // If anything fails, dismiss the spinner.
                    @Override
                    public void onError(Throwable e) {
                        if (progressDialog != null)
                            progressDialog.dismiss();
                    }

                    // Upload the PDF to Cloudant.
                    @Override
                    public void onNext( Boolean aBoolean) {

                        Log.d(TAG, "onNext!" + aBoolean);
                        //upload to cloud
                        DataConnector dataConnector = DataConnector.getInstance(getApplicationContext());
                        Boolean pdfupload=false;
                        try {
                            dataConnector.writeMapwithPDF(map, getApplicationContext(), "Signed Wavier", pdfFile);
                            dataConnector.syncWithThread();
                            pdfupload=true;
                        }catch(RuntimeException e){

                        }
                        File statusFile = new File(curDir.toString(), Configs.STAT_FILENAME);
                        //                        Log.d(TAG, "dir"+currentDir.getAbsolutePath());
                        StorageUtil.writeStatus2File(statusFile, emailAddress,  aBoolean, pdfupload);
                        Toast.makeText(getApplicationContext(), "pdf Submitted!", Toast.LENGTH_LONG).show();
                   //     RestService.RESTuploadPDF(metaData, pdfFile,
                         //       docID, curDir, emailAddress, aBoolean, progressDialog,
                         //       getApplicationContext(), false, aBoolean);
                    }
                });

    }

    /******** Location Service Methods ***********/
    /**
     * Method to verify google play services on the device.
     * If we don't have Play Services, pop up toast.
     *
     * */
    private void checkPlayServices() {
        // Get an object that describes availability.
        GoogleApiAvailability mAvailability = GoogleApiAvailability.getInstance();
        // Check to see we have services.
        int resultCode = mAvailability.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS) {
            // If we don't have services, try to get the user to resolve the issue.
            if(mAvailability.isUserResolvableError(resultCode)) {
                mAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                // If the user cannot resolve the issue, pop up a toast.
                ToastUtil.makeLongToast(this, getString(R.string.err_unrecoverable));
                return;
            }
        }
    }

    /**
     *  Turns on the location services on the Android device.
     */
    public void enableLocationService() {
        final AppCompatActivity activity = this;

        final PendingResult<LocationSettingsResult> result =
                mGoogleApiHelper.getLocationSettingResult();

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
//                        lastLocation = null;
                            mGoogleApiHelper.requestLocationUpdate();
//                        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, mLocationListener);
//                        progressDialog.setMessage("Submitting...");
//                        progressDialog.show();
                        //checkStoragePermisson();
                        // Set a timer. The first arg is the amount of time, in milliseconds,
                        // that we wait for a location update.
                        // The second argument is the polling interval for our requests.
                        new CountDownTimer(30000, 2000){
                            // If the time is not interupted, call onFinish().
                            @Override
                            public void onFinish() {
                                Log.d(TAG,"Get Location Timeout!");
                                // Close the location service.
                                doStuffWithLocation();
                            }
                            // With each poll, ask the Google API for the most recent location
                            // of the Android device.
                            @Override
                            public void onTick(long l) {
                                // Note the location that was returned.
                                lastLocation = mGoogleApiHelper.getLastLocation();
                                // If we get a location ...
                                if(lastLocation != null) {
                                    // Close the location service,
                                    doStuffWithLocation();
                                    // Shut off the timer.
                                    cancel();
                                }
                            }
                        }.start();
//                        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, (LocationListener) activity);
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied.
                        try{
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {}
                        break;
                }
            }
        });
    }


}