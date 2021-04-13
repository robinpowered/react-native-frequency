# ⚠️ DEPRECATION NOTICE
This project is not being actively maintained.
If you have issues using it, feel free to collaborate in the Issues section and use something like [Patch-Package](https://www.npmjs.com/package/patch-package) to fix any issues you might find.

# react-native-frequency

## Getting started

`$ npm install react-native-frequency --save`

### Mostly automatic installation

`$ react-native link react-native-frequency`

## Usage
```javascript
import Frequency from 'react-native-frequency';

// Play frequency (Hz) for certain duration(s)
FrequencyManager.playFrequency(frequency, duration).then(didPlay => console.log(didPlay));
```
