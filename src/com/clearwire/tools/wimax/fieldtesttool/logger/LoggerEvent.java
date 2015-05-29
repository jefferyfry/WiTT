package com.clearwire.tools.wimax.fieldtesttool.logger;

import java.util.Date;
import java.util.EventObject;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggerEvent extends EventObject {
	
	private Log log = LogFactory.getLog(LoggerEvent.class);
	
	private Hashtable<String,String> metrics;
	
	public LoggerEvent(Object source,Hashtable<String,String> metrics,Date eventDate){
		super(source);
		this.metrics=metrics;
	}

	/**
	 * @return the metrics
	 */
	public Hashtable<String,String> getMetrics() {
		return metrics;
	}

	/**
	 * @param metrics the metrics to set
	 */
	public void setMetrics(Hashtable<String,String> metrics) {
		this.metrics = metrics;
	}

}
