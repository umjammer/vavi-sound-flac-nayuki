/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.sampled.flac.nayuki.spi;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

import io.nayuki.flac.common.StreamInfo;
import io.nayuki.flac.decode.AbstractFlacLowLevelInput;
import io.nayuki.flac.decode.DataFormatException;
import io.nayuki.flac.decode.FlacDecoder;
import vavi.util.Debug;


/**
 * FlacAudioFileReader.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2023/10/09 umjammer initial version <br>
 */
public class FlacAudioFileReader extends AudioFileReader {

    private static final Logger logger = Logger.getLogger(FlacAudioFileReader.class.getName());

    @Override
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            return getAudioFileFormat(new BufferedInputStream(inputStream), (int) file.length());
        }
    }

    @Override
    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        try (InputStream inputStream = url.openStream()) {
            return getAudioFileFormat(inputStream instanceof BufferedInputStream ? inputStream : new BufferedInputStream(inputStream));
        }
    }

    @Override
    public AudioFileFormat getAudioFileFormat(InputStream stream) throws UnsupportedAudioFileException, IOException {
        return getAudioFileFormat(stream, AudioSystem.NOT_SPECIFIED);
    }

    /**
     * Return the AudioFileFormat from the given InputStream. Implementation.
     *
     * @param bitStream input to decode
     * @param mediaLength unused
     * @return an AudioInputStream object based on the audio file data contained
     * in the input stream.
     * @throws UnsupportedAudioFileException if the File does not point to a valid audio file data
     *                                       recognized by the system.
     * @throws IOException                   if an I/O exception occurs.
     */
    protected AudioFileFormat getAudioFileFormat(InputStream bitStream, int mediaLength) throws UnsupportedAudioFileException, IOException {
Debug.println(Level.FINER, "enter available: " + bitStream.available());
        if (!bitStream.markSupported()) {
            throw new IllegalArgumentException("must be mark supported");
        }
        AudioFormat format;
        try {
            int LIMIT = 8192; // depends BufferedInputStream default buffer size
            bitStream.mark(LIMIT);
            FlacDecoder decoder = new FlacDecoder(new AbstractFlacLowLevelInput() {
                int position;
                @Override protected int readUnderlying(byte[] buf, int off, int len) throws IOException {
Debug.println(Level.FINER, "read: position: "+ position + ", off: " + off + ", len: " + len);
                    if (LIMIT - off < len || position + len > LIMIT) {
Debug.println(Level.FINER, "read overflow: " + (position + off) + " to " + len + " / " + LIMIT);
                        return -1;
                    }
                    int r = bitStream.read(buf, off, len);
                    position += r;
                    return r;
                }
                @Override public long getLength() {
                    return LIMIT;
                }
                @Override public void seekTo(long pos) throws IOException {
                    throw new UnsupportedOperationException();
                }
            });
            while (decoder.readAndHandleMetadataBlock() != null) {
                if (decoder.streamInfo != null) {
                    break;
                }
            }
            StreamInfo streamInfo = decoder.streamInfo;
            if (streamInfo == null) {
                throw new DataFormatException("streamInfo is not found in LIMIT");
            }
            format = new AudioFormat(FlacEncoding.FLAC,
                    streamInfo.sampleRate,
                    streamInfo.sampleDepth,
                    streamInfo.numChannels,
                    AudioSystem.NOT_SPECIFIED,
                    AudioSystem.NOT_SPECIFIED,
                    false);
        } catch (IOException e) {
            if (e instanceof EOFException) {
Debug.println(Level.FINER, e);
Debug.printStackTrace(Level.FINEST, e);
                throw (UnsupportedAudioFileException) new UnsupportedAudioFileException(e.getMessage()).initCause(e);
            } else {
                throw e;
            }
        } catch (Exception e) {
Debug.println(Level.FINER, e);
Debug.printStackTrace(Level.FINEST, e);
            throw (UnsupportedAudioFileException) new UnsupportedAudioFileException(e.getMessage()).initCause(e);
        } finally {
            try {
                bitStream.reset();
            } catch (IOException e) {
                logger.info(e.getMessage());
            }
Debug.println(Level.FINER, "finally available: " + bitStream.available());
        }
        return new AudioFileFormat(FlacFileFormatType.FLAC, format, AudioSystem.NOT_SPECIFIED);
    }

    @Override
    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = Files.newInputStream(file.toPath());
        return getAudioInputStream(new BufferedInputStream(inputStream), (int) file.length());
    }

    @Override
    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = url.openStream();
        return getAudioInputStream(inputStream instanceof BufferedInputStream ? inputStream : new BufferedInputStream(inputStream));
    }

    @Override
    public AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException {
        return getAudioInputStream(stream, AudioSystem.NOT_SPECIFIED);
    }

    /**
     * Obtains an audio input stream from the input stream provided. The stream
     * must point to valid audio file data.
     *
     * @param inputStream the input stream from which the AudioInputStream should be constructed.
     * @param mediaLength unused
     * @return an AudioInputStream object based on the audio file data contained
     * in the input stream.
     * @throws UnsupportedAudioFileException if the File does not point to a valid audio file data
     *                                       recognized by the system.
     * @throws IOException                   if an I/O exception occurs.
     */
    protected AudioInputStream getAudioInputStream(InputStream inputStream, int mediaLength) throws UnsupportedAudioFileException, IOException {
        AudioFileFormat audioFileFormat = getAudioFileFormat(inputStream, mediaLength);
        return new AudioInputStream(inputStream, audioFileFormat.getFormat(), audioFileFormat.getFrameLength());
    }
}
