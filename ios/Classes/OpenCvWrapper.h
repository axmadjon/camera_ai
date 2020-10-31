//
//  OpenCvWrapper.h
//  Runner
//
//  Created by MacBook Air GWS on 3/26/20.
//  Copyright © 2020 The Chromium Authors. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface OpenCvWrapper: NSObject

+(nonnull NSMutableArray* )detectFace: (nonnull UIImage *)rawImage;

@end
