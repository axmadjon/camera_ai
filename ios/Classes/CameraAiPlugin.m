#import "CameraAiPlugin.h"
#if __has_include(<camera_ai/camera_ai-Swift.h>)
#import <camera_ai/camera_ai-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "camera_ai-Swift.h"
#endif

@implementation CameraAiPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftCameraAiPlugin registerWithRegistrar:registrar];
}
@end
