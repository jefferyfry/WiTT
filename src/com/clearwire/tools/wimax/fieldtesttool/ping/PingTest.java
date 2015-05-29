package com.clearwire.tools.wimax.fieldtesttool.ping;

public class PingTest implements PingEventListener {

	public void eventOccurred(PingEvent pingEvent) {
		System.out.println(pingEvent.getTime());
	}
	
	public static void main(String[] args){
		PingTest pingTest = new PingTest();
		Ping ping = new Ping("172.20.139.1",500);
		ping.addPingListener(pingTest);
		ping.start();
		try {
			Thread.sleep(60000);
		}
		catch(Exception e){}
		ping.stop();
	}

}
