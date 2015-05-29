/**
 * 
 */
package com.clearwire.tools.wimax.fieldtesttool.ui.map;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clearwire.tools.wimax.fieldtesttool.listgeo.ListgeoIO;
import com.clearwire.tools.wimax.fieldtesttool.map.MapsHelper;
import com.clearwire.tools.wimax.fieldtesttool.ui.util.ButtonEditor;
import com.clearwire.tools.wimax.fieldtesttool.ui.util.ButtonRenderer;

/**
 * @author jfry
 *
 */
public class MapManagerDialog extends JDialog implements ActionListener,MouseListener {
	
	public final static int OK = 0;
	public final static int CANCEL = 1;

	private Log log = LogFactory.getLog(MapManagerDialog.class);

	private MapsTableModel mapsModel = new MapsTableModel();
	private JTable mapsTable = new JTable(mapsModel);
	
	private JScrollPane mapsScrollPane = new JScrollPane(mapsTable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		    	
	private JPopupMenu popupMenu = new JPopupMenu();
	
	private MapsHelper mapsHelper = MapsHelper.getInstance();
	
	private int result = OK;
	
	private Frame owner;
	
	public MapManagerDialog(Frame owner){
		super(owner,"Map Management",true);
		this.owner=owner;
		
		TableColumnModel tcm = mapsTable.getColumnModel();
		tcm.getColumn(0).setPreferredWidth(200);
		tcm.getColumn(1).setPreferredWidth(400);
		tcm.getColumn(2).setPreferredWidth(100);
		tcm.getColumn(3).setPreferredWidth(100);
		tcm.getColumn(4).setPreferredWidth(100);
		tcm.getColumn(5).setPreferredWidth(100);
		
		tcm.getColumn(1).setCellRenderer(new ButtonRenderer());
	    tcm.getColumn(1).setCellEditor(new ButtonEditor(owner));
		
		JPanel buttonPanel = new JPanel();
		JButton ok = new JButton("OK");
		ok.addActionListener(this);
		buttonPanel.add(ok);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		buttonPanel.add(cancel);
		
		getContentPane().add(mapsScrollPane,BorderLayout.CENTER);
		getContentPane().add(buttonPanel,BorderLayout.SOUTH);
		
		JMenuItem addMap = new JMenuItem("Add");
		addMap.addActionListener(this);
		popupMenu.add(addMap);
		
		JMenuItem deleteMap = new JMenuItem("Delete");
		deleteMap.addActionListener(this);
		popupMenu.add(deleteMap);
		
		mapsTable.addMouseListener(this);
		this.addMouseListener(this);
		mapsScrollPane.addMouseListener(this);
		
		mapsScrollPane.setPreferredSize(new Dimension(mapsTable.getPreferredSize().width,mapsScrollPane.getPreferredSize().height));
		
		pack();
        setLocationRelativeTo(owner);
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
			popupMenu.show(e.getComponent(),
                    e.getX(), e.getY());
		
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("OK")){
			if(mapsTable.getCellEditor()!=null)
				mapsTable.getCellEditor().stopCellEditing();
			mapsModel.save();
			dispose();
			result = OK;
		}
		else if(e.getActionCommand().equals("Cancel")){
			this.dispose();
			result = CANCEL;
		}
		else if(e.getActionCommand().equals("Add")){
			mapsModel.addNewMap();
		}
		else if(e.getActionCommand().equals("Delete")){
			int row = mapsTable.getSelectedRow();
			mapsModel.deleteMap(row);	
		}
	}

	class MapsTableModel extends AbstractTableModel {
		
		private ArrayList<Map> maps = new ArrayList<Map>();
				
		MapsTableModel(){
			super();
			
			maps.addAll((ArrayList<Map>)MapsHelper.getInstance().getMaps().clone());
		}
		
		public void addNewMap(){
			maps.add(new Map());
			mapsModel.fireTableRowsInserted(getRowCount()-1, getRowCount()-1);
		}
		
		public void deleteMap(int index){
			maps.remove(index);
			mapsModel.fireTableRowsDeleted(index, index);
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getColumnCount()
		 */
		public int getColumnCount() {
			return 6;
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getRowCount()
		 */
		public int getRowCount() {
			return maps.size();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getColumnName(int)
		 */
		public String getColumnName(int column) {
			if(column==0)
				return "Name";
			else if (column==1)
				return "Location";
			else if (column==2)
				return "NW Latitude";
			else if (column==3)
				return "NW Longitude";
			else if (column==4)
				return "SE Latitude";
			else if (column==5)
				return "SE Longitude";
			else
				return "ERROR";
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int row, int column) {
			Map map = maps.get(row);
			
			if(column==0)
				return map.getName();
			else if (column==1)
				return map.getFileUrl();
			else if (column==2)
				return map.getNwLatitude();
			else if (column==3)
				return map.getNwLongitude();
			else if (column==4)
				return map.getSeLatitude();
			else if (column==5)
				return map.getSeLongitude();
			else
				return "ERROR";
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
		 */
		public boolean isCellEditable(int row, int column) {
			return true;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
		 */
		public void setValueAt(Object value, int row, int column) {
			Map map = maps.get(row);
			
			if(column==0) {
				String valueStr = (String)value;
				map.setName(valueStr);
			}
			else if (column==1) {
				String valueStr = (String)value;
				map.setFileUrl(valueStr);
				try {
					URL mapFile = new URL(valueStr);
					InputStream stream = mapFile.openStream();
					long size = stream.available()/1000;
					stream.close();
					if(size>500)
						JOptionPane.showMessageDialog(owner,"Your map file size is larger than 500 KB.  This may impact the performance of WiTT.  Map file sizes smaller than 500 KB perform better." , "WARNING", JOptionPane.WARNING_MESSAGE, null);
				}
				catch(Exception e){
					e.printStackTrace();
				}
				if(valueStr.endsWith(".tif")){
					setGeoTiffCoordinates(map);
					this.fireTableRowsUpdated(row, row);
				}
			}
			else if (column==2) {
				Double valueDb = Double.valueOf((String)value);
				map.setNwLatitude(valueDb.doubleValue());
			}
			else if (column==3) {
				Double valueDb = Double.valueOf((String)value);
				map.setNwLongitude(valueDb.doubleValue());
			}
			else if (column==4) {
				Double valueDb = Double.valueOf((String)value);
				map.setSeLatitude(valueDb.doubleValue());
			}
			else if (column==5) {
				Double valueDb = Double.valueOf((String)value);
				map.setSeLongitude(valueDb.doubleValue());
			}
		}
		
		private void setGeoTiffCoordinates(Map map){
			double[] corners = ListgeoIO.getCorners(map.getFileUrl().substring(map.getFileUrl().indexOf("/")+1));
			map.setNwLongitude(corners[0]);
			map.setNwLatitude(corners[1]);
			map.setSeLongitude(corners[2]);
			map.setSeLatitude(corners[3]);
		}
		
		private void save(){
			mapsHelper.clearMaps();
			for(Iterator<Map> iter = maps.iterator();iter.hasNext();){
				mapsHelper.updateMap((Map)iter.next());
			}
			mapsHelper.saveMapsToConfig();
		}
	}



	/**
	 * @return the result
	 */
	public int getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(int result) {
		this.result = result;
	}
}
