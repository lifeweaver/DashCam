package com.stardecimal.dashcam.app;

import android.app.Activity
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import groovy.transform.CompileStatic;

@CompileStatic
public class MainActivity extends Activity {

    Camera mCamera
    Preview mPreview
    MediaRecorder mMediaRecorder
    private boolean isRecording = false


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        def message = findViewById(R.id.message) as TextView
        message.text = 'hello josh'

        // Create an instance of Camera
        mCamera = getCameraInstance()
        def holder = ((SurfaceView) findViewById(R.id.surfaceView)).holder

        // Create our Preview view and set it as the content of our activity
        mPreview = new Preview(this, mCamera, holder)
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview)
        preview.addView(mPreview)


        Button captureButton = (Button) findViewById(R.id.pushBtn)
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isRecording) {
                            // stop recording and release camera
                            mMediaRecorder.stop()
                            releaseMediaRecorder()
                            mCamera.lock()

                            // inform user that recording has stopped
                            setCaptureButtonText("Capture")
                            isRecording = false
                        } else {
                            // init video camera
                            if(prepareVideoRecorder()) {
                                // camera is available and unlocked, mediaRecorder is prepared, now you can start recording
                                mMediaRecorder.start()

                                // inform user that recording has started
                                setCaptureButtonText("Stop")
                                isRecording = true
                            } else {
                                // prepare didn't work release camera
                                releaseMediaRecorder()
                                // inform user
                                setCaptureButtonText("Capture")
                            }
                        }
                    }
                }
        )

        // Take video
//        dispatchTakeVideoIntent()
    }

    @Override
    protected  void onPause() {
        super.onPause()
        releaseMediaRecorder()
        releaseCamera()
    }


    // Not sure if we want to do this or not, may want to keep recording but probably not.
    @Override
    protected void onStop() {
        super.onStop()
        releaseMediaRecorder()
        releaseCamera()
    }

    @Override
    protected void onRestart() {
        super.onRestart()

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
            mCamera.release()
            mCamera = null
        }
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
            Log.d("DashCam", "failed to get instance of camera: " + e.message)
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private boolean prepareVideoRecorder() {
        if(!mCamera) {
            mCamera = getCameraInstance()
        }

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
        mMediaRecorder.setOutputFile(FileUtil.getOutputMediaFile(FileUtil.MEDIA_TYPE_VIDEO).toString())

        //step 5
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface())

        // step 6
        try {
            mMediaRecorder.prepare()
        } catch (IllegalStateException e) {
            Log.d("Dashcam", "IllegalStateException preparing MediaRecorder: " + e.message)
            releaseMediaRecorder()
            return false
        } catch (IOException e) {
            Log.d("DashCam", "IOException preparing MediaRecorder: " + e.message)
        }
        return true
    }

//    private boolean safeCameraOpen(id) {
//        def cameraOpened = false
//
//        try {
//            releaseCameraAndPreview()
//            mCamera = Camera.open(id)
//            cameraOpened = (mCamera != null)
//        } catch (Exception e) {
//            Log.e(getString(R.string.app_name), "failed to open Camera")
//            e.printStackTrace()
//        }
//        return cameraOpened
//    }
//
//    private void releaseCameraAndPreview() {
//        mPreview.setCamera(null)
//        if(mCamera) {
//            mCamera.release()
//            mCamera = null
//        }
//    }
//
//    public void setCamera(Camera camera) {
//        if(mCamera == camera) {return}
//
//        stopPreviewAndFreeCamera()
//
//        mCamera = camera
//
//        if(mCamera) {
//            List<Camera.Size> localSizes = mCamera.parameters.supportedPreviewSizes
//            mSupportedpreviewSizes = localSizes
//            requestLayout()
//
//            try {
//                mCamera.previewDisplay = mHolder
//            } catch (IOException e) {
//                e.printStackTrace()
//            }
//
//            // Important Call startPreview() to start updating the preview surface
//            // Preview must be started before you can take a picture
//            mCamera.startPreview()
//        }
//    }




//    private void dispatchTakeVideoIntent() {
//        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE)
//        if(takeVideoIntent.resolveActivity(packageManager)) {
//            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
//            def videoUri = intent.data
//            def mVideoView = findViewById(R.id.mVideoView) as VideoView
//            mVideoView.setVideoURI(videoUri)
//        }
//    }


}
