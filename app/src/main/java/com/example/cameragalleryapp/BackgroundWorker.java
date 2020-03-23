// Does background work of saving information and temporarily storing it
// Opens URLConnection with support for HTTP-specific features

package com.example.cameragalleryapp;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewDebug;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

// tutorial: https://www.youtube.com/watch?v=UqY4DY2rHOs
// Asynchronous task runs in the background
public class BackgroundWorker extends AsyncTask<String,Void,String> { //generics or templates
    Context context;
    AlertDialog alertDialog;

    // Pass context to constructor - needed because this is a seperate class
    public BackgroundWorker(Context ctx) {
        context = ctx;
    }


    // Set up alert dialog GUI element
    // Executed before the background processing starts
    @Override
    protected void onPreExecute() {
        //super.onPreExecute();
        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Server Response Code");


    }


    // Opens a http URL connection and POSTS data to server
    // Similar to run() method of thread - DO NOT PUT UI STUFF HERE
    // It can send results multiple times to the UI thread by publishProgress() method
    // To notify that the background processing has been completed, we just need to use return
    @Override
    protected String doInBackground(String... params) { //generics
        int numberOfImages = Integer.valueOf(params[0]);
        String imageFileName = params[1];
        String dataFileName = params[2];
        String mPath = params[3]; //source file
        //String login_url = "http://10.0.2.2/login.php";         //local host ip
        String login_url = "http://24.84.210.161:8080/remote_login.php"; //server address URL
        String serverUploadAddress = "http://10.0.2.2:8081/servletFileUploader/androidUpload";
//        String serverUploadAddress = "http://google.ca";

        int serverResponseCode = 0;
        HttpURLConnection httpURLConnection = null;
        DataOutputStream dataOutputStream = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(mPath);


        // If there are images to upload and it is a valid file, open http URL connection
        if(numberOfImages > 0 && sourceFile.isFile()) {
            //post some data
            try {
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(login_url);

                // Open a HTTP  connection to  the URL
                httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST"); //clients sends info in body, servers response with empty body
                httpURLConnection.setDoOutput(true); //
                httpURLConnection.setDoInput(true);
                httpURLConnection.setUseCaches(false); // Don't use a Cached Copy
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                httpURLConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
                httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                httpURLConnection.setRequestProperty("uploaded_file", imageFileName);

                // Set output stream of httpURL connection to data type
                dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());

                // add parameters
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"type\""
                        + lineEnd);
                dataOutputStream.writeBytes(lineEnd);

                // assign value
                dataOutputStream.writeBytes("Your value");
                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);

                // send image
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name='uploaded_file';filename='"
                        + imageFileName + "'" + lineEnd); //changed renameFile to imageFileName

                dataOutputStream.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dataOutputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necesssary after file data...
                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = httpURLConnection.getResponseCode();
                String serverResponseMessage = httpURLConnection.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

                // Flush buffer and close output
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();

                // Read the response from the server
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                // Catch error if unsuccessful
            } catch (MalformedURLException ex) {
//                dialog.dismiss();
//                ex.printStackTrace();
//
//                activity.runOnUiThread(new Runnable() {
//                    public void run() {
//                        Toast.makeText(activity, "MalformedURLException : : check script url.", Toast.LENGTH_SHORT).show();
//                    }
//                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {
//                dialog.dismiss();
//                e.printStackTrace();
//
//                activity.runOnUiThread(new Runnable() {
//                    public void run() {
//                        Toast.makeText(activity, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
//                    }
//                });

                Log.e("Upload file to server", "Exception : " + e.getMessage(), e);
            }
            //dialog.dismiss();

        }
        return String.valueOf(serverResponseCode);
    }


    // Change the value of the TextView - Notify the user of progress
    // Receives progress updates from doInBackground method,
    // which is published via publishProgress method
    // Can update the UI thread
    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

    }


    // Use the return value (result) of doInBackground
    @Override
    protected void onPostExecute(String result) {
        //super.onPostExecute(aVoid);
        alertDialog.setMessage(result); //show result
        alertDialog.show();             //show response of the server
    }


}
