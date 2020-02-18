package com.example.cameragalleryapp.supportpackage;

import com.example.cameragalleryapp.ImageData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;


public class PickleUnitTest {

    //Create test objects for local use
    private String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); //set current time and date
    private String locationStampLat = "49.0", locationStampLong = "-122.0";
    private String testPath = "./.jpg";
    private String testCaption = "Testing";

    // serialize object
    @Test
    public void saveToPickle() throws Exception {
        // Create a test data file on the file system associated with the image
        ImageData testImageDataIn = new ImageData();
        ImageData testImageDataOut = null; // empty instance for output

        // Load into the test Image Data
        testImageDataIn.timeStamp = timeStamp;
        testImageDataIn.locationStampLat = locationStampLat;
        testImageDataIn.locationStampLong = locationStampLong;
        testImageDataIn.caption = testCaption;
        Pickle.save(testImageDataIn, testPath); // Save the file

        // Load from the Pickle Class
        testImageDataOut = (ImageData) Pickle.load(testImageDataOut, testPath); // Load the file

        // Test if parameters are the same
        assertEquals(testImageDataIn.caption,testImageDataOut.caption);
        assertEquals(testImageDataIn.locationStampLat,testImageDataOut.locationStampLat);
        assertEquals(testImageDataIn.locationStampLong,testImageDataOut.locationStampLong);
        assertEquals(testImageDataIn.timeStamp,testImageDataOut.timeStamp);
    }
}