#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint camera_ai.podspec' to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'camera_ai'
  s.version          = '0.0.3'
  s.summary          = 'Camera AI'
  s.description      = <<-DESC
Camera AI
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.public_header_files = 'Classes/**/*.h'
  s.source_files = 'Classes/**/*', 'Classes/FaceDetection/*'
  
  s.dependency 'Flutter'
  s.dependency 'GoogleMLKit/FaceDetection'
  s.dependency 'OpenCV2'
  
  s.static_framework = true
  s.platform = :ios, '11.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'
  
  s.vendored_frameworks = 'ncnn.framework', 'openmp.framework'
  
  s.library = 'c++'
end
