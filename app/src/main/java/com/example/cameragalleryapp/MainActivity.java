//todo the GPS prints successful in info
package com.example.cameragalleryapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import javax.security.auth.login.LoginException;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int SEARCH_ACTIVITY = 2; // Intent Code
    static final int SHARE_PIC_REQUEST = 3; // Sharing to social media

    static String myCurrentPhotoPath; // this will be updated every time a new image is taken
    static String myCurrentCaptionPath; // this will be updated every time a new image is taken
    static String myStoragePath; // this will remain the same through the program

    static List <String> fileNameList  = new ArrayList<String>(); // List of all the files in the directory (including the image and caption files)
    static List <String> fileShortNameList = new ArrayList<String>(); // List of all names of the image without the extension

    static File storageDir; // Working directory path

    static int imageCount = 0; // Used to increment the name of the file
    static int currentlyDisplayedImageIndex = 0; // Used for displaying the image from the list

    // Defining Permission codes.
    // We can give any value
    // but unique for each permission.
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    // GPS objects //todo fill details in here
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If permission is granted then attempting to use GPS //todo ask tej about how it exactly works
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

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
        Log.d("MainActivity", "onCreate: myStoragePath: " + myStoragePath);

        // Determine the current image count
        updateImageCount();
        displayDefaultImage();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("Location", location.toString());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        // Checks if permission is granted, if not it will default and ask for permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }//onCreate end

    // Displays an empty image or some default image.
    // This method is used at the start up and/or when there are no images in the list
    private void displayDefaultImage(){
        ImageView galleryImageView = (ImageView) findViewById(R.id.galleryImageView);
        galleryImageView.setImageResource(R.drawable.ic_launcher_background); //todo change this to something nicer

    }

    // Displays/updates the image on the screen using the currentlyDisplayedImageIndex
    // If no images exist it will display the default image
    private void displayCurrentImage(){

        // Get the total number of images from the list. This is different from imageCount.
        int numberOfImages = fileShortNameList.size();

        // When there are images in the list
        if (numberOfImages > 0){
            // Get the image file name for the new displaying index
            String imageFileName = fileShortNameList.get(currentlyDisplayedImageIndex) + ".jpg";

            // Show the image
            ImageView mImageView = (ImageView) findViewById(R.id.galleryImageView); //grab handle
            mImageView.setImageBitmap(BitmapFactory.decodeFile(myStoragePath+ "/" + imageFileName)); //JPEG to BITMAP (bit map has intensity at each pixel)

            showCaption();

        }
        // When there no images in the list, display default
        else if (numberOfImages == 0){
            displayDefaultImage();
        }
    }


    public void captionClick(View v) {
        Log.d("MainActivity", "captionClick: called");

        EditText captionEditText = (EditText) findViewById(R.id.captionEditText);
        TextView captionTextView = (TextView) findViewById(R.id.captionTextView);
        String caption = captionEditText.getText().toString(); // Capture the caption string
        captionTextView.setText(caption);
        Log.d("MainActivity", "captionClick: caption: " + caption + ", currentlyDisplayedImageIndex: " + currentlyDisplayedImageIndex);

        Debug.printList("fileShortNameList", fileShortNameList);

        // Load the data object associated to the image
        String dataFileName = fileShortNameList.get(currentlyDisplayedImageIndex) + ".dat";
        ImageData myImageData_ = null; // create an empty instance to hold the data
        myImageData_ = (ImageData) Pickle.load(myImageData_, storageDir + "/" + dataFileName);

        // Update the caption in the object
        myImageData_.caption = caption;

        // Save the data object to the file
        Pickle.save(myImageData_, storageDir + "/" + dataFileName);

        Log.d("MainActivity", "captionClick: " + "caption: " +  myImageData_.caption +", timeStamp: "+ myImageData_.timeStamp);

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
    // SNAP button runs this method
    public void takePhotoClick(View v)
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            File photoFile = null;

            // Create an Image file with Caption file
            try {
                photoFile = createImageFile();
            }
            catch (IOException ex) {
                // Error occurred while creating the File
            }

            // if the File was successfully created, write an image to the file from the intent of taking photo
            if (photoFile != null)
            {

                // Obtain the URI for the files
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.cameragalleryapp.fileprovider", photoFile);

                // Write the image file when done with the takePictureIntent
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI); //  Standard Android Take Picture Intent
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE); // Start the activity
            }

        }
    }

    // When the filter button was pressed, jump to that page.
    public void filterPhotoClick (View v) {
        Intent searchIntent = new Intent(this, SearchActivity.class);
        startActivityForResult(searchIntent, SEARCH_ACTIVITY); //parent activity is expecting int result form child activity

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
        File image = new File(storageDir, imageFileName + ".jpg");
        File data = new File(storageDir, imageFileName + ".dat");

        // Obtain paths of the file object
        myCurrentPhotoPath = image.getAbsolutePath(); // Update the image path
        myCurrentCaptionPath = data.getAbsolutePath(); // Update the caption path
        Log.d("MainActivity", "createImageFile: myCurrentPhotoPath: "+ myCurrentPhotoPath);
        Log.d("MainActivity", "createImageFile: myCurrentCaptionPath: "+ myCurrentCaptionPath);

        // Create a data file on the file system associated with the image
        ImageData myImageData = new ImageData();
        myImageData.caption = "";
        myImageData.timeStamp = timeStamp;
        Pickle.save(myImageData ,myCurrentCaptionPath); // Save the file

        // Image count will be determined after the files are created in the file system
        return image;
    }

    // todo need to include the caption that comes with the image
    // Share image via intent to wake up social media app installed on device
    public void uploadPhotoClick (View v) {
        Log.d("MainActivity", "uploadPhotoClick: called");

        // Check if app is installed
        String packageName = "com.facebook.katana"; //facebook app package name (not messenger)
        final boolean packageInstalled = isPackageInstalled(packageName, this);
        Log.i("MainActivity", "uploadPhotoClick: Package installed = " + packageInstalled);

        // todo consider making the file path global
        // File path to invoke intent
        String imageFileName = fileShortNameList.get(currentlyDisplayedImageIndex) + ".jpg";
        String mPath = myStoragePath + "/" + imageFileName;

        // todo consider making 'caption' a global variable
//        EditText captionEditText = (EditText) findViewById(R.id.captionEditText);
//        TextView captionTextView = (TextView) findViewById(R.id.captionTextView);
//        String caption = captionEditText.getText().toString(); // Capture the caption string

        // Create an intent type that is used to send to social media platforms or any other app
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg"); //default image type
        share.putExtra(Intent.EXTRA_STREAM,Uri.parse(mPath)); //parse the string to include only file path
        share.putExtra(Intent.EXTRA_TEXT, "I sent you an image, here is the text."); //on Whatsapp, this is seen under the image
        share.putExtra(Intent.EXTRA_TITLE,"Sent you a title" );
        share.putExtra(Intent.EXTRA_SUBJECT,"Sent you a subject" );
//        share.setPackage(packageName); //comment this out if you want to share via any app

        // todo ask tej how he wants the upload button to be disabled
        // Change what the upload button reads and notify the user the upload processing has begun
        final Button uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        uploadPhotoButton.setEnabled(false);
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
        uploadPhotoButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                uploadPhotoButton.setEnabled(true);
            }
        }, 5000);

        startActivityForResult(Intent.createChooser(share, "Share The Image Via"),SHARE_PIC_REQUEST);

//        uploadPhotoButton.setEnabled(true);
//        Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show();
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


    // TODO implement this later when using gridview
    // Show all the images
    public void viewPhotoClick (View v) {
        Log.d("MainActivity","viewPhotoClick: called");
        Intent intent = new Intent(this, ViewPhotoActivity.class);
        startActivity(intent);
    }


    public void scrollPhotoLeftClick(View v) {
        Log.d("MainActivity","scrollPhotoLeftClick: called");

        // Get the total number of images form the list. This is different from imageCount.
        int numberOfImages = fileShortNameList.size();

        // When there are images in the list
        if (numberOfImages > 0){

            // decrement the display index
            currentlyDisplayedImageIndex -= 1;
            if (currentlyDisplayedImageIndex <0) currentlyDisplayedImageIndex += numberOfImages; // ratify for the negative index value (make positive)
            currentlyDisplayedImageIndex %= (numberOfImages);
            Log.d("MainActivity","scrollPhotoRightClick: Index: " + currentlyDisplayedImageIndex + "; Number of Images: " + numberOfImages);

            // Update the image on the screen
            displayCurrentImage();

            // Update the caption on the screen. Load from the file. Show on the screen.
            showCaption();
        }

        // When there no images in the list, display default
        else if (numberOfImages == 0){
            displayDefaultImage();
            clearCaptionTextView();
        }

        // Clear the Text Edit field for Caption.
        clearCaptionTextEdit();
    }

    public void scrollPhotoRightClick (View v) {
        Log.d("MainActivity","scrollPhotoRightClick: called");

        // Get the total number of images from the list. This is different from imageCount.
        int numberOfImages = fileShortNameList.size();


        // When there are images in the list
        if (numberOfImages > 0){

            // increment the display index
            currentlyDisplayedImageIndex += 1;
            currentlyDisplayedImageIndex %= (numberOfImages);
            Log.d("MainActivity","scrollPhotoRightClick: Index: " + currentlyDisplayedImageIndex + "; Number of Images: " + numberOfImages);

            // Update the image on the screen
            displayCurrentImage();

            // Update the caption on the screen. Load from the file. Show on the screen.
            showCaption();
        }

        // When there no images in the list, display default
        else if (numberOfImages == 0){
            displayDefaultImage();
            clearCaptionTextView();
        }

        // Clear the Text Edit field for Caption.
        clearCaptionTextEdit();
    }

    // Display/update the caption on the screen.
    // - Load the data from the file for the corresponding viewing image
    // - Show the caption on the screen.
    // args: view: view object of the context
    private void showCaption(){
        Log.d("MainActivity", "showCaption: called");

        TextView captionTextView = (TextView) findViewById(R.id.captionTextView);

        // Load the data object associated to the image
        String dataFileName = fileShortNameList.get(currentlyDisplayedImageIndex) + ".dat";
        ImageData myImageData_ = null; // create an empty instance to hold the data
        myImageData_ = (ImageData) Pickle.load(myImageData_, storageDir + "/" + dataFileName);

        // Obtain the caption data from the object
        String caption = myImageData_.caption;

        // Display the caption on the screen
        captionTextView.setText(caption);

        Log.d("MainActivity", "" + "caption: " +  myImageData_.caption +", timeStamp: "+ myImageData_.timeStamp);

    }

    // Clear the Text Edit field for Caption. User should have an empty box to start typing in.
    // Needed when user entered the text edit and then moves on to another photo and put a caption there
    private void clearCaptionTextEdit() {
        Log.d("MainActivity", "clearCaptionTextEdit: called");

        EditText captionEditText = (EditText) findViewById(R.id.captionEditText);
        captionEditText.setText("");
    }


    // Clear the Text View field for Caption. User should see nothing after a new picture is taken.
    // Needed when a new photo is taken, and we are looking to enter a new caption or half no caption.
    private void clearCaptionTextView() {
        Log.d("MainActivity", "clearCaptionTextView: called");

        TextView captionTextView = (TextView) findViewById(R.id.captionTextView);
        captionTextView.setText("");
    }

    // When coming back from another activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MainActivity", "onActivityResult: called");
        //child activity sends result to parent activity
        super.onActivityResult(requestCode, resultCode, data);

        // If returning from Request Image Capture Intent, display the image onto the home screen
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d("MainActivity", "onActivityResult: returned from REQUEST_IMAGE_CAPTURE");
            ImageView mImageView = (ImageView) findViewById(R.id.galleryImageView); //grab handle
            mImageView.setImageBitmap(BitmapFactory.decodeFile(myCurrentPhotoPath)); //JPEG to BITMAP (bit map has intensity at each pixel)

            // Clear the Text Edit and Text View fields for Caption.
            clearCaptionTextEdit();
            clearCaptionTextView();

            // Update the image count. Update the List of files. Needed for naming future naming and image preview.
            updateImageCount();
        }

        // If returning from Search Activity Intent
        else if (requestCode == SEARCH_ACTIVITY){
            Log.d("MainActivity", "onActivityResult: returned from SEARCH_ACTIVITY");
            displayCurrentImage();
        }

        currentlyDisplayedImageIndex = fileShortNameList.size() - 1; // update the index
        Log.d("MainActivity", "onActivityResult: currentlyDisplayedImageIndex: "+ currentlyDisplayedImageIndex + ", fileShortNameList.size(): " + fileShortNameList.size());
    }


    // Convert the image count into a string with fixed length. Such string is used in the image name
    // for example: input: 1432. Output: "IMG_01432"
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
    // Out: updates fileShortNameList
    static public void updateListDirectory(){
        Log.d("MainActivity", "updateListDirectory: called");

        File dir = storageDir;

        File[] files = dir.listFiles(); // capture all the files in the directory

        // Update the fileList, fileShortNameList
        fileNameList.clear();
        fileShortNameList.clear();

        for (File file : files) {
            String fileName = file.getName();
            String fileShortName;

            // Add the fileName to the list
            fileNameList.add(fileName);


            // Assumption: the name of the .dat and .jpg files are the same to have it working

            // Check if the file name is the same as the image file name, and add to the list
            boolean isImage = fileName.matches("(.*)IMG_[0-9]{5}.jpg(.*)");
            if (isImage){

                fileShortName = fileName.replaceAll(".jpg", ""); // get rid of the extension.

                fileShortNameList.add(fileShortName);
            }

            // Check if the file name is the same as the data file name, and add to the list
            boolean isData = fileName.matches("(.*)IMG_[0-9]{5}.dat(.*)");

            Log.i("FILE NAME", file.getName() + ", isImage: " + isImage + ", isData: " + isData);
        }


    }


    // Upon startup, determine the image count.
    // - Scan the working directory for all files, and find the file with the highest count.
    // - Update the fileShortNameList
    static private void updateImageCount(){
        Log.d("MainActivity", "updateImageCount: "+"called");

        // Update the List of all the files Directory.
        updateListDirectory();

        int maxNumber = 0; // Init the max number to zero

        // Go through each each file name and determine the maximum value of the image count
        for (String fileName_ : fileNameList ) {

            String digits_str;
            int number;

            // Try extract the digits form the file name to determine the maximum image count
            try{
                digits_str = fileName_.replaceAll("[^0-9]", ""); // replace any character that is not a digit with nothing
                number = Integer.parseInt(digits_str);
            }
            // File name doesn't match the image naming convention
            catch(Exception e) {
                number = 0;
            }

            // Update the max number
            maxNumber = Math.max(maxNumber, number);

        }

        imageCount = maxNumber;
        Log.d("MainActivity", "imageCount: " + imageCount);
    }


}
