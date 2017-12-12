#import "RNFrequency.h"
#import "TGSineWaveToneGenerator.h"

// Used to send events to JS
#if __has_include(<React/RCTBridge.h>)
#import <React/RCTBridge.h>
#elif __has_include("RCTBridge.h")
#import "RCTBridge.h"
#else
#import "React/RCTBridge.h"
#endif

#if __has_include(<React/RCTEventDispatcher.h>)
#import <React/RCTEventDispatcher.h>
#elif __has_include("RCTEventDispatcher.h")
#import "RCTEventDispatcher.h"
#else
#import "React/RCTEventDispatcher.h"
#endif

@interface RNFrequency ()
@property(strong) TGSineWaveToneGenerator *toneGenRef;
@end

@implementation RNFrequency

@synthesize bridge = _bridge;

- (instancetype)init
{
    if (self = [super init]) {
        self.toneGenRef = [[TGSineWaveToneGenerator alloc] initWithChannels:2];
    }
    return self;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(playFrequency:(double)frequency duration:(double)duration resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    self.toneGenRef->_channels[0].frequency=frequency;
    self.toneGenRef->_channels[0].frequency=frequency;
    [self.toneGenRef playForDuration:duration];
    resolve(@YES);
}

@end
