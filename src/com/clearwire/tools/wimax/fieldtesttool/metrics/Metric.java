package com.clearwire.tools.wimax.fieldtesttool.metrics;

import java.text.DecimalFormat;
import java.util.List;

import org.nfunk.jep.JEP;


public class Metric implements Comparable<Object>,Cloneable {
	
	private String name;
	private List<String> property;
	private String conversion="x";
	private String value;
	private String format="String";
	private int order;
	
	/**
	 * @param name
	 * @param property
	 * @param conversion
	 */
	public Metric(int order,String name, List<String> property, String conversion,String format) {
		super();
		this.order=order;
		this.name = name;
		this.property = property;
		this.conversion = conversion;
		this.format=format;
	}

	/**
	 * @param name
	 * @param property
	 * @param conversion
	 * @param value
	 */
	public Metric(int order,String name, List<String> property, String conversion,String format, String value) {
		super();
		this.order=order;
		this.name = name;
		this.property = property;
		this.conversion = conversion;
		this.format=format;
		this.value = value;
	}

	/**
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(String format) {
		this.format = format;
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
	 * @return the property
	 */
	public List<String> getProperty() {	
		return property;
	}

	/**
	 * @param property the property to set
	 */
	public void setProperty(List<String> property) {
		this.property = property;
	}

	/**
	 * @return the conversion
	 */
	public String getConversion() {
		return conversion;
	}

	/**
	 * @param conversion the conversion to set
	 */
	public void setConversion(String conversion) {
		this.conversion = conversion;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		if(value==null)
			return null;
		else if (format.equals("String"))
			return value;
		else
			return convertValue();
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	private String convertValue(){
		try {
			JEP parser = new JEP();
			parser.addVariable("x", Double.parseDouble(value));
			parser.parseExpression(conversion);
			double value = parser.getValue();
			DecimalFormat decFormat = new DecimalFormat(format);
			return decFormat.format(value);
		}
		catch(Exception e){
			e.printStackTrace();
			return "Unable to covert "+this.name+": "+e.getMessage();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if(o instanceof Metric){
			Metric metric = (Metric)o;
			if(this.order<metric.getOrder())
				return -1;
			else if(this.order>metric.getOrder())
				return 1;
			else
				return 0;
		}
		else
			return -1;
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(order);
		buffer.append(":");
		buffer.append(name);
		buffer.append(":");
		buffer.append(property);
		buffer.append(":");
		buffer.append(conversion);
		buffer.append(":");
		buffer.append(format);
		try {
			if(value!=null){
				buffer.append(":");
				buffer.append(this.getValue());
			}
		}
		catch(Exception e){
			buffer.append(":");
			buffer.append(e.getMessage());
		}

		return buffer.toString();
	}
	
	public Object clone() {
		try {
			return super.clone();
		}
		catch(CloneNotSupportedException ce){
			return null;
		}
    }

}
