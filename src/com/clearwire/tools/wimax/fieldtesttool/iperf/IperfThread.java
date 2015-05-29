package com.clearwire.tools.wimax.fieldtesttool.iperf;
//Notes:
//-The ParseLine results variable still ends up with a blank 0th string
// which may or may not ever matter (DC)

import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;

import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clearwire.tools.wimax.fieldtesttool.ui.WiTTMain;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.util.Vector;
import java.util.regex.*;


public class IperfThread extends Thread {
  
	private Log log = LogFactory.getLog(IperfThread.class);
	
  String command;
  String output;
  Process process; 
  JTextArea text;
  Vector FinalResults;
  int streamId;
  private String sshServer=null;
  private String sshUsername=null;
  private String sshPassword=null;
  private Session session = null;
  private IperfPanel panel=null;

  public IperfThread(IperfPanel panel,String x, JTextArea o) {
	  this.panel=panel;
    command = x;
    text = o;
    FinalResults = new Vector();
    text.append(x);
    text.append("\n");
  }
  
  public IperfThread(IperfPanel panel,String x, JTextArea o,String sshServer,String sshUsername,String sshPassword){
	  this(panel,x,o);
	  this.sshServer=sshServer;
	  this.sshUsername=sshUsername;
	  this.sshPassword=sshPassword;
  }

  public void run( ) {
	  
	  BufferedReader input = null;
	  BufferedReader errors = null;
	  
	if((sshServer!=null)&&(sshUsername!=null)&&(sshPassword!=null)&&(sshServer.trim().length()!=0)&&(sshUsername.trim().length()!=0)&&(sshPassword.trim().length()!=0)){
		JSch jsch = new JSch();
		try {
			session = jsch.getSession(sshUsername, sshServer, 22);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(sshPassword);
			session.connect(30000);
			Channel channel = session.openChannel("shell");
			channel.connect(30000);
			input = new BufferedReader(new InputStreamReader(channel.getInputStream()));
			PrintWriter out = new PrintWriter(channel.getOutputStream());
			log.debug("Sending '"+command+"' to remote SSH server.");
			out.print(command+"\r");
			out.flush();
		}
		catch(Exception e) {
	      e.printStackTrace();
	      return;
	    }
	}
	else {
		 //try to run Iperf, if we get an error, display it to the user.
	    try {
	    log.debug("Sending '"+command+"' to start local iperf.");
	      process = Runtime.getRuntime().exec(command);
	    }
	    catch(IOException e) {
	      e.printStackTrace();
	      return;
	    }
	    
	    //read in the output from Iperf
	    //BufferedReader output = new BufferedReader(new OutputStreamReader(process.getOutputStream()));
	    input = new BufferedReader(new InputStreamReader(process.getInputStream()));
	    errors = new BufferedReader(new InputStreamReader(process.getErrorStream()));
	}
    
    try {
      String input_line;
      input_line = input.readLine();
      while(input_line != null) {
    	  try {
    		  Thread.sleep(100);
    	  }
    	  catch(Exception e){}
    	  //parseLine(input_line);
    	  if(text.getRows()>100)
    		  text.setText("");
    	  text.append(input_line);
    	  text.append("\n");
    	  input_line = input.readLine();
    	  text.setCaretPosition(text.getDocument().getLength());
      }

      try {
	      String error_line;
	      error_line = errors.readLine();
	      while(error_line != null) {
			text.append(error_line);
			text.append("\n");
			error_line = errors.readLine();
	      }
      }
      catch(Exception e){}
      process = null;
      panel.changeState(true,false);
      //panel.createBWGraph(FinalResults);
      //IperfPanel.createJitterGraph(FinalResults);
      text.append("Done.\n");
    }

    catch(IOException e) {
      //don't do anything?
      text.append("\nError in Iperf thread.\n");
    }

  }
  
  public void quit( ) {
	  if((sshServer==null)||(sshServer.trim().length()==0)){
	    if( process != null ){
	    	String[] command = {
					"taskkill",
					"/F",
					"/IM",
					"iperf.exe"};
			try {
				Runtime.getRuntime().exec(command);
			}
			catch(IOException e){
				
			}
	      process.destroy();
	    }
	  }
	  else {
		  session.disconnect();
	  }
	  log.debug("Iperf Stopped");
    panel.changeState(true,false);
    
  }

  public void parseLine(String line) {
    //only want the actual output lines
    if(line.matches("\\[[ \\d]+\\]  [\\d]+.*")) {

      //Pattern p = Pattern.compile("[ ]+");
      Pattern p = Pattern.compile("[-\\[\\]\\s]+");
      //ok now break up the line into id#, interval, amount transfered, format transferred, bandwidth, and format of  bandwidth
      String[] results = p.split(line);

      
      //get the ID # for the stream
      Integer temp = new Integer(results[1].trim());
      int id = temp.intValue();

      //temp for now.. need to figure out how to do this like I do in C++ with find
      boolean found = false;
      IperfStreamResult streamResult = 	new IperfStreamResult(id);
      for(int i=0; i < FinalResults.size(); ++i) {
	if(((IperfStreamResult)FinalResults.elementAt(i)).getID() == id) {
	  streamResult = (IperfStreamResult)FinalResults.elementAt(i);
	  found = true;
	  break;
	}
      }

      if(!found)
	FinalResults.add(streamResult);

      //this is TCP or Client UDP
      if(results.length == 9) {
	Double start = new Double(results[2].trim());
	Double end = new Double(results[3].trim());
	Double bw = new Double(results[7].trim());
	
	Measurement M = new Measurement(start.doubleValue(), end.doubleValue(), bw.doubleValue(), results[8]);
	streamResult.addBW(M);
      }
      else if(results.length == 14) {

	//results[2] = results[2].substring(0,results[2].lastIndexOf("-"));
	Double start = new Double(results[2].trim());
	Double end = new Double(results[3].trim());
	Double bw = new Double(results[7].trim());
	
	Measurement M = new Measurement(start.doubleValue(), end.doubleValue(), bw.doubleValue(), results[7]);
	streamResult.addBW(M);
	
	Double jitter = new Double(results[9].trim());
	M = new Measurement(start.doubleValue(), end.doubleValue(), jitter.doubleValue(), results[10]);
	streamResult.addJitter(M);

	
      } 
    }
  }
}
