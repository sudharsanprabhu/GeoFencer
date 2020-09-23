<?php
include 'db.ini';

$conn = new mysqli($servername, $username, $password, $dbname);

$usr=$_POST['username'];
if(isset($usr))
{
 $conn->query("UPDATE accounts SET isConnected=false WHERE username='$usr'");
}
$conn->close();
?>