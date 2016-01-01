package edu.gatech.wguo64.lostandfoundandroidapp.camera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by guoweidong on 10/27/15.
 */
public class CameraHelper {
    public static Activity mAct;
    public static File dir;

    /**
     * Must be called before using other methods
     *
     * @param mActivity
     */
    public static void init(Activity mActivity) {
        mAct = mActivity;
        dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getPackageName());
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    /**
     * @param requestCode: the requestCode for onActivityResult
     * @return
     */
    public static String openCameraForImage(int requestCode) {//only support jpg and jpeg
        File imageFile = genOutputMediaFile(MediaType.MEDIA_TYPE_IMAGE);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        mAct.startActivityForResult(intent, requestCode);
//        Log.i("myinfo", imageFile.getPath());
        return imageFile.getName();
    }

    /**
     * @param requestCode: the requestCode for onActivityResult
     * @return
     */
    public static String openCameraForVideo(int requestCode) {
        File videoFile = genOutputMediaFile(MediaType.MEDIA_TYPE_VIDEO);
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoFile));
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        mAct.startActivityForResult(intent, requestCode);
        return videoFile.getName();
    }

    /**
     * @param mediaName: only the mediaFile name without path
     * @return
     */
    public static File getMediaFile(String mediaName) {
        File mediaFile = new File(dir.getPath(), mediaName);
        if (mediaFile.exists()) {
            return mediaFile;
        }
        return null;
    }

    private static String getPackageName() {
        return mAct.getApplicationContext().getPackageName();
    }

    private static File genOutputMediaFile(MediaType mediaType) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = null;
        if (mediaType == MediaType.MEDIA_TYPE_IMAGE) {
            mediaFile = new File(dir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else if (mediaType == MediaType.MEDIA_TYPE_VIDEO) {
            mediaFile = new File(dir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        }
        return mediaFile;
    }

    private static enum MediaType {
        MEDIA_TYPE_IMAGE,
        MEDIA_TYPE_VIDEO
    }
}
