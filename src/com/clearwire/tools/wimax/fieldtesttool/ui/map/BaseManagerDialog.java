/**
 * 
 */
package com.clearwire.tools.wimax.fieldtesttool.ui.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clearwire.tools.wimax.bcs200.event.BCS200Event;
import com.clearwire.tools.wimax.bcs200.event.BCS200EventListener;
import com.clearwire.tools.wimax.bcs200.event.BasebandStatEvent;
import com.clearwire.tools.wimax.bcs200.event.MacMsgEvent;
import com.clearwire.tools.wimax.fieldtesttool.audio.AudioHelper;
import com.clearwire.tools.wimax.fieldtesttool.bases.BasesHelper;
import com.clearwire.tools.wimax.fieldtesttool.logger.LoggerEvent;
import com.clearwire.tools.wimax.fieldtesttool.logger.LoggerEventListener;
import com.clearwire.tools.wimax.fieldtesttool.bases.BasesHelper;
import com.clearwire.tools.wimax.fieldtesttool.metrics.Metric;
import com.clearwire.tools.wimax.fieldtesttool.metrics.MetricsHelper;
import com.clearwire.tools.wimax.fieldtesttool.ui.util.ButtonEditor;
import com.clearwire.tools.wimax.fieldtesttool.ui.util.ButtonRenderer;
import com.clearwire.tools.wimax.typeperf.TypeperfEvent;
import com.clearwire.tools.wimax.typeperf.TypeperfEventListener;
import com.openracesoft.devices.gps.GpsEvent;
import com.openracesoft.devices.gps.GpsEventListener;
import com.openracesoft.devices.gps.nmea.GpsFixDataEvent;

/**
 * @author jfry
 *
 */
public class BaseManagerDialog extends JDialog implements ActionListener,MouseListener {
	
	public final static int OK = 0;
	public final static int CANCEL = 1;

	private Log log = LogFactory.getLog(BaseManagerDialog.class);

	private BasesTableModel basesModel = new BasesTableModel();
	private JTable basesTable = new JTable(basesModel);
	
	private JScrollPane basesScrollPane = new JScrollPane(basesTable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		    	
	private JPopupMenu popupMenu = new JPopupMenu();
	
	private BasesHelper basesHelper = BasesHelper.getInstance();
	
	private int result = OK;
	
	public BaseManagerDialog(Frame owner){
		super(owner,"Base Management",true);
		
		TableColumnModel tcm = basesTable.getColumnModel();
		tcm.getColumn(0).setPreferredWidth(200);
		tcm.getColumn(1).setPreferredWidth(200);
		tcm.getColumn(2).setPreferredWidth(100);
		tcm.getColumn(3).setPreferredWidth(100);
		tcm.getColumn(4).setPreferredWidth(100);
		
		JPanel buttonPanel = new JPanel();
		JButton ok = new JButton("OK");
		ok.addActionListener(this);
		buttonPanel.add(ok);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		buttonPanel.add(cancel);
		
		getContentPane().add(basesScrollPane,BorderLayout.CENTER);
		getContentPane().add(buttonPanel,BorderLayout.SOUTH);
		
		JMenuItem addBase = new JMenuItem("Add");
		addBase.addActionListener(this);
		popupMenu.add(addBase);
		
		JMenuItem deleteBase = new JMenuItem("Delete");
		deleteBase.addActionListener(this);
		popupMenu.add(deleteBase);
		
		JMenuItem displayAll = new JMenuItem("Display All");
		displayAll.addActionListener(this);
		popupMenu.add(displayAll);

		JMenuItem displayNone = new JMenuItem("Display None");
		displayNone.addActionListener(this);
		popupMenu.add(displayNone);

		basesTable.addMouseListener(this);
		this.addMouseListener(this);
		basesScrollPane.addMouseListener(this);
		
		basesScrollPane.setPreferredSize(new Dimension(basesTable.getPreferredSize().width,basesScrollPane.getPreferredSize().height));
		
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
			if(basesTable.getCellEditor()!=null)
				basesTable.getCellEditor().stopCellEditing();
			basesModel.save();
			dispose();
			result = OK;
		}
		else if(e.getActionCommand().equals("Cancel")){
			this.dispose();
			result = CANCEL;
		}
		else if(e.getActionCommand().equals("Add")){
			basesModel.addNewBase();
		}
		else if(e.getActionCommand().equals("Delete")){
			int row = basesTable.getSelectedRow();
			basesModel.deleteBase(row);	
		}
		else if(e.getActionCommand().equals("Display All")){
			basesModel.selectAll();
		}
		else if(e.getActionCommand().equals("Display None")){
			basesModel.deselectAll();
		}
	}

	class BasesTableModel extends AbstractTableModel {
		
		private ArrayList<Base> bases = new ArrayList<Base>();
				
		BasesTableModel(){
			super();
			
			bases.addAll((ArrayList<Base>)BasesHelper.getInstance().getBases().clone());
		}
		
		public void deselectAll(){
			for(Iterator<Base> iter=bases.iterator();iter.hasNext();){
				Base base = iter.next();
				base.setDisplay(false);
			}
			this.fireTableDataChanged();
		}
		
		public void selectAll(){
			for(Iterator<Base> iter=bases.iterator();iter.hasNext();){
				Base base = iter.next();
				base.setDisplay(true);
			}
			this.fireTableDataChanged();
		}
		
		public void addNewBase(){
			bases.add(new Base());
			basesModel.fireTableRowsInserted(getRowCount()-1, getRowCount()-1);
		}
		
		public void deleteBase(int index){
			bases.remove(index);
			basesModel.fireTableRowsDeleted(index, index);
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getColumnCount()
		 */
		public int getColumnCount() {
			return 8;
		}
		
		public Class getColumnClass(int columnIndex){
			if(columnIndex==7)
				return Boolean.class;
			else
				return String.class;
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getRowCount()
		 */
		public int getRowCount() {
			return bases.size();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getColumnName(int)
		 */
		public String getColumnName(int column) {
			if(column==0)
				return "Name";
			else if (column==1)
				return "Base ID";
			else if (column==2)
				return "Latitude";
			else if (column==3)
				return "Longitude";
			else if (column==4)
				return "Azimuth";
			else if (column==5)
				return "Beamwidth";
			else if (column==6)
				return "Color";
			else if (column==7)
				return "Display?";
			else
				return "ERROR";
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int row, int column) {
			Base base = bases.get(row);
			
			if(column==0)
				return base.getText();
			else if (column==1)
				return base.getBaseid();
			else if (column==2)
				return base.getLatitude();
			else if (column==3)
				return base.getLongitude();
			else if (column==4)
				return base.getAzimuth();
			else if (column==5)
				return base.getBeamwidth();
			else if (column==6)
				return "#"+Integer.toHexString(base.getColor().getRed())+Integer.toHexString(base.getColor().getGreen())+Integer.toHexString(base.getColor().getBlue());
			else if (column==7)
				return base.isDisplay();
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
			Base base = bases.get(row);
			
			if(column==0) {
				String valueStr = (String)value;
				base.setText(valueStr);
			}
			else if (column==1) {
				String valueStr = (String)value;
				base.setBaseid(valueStr);
			}
			else if (column==2) {
				Double valueDb = Double.valueOf((String)value);
				base.setLatitude(valueDb.doubleValue());
			}
			else if (column==3) {
				Double valueDb = Double.valueOf((String)value);
				base.setLongitude(valueDb.doubleValue());
			}
			else if (column==4) {
				Integer valueInt = Integer.valueOf((String)value);
				base.setAzimuth(valueInt.intValue());
			}
			else if (column==5) {
				Integer valueInt = Integer.valueOf((String)value);
				base.setBeamwidth(valueInt.intValue());
			}
			else if (column==6) {
				String valueStr = (String)value;
				base.setColor(Color.decode(valueStr));
			}
			else if (column==7) {
				Boolean valueStr = (Boolean)value;
				base.setDisplay(valueStr);
			}
		}
		
		private void save(){
			basesHelper.clearBases();
			for(Iterator<Base> iter = bases.iterator();iter.hasNext();){
				basesHelper.updateBase((Base)iter.next());
			}
			basesHelper.saveBasesToConfig();
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
