package com.stardecimal.dashcam.app;

import android.app.Activity
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import groovy.transform.CompileStatic;

@CompileStatic
public class MainActivity extends Activity {

    Camera mCamera
    Preview mPreview
    MediaRecorder mMediaRecorder
    FrameLayout framePreview
    String outputFile
    private boolean isRecording = false
    static String TAG = "DashCam"


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        KeepScreenOn()

        def message = findViewById(R.id.message) as TextView
        message.text = 'hello josh'

        InitializeCameraAndPreview()

        Button captureButton = (Button) findViewById(R.id.pushBtn)
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isRecording) {
                            StopRecording()
                            Toast.makeText(view.context, "File: ${outputFile}", Toast.LENGTH_SHORT).show()
                        } else {
                            // init video camera
                            if(prepareVideoRecorder()) {
                                StartRecording()
                            } else {
                                // prepare didn't work release camera
                                releaseMediaRecorder()
                            }
                        }
                    }
                }
        )
    }

    def KeepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    def StartRecording() {
        // camera is available and unlocked, mediaRecorder is prepared, now you can start recording
        mMediaRecorder.start()

        // inform user that recording has started
        setCaptureButtonText("Stop")
        isRecording = true
    }

    def StopRecording() {

        // stop recording and release camera
        if(mMediaRecorder) {
            try {
                mMediaRecorder.stop()
            } catch(IllegalStateException e) {
                new File(outputFile).delete()
                Log.d(TAG, "No video record so deleting empty outputFile")
            }
            releaseMediaRecorder()
        }

        // inform user that recording has stopped
        setCaptureButtonText("Start")
        isRecording = false
    }

    def InitializeCameraAndPreview() {
        // Create an instance of Camera
        if(!mCamera) {
            mCamera = getCameraInstance()
        }

        // Create our Preview view and set it as the content of our activity
        if(!mPreview) {
            def holder = ((SurfaceView) findViewById(R.id.surfaceView)).holder
            mPreview = new Preview(this, mCamera, holder)
            framePreview = (FrameLayout) findViewById(R.id.cameraPreview)
            framePreview.addView(mPreview)
        }

    }

    @Override
    protected void onResume() {
        super.onResume()
        InitializeCameraAndPreview()
        Log.d(TAG, "Resumed Josh3: mCamera, mPreview: " + mCamera + ", " + mPreview)
    }

    @Override
    protected  void onPause() {
        StopRecording()

        releaseCamera()
        releasePreview()
        super.onPause()
        Log.d(TAG, "Paused Josh1")
    }

    @Override
    protected void onStop() {
        releaseCamera()
        releasePreview()
        super.onStop()
        Log.d(TAG, "Stopped Josh2")

    }

    @Override
    protected void onDestroy() {
        StopRecording()
        releaseCamera()
        releasePreview()
        super.onDestroy()
        Log.d(TAG, "onDestroy Josh5")
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder) {
            mMediaRecorder.reset()
            mMediaRecorder.release()
            mMediaRecorder = null
            mCamera.lock()
        }
    }

    private void releaseCamera() {
        if (mCamera) {
            mCamera.stopPreview()
            mCamera.release()
            mCamera = null
            mPreview.holder.removeCallback(mPreview)
        }
    }

    def releasePreview() {
        framePreview.removeView(mPreview)
        mPreview = null
    }

    private void setCaptureButtonText(String buttonText) {
        Button captureButton = findViewById(R.id.pushBtn) as Button
        captureButton.text = buttonText
    }

/** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            Log.d(TAG, "failed to get instance of camera: " + e.message)
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private boolean prepareVideoRecorder() {
        Log.d(TAG, "mCamera1: " + mCamera)
        if(!mCamera) {
            mCamera = getCameraInstance()
        }
        Log.d(TAG, "mCamera2: " + mCamera)

        mMediaRecorder = new MediaRecorder()

        // step 1 unlock and set camera to mediarecorder
        mCamera.unlock()
        mMediaRecorder.setCamera(mCamera)

        //step 2
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA)

        // step 3
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))

        // step 4
        outputFile = FileUtil.getOutputMediaFile(FileUtil.MEDIA_TYPE_VIDEO).toString()
        mMediaRecorder.setOutputFile(outputFile)

        //step 5
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface())

        // step 6
        try {
            mMediaRecorder.prepare()
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.message)
            releaseMediaRecorder()
            return false
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.message)
        }
        return true
    }
}
