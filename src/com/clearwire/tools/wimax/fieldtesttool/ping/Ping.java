package com.clearwire.tools.wimax.fieldtesttool.ping;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Ping {
	
	private Log log = LogFactory.getLog(Ping.class);
	
	private String address;
	private int pingInterval=500;
		
	private boolean pingGo=true;
	
	private ArrayList<PingEventListener> pingListeners = new ArrayList<PingEventListener>();
		
	private PingThread pingThread=null;
	
	public Ping(String address,int pingInterval) {
		super();
		this.address = address;
		this.pingInterval=pingInterval;
	}
	
	public Ping(int pingInterval) {
		super();
		this.pingInterval=pingInterval;
	}
	
	
	
	class PingThread extends Thread {

		public void run() {
			while(pingGo){
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("ping -n 1 -w 3000 "+address).getInputStream()));
					String line=null;
					int time=-1;
					while((line=reader.readLine())!=null){
						int index = line.indexOf("time=");
						if(index>0){
							String timeStr = line.substring(index+5,line.indexOf("ms"));
							time = Integer.parseInt(timeStr);
						}
					}
						
					firePingEventOccurred(new PingEvent(this,time));					
				}
				catch(Exception e){
					log.error("ERROR",e);
				}
				finally {
					try {
						Thread.sleep(pingInterval);
					}
					catch(Exception e){
						log.error("ERROR",e);
					}
				}
			}
		}
	}
	
	public void firePingEventOccurred(PingEvent event){
		log.debug("Firing Ping event.");
		for(Iterator<PingEventListener> iter = pingListeners.iterator();iter.hasNext();){
			PingEventListener listener = (PingEventListener)iter.next();
			listener.eventOccurred(event);
		}
	}
	
	public void addPingListener(PingEventListener listener){
		pingListeners.add(listener);
	}
	
	public void removePingListener(PingEventListener listener){
		pingListeners.remove(listener);
	}
	
	public void start(){
		pingGo=true;
		pingThread = new PingThread();
		pingThread.start();
	}
	
	public void stop(){
		pingGo=false;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the pingInterval
	 */
	public int getPingInterval() {
		return pingInterval;
	}

	/**
	 * @param pingInterval the pingInterval to set
	 */
	public void setPingInterval(int pingInterval) {
		this.pingInterval = pingInterval;
	}
	
	public void setToGateway(){
		try {
			String localAddress = InetAddress.getLocalHost().getHostAddress();
			Process result = Runtime.getRuntime().exec("ipconfig");
	
	        BufferedReader reader = new BufferedReader(new InputStreamReader(result.getInputStream()));
	        String line = null;
	        boolean adapterFlag = false;
	        while((line=reader.readLine())!=null){
	        	if(line.contains(localAddress))
	        		adapterFlag=true;
	        	if(line.contains("Default Gateway")&&adapterFlag){
			        StringTokenizer st = new StringTokenizer(line,":");
			        st.nextToken();
			        address = st.nextToken().trim();
			        return;
	        	}
	        }
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}
}
