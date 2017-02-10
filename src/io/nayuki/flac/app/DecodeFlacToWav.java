/* 
 * FLAC library (Java)
 * Copyright (c) Project Nayuki. All rights reserved.
 * https://www.nayuki.io/
 */

package io.nayuki.flac.app;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import io.nayuki.flac.decode.DataFormatException;
import io.nayuki.flac.decode.FlacDecoder;
import io.nayuki.flac.decode.StreamInfo;


public final class DecodeFlacToWav {
	
	public static void main(String[] args) throws IOException {
		// Handle command line arguments
		if (args.length != 2) {
			System.err.println("Usage: java DecodeFlacToWav InFile.flac OutFile.wav");
			System.exit(1);
			return;
		}
		File inFile  = new File(args[0]);
		File outFile = new File(args[1]);
		
		// Read and decode the file
		FlacDecoder dec;
		try (InputStream in = new FileInputStream(inFile)) {
			dec = new FlacDecoder(in);
		}
		
		// Check decoder metadata
		if (dec.hashCheck == 0)
			System.err.println("Warning: MD5 hash field was blank");
		else if (dec.hashCheck == 2)
			throw new DataFormatException("MD5 hash check failed");
		StreamInfo streamInfo = dec.streamInfo;
		if (streamInfo.sampleDepth != 16)
			throw new UnsupportedOperationException("Only 16-bit sample depth supported");
		
		// Start writing WAV output file
		try (DataOutputStream out = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(outFile)))) {
			DecodeFlacToWav.out = out;
			
			// Header chunk
			int[][] samples = dec.samples;
			int sampleDataLen = samples[0].length * streamInfo.numChannels * streamInfo.sampleDepth / 8;
			out.writeInt(0x52494646);  // "RIFF"
			writeLittleInt32(sampleDataLen + 36);
			out.writeInt(0x57415645);  // "WAVE"
			
			// Metadata chunk
			out.writeInt(0x666D7420);  // "fmt "
			writeLittleInt32(16);
			writeLittleInt16(0x0001);
			writeLittleInt16(streamInfo.numChannels);
			writeLittleInt32(streamInfo.sampleRate);
			writeLittleInt32(streamInfo.sampleRate * streamInfo.numChannels * streamInfo.sampleDepth / 8);
			writeLittleInt16(streamInfo.numChannels * streamInfo.sampleDepth / 8);
			writeLittleInt16(streamInfo.sampleDepth);
			
			// Audio data chunk ("data")
			out.writeInt(0x64617461);  // "data"
			writeLittleInt32(sampleDataLen);
			for (int i = 0; i < samples[0].length; i++) {
				for (int j = 0; j < samples.length; j++)
					writeLittleInt16(samples[j][i]);
			}
		}
	}
	
	
	private static DataOutputStream out;
	
	
	private static void writeLittleInt16(int x) throws IOException {
		out.writeShort(Integer.reverseBytes(x) >>> 16);
	}
	
	
	private static void writeLittleInt32(int x) throws IOException {
		out.writeInt(Integer.reverseBytes(x));
	}
	
}
