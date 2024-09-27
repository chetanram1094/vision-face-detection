import * as React from 'react';
import {View, Text, StyleSheet, TouchableOpacity, Image} from 'react-native';
import {
  Camera,
  useCameraDevice,
  useCameraFormat,
  useFrameProcessor,
  type PhotoFile,
} from 'react-native-vision-camera';
import {scanFaces} from 'vision-face-detection';
import {useIsForeground} from './useIsForeground';
import 'react-native-worklets-core';
export default function App() {
  const device = useCameraDevice('front');

  const camera = React.useRef<Camera>(null);
  const [targetFps] = React.useState(30);

  const format = useCameraFormat(device, [
    {
      fps: targetFps,
    },
  ]);
  const [uri, setURI] = React.useState('');

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

  const frameProcessor = useFrameProcessor(frame => {
    'worklet';

    const scannedFaces: any = scanFaces(frame, {
      cameraId: device?.id,
      cameraType: device?.position === 'back' ? 'back' : 'front',
    });
    if (scannedFaces) {
      console.log('scan', JSON.stringify(scannedFaces));
    }
  }, []);
  if (device == null) {
    return (
      <>
        <Text>{'error'}</Text>
      </>
    );
  }

  const takePhoto = () => {
    camera.current
      ?.takePhoto({
        enableShutterSound: true,
      })
      .then(async (photo: PhotoFile) => {
        console.log('photo', photo);
        setURI('file://' + photo.path);
      })
      .catch(e => {
        console.log('e', e);
      });
  };
  // const codeScanner = useCodeScanner({
  //   codeTypes: ['qr', 'ean-13'],
  //   onCodeScanned: codes => {
  //     if (codes && codes.length > 0) {
  //       console.log('Scanned codes!', codes);
  //     }
  //   },
  // });
  return (
    <View style={styles.mainView}>
      <Camera
        ref={camera}
        style={styles.camera}
        device={device}
        photo={true}
        video={true}
        isActive={isActive}
        pixelFormat="yuv"
        format={format}
        preview={true}
        fps={targetFps}
        enableDepthData
        isMirrored={false}
        photoHdr={format?.supportsPhotoHdr && false}
        videoHdr={format?.supportsVideoHdr && false}
        exposure={0}
        frameProcessor={frameProcessor}
        // codeScanner={codeScanner}
      />
      <View style={styles.takeView}>
        <Image source={{uri}} width={100} height={100} />
        <TouchableOpacity style={styles.takePhoto} onPress={takePhoto}>
          <Text>{'Take Photo'}</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}
const styles = StyleSheet.create({
  camera: {width: '100%', height: '100%'},
  mainView: {flex: 1, backgroundColor: 'white'},
  takeView: {
    position: 'absolute',
    bottom: 0,
    alignSelf: 'center',
    padding: 10,
  },
  takePhoto: {backgroundColor: 'red', padding: 10},
});
