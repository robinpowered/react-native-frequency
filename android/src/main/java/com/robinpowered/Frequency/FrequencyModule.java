package com.robinpowered.Frequency;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

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
}

class AudioSession {
    Promise promise;
    AudioTrack track;
    static final String TRACK_STOPPED_PLAYING = "TRACK_STOPPED_PLAYING";

    AudioSession(AudioTrack track, Promise promise) {
        this.track = track;
        this.promise = promise;
    }

    void cancel() {
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

    void complete() {
        track.release();
        track = null;

        promise.resolve(true);
        promise = null;
    }

    boolean isPlaying() {
        return track != null
                && track.getState() != AudioTrack.STATE_UNINITIALIZED
                && track.getPlayState() != AudioTrack.PLAYSTATE_STOPPED;
    }
}

class FrequencyTrackFactory {
    static final int SAMPLE_RATE = 44100;

    public static AudioTrack create(double frequency, double duration) {
        AudioTrack track;


        final int dur = (int) duration;
        final int numOfSamples = dur * SAMPLE_RATE;

        final double sample[] = new double[numOfSamples];
        final byte soundData[] = new byte[2 * numOfSamples];

        for (int i = 0; i < numOfSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (SAMPLE_RATE/frequency));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalized.
        int idx = 0;
        for (double dVal : sample) {
            short val = (short) (dVal * 32767);
            soundData[idx++] = (byte) (val & 0x00ff);
            soundData[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        // create audio track - methods used differ based on OS version
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, numOfSamples, AudioTrack.MODE_STATIC);
        } else {
            track = new AudioTrack.Builder()
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                    .setBufferSizeInBytes(numOfSamples).build();
        }

        // push sample into AudioTrack object
        track.write(soundData, 0, numOfSamples);

        // callback when track finishes playing
        track.setNotificationMarkerPosition(numOfSamples / 2);

        return track;
    }
}
