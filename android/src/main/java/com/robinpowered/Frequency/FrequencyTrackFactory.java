package com.robinpowered.Frequency;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

class FrequencyTrackFactory {
    // number of samples of audio carried per second - higher for better quality/clarity
    static final int SAMPLE_RATE = 44100;

    /**
     * Generates an AudioTrack instance that plays a specific frequency
     * for a certain duration
     *
     * @param frequency Frequency of the audio track
     * @param duration Duration of the audio track
     * @return AudioTrack instance
     */
    public static AudioTrack create(double frequency, double duration) {
        AudioTrack track;

        final int dur = (int) duration;
        final int numOfSamples = dur * SAMPLE_RATE;

        final byte soundData[] = createSoundData(frequency, numOfSamples);        

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

    static byte[] createSoundData(double frequency, int numberOfSamples) {
        double sample[] = new double[numberOfSamples];
        byte soundData[]= new byte[2 * numberOfSamples];

        for (int i = 0; i < numberOfSamples; i++) {
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

        return soundData;
    }
}
