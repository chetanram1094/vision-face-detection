import * as React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import {
  Camera,
  useCameraDevice,
  useCameraFormat,
  useFrameProcessor,
} from 'react-native-vision-camera';
import { scanFaces } from 'vision-face-detection';
import { useIsForeground } from './useIsForeground';
import { usePreferredCameraDevice } from './usePreferredCameraDevice';
import 'react-native-worklets-core';
export default function App() {
  const [preferredDevice] = usePreferredCameraDevice();
  let device = useCameraDevice('front');

  if (preferredDevice != null && preferredDevice.position === 'front') {
    // override default device with the one selected by the user in settings
    device = preferredDevice;
  }

  const camera = React.useRef<Camera>(null);
  const [targetFps] = React.useState(30);

  const format = useCameraFormat(device, [
    { pixelFormat: 'yuv' },
    { fps: targetFps },
  ]);

  const isForeground = useIsForeground();
  const isActive = isForeground;
  // const codeScanner = useCodeScanner({
  //   codeTypes: ['qr', 'ean-13'],
  //   onCodeScanned: codes => {
  //     if (codes.length > 0) {
  //       console.log(`Scanned ${codes[0].value} codes!`);
  //     }
  //   },
  // });

  const frameProcessor = useFrameProcessor((frame) => {
    'worklet';

    const scannedFaces: any = scanFaces(frame, {
      cameraId: device?.id,
      cameraType: device?.position === 'back' ? 'back' : 'front',
    });
    if (scannedFaces) {
      console.log('scan', JSON.stringify(scannedFaces));
    }
  }, []);
  if (device == null)
    return (
      <>
        <Text>{'error'}</Text>
      </>
    );
  return (
    <View style={styles.mainView}>
      <Camera
        ref={camera}
        style={styles.camera}
        device={device}
        photo={true}
        isActive={isActive}
        orientation={'portrait'}
        pixelFormat="yuv"
        format={format}
        fps={targetFps}
        photoHdr={format?.supportsPhotoHdr && false}
        videoHdr={format?.supportsVideoHdr && false}
        exposure={0}
        frameProcessor={frameProcessor}
        // codeScanner={codeScanner}
      />
    </View>
  );
}
const styles = StyleSheet.create({
  camera: { width: '100%', height: '100%' },
  mainView: { flex: 1, backgroundColor: 'white' },
});
