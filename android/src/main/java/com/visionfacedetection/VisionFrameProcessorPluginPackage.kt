package com.visionfacedetection

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import com.mrousavy.camera.frameprocessors.FrameProcessorPluginRegistry
import com.visionfacedetection.visionframeprocessor.VisionFrameProcessorPlugin

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
    VisionFrameProcessorPlugin.initContext(reactContext)
    return emptyList()
  }
}
