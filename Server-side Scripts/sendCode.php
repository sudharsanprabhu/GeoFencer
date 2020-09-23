<?php
include 'db.ini';
$conn = new mysqli($servername, $username, $password, $dbname);
 
 if(isset($_POST['username']) && isset($_POST['id']))
 {
  $usr = $_POST['username'];
  $id = $_POST['id'];
 
	
  $setId="UPDATE accounts SET id='$id' WHERE username='$usr'";
  $conn->query($setId);  
}
$conn->close();
?>