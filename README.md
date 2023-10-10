[![Release](https://jitpack.io/v/umjammer/vavi-sound-flac-nayuki.svg)](https://jitpack.io/#umjammer/vavi-sound-flac-nayuki)
[![Java CI](https://github.com/umjammer/vavi-sound-flac-nayuki/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-sound-flac-nayuki/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-sound-flac-nayuki/actions/workflows/codeql.yml/badge.svg)](https://github.com/umjammer/vavi-sound-flac-nayuki/actions/workflows/codeql.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)
[![Parent](https://img.shields.io/badge/Parent-vavi--sound--sandbox-pink)](https://github.com/umjammer/vavi-sound-sandbox)

# vavi-sound-flac-nayuki

<img src="https://github.com/umjammer/vavi-image-avif/assets/493908/b3c1389e-e50e-402b-921c-1264f8adb117" width="200" alt="FLAC logo"/><sub><a href="https://wiki.xiph.org/XiphWiki:Copyrights">CC BY 3.0 and revised BSD</a></sub>

Pure Java FLAC decoder (Java Sound SPI) powered by [FLAC-library-Java](https://github.com/nayuki/FLAC-library-Java)

## Install

 * https://jitpack.io/#umjammer/vavi-sound-flac-nayuki

## Usage

```java
    AudioInputStream ais = AudioSystem.getAudioInputStream(Files.newInputStream(Paths.get(flac)));
    Clip clip = AudioSystem.getClip();
    clip.open(AudioSystem.getAudioInputStream(new AudioFormat(44100, 16, 2, true, false), ais));
    clip.loop(Clip.LOOP_CONTINUOUSLY);
```

## TODO

 * ~~java sound spi~~
