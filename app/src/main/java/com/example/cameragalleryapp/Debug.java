package com.example.cameragalleryapp;

import android.util.Log;

import java.util.List;

public class Debug {
    static public void printList(String Tag, List<String> list) {

        for (String item : list) {
            Log.d("Debug", Tag+": list item: "+item);
        }
    }
}
