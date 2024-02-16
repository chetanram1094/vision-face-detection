package com.visionfacedetection

import android.app.Activity
import android.app.Application
import android.content.Context.CAMERA_SERVICE
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.SparseIntArray
import com.facebook.react.ReactActivity
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import com.mrousavy.camera.frameprocessor.FrameProcessorPluginRegistry
import com.visionfacedetection.visionframeprocessor.VisionFrameProcessorPlugin
import com.visionfacedetection.visionframeprocessor.VisionFrameProcessorPlugin.Companion.initContext

class VisionFrameProcessorPluginPackage : ReactPackage {
  companion object {
    init {
      FrameProcessorPluginRegistry.addFrameProcessorPlugin("scanFaces") { proxy, options ->
        VisionFrameProcessorPlugin(proxy, options)
      }
    }
  }


  override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
    return emptyList()
  }

  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
    initContext(reactContext)
    return emptyList()
  }
}
