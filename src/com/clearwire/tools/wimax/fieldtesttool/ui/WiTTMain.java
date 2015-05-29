package com.clearwire.tools.wimax.fieldtesttool.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clearwire.tools.wimax.CPEi150.CPEi150;
import com.clearwire.tools.wimax.CPEi150.event.CPEi150EventListener;
import com.clearwire.tools.wimax.CPEi150.event.CPEi150StatsEvent;
import com.clearwire.tools.wimax.bcs200.BCS200;
import com.clearwire.tools.wimax.bcs200.BCS200Exception;
import com.clearwire.tools.wimax.bcs200.event.BCS200Event;
import com.clearwire.tools.wimax.bcs200.event.BCS200EventListener;
import com.clearwire.tools.wimax.fieldtesttool.bases.BasesHelper;
import com.clearwire.tools.wimax.fieldtesttool.configuration.ConfigurationHelper;
import com.clearwire.tools.wimax.fieldtesttool.ftp.FtpEvent;
import com.clearwire.tools.wimax.fieldtesttool.ftp.FtpEventListener;
import com.clearwire.tools.wimax.fieldtesttool.ftp.FtpSettingsDialog;
import com.clearwire.tools.wimax.fieldtesttool.ftp.FtpThread;
import com.clearwire.tools.wimax.fieldtesttool.iperf.IperfPanel;
import com.clearwire.tools.wimax.fieldtesttool.map.MapsHelper;
import com.clearwire.tools.wimax.fieldtesttool.ping.Ping;
import com.clearwire.tools.wimax.fieldtesttool.ui.map.BaseManagerDialog;
import com.clearwire.tools.wimax.fieldtesttool.ui.map.MapManagerDialog;
import com.clearwire.tools.wimax.fieldtesttool.ui.map.MapUI;
import com.clearwire.tools.wimax.fieldtesttool.ui.metrics.MetricsUI;
import com.clearwire.tools.wimax.swin2.Swin2;
import com.clearwire.tools.wimax.swin2.Swin2Exception;
import com.clearwire.tools.wimax.typeperf.Typeperf;
import com.clearwire.tools.wimax.typeperf.TypeperfException;
import com.openracesoft.devices.gps.GpsDevice;
import com.openracesoft.devices.gps.GpsFactory;
import com.openracesoft.devices.gps.nmea.NmeaDevice;

public class WiTTMain extends JFrame implements WindowListener, ActionListener,
		FtpEventListener, CPEi150EventListener, BCS200EventListener {

	private String version = "10.1";

	private Log log = LogFactory.getLog(WiTTMain.class);

	private Typeperf typeperf = null;
	private BCS200 bcs200Process = null;
	private Swin2 swin2Process = null;
	private CPEi150 cpei150Process = null;
	private GpsDevice gps = null;
	private Ping pingProcess = null;

	private MapUI mapUI = null;
	private MetricsUI metricsUI = new MetricsUI();

	private JComboBox gpsPorts = new JComboBox(GpsFactory.getGPSPorts());
	private JLabel gpsLabel = new JLabel("Select GPS Port:");

	private JLabel gpsBaudRateLabel = new JLabel("Select Baud Rate:");
	private JComboBox gpsBaudRate = new JComboBox(new String[] { "4800",
			"9600", "19200", "38400", "57600" });

	private IperfPanel localIperfPanel = null;
	private IperfPanel remoteIperfPanel = null;

	private JButton startMonitor = new JButton("Start Monitor");
	private JButton startLog = new JButton("Start Log");

	private JButton startDownlinkTraffic = new JButton("Start Downlink Traffic");
	private JButton startUplinkTraffic = new JButton("Start Uplink Traffic");
	private JLabel statusLabel = new JLabel("Ready.");

	private String logFileName = null;

	private JRadioButton expertRadio = new JRadioButton("Expert", false); // bcs200
																			// +
																			// swin
																			// +
																			// mac
																			// +
																			// logging
	private JRadioButton advRadio = new JRadioButton("Advanced", false); // bcs200
																			// +
																			// swin
																			// +logging
	private JRadioButton demoRadio = new JRadioButton("Demo", true); // bcs200
	private JRadioButton commRadio = new JRadioButton("Commissioning", false); // bcs200
																				// and
																				// logging

	private JFrame parent = this;

	private ActionListener parentActionListener = this;

	private WindowListener parentWindowListener = this;

	private String pingAddress = ConfigurationHelper.getInstance().getConfig()
			.getString("//ping/@server", "4.2.2.2");
	//private int pingInterval = ConfigurationHelper.getInstance().getConfig()
			//.getInt("//ping/@interval", 500);

	private ArrayList<FtpThread> ftpDownlinkThreads = new ArrayList<FtpThread>();
	private ArrayList<FtpThread> ftpUplinkThreads = new ArrayList<FtpThread>();
	private int threadNumber=5;

	public WiTTMain() {
		super();
		SplashDialog splashDialog = new SplashDialog(parent, "Clearwire WiTT v"
				+ version, true, new ImageIcon(ClassLoader
				.getSystemResource("logo_cw.gif")),
				"Launching Clearwire WiTT v" + version);
		splashDialog.setLocationRelativeTo(parent);
		splashDialog.showDialog();

		parent.setTitle("Clearwire WiTT Version " + version);

		log.debug("Launching Clearwire WiTT version " + version + "...");

		Image img = Toolkit.getDefaultToolkit().getImage(
				java.net.URLClassLoader.getSystemResource("favicon.jpg"));
		parent.setIconImage(img);
		getContentPane().setBackground(new Color(162, 205, 90));

		log.debug("Creating BCS200, Swin2, Typeperf, and GPS Objects...");
		bcs200Process = new BCS200(); // beceem
		swin2Process = new Swin2(); // beceem
		cpei150Process = new CPEi150(); // cpei150 ethernet
		int pingInterval = ConfigurationHelper.getInstance().getConfig().getInt("//ping/@interval");
		pingProcess = new Ping(pingInterval);
		typeperf = new Typeperf();
		gps = new NmeaDevice();
		int baudRate = ConfigurationHelper.getInstance().getConfig().getInt(
				"//gps/@baud-rate", 4800);
		gps.setBaudRate(baudRate);

		splashDialog.setMessage("Loading Typeperf Counters...");
		typeperf.setDeviceName(ConfigurationHelper.getInstance().getConfig()
				.getString("//typeperf/@device", "Beceem"));

		splashDialog.setMessage("Checking for existing processes...");
		// kill any existing processes
		BCS200.killAllBcs200();
		Swin2.killAllSwin2();
		Typeperf.killAllTypeperf();

		splashDialog.setMessage("Creating user interface...");
		// north panel
		JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		northPanel.add(gpsLabel);
		northPanel.add(gpsPorts);
		northPanel.add(gpsBaudRateLabel);
		northPanel.add(gpsBaudRate);
		gpsPorts.addItem("Auto-detect");

		// set previous port
		SubnodeConfiguration gpsNode = ConfigurationHelper.getInstance()
				.getConfig().configurationAt("//gps");
		String gpsPort = gpsNode.getString("@port");
		if (gpsPort != null)
			gpsPorts.setSelectedItem(gpsPort);
		else
			gpsPorts.setSelectedItem("Auto-detect");

		// monitor panel
		JPanel monitorPanel = new JPanel(new BorderLayout());
		monitorPanel.setBackground(new Color(162, 205, 90));
		monitorPanel.add(northPanel, BorderLayout.NORTH);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation((int) metricsUI.getPreferredSize()
				.getWidth()+20);
		mapUI = new MapUI();
		mapUI.addMapTagListener(metricsUI);
		
		monitorPanel.add(splitPane, BorderLayout.CENTER);

		JScrollPane metricsScrollPane = new JScrollPane(metricsUI,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		splitPane.add(metricsScrollPane);
		splitPane.add(mapUI);

		// buttons
		JPanel buttonPanel = new JPanel();

		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(expertRadio);
		radioGroup.add(advRadio);
		radioGroup.add(commRadio);
		radioGroup.add(demoRadio);
		Border radioBorder = BorderFactory.createTitledBorder("Select Mode:");
		JPanel radioPanel = new JPanel();
		radioPanel.setBorder(radioBorder);
		radioPanel.add(demoRadio);
		demoRadio.setToolTipText("Baseband statistics only");
		radioPanel.add(commRadio);
		commRadio.setToolTipText("Baseband statistics and logging");
		radioPanel.add(advRadio);
		advRadio.setToolTipText("Baseband, MIMO, HO statistics and logging");
		radioPanel.add(expertRadio);
		expertRadio
				.setToolTipText("Baseband, MIMO, and HO statistics, MAC messaging, and logging");
		buttonPanel.add(radioPanel);

		// traffic
		Border trafficBorder = BorderFactory
				.createTitledBorder("Select Traffic:");
		JPanel trafficPanel = new JPanel();
		trafficPanel.setBorder(trafficBorder);
		trafficPanel.add(startDownlinkTraffic);
		startDownlinkTraffic.addActionListener(this);
		startUplinkTraffic.addActionListener(this);
		trafficPanel.add(startUplinkTraffic);
		buttonPanel.add(trafficPanel);

		startMonitor.setBackground(new Color(162, 205, 90));
		startMonitor.addActionListener(parentActionListener);

		startLog.setEnabled(false);
		startLog.setBackground(new Color(162, 205, 90));
		startLog.addActionListener(parentActionListener);
		buttonPanel.setBackground(new Color(162, 205, 90));

		Border startBorder = BorderFactory.createTitledBorder("Start:");
		JPanel startPanel = new JPanel();
		startPanel.setBorder(startBorder);
		startPanel.add(startMonitor);
		startPanel.add(startLog);
		buttonPanel.add(startPanel);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setBackground(new Color(162, 205, 90));
		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		boxPanel.add(buttonPanel);
		boxPanel.setBackground(new Color(162, 205, 90));
		bottomPanel.add(boxPanel);
		JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		statusPanel.add(new JLabel("Status:"));
		statusPanel.add(statusLabel);
		boxPanel.add(statusPanel);

		monitorPanel.add(bottomPanel, BorderLayout.SOUTH);

		gps.addGpsEventListener(mapUI);
		gps.addGpsEventListener(metricsUI);
		bcs200Process.addBcs200Listener(metricsUI);
		bcs200Process.addBcs200Listener(this);
		swin2Process.addSwin2Listener(metricsUI);
		pingProcess.addPingListener(metricsUI);
		cpei150Process.addCPEi150Listener(metricsUI);
		cpei150Process.addCPEi150Listener(this);

		bcs200Process.addBcs200Listener(mapUI);
		cpei150Process.addCPEi150Listener(mapUI);

		typeperf.addTypeperfListener(metricsUI);

		// tabbedPane.add("Monitor",monitorPanel);
		parent.getContentPane().add(monitorPanel, BorderLayout.CENTER);

		// menu bar
		// Create the menu bar
		JMenuBar menuBar = new JMenuBar();

		// Create file
		JMenu file = new JMenu("File");
		menuBar.add(file);

		// Create MAPS
		JMenu maps = new JMenu("Maps");
		menuBar.add(maps);

		// Create Traffic
		JMenu traffic = new JMenu("Traffic");
		menuBar.add(traffic);

		// Create help
		JMenu help = new JMenu("Help");
		menuBar.add(help);

		// Log files
		JMenuItem logs = new JMenuItem("Config & Logs");
		logs.addActionListener(this);
		file.add(logs);

		// import sites
		JMenuItem importMap = new JMenuItem("Manage Maps");
		importMap.addActionListener(parentActionListener);
		maps.add(importMap);

		// add sites
		JMenuItem addSite = new JMenuItem("Manage Bases");
		addSite.addActionListener(parentActionListener);
		maps.add(addSite);

		// import sites
		JMenuItem ftpSettings = new JMenuItem("FTP Settings");
		ftpSettings.addActionListener(parentActionListener);
		traffic.add(ftpSettings);

		// exit
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(parentActionListener);
		file.add(exit);

		// About
		JMenuItem about = new JMenuItem("About");
		about.addActionListener(parentActionListener);
		help.add(about);

		// Install the menu bar in the frame
		parent.setJMenuBar(menuBar);

		parent.addWindowListener(parentWindowListener);

		splashDialog.closeDialog();
		
		if(BasesHelper.getInstance().getBaseCount()>50)
			JOptionPane.showMessageDialog(this,"The number of sectors in your bases file is larger than 50.  This may impact the performance of WiTT.  Bases files with a lower number of sectors perform better." , "WARNING", JOptionPane.WARNING_MESSAGE, null);

		if(MapsHelper.getInstance().getLargeMapFiles().size()>0){
			StringBuffer mapsBuffer = new StringBuffer();
			for(Iterator<String> iter = MapsHelper.getInstance().getLargeMapFiles().iterator();iter.hasNext(); )
				mapsBuffer.append(iter.next()).append(",");
			if(MapsHelper.getInstance().getLargeMapFiles().size()==1)
				JOptionPane.showMessageDialog(this,"Map "+mapsBuffer.substring(0, mapsBuffer.length()-1)+" may be too large.  This may impact the performance of WiTT.  Map file sizes smaller than 500 KB perform better." , "WARNING", JOptionPane.WARNING_MESSAGE, null);
			else
				JOptionPane.showMessageDialog(this,"Maps "+mapsBuffer.substring(0, mapsBuffer.length()-1)+" may be too large.  This may impact the performance of WiTT.  Map file sizes smaller than 500 KB perform better." , "WARNING", JOptionPane.WARNING_MESSAGE, null);
		}
		
		ComPortCheckerThread thread = new ComPortCheckerThread();
		thread.start();
	}

	/*
	 * private void createTrafficTabs(){ //local iperf settings
	 * SubnodeConfiguration localIperf =
	 * ConfigurationHelper.getInstance().getConfig
	 * ().configurationAt("//iperf/local"); String localAddress =
	 * localIperf.getString("@address"); String localPort =
	 * localIperf.getString("@port");
	 * 
	 * //remote iperf settings SubnodeConfiguration remoteIperf =
	 * ConfigurationHelper
	 * .getInstance().getConfig().configurationAt("//iperf/remote"); String
	 * remoteAddress = remoteIperf.getString("@address"); String remotePort =
	 * remoteIperf.getString("@port"); String sshAddress =
	 * remoteIperf.getString("@ssh-address"); String sshUsername =
	 * remoteIperf.getString("@ssh-username"); String sshPassword =
	 * remoteIperf.getString("@ssh-password");
	 * 
	 * //ftp settings SubnodeConfiguration ftp =
	 * ConfigurationHelper.getInstance().getConfig().configurationAt("//ftp");
	 * String ftpAddress = ftp.getString("@address"); String ftpUsername =
	 * ftp.getString("@username"); String ftpPassword =
	 * ftp.getString("@password"); String ftpFile = ftp.getString("@file");
	 * 
	 * localIperfPanel = new
	 * IperfPanel("iperf version 1.7.0 (13 Mar 2003) win32 threads"
	 * ,localAddress,localPort); Border localIperfBorder =
	 * BorderFactory.createTitledBorder("Local Iperf");
	 * localIperfPanel.setBorder(localIperfBorder);
	 * 
	 * remoteIperfPanel = new
	 * IperfPanel("iperf version 1.7.0 (13 Mar 2003) win32 threads"
	 * ,remoteAddress,remotePort,sshAddress,sshUsername,sshPassword); Border
	 * remoteIperfBorder = BorderFactory.createTitledBorder("Remote Iperf");
	 * remoteIperfPanel.setBorder(remoteIperfBorder);
	 * 
	 * tabbedPane.add(localIperfPanel,"Local Iperf");
	 * tabbedPane.add(remoteIperfPanel,"Remote Iperf");
	 * 
	 * JPanel ftpBorderPanel = new JPanel(); ftpPanel = new
	 * FtpPanel(ftpAddress,ftpUsername,ftpPassword,ftpFile); Border ftpBorder =
	 * BorderFactory.createTitledBorder("FTP"); ftpPanel.setBorder(ftpBorder);
	 * ftpBorderPanel.add(ftpPanel); tabbedPane.add(ftpBorderPanel,"FTP"); }
	 */

	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void windowClosing(WindowEvent e) {
		quit();
	}

	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Start Monitor"))
			start();
		else if (e.getActionCommand().equals("Config & Logs"))
			showLogFiles();
		else if (e.getActionCommand().equals("Stop Monitor"))
			stop();
		else if (e.getActionCommand().equals("Start Log")) {
			startNewLog();
		} else if (e.getActionCommand().equals("Stop Log")) {
			stopAndCloseLog();
		} else if (e.getActionCommand().equals("Start Downlink Traffic")) {
			startDownlinkTraffic();
		} else if (e.getActionCommand().equals("Start Uplink Traffic")) {
			startUplinkTraffic();
		} else if (e.getActionCommand().equals("Stop Downlink Traffic")) {
			stopDownlinkTraffic();
		} else if (e.getActionCommand().equals("Stop Uplink Traffic")) {
			stopUplinkTraffic();
		} else if (e.getActionCommand().equals("Exit"))
			quit();
		else if (e.getActionCommand().equals("Manage Maps")) {
			MapManagerDialog dialog = new MapManagerDialog(this);
			dialog.setVisible(true);
			if (dialog.getResult() == MapManagerDialog.OK) {
				mapUI.loadMaps();
				statusLabel.setText("Maps reloaded.");
			}
		} else if (e.getActionCommand().equals("Manage Bases")) {
			BaseManagerDialog dialog = new BaseManagerDialog(this);
			dialog.setVisible(true);
			if (dialog.getResult() == BaseManagerDialog.OK) {
				mapUI.reloadBases();
				statusLabel.setText("Bases reloaded.");
			}
		} else if (e.getActionCommand().equals("FTP Settings")) {
			FtpSettingsDialog dialog = new FtpSettingsDialog(this);
			dialog.setVisible(true);
		} else if (e.getActionCommand().equals("About")) {
			JOptionPane.showMessageDialog(this, "Version " + version+" by Jeff Fry (jeff.fry@clearwire.com)",
					"Version", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(
							ClassLoader.getSystemResource("logo_cw.gif")));
		} else if (e.getActionCommand().equals("Open Log")) {
			JFileChooser dialog = new JFileChooser(System
					.getProperty("user.home"));

			dialog.setFileFilter(new FileFilter() {
				public boolean accept(File file) {
					if (file.isDirectory())
						return true;
					else if (file.getPath().endsWith(".csv")) {
						return true;
					} else
						return false;
				}

				public String getDescription() {
					return "Log Files";
				}
			});
			int ret = dialog.showOpenDialog(this);
			if (ret == JFileChooser.APPROVE_OPTION)
				log.debug("Open Log.");
			// openLog(dialog.getSelectedFile());
		}
	}

	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	private void startDownlinkTraffic() {
		SubnodeConfiguration ftp = ConfigurationHelper.getInstance()
				.getConfig().configurationAt("//ftp");
		String ftpAddress = ftp.getString("@address");
		String ftpUsername = ftp.getString("@username");
		String ftpPassword = ftp.getString("@password");
		String ftpRemoteFile = ftp.getString("@remoteFile","downlink/10MB");
		
		for(int i=0;i<threadNumber;i++){
			FtpThread ftpDownlinkThread = new FtpThread(ftpAddress, ftpUsername, ftpPassword,
					ftpRemoteFile,"ftpDownloadTmp"+i, true);
			ftpDownlinkThread.addFtpListener(this);
			ftpDownlinkThread.start();
			ftpDownlinkThreads.add(ftpDownlinkThread);
		}

		statusLabel.setText("Downlink FTP traffic started using file '"
				+ ftpRemoteFile + "'.");
		startDownlinkTraffic.setText("Stop Downlink Traffic");
	}

	private void stopDownlinkTraffic() {
		for(int i=0;i<threadNumber;i++){
			FtpThread ftpDownlinkThread = ftpDownlinkThreads.get(i);
			ftpDownlinkThread.stopFtp();
		}
		ftpDownlinkThreads.clear();
		statusLabel.setText("FTP downlink traffic stopped.");
		startDownlinkTraffic.setText("Start Downlink Traffic");
	}

	private void stopUplinkTraffic() {
		for(int i=0;i<threadNumber;i++){
			FtpThread ftpUplinkThread = ftpUplinkThreads.get(i);
			ftpUplinkThread.stopFtp();
		}
		ftpUplinkThreads.clear();
		statusLabel.setText("FTP uplink traffic stopped.");
		startUplinkTraffic.setText("Start Uplink Traffic");
	}

	private void startUplinkTraffic() {
		SubnodeConfiguration ftp = ConfigurationHelper.getInstance()
				.getConfig().configurationAt("//ftp");
		String ftpAddress = ftp.getString("@address");
		String ftpUsername = ftp.getString("@username");
		String ftpPassword = ftp.getString("@password");
		String ftpLocalFile = ftp.getString("@localFile","lib/WiTT.jar");
		for(int i=0;i<threadNumber;i++){
			FtpThread ftpUplinkThread = new FtpThread(ftpAddress, ftpUsername, ftpPassword,
					ftpLocalFile,"ftpUploadTmp"+i, false);
			ftpUplinkThread.addFtpListener(this);
			ftpUplinkThread.start();
			ftpUplinkThreads.add(ftpUplinkThread);
		}

		statusLabel.setText("Uplink FTP traffic started using file '" + ftpLocalFile
				+ "'.");
		startUplinkTraffic.setText("Stop Uplink Traffic");
	}

	private void startLog() {
		startLog.setText("Stop Log");
		metricsUI.stopLog();
	}

	private void startNewLog() {
		startLog.setText("Stop Log");
		SimpleDateFormat format = new SimpleDateFormat("yyyyy_MMMMM_dd-HH_m_ss");
		logFileName = System.getProperty("user.home") + File.separator + mapUI.getSelectedMapName()
				+ format.format(new Date());
		try {
			metricsUI.startNewLog(logFileName);
			statusLabel.setText("Logging to " + logFileName);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Unable to start log: "
					+ e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE, null);
			log.error("ERROR", e);
			metricsUI.stopLog();
			startLog.setText("Start Log");
		}
	}

	private void stopAndCloseLog() {
		metricsUI.stopLog();
		metricsUI.closeLog();
		startLog.setText("Start Log");
		statusLabel.setText("Logging stopped.");
	}

	public void eventOccurred(FtpEvent ftpEvent) {
		statusLabel.setText(ftpEvent.getMessage());
		/*if (!ftpEvent.isConnected()) {
			if (ftpEvent.getSource() == ftpUplinkThread)
				startUplinkTraffic.setText("Start Uplink Traffic");
			else
				startDownlinkTraffic.setText("Start Downlink Traffic");
		}*/
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.clearwire.tools.wimax.bcs200.event.BCS200EventListener#eventOccurred
	 * (com.clearwire.tools.wimax.bcs200.event.BCS200Event)
	 */
	public void eventOccurred(BCS200Event arg0) {
		pingProcess.setToGateway();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.clearwire.tools.wimax.CPEi150.event.CPEi150EventListener#eventOccurred
	 * (com.clearwire.tools.wimax.CPEi150.event.CPEi150StatsEvent)
	 */
	public void eventOccurred(CPEi150StatsEvent arg0) {
		pingProcess.setAddress(cpei150Process.getGatewayIp());
	}

	private void saveConfiguration() {
		// local iperf saving
		/*
		 * String localIperfServer = localIperfPanel.getServer();
		 * SubnodeConfiguration localIperf =
		 * ConfigurationHelper.getInstance().getConfig
		 * ().configurationAt("//iperf/local");
		 * if((localIperfServer!=null)&&(localIperfServer.trim().length()!=0))
		 * localIperf.setProperty("@address", localIperfServer);
		 * 
		 * String localIperfPort = localIperfPanel.getPort();
		 * if((localIperfPort!=null)&&(localIperfPort.trim().length()!=0))
		 * localIperf.setProperty("@port", localIperfPort);
		 * 
		 * //remote iperf saving String remoteIperfServer =
		 * remoteIperfPanel.getServer(); SubnodeConfiguration remoteIperf =
		 * ConfigurationHelper
		 * .getInstance().getConfig().configurationAt("//iperf/remote");
		 * if((remoteIperfServer!=null)&&(remoteIperfServer.trim().length()!=0))
		 * remoteIperf.setProperty("@address", remoteIperfServer);
		 * 
		 * String remoteIperfPort = remoteIperfPanel.getPort();
		 * if((remoteIperfPort!=null)&&(remoteIperfPort.trim().length()!=0))
		 * remoteIperf.setProperty("@port", remoteIperfPort);
		 * 
		 * String remoteIperfSshAddress = remoteIperfPanel.getSshAddress();
		 * if((remoteIperfSshAddress
		 * !=null)&&(remoteIperfSshAddress.trim().length()!=0))
		 * remoteIperf.setProperty("@ssh-address", remoteIperfSshAddress);
		 * 
		 * String remoteIperfSshUsername = remoteIperfPanel.getSshUsername();
		 * if(
		 * (remoteIperfSshUsername!=null)&&(remoteIperfSshUsername.trim().length
		 * ()!=0)) remoteIperf.setProperty("@ssh-username",
		 * remoteIperfSshUsername);
		 * 
		 * String remoteIperfSshPassword = remoteIperfPanel.getSshPassword();
		 * if(
		 * (remoteIperfSshPassword!=null)&&(remoteIperfSshPassword.trim().length
		 * ()!=0)) remoteIperf.setProperty("@ssh-password",
		 * remoteIperfSshPassword);
		 * 
		 * //ftp SubnodeConfiguration ftp =
		 * ConfigurationHelper.getInstance().getConfig
		 * ().configurationAt("//ftp"); String ftpAddress =
		 * ftpPanel.getServerAddress();
		 * if((ftpAddress!=null)&&(ftpAddress.trim().length()!=0))
		 * ftp.setProperty("@address", ftpAddress);
		 * 
		 * String ftpUsername = ftpPanel.getUsername();
		 * if((ftpUsername!=null)&&(ftpUsername.trim().length()!=0))
		 * ftp.setProperty("@username", ftpUsername);
		 * 
		 * String ftpPassword = ftpPanel.getPassword();
		 * if((ftpPassword!=null)&&(ftpPassword.trim().length()!=0))
		 * ftp.setProperty("@password", ftpPassword);
		 * 
		 * String ftpFile = ftpPanel.getFile();
		 * if((ftpFile!=null)&&(ftpFile.trim().length()!=0))
		 * ftp.setProperty("@file", ftpFile);
		 */

		// set GPS port
//		String gpsPort = (String) gpsPorts.getSelectedItem();
//		SubnodeConfiguration gpsNode = ConfigurationHelper.getInstance()
//				.getConfig().configurationAt("//gps");
//		if (gpsPort != null)
//			gpsNode.setProperty("@port", gpsPort);
//
//		ConfigurationHelper.getInstance().saveConfiguration();
		BasesHelper.getInstance().saveConfiguration();
		MapsHelper.getInstance().saveConfiguration();
	}

	private void quit() {
		saveConfiguration();
		stop();
		this.dispose();
		log.debug("Exited.");
		System.exit(0);
	}

	private void start() {
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		Thread thread = new Thread() {
			public void run() {
				ModalProgressDialog dialog = new ModalProgressDialog(parent,
						"Starting...", true, "Starting...");
				dialog.setLocationRelativeTo(parent);
				dialog.showDialog();

				log.debug("Starting processes...");
				startMonitor.setText("Stop Monitor");
				startLog.setEnabled(true);
				try {
					if ((typeperf.getCounters() == null)
							|| (typeperf.getCounters().length == 0)) {
						log.debug("Loading typeperf counters...");
						typeperf.loadCounters();
					} else
						log.debug("Typerf counters already loaded.");

					log.debug("Starting Typeperf...");
					typeperf.start();
					statusLabel.setText("Typerperf started.");

					// cpe vs pc device logic

					if (cpei150Process.isAvailable()) {
						log.debug("Starting CPEi150 API...");
						startLog.setEnabled(true);
						cpei150Process.start(ConfigurationHelper.getInstance()
								.getConfig().getInt(
										"//cpei150/@polling-interval"));
						
						statusLabel.setText("CPEi150 API started.");
					} else {
						log.debug("Starting BCS200 API...");
						if (demoRadio.isSelected()) {
							startLog.setEnabled(false);
							bcs200Process.start(ConfigurationHelper
									.getInstance().getConfig().getInt(
											"//bcs200/@demo-polling-interval",
											3000), false);
							
							statusLabel.setText("Beceem API started.");
						} else if (commRadio.isSelected()) {
							startLog.setEnabled(true);
							bcs200Process.start(ConfigurationHelper
									.getInstance().getConfig().getInt(
											"//bcs200/@adv-polling-interval"),
									false);
							
							statusLabel.setText("Beceem API started.");
						} else if (advRadio.isSelected()) {
							startLog.setEnabled(true);
							bcs200Process.start(ConfigurationHelper
									.getInstance().getConfig().getInt(
											"//bcs200/@adv-polling-interval"),
									false);
							swin2Process
									.start(
											ConfigurationHelper
													.getInstance()
													.getConfig()
													.getString(
															"//swin2/@location"),
											ConfigurationHelper
													.getInstance()
													.getConfig()
													.getInt(
															"//swin2/@polling-interval"));
							statusLabel
									.setText("Beceem API and Swin API started.");
							
						} else {
							startLog.setEnabled(true);
							bcs200Process.start(ConfigurationHelper
									.getInstance().getConfig().getInt(
											"//bcs200/@adv-polling-interval"),
									true);
							swin2Process
									.start(
											ConfigurationHelper
													.getInstance()
													.getConfig()
													.getString(
															"//swin2/@location"),
											ConfigurationHelper
													.getInstance()
													.getConfig()
													.getInt(
															"//swin2/@polling-interval"));
							
							statusLabel
									.setText("Beceem API, Swin, and MAC messaging started.");
						}
					}

					demoRadio.setEnabled(false);
					commRadio.setEnabled(false);
					advRadio.setEnabled(false);
					expertRadio.setEnabled(false);
					pingProcess.start();

					try {
						String gpsSelection = (String) gpsPorts
								.getSelectedItem();
						if (gpsSelection.equals("Auto-detect"))
							gps.setGpsPort(null);
						else
							gps.setGpsPort(gpsSelection);

						log.debug("Starting GPS...");
						gps.start();
					} catch (Exception e) {
						JOptionPane.showMessageDialog(parent,
								"Unable to start GPS: " + e.getMessage(),
								"ERROR", JOptionPane.ERROR_MESSAGE, null);
					}
				} catch (TypeperfException te) {
					JOptionPane.showMessageDialog(parent,
							"Unable to start Typeperf: " + te.getMessage(),
							"ERROR", JOptionPane.ERROR_MESSAGE, null);
				} catch (BCS200Exception be) {
					JOptionPane.showMessageDialog(parent,
							"Unable to start Bcs200 process: "
									+ be.getMessage(), "ERROR",
							JOptionPane.ERROR_MESSAGE, null);
				} catch (Swin2Exception se) {
					JOptionPane
							.showMessageDialog(parent,
									"Unable to start Swin2 process: "
											+ se.getMessage(), "ERROR",
									JOptionPane.ERROR_MESSAGE, null);
				} catch (Exception e) {
					log.warn("WARN", e);
					statusLabel.setText("WARNING:" + e.toString());
				} finally {
					dialog.closeDialog();
					parent.setCursor(Cursor.getDefaultCursor());
				}
				log.debug("Start complete.");
			}
		};
		thread.start();
	}

	private void stop() {
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		Thread thread = new Thread() {
			public void run() {
				ModalProgressDialog dialog = new ModalProgressDialog(parent,
						"Stopping...", true, "Stopping...");
				dialog.setLocationRelativeTo(parent);
				dialog.showDialog();
				try {
					log.debug("Stopping gps, bcs200, and typeperf process...");
					typeperf.stop();
					bcs200Process.stop();
					swin2Process.stop();
					cpei150Process.stop();
					gps.stop();
					pingProcess.stop();
					startMonitor.setText("Start Monitor");
					startLog.setEnabled(false);
					demoRadio.setEnabled(true);
					commRadio.setEnabled(true);
					advRadio.setEnabled(true);
					expertRadio.setEnabled(true);
					// openLog.setEnabled(true);
					log.debug("Stopped gps, bcs200, and typeperf process.");
					stopAndCloseLog();
					statusLabel.setText("Stopped monitoring processes.");
				} catch (Exception e) {
					log.error("ERROR", e);
				} finally {
					System.gc();
					dialog.closeDialog();
					parent.setCursor(Cursor.getDefaultCursor());
				}
			}
		};
		thread.start();
	}

	public static void main(String[] args) {
		try {
			System.setProperty("Quaqua.tabLayoutPolicy", "wrap"

			);

			UIManager
					.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
			WiTTMain mainGui = new WiTTMain();
			mainGui.pack();
			mainGui.setSize(1000, 1000);
			// mainGui.setSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
			mainGui.setExtendedState(JFrame.MAXIMIZED_BOTH);
			mainGui.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void showLogFiles(){
		try {
			Runtime.getRuntime().exec("explorer "+System.getProperty("user.home"));
		}
		catch(Exception e){}
	}
	
	class ComPortCheckerThread extends Thread {
		
		public void run(){
			while(true){
				if(startMonitor.isEnabled()){
					String[] ports = GpsFactory.getGPSPorts();
					
					if(gpsPorts.getItemCount()!=ports.length){
						int selectedGps = gpsPorts.getSelectedIndex();
						gpsPorts.removeAllItems();
						for(int i=0;i<ports.length;i++)
							gpsPorts.addItem(ports[i]);
						gpsPorts.setSelectedIndex(selectedGps);
					}
					
				}
				try {
					Thread.sleep(5000);
				}
				catch(Exception e){}
			}
		}
	}

}
