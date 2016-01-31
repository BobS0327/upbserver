# upbserver
Universal Powerline Bus back end server to monitor UPB traffic on the power lines

This is the beginning of a fully functional Universal Powerline Bus (UPB) software gateway which can be used to monitor all UPB
powerline traffic.  It will also allow the sending of UPB commands from remote devices such as mobile phones tablets etc.
Currently, this first version only monitors UPB traffic and "decodes that traffic to make it human understandable.  That is,
it parses the "PU" commands into human readable data.

There is a async server stub in the code that is currently disabled.  I plan to work on that function very shortly.

Ultimately, the backend server will maintain a SQLite database of all UPB devices and their current state.  Thus, a remote user
will be able to query the status of a device in addition to manipulating that device.

Please check the config.properties file to make adjustments for your environment.  Also, the delaybetweencommands should
not be set below four (4) seconds.  Otherwise, your serial write commands may fail.

Finally, for serial communications, I use the Java-Simple-Serial-Connector.


Update

The latest version now maintains a SQLite database of the current status (on,off, level) of all devices on the network.  This is 
accomplished by polling all devices listed in the export file when the application is initially started to bring the database up to date.  
The application will thereafter passively "sniff" the powerline and  update the database based upon the packet data.  This is done 
to minimize the traffic on the powerline network.  Thus, when UPB remote access capability is implemented in the app, a 
status request will get info from the database as opposed to issuing a status command packet on the network.

The database will also have other tables in addition to the current device status table.  It will have a table for Leviton/HAI
product name and id and another table for Link ID information


Web commands:
The following command will update a device.  In this example we are updating device 141 to GOTO level of 70 at a faderate of 34.
http://localhost:8080/upb?action=update&moduleid=141&level=70&faderate=34

Results, if command successful:
{"TimeStamp":"Jan 31, 2016 12:25:26 AM","Status":1,"Description":"35A00-1 600W Dimming Switch","Room":"Int Lights","Device":"1st Flr Step","Level":70,"ModuleID":141,"Kind":"Switch"}

Results if command failed:
{} or a web error of 404

The moduleid variable is the device number, level is a value between 0 and 100.  A value of 0 turns the device off.  A value of 100 turns the
light full on.  FadeRate will take a value from 0 to 100.



http://localhost:8080/upb?action=status&moduleid=141

Results, if command successful:
{"TimeStamp":"Jan 31, 2016 8:33:04 AM","Status":0,"Description":"35A00-1 600W Dimming Switch","Room":"Int Lights","Device":"1st Flr Step","Level":0,"ModuleID":141,"Kind":"Switch"}

Results if command failed:
{} or a web error of 404

The action of status only needs a moduleid input.



