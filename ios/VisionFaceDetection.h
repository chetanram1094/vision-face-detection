
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNVisionFaceDetectionSpec.h"

@interface VisionFaceDetection : NSObject <NativeVisionFaceDetectionSpec>
#else
#import <React/RCTBridgeModule.h>

@interface VisionFaceDetection : NSObject <RCTBridgeModule>
#endif

@end
