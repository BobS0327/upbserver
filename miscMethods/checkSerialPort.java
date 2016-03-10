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
package miscMethods;


import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.bobs0327.buildCmd;
import com.bobs0327.moduleVariables;
import com.bobs0327.upbServer;

import databaseMethods.updateDatabase;

public class checkSerialPort {
	
	public Boolean run()
	{
		JSONObject datesObject1 = null;
		JSONObject datesObject2 = null;
		moduleVariables mv = new moduleVariables();
	String[] dates = new String[3];
	
		dates[0] =  updateDatabase.findDeviceRecord( upbServer.dbName , upbServer.deviceIDArray[0]);
	try
	{
		  datesObject1 = (JSONObject) JSONValue.parseWithException(dates[0]);
		 System.out.println(datesObject1.get("TimeStamp"));
	}catch(Exception e)
	{
		e.printStackTrace();
	}
		mv.clear();
		mv.moduleid = upbServer.deviceIDArray[0];
		mv.sourceid = Integer.parseInt(upbServer.sourceID);
		mv.networkid = Integer.parseInt(upbServer.networkID);;
		mv.isDevice = true;
		mv.action = 0x30;  // report State Command
		buildCmd bc = new buildCmd();
		bc.buildCmd(mv);
		String myCmd = mv.message.toString();
		String dateStr = upbServer.getDateandTime();

		System.out.println(dateStr + " Sent: "+ myCmd);
		upbServer.sendCmd(myCmd);
		try {
			Thread.sleep(upbServer.cmdDelay*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		dates[1] =  updateDatabase.findDeviceRecord( upbServer.dbName , upbServer.deviceIDArray[0]);
		
		try
		{
			  datesObject2 = (JSONObject) JSONValue.parseWithException(dates[1]);
			 System.out.println(datesObject2.get("TimeStamp"));
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		if(datesObject2.equals(datesObject1))
		{
		return false;
		}
		else
		{
		return true;	
		}
	}
	
}

