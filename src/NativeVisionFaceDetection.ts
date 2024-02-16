import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';
import { type Frame } from 'react-native-vision-camera';
export interface Spec extends TurboModule {
  scanFaces(frame: Frame): Promise<any>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('VisionFaceDetection');
