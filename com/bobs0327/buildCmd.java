package com.bobs0327;


import gnu.io.CommPortIdentifier;
import java.util.Enumeration;

public class buildCmd {

	static StringBuffer messageBuffer = new StringBuffer(128);

	final 	static  int  ACTIVATE = 0;   /** Got or send a link activate */
	final 	static  int  DEACTIVATE = 1;  /** Got or send a link deactivate */
	final 	static  int  GOTO = 2; /** Got or send a GOTO (link or device) */
	final 	static  int  START_FADE = 3; /** Got or send a START_FADE (link or device) */
	final 	static  int  STOP_FADE = 4;  /** Got or send a STOP_FADE (link or device) */
	final 	static  int  QUERY_STATE = 5; /** Send a query state request (device) */
	final 	static  int	 STATE_REPORT = 6; /** Got a state report (device) */
	public static final int INVALID_CHANNEL = -1;
	public static final int UPB_EOL = 0x0d;

	/** Used to indicate all channels for a device */
	public static final int ALL_CHANNELS = 0;

	/** Used to indicate the device should use it's default fade rate */
	public static final int DEFAULT_FADE_RATE = 255;

	/** Used to indicate invalid/unassigned device state */
	public static final int UNASSIGNED_DEVICE_STATE = -1;
	public static final int LAST_DIM_LEVEL = 255;
	public static final int DEFAULT_DIM_LEVEL = 256;

	static CommPortIdentifier portId;
	static final int UPBMSG_CONTROL_HIGH = 0;
	static final int UPBMSG_CONTROL_LOW = 1;
	static final int UPBMSG_NETWORK_ID = 2;
	static final int UPBMSG_DEST_ID = 3;
	static final int UPBMSG_SOURCE_ID = 4;
	static final int UPBMSG_MESSAGE_ID = 5;
	static final int UPBMSG_BODY = 6;

	static final char hexTable[] = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	@SuppressWarnings("rawtypes")
	static Enumeration portList;

	public static void buildCmd( moduleVariables mv) {
		int message1[] = null;
		message1 =	encodeUPBMessage(  mv ); 
		mv.message = convertBytesToText(message1, mv.message);
		mv.message.append((char) UPB_EOL);


	}

	private static int[] encodeUPBMessage(  moduleVariables mv) {
		// Allocate the message and fill in the header
		int theMessageSize = 6;  // Basic message is at least 6 bytes long
		int theMessage[] = null;
		if(mv.action == 0x30)
		{
			theMessageSize = 6;
		}
		else
		if(mv.action == 0x23)
		{
		theMessageSize +=2;
		}
		else if (mv.action == 0x07 )  // GetStatus
		{
			theMessageSize = 6;
		}
		else if(mv.action == 0x22)	// Goto
		{
			theMessageSize  +=2;	
		}
		else
		{
			theMessageSize = 6;
		}
		
		
		theMessage = new int[theMessageSize];
		

		switch(mv.action) {
		case 0x30:	// Report State Command	
		case 0x07:   // Get Status
			  theMessage[UPBMSG_CONTROL_HIGH] = 0x00 | (theMessageSize + 1);
		      theMessage[UPBMSG_CONTROL_LOW] =  0x14; //0x14;
		  	theMessage[UPBMSG_MESSAGE_ID] = mv.action;
		      theMessage[UPBMSG_NETWORK_ID] = mv.networkid;
		      theMessage[UPBMSG_DEST_ID] = mv.moduleid;
		      theMessage[UPBMSG_SOURCE_ID] = mv.sourceid;
			break;
		case 0x20:    // Activate a link
			
		      theMessage[UPBMSG_CONTROL_HIGH] = 0x80 | (theMessageSize + 1);
		      theMessage[UPBMSG_CONTROL_LOW] =  0x14; //0x14;
		  	theMessage[UPBMSG_MESSAGE_ID] = mv.action;
		      theMessage[UPBMSG_NETWORK_ID] = mv.networkid;
		      theMessage[UPBMSG_DEST_ID] = mv.moduleid;
		      theMessage[UPBMSG_SOURCE_ID] = mv.sourceid;
			break;
		
		case 0x22: //Goto
		case 0x23:  //Turn Dimming wall switch ON/OFF
			theMessage[UPBMSG_CONTROL_HIGH] = theMessageSize + 1;
			theMessage[UPBMSG_CONTROL_LOW] =  0x14;   
			theMessage[UPBMSG_NETWORK_ID] = mv.networkid;
			theMessage[UPBMSG_DEST_ID] = mv.moduleid;
			theMessage[UPBMSG_SOURCE_ID] = mv.sourceid;;
			
			theMessage[UPBMSG_MESSAGE_ID] = mv.action;
			theMessage[UPBMSG_BODY] = mv.level;
			if(mv.fadeRate != DEFAULT_FADE_RATE)
				theMessage[UPBMSG_BODY + 1] =  mv.fadeRate; 
			if(mv.channel != ALL_CHANNELS)
				theMessage[UPBMSG_BODY + 2] = mv.channel;  
			break;
		default:
			break;
		}
		return theMessage;
	}
	public static StringBuffer convertBytesToText(int message[], StringBuffer messageBuffer) {
		int checkSum = 0;
		if (messageBuffer == null) messageBuffer = new StringBuffer();

		// Format each byte of the message
		for (int bytePtr = 0; bytePtr < message.length; bytePtr++) {
			appendHex(message[bytePtr], messageBuffer);
			checkSum += message[bytePtr];
		}
		appendHex((-checkSum & 0xff), messageBuffer);
		return messageBuffer;
	}

	private static void appendHex(int theValue, StringBuffer theBuffer) {
		theBuffer.append(hexTable[(theValue >> 4) & 0x0f]);
		theBuffer.append(hexTable[theValue & 0x0f]);
	}
}

