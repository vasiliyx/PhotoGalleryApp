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

    private EditText fromDate;
    private EditText toDate;
    private Calendar fromCalendar;
    private Calendar toCalendar;
    private DatePickerDialog.OnDateSetListener fromListener;
    private DatePickerDialog.OnDateSetListener toListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        fromDate = (EditText) findViewById(R.id.timeStartEditText);
        toDate   = (EditText) findViewById(R.id.timeEndEditText);
    }


    public void cancel(final View view) {
        finish();
    }

    public void search(final View view) {
        Intent intent = new Intent();
        intent.putExtra("STARTDATE", fromDate.getText().toString());
        intent.putExtra("ENDDATE", toDate.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }


    public void searchForCaptionClick(View v) {
        Log.d("SearchActivity", "searchForCaptionClick: called");

        EditText keywordsEditText = (EditText) findViewById(R.id.keywordsEditText);
        String captionRef = keywordsEditText.getText().toString(); // Capture the caption string

        searchForCaption(captionRef);

        // TODO implement jumping into the MainActivity context. However it should not be here because we have false clicks

    }

    // Args: the caption string
    // Returns the imageName
    private void searchForCaption(String captionRef){
        Log.d("SearchActivity", "searchForCaption: called");

        MainActivity.updateListDirectory();

        if (captionRef == "") {
            Log.d("SearchActivity", "searchForCaption: reset search");

            // Display a toast of the results
            Toast.makeText(getApplicationContext(), "Reset Search", Toast.LENGTH_SHORT).show();
            return;
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


        // TODO if nothing was found
        // ...

        Log.d("SearchActivity", "searchForCaption: finished search");

        // Display a toast of the results: how many images where found
        String numberOfMatches_str = Integer.toString(MainActivity.fileShortNameList.size());
        Toast.makeText(getApplicationContext(), "Found " + numberOfMatches_str + " Images", Toast.LENGTH_SHORT).show();

        // Reset the display index
        MainActivity.currentlyDisplayedImageIndex = 0;

    }


    // TODO RABBY: add a Search button for the Time Search.
    // TODO rename the "searchForStartTimeClick" into "searchForTimeClick" and associate it with the button.
    // TODO RABBY: display the hint of the time stamp format (YYYY/MM/DD HH:MM:DD) or (YYYYMMDD HHMMDD)
    // TODO if wrong format was given, handle the parsing exception to prevent crash
    public void searchForStartTimeClick(View v) throws ParseException {
        Log.d("SearchActivity", "searchForStartTimeClick: called");


        EditText timeEndEditText = (EditText) findViewById(R.id.timeEndEditText);
        String timeEndRef_str = timeEndEditText.getText().toString(); // Capture the caption string

        EditText timeStartEditText = (EditText) findViewById(R.id.timeStartEditText);
        String timeStartRef_str = timeStartEditText.getText().toString(); // Capture the caption string

        Log.d("SearchActivity", "searchForStartTimeClick: timeStartRef_str: " + timeStartRef_str);

        // Parse the time stamp string into the proper format
        Date timeStartRef = parseTimeStamp(timeStartRef_str);
        Date timeEndRef = parseTimeStamp(timeEndRef_str);


        // Update/Reset the list Directory
        MainActivity.updateListDirectory();

        // TODO if one of the timeStartRef or timeEndRef is empty
        // ...

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

    }


    // Parse the time stamp into the proper format
    // Accepted forms: yyyyMMddHHmmss, yyyy/MM/dd HH:mm:ss, yyyy.MM.dd HH.mm.ss
    // Any non digit character will be thrown away from the string
    private Date parseTimeStamp(String time_str) throws ParseException {

        // Convert  "1996/02/21 23:59:59" into "19960221235959"
        time_str = time_str.replaceAll("[^0-9]", ""); // Replace anything that is not a digit with nothing

        // Convert to the proper time format
        String pattern = "yyyyMMddHHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date date = simpleDateFormat.parse(time_str);

        return date;
    }



}
