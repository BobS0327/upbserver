package com.bobs0327;

public class moduleVariables {
	static boolean isDevice;
	static int moduleid;
	static int sourceid;
	static int networkid;
	static int action;
	static int level;
	static int fadeRate;
	// if channel = 0 , all channels, if 1 then channel 1 if 2 then channel 2 etc
	static int channel;
	static StringBuffer message = new StringBuffer(128);
	public static void clear()
	{
		isDevice = false;
		moduleid = -1;
		sourceid = -1;
		networkid = -1;
		action = -1;
		level =-1; 
		fadeRate = -1;
		channel = -1;
		message.delete(0, message.length());
	}
}
