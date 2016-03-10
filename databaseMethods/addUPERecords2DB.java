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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.bobs0327.readCSV;
import com.bobs0327.upbServer;

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
		{
			System.out.println("Database is newer than export file, no action needed"); 
			
			d1 = new File(inputDBName).lastModified();
			d2 = new File(upbServer.productCSVfile).lastModified();
			if (d1 < d2  && bDatabaseJustCreated == false )
			{
				System.out.println("Products table needs to be updated, a newer CSV file exists");	
				updateDatabase.clearTables(inputDBName, "products");	
				// Rebuild the products table
				readCSV csv = new readCSV(inputDBName, upbServer.productCSVfile);
				csv.run();
			}
		}
			else if (d1 < d2 || bDatabaseJustCreated == true)
		{
			System.out.println("Database is just created or is older than export file, recreating database file");
			try {
				if(bDatabaseJustCreated == false)
				{
					updateDatabase.clearTables(inputDBName, "devices");	
					updateDatabase.clearTables(inputDBName, "links");	
					updateDatabase.clearTables(inputDBName, "header");	
					updateDatabase.clearTables(inputDBName, "presets");	
					updateDatabase.clearTables(inputDBName, "products");
					updateDatabase.clearTables(inputDBName, "loginfo");
				}
				// Rebuild the products table
				readCSV csv = new readCSV(inputDBName, upbServer.productCSVfile);
				csv.run();
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
					case 4:
						parsePresets(inputDBName); 
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

	static void parsePresets(String inputDBName) {
		int d1,d3,d4,d5,d6;
		// See if we should ignore this link
	    if (getIntToken(4) == 255) return;
		d1 = getIntToken(1);
		d3 = getIntToken(3);
		d4 = getIntToken(4);
		d5 = getIntToken(5);
		d6 = getIntToken(6);
		updateDatabase.insertPresetRecord(  inputDBName, d3,d4,d1,d5,d6 );
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