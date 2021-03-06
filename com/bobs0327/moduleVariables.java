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

public class moduleVariables {
	public static boolean isDevice;
	public static int moduleid;
	public static int sourceid;
	public static int networkid;
	public static int action;
	public static int level;
	public static int fadeRate;
	public static int blinkRate;
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
		blinkRate = 0;
		channel = 0;
		message.delete(0, message.length());
	}
}
