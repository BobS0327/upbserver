# upbserver
Universal Powerline Bus back end server to monitor UPB traffic on the power lines and also allow executing UPB commands via a web browser

The latest version now maintains a SQLite database of the current status (on,off, level) of all devices on the network.  This is 
accomplished by polling all devices listed in the export file when the application is initially started to bring the database up to date.  
The application will thereafter passively "sniff" the powerline and  update the database based upon the packet data.  This is done 
to minimize the traffic on the powerline network.  UPB commands are sent to the app via a web browser.  Thus, the app acts as a back end
UPB server.  The database is also updated when these remote commands are received.  IOW, if a ActivateLink command is received, the
databse is immediately updated to reflect the issuance of this command.

Two files must exist prior to starting upbserver.  The first file is the export file from Upstart and the second file is a 
Comma Separated Values file that contains  details on various manufacturer products.  An example of the second file follows:

5,1, 35A00-1 600W Dimming Switch, Switch  
  
5,2, 35A00-2 1000W Dimming Switch, Switch  

5,16, 35A00-3 600W Non-Dimming Switch, Switch  

5,17, 35A00-4 1000W Non-Dimming Switch, Switch  

5,18, 40A00-1 15A Relay Switch, Switch  

5,3, 55A00-1 1000W Dimming Switch, Switch  

5,4, 55A00-2 1500W Dimming Switch, Switch  
  
5,5, 55A00-3 2400W Dimming Switch, Switch  

5,32, 59A00-1 300W Lamp Module, Module  

5,48, 60A00-1 15A Appliance Module, Module  

5,80, 38A00-1 6-Button Room Controller, Keypad  

5,96, 38A00-2 8-Button House Controller, Keypad  

The above CSV file contains all the Leviton/Hai devices.  The manufacturer id is 5, the device id for a 40A00-1 15A Realy Switch is 18
Next is the description of the device and finally the device type (kind) which 40A00 Realy Switch is of the SWITCH device type.
This second file must be MANUALLY created by the user.  Please use the UpStart export file description PDF as a reference for manually
creating this required file which is used to create the PRODUCTS table in the database.

The products CSV file is used to eliminate the need for hard coding this information into the app.  Thus, whenever producst are
added/removed, all the needs to be done is update the products CSV file.  Rebuilding of the app is not necessary.

There is also a config.properties file that must be updated prior to executing the application An example of the file is..

*#*upbServer configuration properties  
*#*Linux /dev/ttyUSB0  
comm=COM4  
*#*port for http server  
port=8080  
upbexpfilefullpathname=c:/temp/myUpStart.upe  
dbname=upbserver.db  
sourceid=255  
*#*number of seconds delay between issuing serial write commands
delaybetweencommands=4  
httpcontext=/upb  
networkid=1  
*#*name of csv input file for product description table (products)  
productcsvfile=c:/temp/upbserver.csv  
*#*delay for sending web response, needed for getting response from device and sending response to client  
webresponsedelay=6  
*#* source email for sending emails/texts  
fromemail=abc@wherever.com  
*#*target email  
toemail=cde@whatever.com  
emailuserid=goofy@whatever.com  
emailpassword=password  
*#*reboot timer values  
rebootserver=0,1,0,0,0  

The *#* sign is a COMMENT.   
The delaybetweencommands and webresponsedelay are used to fine tune the app for your installation.  These variable may have to be increased/decreased
as required.

The email variables are NOT yet implemented.  I intend to add diagnostics to the app to determine if network and/or RS-232 connectivity has been lost.  At which
time, I'll restart the app and restest network/RS-232 connectivity to verify problem has been resolved.  I will also send a text alert to notify user of any
issues.

The rebootserver option will allow the user to restart the hardware (RPI, Windows desktop etc.) once a week at a specific time.  This is an optional variable and does NOT have 
to be used.  I added it for anyone using this app on a Windows desktop. I've found that Windows can become somewhat unstable after running for an extended 
period of time.  Especially, when MS sends updates to the machine.  Thus, the reason for the reboot option.  This option is NOT yet implemented.

For serial communications, I use the Java-Simple-Serial-Connector.

There are five web commands, BLINKON, ACTIVATE, DEACTIVATE, STATUS and GOTO.  They are used as follows

BLINKON:  
localhost:8080/upb?action=blinkon&moduleid=141&blinkrate=200

Where moduleid is the  device number and blinkrate is a value from 0 to 255.  blinkrate is optional, if not used, it will default to 255

ACTIVATE A LINK:  
localhost:8080/upb?action=activatelink&linkid=3

This activates link #3


DEACTIVATE A LINK:  
localhost:8080/upb?action=deactivatelink&linkid=3

This deactivates link #3


STATUS:  
localhost:8080/upb?action=status&moduleid=141&blinkrate

This returns current status of device # 140


GOTO:  
localhost:8080/upb?action=goto&moduleid=141&level=98&faderate=200

This sets device # 141 to a level of 98 at a faderate of 200.  Faderate is optional, if not used, it defaults to 255.  Level is required.  It is a value of 0 to 100.



