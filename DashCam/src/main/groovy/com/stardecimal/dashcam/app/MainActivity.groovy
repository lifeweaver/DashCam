package com.stardecimal.dashcam.app;

import android.app.Activity
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import groovy.transform.CompileStatic;

@CompileStatic
public class MainActivity extends Activity {

    // TODO: add option to setting to disable video recording sound if possible, needs root?
    // TODO: start using new camera type
    Camera mCamera
    Preview mPreview
    MediaRecorder mMediaRecorder
    FrameLayout mFramePreview
    String outputFile
    private boolean isRecording = false
    static String TAG = "DashCam"
    static int MAX_VIDEO_DURATION_IN_MILLISECONDS = 300000
    private Timer timerTick = null
    private Handler timerHandler

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, 'oncreate')
        initializeTimerHandler()
    }

    @Override
    protected void onResume() {
        super.onResume()
        setContentView(R.layout.activity_main)
        initializeTimerHandler()
        initializeCameraAndPreview()
        Log.d(TAG, "Resumed Josh3: mCamera, mPreview: " + mCamera + ", " + mPreview)
        keepScreenOn()
    }

    @Override
    protected  void onPause() {
        stopRecording()
        releaseCamera()
        releasePreview()
        stopKeepingScreenOn()
        super.onPause()
        Log.d(TAG, "onPause")
    }

    @Override
    protected void onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    @Override
    protected void onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    void initializeTimerHandler() {
        timerHandler = new Handler()
    }

    void recordingButton(View view) {
        if(isRecording) {
            stopRecording()
            Toast.makeText(view.context, "File: ${outputFile}", Toast.LENGTH_SHORT).show()
        } else {
            if(prepareVideoRecorder()) {
                startRecording()
            } else {
                releaseMediaRecorder()
            }
        }
    }

    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    def initializeCameraAndPreview() {
        if(!mCamera) {
            mCamera = getCameraInstance()
        }

        // Create the Preview view and set it as the content of this activity
        if(!mPreview) {
            def holder = ((SurfaceView) findViewById(R.id.surfaceView)).holder
            mPreview = new Preview(this, mCamera, holder)
            mFramePreview = (FrameLayout) findViewById(R.id.cameraPreview)
            mFramePreview.addView(mPreview)
        }

    }

    void startTimer() {
        timerTick = new Timer((EditText) findViewById(R.id.videoTime), timerHandler)
        timerHandler.postDelayed(timerTick, 1000)
    }

    void stopTimer(Timer timer) {
        timerTick.reset()

        if(timer)
            timerHandler.removeCallbacks(timer)
    }

    def stopRecording() {
        if(isRecording) {
            if(mMediaRecorder) {
                try {
                    mMediaRecorder.stop()
                } catch(IllegalStateException e) {
                    new File(outputFile).delete()
                    Log.d(TAG, "No video record so deleting empty outputFile: ${e}")
                }
                releaseMediaRecorder()
            }

            // inform user that recording has stopped
            toggleButton()

            isRecording = false
            stopTimer(timerTick)
        }
    }

    def startRecording() {
        startTimer()

        // camera is available and unlocked, mediaRecorder is prepared, now you can start recording
        mMediaRecorder.start()

        // inform user that recording has started
        toggleButton()
        isRecording = true
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
        }
    }

    def releasePreview() {
        mFramePreview.removeAllViews()
        mPreview = null
    }

    private void toggleButton() {
        Button captureButton = findViewById(R.id.start) as Button
        if(captureButton.text == 'Start') {
            captureButton.text = 'Stop'
        } else {
            captureButton.text = 'Start'
        }
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
        if(!mCamera) {
            mCamera = getCameraInstance()
        }

        mMediaRecorder = new MediaRecorder()
        mMediaRecorder.onInfoListener = new MediaRecorder.OnInfoListener() {
            @Override
            void onInfo(MediaRecorder mr, int what, int extra) {
                if(what == mr.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecording()
                    prepareVideoRecorder()
                    startRecording()
                }
            }
        }

        // step 1 unlock and set camera to mediarecorder
        mCamera.unlock()
        mMediaRecorder.camera = mCamera

        //step 2
        mMediaRecorder.audioSource = MediaRecorder.AudioSource.CAMCORDER
        mMediaRecorder.videoSource = MediaRecorder.VideoSource.CAMERA

        // step 3
        mMediaRecorder.profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)

        // step 4
        outputFile = FileUtil.getOutputMediaFile(FileUtil.MEDIA_TYPE_VIDEO).toString()
        mMediaRecorder.setOutputFile(outputFile)
        mMediaRecorder.maxDuration = MAX_VIDEO_DURATION_IN_MILLISECONDS
        FileUtil.removeVideos()

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