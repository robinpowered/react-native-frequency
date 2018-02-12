package com.robinpowered.Frequency;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

import android.media.AudioTrack;

public class FrequencyModule extends ReactContextBaseJavaModule {
    private static final String MODULE_NAME = "Frequency";

    private AudioSession audioSession;

    public FrequencyModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    private void cancel() {
        audioSession.cancel();
        audioSession = null;
    }

    private void complete() {
        audioSession.complete();
        audioSession = null;
    }

    @ReactMethod
    public void playFrequency(double frequency, double duration, final Promise promise) {
        if (audioSession != null && audioSession.isPlaying()) {
            cancel();
        }

        AudioTrack track = FrequencyTrackFactory.create(frequency, duration);

        track.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onPeriodicNotification(AudioTrack track) {
                // no-op
            }

            @Override
            public void onMarkerReached(AudioTrack track) {
                FrequencyModule.this.complete();
            }
        });

        audioSession = new AudioSession(track, promise);

        // play track
        track.play();
    }

    @ReactMethod
    public void stop(final Promise promise) {
        if (audioSession != null && audioSession.isPlaying()) {
            cancel();
            promise.resolve(true);
        } else {
            promise.reject(new Error("No audio track is currently playing"));
        }
    }
}
