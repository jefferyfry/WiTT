package com.clearwire.tools.wimax.fieldtesttool.ftp;

public class FtpTest implements FtpEventListener {

	public void eventOccurred(FtpEvent ftpEvent) {
		System.out.println(ftpEvent.getMessage());
	}
	
	public static void main(String[] args){
		FtpTest ftpTest = new FtpTest();
		FtpThread ftp = new FtpThread("www.test.com","test","test","test","500KB",true);
		ftp.addFtpListener(ftpTest);
		ftp.start();
		try {
			Thread.sleep(60000);
		}
		catch(Exception e){}
		ftp.stopFtp();
		
	}

}
