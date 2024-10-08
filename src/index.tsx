import { VisionCameraProxy, type Frame } from 'react-native-vision-camera';

const plugin = VisionCameraProxy.initFrameProcessorPlugin('scanFaces', {
  cameraId: 1,
  cameraType: 'front',
});
declare type Point = {
  x: number;
  y: number;
};
export interface Face {
  leftEyeOpenProbability: number;
  rollAngle: number;
  pitchAngle: number;
  yawAngle: number;
  rightEyeOpenProbability: number;
  smilingProbability: number;
  bounds: {
    y: number;
    x: number;
    height: number;
    width: number;
  };
  contours: {
    FACE: Point[];
    NOSE_BOTTOM: Point[];
    LOWER_LIP_TOP: Point[];
    RIGHT_EYEBROW_BOTTOM: Point[];
    LOWER_LIP_BOTTOM: Point[];
    NOSE_BRIDGE: Point[];
    RIGHT_CHEEK: Point[];
    RIGHT_EYEBROW_TOP: Point[];
    LEFT_EYEBROW_TOP: Point[];
    UPPER_LIP_BOTTOM: Point[];
    LEFT_EYEBROW_BOTTOM: Point[];
    UPPER_LIP_TOP: Point[];
    LEFT_EYE: Point[];
    RIGHT_EYE: Point[];
    LEFT_CHEEK: Point[];
  };
}
export function scanFaces(
  frame: Frame,
  data: {
    cameraId: string | undefined;
    cameraType: 'back' | 'front' | undefined;
  }
) {
  'worklet';

  if (plugin == null) {
    throw new Error('Failed to load Frame Processor Plugin!');
  }
  return plugin.call(frame, data) as string[];
}
