/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import io.nayuki.flac.common.StreamInfo;
import io.nayuki.flac.decode.ByteArrayFlacInput;
import io.nayuki.flac.decode.DataFormatException;
import io.nayuki.flac.decode.FlacDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vavi.sound.sampled.flac.nayuki.spi.FlacFormatConversionProvider;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static vavi.sound.SoundUtil.volume;
import static vavix.util.DelayedWorker.later;


/**
 * DecodeFlacTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2023/10/09 umjammer initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class DecodeFlacTest {

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
    static double volume;

    static {
        time = System.getProperty("vavi.test", "").equals("ide") ? 1000 * 1000 : 9 * 1000;
        volume = Double.parseDouble(System.getProperty("vavi.test.volume",  "0.2"));
    }

    @Property
    String flac = "src/test/resources/test.flac";

    @Test
    @DisplayName("play using api directory")
    void test1() throws Exception {

        InputStream is = Files.newInputStream(Path.of(flac));

        // Decode input FLAC file
        StreamInfo streamInfo;
        int[][] samples;
        try (FlacDecoder dec = new FlacDecoder(new ByteArrayFlacInput(is.readAllBytes()))) {

            // Handle metadata header blocks
            while (dec.readAndHandleMetadataBlock() != null) ;
            streamInfo = dec.streamInfo;
            if (streamInfo.sampleDepth % 8 != 0)
                throw new UnsupportedOperationException("Only whole-byte sample depth supported");

            // Decode every block
            samples = new int[streamInfo.numChannels][(int) streamInfo.numSamples];
            for (int off = 0; ; ) {
                int len = dec.readAudioBlock(samples, off);
                if (len == 0)
                    break;
                off += len;
            }
        }

        int bytesPerSample = streamInfo.sampleDepth / 8;

        AudioFormat outAudioFormat = new AudioFormat(
                streamInfo.sampleRate,
                streamInfo.sampleDepth,
                streamInfo.numChannels,
                true,
                false);
Debug.println("OUT: " + outAudioFormat);

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, outAudioFormat);
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(outAudioFormat);
        line.addLineListener(ev -> Debug.println(ev.getType()));
        line.start();

        volume(line, volume);

        int frameSize = bytesPerSample * streamInfo.numChannels;
        byte[] buf = new byte[frameSize];
        Deque<Byte> deque = new ArrayDeque<>();
        while (!later(time).come()) {
            for (int i = 0; i < streamInfo.numSamples; i++) {
                for (int c = 0; c < streamInfo.numChannels; c++) {
                    int val = samples[c][i];
                    if (bytesPerSample == 1) {
                        deque.offer((byte) (val + 128));  // Convert to unsigned, as per WAV PCM conventions
                    }
                    else {  // 2 <= bytesPerSample <= 4
                        for (int k = 0; k < bytesPerSample; k++)
                            deque.offer((byte) (val >>> (k * 8)));  // Little endian
                    }
                }
                if (deque.size() > frameSize) {
                    for (int j = 0; j < frameSize; j++) {
                        buf[j] = deque.pop();
                    }
                    line.write(buf, 0, frameSize);
                }
            }
        }
        line.drain();
        line.stop();
        line.close();
    }
}
