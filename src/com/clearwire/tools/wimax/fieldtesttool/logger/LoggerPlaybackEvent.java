package com.clearwire.tools.wimax.fieldtesttool.logger;

import java.util.Date;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggerPlaybackEvent extends LoggerEvent {
	
	private Log log = LogFactory.getLog(LoggerPlaybackEvent.class);
	
	public LoggerPlaybackEvent(Object source,Hashtable<String,String> metrics,Date eventDate){
		super(source,metrics,eventDate);
	}

}
