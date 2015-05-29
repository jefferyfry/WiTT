/**
 * 
 */
package com.clearwire.tools.wimax.fieldtesttool.ui.metrics;

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clearwire.tools.wimax.CPEi150.event.CPEi150EventListener;
import com.clearwire.tools.wimax.CPEi150.event.CPEi150StatsEvent;
import com.clearwire.tools.wimax.bcs200.event.BCS200Event;
import com.clearwire.tools.wimax.bcs200.event.BCS200EventListener;
import com.clearwire.tools.wimax.bcs200.event.BasebandStatEvent;
import com.clearwire.tools.wimax.bcs200.event.MacMsgEvent;
import com.clearwire.tools.wimax.fieldtesttool.audio.AudioHelper;
import com.clearwire.tools.wimax.fieldtesttool.bases.BasesHelper;
import com.clearwire.tools.wimax.fieldtesttool.logger.LoggerEvent;
import com.clearwire.tools.wimax.fieldtesttool.logger.LoggerEventListener;
import com.clearwire.tools.wimax.fieldtesttool.metrics.Metric;
import com.clearwire.tools.wimax.fieldtesttool.metrics.MetricsHelper;
import com.clearwire.tools.wimax.fieldtesttool.ping.PingEvent;
import com.clearwire.tools.wimax.fieldtesttool.ping.PingEventListener;
import com.clearwire.tools.wimax.fieldtesttool.ui.map.Base;
import com.clearwire.tools.wimax.fieldtesttool.ui.map.MapTagEvent;
import com.clearwire.tools.wimax.fieldtesttool.ui.map.MapTagEventListener;
import com.clearwire.tools.wimax.swin2.event.Swin2Event;
import com.clearwire.tools.wimax.swin2.event.Swin2EventListener;
import com.clearwire.tools.wimax.typeperf.TypeperfEvent;
import com.clearwire.tools.wimax.typeperf.TypeperfEventListener;
import com.openracesoft.devices.gps.GpsEvent;
import com.openracesoft.devices.gps.GpsEventListener;
import com.openracesoft.devices.gps.nmea.GpsFixDataEvent;

/**
 * @author jfry
 *
 */
public class MetricsUI extends JPanel implements GpsEventListener,
		BCS200EventListener, TypeperfEventListener,LoggerEventListener,Swin2EventListener,PingEventListener,MapTagEventListener,CPEi150EventListener {
	
	private Log log = LogFactory.getLog(MetricsUI.class);

	private MetricsTableModel metricsModel = new MetricsTableModel();
	private JTable metricsTable = new JTable(metricsModel);
	
	private DefaultTableModel macMsgModel = new DefaultTableModel();
	private JTable macMsgTable = new JTable(macMsgModel);

	private JScrollPane metricScrollPane = new JScrollPane(metricsTable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	private JScrollPane macMsgScrollPane = new JScrollPane(macMsgTable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	
	private double currLat;
	private double currLong;
	
	private Base currBase=null;
	
	private boolean logging = false;
	private PrintStream metricsLogFile=null;
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss:SSS");
	
	private String connectionStatus="Not Connected";
	
	JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	    	
	public MetricsUI(){
		super();
		
		splitPane.setOneTouchExpandable(true);
		add(splitPane);
		splitPane.add(metricScrollPane);
		Border metricsBorder = BorderFactory.createTitledBorder("Measurements:");
		metricScrollPane.setBorder(metricsBorder);
		TableColumnModel tcm = metricsTable.getColumnModel();
		tcm.getColumn(0).setMinWidth(150);
		tcm.getColumn(1).setMinWidth(250);
		
		Collection<Metric> metrics = MetricsHelper.getInstance().getAllMetrics();
		
		for(Iterator<Metric> iter = metrics.iterator();iter.hasNext();){
			Metric metric = (Metric)iter.next();
			metricsModel.addMetric(metric);
		}
		
		macMsgModel.addColumn("Time");
		macMsgModel.addColumn("Direction");
		macMsgModel.addColumn("Message");
		
		tcm = macMsgTable.getColumnModel();
		tcm.getColumn(0).setMinWidth(105);
		tcm.getColumn(1).setMinWidth(60);
		tcm.getColumn(2).setMinWidth(235);
		
		splitPane.add(macMsgScrollPane);
		Border macBorder = BorderFactory.createTitledBorder("Mac Messages:");
		macMsgScrollPane.setBorder(macBorder);
		splitPane.setPreferredSize(new Dimension(400,600));
		splitPane.setDividerLocation(500);
	}
	
	public void startNewLog(String filename) throws FileNotFoundException {
		synchronized(this){
			if(metricsLogFile!=null){
				try {
					metricsLogFile.close();
				}
				catch(Exception e){
					log.warn("WARN",e);
				}
			}
			metricsLogFile = new PrintStream(filename+"_stats.csv");
			
			for(int i=0;i<metricsModel.getRowCount();i++){
				metricsLogFile.print(metricsModel.getValueAt(i, 0));
				metricsLogFile.print(",");
			}
			metricsLogFile.println();
			logging=true;
		}
	}
	
	public void resumeLog(){
		logging=true;
	}
	
	private void logStatsData(){
		synchronized(metricsLogFile){
			for(int i=0;i<metricsModel.getRowCount();i++){
				metricsLogFile.print(metricsModel.getValueAt(i, 1));
				metricsLogFile.print(",");
			}
			metricsLogFile.println();
		}
	}
	
	public void stopLog(){
		logging=false;
	}
	
	public void closeLog(){
		if(metricsLogFile!=null){
			try {
				metricsLogFile.close();
			}
			catch(Exception e){
				log.warn("WARN",e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.clearwire.tools.wimax.fieldtesttool.logger.LoggerEventListener#eventOccurred(com.clearwire.tools.wimax.fieldtesttool.logger.LoggerEvent)
	 */
	public void eventOccurred(LoggerEvent loggerEvent) {
		// TODO Auto-generated method stub
		
	}
	
	

	/* (non-Javadoc)
	 * @see com.clearwire.tools.wimax.fieldtesttool.ping.PingEventListener#eventOccurred(com.clearwire.tools.wimax.fieldtesttool.ping.PingEvent)
	 */
	public void eventOccurred(PingEvent pingEvent) {
		pollTime();
		Metric pingMetric = MetricsHelper.getInstance().getMetricByProperty("Ping Latency");
		pingMetric.setValue(Integer.toString(pingEvent.getTime()));
		metricsModel.addMetric(pingMetric);
		if(logging)
			logStatsData();
	}

	/* (non-Javadoc)
	 * @see com.motoflexi.devices.gps.GpsEventListener#eventOccurred(com.motoflexi.devices.gps.GpsEvent)
	 */
	public void eventOccurred(GpsEvent gpsEvent) {
		pollTime();
		if(gpsEvent instanceof GpsFixDataEvent){
			GpsFixDataEvent gpsFixDataEvent = (GpsFixDataEvent)gpsEvent;
			Metric latMetric = MetricsHelper.getInstance().getMetricByName("Latitude");
			latMetric.setValue(Double.toString(gpsFixDataEvent.getLatitude()));
			metricsModel.addMetric(latMetric);
			Metric longMetric = MetricsHelper.getInstance().getMetricByName("Longitude");
			longMetric.setValue(Double.toString(gpsFixDataEvent.getLongitude()));
			metricsModel.addMetric(longMetric);
			currLat=gpsFixDataEvent.getLatitude();
			currLong=gpsFixDataEvent.getLongitude();
			
			//add distance from base
			double distance = distance(currLat,currLong,currBase.getLatitude(),currBase.getLongitude(),'K');
			
			Metric distanceMetric = MetricsHelper.getInstance().getMetricByProperty("distance");
			distanceMetric.setValue(Double.toString(distance));
			metricsModel.addMetric(distanceMetric);
			
			if(logging)
				logStatsData();
			
			//check for IP address
			//checkIpAddress(true);
		}
	}
	
	private void pollTime(){
		Date now = new Date();
		Metric pollTimeMetric = MetricsHelper.getInstance().getMetricByProperty("pollTime");
		pollTimeMetric.setValue(dateFormat.format(now));
		metricsModel.addMetric(pollTimeMetric);
		
		Metric timeSinceMetric = MetricsHelper.getInstance().getMetricByProperty("timeSince");
		timeSinceMetric.setValue(Long.toString(now.getTime()));
		metricsModel.addMetric(timeSinceMetric);
	}

	/* (non-Javadoc)
	 * @see com.clearwire.tools.wimax.CPEi150.event.CPEi150EventListener#eventOccurred(com.clearwire.tools.wimax.CPEi150.event.CPEi150StatsEvent)
	 */
	public void eventOccurred(CPEi150StatsEvent cpei150StatsEvent) {
		pollTime();
		if(cpei150StatsEvent instanceof CPEi150StatsEvent){
			log.debug("Received cpei150 baseband stats event.");
			Hashtable<String,String> metrics = cpei150StatsEvent.getMetrics();
			//MetricsHelper.getInstance().getMetricByProperty(property)
			String baseid = metrics.get("BS ID");
			if(baseid!=null){
				Base base = BasesHelper.getInstance().getBaseByBaseId(baseid.toLowerCase());
				if(base!=null){
					metrics.put("BaseIDPost", baseid+" ("+base.getText()+")");
					if((currBase!=null)&&(currBase!=base))
						metrics.put("Tag", "HO Event");
					else
						metrics.put("Tag", "");
					currBase=base;
				}
				else 
					metrics.put("BaseIDPost", baseid);
			}
			addMetrics(metrics);
		}		
	}

	/* (non-Javadoc)
	 * @see com.clearwire.tools.wimax.bcs200.event.BCS200EventListener#eventOccurred(com.clearwire.tools.wimax.bcs200.event.BCS200Event)
	 */
	public void eventOccurred(BCS200Event bcs200Event) {
		pollTime();
		if(bcs200Event instanceof BasebandStatEvent){
			log.debug("Received bcs200 baseband stats event.");
			Hashtable<String,String> metrics = bcs200Event.getMetrics();
			
			String baseid = metrics.get("BaseID");
			if(baseid!=null){
				Base base = BasesHelper.getInstance().getBaseByBaseId(baseid.toLowerCase());
				if(base!=null){
					metrics.put("BaseIDPost", baseid+" ("+base.getText()+")");
					if((currBase!=null)&&(currBase!=base))
						metrics.put("Tag", "HO Event");
					else
						metrics.put("Tag", "");
					currBase=base;
				}
				else 
					metrics.put("BaseIDPost", baseid);
			}
			addMetrics(metrics);
		}
		else if(bcs200Event instanceof MacMsgEvent){
			log.debug("Received mac msg event.");
			Hashtable<String,String> data = bcs200Event.getMetrics();
			log.trace("MacMsgEvent received:"+data.keySet().toString());
			log.trace("MacMsgEvent received:"+data.values().toString());
			Vector<String> row = new Vector<String>();
			String time = data.get("Time");
			String msgDirection = data.get("MESSAGE DIRECTION");
			String msgText = data.get("MESSAGE");
			row.add(time);
			row.add(msgDirection);
			row.add(msgText);
			Metric macMsgDirection = MetricsHelper.getInstance().getMetricByProperty("Mac Msg Direction");
			Metric macMsgText = MetricsHelper.getInstance().getMetricByProperty("Mac Msg Text");
			if((macMsgDirection!=null)&&(macMsgText!=null)){
				macMsgDirection.setValue(msgDirection);
				macMsgText.setValue(msgText);
				metricsModel.addMetric(macMsgDirection);
				metricsModel.addMetric(macMsgText);
			}
			macMsgModel.addRow(row);
			//do some cleanup
			if(macMsgModel.getRowCount()>50)
				macMsgModel.removeRow(0);
			macMsgTable.scrollRectToVisible(macMsgTable.getCellRect(macMsgModel.getRowCount()-1,0,true));
		}
		else {
			log.debug("Received generic bs200 event.");
			Hashtable<String,String> metrics = bcs200Event.getMetrics();
			addMetrics(metrics);
		}
		
		//check for IP address
		checkLocalIpAddress(false);
		
		if(logging)
			logStatsData();
	}
	
	public void eventOccurred(Swin2Event swin2Event) {
		pollTime();
		log.debug("Received swin2 event.");
		Hashtable<String,String> metrics = swin2Event.getMetrics();
		addMetrics(metrics);
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.clearwire.tools.wimax.fieldtesttool.ui.map.MapTagEventListener#eventOccurred(com.clearwire.tools.wimax.fieldtesttool.ui.map.MapTagEvent)
	 */
	public void eventOccurred(MapTagEvent tagEvent) {
		pollTime();
		Metric tagMetric = MetricsHelper.getInstance().getMetricByProperty("Tag");
		tagMetric.setValue(tagEvent.getTag());
		metricsModel.addMetric(tagMetric);
		if(logging)
			logStatsData();
		tagMetric.setValue("");
		metricsModel.addMetric(tagMetric);
	}

	private void checkLocalIpAddress(boolean audio){
		Metric ipMetric = MetricsHelper.getInstance().getMetricByProperty("clientIP");
		if (ipMetric!=null) {
			try {
				String clientIP = InetAddress.getLocalHost().getHostAddress();
				if(clientIP!=null){
					ipMetric.setValue(clientIP);
					metricsModel.addMetric(ipMetric);
					Metric netEntryStatusMetric  = MetricsHelper.getInstance().getMetricByProperty("NETENTRY STATUS");
					if((netEntryStatusMetric!=null)&&(clientIP.equals("127.0.0.1"))){
						netEntryStatusMetric.setValue("Not Connected");
						if(connectionStatus.equals("Connected")){
							if(audio){
								try {
									AudioHelper.getInstance().playAudio("Network Drop");
								}
								catch(Exception e){
									log.error("ERROR",e);
								}
							}
							connectionStatus="Not Connected";
						}
					}
					else {
						netEntryStatusMetric.setValue("Connected");
						if(connectionStatus.equals("Not Connected")){
							if(audio){
								try {
									AudioHelper.getInstance().playAudio("Network Entry");
								}
								catch(Exception e){
									log.error("ERROR",e);
								}
							}
							connectionStatus="Connected";
						}
					}
				}
				else {
					ipMetric.setValue("No IP");
					metricsModel.addMetric(ipMetric);
				}
			}
			catch(Exception e){
				log.warn("WARN",e);
				ipMetric.setValue("No IP");
				metricsModel.addMetric(ipMetric);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.clearwire.tools.wimax.typeperf.TypeperfEventListener#eventOccurred(com.clearwire.tools.wimax.typeperf.TypeperfEvent)
	 */
	public void eventOccurred(TypeperfEvent typeperfEvent) {
		log.debug("Received typerf event.");
		pollTime();
		Hashtable<String,String> metrics = typeperfEvent.getMetrics();
		log.trace("Typeperf trace: "+metrics);
		addMetrics(metrics);
		
		//check for IP address
		//checkIpAddress(false);
		
		if(logging)
			logStatsData();
	}
	
	private void addMetrics(Hashtable<String,String> metrics){
		for(Iterator<String> iter = metrics.keySet().iterator();iter.hasNext();){
			String property = iter.next();
			String value = metrics.get(property);
			addMetric(property,value);
		}
	}
	
	private void addMetric(String property,String value){
		Metric metric = MetricsHelper.getInstance().getMetricByProperty(property);
		if(metric!=null){
			metric.setValue(value);
			metricsModel.addMetric(metric);
		}
		else {
			log.trace("Property "+property+" is not a valid metric.");
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	class MetricsTableModel extends AbstractTableModel {
		
		private Hashtable<String,Metric> metrics = new Hashtable<String,Metric>();
		
		MetricsTableModel(){
			super();
		}
		
		public void addMetric(Metric metric){
			metrics.put(metric.getName(), metric);
			this.fireTableDataChanged();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getColumnCount()
		 */
		public int getColumnCount() {
			return 2;
		}
		
		

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getRowCount()
		 */
		public int getRowCount() {
			return metrics.size();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getColumnName(int)
		 */
		public String getColumnName(int column) {
			if(column==0)
				return "Metric";
			else
				return "Value";
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int row, int column) {
			ArrayList<Metric> sortedMetrics = new ArrayList<Metric>(metrics.values());
			Collections.sort(sortedMetrics);
			
			if(column==0)
				return sortedMetrics.get(row).getName();
			else
				return sortedMetrics.get(row).getValue();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}
	
	private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
	  double theta = lon1 - lon2;
	  double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
	  dist = Math.acos(dist);
	  dist = Math.toDegrees(dist);
	  dist = dist * 60 * 1.1515;
	  if (unit == 'K') {
	    dist = dist * 1.609344;
	  } else if (unit == 'N') {
	  	dist = dist * 0.8684;
	    }
	  return (dist);
	}
}
