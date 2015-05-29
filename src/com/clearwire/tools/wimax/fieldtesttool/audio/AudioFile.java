package com.clearwire.tools.wimax.fieldtesttool.audio;

import java.net.URL;

public class AudioFile {
	
	private String name;
	private URL audioFile;
	
	public AudioFile(String name, URL audioFile) {
		super();
		this.name = name;
		this.audioFile = audioFile;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the audioFile
	 */
	public URL getAudioFile() {
		return audioFile;
	}
}
