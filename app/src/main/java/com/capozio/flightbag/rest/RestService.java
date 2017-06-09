package com.capozio.flightbag.rest;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.capozio.flightbag.data.model.PDFWrapper;
import com.capozio.flightbag.data.model.PostResponse;
import com.capozio.flightbag.util.Configs;
import com.capozio.flightbag.util.StorageUtil;
import com.capozio.flightbag.util.ToastUtil;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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
 * Created by Ying Zhang on 12/2/16.
 */

public class RestService {
    private static final String TAG = "TAG";
    private static RestInterface restService = RestClient.getInstance().create(RestInterface.class);

    /**
     *   make two REST calls,
     *   first to create an document with the meta-data
     *   retrieve the generated id from the cloud and upload the PDF attachment.
     *
     * @param metaData pdf meta-data
     * @param payload the pdf file
     * @param docid document id
     * @param currentDir
     * @param emailAddress
     * @param stat_email if the email has been sent successfully
     * @param progressDialog cancel the progressDialog after upload finishes
     * @param context
     * @param sync whether it's doing syncing or submitting
     * @param success whether previous operations are successfully
     * @return a subscription that can be unsubscribed within "OnStop()"
     */
    public static Subscription RESTuploadPDF(PDFWrapper metaData, final File payload, final String docid,
                                             final File currentDir, final String emailAddress, final boolean stat_email,
                                             final ProgressDialog progressDialog, final Context context, final boolean sync, final boolean success) {
        // prepare the PDF attachment
        final MultipartBody.Part filePart =
                MultipartBody.Part.createFormData("pdf", payload.getName(),
                        RequestBody.create(MediaType.parse("application/pdf"), payload));

        return restService.uploadPDFMetaData(Configs.PERMISSONDBID, docid, metaData)
                .flatMap(new Func1<PostResponse, Observable<ResponseBody>>() {
                    @Override
                    public Observable<ResponseBody> call(PostResponse postResponse) {
                        String rev = postResponse.getRev();
                        return restService.attachPDF(Configs.PERMISSONDBID, docid, payload.getName(), rev, filePart);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                        ToastUtil.makeShortToast(context, success ? "Synced!":"Sync Failed!");
                    }

                    @Override
                    public void onError(Throwable e) {
                        // store status info into a file
                        File statusFile = new File(currentDir.toString(), Configs.STAT_FILENAME);
                        StorageUtil.writeStatus2File(statusFile, emailAddress, stat_email, false);

                        if(progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        if(!sync) {
                            ToastUtil.makeLongToast(context, "Waiver Saved Locally. Please Sync When Internet is Available.");
                        } else {
                            ToastUtil.makeShortToast(context, "Sync Failed!");
                        }
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        Log.d(TAG, "Attach PDF Success!");
                        // store status info into a file
                        File statusFile = new File(currentDir.toString(), Configs.STAT_FILENAME);
  //                        Log.d(TAG, "dir"+currentDir.getAbsolutePath());
                        StorageUtil.writeStatus2File(statusFile, emailAddress, stat_email, true);
                        if(progressDialog != null)
                            progressDialog.dismiss();

                        if(!sync) {
                            ToastUtil.makeLongToast(context, "Submitted!");
                        }
                    }
                });
    }
}
