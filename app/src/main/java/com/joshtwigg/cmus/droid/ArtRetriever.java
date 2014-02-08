package com.joshtwigg.cmus.droid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by josh on 07/02/14.
 */
public class ArtRetriever {
    private static final String GOOGLE_IMAGE_API_URL = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=";

    private static String[] getImageUrl(final String album, final String artist) {
        URLConnection connection = null;
        try{
            URL url = new URL(GOOGLE_IMAGE_API_URL + album.replace(' ', '+') + "+" + artist.replace(' ', '+') + "+album+cover");
            connection = url.openConnection();
            String line;
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            JSONObject json = new JSONObject(builder.toString());
            JSONArray results = json.getJSONObject("responseData").getJSONArray("results");
            String[] urls = new String[results.length()];
            for (int i = 0; i < results.length(); ++i) {
                urls[i] = results.getJSONObject(i).getString("url");
            }
            return urls;

        } catch(Exception e){
            try {
                int code = ((HttpURLConnection)connection).getResponseCode();
                String msg =  ((HttpURLConnection)connection).getResponseMessage();
                Log.e(ArtRetriever.class.getSimpleName(), "Code " + code + ". message: " + msg + ".", e);
            }
            catch (Exception f) {

            }
            Log.e(ArtRetriever.class.getSimpleName(), "Could not retrieve image url.", e);
        }
        return new String[] {};
    }

    private static Bitmap getArtFromUrl(final String url) {
        Bitmap bitmap = null;
        InputStream in = null;

        try {
            in = new URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e(ArtRetriever.class.getSimpleName(), "Could not download image from url {" + url + "}", e);
        } finally {
            try{
                in.close();
            }
            catch(Exception e) {

            }
        }

        return bitmap;
    }

    private static File saveImage(final Context context, final Bitmap image, final String album, final String artist) {
        File file =  new File(context.getCacheDir(), getName(context, album, artist));
        try {
            file.createNewFile();
            FileOutputStream fileOut = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 90, fileOut);
            fileOut.close();
            return file;
        } catch (Exception e) {
            Log.e(ArtRetriever.class.getSimpleName(), "Error saving image to file.", e);
        }
        return null;
    }

    public static Bitmap getArt(final Context context, final String album, final String artist) {
        File imageFile = new File(context.getCacheDir(), getName(context, album, artist));
        if (!imageFile.exists()) {
            String[] urls = getImageUrl(album, artist);
            Bitmap image = null;
            // try all urls
            for (String url : urls) {
                image = getArtFromUrl(url);
                if (image != null) break;
            }
            // if no urls worked then return null
            if (image == null || (imageFile = saveImage(context, image, album, artist)) == null) {
                Log.e(ArtRetriever.class.getSimpleName(), "None of the urls worked " + urls.length + ".");
                return null;
            }
        }
        try {
            return BitmapFactory.decodeStream(new FileInputStream(imageFile));
        }
        catch (Exception e) {
            Log.e(ArtRetriever.class.getSimpleName(), "Error getting artwork from decoding stream.", e);
        }
        return null;
    }

    private static String getName(final Context context, final String album, final String artist) {
        return context.getString(R.string.image_file_start) + album + artist;
    }
}
