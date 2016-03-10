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
package miscMethods;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import com.bobs0327.upbServer;

public class getIPAddressAndHostname {

	public static String getIPAddresAndHostname()
	{
		String returnString = "";
		String ipAddr = "";
		String hostname = "";
		String OS = System.getProperty("os.name").toLowerCase();
		try {

			if(OS.indexOf("win") >= 0)
			{
				InetAddress inetAddr = InetAddress.getLocalHost();
				byte[] addr = inetAddr.getAddress();
				// Convert to dot representation

				for (int i = 0; i < addr.length; i++) {
					if (i > 0) {
						ipAddr += ".";
					}
					ipAddr += addr[i] & 0xFF;
				}
				hostname = inetAddr.getHostName();
			}
			else
				if(OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 )
				{
					InetAddress inetAddr = InetAddress.getLocalHost();
					  hostname = inetAddr.getHostName();
					for (Enumeration<NetworkInterface> ifaces = 
							NetworkInterface.getNetworkInterfaces();
							ifaces.hasMoreElements(); )
					{
						NetworkInterface iface = ifaces.nextElement();
						System.out.println(iface.getName() + ":");
						for (Enumeration<InetAddress> addresses =
								iface.getInetAddresses();
								addresses.hasMoreElements(); )
						{
							InetAddress address = addresses.nextElement();
							if (iface.getName().equalsIgnoreCase(upbServer.networkInterface))
							{
								ipAddr = address.toString();
								ipAddr = ipAddr.replace("/", "");
							//System.out.println("  " + address);
							}
						}
					}
				}
				else
				{
					ipAddr = "Unknown";
					hostname = "Unknown";
				}
			upbServer.serverIPAddress = ipAddr;
			returnString =  String.format("IP Address: %s      Hostname: %s", ipAddr, hostname );

		}
		catch (UnknownHostException e) {
//			returnString = "Host not found";
			System.out.println("Host not found: " + e.getMessage());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnString;	
	}
}
