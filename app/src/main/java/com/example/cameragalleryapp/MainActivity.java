package com.example.cameragalleryapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    String mCurrentPhotoPath;

    static int imageCount = 0; // create an init the count to zero unless there already exists an image with higher number

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initImageCount(); // Determine the image count
    }

    // SNAP button runs this method
    public void takePhotoClick(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.cameragalleryapp.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // Creates an image file on the file system
    public void filterPhotoClick (View v) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);

    }

    public void scrollPhotoLeftClick(View v) {



    }

    public void scrollPhotoRightClick (View v) {

    }

    // This will create an image file as well as the caption file
    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); //timestamp format
        String imageCount_str = intToString(imageCount);
        String imageFileName = "IMG" + imageCount_str + "_" +  timeStamp + "_"; // add timestamp and image count to file name
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg",storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("createImageFile", mCurrentPhotoPath);
        return image;
    }

    // When coming back from another activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ImageView mImageView = (ImageView) findViewById(R.id.ivGallery); //grab handle
            mImageView.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath)); //JPEG to BITMAP (bit map has intensity at each pixel)

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

    // Upon startup, determine the image count
    // VB
    static private void initImageCount() {
        // TODO: Read from the memory to establish such relationship
        imageCount = 0;
    }




}
