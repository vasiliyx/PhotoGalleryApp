package com.example.cameragalleryapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static String myCurrentPhotoPath; // this will be updated every time a new image is taken
    static String myCurrentCaptionPath; // this will be updated every time a new image is taken
    static String myStoragePath; // this will remain the same through the program

    static List <String> fileNameList  = new ArrayList<String>(); // List of all the files in the directory

    static File storageDir; // Working directory path

    static int imageCount = 0; // create an init the count to zero unless there already exists an image with higher number

    // Defining Permission codes.
    // We can give any value
    // but unique for each permission.
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO figure the permisions out: how to request them in case they've been revoked
        //checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
        //checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);

        // Get storage path
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        myStoragePath = storageDir.getAbsolutePath();

        // Determine the current image count
        updateImageCount();

        Log.d("MainActivity", "onCreate: myStoragePath: " + myStoragePath);
    }


    // Function to check and request permission.
    // TODO figure the permisions out: how to request them in case they've been revoked
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
                == PackageManager.PERMISSION_DENIED)
        {
            // TODO for some reason it shows that permision is denied... Figure it out!
            Log.d("MainActivity", "checkPermission: Permission Denied: requesting: "+permission);
            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] { permission },
                    requestCode);
        }
        else {
            Log.d("MainActivity", "checkPermission: Permission already granted: "+permission);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void takePhotoClick(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;

            // Create an Image file with Caption file
            try {
                photoFile = createImageFile();
            }
            catch (IOException ex) {
                // Error occurred while creating the File
            }

            // if the File was successfully created, write an image to the file from the intent of taking photo
            if (photoFile != null) {

                // Obtain the URI for the files
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.cameragalleryapp.fileprovider", photoFile);


                // Write the image file when done with the takePictureIntent
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI); //  Standard Android Take Picture Intent
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE); // Start the activity
            }

            updateListDirectory(storageDir);
        }
    }

    // This will create an image file as well as the caption file
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public File createImageFile() throws IOException {
        Log.d("MainActivity", "createImageFile: called");

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageCount_str = intToString(imageCount+1); // the count for the next image
        String imageFileName = "IMG_" + imageCount_str; // add time stamp to file name
        Log.d("MainActivity", "createImageFile: imageCount_str: "+ imageCount_str);
        Log.d("MainActivity", "createImageFile: imageFileName: "+ imageFileName);

        // Create a blank image and caption object, but they are not written on the file system yet
        File image = new File(storageDir, imageFileName + ".jpg"); // USE THIS
        File text = new File(storageDir, imageFileName + ".txt"); // USE THIS


        // Obtain paths of the file object
        myCurrentPhotoPath = image.getAbsolutePath(); // Update the image path
        myCurrentCaptionPath = text.getAbsolutePath(); // Update the caption path
        Log.d("MainActivity", "createImageFile: myCurrentPhotoPath: "+ myCurrentPhotoPath);
        Log.d("MainActivity", "createImageFile: myCurrentCaptionPath: "+ myCurrentCaptionPath);


        // Create an a blank text file on the file system
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(myCurrentCaptionPath), "utf-8"))) {
            writer.write(timeStamp); // Write a time stamp
        }

        imageCount++; // Increment the current image count
        return image;
    }


    public void searchPhotoClick (View v) {
        Intent intent = new Intent(this, ViewPhotoActivity.class);
        startActivity(intent);

    }

    //
    public void viewPhotoClick (View v) {

    }


    public void scrollPhotoLeftClick(View v) {


    }

    public void scrollPhotoRightClick (View v) {

    }



    // When coming back from another activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ImageView mImageView = (ImageView) findViewById(R.id.ivGallery); //grab handle
            mImageView.setImageBitmap(BitmapFactory.decodeFile(myCurrentPhotoPath)); //JPEG to BITMAP (bit map has intensity at each pixel)
        }
    }



    // Convert the image count into a string with fixed length
    // VB
    static private String intToString(int number) {
        int stringLengthDesired = 5; // number of characters in the string

        String number_str = Integer.toString(number);
        int numberOfZeros = stringLengthDesired - number_str.length();

        // Add zeros in front of the number to match the desired length of string
        for (int i = 0; i<numberOfZeros; i++) {
            number_str = "0" + number_str;
        }

        return number_str;
    }




    // Update the list of all the files in the specified directory
    // Args: Directory path
    // Out: updates fileNameList
    // VB
    static private void updateListDirectory(File dir){
        Log.d("MainActivity", "updateListDirectory: called");

        File[] files = dir.listFiles(); // capture all the files in the directory

        // Update the fileList
        fileNameList.clear();
        for (File file : files) {
            fileNameList.add(file.getName());
            Log.i("FILE NAME:", file.getName());
        }
    }


    // Upon startup, determine the image count
    // Scan the working directory for all files, and find the file with the highest count
    // VB
    static private void updateImageCount(){
        Log.d("MainActivity", "updateImageCount: "+"called");

        // Update the List of all the files Directory.
        updateListDirectory(storageDir);

        int maxNumber = 0; // Init the max number to zero

        // Go through each each file name and determine the maximum value of the image count
        for (String fileName_ : fileNameList ) {

            String digits_str;
            int number;

            // Extract the digits form the file name
            digits_str = fileName_.replaceAll("[^0-9]", ""); // replace any character that is not a digit with nothing

            number = Integer.parseInt(digits_str);

            // Update the max number
            maxNumber = Math.max(maxNumber, number);

        }

        imageCount = maxNumber;

        Log.d("MainActivity", "imageCount: " + imageCount);
    }

}
