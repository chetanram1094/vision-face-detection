package com.visionfacedetection.visionframeprocessor

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Image
import android.util.Log
import android.util.Pair
import android.util.SparseIntArray
import com.facebook.react.ReactActivity
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.odml.image.MediaMlImageBuilder
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.mrousavy.camera.frameprocessors.Frame
import com.mrousavy.camera.frameprocessors.FrameProcessorPlugin
import com.mrousavy.camera.frameprocessors.VisionCameraProxy


class VisionFrameProcessorPlugin(proxy: VisionCameraProxy, options: Map<String, Any>?): FrameProcessorPlugin() {

  companion object {
    private var reactContext: ReactApplicationContext? = null
    fun initContext(reactContext: ReactApplicationContext) {
      this.reactContext = reactContext


    }

  }
  var faceOptions = FaceDetectorOptions.Builder()
    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
    .enableTracking()
    .setMinFaceSize(0.1f).build()

  var faceDetector: FaceDetector = FaceDetection.getClient(faceOptions)
  private fun processBoundingBox(boundingBox: Rect): WritableMap? {
    val bounds = Arguments.createMap()

    // Calculate offset (we need to center the overlay on the target)
    val offsetX: Double =
      (boundingBox.exactCenterX() - Math.ceil(boundingBox.width().toDouble())) / 2.0f
    val offsetY: Double =
      (boundingBox.exactCenterY() - Math.ceil(boundingBox.height().toDouble())) / 2.0f
    val x = boundingBox.right + offsetX
    val y = boundingBox.top + offsetY
    bounds.putDouble("x", boundingBox.centerX() + (boundingBox.centerX() - x))
    bounds.putDouble("y", boundingBox.centerY() + (y - boundingBox.centerY()))
    bounds.putDouble("width", boundingBox.width().toDouble())
    bounds.putDouble("height", boundingBox.height().toDouble())
    bounds.putDouble("boundingCenterX", boundingBox.centerX().toDouble())
    bounds.putDouble("boundingCenterY", boundingBox.centerY().toDouble())
    bounds.putDouble("boundingExactCenterX", boundingBox.exactCenterX().toDouble())
    bounds.putDouble("boundingExactCenterY", boundingBox.exactCenterY().toDouble())
    return bounds
  }

  private fun processFaceContours(face: Face): WritableMap? {
    // All faceContours
    val faceContoursTypes = intArrayOf(
      FaceContour.FACE,
      FaceContour.LEFT_EYEBROW_TOP,
      FaceContour.LEFT_EYEBROW_BOTTOM,
      FaceContour.RIGHT_EYEBROW_TOP,
      FaceContour.RIGHT_EYEBROW_BOTTOM,
      FaceContour.LEFT_EYE,
      FaceContour.RIGHT_EYE,
      FaceContour.UPPER_LIP_TOP,
      FaceContour.UPPER_LIP_BOTTOM,
      FaceContour.LOWER_LIP_TOP,
      FaceContour.LOWER_LIP_BOTTOM,
      FaceContour.NOSE_BRIDGE,
      FaceContour.NOSE_BOTTOM,
      FaceContour.LEFT_CHEEK,
      FaceContour.RIGHT_CHEEK
    )
    val faceContoursTypesStrings = arrayOf(
      "FACE",
      "LEFT_EYEBROW_TOP",
      "LEFT_EYEBROW_BOTTOM",
      "RIGHT_EYEBROW_TOP",
      "RIGHT_EYEBROW_BOTTOM",
      "LEFT_EYE",
      "RIGHT_EYE",
      "UPPER_LIP_TOP",
      "UPPER_LIP_BOTTOM",
      "LOWER_LIP_TOP",
      "LOWER_LIP_BOTTOM",
      "NOSE_BRIDGE",
      "NOSE_BOTTOM",
      "LEFT_CHEEK",
      "RIGHT_CHEEK"
    )
    val faceContoursTypesMap: WritableMap = WritableNativeMap()
    for (i in faceContoursTypesStrings.indices) {
      val contour: FaceContour = face.getContour(faceContoursTypes[i])!!
      val points = contour.points
      val pointsArray = WritableNativeArray()
      for (j in points.indices) {
        val currentPointsMap: WritableMap = WritableNativeMap()
        currentPointsMap.putDouble("x", points[j].x.toDouble())
        currentPointsMap.putDouble("y", points[j].y.toDouble())
        pointsArray.pushMap(currentPointsMap)
      }
      faceContoursTypesMap.putArray(
        faceContoursTypesStrings[contour.faceContourType - 1],
        pointsArray
      )
    }
    return faceContoursTypesMap
  }

  fun getMatrixFromImage(image: Image?): Matrix? {
    val matrix = Matrix()

    //    matrix.postRotate(-90);
//    matrix.postRotate(-90)

    // Mirror the image along the X or Y axis.
//    matrix.postScale(-1.0f, 1.0f)

    // Apply any transformations you need, for example, rotation
    // matrix.postRotate(90); // Rotate the image by 90 degrees
    return matrix
  }

  fun imageToBitmap(image: Image?): Bitmap? {
    if (image == null) {
      return null
    }
    val planes = image.planes
    if (planes.size == 0) {
      return null
    }
    val buffer = planes[0].buffer
    val pixelStride = planes[0].pixelStride
    val rowStride = planes[0].rowStride
    val rowPadding = rowStride - pixelStride * image.width
    val bitmap = Bitmap.createBitmap(
      image.width + rowPadding / pixelStride,
      image.height,
      Bitmap.Config.ARGB_8888
    )
    buffer.rewind()
    bitmap.copyPixelsFromBuffer(buffer)
    return bitmap
  }

  override fun callback(frame: Frame, arguments: Map<String, Any>?): Any? {

    // code goes here
    var cameraType = "front";
    if (arguments != null && arguments.isNotEmpty()) {

      cameraType = arguments?.get("cameraType").toString()
    }
    val mediaImage = frame.image

    if (mediaImage != null) {
//      val matrix = getMatrixFromImage(mediaImage)
      var rotationCompensation = 0
      if (reactContext != null) {
        val ORIENTATIONS = SparseIntArray()
        val deviceRotation = reactContext!!.currentActivity!!.windowManager.defaultDisplay.rotation
        rotationCompensation = ORIENTATIONS.get(deviceRotation)
        val cameraManager = reactContext!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val sensorOrientation =
          cameraManager.getCameraCharacteristics(arguments?.get("cameraId").toString())
            .get(CameraCharacteristics.SENSOR_ORIENTATION)!!
        if (cameraType == "back") {
          rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360
        } else {
          rotationCompensation = (sensorOrientation + rotationCompensation) % 360
        }
      }

      val image = InputImage.fromMediaImage(mediaImage, rotationCompensation)
//      val mlImage = MediaMlImageBuilder(mediaImage)
//        .setRotation(rotationCompensation)
//        .build()

      val task: Task<List<Face>> = faceDetector.process(image)

      try {
        val faces = Tasks.await<List<Face>>(task)

        val array: WritableArray = WritableNativeArray()

        for (face in faces) {
          val map: WritableMap = WritableNativeMap()

          map.putDouble(
            "rollAngle",
            face.headEulerAngleZ.toDouble()
          ) // Head is rotated to the left rotZ degrees
          map.putDouble(
            "pitchAngle",
            face.headEulerAngleX.toDouble()
          ) // Head is rotated to the right rotX degrees
          map.putDouble(
            "yawAngle",
            face.headEulerAngleY.toDouble()
          ) // Head is tilted sideways rotY degrees
          map.putDouble(
            "leftEyeOpenProbability",
            (if (face.leftEyeOpenProbability != null) face.leftEyeOpenProbability else 0)!!.toDouble()
          )
          map.putDouble(
            "rightEyeOpenProbability",
            (if (face.rightEyeOpenProbability != null) face.rightEyeOpenProbability else 0)!!.toDouble()
          )
          map.putDouble(
            "smilingProbability",
            (if (face.smilingProbability != null) face.smilingProbability else 0)!!.toDouble()
          )
          val contours = processFaceContours(face)
          val bounds = processBoundingBox(face.boundingBox)
          map.putMap("bounds", bounds)
          map.putMap("contours", contours)
          array.pushMap(map)
        }
        return array.toArrayList()
      } catch (e: Exception) {

        return e.message
      }
    }
    var list = ArrayList<Any>()
    list.add("no image")
    return list
  }
}
