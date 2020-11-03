//
//  Face.swift
//  Runner
//
//  Created by MacBook Air GWS on 6/20/20.
//  Copyright © 2020 The Chromium Authors. All rights reserved.
//
import UIKit
import Foundation
import Vision
import MLKitFaceDetection

public class MyFace: NSObject {
    
    private let faceId: NSInteger
    private var rect: CGRect
    private var faceCropped: UIImage
    private var vector = ""
    
    private var rightEyeOpenProbability: CGFloat
    private var leftEyeOpenProbability: CGFloat
    private var smilingProbability: CGFloat
    
    public init(face: Face, rect: CGRect, faceCropped: UIImage) {
        self.faceId = face.trackingID
        self.rect = rect
        self.faceCropped = faceCropped
        self.rightEyeOpenProbability = face.rightEyeOpenProbability
        self.leftEyeOpenProbability = face.leftEyeOpenProbability
        self.smilingProbability = face.smilingProbability
    }
    
    @objc
    public func changeFace(bitmap: UIImage, face: Face, rect: CGRect) {
        if (faceId != face.trackingID) {
           return
        }
        rightEyeOpenProbability = face.rightEyeOpenProbability
        leftEyeOpenProbability = face.leftEyeOpenProbability
        smilingProbability = face.smilingProbability
        
        self.rect = rect
        if (!hasVector()) {
            guard let face = bitmap.fixedCrop(with: rect) else { return }
            self.faceCropped = face.resizeImage(max: 400)
        }
    }
    
    public func getFaceId() -> NSInteger {
        return faceId
    }
    
    public func getFaceCropped() -> UIImage {
        return faceCropped
    }
    
    public func hasVector() -> Bool {
        return !vector.isEmpty
    }
    
    public func set(vector: String) {
        self.vector = vector
    }
    
    public func toString() -> String {
        var model: [String: String] = [:]
        model["face_id"] = faceId.description
        model["rect"] = "\(rect.minX)" + "," + "\(rect.minY)" + "," + "\(rect.maxX)" + "," + "\(rect.maxY)"
        model["left_eye_probability"] = "\(rightEyeOpenProbability)"
        model["right_eye_probability"] = "\(leftEyeOpenProbability)"
        model["smile_probability"] = "\(smilingProbability)"
        if hasVector() {
            model["vector"] = vector
        }
        let jsonData = try! JSONEncoder().encode(model)
        return String(data: jsonData, encoding: .utf8) ?? ""
    }
}

extension UIImage {
    func resizeImage(max: CGFloat) -> UIImage {
        
        if size.width > size.height {
            let scale = max / self.size.width
            let newHeight = self.size.height * scale
            UIGraphicsBeginImageContext(CGSize(width: max, height: newHeight))
            self.draw(in: CGRect(x: 0, y: 0, width: max, height: newHeight))
            let newImage = UIGraphicsGetImageFromCurrentImageContext()
            UIGraphicsEndImageContext()
            return newImage!
        } else {
            let scale = max / self.size.height
            let newWidth = self.size.width * scale
            UIGraphicsBeginImageContext(CGSize(width: newWidth, height: max))
            self.draw(in: CGRect(x: 0, y: 0, width: newWidth, height: max))
            let newImage = UIGraphicsGetImageFromCurrentImageContext()
            UIGraphicsEndImageContext()
            return newImage!
        }
    }
    
    func crop(with rect: CGRect) -> UIImage? {
        let widthPadding = floor(rect.width * 0.1)
        let heightPadding = floor(rect.height * 0.1)
        
        let rectWithPadding = rect.insetBy(dx: -widthPadding, dy: -heightPadding)
        
        if let img = cgImage {
            if let cgCrop = img.cropping(to: rectWithPadding) {
                let resultImage = UIImage(cgImage: cgCrop)
                return resultImage
            } else {
                print("Fail crop CGImage")
                return nil
            }
        }
        
        if let img = ciImage {
            let ciCrop = img.cropped(to: rectWithPadding)
            let resultImage = UIImage(ciImage: ciCrop)
            return resultImage
        }
        print("Cannot crop UIImage")
        return nil
    }
    
    func fixedOrientation() -> UIImage? {
        switch UIDevice.current.orientation {
        case .landscapeLeft:
            return rotate(radians: -.pi / 2)
        case .landscapeRight:
            return rotate(radians: .pi / 2)
        case .portraitUpsideDown:
            return rotate(radians: .pi)
        default:
            return self
        }
    }
    
    func fixedCrop(with rect: CGRect) -> UIImage? {
        let image = crop(with: rect)
        return image?.fixedOrientation()
    }
    
    func rotate(radians: Float) -> UIImage? {
        var newSize = CGRect(origin: CGPoint.zero, size: self.size).applying(CGAffineTransform(rotationAngle: CGFloat(radians))).size
        // Trim off the extremely small float value to prevent core graphics from rounding it up
        newSize.width = floor(newSize.width)
        newSize.height = floor(newSize.height)
        
        UIGraphicsBeginImageContextWithOptions(newSize, false, self.scale)
        let context = UIGraphicsGetCurrentContext()!
        
        // Move origin to middle
        context.translateBy(x: newSize.width/2, y: newSize.height/2)
        // Rotate around middle
        context.rotate(by: CGFloat(radians))
        // Draw the image at its center
        self.draw(in: CGRect(x: -self.size.width/2, y: -self.size.height/2, width: self.size.width, height: self.size.height))
        
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return newImage
    }
}
