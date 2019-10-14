#import "RNFrequency.h"
#import "TGSineWaveToneGenerator.h"

@interface RNFrequency ()
@property(strong) TGSineWaveToneGenerator *toneGenRef;
@property RCTPromiseRejectBlock reject;
@property RCTPromiseResolveBlock resolve;
@end

@implementation RNFrequency
RCT_EXPORT_MODULE(Frequency);

+ (BOOL)requiresMainQueueSetup
{
   return YES;
}

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

RCT_EXPORT_METHOD(playFrequency:(double)frequency duration:(double)duration resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    if ([self.toneGenRef isPlaying]) {
        // if audio is still currently playing, prevent stopWithSuccess selector from executing
        [NSObject cancelPreviousPerformRequestsWithTarget:self
                                                 selector:@selector(stopWithSuccess)
                                                   object:nil];
        [self stopWithFailure];
    }

    self.toneGenRef->_channels[0].frequency = frequency;
    self.toneGenRef->_channels[1].frequency = frequency;

    self.resolve = resolve;
    self.reject = reject;

    [self.toneGenRef play];

    // stop playing after specified duration and perform stopWithSuccess selector
    [self performSelector:@selector(stopWithSuccess) withObject:nil afterDelay:duration];
}

RCT_EXPORT_METHOD(stop:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    if ([self.toneGenRef isPlaying]) {
        // if audio is still currently playing, prevent stopWithSuccess selector from executing
        [NSObject cancelPreviousPerformRequestsWithTarget:self
                                                 selector:@selector(stopWithSuccess)
                                                   object:nil];
        [self stopWithFailure];
        resolve(@YES);
    } else {
        reject(@"NO_TRACK_IS_PLAYING", @"There is no audio track that is currently playing", nil);
    }
}

@end
