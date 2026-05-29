#import <AVFoundation/AVFoundation.h>
#if TARGET_OS_OSX
#import <FlutterMacOS/FlutterMacOS.h>
#else
#import <Flutter/Flutter.h>
#endif

extern NSString * const AudioServiceChildrenChangedNotification;

@interface AudioServicePlugin : NSObject<FlutterPlugin>

@property (readonly, nonatomic) FlutterMethodChannel *channel;

+ (id<FlutterBinaryMessenger>)binaryMessenger;

@end

enum AudioProcessingState {
    ApsIdle,
    ApsLoading,
    ApsBuffering,
    ApsReady,
    ApsCompleted,
    ApsError
};

enum MediaAction {
    AStop,
    APause,
    APlay,
    ARewind,
    ASkipToPrevious,
    ASkipToNext,
    AFastForward,
    ASetRating,
    ASeekTo,
    APlayPause,
    APlayFromMediaId,
    APlayFromSearch,
    ASkipToQueueItem,
    APlayFromUri,
    APrepare,
    APrepareFromMediaId,
    APrepareFromSearch,
    APrepareFromUri,
    ASetRepeatMode,
    ASetShuffleModeEnabled, // deprecated
    ASetCaptioningEnabled,
    ASetShuffleMode,
    // iOS/macOS-specific
    ASeekBackward,
    ASeekForward,
    ASetSpeed,
};
