#import "RNFrequency.h"
#import "TGSineWaveToneGenerator.h"

@interface RNFrequency ()
@property(strong) TGSineWaveToneGenerator *toneGenRef;
@property RCTPromiseRejectBlock reject;
@property RCTPromiseResolveBlock resolve;
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

- (void) stopWithSuccess
{
    [self.toneGenRef stop];
    self.resolve(@YES);
    [self removePromiseReferences];
}

- (void) stopWithFailure
{
    [self.toneGenRef stop];
    self.reject(@"TRACK_STOPPED_PLAYING", @"Track stopped playing", nil);
    [self removePromiseReferences];
}

- (void) removePromiseReferences
{
    self.reject = NULL;
    self.resolve = NULL;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE(@"FrequencyManager");

RCT_EXPORT_METHOD(playFrequency:(double)frequency duration:(double)duration resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    if ([self.toneGenRef isPlaying]) {
        [NSObject cancelPreviousPerformRequestsWithTarget:self
                                                 selector:@selector(stopWithSuccess)
                                                   object:nil];
        [self stopWithFailure];
    }

    self.toneGenRef->_channels[0].frequency=frequency;
    self.toneGenRef->_channels[1].frequency=frequency;

    self.resolve = resolve;
    self.reject = reject;

    [self.toneGenRef play];
    [self performSelector:@selector(stopWithSuccess) withObject:nil afterDelay:duration];
}

@end

