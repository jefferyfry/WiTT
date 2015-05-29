package com.clearwire.tools.wimax.fieldtesttool.ui.map;

import java.util.EventObject;

public class MapTagEvent extends EventObject {
	
	
	private String tag=null;
	
	public MapTagEvent(Object source,String tag){
		super(source);
		this.tag=tag;
	}

	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @param tag the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}


}
