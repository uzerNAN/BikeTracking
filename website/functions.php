<?php
	function get_data(){
		$data = array();
		$i = 0;
		//3. use returned database
		while($row = mysqli_fetch_array($result)){
			//output data from each row
			//var_dump($row);
			$data[$i] = $row;
			$i++;
		}
		echo $data[0][1];
	}	
	
    function redirect_to($new_location){
        header("Location:".$new_location);
        exit;
    }
	function confirm_query($result_set){
		if(!$result_set){
			die("Database query failed.");
		}
	}
	
	function find_all_dates(){
		global $connection;
		$query 	= "select distinct date(time) ";
		$query .= "from node ";
		$dates_set = mysqli_query($connection,$query);
		// test if there was a query error
		confirm_query($dates_set);
		return $dates_set;
	}
	
	function find_pages_for_subject($subject_id){

		global $connection;
		$query 	= "select * ";
		$query .= "from pages ";
		$query .= "WHERE visible = 1 AND subject_id = {$subject_id} ";
		$query .= "ORDER BY position ASC ";

		$page_set = mysqli_query($connection,$query);
		confirm_query($page_set);
		return $page_set;
	}

?>