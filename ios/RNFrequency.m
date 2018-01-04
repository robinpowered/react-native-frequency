#import "RNFrequency.h"
#import "TGSineWaveToneGenerator.h"

@interface RNFrequency ()
@property(strong) TGSineWaveToneGenerator *toneGenRef;
@end

@implementation RNFrequency

static UInt32 const TWO_CHANNELS = 2;

- (instancetype)init
{
    if (self = [super init]) {
        self.toneGenRef = [[TGSineWaveToneGenerator alloc] initWithChannels:TWO_CHANNELS];
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
    [self.toneGenRef stop];
    self.toneGenRef->_channels[0].frequency=frequency;
    self.toneGenRef->_channels[1].frequency=frequency;
    [self.toneGenRef playForDuration:duration callback:^(void){
        resolve(@(YES));
    }];
}

@end
