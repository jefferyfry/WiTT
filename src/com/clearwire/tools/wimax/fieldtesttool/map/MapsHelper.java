package com.clearwire.tools.wimax.fieldtesttool.map;

import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
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

public class MapsHelper {
	
	private Log log = LogFactory.getLog(MapsHelper.class);
	
	private final static MapsHelper mapsHelper = new MapsHelper();
		
	private XMLConfiguration mapsConfig = new XMLConfiguration();
	
	private Hashtable<Integer,Map> idLookup = new Hashtable<Integer,Map>();
	
	private ArrayList<String> largeMapFiles = new ArrayList<String>();
	
	private MapsHelper(){
		try {
			String file = System.getProperty("user.home")+File.separator+"maps.xml";
			mapsConfig.setExpressionEngine(new XPathExpressionEngine());
			mapsConfig.load(file);
			
			log.info("Loaded maps from "+file);
			log.info("Set XPath expression engine.");
		}
		catch(ConfigurationException e){
			try {
				URL fileUrl = ClassLoader.getSystemResource("maps.xml");
				mapsConfig.load(fileUrl);
				log.info("Loaded maps from "+fileUrl.toExternalForm());
			}
			catch(Exception f){
				log.error("Unable to load maps from classpath.",f);
			}
		}
					
		List<HierarchicalConfiguration> fields = mapsConfig.configurationsAt("//maps/map");
		for(Iterator<HierarchicalConfiguration> iter = fields.iterator();iter.hasNext();){
			HierarchicalConfiguration map = (HierarchicalConfiguration) iter.next();
		    // sub contains now all data about a single field
		    String mapName = map.getString("@name");
		    double mapNwLat = map.getDouble("@nw_lat");
			double mapNwLong = map.getDouble("@nw_long");
			
			double mapSeLat = map.getDouble("@se_lat");
			double mapSeLong = map.getDouble("@se_long");
			
			String mapPath = map.getString("@file");
			
			try {
				URL mapFile = new URL(mapPath);
				InputStream stream = mapFile.openStream();
				long size = stream.available()/1000;
				stream.close();
				
				if(size>500)
					largeMapFiles.add(mapName);
			}
			catch(Exception e){
			}
			
			Map newMap = new Map(mapName,mapPath,mapNwLat,mapNwLong,mapSeLat,mapSeLong);
		    
			idLookup.put(new Integer(newMap.getId()), newMap);
			log.debug("Added map "+newMap.getName());
		}
    }
	
	public void updateMap(Map map){
		try {
			idLookup.put(Integer.valueOf(map.getId()), map);
		}
		catch(Exception e){
			log.error("ERROR",e);
		}
	}
	
	public void clearMaps(){
		idLookup.clear();
	}
	
	public int getMapsSize(){
		return idLookup.size();
	}
	
	public XMLConfiguration getConfig(){
		return mapsConfig;
	}
	
	public Map[] getMapArray(){
		Map[] maps = new Map[idLookup.size()];
		ArrayList<Map> mapList = new ArrayList<Map>(idLookup.values());
		Collections.sort(mapList);
		return (Map[])mapList.toArray(maps);
	}
	
	public ArrayList<Map> getMaps(){
		ArrayList<Map> mapList = new ArrayList<Map>(idLookup.values());
		Collections.sort(mapList);
		return mapList;
	}
	
	public static MapsHelper getInstance(){
		return mapsHelper;
	}
	
	public void saveMapsToConfig() {
		SubnodeConfiguration mapsNode = mapsConfig.configurationAt("//maps");
		mapsNode.getRootNode().removeChildren();
		Map[] maps = getMapArray();
		
		for(int i=0;i<maps.length;i++){
			mapsConfig.addProperty("maps map@name", maps[i].getName());
			mapsConfig.addProperty("maps/map[last()] @file", maps[i].getFileUrl());
			mapsConfig.addProperty("maps/map[last()] @nw_lat", maps[i].getNwLatitude());
			mapsConfig.addProperty("maps/map[last()] @nw_long", maps[i].getNwLongitude());
			mapsConfig.addProperty("maps/map[last()] @se_lat", maps[i].getSeLatitude());
			mapsConfig.addProperty("maps/map[last()] @se_long", maps[i].getSeLongitude());
		}
	}
	
	
	
	/**
	 * @return the largeMapFiles
	 */
	public ArrayList<String> getLargeMapFiles() {
		return largeMapFiles;
	}

	public void saveConfiguration(){
		try {
			String file = System.getProperty("user.home")+File.separator+"maps.xml";
			log.info("Saving maps to "+file);
			
			mapsConfig.save(file);
		}
		catch(ConfigurationException e){
			log.error("Unable to save maps.",e);
		}
	}
}
