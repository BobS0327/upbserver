package databaseMethods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class addUPERecords2DB {

	static ArrayList<String> parsedTokenList = new ArrayList<String>();

	public static void addUPERecords(String inputDBName, String inputExpFileName )
	{
		boolean bDatabaseJustCreated = false;
		Path path = Paths.get(inputDBName);

		if (Files.notExists(path)) {
			System.out.println("Database does NOT exist, creating database!!");
			bDatabaseJustCreated = true;
			try {
				initializeDatabase.createDB(inputDBName);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		long d1 = new File(inputDBName).lastModified();
		long d2 = new File(inputExpFileName).lastModified();
		if (d1 > d2 & bDatabaseJustCreated == false)
			System.out.println("Database is newer than export file, no action needed"); 
		else if (d1 < d2 || bDatabaseJustCreated == true)
		{
			System.out.println("Database is just created or older than export file, recreating database file");
			try {
				if(bDatabaseJustCreated == false)
				{
					updateDatabase.clearTables(inputDBName, "devices");	
					updateDatabase.clearTables(inputDBName, "links");	
					updateDatabase.clearTables(inputDBName, "header");	
				}
				File file = new File(inputExpFileName);
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line;

				while ((line = bufferedReader.readLine()) != null) {

					parseImportLine(line);
					int z =getIntToken(0);
					switch(z)
					{
					case 0:
						parseImportHeader(inputDBName);
						break;
					case 2:
						parseLink(inputDBName);
						break;
					case 3:
						parseDevice(inputDBName);
						break;
					default:
						break;
					}
				}
				fileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static void parseImportHeader(String inputDBName) {
		int d1,d2,d3,d4,d5;
		d1 = getIntToken(1);
		d2 = getIntToken(2);
		d3 = getIntToken(3);
		d4 = getIntToken(4);
		d5 = getIntToken(5);
		updateDatabase.insertHeaderRecord(  inputDBName, d1,d2,d3,d4,d5 );
	}

	static void parseDevice(String inputDBName) {
		int bx = 0;
		int index = 0;
		boolean xmitsLinks = (parsedTokenList.size() >= 14) ? getIntToken(13) != 0 : false;
		if(xmitsLinks == true)
			bx =1;
		int tk =  (getIntToken(5) << 8) | getIntToken(6);
		int d1,  d3, d4,d8,d9, d10;
		String sd11, sd12; 
		d1 =  getIntToken(1);
		d3 =  getIntToken(3);
		d4 =  getIntToken(4);
		d8 =  getIntToken(8);
		d9 =  getIntToken(9);
		d10 = getIntToken(10);		
		sd11 = parsedTokenList.get(11);
		sd12 = parsedTokenList.get(12);
		updateDatabase.insertDeviceRecord( inputDBName, d1,  d3, d4, tk , d8, d9,d10, sd11, sd12, bx );	
	}

	static void parseLink(String inputDBName) {
		int d1;
		String d2;
		d1 = getIntToken(1);
		d2 = parsedTokenList.get(2);
		updateDatabase.insertLinkRecord(inputDBName, d1, d2);
	}

	static int getIntToken(int token) {
		try {
			return Integer.parseInt(parsedTokenList.get(token));
		} catch (Exception anyError) { 
			anyError.printStackTrace();
			return -1; }
	}
	static void parseImportLine(String theLine) {
		parsedTokenList.clear();

		int startCharPtr = 0;
		int lineLen = theLine.length();
		for (int charPtr = 0; charPtr < lineLen; charPtr++) {
			// Skip until we are done
			if (theLine.charAt(charPtr) != ',') continue;

			// We have a delimiter -- extract a token
			if (startCharPtr == charPtr) {
				// Empty token
				parsedTokenList.add("");
			} else {
				parsedTokenList.add(theLine.substring(startCharPtr, charPtr));
			}
			// Advance to start of next token
			startCharPtr = charPtr + 1;
		}
		// When done, see if there was a trailing element and add it
		if (startCharPtr >= lineLen)
			parsedTokenList.add("");
		else
			parsedTokenList.add(theLine.substring(startCharPtr));
	}	
}