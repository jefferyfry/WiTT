package com.clearwire.tools.wimax.fieldtesttool.ui.map;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class MapGraphic implements Comparable<MapGraphic> {

	private double latitude;
	private double longitude;
	private String text;
	private Color color=Color.BLUE;
	private boolean isConnected;

	private BufferedImage graphic=null;
	
	public MapGraphic(){
		//use defaults
	}
	
	public MapGraphic(double latitude, double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public MapGraphic(double latitude, double longitude, String text) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.text = text;
	}
	

	public MapGraphic(double latitude, double longitude, String text,
			Color color) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.text = text;
		this.color = color;
	}


	public MapGraphic(double latitude, double longitude, String text,BufferedImage graphic) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.text = text;
		this.graphic = graphic;
	}
	
	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	/**
	 * @return the isConnected
	 */
	public boolean isConnected() {
		return isConnected;
	}

	/**
	 * @param isConnected the isConnected to set
	 */
	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}


	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
	}


	/**
	 * @return the graphic
	 */
	public BufferedImage getGraphic() {
		return graphic;
	}

	/**
	 * @param graphic the graphic to set
	 */
	public void setGraphic(BufferedImage graphic) {
		this.graphic = graphic;
	}
	
	public int compareTo(MapGraphic o) {
		return this.text.compareTo(o.getText());
}
}
