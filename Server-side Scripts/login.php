<?php
include 'db.ini';
$conn = new mysqli($servername, $username, $password, $dbname);
$out = new stdClass();
if ($conn->connect_error)
 {
	 $out->code=0;
	 $out->msg="Connection Error ".$conn->connect_error;
	 $jsonOut=json_encode($out);
     echo $jsonOut;
	 die();
 }
 
 if(isset($_POST['username']) && isset($_POST['password']))
 {
  $usr = $_POST['username'];
  $pwd = $_POST['password'];
 
	$sql = "SELECT username FROM accounts WHERE username='$usr'";
    $result = $conn->query($sql);
	
	
	if ($result->num_rows == 1)
    {
	  $db_pwd = $conn->query("SELECT password FROM accounts WHERE username = '$usr'")->fetch_object()->password; 
	  if($pwd == $db_pwd)
	  { 
          $active=$conn->query("SELECT isActive FROM accounts WHERE username='$usr'")->fetch_object()->isActive;
		  if($active==0)
		  {
		   $conn->query("UPDATE accounts SET isActive=true WHERE username='$usr'");	  
           $out->code=1;	
           $jsonOut=json_encode($out);
		   echo $jsonOut;		
		  }
		  else
		  {
	     $out->code=0;
         $out->msg="You have already been logged on a different device";		 
		 $jsonOut=json_encode($out);
		 echo $jsonOut;
		  }
	  }
     else
     {	
         $out->code=0;
         $out->msg="Incorrect Password";		 
		 $jsonOut=json_encode($out);
		 echo $jsonOut;
	 }
    }
    else 
    {
	     $out->code=0;
         $out->msg="Sorry, we cannot find your account. Please register instead";		 
		 $jsonOut=json_encode($out);
		 echo $jsonOut;	
    }	 
 }	
$conn->close();
?>

