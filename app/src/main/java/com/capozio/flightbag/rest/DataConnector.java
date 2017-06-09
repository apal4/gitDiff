package com.capozio.flightbag.rest;

/**
 * Created by jkoch on 1/11/17.
 * <p>
 * ***********************************************************************
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
 */

import android.content.Context;
import android.util.Log;

import com.capozio.flightbag.App;
import com.cloudant.sync.documentstore.AttachmentException;
import com.cloudant.sync.documentstore.Attachment;
import com.cloudant.sync.documentstore.ConflictException;
import com.cloudant.sync.documentstore.DocumentBodyFactory;
import com.cloudant.sync.documentstore.DocumentRevision;
import com.cloudant.sync.documentstore.DocumentStore;
import com.cloudant.sync.documentstore.DocumentStoreException;
import com.cloudant.sync.documentstore.DocumentStoreNotOpenedException;
import com.cloudant.sync.documentstore.UnsavedFileAttachment;
import com.cloudant.sync.replication.Replicator;
import com.cloudant.sync.replication.ReplicatorBuilder;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import android.app.Application;
import android.content.Context;

import com.capozio.flightbag.util.GoogleApiHelper;

/*
import com.cloudant.sync.datastore.Datastore;
import com.cloudant.sync.datastore.DatastoreException;
import com.cloudant.sync.datastore.DatastoreManager;
import com.cloudant.sync.datastore.DocumentBodyFactory;
import com.cloudant.sync.datastore.DocumentException;
import com.cloudant.sync.datastore.DocumentRevision;
import com.cloudant.sync.documentstore.DocumentStore;
*/


/** Singleton class that manages reading and writing to the local datastore,
     and syncing that datastore with the cloud database. */

// Todo: This is supposed to read and write.  It doesn't read,
// and the name doesn't reflect reading even if it did.

public class DataConnector {

    // todo:  Move to Config Class.
    private static String mDataStoreName = "my_datastore3";
    private static String mDatabaseName = "test8";
    // Todo: Don't put passwords in source code.
    private static String mUriString = "https://jkoch:Kochie666@jkoch.cloudant.com/test8";

    private static DocumentStore mDocumentStore = null;

    
    // Only instance.
    private  static DataConnector mDataWriter = new DataConnector();

    // Prevent any other class from instantiating a DataConnector.
    private DataConnector() {
    }

    /**
     * Returns the singleton DataConnector.
     */
    // Todo: Never used. Zero value?
    public static DataConnector getInstance(Context context) {
        // Make the connection to the datastore, if needed.
        init(context);
        return mDataWriter;
    }

    // Todo: Test this.  This was revised to use DocumentStore, but never tested.
    private static void init(Context context) {

        if (mDocumentStore == null) {
            try {
             //  Context context1 = this.getContext();

                // Create a DatastoreManager using application internal storage path
                // It seems likely that first arg must be "datastores", but this isn't clear.
                final File path = context.getDir("datastores", Context.MODE_PRIVATE);
                mDocumentStore = DocumentStore.getInstance(path);

                //   mDatastoreManager = new DatastoreManager(path.getAbsolutePath());
                // Look in /data/data/pts.test15/app_datastores
                // to actually see the corresponding file.
                // After doing
                // $ adb shell
                // $ run-as pts.test14
                // mDatastore = mDatastoreManager.openDatastore(mDataStoreName);
            } catch (DocumentStoreNotOpenedException datastoreException) {
                // Todo: Log Error.
            }
        }
    }

    /**
     * Writes the object in Map to the local datastore.
     */
    // Todo: Test this.  This was revised to use DocumentStore, but never tested.
    // Todo: Do we still need a thread?  Maybe this is now taken care of for us.
    //  see https://github.com/cloudant/sync-android
    public static synchronized void writeMap(final Map<String, Object> map,Context context) {

        init(context);  // make sure the class is initialized.
        // Do the write in a new thread, to ensure that we don't block the main thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Make a new document revision, and write it to our local database.
                    DocumentRevision revision = new DocumentRevision();
                    revision.setBody(DocumentBodyFactory.create(map));
                    // rework this code so that everything goes in.
                    // DocumentRevision saved = mDatastore.createDocumentFromRevision(revision);
                    DocumentRevision saved = mDocumentStore.database().create(revision);


                } catch (DocumentStoreException documentException) {
                    documentException.printStackTrace();
                } catch (AttachmentException e) {
                    e.printStackTrace();
                } catch (ConflictException e) {
                    e.printStackTrace();
                }
            }
            public void onFailure() {
                throw new RuntimeException();
            }
        }, "WriteMapToDatastore").start();


    }

    public static synchronized void writeMapwithPDF(final Map<String,Object> map,Context context, final String fileName,
                                                    final File payload) {
        init(context);

        new Thread(new Runnable() {
            @Override
            public void run() {
                    DocumentRevision revision = new DocumentRevision();
                    revision.setBody(DocumentBodyFactory.create(map));


                    Attachment pdf=new UnsavedFileAttachment(payload,"application/pdf");
                    Map<String,Attachment> pdfmap=new HashMap<String,Attachment>();
                    pdfmap.put(fileName,pdf);
                    revision.setAttachments(pdfmap);

                try {
                    DocumentRevision saved = mDocumentStore.database().create(revision);
                } catch (AttachmentException e) {
                    e.printStackTrace();
                } catch (ConflictException e) {
                    e.printStackTrace();
                } catch (DocumentStoreException e) {
                    e.printStackTrace();
                }


            }
            public void onFailure() {
                throw new RuntimeException();
            }




    },"WriteMapwithPDFToDatastore").start();


    }
    /*
    synchronized void writeMap(final Map<String, Object> map) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Make a new document revision, and write it to our local database.
                    DocumentRevision revision = new DocumentRevision();
                    revision.setBody(DocumentBodyFactory.create(map));
                    // rework this code so that everything goes in.
                    DocumentRevision saved = mDatastore.createDocumentFromRevision(revision);
                } catch (DocumentException documentException) {
                }
            }
        }, "WriteMapToDatastore").start();
    }
    */

    /**
     * Syncronizes the local datastore with the cloud database.
     * This version creates a new thread, which may be unnecessary.
     */
  public  static synchronized void syncWithThread() {

        // Run in separate thread to avoid blocking the main thead.
        // Todo: Test this.  This was revised to use DocumentStore, but never tested.
        // Todo: do we still need a thread, or has this been done for us.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Username/password are supplied in the URL and can be Cloudant API keys
                    URI uri = new URI(mUriString);

                    // Create a replicator that replicates changes from the local
                    // datastore to the remote database.
                    Replicator replicator = ReplicatorBuilder
                            .push()
                            .to(uri)
                            .from(mDocumentStore)
                             .build();
                    // And
                    replicator.start();

                } catch (URISyntaxException ex) {
                    Log.i("sync() error ", ex.toString());
                }

            }
            public void onFailure() {
                throw new RuntimeException();
            }
        }, "ReplicateToCloud").start();
    }

    /**
     * Syncronizes the local datastore with the cloud database.
     * This version does not create a new thread for execution.
     */
    public static synchronized void sync(Context context)    {
        init(context);
        try {
            // Username/password are supplied in the URL and can be Cloudant API keys
            URI uri = new URI(mUriString);

            // Create a replicator that replicates changes from the local
            // datastore to the remote database.
            Replicator replicator = ReplicatorBuilder.push().to(uri).from(mDocumentStore).build();
            // And
            replicator.start();

        } catch (URISyntaxException ex) {
            Log.i("sync() error ", ex.toString());

        }
    }
}