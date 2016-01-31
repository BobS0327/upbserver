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
	private static final String LEVEL = "level";
	private static final String FADERATE = "faderate";
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
		String Level= "";
		String FadeRate = "";
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
							if (LEVEL.equalsIgnoreCase(param[PARAM_NAME_IDX])) {
								Level = param[PARAM_VALUE_IDX];
							}
							if (FADERATE.equalsIgnoreCase(param[PARAM_NAME_IDX])) {
								FadeRate = param[PARAM_VALUE_IDX];
							}
						}
					}
				}
			}
		}

		if (Action.equalsIgnoreCase("UPDATE"))
		{
			System.out.println("Web Update");
			mvInput.clear();
			mvInput.moduleid = Integer.parseInt(ModuleID);
			tempModuleID = Integer.parseInt(ModuleID);
			int inSourceID = Integer.parseInt(upbServer.sourceID);
			mvInput.sourceid = inSourceID;
			mvInput.isDevice = true;
			int inNetworkID = Integer.parseInt(upbServer.networkID);
			mvInput.networkid = inNetworkID;
			mvInput.isDevice = true;
			mvInput.action = 0x22;  // report State Command
			mvInput.level = Integer.parseInt(Level);
			mvInput.fadeRate = Integer.parseInt(FadeRate);
			mvInput.channel =0; 
			System.out.println("Web Update1");
			bc1.buildCmd(mvInput);
			System.out.println("Web Update2");
			String 	myCmd = mvInput.message.toString();
			String dateStr = upbServer.getDateandTime();

			System.out.println(dateStr + " Sent: "+ myCmd);
			System.out.println("Web Update3");
			upbServer.sendCmd(myCmd);
			System.out.println("Web Update4");
			try {
				Thread.sleep(upbServer.cmdDelay*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mvInput.clear();
			mvInput.moduleid = Integer.parseInt(ModuleID);
			mvInput.sourceid = inSourceID;
			mvInput.networkid = inNetworkID;
			mvInput.isDevice = true;
			mvInput.level = Integer.parseInt(Level);
			mvInput.fadeRate = Integer.parseInt(FadeRate);
			mvInput.channel =0; 
			mvInput.action = 0x30;  // report State Command

			bc1.buildCmd(mvInput);
			myCmd = mvInput.message.toString();
			dateStr = upbServer.getDateandTime();

			System.out.println(dateStr + " Sent: "+ myCmd);
			upbServer.sendCmd(myCmd);

			try {
				Thread.sleep(upbServer.cmdDelay*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			responseBody =	updateDatabase.findDeviceRecord( upbServer.dbName, tempModuleID );	
		}
		else
			if (Action.equalsIgnoreCase("STATUS"))
			{
				tempModuleID = Integer.parseInt(ModuleID);
				responseBody =	updateDatabase.findDeviceRecord( upbServer.dbName, tempModuleID );
			}
			else
			{

			}
		return responseBody;
	}
}
