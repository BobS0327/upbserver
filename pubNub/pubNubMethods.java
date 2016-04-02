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
package pubNub;

import org.json.JSONException;
import org.json.JSONObject;

import com.bobs0327.buildCmd;
import com.bobs0327.moduleVariables;
import com.bobs0327.upbServer;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import databaseMethods.updateDatabase;



public class pubNubMethods {
	String myChannel = "myChannel";
	Pubnub pubnub = new Pubnub("pub-demo", "sub-demo");

	public void Subscribe() throws PubnubException
	{
		pubnub.subscribe(myChannel, new Callback() {
			 
		      @Override
		      public void connectCallback(String channel, Object message) {
		          System.out.println("SUBSCRIBE : CONNECT on channel:" + channel
		                     + " : " + message.getClass() + " : "
		                     + message.toString());
		      }
		 
		      @Override
		      public void disconnectCallback(String channel, Object message) {
		          System.out.println("SUBSCRIBE : DISCONNECT on channel:" + channel
		                     + " : " + message.getClass() + " : "
		                     + message.toString());
		      }
		 
		      public void reconnectCallback(String channel, Object message) {
		          System.out.println("SUBSCRIBE : RECONNECT on channel:" + channel
		                     + " : " + message.getClass() + " : "
		                     + message.toString());
		      }
		 // This is where we parse the string
		      @Override
		      public void successCallback(String channel, Object message) {
		          System.out.println("SUBSCRIBE : " + channel + " : "
		                     + message.getClass() + " : " + message.toString());
		     String temp =  message.toString();
		          
		          JSONObject jsonObject = null;
				try {
					jsonObject = new JSONObject(temp);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
		          String source = null, dest = null;
		          String jsontimestamp = null, deviceid = null, linkid = null;
		          String devicename = null, linkname = null, kind = null, timestamp = null;
		          String room = null, level = null, info = null;
		          String cmd1 = null;
		          int status = 0;
		          int action = 0, recnum = 0, reccount = 0;
				try {
					try {
					source = (String) jsonObject.get("source");
					}
					catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					 dest = (String) jsonObject.get("destination");
					 action =  (int) jsonObject.get("action");
					 recnum = (int) jsonObject.get("recnum");
					 reccount = (int) jsonObject.get("reccount");
				// Room is null
					 
					 room = (String)jsonObject.get("room");
					 level = (String)jsonObject.get("level");
				jsontimestamp = (String)jsonObject.get("jsontimestamp");
				deviceid  = (String) jsonObject.get("deviceid");
				linkid = (String) jsonObject.get("linkid");
				devicename = (String) jsonObject.get("devicename");
				linkname = (String) jsonObject.get("linkname");
				kind = (String) jsonObject.get("kind");
				status = (int) jsonObject.get("status");
				info = (String) jsonObject.get("info");
				timestamp = (String) jsonObject.get("timestamp");
				cmd1 = (String) jsonObject.get("cmd");
			
				
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
				System.out.println(upbServer.serverIPAddress + "source = " + source);
		//		updateDatabase.pubNubSendTableData( upbServer.dbName, source);
				if (!source.equalsIgnoreCase(upbServer.serverIPAddress))
				{
					switch(action) // retrieve command to be executed by server
					{
					case 0:  //retrieve commnad to be executed by server

						 upbServer.sendCmd(cmd1);
						 
						 try {
							Thread.sleep(4000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						 moduleVariables mvInput = new moduleVariables();
							buildCmd bc = new buildCmd();

							String 	myCmd = mvInput.message.toString();
						 mvInput.clear();
							mvInput.moduleid = Integer.parseInt(deviceid);
							mvInput.sourceid = Integer.parseInt(upbServer.sourceID);
							mvInput.networkid = Integer.parseInt(upbServer.networkID);;
							mvInput.isDevice = true;
							mvInput.action = 0x30;  // report State Command

							bc.buildCmd(mvInput);
							myCmd = mvInput.message.toString();
						//	dateStr = getDateandTime();

						//	System.out.println(dateStr + " Sent: "+ myCmd);
							upbServer.sendCmd(myCmd);
						 
						 
							System.out.println(cmd1 + "in execute command");
					break;
					case 1: // retrieve all devices & device status
						updateDatabase.pubNubSendTableData( upbServer.dbName, source);
						break;
					case 3: // Retrieve all links
						updateDatabase.pubNubSendLinkData(  upbServer.dbName, source);	
						break;
					case 4:  // execute device command
						System.out.println("Got a four (4)");
						break;
				
					
					default:
						break;
					}
					
					
					
				}
		      }
		      @Override
		      public void errorCallback(String channel, PubnubError error) {
		          System.out.println("SUBSCRIBE : ERROR on channel " + channel
		                     + " : " + error.toString());
		      }
		    }
		  );
		
	}
	
	public void Publish(String input) throws PubnubException
	{
		
		Callback callback = new Callback() {
			  public void successCallback(String channel, Object response) {
			    System.out.println(response.toString());
			  }
			  public void errorCallback(String channel, PubnubError error) {
			    System.out.println(error.toString());
			  }
			};
			pubnub.publish(myChannel, input , callback);
		
	}
}

