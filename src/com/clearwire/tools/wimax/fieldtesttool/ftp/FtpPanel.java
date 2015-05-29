package com.clearwire.tools.wimax.fieldtesttool.ftp;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class FtpPanel extends JPanel {
	
	private JLabel serverLabel = new JLabel("FTP Server");
	private JTextField serverField = new JTextField();
	private JLabel usernameLabel = new JLabel("Username");
	private JTextField usernameField = new JTextField();
	private JLabel passwordLabel = new JLabel("Password");
	private JPasswordField passwordField = new JPasswordField();
	private JLabel remoteFileLabel = new JLabel("Remote File for Download");
	private JTextField remoteFileField = new JTextField();
	private JLabel localFileLabel = new JLabel("Local File for Upload");
	private JTextField localFileField = new JTextField();
	
	public FtpPanel(String ftpAddress,String username,String password,String remoteFile,String localFile){
		super(new GridLayout(6,1));
		JPanel serverPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		serverPanel.add(serverLabel);
		serverPanel.add(serverField);
		serverField.setColumns(20);
		serverField.setText(ftpAddress);
		add(serverPanel);

		JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		usernamePanel.add(usernameLabel);
		usernamePanel.add(usernameField);
		usernameField.setColumns(20);
		usernameField.setText(username);
		add(usernamePanel);

		JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		passwordPanel.add(passwordLabel);
		passwordPanel.add(passwordField);
		passwordField.setColumns(20);
		passwordField.setText(password);
		add(passwordPanel);

		JPanel remoteFilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		remoteFilePanel.add(remoteFileLabel);
		remoteFilePanel.add(remoteFileField);
		remoteFileField.setColumns(20);
		remoteFileField.setText(remoteFile);
		add(remoteFilePanel);
		
		JPanel localFilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		localFilePanel.add(localFileLabel);
		localFilePanel.add(localFileField);
		localFileField.setColumns(20);
		localFileField.setText(localFile);
		add(localFilePanel);
				
		/*JPanel buttonPanel = new JPanel();
		buttonPanel.add(actionButton);
		add(buttonPanel);
		actionButton.addActionListener(this);*/
	}
	
	public String getServerAddress(){
		return serverField.getText().trim();
	}
	
	public String getUsername(){
		return usernameField.getText().trim();
	}

	public String getPassword(){
		return passwordField.getText().trim();
	}
	
	public String getRemoteFile(){
		return remoteFileField.getText().trim();
	}
	
	public String getLocalFile(){
		return localFileField.getText().trim();
	}

	/* (non-Javadoc)
	 * @see com.clearwire.tools.wimax.fieldtesttool.ftp.FtpEventListener#eventOccurred(com.clearwire.tools.wimax.fieldtesttool.ftp.FtpEvent)
	 *
	public void eventOccurred(FtpEvent ftpEvent) {
		status.setText(ftpEvent.getMessage());
		if(!ftpEvent.isConnected())
			actionButton.setText("Start");
	}
	
	private void startFtp(){
		if((serverField.getText().trim().length()==0)||
				(usernameField.getText().trim().length()==0)||
				(passwordField.getText().trim().length()==0)||
				(fileField.getText().trim().length()==0))
			JOptionPane.showMessageDialog(this, "Please complete all FTP inputs.","ERROR",JOptionPane.ERROR_MESSAGE,null);
		else {
			actionButton.setText("Stop");
			ftpThread = new FtpThread(serverField.getText(),usernameField.getText(),passwordField.getText(),fileField.getText());
			ftpThread.addFtpListener(this);
			ftpThread.start();
		}
	}
	
	private void stopFtp(){
		if(ftpThread!=null)
			ftpThread.stopFtp();
	}*/
}
