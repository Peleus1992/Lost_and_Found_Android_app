package edu.gatech.wguo64.lostandfoundandroidapp.utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.ByteArrayOutputStream;

import biz.source_code.base64Coder.Base64Coder;

/**
 * Created by guoweidong on 11/24/15.
 */
public class ImageConvertor {
    public final static int IMAGE_WIDTH = 640;
    public final static int IMAGE_HEIGHT = 480;
    public final static int ICON_WIDTH = 128;
    public final static int ICON_HEIGHT = 96;
    public static String drawableToString(Drawable drawable) {
        byte[] data = drawableToByteArray(drawable);
        StringBuffer out = new StringBuffer();
        out.append(Base64Coder.encode(data, 0, data.length));
        Log.i("myinfo", "" + data.length);
        return out.toString();
    }
    public static byte[] drawableToByteArray(Drawable drawable) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap resized = Bitmap.createScaledBitmap(((BitmapDrawable)drawable).getBitmap(), IMAGE_WIDTH, IMAGE_HEIGHT, true);
        resized.compress(Bitmap.CompressFormat.JPEG, 20, stream);
        return stream.toByteArray();
    }
    public static Drawable stringToDrawable(String image, boolean icon) {
        byte[] data = Base64Coder.decode(image);
        return byteArrayToDrawable(data, icon);
    }
    public static Drawable byteArrayToDrawable(byte[] data, boolean icon) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        if(icon) {
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, ICON_WIDTH, ICON_HEIGHT, true);
            return new BitmapDrawable(resized);
        } else {
            return new BitmapDrawable(bitmap);
        }
    }
}
