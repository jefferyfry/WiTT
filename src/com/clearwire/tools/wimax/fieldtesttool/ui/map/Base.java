package com.clearwire.tools.wimax.fieldtesttool.ui.map;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Base extends MapGraphic implements Cloneable {

	private double azimuth=0.0;
	private String baseid="000000000000";
	private boolean connected;
	private int beamwidth=90;
	private boolean display=true;
	
	public Base(){
		super();
		this.setText("New Base");
		//uses defaults
	}
	
	public Base(double latitude, double longitude, String text,double azimuth,int beamwidth,String baseid) {
		super(latitude,longitude,text);
		this.azimuth=azimuth;
		this.baseid=baseid;
		this.beamwidth=beamwidth;
	}
	
	public Base(double latitude, double longitude, String text, double azimuth,int beamwidth, String baseid, Color color) {
		super(latitude,longitude,text,color);
		this.azimuth=azimuth;
		this.baseid=baseid;
		this.beamwidth=beamwidth;
	}

	public Base(double latitude, double longitude, String text,double azimuth,int beamwidth, String baseid,
			BufferedImage graphic) {
		super(latitude,longitude,text,graphic);
		this.azimuth=azimuth;
		this.baseid=baseid;
		this.beamwidth=beamwidth;
	}


	/**
	 * @return the beamwidth
	 */
	public int getBeamwidth() {
		return beamwidth;
	}

	/**
	 * @param beamwidth the beamwidth to set
	 */
	public void setBeamwidth(int beamwidth) {
		this.beamwidth = beamwidth;
	}

	/**
	 * @return the baseid
	 */
	public String getBaseid() {
		return baseid;
	}

	/**
	 * @param baseid the baseid to set
	 */
	public void setBaseid(String baseid) {
		this.baseid = baseid;
	}

	/**
	 * @return the azimuth
	 */
	public double getAzimuth() {
		return azimuth;
	}

	/**
	 * @param azimuth the azimuth to set
	 */
	public void setAzimuth(double azimuth) {
		this.azimuth = azimuth;
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
	 * @return the display
	 */
	public boolean isDisplay() {
		return display;
	}

	/**
	 * @param display the display to set
	 */
	public void setDisplay(boolean display) {
		this.display = display;
	}
}
