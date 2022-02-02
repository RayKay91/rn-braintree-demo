#import "BTDropIn.h"
#import <React/RCTLog.h>

@implementation BTDropIn
- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE(BTDropIn)
RCT_EXPORT_METHOD(show:(NSDictionary*)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
  if (!options[@"clientToken"]) {
    reject(@"NO_CLIENT_TOKEN", @"You must provide a client token", nil);
  }

  NSString* clientToken = options[@"clientToken"];

  BTDropInRequest *dropInRequest = [[BTDropInRequest alloc] init];
  dropInRequest.paypalDisabled = YES; // paypal is enabled by default
  
  dropInRequest.vaultManager = NO; // do not allow the removal of exisiting cards as this will break recurring billing

  BTThreeDSecurePostalAddress *address = [[BTThreeDSecurePostalAddress alloc] init];
  address.givenName = options[@"forename"];
  address.surname = options[@"surname"];
  address.streetAddress = options[@"addressLine1"]; // line 1 of address 22 My Street
  address.locality = options[@"city"];
  address.postalCode = options[@"postcode"];

  
  // the additional info is optional but the more info the less likely the customer is presented with a challenge.
  BTThreeDSecureAdditionalInformation *additionalInfo = [[BTThreeDSecureAdditionalInformation alloc] init];
  additionalInfo.shippingAddress = address;

  BTThreeDSecureRequest *threeDSecureRequest = [[BTThreeDSecureRequest alloc] init]; // init 3D secure payments
  NSString* amount = options[@"amount"];
  threeDSecureRequest.amount = [NSDecimalNumber decimalNumberWithString:amount];
  threeDSecureRequest.versionRequested = BTThreeDSecureVersion2;
  threeDSecureRequest.email = options[@"email"];
  threeDSecureRequest.billingAddress = address;
  
  dropInRequest.threeDSecureRequest = threeDSecureRequest;
  

  // init the drop in controller with the request and clientToken
  BTDropInController *dropIn = [[BTDropInController alloc] initWithAuthorization:clientToken request:dropInRequest handler:^(BTDropInController * _Nonnull controller, BTDropInResult * _Nullable result, NSError * _Nullable error) {
    [self.reactRoot dismissViewControllerAnimated: YES completion: nil];
    if (error != nil) {
      reject(@"ERROR", @"There was a problem getting the result", nil);
    } else if (result.canceled) {
      reject(@"CANCELED", @"The user cancelled", nil); // user closed the dropin
    } else {
      BTPaymentMethodNonce *paymentMethod = result.paymentMethod;
      resolve(paymentMethod.nonce);
    }
    
  }];
  
  if (dropIn != nil) {
      [self.reactRoot presentViewController:dropIn animated:YES completion:nil];
  } else {
      reject(@"INVALID_CLIENT_TOKEN", @"The client token seems invalid", nil);
  }
}

- (UIViewController*)reactRoot { // get root React view
    UIViewController *root  = [UIApplication sharedApplication].keyWindow.rootViewController;
    UIViewController *maybeModal = root.presentedViewController;

    UIViewController *modalRoot = root;

    if (maybeModal != nil) {
        modalRoot = maybeModal;
    }

    return modalRoot;
}
@end

