package com.joshtwigg.cmus.droid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by josh on 07/02/14.
 */
public class ArtRetriever {
    public final String GOOGLE_IMAGE_API_URL = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=";

    public String getImageUrl(final String album, final String artist) {
        try{
            URLConnection connection = (new URL(GOOGLE_IMAGE_API_URL + album + "+" + artist + "+album+cover")).openConnection();
            String line;
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }

            JSONObject json = new JSONObject(builder.toString());
            String imageUrl = json.getJSONObject("responseData").getJSONArray("results").getJSONObject(0).getString("url");
            return imageUrl;

        } catch(Exception e){
            Log.e(getClass().getSimpleName(), "Could not retrieve image url.", e);
            e.printStackTrace();
        }
        return "NO IMAGE";
    }

    public Bitmap getArtFromUrl(final String url) {
        Bitmap bitmap = null;
        InputStream in = null;

        try {
            in = new URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Could not download image from url.", e);
        } finally {
            try{
                in.close();
            }
            catch(Exception e) {

            }
        }

        return bitmap;
    }
}
