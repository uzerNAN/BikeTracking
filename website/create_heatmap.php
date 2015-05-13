<?php require_once 'functions.php'; ?>

<html lang="en">
<head>
	<title>Heatmap</title>
	<meta charset="utf-8">
	<link rel="stylesheet" href="css/style.css" type="text/css" media="all">
	<script type="text/javascript" src="js/jquery-1.5.2.js" ></script>
	<script type="text/javascript" src="js/jquery.nivo.slider.pack.js"></script>
	<script>

<?php
	//1. creat a database connection
		$dbhost = "localhost";
		$dbuser = "newuser";
		$dbpass = "7zijian";
		$dbname = "cn3";
		$connection = mysqli_connect($dbhost, $dbuser, $dbpass, $dbname);
		
	//Test if connection occurred
	if(mysqli_connect_errno()){
		die("Database connenction failed:".
			mysqli_connect_error().
			"(".mysqli_connect_errno().")"
			);
	}

	
?>

</script>
</head>
<body id="page1">
<div class="main">
<!--header -->
	<header>
		<div class="wrapper">
			<span id="slogan">Bike tracking	via	smartphones</span>
		</div>
		<nav>
			<ul id="menu">
				<li ><a href="index.html"><span><span>About</span></span></a></li>
				<li><a href="bicycling_route.html"><span><span>Bicycling Route</span></span></a></li>
				<li id="menu_active" class="end"><a href="create_heatmap.php"><span><span>Heatmap</span></span></a></li>
			</ul>
		</nav>
	</header>
<!-- / header -->
			
			<form action="t0_heatmap.php" method="post" class="centered">
			<h2>Select a date:
		      <select  name="date" >
			  <option value="0"></option>
		      <?php 
		          $date_set = find_all_dates();
				  $date_db_num_rows= mysqli_num_rows($date_set);
		          for($count=0; $count<$date_db_num_rows; $count++){
					$date_db_result= mysqli_fetch_row($date_set);
					  echo "<option value=\"{$date_db_result[0]}\">{$date_db_result[0]}</option>";
		          }
		      ?>		          
		      </select><br>
			  Select a time:
			  <select name="time" >
				<option value="0"></option>
				<option value="1">00:00-00:59</option>
				<option value="2">01:00-01:59</option>
				<option value="3">02:00-02:59</option>
				<option value="4">03:00-03:59</option>
				<option value="5">04:00-04:59</option>
				<option value="6">05:00-05:59</option>
				<option value="7">06:00-06:59</option>
				<option value="8">07:00-07:59</option>
				<option value="9">08:00-08:59</option>
				<option value="10">09:00-09:59</option>
				<option value="11">10:00-10:59</option>
				<option value="12">11:00-11:59</option>
				<option value="13">12:00-12:59</option>
				<option value="14">13:00-13:59</option>
				<option value="15">14:00-14:59</option>
				<option value="16">15:00-15:59</option>
				<option value="17">16:00-16:59</option>
				<option value="18">17:00-17:59</option>
				<option value="19">18:00-18:59</option>
				<option value="20">19:00-19:59</option>
				<option value="21">20:00-20:59</option>
				<option value="22">21:00-21:59</option>
				<option value="23">22:00-22:59</option>
				<option value="24">23:00-23:59</option>

			  </select><br>
			  (Note: Cannot be blank in BOTH options)
		   </h2>
		   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		   <input type="submit" name="submit" value="Display"/>
		</form>

	<br><br><br>
			<!--footer -->
			<footer>
				<div class="wrapper">
					<div class="links">
Copyright &copy; 2015. Uppsala University All rights reserved.
				</div>
			</footer>
			<!--footer end-->
		</div>
</body>
</html>
<?php
			//4. release returned data
			mysqli_free_result($date_set);

	//5.close database connection
	mysqli_close($connection);
?>