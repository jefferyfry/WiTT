package com.clearwire.tools.wimax.fieldtesttool.configuration;

import java.io.File;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConfigurationHelper {
	
private Log log = LogFactory.getLog(ConfigurationHelper.class);
	
	private final static ConfigurationHelper configurationHelper = new ConfigurationHelper();
		
	private XMLConfiguration config = new XMLConfiguration();
	
	private ConfigurationHelper(){
		try {
			String file = System.getProperty("user.home")+File.separator+"witt_config.xml";
			config.setExpressionEngine(new XPathExpressionEngine());
			config.load(file);
			
			log.info("Loaded configuration from "+file);
			log.info("Set XPath expression engine.");
			
		}
		catch(ConfigurationException e){
			try {
				URL fileUrl = ClassLoader.getSystemResource("witt_config.xml");
				config.load(fileUrl);
				log.info("Loaded configuration from "+fileUrl.toExternalForm());
			}
			catch(Exception f){
				log.error("Unable to load configuration from classpath.",f);
			}
		}
	}
	
	public static ConfigurationHelper getInstance(){
		return configurationHelper;
	}

	/**
	 * @return the config
	 */
	public XMLConfiguration getConfig() {
		return config;
	}
	
	public void saveConfiguration(){
		try {
			String file = System.getProperty("user.home")+File.separator+"witt_config.xml";
			log.info("Saving config to "+file);
			config.save(file);
		}
		catch(ConfigurationException e){
			log.error("Unable to save configuration.",e);
		}
	}

}
