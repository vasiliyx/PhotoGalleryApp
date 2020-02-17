package com.example.cameragalleryapp.supportpackage;

import com.example.cameragalleryapp.ImageData;
import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.After;
import org.junit.Assert;
import junit.framework.JUnit4TestAdapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;


public class PickleUnitTest {

    //Create test objects for local use
    private String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); //set current time and date
    private String locationStampLat = "49.0", locationStampLong = "-122.0";
    private String testPath = "C:\\Users\\Rabby\\Desktop";
    private String testCaption = "Testing";

    // serialize object
    @Test
    public void saveToPickle() throws Exception {
        ImageData testImageDataIn = new ImageData(); // Create a test data file on the file system associated with the image
        ImageData testImageDataOut = null; // Create a test data file on the file system associated with the image
        testImageDataIn.timeStamp = timeStamp;
        testImageDataIn.locationStampLat = locationStampLat;
        testImageDataIn.locationStampLong = locationStampLong;
        testImageDataIn.caption = testCaption;
        Pickle.save(testImageDataIn, testPath); // Save the file
        Pickle.load(testImageDataOut, testPath); // Load the file
        assertEquals(testImageDataIn,testImageDataOut);
    }

//    // deserialize object
//    @Test
//    public void loadFromPickle() throws Exception {
//        // Load the data object associated to the image
//        ImageData testImageData = null; // create an empty instance to hold the data
//        String testPath = "C:\\Program Files\\Java";
//        testImageData = (ImageData) Pickle.load(testImageData, testPath); //load from pickle and cast as ImageData
//    }


}