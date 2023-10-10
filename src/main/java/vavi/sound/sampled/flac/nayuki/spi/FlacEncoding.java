/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.sampled.flac.nayuki.spi;

import javax.sound.sampled.AudioFormat;


/**
 * FlacEncoding.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2023/10/09 umjammer initial version <br>
 */
public class FlacEncoding extends AudioFormat.Encoding {

    /** Specifies any Flac encoded data. */
    public static final FlacEncoding FLAC = new FlacEncoding("FLAC");

    /**
     * Constructs a new encoding.
     *
     * @param name Name of the Flac encoding.
     */
    public FlacEncoding(String name) {
        super(name);
    }
}
