/*
Copyright (C) 2016  R.W. Sutnavage

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/.
 */
package com.bobs0327;


import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import miscMethods.checkSerialPort;
import miscMethods.getIPAddressAndHostname;
import miscMethods.miscellaneous;
import miscMethods.sendMailSSL;
import schedulerMethods.DailyIterator;
import schedulerMethods.Scheduler;
import schedulerMethods.SchedulerTask;
import upbHttpServerMethods.HTTPServerFileDownload;
import upbHttpServerMethods.HttpRequestHandler;
import upbHttpServerMethods.UPBHttpServer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.Date;
import databaseMethods.addUPERecords2DB;
import databaseMethods.updateDatabase;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

class TaskScheduler
{
	private final Scheduler scheduler = new Scheduler();
	private final SimpleDateFormat dateFormat =
			new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
	private  int hourOfDay, minute, second;

	public TaskScheduler(int hourOfDay, int minute, int second) {
		this.hourOfDay = hourOfDay;
		this.minute = minute;
		this.second = second;
	}

	public void start() {
		scheduler.schedule(new SchedulerTask() {
			public void run() {
				executeTask();

			}
			private void executeTask() {
				String checkSerPort = "";
				String checkNetwork = "";
				int rowCount = -1;
				String ipAndHostname = getIPAddressAndHostname.getIPAddresAndHostname();
				checkSerialPort csp = new checkSerialPort();
				if(csp.run() == false)
				{
					checkSerPort = "Serial Port check FAILED!!!";
				}
				else
				{
					checkSerPort = "Serial Port check SUCCESSFUL!!!";	
				}
				try {
					if(miscellaneous.getNetworkStatus("http://google.com") == true)
					{
						checkNetwork = "Network check SUCCESSFUL";	
					}
					else
					{
						checkNetwork = "Network check FAILED";
					}
				} catch (IOException ex) {
					updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}

				SimpleDateFormat simpleDateFormat = 
						new SimpleDateFormat("dd/M/yyyy hh:mm:ss");

				String elapsedTime = "";
				try {
					Date date1 = simpleDateFormat.parse(upbServer.appStartDate);
					Date date2 = simpleDateFormat.parse(upbServer.getCurrentDateTime());
					elapsedTime = upbServer.DateDifference(date1, date2);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				 rowCount = updateDatabase.getTableRowCount(upbServer.dbName, "loginfo"); 
			//	String emailString = upbServer.getDateandTime() + "\n\n" + ipAndHostname + "\n\n" + checkSerPort + "\n\n" + checkNetwork + "\n\n" +elapsedTime;
					String emailString = upbServer.getDateandTime() +  "\n\n" + ipAndHostname + "\n\n" + checkSerPort + "\n\n" + checkNetwork + "\n\n" + "loginfo table contains: " + rowCount +" record(s)" +   "\n\n"  + elapsedTime ;
				 
				 sendMailSSL sm = new sendMailSSL(upbServer.emailUserid, upbServer.emailPassword, upbServer.toEmail, upbServer.fromEmail, emailString); 
				sm.run();                  


			}
		}, new DailyIterator(hourOfDay, minute, second));
	}
}

public class upbServer extends Thread
{

	public static boolean bServerStarted = false;
	static int prevModuleId;
	static boolean bAlreadyUpdated = false;
	static StringBuilder sb = new StringBuilder();
	ArrayList<String> obj = new ArrayList<String>();
	public static int[] deviceIDArray = new int[250];
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	static String configfile;
	static String  portIn;
	static String comm = "COM4";
	static int port = 3306;
	static public String sourceID;
	static String delayTemp;
	public static int cmdDelay;
	static String expfilename;
	public static String dbName;
	static String httpContext;
	static public String networkID;
	static 	SerialPort serialPort; 	
	static int _portNumber; 
	public static int webDelay;
	public static String productCSVfile;
	public static String fromEmail;
	public static String toEmail;
	public static String emailUserid;
	public static String emailPassword;
	public static String[] diagRunTime;
	static int diagHour;
	static int diagMinute;
	public static String appStartDate;
	public static String networkInterface;
	public static int portUsedForFileDownload = 0;
	public static String serverIPAddress;
	public static String configProperties = "config.properties";
	final static long fONCE_PER_DAY = 1000*60*60*24;
	static int rowCount = 0;


	private final Scheduler scheduler = new Scheduler();
	private final SimpleDateFormat dateFormat =
			new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
	private int hourOfDay, minute, second;  

	static ServerSocket Server = null;

	public static void main(String[] args) 
	{


		//		String info = upbServer.getDateandTime().toString()+ "upbServerStarted";
		//		updateDatabase.insertLogRecord(dbName,"xxx", info );
		//		System.exit(0);
		if(args.length > 0)
		{
			configProperties = args[0];
			File varFile = new File(configProperties);
			if(varFile.exists() != true)
			{
				System.out.println("Cannot locate" + args[0] + " defaulting to config.properties");
				configProperties = "config.properties";	 
			}
			else
			{
				System.out.println("Using :" + args[0]);	
			}
		}
		else
		{
			System.out.println("Using : config.properties");	
		}

		appStartDate =	getCurrentDateTime();
		System.out.println("Initializing application");

		initializeApplication();

		try {
			serialPort = new SerialPort(comm); 
			_portNumber = port; //Arbitrary port number
			serialPort.openPort(); //Open port
			serialPort.setParams(4800, 8, 1, 0); //Set params for UPB PIM
			int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
			serialPort.addEventListener(new SerialPortReader()); //Add SerialPortEventListener
		} catch (Exception ex) {
			updateDatabase.insertLogRecord(dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			System.out.println("I/O failure: " + ex.getMessage());
			ex.printStackTrace();
		}
		updateDatabase.loadDeviceIDArray( dbName, deviceIDArray);	
		moduleVariables mvInput = new moduleVariables();
		buildCmd bc = new buildCmd();
		//		mvInput.clear();

		//		mvInput.moduleid =  0x03;  //deviceIDArray[index];
		//		mvInput.sourceid = Integer.parseInt(sourceID);
		//		mvInput.networkid = Integer.parseInt(networkID);;
		//		mvInput.isDevice = false;
		//		mvInput.action = 0x20;  // Activate Link # 0x03

		//		bc.buildCmd(mvInput);
		String 	myCmd = mvInput.message.toString();
		String dateStr = getDateandTime();
		System.out.println("Updating database..");
		for(int index = 0; index <= deviceIDArray.length; index++)
		{
			if(deviceIDArray[index] == 0)
				break;
			mvInput.clear();
			mvInput.moduleid = deviceIDArray[index];
			mvInput.sourceid = Integer.parseInt(sourceID);
			mvInput.networkid = Integer.parseInt(networkID);;
			mvInput.isDevice = true;
			mvInput.action = 0x30;  // report State Command

			bc.buildCmd(mvInput);
			myCmd = mvInput.message.toString();
			dateStr = getDateandTime();

			System.out.println(dateStr + " Sent: "+ myCmd);
			sendCmd(myCmd);
			try {
				Thread.sleep(cmdDelay*1000);
			} catch (InterruptedException ex) {
				updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
				ex.printStackTrace();
			}
		}
		upbHttpServerMethods.UPBHttpServer upbHttpServer = new UPBHttpServer(port, httpContext,
				new HttpRequestHandler());
		upbHttpServer.start();


		TaskScheduler ts = new TaskScheduler(diagHour, diagMinute, 0);
		ts.start();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException ex) {
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			ex.printStackTrace();
		}
		int rowCount = updateDatabase.getTableRowCount(dbName, "loginfo"); 
		runDiagnosticsReport(true);

		System.out.println("Application now running");
		updateDatabase.insertLogRecord(dbName, upbServer.getDateandTime(),"upbServer STARTED!!!" );

		try {
			Server = new ServerSocket (8081, 10, InetAddress.getByName(serverIPAddress));
		} catch (IOException ex) {
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			ex.printStackTrace();
		}         
		System.out.println ("TCPServer: "+ serverIPAddress + "  Waiting for client on port 8081");

		while(true) {	                	   	      	
			Socket connected = null;
			try {
				connected = Server.accept();
			} catch (IOException ex) {
				updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );				
				ex.printStackTrace();
			}
			(new HTTPServerFileDownload(connected)).start();
		}    
	}

	public static void initializeApplication()
	{
		try {
			configfile = new File(configProperties).getAbsolutePath();
			//	File file = new File(configfile);
			FileInputStream fileInput = new FileInputStream(configfile);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();

			portIn = properties.getProperty("port");
			comm = properties.getProperty("comm");
			expfilename = properties.getProperty("upbexpfilefullpathname");
			dbName = properties.getProperty("dbname");
			port  =  Integer.parseInt(portIn); 
			sourceID = properties.getProperty("sourceid");
			delayTemp = properties.getProperty("delaybetweencommands");
			cmdDelay = Integer.parseInt(delayTemp); 
			httpContext = properties.getProperty("httpcontext");
			networkID = properties.getProperty("networkid");
			productCSVfile = properties.getProperty("productcsvfile");
			delayTemp = properties.getProperty("webresponsedelay");
			webDelay = Integer.parseInt(delayTemp);
			fromEmail = properties.getProperty("fromemail");
			fromEmail = properties.getProperty("fromemail");
			toEmail = properties.getProperty("toemail");
			emailUserid = properties.getProperty("emailuserid");
			emailPassword = properties.getProperty("emailpassword");
			diagRunTime = properties.getProperty("diagnosticRunTime").split(",");
			diagHour  =  Integer.parseInt(diagRunTime[0]);
			diagMinute  =  Integer.parseInt(diagRunTime[1]);
			networkInterface = properties.getProperty("networkinterface");
			portIn = null;
			portIn = properties.getProperty("portusedforfiledownload");
			portUsedForFileDownload  =  Integer.parseInt(portIn); 

		} catch (FileNotFoundException ex) {
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );			
			ex.printStackTrace();
			System.err.println("Aborting, cannot open "+ configfile);
			System.exit(0);

		} catch (IOException ex) {
			ex.printStackTrace();
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			System.err.println("Aborting, cannot read "+ configfile);
			System.exit(0);
		}

		addUPERecords2DB.addUPERecords(dbName, expfilename);	
		updateDatabase.clearTables(dbName, "loginfo");
	}

	public static class upbCommandProcessor {
		private static final int Initialize = 0;
		private static final int GetCommand = 1;
		private static boolean bJustStarted = true;
		private int state = Initialize;
		private String inputCmd =  null;

		public String processInput(String clientRequest) {
			String reply = null;
			try {
				if(clientRequest != null && clientRequest.equalsIgnoreCase("exit")) {
					return "exit";
				}
				inputCmd = clientRequest;

				if(bJustStarted == true)
				{
					reply = "Server started";
					bJustStarted = false;
				}
				else
				{
					System.out.println(inputCmd);
					reply = inputCmd;
					reply = "TTT";
					reply = "EEEE";
				}
			} catch(Exception ex) {
				updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
				System.out.println("input process falied: " + ex.getMessage());
				return "exit";
			}
			return reply;
		}
	}

	static class SerialPortReader implements SerialPortEventListener {

		public void serialEvent(SerialPortEvent event) {
			if(event.isRXCHAR()){ //If data is available
				try {
					final byte buffer[] = serialPort.readBytes(event.getEventValue());
					final String readed = new String(buffer);
					if(buffer[0] != '\r')
					{
						sb.append(readed);
					}
					else
						sb.append("");
					if(buffer[0] == 0x0D)
					{
						if (sb.toString().startsWith("PU"))
						{
							processData(sb);
						}
						sb.setLength(0);
					}
				}
				catch (SerialPortException ex) {
					updateDatabase.insertLogRecord(dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
					System.out.println(ex);
				}
			}
			else if(event.isCTS()){ //If CTS line has changed state
				if(event.getEventValue() == 1){ //If line is ON
					System.out.println("CTS - ON");
				}
				else {
					System.out.println("CTS - OFF");
				}
			}
			else if(event.isDSR()){ //If DSR line has changed state
				if(event.getEventValue() == 1){ //If line is ON
					System.out.println("DSR - ON");
				}
				else {
					System.out.println("DSR - OFF");
				}
			}
		}
	}

	public void close() {
		try {
			serialPort.removeEventListener();
			serialPort.closePort();
		} catch (SerialPortException ex) {
			updateDatabase.insertLogRecord(dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );			
		}
	}

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static synchronized void processData(StringBuilder theMessage)
	{
		if (theMessage.toString().startsWith("PA"))
		{
			System.out.println("PA   PIM ACK'd current message");
		}
		if (theMessage.toString().startsWith("PE"))
		{
			System.out.println("PE   PIM NAK'd message ");
		}
		if (theMessage.toString().startsWith("PB"))
		{
			System.out.println("PB   PIM too BUSY to accept message  ");
		}

		if (theMessage.toString().startsWith("PK"))
		{
			System.out.println("PK   UPB Device ACK'd current message  ");
		}
		if (theMessage.toString().startsWith("PU")) {
			String dateStr = getDateandTime();
			System.out.println("\n" + dateStr + " Received: " + theMessage.toString());

			int messageLen = theMessage.length();
			int messageBody[] = new int[(messageLen / 2) - 2];
			int checkSum = 0;
			int messagePtr = 0;
			char firstChar = 0, secondChar = 0;
			int theValue = 0;
			checkSum = 0;
			for (int charPtr = 2; charPtr < messageLen; ) {
				firstChar = theMessage.charAt(charPtr++);
				secondChar = theMessage.charAt(charPtr++);
				theValue = ((firstChar >= 'A' ? (firstChar - 'A') + 10 : (firstChar - '0')) << 4)
						| (secondChar >= 'A' ? (secondChar - 'A') + 10 : (secondChar - '0'));

				if (messagePtr < messageBody.length) {
					checkSum += theValue;
					messageBody[messagePtr++] = theValue;
					continue;
				}
				checkSum = (-checkSum & 0xff);
				System.out.println("Checksum = " + checkSum + "  theValue =  "+theValue );		
				if (checkSum != theValue) {
					System.err.println(System.lineSeparator() + "Check sum on received UPB packet failed -- should be " + checkSum + " but received as " + theValue);
					System.err.println(System.lineSeparator() +"   BAD MESSAGE[" + theMessage + "], " + theMessage.length() + " bytes");
					return;
				}
			}
			// Dispatch valid UPP message
			parseUPBMessage(messageBody);
			return;
		}
	}

	private static void parseUPBMessage(int theMessage[]) {
		String sBlink = "";
		String hex;
		String newline = System.getProperty("line.separator");
		/** Used to indicate the device should use it's default fade rate */
		final int DEFAULT_FADE_RATE = 255;
		final int UPBMSG_BODY = 6;
		/** Used to indicate invalid/unassigned device state */
		final int UNASSIGNED_DEVICE_STATE = -1;
		int parsedLinkID = 0;
		int parsedChannel = 0;
		int parsedLevel = UNASSIGNED_DEVICE_STATE;
		int parsedFadeRate = DEFAULT_FADE_RATE;
		int parsedBlinkRate;
		int parsedDestID;
		int level = 0;

		parsedDestID = theMessage[3];

		boolean linkPacket = (theMessage[0] & 0x80) != 0;
		if(linkPacket == true)
			System.out.println(newline +"This is a Link");
		else
			System.out.println(newline + "This is a Device");

		// See if this message is asking for an ACK or not
		if (((theMessage[1] >> 4) & 0x07) != 0) {
			System.out.println("Message indicates a ACK is requested");
		}	
		else
			System.out.println("Message indicates a ACK is *NOT* requested");  

		int transeq = theMessage[1] & 3;
		System.out.println("Transmission sequence = " + ++transeq);
		int transcount = theMessage[1]>> 2 & 3;

		System.out.println("Transmission count = " + ++transcount );
		int networkID = theMessage[2];
		System.out.format("Network ID is " + "is %d"+ newline ,networkID); 

		int messageID = theMessage[5];
		System.out.format("Message ID is %d"+ newline ,messageID); 

		System.out.format("Destination ID is %d"+ newline ,parsedDestID); 
		if(messageID != 0x20 && messageID != 0x21)
			updateDatabase.findDeviceRecord( dbName, parsedDestID);
		int sourceID = theMessage[4];
		hex = Integer.toHexString(sourceID);
		System.out.format("Source ID is %d " + newline ,sourceID); 
		updateDatabase.findDeviceRecord( dbName, sourceID);

		int messageSet = (messageID >> 5) & 0x07;
		System.out.format("Message Set is %d"+ newline ,messageSet); 

		if (theMessage.length > UPBMSG_BODY + 1)
		{
			parsedFadeRate = theMessage[UPBMSG_BODY + 1];
		}
		System.out.format("Fade Rate is %d"+ newline ,parsedFadeRate); 

		// Decode the message type
		switch (messageID) {
		case 0x20: // Activate Link Command
			System.out.println("Activate Link Command");
			updateDatabase.updateDevicesFromLinkCmd( upbServer.dbName,  parsedDestID, true);
			break;

		case 0x21:  // Deactivate Link Command
			System.out.println("Deactivate Link Command");
			updateDatabase.updateDevicesFromLinkCmd( upbServer.dbName,  parsedDestID, false);
			break;

		case 0x22: // Goto Command
			System.out.println("GOTO Command");
			if (theMessage.length > UPBMSG_BODY)
			{
				parsedLevel = theMessage[UPBMSG_BODY];
			}
			updateDatabase.updateStatusTable( upbServer.dbName , parsedDestID ,parsedLevel,parsedFadeRate, "");
			break;
		case 0x23: // Fade Start Command

			if (theMessage.length == UPBMSG_BODY + 2) // Link id AND Fade rate
			{
				parsedLevel   = theMessage[UPBMSG_BODY];
				parsedFadeRate = theMessage[UPBMSG_BODY + 1];
			}
			else if (theMessage.length == UPBMSG_BODY + 1)
			{
				parsedLevel   = theMessage[UPBMSG_BODY];
				parsedFadeRate = DEFAULT_FADE_RATE;
			}
			else
			{
				if (theMessage.length == UPBMSG_BODY + 3) // Link id AND Fade rate
				{
					parsedLevel   = theMessage[UPBMSG_BODY ];
					parsedFadeRate = theMessage[UPBMSG_BODY + 1];
					parsedChannel = theMessage[UPBMSG_BODY + 2];
				}
				else if (theMessage.length == UPBMSG_BODY + 2) // Link id AND Fade rate
				{
					parsedLevel   = theMessage[UPBMSG_BODY];
					parsedFadeRate = theMessage[UPBMSG_BODY + 1];
					parsedChannel = 0;
				}
				else if (theMessage.length == UPBMSG_BODY + 1) // Link id AND Fade rate
				{
					parsedLevel   = theMessage[UPBMSG_BODY + 1];
					parsedFadeRate = DEFAULT_FADE_RATE;
					parsedChannel = 0;
				}
			}
			if(linkPacket  == false)
			{
				String timeStamp = "Last Update: " + upbServer.getDateandTime();
				updateDatabase.updateStatusTable( upbServer.dbName  , parsedDestID ,parsedLevel, parsedFadeRate, timeStamp);
			}
			break;

		case 0x24:  // Fade Stop Command
			System.out.println("Fade Stop Command");
			break;

		case 0x25:  // Blink
			if(linkPacket  == false)  // Direct packet
			{
				parsedBlinkRate =  theMessage[UPBMSG_BODY];
				parsedChannel =  theMessage[UPBMSG_BODY + 1];
				if(parsedBlinkRate > 0)
				{
					sBlink = "BLINK ON Blink Rate is " + parsedBlinkRate ;
				}
				else sBlink = "";
				updateDatabase.updateStatusTable( upbServer.dbName  , parsedDestID ,100, parsedFadeRate, sBlink);
			}
			else  // Link
			{
				parsedBlinkRate =  theMessage[UPBMSG_BODY];
			}
			break;

		case 0x86:
			parsedLevel = theMessage[6];
			System.out.format("Level is  %d"+newline, parsedLevel  );

			updateDatabase.updateStatusTable( upbServer.dbName  , sourceID ,parsedLevel, parsedFadeRate, "");
			break;

		default:
			break;
		}
		System.out.println(System.lineSeparator());
		return;
	}

	public static void sendCmd(String inCmd)
	{
		byte[] cr = {0x0D};
		byte[] ctlt =  {0x14} ;
		byte[] ctlw = {0x17};
		String messagemode = "70028E";
		byte[] msgmode = messagemode.getBytes();
		byte[] bytebuffer = inCmd.getBytes();
		try {
			serialPort.writeBytes(ctlw);
			serialPort.writeBytes(msgmode);
			serialPort.writeBytes(cr);
			serialPort.writeBytes(ctlt);
			serialPort.writeBytes(bytebuffer);
			serialPort.writeBytes(cr);
		} catch (SerialPortException ex) {
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			ex.printStackTrace();
		}
		processSendCommand(inCmd);
	}
	public static String getDateandTime()
	{
		long currentDateTime = System.currentTimeMillis();
		Date now = new Date(currentDateTime);
		DateFormat formatter = DateFormat.getDateTimeInstance(); // Date and time
		return ( formatter.format(now));

	}
	public static void processSendCommand(String input)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("PU");
		sb.append(input);
		// get rid or \r at end of sb
		sb.setLength(sb.length() - 1);
		processData(sb);
	}
	static public String getCurrentDateTime() {
		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");
		return sdf.format(date);
	}
	public static String DateDifference(Date startDate, Date endDate){

		//1 minute = 60 seconds
		//1 hour = 60 x 60 = 3600
		//1 day = 3600 x 24 = 86400
		//milliseconds
		String sReturn= "";
		long different = endDate.getTime() - startDate.getTime();
		long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
		long daysInMilli = hoursInMilli * 24;

		long elapsedDays = different / daysInMilli;
		different = different % daysInMilli;

		long elapsedHours = different / hoursInMilli;
		different = different % hoursInMilli;

		long elapsedMinutes = different / minutesInMilli;
		different = different % minutesInMilli;

		long elapsedSeconds = different / secondsInMilli;
		String totalUpTime = String.format("Days: %1$d    Hours: %2$d     Minutes: %3$d      Seconds: %4$d", 
				elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);
		sReturn = "Start: " + startDate +  "\n" +  "Current: " + endDate + "\n\n" + "Total Uptime:" + "\n" + totalUpTime;
		return sReturn;	
	}	

	public static void runDiagnosticsReport(Boolean bFirstTime)
	{
		String checkSerPort = "";
		String checkNetwork = "";

		String ipAndHostname = getIPAddressAndHostname.getIPAddresAndHostname();

		checkSerialPort csp = new checkSerialPort();
		if(csp.run() == false)
		{
			checkSerPort = "Serial Port check FAILED!!!";
			// Failed, try a second time
			checkSerialPort csp1 = new checkSerialPort();
			if(csp1.run() == false)
			{
				checkSerPort = "Serial Port check FAILED!!!";	
			}
			else
			{
				checkSerPort = "Serial Port check SUCCESSFUL!!!";		
			}
		}
		else
		{
			checkSerPort = "Serial Port check SUCCESSFUL!!!";	
		}
		try {
			if(miscellaneous.getNetworkStatus("http://google.com") == true)
			{
				checkNetwork = "Network check SUCCESSFUL";	
			}
			else
			{
				checkNetwork = "Network check FAILED";
			}
		} catch (IOException ex) {
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			ex.printStackTrace();
		}

		SimpleDateFormat simpleDateFormat = 
				new SimpleDateFormat("dd/M/yyyy hh:mm:ss");

		String elapsedTime = "";
		try {
			Date date1 = simpleDateFormat.parse(upbServer.appStartDate);
			Date date2 = simpleDateFormat.parse(upbServer.getCurrentDateTime());
			elapsedTime = upbServer.DateDifference(date1, date2);
		} catch (ParseException ex) {
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			ex.printStackTrace();
		}
		String isFirstTime;
		if(bFirstTime == true)
		{
			isFirstTime = "  *** upbServer has just started ***";
		}
		else
		{
			isFirstTime = "";	
		}
	 rowCount = updateDatabase.getTableRowCount(dbName, "loginfo"); 
		String emailString = upbServer.getDateandTime() + isFirstTime +   "\n\n" + ipAndHostname + "\n\n" + checkSerPort + "\n\n" + checkNetwork + "\n\n" + "Info (Log) table contains: " + rowCount +" record(s)" +   "\n\n"  + elapsedTime ;
		sendMailSSL sm = new sendMailSSL(upbServer.emailUserid, upbServer.emailPassword, upbServer.toEmail, upbServer.fromEmail, emailString); 
		sm.run();                  
	}
}
