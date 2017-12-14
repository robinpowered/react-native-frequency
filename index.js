import {
  DeviceEventEmitter,
  NativeModules
} from 'react-native';

const {RNFrequency} = NativeModules;

export default {
  ...RNFrequency,
  addListener (callback) {
    return DeviceEventEmitter.addListener(
      RNFrequency.AUDIO_CHANGED_NOTIFICATION,
      callback
    );
  }
};
