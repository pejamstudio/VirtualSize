package com.example.virtualsize

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.se.omapi.Session
import android.widget.Toast
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.lang.Exception
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), Scene.OnUpdateListener {
    override fun onUpdate(p0: FrameTime?) {
        val frame = arView.arFrame
        val updateAugmentedImage = frame?.getUpdatedTrackables(AugmentedImage::class.java)
        for (image in updateAugmentedImage!!) {
            if(image.trackingState == TrackingState.TRACKING){
                if(image.name.equals("iphone")){
                    val node = MyARNode(this, R.raw.iphone)
                    node.image = image
                    arView.scene.addChild(node)
                }
            }
        }
    }

    lateinit var arView: ArSceneView
    private var session: com.google.ar.core.Session? = null
    private var configureSession: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // view
        arView = findViewById(R.id.arView) as ArSceneView

        Dexter.withActivity(this)
            .withPermission(android.Manifest.permission.CAMERA)
            .withListener(object : PermissionListener{
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    setupSession()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(applicationContext, "Permission need to display camera", Toast.LENGTH_SHORT).show()
                }

            }).check()

        initSceneView()
    }

    private fun initSceneView() {
        arView.scene.addOnUpdateListener(this)
    }

    private fun setupSession() {
        if(session ==null){
            try {
                session = com.google.ar.core.Session(this)
            } catch (e: UnavailableArcoreNotInstalledException) {
                e.printStackTrace()
            } catch (e: UnavailableApkTooOldException) {
                e.printStackTrace()
            } catch (e: UnavailableSdkTooOldException) {
                e.printStackTrace()
            } catch (e: UnavailableDeviceNotCompatibleException) {
                e.printStackTrace()
            }
            configureSession = true
        }

        if(configureSession){
            configsession()
            configureSession = false
            arView.setupSession(session)
        }

        try {
            session?.resume()
            arView.resume()
        } catch (e: CameraNotAvailableException){
            e.printStackTrace()
            session = null
            return
        }
    }

    private fun configsession() {
        val config = Config(session)
        if(!buildDatabase(config)){
            Toast.makeText(applicationContext, "Database Error", Toast.LENGTH_SHORT).show()
        }
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE)
        session?.configure(config)
    }

    private fun buildDatabase(config: Config): Boolean {
        val bitmap = loadImage()
        if(bitmap == null){
            return false
        }
        val augmentedImageDatabase = AugmentedImageDatabase(session)
        augmentedImageDatabase.addImage("iphone", bitmap)
        config.setAugmentedImageDatabase(augmentedImageDatabase)
        config.focusMode = Config.FocusMode.FIXED
        return true
    }

    private fun loadImage(): Bitmap? {
        try {
            val it = assets.open("iphone.png")
            return BitmapFactory.decodeStream(it)
        }catch (e: Exception){
            e.printStackTrace()
        }
        return null
    }

    override fun onResume() {
        super.onResume()
        Dexter.withActivity(this)
            .withPermission(android.Manifest.permission.CAMERA)
            .withListener(object : PermissionListener{
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    setupSession()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(applicationContext, "Permission need to display camera", Toast.LENGTH_SHORT).show()
                }

            }).check()
    }

    override fun onPause() {
        super.onPause()
        if(session != null){
            arView.pause()
            session!!.pause()
        }
    }
}
