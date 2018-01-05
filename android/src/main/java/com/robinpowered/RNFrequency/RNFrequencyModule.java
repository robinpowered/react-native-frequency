package com.robinpowered.RNFrequency;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableNativeMap;

import java.util.Map;
import java.util.HashMap;
import javax.annotation.Nullable;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioDeviceInfo;
import android.os.Build;

public class RNFrequencyModule extends ReactContextBaseJavaModule {
    private static final String MODULE_NAME = "RNFrequency";
    private static final String TRACK_STOPPED_PLAYING = "TRACK_STOPPED_PLAYING";
    private static final int SAMPLE_RATE = 44100;

    private AudioTrack track;
    private Promise promise;

    public RNFrequencyModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    private void stop() {
        // If track is initialized, stop playback and release resources
        try {
            track.stop();
        } catch (IllegalStateException e) {
            // no-op
        }

        track.release();
        track = null;

        promise.reject(TRACK_STOPPED_PLAYING, "Track stopped playing");
        promise = null;
    }

    private void saveAudioTrackAndPromise(AudioTrack audioTrack, Promise promise) {
        track = audioTrack;
        this.promise = promise;
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @ReactMethod
    public void playFrequency(double frequency, double duration, final Promise promise) {
        if (track != null
            && track.getState() != AudioTrack.STATE_UNINITIALIZED
            && track.getPlayState() != AudioTrack.PLAYSTATE_STOPPED
        ) {
          stop();
        }

        final int dur = (int) duration;
        final int numOfSamples = dur * SAMPLE_RATE;

        final double sample[] = new double[numOfSamples];
        final byte soundData[] = new byte[2 * numOfSamples];

        for (int i = 0; i < numOfSamples; ++i) {
         sample[i] = Math.sin(2 * Math.PI * i / (SAMPLE_RATE/frequency));
        }

        int idx = 0;
        for (double dVal : sample) {
         short val = (short) (dVal * 32767);
         soundData[idx++] = (byte) (val & 0x00ff);
         soundData[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        AudioTrack freqTrack;

        // create audio track - methods used differ based on OS version
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            freqTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, numOfSamples, AudioTrack.MODE_STATIC);
        } else {
            freqTrack = new AudioTrack.Builder()
                .setAudioFormat(new AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setBufferSizeInBytes(numOfSamples).build();
        }

        // push sample into AudioTrack object
        freqTrack.write(soundData, 0, numOfSamples);

        // callback when track finishes playing
        freqTrack.setNotificationMarkerPosition(numOfSamples / 2);

        freqTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onPeriodicNotification(AudioTrack track) {
                // no-op
            }

            @Override
            public void onMarkerReached(AudioTrack track) {
                track.release();
                track = null;
                promise.resolve(true);
            }
        });

        saveAudioTrackAndPromise(freqTrack, promise);

        // play track
        freqTrack.play();
    }
}
