/**
 * 
 */
package com.clearwire.tools.wimax.fieldtesttool.ui.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.clearwire.tools.wimax.CPEi150.event.CPEi150EventListener;
import com.clearwire.tools.wimax.CPEi150.event.CPEi150StatsEvent;
import com.clearwire.tools.wimax.bcs200.event.BCS200Event;
import com.clearwire.tools.wimax.bcs200.event.BCS200EventListener;
import com.clearwire.tools.wimax.bcs200.event.BasebandStatEvent;
import com.clearwire.tools.wimax.fieldtesttool.bases.BasesHelper;
import com.clearwire.tools.wimax.fieldtesttool.configuration.ConfigurationHelper;
import com.clearwire.tools.wimax.fieldtesttool.logger.LoggerEvent;
import com.clearwire.tools.wimax.fieldtesttool.logger.LoggerEventListener;
import com.clearwire.tools.wimax.fieldtesttool.map.MapsHelper;
import com.clearwire.tools.wimax.fieldtesttool.metrics.DisplayMetric;
import com.clearwire.tools.wimax.fieldtesttool.metrics.MetricsHelper;
import com.clearwire.tools.wimax.typeperf.TypeperfEvent;
import com.clearwire.tools.wimax.typeperf.TypeperfEventListener;
import com.openracesoft.devices.gps.GpsEvent;
import com.openracesoft.devices.gps.GpsEventListener;
import com.openracesoft.devices.gps.nmea.GpsFixDataEvent;

/**
 * @author jfry
 *
 */
public class MapUI extends JPanel implements CPEi150EventListener,GpsEventListener,BCS200EventListener,TypeperfEventListener,LoggerEventListener,ActionListener,ChangeListener,MouseListener {
	
	private Log log = LogFactory.getLog(MapCanvas.class);
		
	private MapCanvas mapCanvas;
	
	private HandoverTableModel hoCountModel = new HandoverTableModel();
	private JTable hoCountTable = new JTable(hoCountModel);
	
	private JLabel displayMetricLabel = new JLabel("Display Metric:");
	private JComboBox displayMetric = new JComboBox();
	
	private JLabel mapSelectionLabel = new JLabel("Select Map:");
	private JComboBox mapSelection= new JComboBox();
	
	private JButton clearMap = new JButton("Clear Map");
	private JButton clearHandovers = new JButton("Clear Handovers");
	private JButton zoomIn = new JButton(new ImageIcon(java.net.URLClassLoader.getSystemResource("ZoomIn16.gif")));
	private JButton zoomOut = new JButton(new ImageIcon(java.net.URLClassLoader.getSystemResource("ZoomOut16.gif")));

	private JButton tag = new JButton("Tag!");
	
	private JLabel positionSizeLabel = new JLabel("Position Size:");
	private JSpinner positionSize = new JSpinner(new SpinnerNumberModel(7,1,20,1));

	private JLabel sectorSizeLabel = new JLabel("Sector Size:");
	private JSpinner sectorSize = new JSpinner(new SpinnerNumberModel(1.0,0.25,15.0,0.1));
	
	private JCheckBox mapCentering = new JCheckBox("Map Centering",true);
	private JCheckBox colorScale = new JCheckBox("Color Scale",true);
	private boolean zoomGo=true;
	
	private ArrayList<MapTagEventListener> mapTagListeners = new ArrayList<MapTagEventListener>();
	
	public MapUI(){
		super(new BorderLayout());
		loadMaps();
		
		//north panel contains pulldowns for map, base, and display
		JToolBar toolBar = new JToolBar();
		JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		toolBar.add(northPanel);
		Box northLeftPanel = new Box(BoxLayout.Y_AXIS);
		JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel middleRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		northLeftPanel.add(topRow);
		northLeftPanel.add(middleRow);
		northLeftPanel.add(bottomRow);
		northPanel.add(northLeftPanel);
		
		JPanel northRightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		Box handoverBox = new Box(BoxLayout.Y_AXIS);
		northRightPanel.add(handoverBox);
		JScrollPane hoScrollPane = new JScrollPane(hoCountTable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		handoverBox.add(hoScrollPane);
		handoverBox.add(clearHandovers);
		clearHandovers.addActionListener(this);
		northPanel.add(northRightPanel);
		TableColumnModel tcm = hoCountTable.getColumnModel();
		tcm.getColumn(0).setPreferredWidth(175);
		tcm.getColumn(1).setPreferredWidth(50);
		hoScrollPane.setPreferredSize(new Dimension(225,100));
		
		topRow.add(mapSelectionLabel);
		topRow.add(mapSelection);
		mapSelection.addActionListener(this);
		
		for(Iterator<DisplayMetric> iter = MetricsHelper.getInstance().getDisplayMetrics().iterator();iter.hasNext();){
			DisplayMetric displayMetricValue = (DisplayMetric)iter.next();
			displayMetric.addItem(displayMetricValue);
		}
		
		topRow.add(displayMetricLabel);
		topRow.add(displayMetric);
		displayMetric.addActionListener(this);

		middleRow.add(positionSizeLabel);
		middleRow.add(positionSize);
		positionSize.addChangeListener(this);
		
		middleRow.add(sectorSizeLabel);
		middleRow.add(sectorSize);
		middleRow.add(new JLabel("Km"));
		sectorSize.addChangeListener(this);
		
		middleRow.add(clearMap);
		clearMap.addActionListener(this);
		
		bottomRow.add(mapCentering);
		mapCentering.addActionListener(this);

		bottomRow.add(colorScale);
		colorScale.addActionListener(this);

		bottomRow.add(zoomIn);
		zoomIn.addMouseListener(this);
		
		bottomRow.add(zoomOut);
		zoomOut.addMouseListener(this);
		
		bottomRow.add(tag);
		tag.addActionListener(this);
		
		//bottomRow.add(ping);
		//ping.addActionListener(this);
		                                                                                                            
		DisplayMetric selDisplayMetric = (DisplayMetric)displayMetric.getItemAt(0);
		mapCanvas = new MapCanvas(selDisplayMetric);
		
		Map selMap = (Map)mapSelection.getItemAt(0);
		try {
			mapCanvas.loadMap(selMap);
		}
		catch(IOException ie){
			JOptionPane.showMessageDialog(this, "Unable to load map file: "+ie.getMessage()+". Please verify the file path to: "+selMap.getName(),"ERROR",JOptionPane.ERROR_MESSAGE,null);															
		}
		add(toolBar,BorderLayout.NORTH);
		//JScrollPane scrollPane = new JScrollPane(mapCanvas,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		add(mapCanvas,BorderLayout.CENTER);
		Border mapBorder = BorderFactory.createTitledBorder("Map View:");
		mapCanvas.setBorder(mapBorder);
		loadColorScale();
		mapCanvas.addBases(BasesHelper.getInstance().getBases());
	}
	
	
	
	public void reloadBases(){
		mapCanvas.clearBases();
		mapCanvas.addBases(BasesHelper.getInstance().getBases());
		mapCanvas.invalidate();
		mapCanvas.repaint();
	}
	
	public String getSelectedMapName(){
		return mapSelection.getSelectedItem().toString();
	}
    
    public void loadMaps(){
    	mapSelection.removeAllItems();
    	Map[] maps = MapsHelper.getInstance().getMapArray();
    	for(int i=0;i<maps.length;i++){
    		Map newMap = maps[i];
    		mapSelection.addItem(newMap);
    	}
    }
	
	public void clearMap(){
		mapCanvas.clearMap();
	}
	
	public void clearHandovers(){
		hoCountModel.clearHandovers();
	}
    
    private void loadColorScale(){
    	List<HierarchicalConfiguration> fields = ConfigurationHelper.getInstance().getConfig().configurationsAt("//color-scale/color");
		
		ArrayList<Color> colors = new ArrayList<Color>();
		for(Iterator<HierarchicalConfiguration> it = fields.iterator(); it.hasNext();)
		{
			try {
			    HierarchicalConfiguration sub = (HierarchicalConfiguration) it.next();
			    // sub contains now all data about a single field
			    String color = sub.getString("@value","FF3333");
			    
			    colors.add(Color.decode(color));
			}
			catch(Exception e){
				log.error("ERROR",e);
			}
		}
		mapCanvas.setColorScale(colors);
    }
    
    /* (non-Javadoc)
	 * @see com.clearwire.tools.wimax.typeperf.TypeperfEventListener#eventOccurred(com.clearwire.tools.wimax.typeperf.TypeperfEvent)
	 */
	public void eventOccurred(TypeperfEvent typeperfEvent) {
		mapCanvas.processDisplayMetric(typeperfEvent.getMetrics());
	}

	/* (non-Javadoc)
	 * @see com.clearwire.tools.wimax.bcs200.event.BCS200EventListener#eventOccurred(com.clearwire.tools.wimax.bcs200.event.BCS200Event)
	 */
	public void eventOccurred(BCS200Event bcs200Event) {
		if(bcs200Event instanceof BasebandStatEvent){
			boolean connected=true;
			try {
				String clientIP = InetAddress.getLocalHost().getHostAddress();
				if((clientIP!=null)&&(!clientIP.equalsIgnoreCase("127.0.0.1")))
					connected=true;
				else
					connected=false;
			}
			catch(Exception e){
				connected=false;
			}
			String handoverResult = mapCanvas.processBaseId(bcs200Event.getMetrics(),connected);
			if(handoverResult!=null)
				hoCountModel.putHandover(handoverResult);
			mapCanvas.processDisplayMetric(bcs200Event.getMetrics());
		}
	}
	
	/* (non-Javadoc)
	 * @see com.clearwire.tools.wimax.CPEi150.event.CPEi150EventListener#eventOccurred(com.clearwire.tools.wimax.CPEi150.event.CPEi150StatsEvent)
	 */
	public void eventOccurred(CPEi150StatsEvent cpei150Event) {
		if(cpei150Event instanceof CPEi150StatsEvent){
			mapCanvas.processBaseId(cpei150Event.getMetrics(),true);
			mapCanvas.processDisplayMetric(cpei150Event.getMetrics());
		}
	}



	/* (non-Javadoc)
	 * @see com.clearwire.tools.wimax.fieldtesttool.logger.LoggerEventListener#eventOccurred(com.clearwire.tools.wimax.fieldtesttool.logger.LoggerEvent)
	 */
	public void eventOccurred(LoggerEvent typeperfEvent) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.openracesoft.devices.gps.GpsEventListener#eventOccurred(com.openracesoft.devices.gps.GpsEvent)
	 */
	public void eventOccurred(GpsEvent gpsEvent) {
		if(gpsEvent instanceof GpsFixDataEvent){
			GpsFixDataEvent gpsFixDataEvent = (GpsFixDataEvent)gpsEvent;
			mapCanvas.processGpsCoordinates(gpsFixDataEvent.getLatitude(), gpsFixDataEvent.getLongitude());
		}
	}



	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		zoomGo=true;
		if(e.getSource().equals(zoomIn)){
			Thread zoomThread = new Thread(){
				public void run(){
					while(zoomGo){
						mapCanvas.zoomIn();
						try {
							Thread.sleep(250);
						}
						catch(Exception e){}
					}
					
				}
			};
			zoomThread.start();
			
		}
		else if(e.getSource().equals(zoomOut)){
			Thread zoomThread = new Thread(){
				public void run(){
					while(zoomGo){
						mapCanvas.zoomOut();
						try {
							Thread.sleep(250);
						}
						catch(Exception e){}
					}
					
				}
			};
			zoomThread.start();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		zoomGo=false;
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(mapSelection)){
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			try {
				log.debug("Loading map "+mapSelection.getSelectedItem());
				
				Map map = (Map)mapSelection.getSelectedItem();
				mapCanvas.loadMap(map);
				
			}
			catch(IOException ie){
				log.error("ERROR",ie);
				JOptionPane.showMessageDialog(this, "Unable to load map file: "+ie.getMessage()+". Please verify the file path to: "+mapSelection.getSelectedItem(),"ERROR",JOptionPane.ERROR_MESSAGE,null);															
			}
			this.setCursor(Cursor.getDefaultCursor());
		}
		else if(e.getSource().equals(displayMetric)){
			mapCanvas.setDisplayMetric((DisplayMetric)displayMetric.getSelectedItem());
		}
		else if(e.getSource().equals(mapCentering)){
			mapCanvas.setMapCentering(mapCentering.isSelected());
		}
		else if(e.getSource().equals(colorScale)){
			mapCanvas.setShowColorScale(colorScale.isSelected());
		}
		else if(e.getSource().equals(tag)){
			String tagText = JOptionPane.showInputDialog(this, "Tag the current position with the following text.", "Tag the Current Position", JOptionPane.PLAIN_MESSAGE);
			if((tagText!=null)&&(tagText.trim().length()>0)){
				mapCanvas.tagCurrentPosition(tagText.trim());
				fireMapTagEventOccurred(new MapTagEvent(this,tagText.trim()));
			}
		}
		else if(e.getActionCommand().equals("Clear Map")){
			int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear the map?","Clear Map",JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,new ImageIcon(java.net.URLClassLoader.getSystemResource("favicon.jpg")));
			if(option==JOptionPane.OK_OPTION)
				clearMap();
		}
		else if(e.getActionCommand().equals("Clear Handovers")){
			int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear the handovers?","Clear Handovers",JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,new ImageIcon(java.net.URLClassLoader.getSystemResource("favicon.jpg")));
			if(option==JOptionPane.OK_OPTION)
				clearHandovers();
		}
		this.repaint();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		if(e.getSource().equals(positionSize)){
			mapCanvas.setPositionSize((Integer)positionSize.getValue());
			mapCanvas.repaint();
		}
		else if(e.getSource().equals(sectorSize)){
			mapCanvas.setSectorSize((Double)sectorSize.getValue());
			mapCanvas.repaint();
		}
		
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see com.clearwire.tools.wimax.fieldtesttool.ping.MapTagEventListener#eventOccurred(com.clearwire.tools.wimax.fieldtesttool.ping.MapTagEvent)
	 *
	public void eventOccurred(MapTagEvent pingEvent) {
		try {
			if(pingEvent.getTime()==-1)
				AudioHelper.getInstance().playAudio("Pong");
			else
				AudioHelper.getInstance().playAudio("MapTag");
		}
		catch(Exception e){
			log.error("ERROR",e);
		}
		
	}*/
	
	public void fireMapTagEventOccurred(MapTagEvent event){
		log.debug("Firing MapTag event.");
		for(Iterator<MapTagEventListener> iter = mapTagListeners.iterator();iter.hasNext();){
			MapTagEventListener listener = (MapTagEventListener)iter.next();
			listener.eventOccurred(event);
		}
	}
	
	public void addMapTagListener(MapTagEventListener listener){
		mapTagListeners.add(listener);
	}
	
	public void removeMapTagListener(MapTagEventListener listener){
		mapTagListeners.remove(listener);
	}

	public static void main(String[] args){
	   	 try {
		        JFrame f = new JFrame();
		        MapUI mapUI = new MapUI();
	
		        f.add(mapUI);
		        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		        f.setSize(400, 400);
		        f.setVisible(true);
	 	 }
	 	 catch(Exception e){
	 		 e.printStackTrace();
	 	 }
	}
	
	class HandoverTableModel extends AbstractTableModel {
		
		private Hashtable<String,Integer> handovers = new Hashtable<String,Integer>();
		
		HandoverTableModel(){
			super();
		}
		
		public void clearHandovers(){
			handovers.clear();
			this.fireTableDataChanged();
		}
		
		public void putHandover(String handover){
			Integer handoverCount = handovers.get(handover);
			if(handoverCount==null)
				handovers.put(handover, 1);
			else
				handovers.put(handover, (handoverCount+1));
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
			return handovers.size();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getColumnName(int)
		 */
		public String getColumnName(int column) {
			if(column==0)
				return "Handover";
			else
				return "Count";
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int row, int column) {
			ArrayList<String> handoverLabels = new ArrayList<String>(handovers.keySet());
			Collections.sort(handoverLabels);
			
			if(column==0)
				return handoverLabels.get(row);
			else
				return handovers.get(handoverLabels.get(row));
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}
}
