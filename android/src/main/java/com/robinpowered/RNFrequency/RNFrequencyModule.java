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
  private AudioTrack track;

  public RNFrequencyModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return MODULE_NAME;
  }

  @ReactMethod
  public void playFrequency(double frequency, double duration, final Promise promise) {
    final int dur = (int) duration;
    final int freq = (int) frequency;

    // ~1 to make sure count is an even number
    // *2 for stereo
    int count = (int)(11025.0 * 2.0 * (dur)) & ~1;

    // 0x7FFF to convert 32b to 16b
    short[] samples = new short[count];
    for(int i = 0; i < count; i += 2){
      short sample = (short)(Math.sin(2 * Math.PI * i / (44100.0 / freq)) * 0x7FFF);
      samples[i + 0] = sample;
      samples[i + 1] = sample;
    }

    // If track is initialized, stop playback and release resources
    if (track != null && track.getState() != AudioTrack.STATE_UNINITIALIZED) {
        if (track.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) {
            try{
                track.stop();
            }catch (IllegalStateException e)
            {
                // no-op
            }

        }
        track.release();
    }

    // create audio track - methods used differ based on OS version
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        track = new AudioTrack(AudioManager.STREAM_MUSIC, 11025,
        AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
        count * (Short.SIZE / 8), AudioTrack.MODE_STATIC);
    } else {
        track = new AudioTrack.Builder()
            .setAudioFormat(new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(11025)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build())
            .setBufferSizeInBytes(count * (Short.SIZE / 8))
            .build();
    }

    // push sample into AudioTrack object
    track.write(samples, 0, count);

    // play track
    track.play();

    new android.os.Handler().postDelayed(
        new Runnable() {
            public void run() {
                promise.resolve(true);
            }
        },
    (long) (duration * 1000));
  }
}
