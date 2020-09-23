# GeoFencer
An android app to create and manage virtual geographical fences

GeoFencer is an android application that alerts the user when their device crosses a virtual boundary which was previously set by giving a notification. 
It enables us to draw polygons over a base map by adding the end points. 
The polygon may be of any shape but it must contain at least three node points. 
The location of the user’s device is marked as a blue circle and it will be updated whenever its location changes. 
The application also allows to share one’s location to another user in real time provided that both the users has logged in to their accounts. 
New users can create their own account by registering their name, username and password which will be stored in a backend database

This app requires location permission (Background location permission from Android 10+) and camera permission to scan QR code so that it can easily connect with another device running this app.

# GeoAlarm
This app also provides another feature called GeoAlarm which can be accessed from the side navigation panel. As the name suggests, it is basically an alarm which gets triggered by the device location. It creates a circular region around a specified location of given radius. When the user enters the circular region, it will play an alarm ringtone which was set by the user. It can also provide vibration feedback if required.