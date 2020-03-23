package com.example.cameragalleryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.jar.Attributes;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.widget.Toast;

//import com.yai.app.support.DialogHandler;


import static com.example.cameragalleryapp.MainActivity.SHARE_PIC_REQUEST;
import static com.example.cameragalleryapp.MainActivity.currentlyDisplayedImageIndex;
import static com.example.cameragalleryapp.MainActivity.fileShortNameList;


// Uploads photos to social
public class UploadPhotoActivity extends AppCompatActivity {
    private static final String TAG = "UploadPhotoActivity";

    //UploadToServerTask task = new UploadToServerTask();
    //task.execute(new String[] { "https://www.bcit.ca" }); //for http lab
    //task.execute(new String[] { "http://10.0.2.2:8080/midp/hits" }); //for webapp lab using emulator
    // task.execute(new String[] { "http://142.232.61.32:8080/PhotoGallery/hits" }); //for webapp lab using phone
    private static final String serverUploadAddress = "http://10.0.2.2:8081/servletFileUploader/androidUpload";
//^^server address

    static int tempIndex = 0;
    static int currentlyDisplayedImageIndex = 0; // Used for displaying the image from the list
    static String myStoragePath; // this will remain the same through the program
    static File storageDir; // Working directory path

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);



    }

    // todo need to include the caption that comes with the image
    // Share image via intent to wake up social media app installed on device
    public void socialMediaClick(View v) {
        Log.d("MainActivity", "socialMediaClick: called");
        final Button socialMediaButton = findViewById(R.id.socialMediaButton); //to enable/disable button

        // Get the total number of images form the list. This is different from imageCount.
        int numberOfImages = fileShortNameList.size();

        // When there are images in the list
        if (numberOfImages > 0){
            // Check if app is installed
            String packageName = "com.facebook.katana"; //facebook app package name (not messenger)
            final boolean packageInstalled = isPackageInstalled(packageName, this);
            Log.i("MainActivity", "socialMediaClick: Package installed = " + packageInstalled);

            // Obtain the previous intent used - this case was the intent to open this activity
            Intent intent = getIntent();

            // Access parameters from previous activity through intent
            currentlyDisplayedImageIndex = intent.getIntExtra("index", tempIndex);
            myStoragePath = intent.getStringExtra("location");

            // Put string into proper format for the datatype
            String imageFileName = fileShortNameList.get(currentlyDisplayedImageIndex) + ".jpg";
            String mPath = myStoragePath + "/" + imageFileName;

            // Create an intent type that is used to send to social media platforms or any other app
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg"); //default image type
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse(mPath)); //parse the string to include only file path
            share.putExtra(Intent.EXTRA_TEXT, "I sent you an image, here is the text."); //on Whatsapp, this is seen under the image
            share.putExtra(Intent.EXTRA_TITLE,"Sent you a title" );
            share.putExtra(Intent.EXTRA_SUBJECT,"Sent you a subject" );
//        share.setPackage(packageName); //comment this out if you want to share via any app

            // Change what the upload button reads and notify the user the upload processing has begun
            socialMediaButton.setEnabled(false);
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
            socialMediaButton.postDelayed(new Runnable() {
                @Override
                public void run() {
                    socialMediaButton.setEnabled(true);
                }
            }, 1000);

            startActivityForResult(Intent.createChooser(share, "Share The Image Via"),SHARE_PIC_REQUEST);
        }

        // When there no images in the list, display default
        else if (numberOfImages == 0){
            socialMediaButton.setEnabled(false);
        }



    }


    //todo this is where uploading to the cloud will happen, tristan only change this method
    // Share image to the server
    public void serverClick(View v) {
        Log.d("MainActivity", "serverClick: called");

        // Get the total number of images form the list
        int numberOfImages = fileShortNameList.size();

        // Put string into proper format for the datatype
        String imageFileName = fileShortNameList.get(currentlyDisplayedImageIndex) + ".jpg"; //image
        String dataFileName = fileShortNameList.get(currentlyDisplayedImageIndex) + ".dat"; //image data - location, timestamp, keyword

        // Get storage path
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        myStoragePath = storageDir.getAbsolutePath();
        String mPath = myStoragePath + "/" + imageFileName;

        // Allow background to obtain context and store information
        BackgroundWorker backgroundWorker = new BackgroundWorker(this); // declare, instantiate, initialize
        backgroundWorker.execute(String.valueOf(numberOfImages), imageFileName, dataFileName, mPath);

//        // WRITE CODE HERE
//
//
//
//
//
//        // When there are images in the list
//        if (numberOfImages > 0){
//            Log.i("MainActivity", "serverClick: items to be written to server");
//            int serverResponseCode = 0;
//
//            // public int uploadFile(final String sourceFileUri, final String upLoadServerUri, final String renameFile) {
//            //
//           int response = uploadFile(mPath, serverUploadAddress , imageFileName);
//            Log.i(TAG, "serverClick: " + response);
//        }
//        else{
//
//            //we have no pictures
//            Log.i("MainActivity", "serverClick: we have no items to be written to server");
//           //call asynch task to upload images
//
//
//        }
//
//
//
//
//        ///
//
//
//        // Change what the upload button reads and notify the user the upload processing has begun
//        serverButton.setEnabled(false);
//        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
//        serverButton.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                serverButton.setEnabled(true);
//            }
//        }, 1000);
    }



    // Check to see if Social Media Application is installed
    private boolean isPackageInstalled(String packageName, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


//    public int uploadFile(final String sourceFileUri, final String upLoadServerUri, final String renameFile) {
//        int serverResponseCode = 0;
//        HttpURLConnection conn = null;
//        DataOutputStream dos = null;
//        String lineEnd = "\r\n";
//        String twoHyphens = "--";
//        String boundary = "*****";
//        int bytesRead, bytesAvailable, bufferSize;
//        byte[] buffer;
//        int maxBufferSize = 1 * 1024 * 1024;
//        File sourceFile = new File(sourceFileUri);
//
//        if (!sourceFile.isFile()) {
//            //dialog.dismiss();
//
//            Log.e("uploadFile", "Source File not exist : " + sourceFileUri);
//
//           // activity.runOnUiThread(new Runnable() {
////                public void run() {
////                    new DialogHandler().customDialog(activity, "ERROR", "Source File not exist : " + sourceFileUri);
////                }
//           // });
//
//            return 0;
//
//        }
//        else{
//
//            try {
//                // open a URL connection to the Servlet
//                FileInputStream fileInputStream = new FileInputStream(sourceFile);
//                URL url = new URL(upLoadServerUri);
//
//                // Open a HTTP  connection to  the URL
//                conn = (HttpURLConnection) url.openConnection();
//                conn.setDoInput(true); // Allow Inputs
//                conn.setDoOutput(true); // Allow Outputs
//                conn.setUseCaches(false); // Don't use a Cached Copy
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Connection", "Keep-Alive");
//                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
//                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
//
//                conn.setRequestProperty("uploaded_file", renameFile);
//
//                dos = new DataOutputStream(conn.getOutputStream());
//
//                // add parameters
//                dos.writeBytes(twoHyphens + boundary + lineEnd);
//                dos.writeBytes("Content-Disposition: form-data; name=\"type\""
//                        + lineEnd);
//                dos.writeBytes(lineEnd);
//
//                // assign value
//                dos.writeBytes("Your value");
//                dos.writeBytes(lineEnd);
//                dos.writeBytes(twoHyphens + boundary + lineEnd);
//
//                // send image
//                dos.writeBytes(twoHyphens + boundary + lineEnd);
//                dos.writeBytes("Content-Disposition: form-data; name='uploaded_file';filename='"
//                        + renameFile + "'" + lineEnd);
//
//                dos.writeBytes(lineEnd);
//
//                // create a buffer of  maximum size
//                bytesAvailable = fileInputStream.available();
//
//                bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                buffer = new byte[bufferSize];
//
//                // read file and write it into form...
//                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//                while (bytesRead > 0) {
//                    dos.write(buffer, 0, bufferSize);
//                    bytesAvailable = fileInputStream.available();
//                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//                }
//
//                // send multipart form data necesssary after file data...
//                dos.writeBytes(lineEnd);
//                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
//
//                // Responses from the server (code and message)
//                serverResponseCode = conn.getResponseCode();
//                String serverResponseMessage = conn.getResponseMessage();
//
//                Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
//
//                if(serverResponseCode == 200){
//                    Log.e("Upload file to server",  "File Upload Completed.");
////                    activity.runOnUiThread(new Runnable() {
////                        public void run() {
////                            String message = "File Upload Completed.";
////                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
////                        }
////                    });
//                }
//
//                //close the streams //
//                fileInputStream.close();
//                dos.flush();
//                dos.close();
//
//            } catch (MalformedURLException ex) {
////                dialog.dismiss();
////                ex.printStackTrace();
////
////                activity.runOnUiThread(new Runnable() {
////                    public void run() {
////                        Toast.makeText(activity, "MalformedURLException : : check script url.", Toast.LENGTH_SHORT).show();
////                    }
////                });
//
//                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
//            } catch (Exception e) {
////                dialog.dismiss();
////                e.printStackTrace();
////
////                activity.runOnUiThread(new Runnable() {
////                    public void run() {
////                        Toast.makeText(activity, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
////                    }
////                });
//
//                Log.e("Upload file to server", "Exception : " + e.getMessage(), e);
//            }
//            //dialog.dismiss();
//            return serverResponseCode;
//
//        }
//    }
}
