package com.clearwire.tools.wimax.fieldtesttool.listgeo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ListgeoIO {
	
	private static Log log = LogFactory.getLog(ListgeoIO.class);
	
	public static double[] getCorners(String tifImageFile){
		double[] corners = new double[4];
		try {
			String command = "listgeo -d \""+tifImageFile+"\"";
			Process process =Runtime.getRuntime().exec(command);
			log.debug("Executed "+command);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			
			while((line=reader.readLine())!=null){
					log.trace(line);
					if(line.startsWith("Upper Left")){
						StringTokenizer st = new StringTokenizer(line,"()W,ENS");
						st.nextToken(); //skip
						String longitude = st.nextToken().trim();
						String latitude = st.nextToken().trim();
						corners[0] = Double.parseDouble(longitude);
						corners[1] = Double.parseDouble(latitude);
					}
					else if(line.startsWith("Lower Right")){
						StringTokenizer st = new StringTokenizer(line,"()W,ENS");
						st.nextToken(); //skip
						String longitude = st.nextToken().trim();
						String latitude = st.nextToken().trim();
						corners[2] = Double.parseDouble(longitude);
						corners[3] = Double.parseDouble(latitude);
					}
			}
		}
		catch(Exception e){
			log.warn("WARN",e);
		}
		finally {
			log.debug("Returning "+corners[0]+" "+corners[1]+" "+corners[2]+" "+corners[3]+" "+" for "+tifImageFile);
			return corners;
		}
	}
	
	public static void main(String[] args){
		double[] corners = ListgeoIO.getCorners("C:\\Documents and Settings\\jfry\\workspace\\WiTT\\Hillsboro_Area.tif");
		System.out.println(corners[0]+" "+corners[1]+" "+corners[2]+" "+corners[3]+" ");
	}

}
