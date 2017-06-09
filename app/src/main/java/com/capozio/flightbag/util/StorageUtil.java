package com.capozio.flightbag.util;

import android.os.Environment;
import android.util.JsonWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import okhttp3.ResponseBody;

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
 * Created by Ying Zhang on 10/24/16.
 */

/**
 * For reading and writing files on the adroid device.
 * Search for internal vs. external storage.
 */

public class StorageUtil {
    /** Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /** Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Write a json string to a file
     * @param file file to be created
     * @param jsonStr the json string to be written
     */
    public static void writeJson2File(File file, String jsonStr) {
        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(jsonStr);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write status flags and email address to a local file
     * @param file the local file to be created
     * @param emailAddress  user specified email address
     * @param email whether the email has been sent
     * @param pdf whether the signed waiver has been uploaded
     */
    public static void writeStatus2File(File file, String emailAddress, boolean email, boolean pdf){
        try {
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(outputStream));
            jsonWriter.beginObject();
            jsonWriter.name(Configs.STATUS_EMAIL).value(email);
            jsonWriter.name(Configs.EMAIL_ADDR).value(emailAddress);
            jsonWriter.name(Configs.STATUS_PDF).value(pdf);
            jsonWriter.endObject();
            jsonWriter.close();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Write the HTTP response into a file
     * @param reponse response from HTTP call
     * @param path the path of the file to be created
     */
    public static void writeResponseBody2File(ResponseBody reponse, String path) {
        File pdfFile = new File(path);
        if(pdfFile.exists())
            return;
        OutputStream outputStream = null;
        InputStream inputStream = reponse.byteStream();
        byte[] fileReader = new byte[4096];

        int read;
        try {

            outputStream = new FileOutputStream(pdfFile);
            while((read = inputStream.read(fileReader)) != -1) {
                outputStream.write(fileReader, 0, read);
            }
            outputStream.flush();
        } catch (IOException e) {
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if(outputStream != null)
                    outputStream.close();
            } catch (IOException e) {
            }
        }

    }

    /**
     * Convert a file into a string
     * @param file input file
     * @return the string representation of the file
     */
    public static String readFile(File file) {
        StringBuilder sb = new StringBuilder();
        try {
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
        } catch (FileNotFoundException e) {}
        catch (IOException e) {}
        return sb.toString();
    }
}
