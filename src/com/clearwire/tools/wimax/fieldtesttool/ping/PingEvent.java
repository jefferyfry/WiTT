package com.clearwire.tools.wimax.fieldtesttool.ping;

import java.util.EventObject;

public class PingEvent extends EventObject {
	
	
	private int time=-1;
	
	public PingEvent(Object source,int time){
		super(source);
		this.time=time;
	}

	/**
	 * @return the time
	 */
	public int getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(int time) {
		this.time = time;
	}
}
