package com.bobs0327;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import java.net.*;
import java.util.ArrayList;
import java.util.Properties;
import databaseMethods.addUPERecords2DB;
import databaseMethods.updateDatabase;
import java.io.*;

public class upbServer extends Thread
{
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
	static String sourceID;
	static String delayTemp;
	static int cmdDelay;
	static String expfilename, dbName;

	static 	SerialPort serialPort; 	
	static int _portNumber; // = 3306; //Arbitrary port number

	public static void main(String[] args) 
	{
		System.out.println("Initializing application");
		initializeApplication();


		try {
			serialPort = new SerialPort(comm); 
			_portNumber = port; //Arbitrary port number
			serialPort.openPort(); //Open port
			serialPort.setParams(4800, 8, 1, 0); //Set params for UPB PIM
			int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
			serialPort.addEventListener(new SerialPortReader()); //Add SerialPortEventListener
			//	new upbServer().startServer();
		} catch (Exception e) {
			System.out.println("I/O failure: " + e.getMessage());
			e.printStackTrace();
		}

		updateDatabase.loadDeviceIDArray( dbName, deviceIDArray);	

		moduleVariables mvInput = new moduleVariables();
		buildCmd bc = new buildCmd();
		System.out.println("Updating database..");
		for(int index = 0; index <= deviceIDArray.length; index++)
		{
			if(deviceIDArray[index] == 0)
				break;
			mvInput.clear();
			mvInput.moduleid = deviceIDArray[index];
			mvInput.sourceid = 255;
			mvInput.networkid = 1;
			mvInput.isDevice = true;
			mvInput.action = 0x30;  // report State Command

			bc.buildCmd(mvInput);
			String 	myCmd = mvInput.message.toString();
			sendCmd(myCmd);
			try {
				Thread.sleep(cmdDelay*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Application now running");
	}

	public static void initializeApplication()
	{
		try {
			configfile = new File("config.properties").getAbsolutePath();
			File file = new File(configfile);
			FileInputStream fileInput = new FileInputStream(file);
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Aborting, cannot read "+ configfile);
			System.exit(0);
		}
		addUPERecords2DB.addUPERecords(dbName, expfilename);	
	}
	public void startServer() throws Exception {
		ServerSocket serverSocket = null;
		boolean listening = true;

		try {
			serverSocket = new ServerSocket(_portNumber);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + _portNumber);
			System.exit(-1);
		}
		while (listening) {
			handleClientRequest(serverSocket);
		}
		serverSocket.close();
	}

	private void handleClientRequest(ServerSocket serverSocket) {
		try {
			new ConnectionRequestHandler(serverSocket.accept()).run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles client connection requests. 
	 */
	public class ConnectionRequestHandler implements Runnable{
		private Socket _socket = null;
		private PrintWriter _out = null;
		private BufferedReader _in = null;

		public ConnectionRequestHandler(Socket socket) {
			_socket = socket;
		}

		public void run() {
			System.out.println("Client connected to socket: " + _socket.toString());

			try {
				_out = new PrintWriter(_socket.getOutputStream(), true);
				_in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));

				String inputLine, outputLine;
				upbCommandProcessor upbCommandProcessor = new upbCommandProcessor();
				outputLine = upbCommandProcessor.processInput(null);
				_out.println(outputLine);

				//Read from socket and write back the response to client. 
				while ((inputLine = _in.readLine()) != null) {
					outputLine = upbCommandProcessor.processInput(inputLine);
					if(outputLine != null) {
						_out.println(outputLine);
						if (outputLine.equals("exit")) {
							System.out.println("Server is closing socket for client:" + _socket.getLocalSocketAddress());
							break;
						}
					} else {
						System.out.println("OutputLine is null!!!");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally { //In case anything goes wrong we need to close our I/O streams and sockets.
				try {
					_out.close();
					_in.close();
					_socket.close();
				} catch(Exception e) { 
					System.out.println("Couldn't close I/O streams");
				}
			}
		}
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
			} catch(Exception e) {
				System.out.println("input process falied: " + e.getMessage());
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
						sb.append(readed);
					else
						sb.append("");
					System.out.print(readed);
					if(buffer[0] == 0x0D)
					{
						processData(sb);
						sb.setLength(0);
					}
				}
				catch (SerialPortException ex) {
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
		if (theMessage.toString().startsWith("PU")) {
			int messageLen = theMessage.length();
			int messageBody[] = new int[(messageLen / 2) - 2];
			int checkSum = 0;
			int messagePtr = 0;

			char firstChar = 0, secondChar = 0;
			int theValue = 0;
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
				// Compute checksum
				checkSum = (-checkSum & 0xff);
				if (checkSum != theValue) {
					System.err.println(System.lineSeparator() + "Check sum on received UPB packet failed -- should be " + checkSum + " but received as " + theValue);
					System.err.println("   BAD MESSAGE[" + theMessage + "], " + theMessage.length() + " bytes");
					return;
				}
			}
			// Dispatch valid UPP message
			parseUPBMessage(messageBody);
			return;
		}
	}

	private static void parseUPBMessage(int theMessage[]) {
		String newline = System.getProperty("line.separator");
		/** Used to indicate the device should use it's default fade rate */
		final int DEFAULT_FADE_RATE = 255;
		final int UPBMSG_BODY = 6;
		/** Used to indicate invalid/unassigned device state */
		final int UNASSIGNED_DEVICE_STATE = -1;

		int parsedLevel = UNASSIGNED_DEVICE_STATE;
		int parsedFadeRate = DEFAULT_FADE_RATE;
		int level = 0;
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
		int destID = theMessage[3];
		System.out.format("Destination ID is " + "is %d"+ newline ,destID); 
		updateDatabase.findDeviceRecord( dbName, destID);
		int sourceID = theMessage[4];
		System.out.format("Source ID is " + "is %d"+ newline ,sourceID); 
		updateDatabase.findDeviceRecord( dbName, sourceID);
		int messageID = theMessage[5];
		System.out.format("Message ID is " + "is %d"+ newline ,messageID); 

		int messageSet = (messageID >> 5) & 0x07;
		System.out.format("Message Set is " + "is %d"+ newline ,messageSet); 
		if(messageID == 0x86)
		{
			level = theMessage[6];
			System.out.format("Level is  %d"+newline, level  );
		}	
		// Decode the message type
		switch (messageID) {
		case 0x20: // Activate Link Command
			System.out.println("Activate Link Command");
			break;

		case 0x21:  // Deactivate Link Command
			System.out.println("Deactivate Link Command");
			break;

		case 0x22: // Goto Command
			System.out.println("GOTO Command");
			if (theMessage.length > UPBMSG_BODY)
			{
				parsedLevel = theMessage[UPBMSG_BODY];
				System.out.format("Level is " + "is %d"+ newline ,parsedLevel); 
			}
			if (theMessage.length > UPBMSG_BODY + 1)
			{
				parsedFadeRate = theMessage[UPBMSG_BODY + 1];
				System.out.format("Fade Rate is " + "is %d"+ newline ,parsedFadeRate); 
			}
			updateDatabase.updateStatusTable("upbserver.db" , destID ,parsedLevel);
			break;

		case 0x23: // Fade Start Command
			System.out.println("Fade Start Command");
			if (theMessage.length > UPBMSG_BODY)
			{
				parsedLevel = theMessage[UPBMSG_BODY];
				System.out.format("Level is " + "is %d"+ newline ,parsedLevel); 
			}
			if (theMessage.length > UPBMSG_BODY + 1)
			{
				parsedFadeRate = theMessage[UPBMSG_BODY + 1];
				System.out.format("Fade Rate is " + "is %d"+ newline ,parsedFadeRate); 
			}
			updateDatabase.updateStatusTable("upbserver.db" , destID ,parsedLevel);
			break;

		case 0x24:  // Fade Stop Command
			System.out.println("Fade Stop Command");
			break;

		case 0x86:  // Device state report, updated device status
			updateDatabase.updateStatusTable("upbserver.db" , sourceID ,level);	
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
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}
