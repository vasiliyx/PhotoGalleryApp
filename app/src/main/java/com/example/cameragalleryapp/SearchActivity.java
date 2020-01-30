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
        List <String> fileShortNameList_ = new ArrayList<>();
        fileShortNameList_.addAll(MainActivity.fileShortNameList);

        MainActivity.fileShortNameList.clear();


        // Iterate through the file name
        for (int i = 0; i< fileShortNameList_.size(); i++){

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
            if (isFound){
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


        EditText timeEndEditText = (EditText) findViewById(R.id.timeEndEditText);
        String timeEndRef_str = timeEndEditText.getText().toString(); // Capture the caption string

        EditText timeStartEditText = (EditText) findViewById(R.id.timeStartEditText);
        String timeStartRef_str = timeStartEditText.getText().toString(); // Capture the caption string

        Log.d("SearchActivity", "searchForStartTimeClick: timeStartRef_str: " + timeStartRef_str);


        // Parse the time stamp string into the proper format
        Date timeStartRef = parseTimeStamp(timeStartRef_str);
        Date timeEndRef = parseTimeStamp(timeEndRef_str);


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
        List <String> fileShortNameList_ = new ArrayList<>();
        fileShortNameList_.addAll(MainActivity.fileShortNameList);

        MainActivity.fileShortNameList.clear();

        // Iterate through the file name
        for (int i = 0; i< fileShortNameList_.size(); i++){

            // Update the loop variables
            String fileShortName = fileShortNameList_.get(i);
            String dataFileName = fileShortName + ".dat";

            // Load the data object associated to the image
            ImageData myImageData_ = null; // create an empty instance to hold the data
            myImageData_ = (ImageData) Pickle.load(myImageData_, MainActivity.storageDir + "/" + dataFileName);

            // Obtain the caption data from the object
            String timeStampFile_str = myImageData_.timeStamp;
            Date timeStampFile = parseTimeStamp(timeStampFile_str);

            // Check that the timestamp is within the specified limits
            // timeStartRef < timeStampFile < timeEndRef
            if (timeEndRef.compareTo(timeStampFile) > 0 &&      // (timeEndRef - timeStampFile) > 0
                    timeStampFile.compareTo(timeStartRef) > 0){  // (timeStampFile - timeStartRef) > 0

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
        if (time_str.length() != 14){
            Log.d("SearchActivity", "parseTimeStamp: invalid entry: " + time_str );
            return null;
        }

        // Convert to the proper time format
        String pattern = "yyyyMMddHHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date date = simpleDateFormat.parse(time_str);

        return date;
    }

}
