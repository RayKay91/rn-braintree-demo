//
//  BTDropIn.h
//  v67jsc
//
//  Created by Rayhaan Khalid on 28/01/2022.
//

#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#else
#import <React/RCTBridgeModule.h>
#endif
#import <Foundation/Foundation.h>
#import <BraintreeDropIn/BraintreeDropIn.h>
#import <Braintree/BraintreeThreeDSecure.h>

NS_ASSUME_NONNULL_BEGIN

@interface BTDropIn : NSObject <RCTBridgeModule>
@property (nonatomic, strong) UIViewController *_Nonnull reactRoot;
@end

NS_ASSUME_NONNULL_END
