package com.clearwire.tools.wimax.fieldtesttool.metrics;

public class DisplayMetric {
	
	private String name=null;
	private int min;
	private int max;
	/**
	 * @param name
	 * @param min
	 * @param max
	 */
	public DisplayMetric(String name, int min, int max) {
		super();
		this.name = name;
		this.min = min;
		this.max = max;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the min
	 */
	public int getMin() {
		return min;
	}
	/**
	 * @return the max
	 */
	public int getMax() {
		return max;
	}
	
	public String toString(){
		return name;
	}

}
