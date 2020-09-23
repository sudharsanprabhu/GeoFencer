<?php
include 'db.ini';

$conn = new mysqli($servername, $username, $password, $dbname);

 $usr=$_POST['username'];
 $lat=$_POST['latitude'];
 $lon=$_POST['longitude'];
 
 $setLocation="UPDATE accounts SET latitude='$lat', longitude='$lon', isConnected=true WHERE username='$usr'";
 $conn->query($setLocation);

$conn->close();
?>