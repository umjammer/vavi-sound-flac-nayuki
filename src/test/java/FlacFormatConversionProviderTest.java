/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vavi.sound.SoundUtil;
import vavi.util.Debug;
import vavi.util.StringUtil;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vavi.sound.SoundUtil.volume;
import static vavix.util.DelayedWorker.later;


/**
 * FlacFormatConversionProviderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2023/10/08 umjammer initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class FlacFormatConversionProviderTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    static long time;

    static {
        System.setProperty("vavi.util.logging.VaviFormatter.extraClassMethod", "org\\.tritonus\\.share\\.TDebug#out");

        time = System.getProperty("vavi.test", "").equals("ide") ? 1000 * 1000 : 9 * 1000;
    }

    @Property
    String flac = "src/test/resources/test.flac";

//    @Test
//    @DisplayName("directly")
//    void test0() throws Exception {
//
//        Path path = Paths.get(flac);
//        AudioInputStream sourceAis = new FlacAudioFileReader().getAudioInputStream(new BufferedInputStream(Files.newInputStream(path)));
//
//        AudioFormat inAudioFormat = sourceAis.getFormat();
//Debug.println("IN: " + inAudioFormat);
//        AudioFormat outAudioFormat = new AudioFormat(
//            inAudioFormat.getSampleRate(),
//            16,
//            inAudioFormat.getChannels(),
//            true,
//            false);
//Debug.println("OUT: " + outAudioFormat);
//
//        assertTrue(AudioSystem.isConversionSupported(outAudioFormat, inAudioFormat));
//
//        AudioInputStream pcmAis = new FlacFormatConversionProvider().getAudioInputStream(outAudioFormat, sourceAis);
//        DataLine.Info info = new DataLine.Info(SourceDataLine.class, pcmAis.getFormat());
//        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
//        line.open(pcmAis.getFormat());
//        line.addLineListener(ev -> Debug.println(ev.getType()));
//        line.start();
//
//        volume(line, .1d);
//
//        byte[] buf = new byte[1024];
//        while (!later(time).come()) {
//            int r = pcmAis.read(buf, 0, 1024);
//            if (r < 0) {
//                break;
//            }
//            line.write(buf, 0, r);
//        }
//        line.drain();
//        line.stop();
//        line.close();
//    }

    @Test
    @Disabled
    @DisplayName("as spi")
    void test1() throws Exception {

        Path path = Paths.get(flac);
        AudioInputStream sourceAis = AudioSystem.getAudioInputStream(new BufferedInputStream(Files.newInputStream(path)));

        AudioFormat inAudioFormat = sourceAis.getFormat();
Debug.println("IN: " + inAudioFormat);
        AudioFormat outAudioFormat = new AudioFormat(
            inAudioFormat.getSampleRate(),
            16,
            inAudioFormat.getChannels(),
            true,
            false);
Debug.println("OUT: " + outAudioFormat);

        assertTrue(AudioSystem.isConversionSupported(outAudioFormat, inAudioFormat));

        AudioInputStream pcmAis = AudioSystem.getAudioInputStream(outAudioFormat, sourceAis);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, pcmAis.getFormat());
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(pcmAis.getFormat());
        line.addLineListener(ev -> Debug.println(ev.getType()));
        line.start();

        volume(line, .1d);

        byte[] buf = new byte[1024];
        while (!later(time).come()) {
            int r = pcmAis.read(buf, 0, 1024);
            if (r < 0) {
                break;
            }
            line.write(buf, 0, r);
        }
        line.drain();
        line.stop();
        line.close();
    }

//    @Test
//    @DisplayName("another input type 2")
//    void test2() throws Exception {
//        URL url = Paths.get(flac).toUri().toURL();
//        AudioInputStream ais = AudioSystem.getAudioInputStream(url);
//        assertEquals(FlacEncoding.FLAC, ais.getFormat().getEncoding());
//    }
//
//    @Test
//    @DisplayName("another input type 3")
//    void test3() throws Exception {
//        File file = Paths.get(flac).toFile();
//        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
//        assertEquals(FlacEncoding.FLAC, ais.getFormat().getEncoding());
//    }

    @Test
    @Disabled
    @DisplayName("when unsupported file coming")
    void test5() throws Exception {
        InputStream is = FlacFormatConversionProviderTest.class.getResourceAsStream("/test.caf");
        int available = is.available();
        UnsupportedAudioFileException e = assertThrows(UnsupportedAudioFileException.class, () -> {
Debug.println(StringUtil.paramString(is));
            AudioSystem.getAudioInputStream(is);
        });
Debug.println(e.getMessage());
        assertEquals(available, is.available()); // spi must not consume input stream even one byte
    }

    @Test
    @Disabled
    @DisplayName("clip")
    void test4() throws Exception {

        AudioInputStream ais = AudioSystem.getAudioInputStream(Paths.get(flac).toFile());
Debug.println(ais.getFormat());

        Clip clip = AudioSystem.getClip();
CountDownLatch cdl = new CountDownLatch(1);
clip.addLineListener(ev -> {
 Debug.println(ev.getType());
 if (ev.getType() == LineEvent.Type.STOP)
  cdl.countDown();
});
        clip.open(AudioSystem.getAudioInputStream(new AudioFormat(44100, 16, 2, true, false), ais));
SoundUtil.volume(clip, 0.1f);
        clip.start();
if (!System.getProperty("vavi.test", "").equals("ide")) {
 Thread.sleep(10 * 1000);
 clip.stop();
 Debug.println("Interrupt");
} else {
 cdl.await();
}
        clip.drain();
        clip.stop();
        clip.close();
    }
}

/* */
