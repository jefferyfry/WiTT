package com.clearwire.tools.wimax.fieldtesttool.ftp;

import java.util.EventObject;

public class FtpEvent extends EventObject {
	
	
	private String message=null;
	
	private boolean connected=false;
	
	public FtpEvent(Object source,String message){
		super(source);
		this.message=message;
	}
	
	

	public FtpEvent(Object source, String message, boolean connected) {
		super(source);
		this.message = message;
		this.connected = connected;
	}



	/**
	 * @return the connected
	 */
	public boolean isConnected() {
		return connected;
	}



	/**
	 * @param connected the connected to set
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}



	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param messsage the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
