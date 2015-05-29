package com.clearwire.tools.wimax.fieldtesttool.ui;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class SplashDialog extends JDialog {
	
	private JProgressBar progressBar = new JProgressBar (JProgressBar.HORIZONTAL);
	
	public SplashDialog(JFrame parent, String title, boolean modal,ImageIcon icon) {
		super(parent, title, modal);
		progressBar.setBorderPainted(true);
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		this.getContentPane().add(progressBar,BorderLayout.SOUTH);
		setUndecorated(true);
		pack();

	}
	
	public SplashDialog(JFrame parent, String title, boolean modal,ImageIcon icon,String message) {
		super(parent, title, modal);
		progressBar.setBorderPainted(true);
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setString(message);
		JLabel imageIcon = new JLabel(icon);
		this.getContentPane().add(imageIcon,BorderLayout.CENTER);
		this.getContentPane().add(progressBar,BorderLayout.SOUTH);
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
