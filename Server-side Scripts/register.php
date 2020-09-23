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
 
 

	if(!isset($_POST['username'])||!isset($_POST['password'])||!isset($_POST['name']))
	{
	 $out->code=0;	
	 $out->msg="Pease fill out all the fields...";
	 $jsonOut=json_encode($out);
      echo $jsonOut;
	  die();
	}
	else
	{
	$usr = $_POST['username'];
    $pwd = $_POST['password'];
    $name = $_POST['name'];

	$sql = "SELECT username FROM accounts WHERE username='$usr'";
    $result = $conn->query($sql);
	
	
	if ($result->num_rows == 1)
    {
	 $out->code=0;
	 $out->msg="Account already exists... Please try a different username";
	 $jsonOut=json_encode($out);
     echo $jsonOut;
	}
		  	  
	  
     else
     {		 
         $create="INSERT INTO accounts values ('$usr','$pwd','$name',null,null,null,null,null)";
		if($conn->query($create)===TRUE)
		{
			$out->code=1;
			$out->msg="Your account has been created successfully";
			$jsonOut=json_encode($out);
			echo $jsonOut;
		}
		else
		{
			$out->code=0;
	        $out->msg="Oops, something went wrong. Try again after some time";
	        $jsonOut=json_encode($out);
            echo $jsonOut;
		}		 
	 }
	}
	
$conn->close();
?>

