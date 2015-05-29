package com.clearwire.tools.wimax.fieldtesttool.ui.map;

import java.io.File;

public class Map implements Comparable<Map>,Cloneable {
	
	private String name = "New Map";
	private String fileUrl = "classpath:default.jpg";
	private double nwLatitude=45.0;
	private double nwLongitude=-122.0;
	private double seLatitude=46.0;
	private double seLongitude=-123.0;
	private int id=(int)(Math.random()*99999);
	
	public Map(){
		//does nothing
	}
	
	/**
	 * @param name
	 * @param fileUrl
	 * @param nwLatitude
	 * @param nwLongitude
	 * @param seLatitude
	 * @param seLongitude
	 */
	public Map(String name, String fileUrl, double nwLatitude,
			double nwLongitude, double seLatitude, double seLongitude) {
		super();
		this.name = name;
		this.fileUrl = fileUrl;
		this.nwLatitude = nwLatitude;
		this.nwLongitude = nwLongitude;
		this.seLatitude = seLatitude;
		this.seLongitude = seLongitude;
	}

	
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}



	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}



	/**
	 * @return the fileUrl
	 */
	public String getFileUrl() {
		return fileUrl;
	}



	/**
	 * @param fileUrl the fileUrl to set
	 */
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}



	/**
	 * @return the nwLatitude
	 */
	public double getNwLatitude() {
		return nwLatitude;
	}



	/**
	 * @param nwLatitude the nwLatitude to set
	 */
	public void setNwLatitude(double nwLatitude) {
		this.nwLatitude = nwLatitude;
	}



	/**
	 * @return the nwLongitude
	 */
	public double getNwLongitude() {
		return nwLongitude;
	}



	/**
	 * @param nwLongitude the nwLongitude to set
	 */
	public void setNwLongitude(double nwLongitude) {
		this.nwLongitude = nwLongitude;
	}



	/**
	 * @return the seLatitude
	 */
	public double getSeLatitude() {
		return seLatitude;
	}



	/**
	 * @param seLatitude the seLatitude to set
	 */
	public void setSeLatitude(double seLatitude) {
		this.seLatitude = seLatitude;
	}



	/**
	 * @return the seLongitude
	 */
	public double getSeLongitude() {
		return seLongitude;
	}



	/**
	 * @param seLongitude the seLongitude to set
	 */
	public void setSeLongitude(double seLongitude) {
		this.seLongitude = seLongitude;
	}



	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}



	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}



	public String toString(){
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Map o) {
			return this.name.compareTo(o.getName());
	}
}
