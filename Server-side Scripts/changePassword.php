<?php
include 'db.ini';

$conn = new mysqli($servername, $username, $password, $dbname);
$out = new stdClass();

if(isset($_POST['username'])&&isset($_POST['newPassword']))
{
 $usr=$_POST['username'];
 $newPassword=$_POST['newPassword'];
 
 $oldPassword=$conn->query("SELECT password FROM accounts WHERE username='$usr'")->fetch_object()->password;
 
 if($oldPassword==$newPassword)
 {
	 $out->code=0;
	 $out->message="New and old passwords are the same";
 }
 else
 {
   $conn->query("UPDATE accounts set password='$newPassword' WHERE username='$usr'");
   $out->code=1;
 }
 

 $jsonOut=json_encode($out);
 echo $jsonOut; 
}
 $conn->close();
 ?>