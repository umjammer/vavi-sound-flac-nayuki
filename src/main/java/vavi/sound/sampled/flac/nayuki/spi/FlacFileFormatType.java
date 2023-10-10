/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.sampled.flac.nayuki.spi;

import javax.sound.sampled.AudioFileFormat;


/**
 * FlacFileFormatType.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2023/10/09 umjammer initial version <br>
 */
public class FlacFileFormatType extends AudioFileFormat.Type {

    /**
     * Specifies an FLAC file.
     */
    public static final AudioFileFormat.Type FLAC = new FlacFileFormatType("FLAC", "flac");

    /**
     * Constructs a file type.
     *
     * @param name      the name of the Flac File Format.
     * @param extension the file extension for this Flac File Format.
     */
    public FlacFileFormatType(String name, String extension) {
        super(name, extension);
    }
}
