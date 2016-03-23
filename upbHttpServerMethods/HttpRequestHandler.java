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
package upbHttpServerMethods;


import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import com.bobs0327.buildCmd;
import com.bobs0327.moduleVariables;
import com.bobs0327.upbServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import databaseMethods.updateDatabase;

import org.json.simple.JSONObject;

public class HttpRequestHandler implements HttpHandler {

	private static final String ACTION = "action";
	private static final String MODULEID = "moduleid";
	private static final String LINKID = "linkid";
	private static final String LEVEL = "level";
	private static final String FADERATE = "faderate";
	private static final String BLINKRATE = "blinkrate";
	
	private static final String CHANNEL = "channel";
	private static final int PARAM_NAME_IDX = 0;
	private static final int PARAM_VALUE_IDX = 1;
	private static final int HTTP_OK_STATUS = 200;
	private static final String AND_DELIMITER = "&";
	private static final String EQUAL_DELIMITER = "=";

	public void handle(HttpExchange t) throws IOException {

		//Create a response form the request query parameters
		URI uri = t.getRequestURI();
		String response = createResponseFromQueryParams(uri);
		System.out.println("Response: " + response);
		//Set the response header status and length
		t.sendResponseHeaders(HTTP_OK_STATUS, response.getBytes().length);
		//Write the response string
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	private String createResponseFromQueryParams(URI uri) {
		String responseBody = null;
		int tempModuleID = 0;
		JSONObject updateObj = new JSONObject();
		moduleVariables mvInput = new moduleVariables();
		buildCmd bc1 = new buildCmd();
		String Action = "";
		String ModuleID = "";
		String LinkID = "";
		String Level= "";
		String FadeRate = "";
		String Channel = "";
		String BlinkRate = "";
		//Get the request query
		String query = uri.getQuery();
		if (query != null) {
			System.out.println("Query: " + query);
			String[] queryParams = query.split(AND_DELIMITER);
			if (queryParams.length > 0) {
				for (String qParam : queryParams) {
					String[] param = qParam.split(EQUAL_DELIMITER);
					//		System.out.println(param[PARAM_NAME_IDX]);
					if (param.length > 0) {
						for (int i = 0; i < param.length; i++) {
							if (ACTION.equalsIgnoreCase(param[PARAM_NAME_IDX])) {
								Action = param[PARAM_VALUE_IDX];
							}
							if (MODULEID.equalsIgnoreCase(param[PARAM_NAME_IDX])) {
								ModuleID = param[PARAM_VALUE_IDX];
							}
							if (LINKID.equalsIgnoreCase(param[PARAM_NAME_IDX])) {
								LinkID = param[PARAM_VALUE_IDX];
							}
							if (LEVEL.equalsIgnoreCase(param[PARAM_NAME_IDX])) {
								Level = param[PARAM_VALUE_IDX];
							}
							if (FADERATE.equalsIgnoreCase(param[PARAM_NAME_IDX])) {
								FadeRate = param[PARAM_VALUE_IDX];
							}
							if (CHANNEL.equalsIgnoreCase(param[PARAM_NAME_IDX])) {
								Channel = param[PARAM_VALUE_IDX];
							}
							if (BLINKRATE.equalsIgnoreCase(param[PARAM_NAME_IDX])) {
								BlinkRate = param[PARAM_VALUE_IDX];
							}
						}
					}
				}
			}
		}

		//		 upbServer.webString.add("First");
		if (Action.equalsIgnoreCase("BLINKON"))
		{
			mvInput.clear();
			mvInput.action = 0x25;
			if(BlinkRate != "")
			{
				mvInput.blinkRate = Integer.parseInt(BlinkRate);
			}
			else
			{
				mvInput.blinkRate = buildCmd.DEFAULT_BLINK_RATE;  // 255
			}
			
			if(LinkID != "")
			{
				mvInput.isDevice = false;
				mvInput.moduleid = Integer.parseInt(LinkID);
				// mvInput.fadeRate = Integer.parseInt(FadeRate);
			}
			if(ModuleID != "")
			{
				mvInput.isDevice = true;
				mvInput.moduleid = Integer.parseInt(ModuleID);
				if(Channel != "")
				{
					mvInput.channel = Integer.parseInt(Channel);
				}
				else 
				{
					mvInput.channel = 0;
				}
			}
			int inSourceID = Integer.parseInt(upbServer.sourceID);
			mvInput.sourceid = inSourceID;
			int inNetworkID = Integer.parseInt(upbServer.networkID);
			mvInput.networkid = inNetworkID;
			bc1.buildCmd(mvInput);
			String 	myCmd = mvInput.message.toString();
			String dateStr = upbServer.getDateandTime();
			System.out.println(dateStr + " Sent: "+ myCmd);
			upbServer.sendCmd(myCmd);

			try {
				Thread.sleep(upbServer.webDelay*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			JSONObject activatelinkobj = new JSONObject();
			activatelinkobj.put("action", "BLINKON");
			activatelinkobj.put("result", "BLINKON Command submitted successfully");

			responseBody =	activatelinkobj.toString();
		}
		else if (Action.equalsIgnoreCase("ACTIVATELINK"))
		{
			mvInput.clear();	
			mvInput.moduleid = Integer.parseInt(LinkID);
			mvInput.isDevice = false;
			int inSourceID = Integer.parseInt(upbServer.sourceID);
			mvInput.sourceid = inSourceID;

			int inNetworkID = Integer.parseInt(upbServer.networkID);
			mvInput.networkid = inNetworkID;
			mvInput.action = 0x20;  // Activate Link
			System.out.println("In ACTIVATELINK");
			bc1.buildCmd(mvInput);
			String 	myCmd = mvInput.message.toString();
			String dateStr = upbServer.getDateandTime();
			upbServer.sendCmd(myCmd);
			try {
				Thread.sleep(upbServer.webDelay*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			JSONObject activatelinkobj = new JSONObject();
			activatelinkobj.put("action", "ACTIVATELINK");
			activatelinkobj.put("result", "Command submitted successfully for link # " + LinkID);

			responseBody =	activatelinkobj.toString();   // updateDatabase.findDeviceRecord( upbServer.dbName, tempModuleID );	

			updateDatabase.updateDevicesFromLinkCmd( upbServer.dbName,  Integer.parseInt(LinkID), true);
		}
		else if (Action.equalsIgnoreCase("DEACTIVATELINK"))
		{
			mvInput.clear();	
			mvInput.moduleid = Integer.parseInt(LinkID);
			mvInput.isDevice = false;
			int inSourceID = Integer.parseInt(upbServer.sourceID);
			mvInput.sourceid = inSourceID;

			int inNetworkID = Integer.parseInt(upbServer.networkID);
			mvInput.networkid = inNetworkID;
			mvInput.action = 0x21;  // DeActivate Link
			System.out.println("In DEACTIVATELINK");
			bc1.buildCmd(mvInput);
			String 	myCmd = mvInput.message.toString();
			String dateStr = upbServer.getDateandTime();
			upbServer.sendCmd(myCmd);
			try {
				Thread.sleep(upbServer.webDelay*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			JSONObject activatelinkobj = new JSONObject();
			activatelinkobj.put("action", "DEACTIVATELINK");
			activatelinkobj.put("result", "Command submitted successfully for link # " + LinkID);

			responseBody =	activatelinkobj.toString();   // updateDatabase.findDeviceRecord( upbServer.dbName, tempModuleID );	
		}
		else if (Action.equalsIgnoreCase("GOTO")  )
		{
		if(ModuleID == null && ModuleID.isEmpty())
			{
			JSONObject activatelinkobj = new JSONObject();
			activatelinkobj.put("action", "GOTO");
			activatelinkobj.put("result", "moduleid not entered (or misspelled) for GOTO command");
			responseBody =	activatelinkobj.toString();	
			return responseBody;
			}
			if(Level.equals(""))
			{
			JSONObject activatelinkobj = new JSONObject();
			activatelinkobj.put("action", "GOTO");
			activatelinkobj.put("result", "level not entered (or misspelled) for GOTO command");
			responseBody =	activatelinkobj.toString();	
			return responseBody;
			}
			mvInput.clear();
			mvInput.action = 0x22;  // GOTO
			if(LinkID != null && !LinkID.isEmpty())
			{    // Example
				// localhost:8080/upb?action=goto&linkid=3&level=100
				mvInput.moduleid = Integer.parseInt(LinkID);
				mvInput.level = Integer.parseInt(Level);
				if(FadeRate != "")
				{
					mvInput.fadeRate = Integer.parseInt(FadeRate);
				}
				else
				{
					mvInput.fadeRate = buildCmd.DEFAULT_FADE_RATE;  // 255
				}
				mvInput.isDevice = false;
			}
			else   if(ModuleID != null && !ModuleID.isEmpty()) 
			{
				mvInput.isDevice = true;
				mvInput.moduleid = Integer.parseInt(ModuleID);
				mvInput.level = Integer.parseInt(Level);
				if(FadeRate != "")
				{
					mvInput.fadeRate = Integer.parseInt(FadeRate);
				}
				else
				{
					mvInput.fadeRate = buildCmd.DEFAULT_FADE_RATE;  // 255
				}
				if(Channel != "")
				{
					mvInput.channel = Integer.parseInt(Channel);
				}
				else 
				{
					mvInput.channel = 0;
				}
			}
			else // Both ModuleID and  LinkID are EMPTY on GOTO action
			{
				JSONObject activatelinkobj = new JSONObject();
				activatelinkobj.put("action", "GOTO");
				activatelinkobj.put("result", "moduleid or linkid not entered (or misspelled) for GOTO command");
				responseBody =	activatelinkobj.toString();	
				return responseBody;
			}
			int inSourceID = Integer.parseInt(upbServer.sourceID);
			mvInput.sourceid = inSourceID;
			int inNetworkID = Integer.parseInt(upbServer.networkID);
			mvInput.networkid = inNetworkID;
			bc1.buildCmd(mvInput);
			String 	myCmd = mvInput.message.toString();
			String dateStr = upbServer.getDateandTime();
			System.out.println(dateStr + " Sent: "+ myCmd);
			upbServer.sendCmd(myCmd);

			try {
				Thread.sleep(upbServer.webDelay*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(mvInput.isDevice == true)
			{
				responseBody =	updateDatabase.findDeviceRecord( upbServer.dbName, mvInput.moduleid  );
			}
			else
			{
				JSONObject activatelinkobj = new JSONObject();
				activatelinkobj.put("action", "GOTO-BLINKOFF");
				activatelinkobj.put("result", "Command submitted successfully for link # " + LinkID);
				responseBody =	activatelinkobj.toString();	
			}
		}
		else
			if (Action.equalsIgnoreCase("STATUS"))
			{
				tempModuleID = Integer.parseInt(ModuleID);
			// If OP2 connected, execute STATUS REQUEST for device
				responseBody =	updateDatabase.findDeviceRecord( upbServer.dbName, tempModuleID );
			}
			else
			{

			}
		return responseBody;
	}
}
