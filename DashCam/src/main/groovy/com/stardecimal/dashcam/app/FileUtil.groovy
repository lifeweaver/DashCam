package com.stardecimal.dashcam.app

import android.net.Uri
import android.os.Environment
import android.util.Log
import groovy.transform.CompileStatic

import java.text.SimpleDateFormat

/**
 * Created by LifeWeaver on 12/10/2015.
 */

@CompileStatic
class FileUtil {

    public static final int MEDIA_TYPE_IMAGE = 1
    public static final int MEDIA_TYPE_VIDEO = 2

    def Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type))
    }

    def static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DashCam")

        if(! mediaStorageDir.exists()) {
            if(! mediaStorageDir.mkdirs()) {
                Log.d("DashCam", "failed to create directory")
                return null
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())
        File mediaFile = null
        if(type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.path + File.separator + "IMG_" + timeStamp + ".jpg")
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.path + File.separator + "VID_" + timeStamp + ".mp4")
        }

        return mediaFile
    }

}
