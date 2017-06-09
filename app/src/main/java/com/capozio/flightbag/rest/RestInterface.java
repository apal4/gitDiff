package com.capozio.flightbag.rest;

import com.capozio.flightbag.data.model.ChecklistWrapper;
import com.capozio.flightbag.data.model.FlightStart;
import com.capozio.flightbag.data.model.PDFWrapper;
import com.capozio.flightbag.data.model.PermissonSlipMetaDataResponse;
import com.capozio.flightbag.data.model.PermissonSlipResponse;
import com.capozio.flightbag.data.model.PilotDataResponse;
import com.capozio.flightbag.data.model.PostResponse;
import com.capozio.flightbag.data.model.TemplateResponse;
import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

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
 * Created by Ying Zhang on 9/9/16.
 *
 *  REST API service for exchanging data between app and Cloud server
 */
public interface RestInterface {
    /*
    @FormUrlEncoded
    @POST("_session")
    Call<ResponseBody> cloudantLogin(
            @Field("username") String username,
            @Field("password") String passward
    );
*/

    /**
     * POST vs PUT
     *
     * The POST operation can be used to create a new document with a server generated DocID.
     * To do so, the URL must point to the database's location. To create a named document,
     * use the PUT method instead.
     */
    @POST("{dbid}")
    Call<ResponseBody> uploadChecklist(
            @Path("dbid") String id,
            @Body ChecklistWrapper checklist
    );

    @GET("{dbid}/_design/Views/_view/ChecklistTemplate")
    Call<TemplateResponse> getCheckListTemplate(
            @Path("dbid") String id,
            @Query("TemplateName") String templateid
    );

    @GET("{dbid}/b78f34dd31f397d7a626805c8c3ccebf/DemoWaiver.pdf")
    Call<ResponseBody> getPermissonPDF(
            @Path("dbid") String id
    );

    @GET("{dbid}/b78f34dd31f397d7a626805c8c3ccebf")
    Call<PermissonSlipResponse> getPermissionText(
            @Path("dbid") String id
    );

    @POST("{dbid}")
    Call<PostResponse> uploadSignedPDFDoc(
            @Path("dbid") String id,
            @Body PDFWrapper metadata
    );
//
//    @Headers("Content-type: application/pdf")
//    @PUT("{dbid}/{docid}/{pdfname}")
//    Call<ResponseBody> attachPDF(
//            @Path("dbid") String id,
//            @Path("docid") String docID,
//            @Path("pdfname") String pdfNAME,
//            @Query("rev") String rev,
//            @Body byte[] payload
//    );

    @Multipart
    @Headers("Content-type: application/pdf")
    @PUT("{dbid}/{docid}/{pdfname}")
    Observable<ResponseBody> attachPDF(
            @Path("dbid") String id,
            @Path("docid") String docID,
            @Path("pdfname") String pdfNAME,
            @Query("rev") String rev,
            @Part MultipartBody.Part filePart
    );

    @PUT("{dbid}/{docid}")
    Observable<PostResponse> uploadPDFMetaData(
            @Path("dbid") String id,
            @Path("docid") String docID,
            @Body PDFWrapper metadata
    );


    @GET("{dbid}/_design/Views/_view/HomeOwner")
    Observable<PermissonSlipMetaDataResponse> getPermissonSlipMetaData(
            @Path("dbid") String id
    );

    @GET("{dbid}/{docid}/signedWaiver.pdf")
    Observable<ResponseBody> getAttachment(
            @Path("dbid") String id,
            @Path("docid") String docID
    );

//    @GET
//    Observable<GeoResponse> getCoordinates (
//            @Url String url
//    );

    @POST("{dbid}/_find")
    Observable<PilotDataResponse> getPilotInfo (
            @Path("dbid") String id,
            @Body JsonObject emailAddress
    );

    @POST("{dbid}")
    Observable<ResponseBody> uploadPilotData(
            @Path("dbid") String dbid,
            @Body FlightStart flightStart
    );

}
