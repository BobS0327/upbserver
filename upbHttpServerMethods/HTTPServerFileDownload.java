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


import java.io.*;
import java.net.*;
import java.util.*;

public class HTTPServerFileDownload extends Thread {
	
	static final String HTML_START = 
			"<html>" +
			"<title>HTTP upbServer</title>" +
			"<body>";
			
    static final String HTML_END = 
			"</body>" +
			"</html>";
			
	Socket connectedClient = null;	
	BufferedReader inFromClient = null;
	DataOutputStream outToClient = null;
	
			
	public  HTTPServerFileDownload(Socket client) {
		connectedClient = client;
	}			
			
	public void run() {
		
		try {
		
			System.out.println( "The Client "+
            connectedClient.getInetAddress() + ":" + connectedClient.getPort() + " is connected");
            
            inFromClient = new BufferedReader(new InputStreamReader (connectedClient.getInputStream()));                  
            outToClient = new DataOutputStream(connectedClient.getOutputStream());
		
			String requestString = inFromClient.readLine();
            String headerLine = requestString;
            	
            StringTokenizer tokenizer = new StringTokenizer(headerLine);
			String httpMethod = tokenizer.nextToken();
			String httpQueryString = tokenizer.nextToken();
				
			StringBuffer responseBuffer = new StringBuffer();
			responseBuffer.append("<b> This is the HTTP Server Home Page.... </b><BR>");
            responseBuffer.append("The HTTP Client request is ....<BR>");
              
            System.out.println("The HTTP request string is ....");
           	while (inFromClient.ready())
            {
	            // Read the HTTP complete HTTP Query
	            responseBuffer.append(requestString + "<BR>");
				System.out.println(requestString);
				requestString = inFromClient.readLine();
			}        
				
			if (httpMethod.equals("GET")) {
			  if (httpQueryString.equals("/")) {
		       	// The default home page
				sendResponse(200, responseBuffer.toString(), false);							
			  } else {							
				//This is interpreted as a file name
				String fileName = httpQueryString.replaceFirst("/", "");
				fileName = URLDecoder.decode(fileName);
				if (new File(fileName).isFile()){								
				  sendResponse(200, fileName, true);
				}
				else {
			      sendResponse(404, "<b>The Requested resource not found ...." + 
				  "Usage: http://127.0.0.1:8080/<fileName></b>", false);	
				}						
			  }										
			} 
			else sendResponse(404, "<b>The Requested resource not found ...." +
				"Usage: http://127.0.0.1:8080/<fileName></b>", false);				
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void sendResponse (int statusCode, String responseString, boolean isFile) throws Exception {
		
		String statusLine = null;
		String serverdetails = "Server: upbServer HTTPServer";
		String contentLengthLine = null;
		String fileName = null;		
		String contentTypeLine = "Content-Type: text/html" + "\r\n";
		FileInputStream fin = null;
		
		if (statusCode == 200)
			statusLine = "HTTP/1.1 200 OK" + "\r\n";
		else
			statusLine = "HTTP/1.1 404 Not Found" + "\r\n";	
			
		if (isFile) {
			fileName = responseString;			
			fin = new FileInputStream(fileName);
			contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
			if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
				contentTypeLine = "Content-Type: \r\n";	
		}						
		else {
			responseString = HTTPServerFileDownload.HTML_START + responseString + HTTPServerFileDownload.HTML_END;
			contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";	
		}			
		 
		outToClient.writeBytes(statusLine);
		outToClient.writeBytes(serverdetails);
		outToClient.writeBytes(contentTypeLine);
		outToClient.writeBytes(contentLengthLine);
		outToClient.writeBytes("Connection: close\r\n");
		outToClient.writeBytes("\r\n");		
		
		if (isFile) sendFile(fin, outToClient);
		else outToClient.writeBytes(responseString);
		
		outToClient.close();
	}
	
	public void sendFile (FileInputStream fin, DataOutputStream out) throws Exception {
		byte[] buffer = new byte[1024] ;
		int bytesRead;
	
		while ((bytesRead = fin.read(buffer)) != -1 ) {
		out.write(buffer, 0, bytesRead);
	    }
	    fin.close();
	}
			
}

