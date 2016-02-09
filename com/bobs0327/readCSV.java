package com.bobs0327;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import databaseMethods.updateDatabase;

public class readCSV {

	 private String csvFile;
	 private String dbname;

  public readCSV(String dname, String csvfname) {
		csvFile = csvfname;
		dbname = dname;
	}

public void run() {

	BufferedReader br = null;
	String line = "";
	String cvsSplitBy = ",";

	try {

		br = new BufferedReader(new FileReader(csvFile));
		while ((line = br.readLine()) != null) {
		        // use comma as separator
			String[] products = line.split(cvsSplitBy);
			updateDatabase.insertProductsRecord( dbname, Integer.parseInt(products[0]), Integer.parseInt(products[1]), products[2] , products[3] );
		}

	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	System.out.println("Product Descriptions loaded");
  }
}