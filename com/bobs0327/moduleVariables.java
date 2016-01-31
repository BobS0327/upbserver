package com.bobs0327;

public class moduleVariables {
	public static boolean isDevice;
	public static int moduleid;
	public static int sourceid;
	public static int networkid;
	public static int action;
	public static int level;
	public static int fadeRate;
	// if channel = 0 , all channels, if 1 then channel 1 if 2 then channel 2 etc
	static public int channel;
	public static StringBuffer message = new StringBuffer(128);
	public static void clear()
	{
		isDevice = false;
		moduleid = -1;
		sourceid = -1;
		networkid = -1;
		action = -1;
		level = 0; 
		fadeRate = 0;
		channel = 0;
		message.delete(0, message.length());
	}
}
