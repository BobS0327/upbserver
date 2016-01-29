package databaseMethods;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class updateDatabase
{
	public static void insertDeviceRecord( String dbName, int moduleid, int manufacturer, int prodid, int fw, int channels, int numtransmit, int numrece, String room, String devname, int packet )
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
			String values =	  "VALUES (" + moduleid + "," + manufacturer + "," + prodid + "," + fw + "," + channels + "," + numtransmit + "," + numrece + "," + "'" + room + "', '" + devname + "', " + packet + ");";
			String sql = "INSERT INTO devices (moduleid, manufacturer, prodid, fw, channels,  numtransmitcomp, numrececomp, roomname, devname,packettype) " + values;
			stmt.executeUpdate(sql);
			stmt.close();
			c.commit();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
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
			System.out.println("Opened database successfully");
			stmt = c.createStatement();
			String values =	  "VALUES (" + importVersion + "," + expectedDeviceCount + "," + expectedLinkCount + "," + importNetworkID + "," + networkPassword + ");";
			String sql = "INSERT INTO header (fileversion, numupbdevices, numdefinedlinks, networkid, networkpassword) " + values;
			stmt.executeUpdate(sql);
			stmt.close();
			c.commit();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		System.out.println("Header record inserted successfully");
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
			System.out.println("Opened database successfully");
			stmt = c.createStatement();
			String values =	  "VALUES (" + linkid + ", " + "'" + linkname +"'" + ");";
			String sql = "INSERT INTO links (linkidnum, linkname) " + values;
			stmt.executeUpdate(sql);
			stmt.close();
			c.commit();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		System.out.println("Link Record inserted successfully");
	}

	public static void clearTables(String dbName, String tableName)  {
		Connection c = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String conn = "jdbc:sqlite:" + dbName;
			c = DriverManager.getConnection(conn);
			//  c.setAutoCommit(false);
			System.out.println("Opened database successfully");
			Statement stmt = c.createStatement();
			String sql = "DELETE FROM " + tableName+ ";";
			stmt.executeUpdate(sql);
			//   c.commit();
		}
		catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	} 
	public static void findDeviceRecord( String dbName, int  moduleID)
	{
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
			int manf, prod;
			ResultSet rs = stmt.executeQuery(sel);
			boolean devrecfound = false;
			boolean prodrecfound = false;

			while (rs.next()) {
				devrecfound = true;
				System.out.println("Room Name = " + rs.getString("roomname"));
				System.out.println("Device Name = " + rs.getString("devname"));
				manf = rs.getInt("manufacturer");
				prod = rs.getInt("prodid");
				System.out.println("Manufacturer # = " + manf);
				System.out.println("Prod ID # = " + prod);
				sel1 = "SELECT * FROM products WHERE manufacturer = " + manf + " AND prodid = " + prod;
				ResultSet rs1 = stmt.executeQuery(sel1);

				while (rs1.next()) {
					prodrecfound = true;
					System.out.println("Product Desc = " + rs1.getString("proddesc"));
					System.out.println("Kind = " + rs1.getString("kind"));
				}
				rs1.close(); 
			}
			rs.close();
			stmt.close();
			//  c.commit();
			rs.close();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}

	public static void updateStatusTable( String dbName, int  moduleID,  int level )
	{
		Connection c = null;
		Statement stmt = null;
		int status;
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
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
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();

			if(devrecfound == false)
			{	

				String insertTableSQL = "INSERT INTO devicestatus"
						+ "(moduleid, upddatetime, status, level) VALUES"
						+ "(?,?,?,?)";

				PreparedStatement preparedStatement = c.prepareStatement(insertTableSQL);
				preparedStatement.setInt(1, moduleID);
				preparedStatement.setString(2, timeStamp);
				if(level > 0)
					status = 1;
				else
					status = 0;
				preparedStatement.setInt(3, status);
				preparedStatement.setInt(4, level);

				// execute insert SQL stetement
				preparedStatement.executeUpdate();

			}
			else
			{
				String sql = "update devicestatus set  moduleid=?, upddatetime=? , status=? , level=? where moduleid=?";

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
				preparedStatement.setInt  (5, moduleID); 

				int rowsAffected = preparedStatement.executeUpdate();
				devrecfound = false;
			}


			rs.close();
			stmt.close();
			rs.close();
			c.close();



		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
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
			
			//	prod = rs.getInt("prodid");
			
			}
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}

}

