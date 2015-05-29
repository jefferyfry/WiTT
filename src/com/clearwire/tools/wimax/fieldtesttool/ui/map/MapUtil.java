package com.clearwire.tools.wimax.fieldtesttool.ui.map;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clearwire.tools.wimax.fieldtesttool.configuration.ConfigurationHelper;
import com.clearwire.tools.wimax.fieldtesttool.ui.WiTTMain;

public class MapUtil extends JComponent {
	
	private Log log = LogFactory.getLog(MapUtil.class);
	
	public void addBase(String baseName,double latitude,double longitude,String baseIdA,String baseIdB,String baseIdC,int azimuthA,int azimuthB,int azimuthC){
    	/*BaseHelper.getInstance().getConfig().addProperty("maps map@name", mapName);
    	BaseHelper.getInstance().getConfig().addProperty("maps/map[last()] @file", mapPath);
    	BaseHelper.getInstance().getConfig().addProperty("maps/map[last()] @ul_lat", ulLat);
    	BaseHelper.getInstance().getConfig().addProperty("maps/map[last()] @ul_long", ulLong);
    	BaseHelper.getInstance().getConfig().addProperty("maps/map[last()] @lr_lat", lrLat);
    	BaseHelper.getInstance().getConfig().addProperty("maps/map[last()] @lr_long", lrLong);
		Map newMap = new Map(mapName,mapPath,ulLat,ulLong,lrLat,lrLong);
		
	    mapSelection.addItem(newMap);*/
    }
    
    public void importMap(String mapName,String mapPath,double ulLat,double ulLong,double lrLat,double lrLong){
    	ConfigurationHelper.getInstance().getConfig().addProperty("maps map@name", mapName);
    	ConfigurationHelper.getInstance().getConfig().addProperty("maps/map[last()] @file", mapPath);
    	ConfigurationHelper.getInstance().getConfig().addProperty("maps/map[last()] @ul_lat", ulLat);
    	ConfigurationHelper.getInstance().getConfig().addProperty("maps/map[last()] @ul_long", ulLong);
    	ConfigurationHelper.getInstance().getConfig().addProperty("maps/map[last()] @lr_lat", lrLat);
    	ConfigurationHelper.getInstance().getConfig().addProperty("maps/map[last()] @lr_long", lrLong);
		Map newMap = new Map(mapName,mapPath,ulLat,ulLong,lrLat,lrLong);
		
	    //mapSelection.addItem(newMap);
    }
    
    public void importMapImageDialog(Component parent){
    	JFileChooser dialog = new JFileChooser(System.getProperty("user.home"));
    	dialog.setDialogTitle("Import Map Image");
		
    	JPanel panel = new JPanel();
    	JPanel coordPanel = new JPanel(new GridLayout(5,2));
    	DecimalFormat decimalFormat = new DecimalFormat("###0.00000");
    	
    	JPanel mapNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    	mapNamePanel.add(new JLabel("Map Name"));
    	JPanel mapNameFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JTextField mapName = new JTextField() ;
		mapName.setColumns(15);
		mapNameFieldPanel.add(mapName);
		coordPanel.add(mapNamePanel);
		coordPanel.add(mapNameFieldPanel);
    	
    	JPanel ulLatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    	ulLatPanel.add(new JLabel("Northwest Corner Latitude"));
    	JPanel ulLatFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JFormattedTextField ulLat = new JFormattedTextField(decimalFormat) ;
		ulLat.setColumns(15);
		ulLatFieldPanel.add(ulLat);
		coordPanel.add(ulLatPanel);
		coordPanel.add(ulLatFieldPanel);
		
		JPanel ulLongPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ulLongPanel.add(new JLabel("Northwest Corner Longitude"));
		JPanel ulLongFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JFormattedTextField ulLong = new JFormattedTextField(decimalFormat) ;
		ulLong.setColumns(15);
		ulLongFieldPanel.add(ulLong);
		coordPanel.add(ulLongPanel);
		coordPanel.add(ulLongFieldPanel);
		
		JPanel lrLatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		lrLatPanel.add(new JLabel("Southeast Corner Latitude"));
		JPanel lrLatFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JFormattedTextField lrLat = new JFormattedTextField(decimalFormat) ;
		lrLat.setColumns(15);
		lrLatFieldPanel.add(lrLat);
		coordPanel.add(lrLatPanel);
		coordPanel.add(lrLatFieldPanel);
		
		JPanel lrLongPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		lrLongPanel.add(new JLabel("Southeast Corner Longitude"));
		JPanel lrLongFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JFormattedTextField lrLong = new JFormattedTextField(decimalFormat) ;
		lrLong.setColumns(15);
		lrLongFieldPanel.add(lrLong);
		coordPanel.add(lrLongPanel);
		coordPanel.add(lrLongFieldPanel);
		
		panel.add(coordPanel);
		
		dialog.add(panel,BorderLayout.WEST);
		
		//remove the new folder button
		JPanel compPanel = (JPanel)dialog.getComponent(2);
		JPanel comp1Panel = (JPanel)compPanel.getComponent(2);
		comp1Panel.remove(0);
		
		//change open to import
		JPanel comp2Panel = (JPanel)comp1Panel.getComponent(0);
		JButton openButton = (JButton)comp2Panel.getComponent(1);
		openButton.setText("Import Map");
		
		
		dialog.setFileFilter(new FileFilter(){
			public boolean accept(File file){
				if(file.isDirectory())
					return true;
				else if(file.getPath().endsWith(".jpg")){
					return true;
				}
				else 
					return false;
			}
			
			public String getDescription(){
				return "Map Image Files";
			}
		});
		int ret = dialog.showOpenDialog(this);
		if(ret==JFileChooser.APPROVE_OPTION){
			log.debug("Importing image.");
			try {
				if((ulLat.getText().trim().length()==0)||(ulLong.getText().trim().length()==0)||
						(lrLong.getText().trim().length()==0)||(lrLong.getText().trim().length()==0)||
						(mapName.getText().trim().length()==0))
					throw new Exception("Missing required parameters.");
				else
					importMap(mapName.getText(),dialog.getSelectedFile().toURL().toExternalForm(),
							Double.parseDouble(ulLat.getText()),Double.parseDouble(ulLong.getText()),
							Double.parseDouble(lrLat.getText()),Double.parseDouble(lrLong.getText()));
			}
			catch(Exception e){
				log.error("ERROR",e);
				JOptionPane.showMessageDialog(this, "Unable to import map image: "+e.getMessage(),"ERROR",JOptionPane.ERROR_MESSAGE,null);									
			}
		}
    }

}
