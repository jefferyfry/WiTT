package com.clearwire.tools.wimax.fieldtesttool.ui.map;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.awt.image.codec.JPEGImageEncoderImpl;

import com.clearwire.tools.wimax.fieldtesttool.audio.AudioHelper;
import com.clearwire.tools.wimax.fieldtesttool.bases.BasesHelper;
import com.clearwire.tools.wimax.fieldtesttool.configuration.ConfigurationHelper;
import com.clearwire.tools.wimax.fieldtesttool.metrics.DisplayMetric;
import com.clearwire.tools.wimax.fieldtesttool.metrics.Metric;
import com.clearwire.tools.wimax.fieldtesttool.metrics.MetricsHelper;
import com.clearwire.tools.wimax.fieldtesttool.tif.TifIO;

public class MapCanvas extends JLabel implements MouseListener,ActionListener,MouseMotionListener {
	
	private Log log = LogFactory.getLog(MapCanvas.class);

	//map properties
    private BufferedImage map=null;
    private double mapNwLat;
    private double mapNwLong;
    private double mapSeLat;
    private double mapSeLong;
    
    private int bufferSize=5000;
    
    //base flashing
    int flashIndex=1;

    //scaling
    private double scale = 1.0;
    
    //current center of the visible rect
    private double centerX=-1;
    private double centerY=-1;
    
    //the current position
    private int currX=-1;
    private int currY=-1;
    
    //the prior position
    private int lastX=-1;
    private int lastY=-1;
    
    //mouse dragging
    private int mouseX=-9999;
    private int mouseY=-9999;
    private int mouseDx=0;
    private int mouseDy=0;
    
    private boolean connected;
    
    
    private boolean mapCentering=true;
    
    private DisplayMetric displayMetric=null;
    private double sectorSize=1.0;
    private double km2PxFactor = 1.0;
    
    private int positionSize=7;
    
    private ArrayList<Base> bases = new ArrayList<Base>();
    private ArrayList<MapGraphic> positions = new ArrayList<MapGraphic>();
    private ArrayList<Color> colorScale = new ArrayList<Color>();
        
    private Color currColor=null;
    
    private Base currBase=null;
    private MapGraphic currPos=null;
    
    private boolean showColorScale=true;
    
    private JPopupMenu popup;
    
    private String connectionStatus = "Not Connected";
    
    /**
	 * @param displayMetric
	 */
	public MapCanvas(DisplayMetric displayMetric) {
		super();
		this.displayMetric = displayMetric;
		
		popup = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Save Image");
	    menuItem.addActionListener(this);
	    popup.add(menuItem);
	    this.addMouseListener(this);
	    this.addMouseMotionListener(this);
		SubnodeConfiguration gps = ConfigurationHelper.getInstance().getConfig().configurationAt("//gps");
		int bufferSize = gps.getInt("@buffer-size",5000);
		this.bufferSize=bufferSize;
	}
	
	public void saveImage() {
		String filename=null;
		try {
			JFileChooser dialog = new JFileChooser();
			
			int ret = dialog.showSaveDialog(this);
			if(ret==JFileChooser.APPROVE_OPTION) {
				BufferedImage image = new BufferedImage(this.getWidth(),this.getHeight(),BufferedImage.TYPE_INT_RGB);
				Graphics g = image.getGraphics();
				this.printAll(g);
				filename = dialog.getSelectedFile()+".jpg";
				FileOutputStream out = new FileOutputStream(filename);
				JPEGImageEncoderImpl j = new JPEGImageEncoderImpl(out);
				j.encode(image);
				out.close();
			}
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(this, "Unable to save map image file "+filename+": "+e.getMessage(),"ERROR",JOptionPane.ERROR_MESSAGE,null);									
		}
	}
	
	
	
	/**
	 * @return the bufferSize
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param bufferSize the bufferSize to set
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * @return the showColorScale
	 */
	public boolean isShowColorScale() {
		return showColorScale;
	}

	/**
	 * @param showColorScale the showColorScale to set
	 */
	public void setShowColorScale(boolean showColorScale) {
		this.showColorScale = showColorScale;
	}

	/**
	 * @return the mapCentering
	 */
	public boolean isMapCentering() {
		return mapCentering;
	}

	/**
	 * @param mapCentering the mapCentering to set
	 */
	public void setMapCentering(boolean mapCentering) {
		if(!mapCentering){
			lastX=currX;
			lastY=currY;
		}

		this.mapCentering = mapCentering;
	}

	/**
	 * @return the displayMetric
	 */
	public DisplayMetric getDisplayMetric() {
		return displayMetric;
	}

	/**
	 * @param displayMetric the displayMetric to set
	 */
	public void setDisplayMetric(DisplayMetric displayMetric) {
		this.displayMetric = displayMetric;
	}

	/**
	 * @return the colorScale
	 */
	public ArrayList<Color> getColorScale() {
		return colorScale;
	}
	
	public void tagCurrentPosition(String text){
		if(currPos!=null)
			currPos.setText(text);
	}
	
	/**
	 * @param colorScale the colorScale to set
	 */
	public void setColorScale(ArrayList<Color> colorScale) {
		this.colorScale = colorScale;
	}
	
	/**
	 * @return the sectorSize
	 */
	public double getSectorSize() {
		return sectorSize;
	}

	/**
	 * @param sectorSize the sectorSize to set
	 */
	public void setSectorSize(double sectorSize) {
		this.sectorSize = sectorSize;
	}

	/**
	 * @return the positionSize
	 */
	public int getPositionSize() {
		return positionSize;
	}

	/**
	 * @param positionSize the positionSize to set
	 */
	public void setPositionSize(int positionSize) {
		this.positionSize = positionSize;
	}

	public void addBase(Base base){
		bases.add(base);
	}
	
	public void addBases(Collection<Base> bases){
		this.bases.addAll(bases);
	}
	
	public void clearBases(){
		bases.clear();
	}

	public void loadMap(Map map) throws IOException,FileNotFoundException {
		if(map==null)
			return;
		String file = map.getFileUrl();
		URL fileUrl=null;
		
		if(file.startsWith("classpath:"))
			fileUrl = ClassLoader.getSystemResource(file.substring(file.indexOf(":")+1));
		else 
			fileUrl = new URL(file);
		
		log.debug("Loaded map from file "+fileUrl);
		
		if(file.endsWith(".jpg"))
			this.map = ImageIO.read(fileUrl);
		else
			this.map = TifIO.read(fileUrl);
        
        log.debug("Map image read.");
        
        this.mapNwLat=map.getNwLatitude();
        this.mapNwLong=map.getNwLongitude();
        this.mapSeLat=map.getSeLatitude();
        this.mapSeLong=map.getSeLongitude();
        
        //calculate km to pixels factor
        int height = this.map.getHeight();
        int width = this.map.getWidth();
        double diagPx = Math.sqrt((height*height+width*width));
        double diagKm = distance(mapNwLat,mapNwLong,mapSeLat,mapSeLong,'K');
        km2PxFactor = diagPx/diagKm;
        
        //reset the coordinates and scaling
        resetMap();
        log.debug("Map coords reset.");

        this.repaint();
        log.debug("Canvas repainted.");
    }
	
	private void resetMap(){
		currX=-1;
        currY=-1;
        centerX=-1;
        centerY=-1;
        scale=1.0;
	}
	
	public void zoomIn(){
		scale=scale+0.05;
		this.repaint();
	}
	
	public void zoomOut(){
		if((scale-0.05)>0){
			scale=scale-0.05;
			this.repaint();
		}
	}

	@Override
    public void paint(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		
		g.setColor(Color.DARK_GRAY);
		g.fillRect(this.getVisibleRect().x, this.getVisibleRect().y, (int)this.getVisibleRect().getWidth(), (int)this.getVisibleRect().getHeight());
						
		AffineTransform origTx = g2d.getTransform();
		
		if(mapCentering)
			centerMapOnCurrent(g2d);
		else
			centerMapOnLast(g2d);
		
		//tx.setToTranslation(0, 0);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		//draw base map
		g2d.drawImage(map,0,0,this);
		
		//draw bases
		for(Iterator<Base> iter = bases.iterator();iter.hasNext();){
			Base base = (Base)iter.next();
			if(base.isDisplay())
				drawBase(g2d,base);
		}
		
		//draw positions
		for(Iterator<MapGraphic> iter = positions.iterator();iter.hasNext();){
			MapGraphic position = (MapGraphic)iter.next();
			drawPosition(g2d,position);
		}
		
		//apply back original transform before putting on draw scale
		g2d.setTransform(origTx);
		
		//draw color scale
		if(showColorScale)
			drawColorScale(g2d);
		
     }
	
	private void centerMapOnCurrent(Graphics2D g2d){
		AffineTransform tx = g2d.getTransform();
		
		//calculate the center of he visible rectangle
		centerX=this.getVisibleRect().getWidth()/2;
		centerY=this.getVisibleRect().getHeight()/2;
		
		
		//for first time, the curr location is the center of the map image
		if((currX==-1)||(currY==-1)){
			currX=map.getWidth()/2;
			currY=map.getHeight()/2;
		}
		
		//now translate the position to the center
		double dx = centerX-currX;
		double dy = centerY-currY;
		
		//centers, moves view port over the current location
		tx.translate(dx, dy);
		
		//now doing scaling.  move the origin 0,0 to the current location, scale and then move back
		tx.translate(currX,currY);
		
		tx.scale(scale, scale);
		
		tx.translate(-currX,-currY);
		
		//apply transform after scaling and centering
		g2d.setTransform(tx);
	}
	
	private void centerMapOnLast(Graphics2D g2d){
		AffineTransform tx = g2d.getTransform();
		
		//calculate the center of he visible rectangle
		centerX=this.getVisibleRect().getWidth()/2;
		centerY=this.getVisibleRect().getHeight()/2;
		
		
		//for first time, the last location is the center of the map image
		if((lastX==-1)||(lastY==-1)){
			lastX=map.getWidth()/2;
			lastY=map.getHeight()/2;
		}
		
		//now translate the position to the center
		lastX=lastX-mouseDx;
		lastY=lastY-mouseDy;
		double dx = centerX-lastX;
		double dy = centerY-lastY;
		
		//centers, moves view port over the last location
		tx.translate(dx, dy);
		
		//now doing scaling.  move the origin 0,0 to the last location, scale and then move back
		tx.translate(lastX,lastY);
		
		tx.scale(scale, scale);
		
		tx.translate(-lastX,-lastY);
		
		//apply transform after scaling and centering
		g2d.setTransform(tx);
	}
	
	private void drawColorScale(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		Composite originalComposite = g2d.getComposite();
	    int type = AlphaComposite.SRC_OVER;
	    
	    Composite alphaComp = AlphaComposite.getInstance(type, 0.65F);
	    g2d.setComposite(alphaComp);
	    g2d.setPaint(Color.gray);
	    g2d.fillRect(10,5,150,(colorScale.size()+1)*20);
	    g2d.setComposite(originalComposite);
	    g2d.draw3DRect(10,5,150,(colorScale.size()+1)*20, true);
	    
		DecimalFormat format = new DecimalFormat("#0.00");
		int y=40;
		Color[] colors = new Color[colorScale.size()];
		colors = colorScale.toArray(colors);
		double step = (displayMetric.getMax()-displayMetric.getMin())/(double)colors.length;
		g.setColor(Color.BLACK);
		g.drawString(displayMetric.getName(), 20, 20);
		for(int i=0;i<colors.length;i++){
			double start = displayMetric.getMax()-(i+1)*step;
			double end = displayMetric.getMax()-(i+1)*step+step;
			g.setColor(colors[i]);
			g.drawString(format.format(start)+" - "+format.format(end), 20, y+(i*20));
		}
	}
	
	private void drawBase(Graphics2D g2d,Base base){
		int siteX = translateLongitudeToXCoord(base.getLongitude());
		int siteY = translateLatitudeToYCoord(base.getLatitude());
		
		int sectorSizePx = (int)(km2PxFactor * sectorSize);
		
		if(base.getColor()!=null)
			g2d.setColor(base.getColor());
		
		int beamwidth = base.getBeamwidth();
		int halfBeam = base.getBeamwidth()/2;
		
		int azimuthXpoint = (int)(siteX+Math.cos(Math.toRadians(90-base.getAzimuth()))*sectorSizePx);
		int azimuthYpoint = (int)(siteY-Math.sin(Math.toRadians(90-base.getAzimuth()))*sectorSizePx);
	
		int azimuthXpoint1 = (int)(siteX+Math.cos((int)(90-base.getAzimuth()+halfBeam)/180.0*Math.PI)*sectorSizePx);
		int azimuthYpoint1 = (int)(siteY-Math.sin((int)(90-base.getAzimuth()+halfBeam)/180.0*Math.PI)*sectorSizePx);
	
		int azimuthXpoint2 = (int)(siteX+Math.cos((int)(90-base.getAzimuth()-halfBeam)/180.0*Math.PI)*sectorSizePx);
		int azimuthYpoint2 = (int)(siteY-Math.sin((int)(90-base.getAzimuth()-halfBeam)/180.0*Math.PI)*sectorSizePx);
	
		g2d.drawArc(siteX-sectorSizePx,siteY-sectorSizePx,2*sectorSizePx,2*sectorSizePx,(int)(90-base.getAzimuth()+halfBeam),-beamwidth);
		
		
		g2d.fillOval(siteX-7, siteY-7, 14, 14);
		g2d.drawString(base.getText(), azimuthXpoint, azimuthYpoint);
		
		//base is connected, so draw line
		if(base.isConnected()){
			Composite originalComposite = g2d.getComposite();
			Stroke originalStroke = g2d.getStroke();
		    int type = AlphaComposite.SRC_OVER;
		    Composite alphaComp = AlphaComposite.getInstance(type, 0.65F);
		    g2d.setComposite(alphaComp);
		    float dash = (float)(Math.random()*10)+1;
		    float gap = (float)(Math.random()*10)+1;
		    float[] dashes = { gap, dash };
		    g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10,
                    dashes, 0));
			g2d.drawLine(siteX, siteY, currX, currY);
			g2d.setComposite(originalComposite);
			g2d.setStroke(originalStroke);
		}
		
		
		g2d.drawLine(siteX,siteY,azimuthXpoint1,azimuthYpoint1);
		g2d.drawLine(siteX,siteY,azimuthXpoint2,azimuthYpoint2);
		
		Composite originalComposite = g2d.getComposite();
	    int type = AlphaComposite.SRC_OVER;
	    float alpha = 0.3F;
	    if(base.isConnected())
	    	alpha = (float)Math.random()*0.65F;
	    Composite alphaComp = AlphaComposite.getInstance(type, alpha);
	    g2d.setComposite(alphaComp);
		g2d.fillArc(siteX-sectorSizePx,siteY-sectorSizePx,2*sectorSizePx,2*sectorSizePx,(int)(90-base.getAzimuth()+halfBeam),-beamwidth);
	    g2d.setComposite(originalComposite);
	    
	    Stroke originalStroke = g2d.getStroke();
	    g2d.setStroke (new BasicStroke(
			      1f, 
			      BasicStroke.CAP_ROUND, 
			      BasicStroke.JOIN_ROUND, 
			      1f, 
			      new float[] {4f}, 
			      0f));
		
		g2d.drawLine(siteX,siteY,azimuthXpoint,azimuthYpoint);
		g2d.setStroke(originalStroke);
	}
	
	private void drawPosition(Graphics2D g2d,MapGraphic position){
		int posX = translateLongitudeToXCoord(position.getLongitude());
		int posY = translateLatitudeToYCoord(position.getLatitude());

		g2d.setColor(position.getColor());
		
		if(position.isConnected())
			g2d.fillOval(posX, posY, positionSize, positionSize);
		else {
			g2d.fillRect(posX, posY, positionSize, positionSize);
			g2d.setColor(Color.WHITE);
			g2d.drawRect(posX, posY, positionSize, positionSize);
		}
		
		if(position.getText()!=null){
			g2d.setColor(Color.WHITE);
			g2d.drawString(position.getText(), posX, posY);
		}
	}
	
	private int translateLongitudeToXCoord(double longitude){
		double dx = longitude-mapNwLong;
		double mapWidth = mapSeLong - mapNwLong;
		double ratioX = dx/mapWidth;
		
		int px = (int)(ratioX*map.getWidth());
		return px;
	}
	
	private int translateLatitudeToYCoord(double latitude){
		double dy = mapNwLat - latitude;
		double mapHeight = mapNwLat - mapSeLat;
		double ratioY = dy/mapHeight;
		
		int py = (int)(ratioY*map.getHeight());
		return py;
	}
	
	public void clearMap(){
		positions.clear();
		System.gc();
		repaint();
	}
	


	/* (non-Javadoc)
	 * @see com.motoflexi.devices.gps.GpsEventListener#eventOccurred(com.motoflexi.devices.gps.GpsEvent)
	 */
	public void processGpsCoordinates(double latitude,double longitude) {
		currPos = new MapGraphic(latitude,longitude,null,currColor);
		positions.add(currPos);
		currPos.setConnected(connected);
		
		currX = translateLongitudeToXCoord(longitude);
		currY = translateLatitudeToYCoord(latitude);
				
		//check for IP address
		try {
			String clientIP = InetAddress.getLocalHost().getHostAddress();
			if(clientIP!=null){
				if(clientIP.equals("127.0.0.1")){
					//drop
					if(connectionStatus.equals("Connected")){
						currPos.setText("Network Drop");
						connectionStatus="Not Connected";
					}
				}
				else {
					if(connectionStatus.equals("Not Connected")){
						currPos.setText("Network Entry");
						connectionStatus="Connected";
					}
				}
			}
		}
		catch(Exception e){
			log.warn("WARN",e);
		}
		this.repaint();
	}
	
	public void processDisplayMetric(Hashtable<String,String> metrics){
		Metric metric = MetricsHelper.getInstance().getMetricByName(displayMetric.getName());
		if(metric!=null){
			String value = null;
			for(Iterator<String> iter=metric.getProperty().iterator();((iter.hasNext())&&(value==null));)
				value = metrics.get(iter.next());
			if(value!=null){
				try {
					metric.setValue(value);
					double step = (displayMetric.getMax()-displayMetric.getMin())/(double)colorScale.size();
					double doubleVal = Double.parseDouble(metric.getValue());
					int colorIndex = colorScale.size()-Math.abs((int)((doubleVal-displayMetric.getMin())/step));
					
					if(colorIndex>colorScale.size()-1)
						colorIndex = colorScale.size()-1;
					currColor = colorScale.get(colorIndex);
				}
				catch(Exception e){
					log.error("Unable to determine display metric color index using value "+value+" and metric "+metric);
				}
			}
			else
				log.trace("Display metric "+displayMetric.getName()+" did not have a data collection metric for this event.");
		}
		else
			log.error("Display metric does not have a corresponding data collection metric.");
	}
	
	public String processBaseId(Hashtable<String,String> metrics,boolean connected){
		String handoverResult=null;
		this.connected=connected;
		String baseId = metrics.get("BaseID");
		if(baseId==null)
			baseId = metrics.get("BS ID");
		if(baseId==null)
			return handoverResult;
		else if(baseId.equals("010203040506")){
			if(currBase!=null){
				currBase.setConnected(false);
				currBase=null;
			}
		}
		else {
			Base base = BasesHelper.getInstance().getBaseByBaseId(baseId);
			if(base==null){
				log.warn("Base "+baseId+" does not exist in the application bases list.  Please check the list.");
				handoverResult = currBase.getText()+" -> "+baseId+" ? ";
				if(currBase!=null)
					currBase.setConnected(false);
				if(currPos!=null)
					currPos.setText(handoverResult);
				currBase=null;
			}
			else {
				base.setConnected(true);
				if((currBase!=null))
					currBase.setConnected(false);
				if(connected&&(currBase!=base)&&(currPos!=null)&&(currBase!=null)){
					handoverResult = currBase.getText()+" -> "+base.getText();
					currPos.setText(handoverResult);
					try {
						AudioHelper.getInstance().playAudio("Handover");
					}
					catch(Exception e){
						log.error("ERROR",e);
					}
				}
				
				currBase = base;
			}
		}
		return handoverResult;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Save Image"))
			saveImage();
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
		if(e.getButton()==MouseEvent.BUTTON3)
			popup.show(e.getComponent(),
                    e.getX(), e.getY());
		mouseDx=0;
		mouseDy=0;
		mouseX=-9999;
		mouseY=-9999;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		mouseDx=0;
		mouseDy=0;
	}
	
	/**
	 * Mouse Motion Listener Methods
	 */

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if((mouseX!=-9999)&&(mouseY!=-9999)){
			mouseDx=x-mouseX;
			mouseDy=y-mouseY;
		}
		mouseX=x;
		mouseY=y;
		this.repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
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
