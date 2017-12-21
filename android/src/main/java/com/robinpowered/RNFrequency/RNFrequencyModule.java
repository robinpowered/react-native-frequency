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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioDeviceInfo;
import android.os.Build;

public class RNFrequencyModule extends ReactContextBaseJavaModule {
  private final String moduleName = "RNFrequency";
  private final String AUDIO_CHANGED_NOTIFICATION = "AUDIO_CHANGED_NOTIFICATION";

  public RNFrequencyModule(ReactApplicationContext reactContext) {
    super(reactContext);

    final ReactApplicationContext thisContext = reactContext;

    IntentFilter headphonesFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
    BroadcastReceiver headphonesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean headsetPluggedIn = false;
            int state = intent.getIntExtra("state", -1);
            switch (state) {
                case (0):
                    headsetPluggedIn = false;
                    break;
                case (1):
                    headsetPluggedIn = true;
                    break;
                default:
                    headsetPluggedIn = false;
            }
            WritableNativeMap data = new WritableNativeMap();
            data.putBoolean("audioJackPluggedIn", headsetPluggedIn);
            if (thisContext.hasActiveCatalystInstance()) {
                thisContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(AUDIO_CHANGED_NOTIFICATION,
                        data);
            }
        }
    };
    thisContext.registerReceiver(headphonesReceiver, headphonesFilter);
  }

  private boolean isHeadSetPluggedIn() {
    AudioManager am = (AudioManager) getReactApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return am.isWiredHeadsetOn();
    } else {
      AudioDeviceInfo[] devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
      for (int i = 0; i < devices.length; i++) {
        AudioDeviceInfo device = devices[i];
        if (device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public @Nullable Map<String, Object> getConstants() {
    HashMap<String, Object> constants = new HashMap<String, Object>();
    constants.put(AUDIO_CHANGED_NOTIFICATION, AUDIO_CHANGED_NOTIFICATION);
    return constants;
  }

  @Override
  public String getName() {
    return moduleName;
  }

  @ReactMethod
  public void playFrequency(double frequency, double duration, final Promise promise) {
    final int dur = (int) duration;
    final int freq = (int) frequency;

    int count = (int)(44100.0 * 2.0 * (dur)) & ~1;

    short[] samples = new short[count];
    for(int i = 0; i < count; i += 2){
      short sample = (short)(Math.sin(2 * Math.PI * i / (44100.0 / freq)) * 0x7FFF);
      samples[i + 0] = sample;
      samples[i + 1] = sample;
    }

    AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
      AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
      count * (Short.SIZE / 8), AudioTrack.MODE_STATIC);

    track.write(samples, 0, count);
    track.play();

    promise.resolve(true);
  }

  @ReactMethod
  public void isAudioJackPluggedIn(final Promise promise) {
    promise.resolve(isHeadSetPluggedIn());
  }
}
