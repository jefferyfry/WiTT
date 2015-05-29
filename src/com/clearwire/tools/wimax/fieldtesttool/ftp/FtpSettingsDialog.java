/**
 * 
 */
package com.clearwire.tools.wimax.fieldtesttool.ftp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
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

import org.apache.commons.configuration.SubnodeConfiguration;
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
import com.clearwire.tools.wimax.fieldtesttool.configuration.ConfigurationHelper;
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
public class FtpSettingsDialog extends JDialog implements ActionListener {
	
	public final static int OK = 0;
	public final static int CANCEL = 1;

	private Log log = LogFactory.getLog(FtpSettingsDialog.class);
	
	private FtpPanel ftpPanel = null;
	
	public FtpSettingsDialog(Frame owner){
		super(owner,"FTP Settings",true);
		
		SubnodeConfiguration ftp = ConfigurationHelper.getInstance().getConfig().configurationAt("//ftp");
		String ftpAddress = ftp.getString("@address");
		String ftpUsername = ftp.getString("@username");
		String ftpPassword = ftp.getString("@password");
		String ftpRemoteFile = ftp.getString("@remoteFile","downlink/10MB");
		String ftpLocalFile = ftp.getString("@localFile","lib/WiTT.jar");
		
		ftpPanel = new FtpPanel(ftpAddress,ftpUsername,ftpPassword,ftpRemoteFile,ftpLocalFile);
		
		getContentPane().add(ftpPanel,BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		JButton ok = new JButton("OK");
		ok.addActionListener(this);
		buttonPanel.add(ok);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		buttonPanel.add(cancel);
		
		
		getContentPane().add(buttonPanel,BorderLayout.SOUTH);
		
		pack();
        setLocationRelativeTo(owner);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("OK")){
			//save config
			saveFtpSettings();
			dispose();
		}
		else if(e.getActionCommand().equals("Cancel")){
			this.dispose();
		}
		
	}
	
	private void saveFtpSettings(){
		SubnodeConfiguration ftp = ConfigurationHelper.getInstance().getConfig().configurationAt("//ftp");
		String ftpAddress = ftpPanel.getServerAddress();
		if((ftpAddress!=null)&&(ftpAddress.trim().length()!=0))
			ftp.setProperty("@address", ftpAddress);

		String ftpUsername = ftpPanel.getUsername();
		if((ftpUsername!=null)&&(ftpUsername.trim().length()!=0))
			ftp.setProperty("@username", ftpUsername);

		String ftpPassword = ftpPanel.getPassword();
		if((ftpPassword!=null)&&(ftpPassword.trim().length()!=0))
			ftp.setProperty("@password", ftpPassword);
		
		String ftpRemoteFile = ftpPanel.getRemoteFile();
		if((ftpRemoteFile!=null)&&(ftpRemoteFile.trim().length()!=0))
			ftp.setProperty("@remoteFile", ftpRemoteFile);
		
		String ftpLocalFile = ftpPanel.getLocalFile();
		if((ftpLocalFile!=null)&&(ftpLocalFile.trim().length()!=0))
			ftp.setProperty("@localFile", ftpLocalFile);
	}

	
}
