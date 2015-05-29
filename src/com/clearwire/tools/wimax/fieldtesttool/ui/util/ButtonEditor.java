package com.clearwire.tools.wimax.fieldtesttool.ui.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ButtonEditor extends DefaultCellEditor {
	
	private Log log = LogFactory.getLog(ButtonEditor.class);

	  protected JButton button;
	  private String    label;
	  private boolean   isPushed;
	  private Component parent = null;
	 
	  public ButtonEditor(Component parent) {
	    super(new JCheckBox());
	    this.parent=parent;
	    button = new JButton();
	    button.setOpaque(true);
	    button.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        fireEditingStopped();
	      }
	    });
	  }
	 
	  public Component getTableCellEditorComponent(JTable table, Object value,
	                   boolean isSelected, int row, int column) {
	    if (isSelected) {
	      button.setForeground(table.getSelectionForeground());
	      button.setBackground(table.getSelectionBackground());
	    } else{
	      button.setForeground(table.getForeground());
	      button.setBackground(table.getBackground());
	    }
	    label = (value ==null) ? "" : value.toString();
	    button.setText( label );
	    isPushed = true;
	    return button;
	  }
	 
	  public Object getCellEditorValue() {
	    if (isPushed)  {
	    	JFileChooser dialog = new JFileChooser(System.getProperty("user.home"));
	    	dialog.setDialogTitle("Import Map Image");
	    	dialog.setFileFilter(new FileFilter(){
				public boolean accept(File file){
					if(file.isDirectory())
						return true;
					else if(file.getPath().endsWith(".jpg")){
						return true;
					}
					else if(file.getPath().endsWith(".tif")){
						return true;
					}
					else 
						return false;
				}
				
				public String getDescription(){
					return "Map Image Files";
				}
			});
			int ret = dialog.showOpenDialog(parent);
			if(ret==JFileChooser.APPROVE_OPTION){
				log.debug("Importing image.");
				try {
					 return dialog.getSelectedFile().toURL().toExternalForm();
				}
				catch(Exception e){
					log.error("ERROR",e);
					JOptionPane.showMessageDialog(parent, "Unable to import map image: "+e.getMessage(),"ERROR",JOptionPane.ERROR_MESSAGE,null);									
				}
			}
	    }
	    isPushed = false;
	    return new String( label ) ;
	  }
	   
	  public boolean stopCellEditing() {
	    isPushed = false;
	    return super.stopCellEditing();
	  }
	 
	  protected void fireEditingStopped() {
	    super.fireEditingStopped();
	  }
}
