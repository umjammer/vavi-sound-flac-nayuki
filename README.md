[![Release](https://jitpack.io/v/umjammer/vavi-sound-flac-nayuki.svg)](https://jitpack.io/#umjammer/vavi-sound-flac-nayuki)
[![Java CI](https://github.com/umjammer/vavi-sound-flac-nayuki/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-sound-flac-nayuki/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-sound-flac-nayuki/actions/workflows/codeql.yml/badge.svg)](https://github.com/umjammer/vavi-sound-flac-nayuki/actions/workflows/codeql.yml)
![Java](https://img.shields.io/badge/Java-25-b07219)
[![Parent](https://img.shields.io/badge/Parent-vavi--sound--sandbox-pink)](https://github.com/umjammer/vavi-sound-sandbox)

# vavi-sound-flac-nayuki

<img src="src/test/resources/duke_flac.png" width="160" alt="logo"/>

Pure Java FLAC decoder (Java Sound SPI) powered by [FLAC-library-Java](https://github.com/nayuki/FLAC-library-Java)

## Install

 * https://jitpack.io/#umjammer/vavi-sound-flac-nayuki

## Usage

```java
    AudioInputStream ais = AudioSystem.getAudioInputStream(Paths.get(flac).toFile());
    Clip clip = AudioSystem.getClip();
    clip.open(AudioSystem.getAudioInputStream(new AudioFormat(44100, 16, 2, true, false), ais));
    clip.loop(Clip.LOOP_CONTINUOUSLY);
```

## References



## TODO

 * ~~java sound spi~~

---

<sub>image designed by @umjammer, drawn by nano banana</sub>
