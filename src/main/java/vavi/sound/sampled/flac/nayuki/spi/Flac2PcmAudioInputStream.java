/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.sampled.flac.nayuki.spi;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import io.nayuki.flac.common.StreamInfo;
import io.nayuki.flac.decode.ByteArrayFlacInput;
import io.nayuki.flac.decode.FlacDecoder;
import vavi.io.OutputEngine;
import vavi.io.OutputEngineInputStream;


/**
 * Flac2PcmAudioInputStream.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2023/10/09 umjammer initial version <br>
 */
public class Flac2PcmAudioInputStream extends AudioInputStream {

    /**
     * Constructor.
     *
     * @param in     the underlying input stream.
     * @param format the target format of this stream's audio data.
     * @param length the length in sample frames of the data in this stream.
     */
    public Flac2PcmAudioInputStream(AudioInputStream in, AudioFormat format, long length) throws IOException {
        super(new OutputEngineInputStream(new FlacOutputEngine(in)), format, length);
    }

    /** */
    private static class FlacOutputEngine implements OutputEngine {

        /** */
        private DataOutputStream out;

        /** */
        private final FlacDecoder decoder;

        /** @throws IllegalArgumentException Only whole-byte sample depth supported */
        public FlacOutputEngine(AudioInputStream in) throws IOException {
            this.decoder = new FlacDecoder(new ByteArrayFlacInput(in.readAllBytes()));
            while (decoder.readAndHandleMetadataBlock() != null) ;
            StreamInfo streamInfo = decoder.streamInfo;
            if (streamInfo.sampleDepth % 8 != 0)
                throw new IllegalArgumentException("Only whole-byte sample depth supported");
            this.samples = new int[streamInfo.numChannels][(int) streamInfo.numSamples];
            for (int off = 0; ; ) {
                int len = decoder.readAudioBlock(samples, off);
                if (len == 0)
                    break;
                off += len;
            }
            this.bytesPerSample = streamInfo.sampleDepth / 8;
        }

        @Override
        public void initialize(OutputStream out) throws IOException {
            if (this.out != null) {
                throw new IOException("Already initialized");
            } else {
                this.out = new DataOutputStream(out);
            }
        }

        /** */
        private int bytesPerSample;

        /** PCM [ch][Hz*sec] */
        private int[][] samples;

        /** samples index */
        private int index;

        @Override
        public void execute() throws IOException {
            if (out == null) {
                throw new IOException("Not yet initialized");
            } else {
                if (index < samples[0].length) {
                    for (int[] sample : samples) {
                        int val = sample[index];
                        if (bytesPerSample == 1) {
                            out.write(val + 128);  // Convert to unsigned, as per WAV PCM conventions
                        } else {  // 2 <= bytesPerSample <= 4
                            for (int k = 0; k < bytesPerSample; k++)
                                out.write(val >>> (k * 8));  // Little endian
                        }
                    }
                    index++;
                } else {
                    out.close();
                }
            }
        }

        @Override
        public void finish() throws IOException {
            decoder.close();
        }
    }
}
