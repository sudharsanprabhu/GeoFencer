<?php
include 'db.ini';

$conn = new mysqli($servername, $username, $password, $dbname);
$out = new stdClass();

if(isset($_POST['username']))
{
 $usr=$_POST['username'];
 
 $latitude=$conn->query("SELECT latitude FROM accounts WHERE username='$usr'")->fetch_object()->latitude;
 $longitude=$conn->query("SELECT longitude FROM accounts WHERE username='$usr'")->fetch_object()->longitude;
 $code=$conn->query("SELECT id FROM accounts WHERE username='$usr'")->fetch_object()->id;
 $name=$conn->query("SELECT name FROM accounts WHERE username='$usr'")->fetch_object()->name;
 $connectionStatus=$conn->query("SELECT isConnected FROM accounts WHERE username='$usr'")->fetch_object()->isConnected;


 $out->latitude=$latitude;
 $out->longitude=$longitude;
 $out->code=$code;
 $out->name=$name;
 $out->connectionStatus=$connectionStatus;

 $jsonOut=json_encode($out);
 echo $jsonOut; 
}
 $conn->close();
 ?>