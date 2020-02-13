package com.example.cameragalleryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.lang.Double.parseDouble;


public class SearchActivity extends AppCompatActivity {

    //private EditText fromDate;
    //private EditText toDate;
    private Calendar fromCalendar;
    private Calendar toCalendar;
    private DatePickerDialog.OnDateSetListener fromListener;
    private DatePickerDialog.OnDateSetListener toListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //fromDate = (EditText) findViewById(R.id.timeStartEditText);
        //toDate   = (EditText) findViewById(R.id.timeEndEditText);
    }


    // ?? What is this for?
    // TODO remove this if not needed
    public void cancel(final View view) {
        finish();
    }


    // ?? What is this for?
    // TODO remove this if not needed
    public void search(final View view) {
        Intent intent = new Intent();
        //intent.putExtra("STARTDATE", fromDate.getText().toString());
        //intent.putExtra("ENDDATE", toDate.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }


    // When the enter button is pressed, look for the key word in all the images.
    public void searchForCaptionClick(View v) {
        Log.d("SearchActivity", "searchForCaptionClick: called");

        EditText keywordsEditText = (EditText) findViewById(R.id.keywordsEditText);
        String captionRef = keywordsEditText.getText().toString(); // Capture the caption string

        // Update/Reset the list Directory
        MainActivity.updateListDirectory();

        // When empty string is given, reset the search and go back
        if (captionRef == "") {
            Log.d("SearchActivity", "searchForCaption: reset search");

            // Display a toast of the results
            Toast.makeText(getApplicationContext(), "Reset Search", Toast.LENGTH_SHORT).show();

            // Jump back to Main Activity
            finish();
        }

        // Create a local copy of the list, so that we can modify the global list
        List<String> fileShortNameList_ = new ArrayList<>();
        fileShortNameList_.addAll(MainActivity.fileShortNameList);
        MainActivity.fileShortNameList.clear();

        // Iterate through the file name
        for (int i = 0; i < fileShortNameList_.size(); i++) {

            // Update the loop variables
            String fileShortName = fileShortNameList_.get(i);
            String dataFileName = fileShortName + ".dat";

            //for (String dataFileName : MainActivity.dataFileNameList){

            // Load the data object associated to the image
            ImageData myImageData_ = null; // create an empty instance to hold the data
            myImageData_ = (ImageData) Pickle.load(myImageData_, MainActivity.storageDir + "/" + dataFileName);

            // Obtain the caption data from the object
            String captionFile = myImageData_.caption;

            // Compare the caption ref with caption file
            boolean isFound = captionFile.contains(captionRef);

            // If found add back to the general list list
            if (isFound) {
                Log.d("SearchActivity", "Found: " + fileShortName);
                MainActivity.fileShortNameList.add(fileShortName);
            }
        }
        Log.d("SearchActivity", "searchForCaption: finished search");

        // Display a toast of the results: how many images where found
        String numberOfMatches_str = Integer.toString(MainActivity.fileShortNameList.size());
        Toast.makeText(getApplicationContext(), "Found " + numberOfMatches_str + " Images", Toast.LENGTH_SHORT).show();

        // Reset the display index
        MainActivity.currentlyDisplayedImageIndex = 0;

        // Jump back to Main Activity
        finish();
    }


    // When the go button is pressed, look for matching time stamp.
    public void searchForTimeClick(View v) throws ParseException {
        Log.d("SearchActivity", "searchForStartTimeClick: called");

        // Capture the user input and convert into string
        EditText timeStartEditText = (EditText) findViewById(R.id.timeStartEditText);
        EditText timeEndEditText = (EditText) findViewById(R.id.timeEndEditText);
        String timeStart_str = timeStartEditText.getText().toString();
        String timeEnd_str = timeEndEditText.getText().toString();
        Log.d("SearchActivity", "searchForStartTimeClick: timeStart_str: " + timeStart_str);
        Log.d("SearchActivity", "searchForStartTimeClick: timeEnd_str: " + timeEnd_str);

        // Parse the time stamp string into the proper 'Date' format
        Date timeStartRef = parseTimeStamp(timeStart_str);
        Date timeEndRef = parseTimeStamp(timeEnd_str);

        // Handle when timeStartRef and/or timeEndRef inputs are invalid
        // If no valid start time was given, make it as early as possible
        if (timeStartRef == null) {
            timeStartRef = parseTimeStamp("20000101 000001");
        }
        // If no valid end time was given, make it as late as possible
        if (timeEndRef == null) {
            timeEndRef = parseTimeStamp("20990101 000001");
        }

        // Update/Reset the list Directory
        MainActivity.updateListDirectory();

        // Create a local copy of the list, so that we can modify the global list
        List<String> fileShortNameList_ = new ArrayList<>();
        fileShortNameList_.addAll(MainActivity.fileShortNameList);

        MainActivity.fileShortNameList.clear();

        // Iterate through the file name
        for (int i = 0; i < fileShortNameList_.size(); i++) {

            // Update the loop variables
            String fileShortName = fileShortNameList_.get(i);
            String dataFileName = fileShortName + ".dat";

            // Load the data object associated to the image
            ImageData myImageData_ = null; // create an empty instance to hold the data
            myImageData_ = (ImageData) Pickle.load(myImageData_, MainActivity.storageDir + "/" + dataFileName); //load from pickle and cast as ImageData

            // Obtain the caption data from the object
            String timeStampFile_str = myImageData_.timeStamp;
            Date timeStampFile = parseTimeStamp(timeStampFile_str);

            // Check that the timestamp is within the specified limits
            // timeStartRef < timeStampFile < timeEndRef
            if (timeEndRef.compareTo(timeStampFile) > 0 &&      // (timeEndRef - timeStampFile) > 0
                    timeStampFile.compareTo(timeStartRef) > 0) {  // (timeStampFile - timeStartRef) > 0

                Log.d("SearchActivity", "Found: " + fileShortName);
                MainActivity.fileShortNameList.add(fileShortName);
            }
        }
        Log.d("SearchActivity", "searchForTime: finished search");

        // Display a toast of the results: how many images where found
        String numberOfMatches_str = Integer.toString(MainActivity.fileShortNameList.size());
        Toast.makeText(getApplicationContext(), "Found " + numberOfMatches_str + " Images", Toast.LENGTH_SHORT).show();

        // Reset the display index
        MainActivity.currentlyDisplayedImageIndex = 0;

        // Jump back to Main Activity
        finish();
    }


    // Parse the time stamp into the proper format
    // Accepted forms: yyyyMMddHHmmss, yyyy/MM/dd HH:mm:ss, yyyy.MM.dd HH.mm.ss
    // Any non digit character will be thrown away from the string
    // If the format doesn't match, returns null
    private Date parseTimeStamp(String time_str) throws ParseException {
        Log.d("SearchActivity", "parseTimeStamp: called: time_str " + time_str);

        // Convert  "1996/02/21 23:59:59" into "19960221235959"
        time_str = time_str.replaceAll("[^0-9]", ""); // Replace anything that is not a digit with nothing

        // return nothing if doesn't match the format (not enough or too many digits were given)
        if (time_str.length() != 14) {
            Log.d("SearchActivity", "parseTimeStamp: invalid entry: " + time_str);
            return null;
        }

        // Convert to the proper time format
        String pattern = "yyyyMMddHHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date date = simpleDateFormat.parse(time_str);

        return date;
    }//end parseTimeStamp


    public void searchForLocationClick(View v) throws ParseException {
        Log.d("SearchActivity", "searchForLocationClick: called");

        // Capture the coordinate inputs and convert into doubles
        EditText topLeftLatEditText = (EditText) findViewById(R.id.topLeftLatEditText);
        EditText topLeftLongEditText = (EditText) findViewById(R.id.topLeftLongEditText);
        EditText bottomRightLatEditText = (EditText) findViewById(R.id.bottomRightLatEditText);
        EditText bottomRightLongEditText = (EditText) findViewById(R.id.bottomRightLongEditText);
        Double topLeftLatRef = parseDouble(topLeftLatEditText.getText().toString());
        Double topLeftLongRef = parseDouble(topLeftLongEditText.getText().toString());
        Double bottomRightLatRef = parseDouble(bottomRightLatEditText.getText().toString());
        Double bottomRightLongRef = parseDouble(bottomRightLongEditText.getText().toString());
        Log.d("SearchActivity", "searchForLocationClick: topLeftLatRef: " + topLeftLatRef);
        Log.d("SearchActivity", "searchForLocationClick: topLeftLongRef: " + topLeftLongRef);
        Log.d("SearchActivity", "searchForLocationClick: bottomRightLatRef: " + bottomRightLatRef);
        Log.d("SearchActivity", "searchForLocationClick: bottomRightLongRef: " + bottomRightLongRef);

//        // Parse the time stamp string into the proper 'Date' format
//        Date timeStartRef = parseTimeStamp(timeStartRef_str);
//        Date timeEndRef = parseTimeStamp(timeEndRef_str);

//        // Handle when inputs are invalid
//        // If no valid location was given, make it top left corner
//        if (topLeftLatRef == "") {
//            topLeftLatRef = "85";
//        }
//        if (topLeftLongRef == "") {
//            topLeftLongRef = "-180";
//        }
//
//        // If no valid location was given, make it bottom right corner
//        if (bottomRightLatRef == "") {
//            bottomRightLatRef = "-85";
//        }
//        if (bottomRightLongRef == "") {
//            bottomRightLongRef = "180";
//        }

        // Update/Reset the list Directory
        MainActivity.updateListDirectory();

        // Create a local copy of the list, so that we can modify the global list
        List<String> fileShortNameList_ = new ArrayList<>();
        fileShortNameList_.addAll(MainActivity.fileShortNameList);
        MainActivity.fileShortNameList.clear();

        // Iterate through the file name
        for (int i = 0; i < fileShortNameList_.size(); i++) {

            // Update the loop variables
            String fileShortName = fileShortNameList_.get(i);
            String dataFileName = fileShortName + ".dat";

            // Load the data object associated to the image
            ImageData myImageData_ = null; // create an empty instance to hold the data
            myImageData_ = (ImageData) Pickle.load(myImageData_, MainActivity.storageDir + "/" + dataFileName); //load from pickle and cast as ImageData

            // Obtain the caption data from the object
            String locationStampLatFile_str = myImageData_.locationStampLat;
            String locationStampLongFile_str = myImageData_.locationStampLong;
            Double locationStampLatFile = parseDouble(locationStampLatFile_str);
            Double locationStampLongFile = parseDouble(locationStampLongFile_str);

            // Check that the location stamp is within the specified limits
            if ((locationStampLatFile < topLeftLatRef && locationStampLatFile > bottomRightLatRef) && //top left: lat = 85, long = -180
                locationStampLongFile < bottomRightLongRef && locationStampLongFile > topLeftLongRef) { //bottom right: lat = -85, long = 180
                Log.d("SearchActivity", "Found: " + fileShortName);
                MainActivity.fileShortNameList.add(fileShortName);
            }//end if

        }//end for

            Log.d("SearchActivity", "searchForLocation: finished search");

            // Display a toast of the results: how many images where found
            String numberOfMatches_str = Integer.toString(MainActivity.fileShortNameList.size());
            Toast.makeText(getApplicationContext(), "Found " + numberOfMatches_str + " Images", Toast.LENGTH_SHORT).show();

            // Reset the display index
            MainActivity.currentlyDisplayedImageIndex = 0;

            // Jump back to Main Activity
            finish();

    }//end searchForLocationClick
}//end SearchActivity
