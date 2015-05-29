package com.clearwire.tools.wimax.fieldtesttool.metrics;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clearwire.tools.wimax.fieldtesttool.configuration.ConfigurationHelper;

public class MetricsHelper {
	
	private Log log = LogFactory.getLog(MetricsHelper.class);
	
	private final static MetricsHelper metricsHelper = new MetricsHelper();
	
	private Hashtable<String,Metric> nameLookup = new Hashtable<String,Metric>();
	private Hashtable<String,Metric> propertyLookup = new Hashtable<String,Metric>();
	
	private ArrayList<DisplayMetric> displayMetrics = new ArrayList<DisplayMetric>();
		
	private MetricsHelper(){
		int index=0;
		List<HierarchicalConfiguration> fields = ConfigurationHelper.getInstance().getConfig().configurationsAt("//valid-metrics/metric");
		for(Iterator<HierarchicalConfiguration> iter = fields.iterator();iter.hasNext();){
			HierarchicalConfiguration sub = (HierarchicalConfiguration) iter.next();
		    String name = sub.getString("@name").trim();
		    List<String> property = sub.getList("@property");
		    
		    String conversion = sub.getString("@conversion","x");
		    String format = sub.getString("@format","String");
		    
		    Metric newMetric = new Metric(index++,name,property,conversion,format);
		    nameLookup.put(name,newMetric);
		    for(Iterator<String> iterProp=property.iterator();iterProp.hasNext();)
		    	propertyLookup.put(iterProp.next(),newMetric);
		    log.debug("Added metric "+newMetric);
		}
		
    	fields = ConfigurationHelper.getInstance().getConfig().configurationsAt("//display-options/display-value");
    	for(Iterator<HierarchicalConfiguration> iter = fields.iterator();iter.hasNext();){
			HierarchicalConfiguration sub = (HierarchicalConfiguration) iter.next();
			String name = sub.getString("@name");
			int min = sub.getInt("@min");
			int max = sub.getInt("@max");
			
			DisplayMetric displayMetricValue = new DisplayMetric(name,min,max);
			
			displayMetrics.add(displayMetricValue);
			log.debug("Added DisplayMetric "+name);
		}
	}
	
	public static MetricsHelper getInstance(){
		return metricsHelper;
	}
	
	public Collection<DisplayMetric> getDisplayMetrics(){
		return displayMetrics;
	}
	
	public Collection<Metric> getAllMetrics(){
		return nameLookup.values();
	}
	
	public Metric getMetricByName(String name){
		return nameLookup.get(name.trim());
	}
	
	public Metric getMetricByProperty(String property){
		return propertyLookup.get(property.trim());
	}
	
	public boolean isValidByName(String name){
		if(getMetricByName(name)==null)
			return false;
		else
			return true;
	}
	
	public boolean isValidByProperty(String property){
		if(getMetricByProperty(property)==null)
			return false;
		else
			return true;
	}
	
	public int getMetricCount(){
		return nameLookup.size();
	}
}
