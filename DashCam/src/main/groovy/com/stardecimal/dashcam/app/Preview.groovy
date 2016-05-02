package com.stardecimal.dashcam.app

import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.Display
import android.view.Surface
import android.view.SurfaceHolder
import android.view.ViewGroup
import android.view.WindowManager
import groovy.transform.CompileStatic

/**
 * Created by LifeWeaver on 12/10/2015.
 */
@CompileStatic
class Preview extends ViewGroup implements SurfaceHolder.Callback {
    SurfaceHolder mHolder
    Camera mCamera

    Preview(Context context, Camera camera, SurfaceHolder holder) {
        super(context)
        mCamera = camera

        // install a surfaceHolder.callback so we get notified when the underlying surface is created and destroyeddef

        mHolder = holder
        mHolder.addCallback(this)

        // deprecated setting, but required on android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    def SurfaceHolder getHolder() {
//        return ((SurfaceView) findViewById(R.id.surfaceView)).holder
        return mHolder
    }

    @Override
    void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.previewDisplay = holder
            mCamera.startPreview()
        } catch (IOException e) {
            Log.d("DashCam", "Error setting camera preview: " + e.message)
        }
    }

    @Override
    void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity
    }

    @Override
    void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if(!mHolder.surface) {
            Log.d("DashCam", 'preview surface does not exist')
            return
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview()
        } catch ( Exception e) {
            Log.d("DashCam", "Tried to stop a non-existent preview: ${e}")
        }

        // set preview size and make any resize, rotate or reformatting changes here
        Display display = ((WindowManager)context.getSystemService(context.WINDOW_SERVICE)).defaultDisplay

        if(display.getRotation() == Surface.ROTATION_0) {
            mCamera.setDisplayOrientation(90)
        }else if(display.getRotation() == Surface.ROTATION_270) {
            mCamera.setDisplayOrientation(180)
        }

        // Start preview with new settings
        try {
            mCamera.previewDisplay = mHolder
            mCamera.startPreview()
        } catch (Exception e) {
            Log.d("DashCam", "Error starting camera preview: " + e.message)
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super
    }
}
