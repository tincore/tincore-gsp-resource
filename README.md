# gliderun-server / gsp-resource

Private server for Gliderun Android build 310+

https://play.google.com/store/apps/details?id=com.tincore.and.gliderun

Stores and synchronizes all metrics from and to Gliderun.
You can host this server at home or private network.

# Execution preconditions

You will need Java 8 to execute this program.
https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html

# Installation and execution

You can build the program yourself or, temporarily, grab already built packages from https://github.com/tincore/gliderun-server/tree/master/build

There are two jar files that you may execute:

##Resource server:
tincore-gsp-resource-<VERSION>.jar

This is the main program that you can use to syncronize Gliderun App with your PC. This program runs, by default, on port 7678. 

To execute you have two options:

###Multiple user with security:
java -jar tincore-gsp-resource-<VERSION>.jar

In multiple user mode several users can share the same server. This is the ideal situation but it may be a bit more complicated to setup. 

You would need an authorisation server to be running. Get it from https://github.com/tincore/gliderun-aut if you want to build it or, alternatively, from https://github.com/tincore/gliderun-server/tree/master/build.

Execute with the command:
java -jar tincore-aut-<VERSION>.jar
Access with the browser. You can access as user 'admin' and password 'admin'
http://localhost:7679/uaa/login

The authorisation server is needed to store users and passwords. When you try to connect with Gliderun Android app you may be presented with a login screen to identify or register your user.

###Single user without security:

java -jar tincore-gsp-resource-<VERSION>.jar --spring.profiles.active=singleuser

This is a simplified way of executing the server without authorisation or security. 

In this case only one user information is stored in the server and no authorisation is needed. This user is called "singleuser". No authorisation or security is running in the server.

If two instances of gliderun app connect in single user mode they will share the same information and possibly compete and overwrite each other information for the same periods of time.

# Android app usage
Run Gliderun 
https://play.google.com/store/apps/details?id=com.tincore.and.gliderun

1.Got to Synchronize menu option
2. Click on Gliderun SRV
3. Click on preferences to enable autodiscover or type the address of the server.
4. Click syncronize

# Finally

Send me an email if you need more details.
