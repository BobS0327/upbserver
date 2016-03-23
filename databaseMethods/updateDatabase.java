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
package databaseMethods;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import org.json.simple.JSONObject;

import com.bobs0327.buildCmd;
import com.bobs0327.upbServer;

import miscMethods.miscellaneous;


import java.sql.PreparedStatement;

public class updateDatabase
{
	
	public static void pubNubSendLinkData( String dbName, String destIPAddress)
	{
		Connection c = null;
		Statement stmt = null;
		int rnum = 1;
		
	
		try {
			Class.forName("org.sqlite.JDBC");
			String conn = "jdbc:sqlite:" + dbName;
			c = DriverManager.getConnection(conn);
			c.setAutoCommit(false);
			stmt = c.createStatement();
			String sel = "SELECT * FROM links;";
			String temp = null;
			 ResultSet rsTemp = stmt.executeQuery("SELECT * FROM links");

			    rsTemp = stmt.executeQuery("SELECT COUNT(*) FROM links");
			    // get the number of rows from the result set
			    rsTemp.next();
			    int rowCount = rsTemp.getInt(1);
			
			
			ResultSet rs = stmt.executeQuery(sel);
			while (rs.next()) 
			{
			String linkname = rs.getString("linkname");	
			String linkid = rs.getString("linkidnum");	
							JSONObject obj = new JSONObject();
							
							obj.put("source", upbServer.serverIPAddress);
							obj.put("destination", destIPAddress);
							obj.put("room", "");
							obj.put("level", "");
							obj.put("source", upbServer.serverIPAddress);
							obj.put("destination", destIPAddress);
							// retrieve all links = 3
							obj.put("action", new Integer(3));
							obj.put("recnum", new Integer(rnum++));
							obj.put("reccount", new Integer(rowCount));
							obj.put("jsontimestamp",  upbServer.getCurrentDateTime());
							obj.put("deviceid", "");
							obj.put("linkid", linkid);
							obj.put("devicename", "");
							obj.put("linkname", linkname);
							obj.put("kind", "");
							obj.put("status", new Integer(0));
							obj.put("desc", "");
							obj.put("info", "");
							obj.put("timestamp", "");
							obj.put("cmd", "");
				upbServer.pnMethods.Publish(obj.toString());
			}
		
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}
		
	
	
	
	public static void pubNubSendTableData( String dbName, String destIPAddress)
	{
		int index = 0;
		int st = 0;
		int lev = 0;
		Connection c = null;
		Statement stmt = null;
		Statement stmt1 = null;
		Statement stmt2 = null;
	
		try {
			Class.forName("org.sqlite.JDBC");
			String conn = "jdbc:sqlite:" + dbName;
			c = DriverManager.getConnection(conn);
			c.setAutoCommit(false);
			stmt = c.createStatement();
			stmt1 = c.createStatement();
			stmt2 = c.createStatement();
			String sel = "SELECT * FROM devices;";
			String sel1 = null;
			String status = null;
			String level = null;
			String info = null;
			String timeStamp = null;
			String temp = null;
			int manf = 0, prod = 0;
			 ResultSet rsTemp = stmt.executeQuery("SELECT * FROM devices");

			    rsTemp = stmt.executeQuery("SELECT COUNT(*) FROM devices");
			    // get the number of rows from the result set
			    rsTemp.next();
			    int rowCount = rsTemp.getInt(1);
			
			
			ResultSet rs = stmt.executeQuery(sel);
			int rnum = 1;
			String desc = null;
			while (rs.next()) 
			{
				int mod = rs.getInt("moduleid");
				String id = Integer.toString(mod);
				String name = rs.getString("devname");	
				String room = rs.getString("roomname");	
				manf = rs.getInt("manufacturer");
				prod = rs.getInt("prodid");
				sel1 = "SELECT * FROM products WHERE manufacturer = " + manf + " AND prodid = " + prod;	
				ResultSet	rs1 = stmt1.executeQuery(sel1);
				while (rs1.next()) 
				{
					desc = rs1.getString("proddesc");	
				}
				rs1.close();
				String 	sel2 = "SELECT * FROM devicestatus WHERE moduleid = " + mod; 	
				ResultSet	rs2 = stmt2.executeQuery(sel2);
				while (rs2.next()) 
				{
					timeStamp = rs2.getString("upddatetime");
					info = rs2.getString("info");
					lev = rs2.getInt("level");
					if(desc.contains("Non-Dimming"))
					{
					//	level = "N/A";
					 level = Integer.toString(lev);
					}
					else
					{
						level = 	Integer.toString(lev);
					}
					st = rs2.getInt("status");
					if(st == 0)
					{
						status = "Off";
					}
					else
					{
						status = "On";
					}
				}
				rs2.close();
				if ("".equals(info))
				{ 
					if(st == 0)
					{
						temp = timeStamp +" Device turned off";
					}		 
					else
					{
						temp = timeStamp +" Device turned on";	
					}
					//	temp = "No Activity";	
				}
				else
				{
					temp = timeStamp +" " + info; 

				}
				JSONObject obj = new JSONObject();
				obj.put("room", room);
				obj.put("level", level);
				obj.put("source", upbServer.serverIPAddress);
				obj.put("destination", destIPAddress);
				// retrieve all devices = 1
				obj.put("action", new Integer(1));
				obj.put("recnum", new Integer(rnum++));
				obj.put("reccount", new Integer(rowCount));
				obj.put("jsontimestamp",  upbServer.getCurrentDateTime());
				obj.put("deviceid", id);
				obj.put("linkid", "");
				obj.put("devicename", name);
				obj.put("linkname", "");
				obj.put("kind", "");
				obj.put("status", new Integer(st));
				obj.put("desc", desc);
				obj.put("info", info);
				obj.put("timestamp", temp);
				obj.put("cmd", "");
				upbServer.pnMethods.Publish(obj.toString());
				
			//	model.addRow(new Object[]{id, name, room, desc, status, level, temp });
			
			}

			rs.close();
			stmt.close();
			stmt1.close();
			stmt2.close();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}
	
	
	
	
	public static int getTableRowCount(String dbName, String tableName) 
	{
		int rowCount = 0;
		int index = 0;
		Connection c = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String conn = "jdbc:sqlite:" + dbName;
			c = DriverManager.getConnection(conn);
			c.setAutoCommit(false);
			stmt = c.createStatement();

			String sel = "SELECT * FROM " + tableName;
			String sel1;
			int manf, prod;
			 rs = stmt.executeQuery(sel);

			while (rs.next()) {
				++rowCount;
				
			}
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception ex ) {
			try
			{
			rs.close();
			stmt.close();
			c.close();
			} catch ( Exception ex1 ) {
				
			}
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
		}

	return rowCount;
	
	}

	public static void insertLogRecord(String dbName, String timeStamp,String inData )
	{
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String conn = "jdbc:sqlite:" + dbName;
			c = DriverManager.getConnection(conn);
			c.setAutoCommit(false);
			stmt = c.createStatement();
			String values =	  "VALUES (" + "'" +  timeStamp + "'" +  "," + "'" + inData +"'" +  ");";
			//String values =	  "VALUES (" + timeStamp + "," +  information + ");";
			String sql = "INSERT INTO loginfo (timestamp,info) " + values;
			stmt.executeUpdate(sql);
			stmt.close();
			c.commit();
			c.close();
		} catch ( Exception ex ) {
			System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
		//	updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
		}
		System.out.println("Log record inserted successfully");
	}



	public static void insertDeviceRecord( String dbName, int moduleid, int manufacturer, int prodid, int fw, int channels, int numtransmit, int numrece, String room, String devname, int packet )
	{
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String conn = "jdbc:sqlite:" + dbName;
			c = DriverManager.getConnection(conn);
			c.setAutoCommit(false);
			stmt = c.createStatement();
			String values =	  "VALUES (" + moduleid + "," + manufacturer + "," + prodid + "," + fw + "," + channels + "," + numtransmit + "," + numrece + "," + "'" + room + "', '" + devname + "', " + packet + ");";
			String sql = "INSERT INTO devices (moduleid, manufacturer, prodid, fw, channels,  numtransmitcomp, numrececomp, roomname, devname,packettype) " + values;
			stmt.executeUpdate(sql);
			stmt.close();
			c.commit();
			c.close();
		} catch ( Exception ex ) {
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );

		}
		System.out.println("Device record inserted successfully");
	}

	public static void insertHeaderRecord( String dbName, int importVersion, int expectedDeviceCount, int expectedLinkCount, int importNetworkID, int networkPassword )
	{
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String conn = "jdbc:sqlite:" + dbName;
			c = DriverManager.getConnection(conn);
			c.setAutoCommit(false);
			stmt = c.createStatement();
			String values =	  "VALUES (" + importVersion + "," + expectedDeviceCount + "," + expectedLinkCount + "," + importNetworkID + "," + networkPassword + ");";
			String sql = "INSERT INTO header (fileversion, numupbdevices, numdefinedlinks, networkid, networkpassword) " + values;
			stmt.executeUpdate(sql);
			stmt.close();
			c.commit();
			c.close();
		} catch ( Exception ex ) {
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
		}
		System.out.println("Header record inserted successfully");
	}

	public static void insertPresetRecord( String dbName, int moduleID, int linkID, int channel, int presetDim, int presetFadeRate )
	{
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String conn = "jdbc:sqlite:" + dbName;
			c = DriverManager.getConnection(conn);
			c.setAutoCommit(false);
			stmt = c.createStatement();
			String values =	  "VALUES (" + moduleID + "," + linkID + "," + channel + "," + presetDim + "," + presetFadeRate + ");";
			String sql = "INSERT INTO presets (moduleid, linkid, channel, presetdim, presetfade) " + values;
			stmt.executeUpdate(sql);
			stmt.close();
			c.commit();
			c.close();
		} catch ( Exception ex ) {
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
		}
		System.out.println("Preset record inserted successfully");
	}


	public static void insertProductsRecord( String dbName, int manuf, int prod, String desc, String kind )
	{
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String conn = "jdbc:sqlite:" + dbName;
			c = DriverManager.getConnection(conn);
			c.setAutoCommit(false);
			stmt = c.createStatement();
			String values =	  "VALUES (" + manuf + "," + prod + "," + "'" + desc + "'" + "," + "'"  +kind + "'" + ");";
			String sql = "INSERT INTO products (manufacturer, prodid, proddesc, kind) " + values;
			stmt.executeUpdate(sql);
			stmt.close();
			c.commit();
			c.close();
		} catch ( Exception ex ) {
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
		}
		System.out.println("Products record inserted successfully");
	}




	public static void insertLinkRecord( String dbName, int linkid, String linkname)
	{
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String conn = "jdbc:sqlite:" + dbName;
			c = DriverManager.getConnection(conn);
			c.setAutoCommit(false);
			stmt = c.createStatement();
			String values =	  "VALUES (" + linkid + ", " + "'" + linkname +"'" + ");";
			String sql = "INSERT INTO links (linkidnum, linkname) " + values;
			stmt.executeUpdate(sql);
			stmt.close();
			c.commit();
			c.close();
		} catch ( Exception ex ) {
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
		}
		System.out.println("Link Record inserted successfully");
	}

	public static void clearTables(String dbName, String tableName)  {
		Connection c = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String conn = "jdbc:sqlite:" + dbName;
			c = DriverManager.getConnection(conn);
			Statement stmt = c.createStatement();
			String sql = "DELETE FROM " + tableName+ ";";
			stmt.executeUpdate(sql);
		}
		catch ( Exception ex ) {
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
		}
	} 
	public static String findDeviceRecord( String dbName, int  moduleID)
	{
		JSONObject updateObj = new JSONObject();
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String conn = "jdbc:sqlite:" + dbName;
			c = DriverManager.getConnection(conn);
			c.setAutoCommit(false);
			stmt = c.createStatement();
			String sel = "SELECT * FROM devices WHERE moduleid = " + moduleID + ";";
			String sel1;
			String sel2 = "SELECT * FROM devicestatus WHERE moduleid = " + moduleID + ";";
			int manf, prod;
			ResultSet rs = stmt.executeQuery(sel);
			boolean devrecfound = false;
			boolean prodrecfound = false;

			while (rs.next()) {
				devrecfound = true;
				updateObj.put("ModuleID", new Integer(moduleID));
				updateObj.put("Room",rs.getString("roomname") );
				updateObj.put("Device",rs.getString("devname"));
			//	updateObj.put("Kind",rs.getString("kind"));

			//	System.out.println("Room Name = " + rs.getString("roomname"));
		//		System.out.println("Device Name = " + rs.getString("devname"));
				manf = rs.getInt("manufacturer");
				prod = rs.getInt("prodid");
		//		System.out.println("Manufacturer # = " + manf);
		//		System.out.println("Prod ID # = " + prod);
				sel1 = "SELECT * FROM products WHERE manufacturer = " + manf + " AND prodid = " + prod;
				ResultSet rs1 = stmt.executeQuery(sel1);

				while (rs1.next()) {
					prodrecfound = true;
					updateObj.put("Description",rs1.getString("proddesc"));
					updateObj.put("Kind",rs1.getString("kind"));

			//		System.out.println("Product Desc = " + rs1.getString("proddesc"));
			//		System.out.println("Kind = " + rs1.getString("kind"));
				}
				rs1.close(); 
			}
			rs.close();
			stmt.close();

			ResultSet rs2 = stmt.executeQuery(sel2);
			while (rs2.next()) {
				prodrecfound = true;
				updateObj.put("TimeStamp",rs2.getString("upddatetime"));
				updateObj.put("Status", new Integer( rs2.getInt(3)));
				updateObj.put("Level", new Integer( rs2.getInt(4)));

				System.out.println("TimeStamp = " + rs2.getString("upddatetime"));
			}
			rs2.close(); 
			c.close();
		} catch ( Exception ex ) {
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
		}
		final String responseBody = updateObj.toJSONString();
		return responseBody;
	}

	public static void updateStatusTable( String dbName, int  moduleID,  int level, int fadeRate, String info )
	{
		Connection c = null;
		Statement stmt = null;
		int status;
		String timeStamp = upbServer.getDateandTime();
		try {
			Class.forName("org.sqlite.JDBC");
			String conn = "jdbc:sqlite:" + dbName;
			c = DriverManager.getConnection(conn);
			c.setAutoCommit(true);
			stmt = c.createStatement();

			String sel = "SELECT * FROM devicestatus WHERE moduleid = " + moduleID + ";";
			ResultSet rs = stmt.executeQuery(sel);
			boolean devrecfound = false;

			while (rs.next()) {
				devrecfound = true;
			}
			if(devrecfound == false)
			{	
				String insertTableSQL = "INSERT INTO devicestatus"
						+ "(moduleid, upddatetime, status, level, faderate, info) VALUES"
						+ "(?,?,?,?,?, ?)";

				PreparedStatement preparedStatement = c.prepareStatement(insertTableSQL);
				preparedStatement.setInt(1, moduleID);
				preparedStatement.setString(2, timeStamp);
				if(level > 0)
					status = 1;
				else
					status = 0;
				preparedStatement.setInt(3, status);
				preparedStatement.setInt(4, level);
				preparedStatement.setInt(5, fadeRate);
				preparedStatement.setString(6, info);
				// execute insert SQL stetement
				preparedStatement.executeUpdate();
			}
			else
			{
				String sql = "update devicestatus set  moduleid=?, upddatetime=? , status=? , level=?, faderate=?, info=? where moduleid=?";

				PreparedStatement preparedStatement =
						c.prepareStatement(sql);
				preparedStatement.setInt  (1, moduleID);  //moduleid
				preparedStatement.setString(2, timeStamp);
				if(level > 0)
					status = 1;
				else
					status = 0;
				preparedStatement.setInt(3, status);
				preparedStatement.setInt  (4, level);  //level
				preparedStatement.setInt  (5, fadeRate);  //faderate
				preparedStatement.setString  (6, info);
				preparedStatement.setInt  (7, moduleID); 

				int rowsAffected = preparedStatement.executeUpdate();
				devrecfound = false;
			}
			rs.close();
			stmt.close();
			rs.close();
			c.close();
		} catch ( Exception ex ) {
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
		}
	}
	public static void loadDeviceIDArray( String dbName, int[]  dArray)
	{
		int index = 0;
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String conn = "jdbc:sqlite:" + dbName;
			c = DriverManager.getConnection(conn);
			c.setAutoCommit(false);
			stmt = c.createStatement();

			String sel = "SELECT * FROM devices;";
			String sel1;
			int manf, prod;
			ResultSet rs = stmt.executeQuery(sel);

			while (rs.next()) {
				dArray[index++] = rs.getInt("moduleid");
			}
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception ex ) {
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
		}
	}
	public static void updateDevicesFromLinkCmd( String dbName,  int linkID, Boolean bActivate)
	{
		Connection c = null;
		Statement stmt = null;
		try {
			int status = 0;
			Class.forName("org.sqlite.JDBC");
			String conn = "jdbc:sqlite:" + dbName;
			c = DriverManager.getConnection(conn);
			c.setAutoCommit(true);
			stmt = c.createStatement();
			String sel = "SELECT * FROM presets WHERE linkid = " + linkID + ";";
			String sel1 = "SELECT * FROM links WHERE linkidnum = " + linkID + ";";
			String linkName = "";
			int manf, prod;
			ResultSet rs1 = stmt.executeQuery(sel1);
			while (rs1.next()) {
				linkName = 	rs1.getString("linkname");	
			}
			//	linkName += " ON";
			String timeStamp = upbServer.getDateandTime();
			ResultSet rs = stmt.executeQuery(sel);

			while (rs.next()) {

				int tempModuleID = rs.getInt("moduleid");
				int presetDim = rs.getInt("presetdim");
				int presetFade = rs.getInt("presetfade");
				System.out.println("Device ID = " + rs.getInt("moduleid"));
				System.out.println("Link ID = " + rs.getInt("linkid"));
				System.out.println("Preset Dim = " + rs.getInt("presetdim"));
				System.out.println("FadeRate = " + rs.getInt("presetfade"));
				String sql = "update devicestatus set  moduleid=?, upddatetime=? , status=? , level=?, faderate=?, info=? where moduleid=?";
				PreparedStatement preparedStatement =
						c.prepareStatement(sql);
				preparedStatement.setInt  (1, tempModuleID);  //moduleid
				preparedStatement.setString(2, timeStamp); //
				if(bActivate == false)
				{
					presetDim = 0;
					presetFade = buildCmd.DEFAULT_FADE_RATE;
				}
				if(presetDim > 0)
					status = 1;
				else
					status = 0;
				preparedStatement.setInt(3, status);
				preparedStatement.setInt  (4, presetDim);  //level
				preparedStatement.setInt  (5, presetFade);  //faderate

				if(bActivate == true)
				{
					preparedStatement.setString  (6, linkName); 
				}
				else
				{
					preparedStatement.setString  (6, ""); 
				}

				preparedStatement.setInt  (7, tempModuleID); 
				preparedStatement.executeUpdate();
			}

			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception ex ) {
			updateDatabase.insertLogRecord(upbServer.dbName, upbServer.getDateandTime(),miscellaneous.getStackTrace(ex) );
			System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
		}
	}
}

