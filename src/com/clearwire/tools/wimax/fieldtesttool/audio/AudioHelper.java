package com.clearwire.tools.wimax.fieldtesttool.audio;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clearwire.tools.wimax.fieldtesttool.configuration.ConfigurationHelper;

public class AudioHelper {
	
	private Log log = LogFactory.getLog(AudioHelper.class);
	
	private final static AudioHelper audioHelper = new AudioHelper();
	
    private Hashtable<String,AudioFile> audioFiles = new Hashtable<String,AudioFile>();

	private AudioHelper(){
		List<HierarchicalConfiguration> fields = ConfigurationHelper.getInstance().getConfig().configurationsAt("//audio-files/audio");
		for(Iterator<HierarchicalConfiguration> iter = fields.iterator();iter.hasNext();){
			HierarchicalConfiguration map = (HierarchicalConfiguration) iter.next();
		    // sub contains now all data about a single field
		    String audioName = map.getString("@name");
		    String audioFile = map.getString("@file");
		    if((audioName!=null)&&(audioFile!=null)){
			    try {
			    	URL fileUrl = null;
					if(audioFile.startsWith("classpath:"))
						fileUrl = ClassLoader.getSystemResource(audioFile.substring(audioFile.indexOf(":")+1));
					else {
						File file = new File(audioFile);
						fileUrl = file.toURL();
					}
				
			    	audioFiles.put(audioName,new AudioFile(audioName,fileUrl));
			    	log.debug("Loaded audio file "+audioName);
			    }
			    catch(Exception e){
					log.error("Unable to load audio file: "+audioFile,e);
				}
		    }
		}
	}
	
	public static AudioHelper getInstance(){
		return audioHelper;
	}
	
	public void playAudio(String name) throws IOException, LineUnavailableException,UnsupportedAudioFileException {
		URL audioFileUrl = audioFiles.get(name).getAudioFile();
		if(audioFileUrl==null)
			return;
		AudioInputStream audioInput = AudioSystem.getAudioInputStream(audioFileUrl);
		BufferedInputStream bufferedAudio = new BufferedInputStream(audioInput);
		AudioFormat audioFormat = audioInput.getFormat();
		SourceDataLine	line = null;
		DataLine.Info	info = new DataLine.Info(SourceDataLine.class,audioFormat);
		
		line = (SourceDataLine) AudioSystem.getLine(info);

		line.open(audioFormat);

		line.start();

		int	nBytesRead = 0;
		byte[]	abData = new byte[20000];
		while (nBytesRead != -1)
		{
			try
			{
				nBytesRead = bufferedAudio.read(abData, 0, abData.length);
			}
			catch (IOException e)
			{
				log.error("Unable to play audio.",e);
			}
			if (nBytesRead >= 0)
			{
				int	nBytesWritten = line.write(abData, 0, nBytesRead);
			}
		}

		line.drain();

		line.close();
	}
}
