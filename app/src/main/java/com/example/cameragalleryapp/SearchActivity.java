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
import java.util.ArrayList;
import java.util.Calendar;
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
            String dataFileName = fileShortNameList_.get(i) + ".dat";
            String imageFileName = fileShortNameList_.get(i) + ".jpg";
            String fileShortName = fileShortNameList_.get(i);

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

        // Display a toast of the results
        String numberOfMatches_str = Integer.toString(MainActivity.fileShortNameList.size());
        Toast.makeText(getApplicationContext(), "Found " + numberOfMatches_str + " Images", Toast.LENGTH_SHORT).show();


        // Reset the display index
        MainActivity.currentlyDisplayedImageIndex = 0;

    }








}
