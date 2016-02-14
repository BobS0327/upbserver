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
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class UPBHttpServer {

	private HttpServer httpServer;
	public UPBHttpServer(int port, String context, HttpHandler handler) {
		try {
			//Create HttpServer which is listening on the given port 
			httpServer = HttpServer.create(new InetSocketAddress(port), 0);
			//Create a new context for the given context and handler
			httpServer.createContext(context, handler);
			//Create a default executor
			httpServer.setExecutor(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void start() {
		this.httpServer.start();
	}
}
