package com.clearwire.tools.wimax.fieldtesttool.ftp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class FtpThread extends Thread {
	
	private Log log = LogFactory.getLog(FtpThread.class);
	
	private String server;
	private String username;
	private String password;
	private String file;
	private String destFile;
		
	private boolean ftpGo=true;
	
	private ArrayList<FtpEventListener> ftpListeners = new ArrayList<FtpEventListener>();
	
	private FTPClient ftp=new FTPClient();
	
	private boolean downlink=true;
	
	public FtpThread(String server, String username, String password,
			String file,String destFile,boolean downlink) {
		super();
		this.server = server;
		this.username = username;
		this.password = password;
		this.file = file;
		this.destFile=destFile;
		this.downlink=downlink;
	}

	public void run(){
		
		while(ftpGo){
				try {
					ftp.connect(server);
				}
				catch(Exception e){
					fireFtpEventOccurred(new FtpEvent(this,"Unable to connect to server: "+e.getMessage(),false));
					ftpGo=false;
				}
				try {
			    ftp.login(username, password);
			    
			    int reply = ftp.getReplyCode();
	
			    if(!FTPReply.isPositiveCompletion(reply)) {
			    	ftp.disconnect();
			    	fireFtpEventOccurred(new FtpEvent(this,"FTP server refused connection.",false));
			    }
			    
			    while(ftpGo){
			    	fireFtpEventOccurred(new FtpEvent(this,"Retrieving "+file+".",true));
			    	if(downlink) {
			    		log.debug("Downloading "+file+" to "+destFile);
				    	FileOutputStream tmpFile = new FileOutputStream(destFile);
				    	ftp.retrieveFile(file, tmpFile);
			    	}
			    	else {
			    		log.debug("Putting "+file+" to "+destFile);
			    		FileInputStream localFile = new FileInputStream(file);
			    		ftp.storeFile(destFile, localFile);
			    	}
			    	fireFtpEventOccurred(new FtpEvent(this,"Finished retrieving "+file+".",true));		    	
			    }
			    ftp.logout();
			    ftp.disconnect();
			    fireFtpEventOccurred(new FtpEvent(this,"Retrieving "+file+".",false));
			}
			catch(Exception e){
				if(ftpGo)
					fireFtpEventOccurred(new FtpEvent(this,"Unable to retrieve file: "+e.getMessage(),false));
				else
					fireFtpEventOccurred(new FtpEvent(this,"FTP Stopped.",false));
				
				try {
					Thread.sleep(1000);
				}
				catch(Exception f){}
			}
		}
	}
	
	public void fireFtpEventOccurred(FtpEvent event){
		log.debug("Firing Ftp event.");
		for(Iterator<FtpEventListener> iter = ftpListeners.iterator();iter.hasNext();){
			FtpEventListener listener = (FtpEventListener)iter.next();
			listener.eventOccurred(event);
		}
	}
	
	public void addFtpListener(FtpEventListener listener){
		ftpListeners.add(listener);
	}
	
	public void removeFtpListener(FtpEventListener listener){
		ftpListeners.remove(listener);
	}
	
	public void stopFtp(){
		ftpGo=false;
		
		Thread quitThread = new Thread(){
			public void run(){
				try {
					ftp.abort();
					ftp.disconnect();
				}
				catch(Exception e){
					fireFtpEventOccurred(new FtpEvent(this,"Unable to abort ftp: "+e.getMessage(),false));
				}
			}
		};
		quitThread.start();
		fireFtpEventOccurred(new FtpEvent(this,"Stopped FTP.",false));
	}

	/**
	 * @return the server
	 */
	public String getServer() {
		return server;
	}



	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}



	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}



	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}



	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}



	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}



	/**
	 * @return the file
	 */
	public String getFile() {
		return file;
	}



	/**
	 * @param file the file to set
	 */
	public void setFile(String file) {
		this.file = file;
	}
}
