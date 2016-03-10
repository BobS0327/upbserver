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
import java.sql.Statement;
import java.sql.DriverManager;

public class initializeDatabase {
   
	 public static void createDB(String dbName) throws Exception
	    {
	        // register the driver 
	        String sDriverName = "org.sqlite.JDBC";
	        Class.forName(sDriverName);
	        String sTempDb = dbName;
	        String sJdbc = "jdbc:sqlite";
	        String sDbUrl = sJdbc + ":" + sTempDb;
	        int iTimeout = 30;
	        String sMakeTable = "CREATE TABLE devices (moduleid numeric, manufacturer numeric, prodid numeric,  fw numeric, channels numeric, numtransmitcomp numeric, numrececomp numeric, roomname text, devname text, packettype numeric)";
	        String sMakeTable1  = "CREATE TABLE links (linkidnum numeric, linkname text)";
	        String sMakeTable2 = "CREATE TABLE header (fileversion numeric, numupbdevices numeric, numdefinedlinks numeric, networkid numeric, networkpassword numeric)";
	        String sMakeTable3 = "CREATE TABLE products (manufacturer numeric, prodid numeric, proddesc text, kind text)";
	        String sMakeTable4 = "CREATE TABLE devicestatus (moduleid numeric, upddatetime text, status numeric, level numeric, faderate numeric, info text)";
	        String sMakeTable5 = "CREATE TABLE presets (moduleid numeric, linkid numeric, channel numeric, presetdim numeric, presetfade numeric)";
	        String sMakeTable6 = "CREATE TABLE loginfo (timestamp text, info text)"; 
	        
	        
	        // create a database connection
	        Connection conn = DriverManager.getConnection(sDbUrl);
	        try {
	            Statement stmt = conn.createStatement();
	            try {
	                stmt.setQueryTimeout(iTimeout);
	                stmt.executeUpdate( sMakeTable );
	                stmt.executeUpdate( sMakeTable1 );
	                stmt.executeUpdate( sMakeTable2 );
	                stmt.executeUpdate( sMakeTable3 );
	                stmt.executeUpdate( sMakeTable4 );
	                stmt.executeUpdate( sMakeTable5 );
	                stmt.executeUpdate( sMakeTable6 );
	             
	            } finally {
	                try { stmt.close(); } catch (Exception ignore) {  }
	            }
	        } finally {
	            try { conn.close(); } catch (Exception ignore) {}
	        }
	        addProducts(dbName);
	    }
	
	 public static void addProducts(String dbName)
	 {
		 Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      String conn = "jdbc:sqlite:" + dbName;
	      c = DriverManager.getConnection(conn);
	      c.setAutoCommit(false);
	      System.out.println("Opened database successfully");
	      stmt = c.createStatement();
	      stmt.executeUpdate("INSERT INTO products " + "VALUES (5,1, '35A00-1 600W Dimming Switch', 'Switch')");
	      stmt.executeUpdate("INSERT INTO products " + "VALUES (5,2, '35A00-2 1000W Dimming Switch', 'Switch')");
	      stmt.executeUpdate("INSERT INTO products " + "VALUES (5,16, '35A00-3 600W Non-Dimming Switch', 'Switch')");
	      stmt.executeUpdate("INSERT INTO products " + "VALUES (5,18, '40A00-1 15A Relay Switch', 'Switch')");
	      stmt.executeUpdate("INSERT INTO products " + "VALUES (5,3, '55A00-1 1000W Dimming Switch', 'Switch')");
	      stmt.executeUpdate("INSERT INTO products " + "VALUES (5,4, '55A00-2 1500W Dimming Switch', 'Switch')");
	      stmt.executeUpdate("INSERT INTO products " + "VALUES (5,5, '55A00-3 2400W Dimming Switch', 'Switch')");
	      stmt.executeUpdate("INSERT INTO products " + "VALUES (5,32, '59A00-1 300W Lamp Module', 'Module')");
	      stmt.executeUpdate("INSERT INTO products " + "VALUES (5,48, '60A00-1 15A Appliance Module', 'Module')");
	      stmt.executeUpdate("INSERT INTO products " + "VALUES (5,80, '38A00-1 6-Button Room Controller', 'Keypad')");
	      stmt.executeUpdate("INSERT INTO products " + "VALUES (5,96, '38A00-2 8-Button House Controller', 'Keypad')");
	          stmt.close();
	          c.commit();
	          c.close();
	        } catch ( Exception e ) {
	          System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	          System.exit(0);
	        }
	        System.out.println("Products table successfully created");
	 }
	 
	
}
