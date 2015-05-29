package com.clearwire.tools.wimax.fieldtesttool.ui;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class ModalProgressDialog extends JDialog {
	
	private JProgressBar progressBar = new JProgressBar (JProgressBar.HORIZONTAL);
	
	public ModalProgressDialog(JFrame parent, String title, boolean modal,String message) {
		super(parent, title, modal);
		progressBar.setBorderPainted(true);
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setString(message);
		this.getContentPane().add(progressBar);
		setUndecorated(true);
		pack();
	}
	
	public void setMessage(String message){
		progressBar.setString(message);
	}
	
	public void showDialog(){
		Thread dialogThread = new Thread(){
			public void run(){
				setVisible(true);
			}
		};
		dialogThread.start();
	}
	
	public void closeDialog(){
		dispose();
	}

}
