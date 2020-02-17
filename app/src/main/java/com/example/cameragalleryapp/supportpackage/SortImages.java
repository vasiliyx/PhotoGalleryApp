//todo - this is not implemented, use Pickle
package com.example.cameragalleryapp.supportpackage;
import android.util.Log;


//ADCB
import com.example.cameragalleryapp.MainActivity; //import the other class

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortImages {

    public static void main(String[] args) throws IOException {

        List<String> contactList = new ArrayList();
        contactList.add("Ann");
        contactList.add("Sam");
        contactList.add("John");
        contactList.add("Mike");
        contactList.add("Peter");
        Collections.sort(contactList, ALPHABETICAL_ORDER);
        System.out.println(contactList);

        // to access the static method
//        MainActivity.updateListDirectory();

//        // Create a local copy of the list, so that we can modify the global list
//        List<String> fileShortNameList_ = new ArrayList<>(); //declare local copy
//        fileShortNameList_.addAll(MainActivity.fileShortNameList); //move global copy to local copy
//        MainActivity.fileShortNameList.clear(); //clear global copy
//
//        // Iterate through local copy and to global copy whatever fits criteria
//        for (int i = 0; i < fileShortNameList_.size(); i++) {
//
////            Collections.sort(fileShortNameList_);
//            // Extract the string name from the list item
//            String fileShortName = fileShortNameList_.get(i);
//
//            // sort alphabetically
//            Collections.sort(fileShortNameList_, ALPHABETICAL_ORDER);
//
//            System.out.println(fileShortNameList_);
//            Log.d("SearchActivity", "Found: " + fileShortName);
//            MainActivity.fileShortNameList.add(fileShortName);
//
//        }
    }


    private static Comparator<String> ALPHABETICAL_ORDER = new Comparator<String>() {
        public int compare(String str1, String str2) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
            if (res == 0) {
                res = str1.compareTo(str2);
            }
            return res;
        }
    };
}






//// sort alphabetically
//            Collections.sort(fileShortNameList_, new Comparator<String>() {
//@Override
//public int compare(String s1, String s2) {
////                    String s1 = list.get(i);
////                    String s2 = list2.get(i+1);
////                    return s1.compareToIgnoreCase(s2); //compares the two strings and ignore uppercase/lowercase
//        int res = String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
//        if (res == 0) {
//        res = s1.compareTo(s2);
//        }
//        return res;
//        }
//        });




