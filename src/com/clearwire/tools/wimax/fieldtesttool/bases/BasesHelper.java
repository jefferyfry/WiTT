package com.clearwire.tools.wimax.fieldtesttool.bases;

import java.awt.Color;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clearwire.tools.wimax.fieldtesttool.configuration.ConfigurationHelper;
import com.clearwire.tools.wimax.fieldtesttool.metrics.MetricsHelper;
import com.clearwire.tools.wimax.fieldtesttool.ui.map.Base;
import com.clearwire.tools.wimax.fieldtesttool.ui.map.Map;

public class BasesHelper {
	
	private Log log = LogFactory.getLog(BasesHelper.class);
	
	private final static BasesHelper baseHelper = new BasesHelper();
	
	private Hashtable<String,Base> bsidLookup = new Hashtable<String,Base>();
	
	private XMLConfiguration basesConfig = new XMLConfiguration();
	
	private BasesHelper(){
		
		try {
			String file = System.getProperty("user.home")+File.separator+"bases.xml";
			basesConfig.setExpressionEngine(new XPathExpressionEngine());
			basesConfig.load(file);
			
			log.info("Loaded bases from "+file);
			log.info("Set XPath expression engine.");
			
		}
		catch(ConfigurationException e){
			try {
				URL fileUrl = ClassLoader.getSystemResource("bases.xml");
				basesConfig.load(fileUrl);
				log.info("Loaded bases from "+fileUrl.toExternalForm());
			}
			catch(Exception f){
				log.error("Unable to load bases from classpath.",f);
			}
		}
					
		List<HierarchicalConfiguration> fields = basesConfig.configurationsAt("//bases/base");
		for(Iterator<HierarchicalConfiguration> iter = fields.iterator();iter.hasNext();){
			HierarchicalConfiguration sub = (HierarchicalConfiguration) iter.next();
		    String baseName = sub.getString("@name");
		    //String frequency = sub.getString("@frequency");
		    String baseid = sub.getString("@baseid");
		    String color = sub.getString("@color","FF3333");
		    double latitude = sub.getDouble("@latitude");
		    double longitude = sub.getDouble("@longitude");
		    double azimuth = sub.getDouble("@azimuth");
		    int beamwidth = sub.getInt("@beamwidth",90);
		    
		    boolean display = sub.getBoolean("@display",true);
		    
		    Base base = new Base(latitude,longitude,baseName,azimuth,beamwidth,baseid,Color.decode(color));
		    
		    base.setDisplay(display);
		    
			bsidLookup.put(base.getBaseid().toLowerCase(), base);
			log.debug("Added base "+base.getText());
		}
    }
	
	public XMLConfiguration getConfig(){
		return basesConfig;
	}
	
	public void updateBase(Base base){
		try {
			bsidLookup.put(base.getBaseid().toLowerCase(), base);
		}
		catch(Exception e){
			log.error("ERROR",e);
		}
	}
	
	public void clearBases(){
		bsidLookup.clear();
	}
	
	public void saveBasesToConfig() {
		SubnodeConfiguration mapsNode = basesConfig.configurationAt("//bases");
		mapsNode.getRootNode().removeChildren();
		
		for(Iterator<Base> iter = getBases().iterator();iter.hasNext();){
			Base base = iter.next();
			basesConfig.addProperty("bases base@name", base.getText());
			basesConfig.addProperty("bases/base[last()] @baseid", base.getBaseid().toLowerCase());
			basesConfig.addProperty("bases/base[last()] @color", "#"+Integer.toHexString(base.getColor().getRed())+Integer.toHexString(base.getColor().getGreen())+Integer.toHexString(base.getColor().getBlue()));
			basesConfig.addProperty("bases/base[last()] @latitude", base.getLatitude());
			basesConfig.addProperty("bases/base[last()] @longitude", base.getLongitude());
			basesConfig.addProperty("bases/base[last()] @azimuth", base.getAzimuth());
			basesConfig.addProperty("bases/base[last()] @beamwidth", base.getBeamwidth());
			basesConfig.addProperty("bases/base[last()] @display", base.isDisplay());
		}
	}
	
	public Base getBaseByBaseId(String bsid){
		return (Base)bsidLookup.get(bsid.toLowerCase());
	}
	
	public static BasesHelper getInstance(){
		return baseHelper;
	}
	
	public ArrayList<Base> getBases(){
		ArrayList<Base> basesList = new ArrayList<Base>(bsidLookup.values());
		Collections.sort(basesList);
		return basesList;
	}
	
	public int getBaseCount(){
		return bsidLookup.size();
	}
	
	public void saveConfiguration(){
		try {
			String file = System.getProperty("user.home")+File.separator+"bases.xml";
			log.info("Saving bases to "+file);
			basesConfig.save(file);
		}
		catch(ConfigurationException e){
			log.error("Unable to save bases.",e);
		}
	}
}
