package com.example.cameragalleryapp;

//TODO allow for both network and gps locations

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cameragalleryapp.supportpackage.Pickle;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int SEARCH_ACTIVITY = 2; // Intent Code
    static final int SHARE_PIC_REQUEST = 3; // Sharing to social media

    static int imageCount = 0; // Used to increment the name of the file
    static int currentlyDisplayedImageIndex = 0; // Used for displaying the image from the list

    static String myCurrentPhotoPath; // this will be updated every time a new image is taken
    static String myCurrentCaptionPath; // this will be updated every time a new image is taken
    static String myStoragePath; // this will remain the same through the program

    public static List<String> fileNameList  = new ArrayList<>(); // List of all the files in the directory (including the image and caption files)
    public static List<String> fileShortNameList = new ArrayList<>(); // List of all names of the image without the extension

    static File storageDir; // Working directory path

    // GPS objects
    LocationManager locationManager;
    LocationListener locationListener;
    Location myLocation;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If permission is granted then attempting to use GPS
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Notify user the location is still loading
        TextView latDebugTextView = findViewById(R.id.latDebugTextView);
        TextView longDebugTextView = findViewById(R.id.longDebugTextView);
        latDebugTextView.setText("LAT: loading...");
        latDebugTextView.setSingleLine(); //doesn't allow number to roll over to next line
        longDebugTextView.setText("LONG: loading...");
        longDebugTextView.setSingleLine();

        // Get storage path
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        myStoragePath = storageDir.getAbsolutePath();
        Log.d("MainActivity", "onCreate: myStoragePath: " + myStoragePath);

        // Determine the current image count
        updateImageCount();
        displayDefaultImage();

        // Register location listener with location manager, and THEN call location changed
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("Location", location.toString());
                myLocation = location; //set to class variable for use outside of method

                // Displays the location on the main activity
                TextView latDebugTextView = findViewById(R.id.latDebugTextView);
                TextView longDebugTextView = findViewById(R.id.longDebugTextView);
                String lat_str = String.valueOf(myLocation.getLatitude());
                String long_str = String.valueOf(myLocation.getLongitude());
                latDebugTextView.setText("LAT: "+ lat_str);
                latDebugTextView.setSingleLine(); //doesn't allow number to roll over to next line
                longDebugTextView.setText("LONG: " + long_str);
                longDebugTextView.setSingleLine();
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

        // Checks if permission is granted, if not it will default and take permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        else
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        // If location disabled, will alert user to enable location
        // It will redirect to the settings page, where user manually turns it on
        if (!(locationManager.isProviderEnabled((LocationManager.NETWORK_PROVIDER)))) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Location Settings");
            builder.setMessage(Html.fromHtml("<font color='#101010'>Please enable location.</font>")); //changes the text color too
//            builder.setIcon(xml)
            builder.setCancelable(false);
            builder.setPositiveButton(Html.fromHtml("<font color='#f23933'>OK</font>"), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            // Launch settings activity
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });
            builder.setNegativeButton(Html.fromHtml("<font color='#222322'>Cancel</font>"), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.cancel();
                        }
                    });
            builder.create().show();
        }


    }//onCreate end


    // Displays an empty image or some default image.
    // This method is used at the start up and/or when there are no images in the list
    private void displayDefaultImage(){
        ImageView galleryImageView = (ImageView) findViewById(R.id.galleryImageView);
        galleryImageView.setImageResource(R.drawable.sym_left_right_white_24dp); //this is the default image when no pic loaded

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


    @RequiresApi(api = Build.VERSION_CODES.M)
    // SNAP button runs this method
    // Uses the intent from image capture to create a file and invoke another activity to show the image
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

        // Declare image information strings
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); //set current time and date
        String locationStampLat, locationStampLong;

        // When location is initially null will crash app if no location has loaded yet (it takes a few seconds)
        try{
            locationStampLat = String.valueOf(myLocation.getLatitude());
            locationStampLong = String.valueOf(myLocation.getLongitude());
        }
        //todo get last known location
        catch (Exception e){ //Null pointer exception if location not found
            locationStampLat = "49";
            locationStampLong = "-123";

            Log.d("MainActivity", "createImageFile: location null");
        }

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
        myImageData.timeStamp = timeStamp;
        myImageData.locationStampLat = locationStampLat;
        myImageData.locationStampLong = locationStampLong;
        myImageData.caption = "";
        Pickle.save(myImageData, myCurrentCaptionPath); // Save the file

        // Image count will be determined after the files are created in the file system
        return image;
    }

    public void uploadPhotoClick(View v) {
        Log.d("MainActivity", "uploadPhotoClick: called");

        // Open another activity to choose upload method
        startActivity(new Intent(this, UploadPhotoActivity.class)
                .putExtra("index", currentlyDisplayedImageIndex) //needs the index of the image
                .putExtra("location", myStoragePath));                   //as well as the location
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


    }//end updateListDirectory


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
