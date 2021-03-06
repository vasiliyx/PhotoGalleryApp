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
import org.json.JSONException;

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


import com.example.cameragalleryapp.supportpackage.Pickle;

import static com.example.cameragalleryapp.MainActivity.SHARE_PIC_REQUEST;
import static com.example.cameragalleryapp.MainActivity.currentlyDisplayedImageIndex;
import static com.example.cameragalleryapp.MainActivity.fileShortNameList;


// Uploads photos to social
public class UploadPhotoActivity extends AppCompatActivity {
    private static final String TAG = "UploadPhotoActivity";


    static int tempIndex = 0;
    static int currentlyDisplayedImageIndex = 0; // Used for displaying the image from the list
    static String myStoragePath; // this will remain the same through the program
    static File storageDir; // Working directory path

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        Log.d(TAG, "onCreate: called");



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


    public void serverClick(View v) throws JSONException
    {
        Log.d("MainActivity", "serverClick: called");

        // Get the total number of images form the list
        int numberOfImages = fileShortNameList.size();


        // Put string into proper format for the datatype
        String imageFileName = fileShortNameList.get(MainActivity.currentlyDisplayedImageIndex) + ".jpg"; //image
        String dataFileName = fileShortNameList.get(MainActivity.currentlyDisplayedImageIndex) + ".dat"; //image data - location, timestamp, keyword
        String jsnFileName = fileShortNameList.get(MainActivity.currentlyDisplayedImageIndex) + ".jsn";

        // Get storage path
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        myStoragePath = storageDir.getAbsolutePath();
        String imagePath = myStoragePath + "/" + imageFileName;
        String dataPath = myStoragePath + "/" + dataFileName;
        String jsnPath = myStoragePath + "/" + jsnFileName;


        // Load the data object associated to the image
        ImageData myImageData = null; // create an empty instance to hold the data
        myImageData = (ImageData) Pickle.load(myImageData, dataPath);

        // Obtain the data from the current '.dat' file
        // Save it into the json structure
        ImageDataJ myImageDataJ = new ImageDataJ();
        myImageDataJ.caption = myImageData.caption;
        myImageDataJ.locationStampLat = myImageData.locationStampLat;
        myImageDataJ.locationStampLong = myImageData.locationStampLong;
        myImageDataJ.timeStamp = myImageData.timeStamp;


        // Save the Structure into the JSN file
        JsonFile.save(myImageDataJ, storageDir + "/" + jsnFileName);
        System.out.println("Saving JSN the object into storage");




        // Allow background to obtain context and store information
        BackgroundWorker backgroundWorker = new BackgroundWorker(this); // declare, instantiate, initialize
        backgroundWorker.execute(String.valueOf(numberOfImages), imageFileName, imagePath);

        BackgroundWorker backgroundWorker2 = new BackgroundWorker(this); // declare, instantiate, initialize
        backgroundWorker2.execute(String.valueOf(numberOfImages), jsnFileName, jsnPath);
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



}
