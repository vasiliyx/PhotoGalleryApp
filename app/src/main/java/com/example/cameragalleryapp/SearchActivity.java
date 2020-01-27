package com.example.cameragalleryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.Calendar;

public class SearchActivity extends AppCompatActivity {

//    private EditText fromDate; //enable when using search by date -RM
//    private EditText toDate;
//    private Calendar fromCalendar;
//    private Calendar toCalendar;
    private DatePickerDialog.OnDateSetListener fromListener;
    private DatePickerDialog.OnDateSetListener toListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
//        EditText fromDateEditText = (EditText) findViewById(R.id.fromDateEditText); //enable when using search by date -RM
//        EditText toDateEditText   = (EditText) findViewById(R.id.toDateEditText); //enable when using search by date -RM
    }


    public void cancel(final View v) {
        finish();
    }

    public void search(final View v) {
        Intent intent = new Intent();
//        intent.putExtra("STARTDATE", fromDateEditText.getText().toString()); //enable when using search by date -RM
//        intent.putExtra("ENDDATE", toDateEditText.getText().toString()); //enable when using search by date -RM
        setResult(RESULT_OK, intent);
        finish();
    }
}
