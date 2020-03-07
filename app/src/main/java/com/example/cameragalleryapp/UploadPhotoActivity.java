package com.example.cameragalleryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.jar.Attributes;



import static com.example.cameragalleryapp.MainActivity.SHARE_PIC_REQUEST;
import static com.example.cameragalleryapp.MainActivity.currentlyDisplayedImageIndex;
import static com.example.cameragalleryapp.MainActivity.fileShortNameList;


// Uploads photos to social
public class UploadPhotoActivity extends AppCompatActivity {


    //UploadToServerTask task = new UploadToServerTask();
    //task.execute(new String[] { "https://www.bcit.ca" }); //for http lab
    //task.execute(new String[] { "http://10.0.2.2:8080/midp/hits" }); //for webapp lab using emulator
    // task.execute(new String[] { "http://142.232.61.32:8080/PhotoGallery/hits" }); //for webapp lab using phone
    private static final String SERVER_ADDRESS = "localhost:8081/PhotoGallery";
//^^server address

    static int tempIndex = 0;
    static int currentlyDisplayedImageIndex = 0; // Used for displaying the image from the list
    static String myStoragePath; // this will remain the same through the program

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);



    }

    // todo need to include the caption that comes with the image
    // Share image via intent to wake up social media app installed on device
    public void socialMediaClick(View v) {
        Log.d("MainActivity", "socialMediaClick: called");
        final Button socialMediaButton = findViewById(R.id.socialMediaButton); //to enable/disable button

        // Get the total number of images form the list. This is different from imageCount.
        int numberOfImages = fileShortNameList.size();

        // When there are images in the list
        if (numberOfImages > 0){
            // Check if app is installed
            String packageName = "com.facebook.katana"; //facebook app package name (not messenger)
            final boolean packageInstalled = isPackageInstalled(packageName, this);
            Log.i("MainActivity", "socialMediaClick: Package installed = " + packageInstalled);

            // Obtain the previous intent used - this case was the intent to open this activity
            Intent intent = getIntent();

            // Access parameters from previous activity through intent
            currentlyDisplayedImageIndex = intent.getIntExtra("index", tempIndex);
            myStoragePath = intent.getStringExtra("location");

            // Put string into proper format for the datatype
            String imageFileName = fileShortNameList.get(currentlyDisplayedImageIndex) + ".jpg";
            String mPath = myStoragePath + "/" + imageFileName;

            // Create an intent type that is used to send to social media platforms or any other app
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg"); //default image type
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse(mPath)); //parse the string to include only file path
            share.putExtra(Intent.EXTRA_TEXT, "I sent you an image, here is the text."); //on Whatsapp, this is seen under the image
            share.putExtra(Intent.EXTRA_TITLE,"Sent you a title" );
            share.putExtra(Intent.EXTRA_SUBJECT,"Sent you a subject" );
//        share.setPackage(packageName); //comment this out if you want to share via any app

            // Change what the upload button reads and notify the user the upload processing has begun
            socialMediaButton.setEnabled(false);
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
            socialMediaButton.postDelayed(new Runnable() {
                @Override
                public void run() {
                    socialMediaButton.setEnabled(true);
                }
            }, 1000);

            startActivityForResult(Intent.createChooser(share, "Share The Image Via"),SHARE_PIC_REQUEST);
        }

        // When there no images in the list, display default
        else if (numberOfImages == 0){
            socialMediaButton.setEnabled(false);
        }



    }


    //todo this is where uploading to the cloud will happen, tristan only change this method
    // Share image to the server
    public void serverClick(View v) {
        Log.d("MainActivity", "serverClick: called");
        final Button serverButton = findViewById(R.id.serverButton); //to enable/disable button

        // WRITE CODE HERE

        // Get the total number of images form the list. This is different from imageCount.
        int numberOfImages = fileShortNameList.size();


        // Put string into proper format for the datatype
        String imageFileName = fileShortNameList.get(currentlyDisplayedImageIndex) + ".jpg"; //image
        String dataFileName = fileShortNameList.get(currentlyDisplayedImageIndex) + ".dat"; //image data - location, timestamp, keyword
        String mPath = myStoragePath + "/" + imageFileName;

        String uploadImageName= mPath;
        String image=imageFileName;
        // When there are images in the list
        if (numberOfImages > 0){
            Log.d("MainActivity", "serverClick: items to be writen to server");

           // new UploadImage(image, uploadImageName); //

            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 1);

            Uri selectedImage = Uri.fromFile(new File(mPath));

            //String filePath = getPath(selectedImage);
            //String file_extn = mPath.substring(mPath.lastIndexOf(".") + 1);
            //image_name_tv.setText(mPath);

            try {
               // if (file_extn.equals("img") || file_extn.equals("jpg") || file_extn.equals("jpeg") || file_extn.equals("gif") || file_extn.equals("png")) {
                    //FINE
                    HttpClient httpclient = new DefaultHttpClient();//maybe this is an older library??
                    HttpPost httppost = new HttpPost("LINK TO SERVER");

              //  } else {
                    //NOT IN REQUIRED FORMAT
               // }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        }
        else{

            //we have no pictures
            Log.d("MainActivity", "serverClick: we have no items to be writen to server");
           //call asynch task to upload images


        }




        ///


        // Change what the upload button reads and notify the user the upload processing has begun
        serverButton.setEnabled(false);
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
        serverButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                serverButton.setEnabled(true);
            }
        }, 1000);
    }


//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 1)
//            if (resultCode == Activity.RESULT_OK) {
//                Uri selectedImage = data.getData();
//
//                String filePath = getPath(selectedImage);
//                String file_extn = filePath.substring(filePath.lastIndexOf(".") + 1);
//                image_name_tv.setText(filePath);
//
//                try {
//                    if (file_extn.equals("img") || file_extn.equals("jpg") || file_extn.equals("jpeg") || file_extn.equals("gif") || file_extn.equals("png")) {
//                        //FINE
//                    } else {
//                        //NOT IN REQUIRED FORMAT
//                    }
//                } catch (FileNotFoundException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//    }

//    public String getPath(Uri uri) {
//        String[] projection = {MediaColumns.DATA};
//        Cursor cursor = managedQuery(uri, projection, null, null, null);
//        column_index = cursor
//                .getColumnIndexOrThrow(MediaColumns.DATA);
//        cursor.moveToFirst();
//        imagePath = cursor.getString(column_index);
//
//        return cursor.getString(column_index);
//    }


//
//    private class UploadImage extends AsyncTask< Void, Void, Void>{
//
//        Bitmap image;
//        String name;
//
//        public UploadImage(Bitmap image, String name){
//            this.image = image;
//            this.name = name;
//        }
//        @Override
//        protected Void doInBackground(Void... voids) {
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
//
//            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
//            dataToSend.add(new BasicNameValuePair ("image", encodedImage);
//            dataToSend.add(new BasicNameValuePair("name", name));
//
//            HttpParams httpRequestParams= getHttpRequestParams();
//            HttpClient client = new DefaultHttpClient(httpRequestParams);
//            HttpPost post = new HttpPost(SERVER_ADDRESS+ "/SavePicture");
//            try{
//                post.setEntity(new UrlEncodedFormEntity(dataToSend));
//                client.execute(post);
//
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected Void onPostExecute(Void aVoid){
//
//            super.onPostExecute(aVoid);
//            Toast.makeText(getApplicationContext(),"Image Uploaded", Toast.LENGTH_SHORT).show();
//
//        }
//    }
//
//    private HttpParams getHttpRequestParams(){
//        HttpParams httpRequestParams = new BasicHttpParams();
//        HttpConnectionParams.setConnectionTimeout(httpRequestParams, 1000 * 30);
//        HttpConnectionParams.setSoTimeout(httpRequestParams, 1000 * 30);
//        return httpRequestParams;
//
//    }


    // Check to see if Social Media Application is installed
    private boolean isPackageInstalled(String packageName, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}
