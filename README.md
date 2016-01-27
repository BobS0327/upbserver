# upbserver
Universal Powerline Bus back end server to monitor UPB traffic on the power lines

This is the beginning of a fully functional Universal Powerline Bus (UPB) software gateway which can be used to monitor all UPB
powerline traffic.  It will also allow the sending of UPB commands from remote devices such as mobile phones tablets etc.
Currently, this first version only monitors UPB traffic and "decodes that traffic to make it human understandable.  That is,
it parses the "PU" commands into human readable data.

There is a async server stub in the code that is currently disabled.  I plan to work on that function very shortly.

Ultimately, the backend server will maintain a SQLite database of all UPB devices and their current state.  Thus, a remote user
will be able to query the status of a device in addition to manipulating that device.

Finally, for serial communications, I use the Java-Simple-Serial-Connector.


