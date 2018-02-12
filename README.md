
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
