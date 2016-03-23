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
import java.io.IOException;

public class restartComputer {

	public static void restart() throws RuntimeException, IOException {
		String shutdownCommand;
		String operatingSystem = System.getProperty("os.name");

		if ("Linux".equals(operatingSystem) || "Mac OS X".equals(operatingSystem)) {
			shutdownCommand = "shutdown -r now";
		}
		else if ("Windows".equals(operatingSystem)) {
			shutdownCommand = "shutdown.exe -r -t 0";
		}
		else {
			throw new RuntimeException("Unsupported operating system.");
		}
		Runtime.getRuntime().exec(shutdownCommand);
		System.exit(0);
	}
}

