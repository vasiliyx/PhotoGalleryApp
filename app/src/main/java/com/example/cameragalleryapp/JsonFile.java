package com.example.cameragalleryapp;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;



import java.io.FileWriter;
import java.io.IOException;

//com.example.cameragalleryapp.supportpackage.

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class JsonFile {
 
    // Save the object to the memory
    // args:    ImageData: the object instance
    //          path:   the exact path and file name of where it will be stored    
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    static public void save(ImageDataJ myImageData_, String path) throws JSONException
    {
        JSONObject myImageData_json = myImageData_.toJson();
        
        // Write JSON file
        try (FileWriter file = new FileWriter(path)) {
            file.write(myImageData_json.toString());
            file.flush();
            System.out.println("Data is saved in: " + path);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to save the file. ");
        }
        
    }


    // Load the object from the memory
    // args:    
    //          path:   the exact path and file name of where it is be stored 
    // returns: ImageData: the recovered object instance            
    static public ImageDataJ load(String path){
        ImageDataJ myImageData = null;

        try {

            Path path_Path = Paths.get(path);
            List<String> lines  = Files.readAllLines(path_Path);
            String myString = lines.get(0);
            Log.d("AD", myString);

            // Read string from the file
            // String myString = Files.readString(Path.of(path));


            // Convert the string into the JSON
            JSONObject myImageData_json = new JSONObject(myString);


            System.out.println(myImageData_json);            
        

            // Update the Operations Data Structure
            myImageData = new ImageDataJ();
            myImageData.update(myImageData_json);

            System.out.println(myImageData);

            System.out.println("Data is read from: " + path);

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } 
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("File not found");
        } 
        catch (Exception e) {
            //TODO: handle exception
        }

        
        return myImageData; 
    }
    
}
