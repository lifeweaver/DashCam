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

    static final int MEDIA_TYPE_IMAGE = 1
    static final int MEDIA_TYPE_VIDEO = 2
    static int MAX_FILES_TO_KEEP = 3
    static final String SAVE_FILE = 'DashCam'
    static final String SAVE_FILE_REGEX = ~/^DashCam[_0-9]+\.mp4$/

    def Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type))
    }

    def static File getOutputMediaFile(int type) {
        File mediaStorageDir = MediaStorage()

        if(! mediaStorageDir.exists()) {
            if(! mediaStorageDir.mkdirs()) {
                Log.d("DashCam", "failed to create directory")
                return null
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())
        File mediaFile

        if(type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.path + File.separator + "IMG_" + timeStamp + ".jpg")
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.path + File.separator + SAVE_FILE + "_" + timeStamp + ".mp4")
        }

        return mediaFile
    }

    private static File MediaStorage() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), SAVE_FILE)
    }

    def static void removeVideos() {
        File mediaStorageDir = MediaStorage()
        File[] files = mediaStorageDir.listFiles().findAll { it.name.matches(SAVE_FILE_REGEX) }

        if(files.size() > MAX_FILES_TO_KEEP) {
            def result = files.sort{ File a, File b -> b.lastModified() <=> a.lastModified() }
            Log.d("DashCam", "about to delete: " + result[-1].absolutePath)
            result[-1].delete()
            Log.d("DashCam", "File deleted")
        }
    }
}
