//
//  FaceDetector.swift
//  Runner
//
//  Created by MacBook Air GWS on 6/20/20.
//  Copyright © 2020 The Chromium Authors. All rights reserved.
//
import MLKitFaceDetection
import AVFoundation
import MLKitVision

@objc
public class MyFaceDetector: NSObject {
    
    private let faceRecognitionQueue = DispatchQueue(label: "Recognition",
                                                     qos: .background, attributes: .concurrent,
                                                     autoreleaseFrequency: .workItem, target: nil)
    private var faces = [_Face]()
    
    private let faceDetector: FaceDetector
    private var visionImage: VisionImage?
    private var analyzeMaxFace = false
    private var onlyBase64 = false
    
    private var mCurrentFrame: UIImage?
    private var isDetection = false
    private var isRecognition = false
    
    public override init() {
        isDetection = false;
        let options = FaceDetectorOptions()
        options.performanceMode =  .accurate
        options.landmarkMode = .none
        options.classificationMode = .all
        options.contourMode = .none
        options.isTrackingEnabled = true
        faceDetector = FaceDetector.faceDetector(options: options)
    }
    
    @objc public func getResultToString() -> NSString {
        var result = "";
        var append = false;
        for face in faces {
            if append { result += "#" }
            result += face.toString();
            append = true;
        }
        return NSString(string: result)
    }
    
    @objc public func getFaces() -> [_Face] {
        return faces
    }
    
    @objc public func detection() -> Bool {
        return isDetection
    }
    
    @objc public func set(analyzeMaxFace: Bool,
                    onlyBase64: Bool) {
        self.analyzeMaxFace = analyzeMaxFace
        self.onlyBase64 = onlyBase64
    }
    
    @objc public func canAnalyzeMaxFace() -> Bool {
        return analyzeMaxFace
    }
    
    @objc public func isOnlyBase64() -> Bool {
       return onlyBase64
    }
    
    @objc public func resetFaceId(ids: NSString) {
        let faceIds = Array(String(ids).split(separator: ","))
        for id in faceIds {
            guard let face = faces.first(where: { String($0.getFaceId()) == id}) else { return }
            face.set(vector: "")
        }
    }
    
    @objc public func predict(sampleBuffer: CMSampleBuffer,
                        cameraPosition: AVCaptureDevice.Position) {
        guard let imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else {
            debugPrint("unable to get image from sample buffer")
            return
        }
        isDetection = true
        let ciImage = CIImage(cvImageBuffer: imageBuffer)
        let image = UIImage(ciImage: ciImage)
        let visionImage = VisionImage(buffer: sampleBuffer)
        let orientation = imageOrientation(deviceOrientation: UIDevice.current.orientation,
                                           cameraPosition: cameraPosition)
        visionImage.orientation = orientation
        self.visionImage = visionImage
        faceDetector.process(visionImage) { (faces, error) in
            var visionFaces = [Face]()
            self.isDetection = false
            if (error != nil) {
                self.faces = []
                return
            } else if let faces = faces, let face = faces.first {
                self.mCurrentFrame = image.resizeImage(max: 300)
                if self.analyzeMaxFace {
                    visionFaces = [face]
                } else {
                    visionFaces = faces
                }
            } else {
                self.faces = []
                return
            }
        
            var cashedFaces: [NSInteger: _Face] = [:]
            self.faces.forEach { (face) in
                cashedFaces[face.getFaceId()] = face
            }
            var trackIds = Set<NSInteger>()
            var newFaces = [_Face]()
            for visionFace in visionFaces {
                let rect = visionFace.frame
                trackIds.insert(visionFace.trackingID)
                if let face = self.faces.first(where: { $0.getFaceId() == visionFace.trackingID }) {
                    face.changeFace(bitmap: image, face: visionFace, rect: rect)
                } else {
                    guard let croppedImage = image.fixedCrop(with: rect) else { return }
                    let newFace = _Face(face: visionFace,
                                        rect: rect,
                                        faceCropped: croppedImage)
                    newFaces.append(newFace)
                }
            }
            var removeFaceSet = Set<_Face>()
            for face in self.faces {
                if !trackIds.contains(face.getFaceId()) {
                    removeFaceSet.insert(face)
                }
            }
            let removeIds = removeFaceSet.map({ $0.getFaceId() })
            self.faces = self.faces.filter({ !removeIds.contains($0.getFaceId())})
            self.faces.append(contentsOf: newFaces)
            self.faceRecognition()
        }
    }
    
    func faceRecognition() {
        for face in self.faces {
            if face.hasVector() {
                continue
            }
            let vector = OpenCvWrapper.detectFace(face.getFaceCropped())
            if let vectorArray = vector as? [NSNumber] {
                let vectorString = vectorArray.map({ $0.stringValue }).joined(separator: ",")
                print(vectorArray.count)
                face.set(vector: vectorString)
            }
        }
    }
    
    @objc public func getCurrentFrameToBase64() -> NSString {
        guard let currentFrame = mCurrentFrame else { return "" }
        let base64 = currentFrame.pngData()?.base64EncodedString() ?? ""
        return NSString(string: base64)
    }
    
    func imageOrientation(deviceOrientation: UIDeviceOrientation,cameraPosition: AVCaptureDevice.Position) -> UIImage.Orientation {
        switch deviceOrientation {
        case .portrait:
            return cameraPosition == .front ? .upMirrored : .down
        case .landscapeLeft:
            return cameraPosition == .front ? .left : .left
        case .portraitUpsideDown:
            return cameraPosition == .front ? .downMirrored : .up
        case .landscapeRight:
            return cameraPosition == .front ? .right : .left
        default:
            return .up
        }
    }
}

extension UIImage {
    public convenience init?(pixelBuffer: CVPixelBuffer) {
        CVPixelBufferLockBaseAddress(pixelBuffer, .readOnly)
        defer { CVPixelBufferUnlockBaseAddress(pixelBuffer, .readOnly) }
        let baseAddress = CVPixelBufferGetBaseAddress(pixelBuffer)
        let width = CVPixelBufferGetWidth(pixelBuffer)
        let height = CVPixelBufferGetHeight(pixelBuffer)
        let bytesPerRow = CVPixelBufferGetBytesPerRow(pixelBuffer)
        let colorSpace = CGColorSpaceCreateDeviceRGB()
        let bitmapInfo = CGBitmapInfo(rawValue: CGImageAlphaInfo.premultipliedFirst.rawValue | CGBitmapInfo.byteOrder32Little.rawValue)
        guard let context = CGContext(data: baseAddress, width: width, height: height,
                                      bitsPerComponent: 8, bytesPerRow: bytesPerRow,
                                      space: colorSpace, bitmapInfo: bitmapInfo.rawValue) else { return nil }
        
        guard let cgImage = context.makeImage() else { return nil }
        self.init(cgImage: cgImage, scale: 1, orientation: .upMirrored)
    }
}
