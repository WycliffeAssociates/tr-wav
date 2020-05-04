# tr-wav
[![](https://jitpack.io/v/mbr4477/tr-wave.svg)](https://jitpack.io/#mbr4477/tr-wave)
[![Build Status](https://travis-ci.org/mbr4477/tr-wav.svg?branch=master)](https://travis-ci.org/mbr4477/tr-wav)

A Kotlin library for working with tR WAV audio file metadata. 
Ported from the [BTT Recorder Android app](https://github.com/Bible-Translation-Tools/BTT-Recorder/tree/dev/translationRecorder/app/src/main/java/org/wycliffeassociates/translationrecorder/wav).

## Gradle Usage
Add the following repository to `build.gradle`.
```groovy
maven { url 'https://jitpack.io' }
```
Add the dependency:
```groovy
implementation 'com.github.WycliffeAssociates:tr-wav:<latest-version>'
```
## Example
```kotlin
import com.matthewrussell.trwav.*

fun main(args: Array<String>) {
    // Read in a file
    val wavFile: WavFile = WavFileReader().read(wavFile)
    // Print the file's tR metadata
    println(wavFile.metadata)
    // Update the metadata
    waveFile.metadata.slug = "gen"
    // Write the file back out
    WavFileWriter().write(wavFile, File("output.wav"))
}
```