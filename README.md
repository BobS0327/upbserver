
# upbserver
IMPORTANT:  You must now have a hobbyist (free) PubNub account to use the upbServer and upbClient applications.  Also, note that
upbServer and upbClient must run on separate machines.  This is because Pubnub uses the IP addresses in the source and target operatons.
Thus, the need for two IP addresses.

Universal Powerline Bus back end server to monitor UPB traffic on the power lines and also allow executing UPB commands via 
primarily via a Pubnub enabled GUI client and secondarily from a web browser.

The latest version now maintains a SQLite database of the current status (on,off, level) of all devices on the network.  This is 
accomplished by passively monitor all traffic on the powerline and updating the database with the current status of the device.  Initially, on start up, the serve app will poll
all devices to get theire current status.  This is done to minimize the traffic on the powerline network.  
The app acts as a back end UPB server.  The database is also updated when these remote commands are received.  IOW, if a Activate Link command 
is received, the databse is immediately updated to reflect the issuance of this command.

upbServer is designed to run headless as a back end server.  Status of the server is reported once every twenty four hours via
email text message.  Also, the app attempts to recover from any problems it encounters.  It will also post details of the 
problem(s) to a database log table.  This table can be reviewed to determine the source of the problem.


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
delaybetweencommands=5   
httpcontext=/upb   
networkid=1   
*#*name of csv input file for product description table (products)   
productcsvfile=c:/temp/upbserver.csv   
*#*delay for sending web response, needed for getting response from device and sending response to client   
webresponsedelay=6   
*#* source email for sending emails/texts   
fromemail=user@usermail.com   
*#*target email   
toemail=targetuser@usermail.com   
emailuserid=targetuser@usermail.com   
emailpassword=password   
*#*run diagnostic at specific hour minute (HH,MM) in 24 hour military time   
diagnosticRunTime=7,30   
*#*name of interface needed to get local ip address of computer   
*#*this is needed for RPI since RPI default interface returns 127.0.0.1   
*#*networkinterface is meaningless for Windows   
networkinterface=eth1   
*#*port used to transfer database file to client   
portusedforfiledownload=8081   

The *#* sign is a COMMENT.   
The delaybetweencommands and webresponsedelay are used to fine tune the app for your installation.  These variable may have to be increased/decreased
as required.

The diagnosticRumTime variable is used to send an email at the time determined (hh,mm in military time) with info on the health (status)
of upbServer.  It provides total uptime as well as test results verifying that the network and serial link are functioning properly.

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



