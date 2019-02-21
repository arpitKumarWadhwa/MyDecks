package com.kumar.ak.arpit.mydecks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class QueryUtils {

    URL createUrl(String stringUrl){
        URL url = null;
        try {
            url = new URL(stringUrl);
        }
        catch (MalformedURLException e){
            //Log.e("Malformed URL: ","Check your URL");
        }

        return url;
    }

    String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        //if the URL is null then return early
        if(url == null){
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try{
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //If the request was successful i.e response code(200),
            //then read input stream and parse the response.
            if(urlConnection.getResponseCode()==200){
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
            else{
                //Log.e("Unsuccessful Request: ", String.valueOf(urlConnection.getResponseCode()));
            }
        }
        catch (IOException e){
           // Log.e("IOException: ", e.toString());
        }
        finally{
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
            if(inputStream!=null){
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    String readFromStream(InputStream inputStream) throws IOException{
        StringBuilder output = new StringBuilder();
        if(inputStream != null){
            InputStreamReader reader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(reader);
            String line = br.readLine();

            while(line!=null){
                output.append(line);
                line = br.readLine();
            }
        }
        return output.toString();
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            //e.printStackTrace();
            return null;
        }
    }
}

