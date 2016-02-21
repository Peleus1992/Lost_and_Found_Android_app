package edu.gatech.wguo64.lostandfoundandroidapp.utility;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.renderscript.ScriptGroup;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import edu.gatech.wguo64.lostandfoundandroidapp.constants.Credentials;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;

/**
 * Created by guoweidong on 2/18/16.
 */
public class ImageUploader {
    public static class ImageInfo {
        public String imageKey;
        public String imageURL;
    }

    private static final String LINE_FEED = "\r\n";


    public static ImageInfo upload(Drawable drawable) {
        try {
            // Transform the Image to HttpEntity.
            byte[] imageBytes = ImageConvertor.drawableToByteArray(drawable);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            HttpEntity httpEntity =  builder.addBinaryBody("image", imageBytes
                    , ContentType.create("image/jpeg"), "image").build();

            // Get URL where the image will be uploaded
            String url = Api.getClient().foundReport().newImageURL().execute().getStringResponse();
            HttpPost httpPost = new HttpPost(url);
            // Execute the http post
            httpPost.setEntity(httpEntity);

            // Process httpResponse
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(httpPost);
            InputStream in = response.getEntity().getContent();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in));
            String line;
            StringBuffer sb = new StringBuffer();
            while((line = rd.readLine()) != null) {
                sb.append(line);
                sb.append('\r');
            }
            rd.close();
            Log.d("fsfs", sb.toString());
            JSONObject jsonObject = new JSONObject(sb.toString());
            ImageInfo imageInfo = new ImageInfo();
            imageInfo.imageKey = (String)jsonObject.get("imageKey");
            imageInfo.imageURL = (String)jsonObject.get("imageURL");
            return imageInfo;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
