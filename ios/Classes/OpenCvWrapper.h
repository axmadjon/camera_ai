//
//  OpenCvWrapper.h
//  Runner
//
//  Created by MacBook Air GWS on 3/26/20.
//  Copyright © 2020 The Chromium Authors. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface OpenCvWrapper: NSObject
+(nonnull NSMutableArray* )detectFace: (nonnull UIImage *)rawImage;
@end

NS_ASSUME_NONNULL_END
