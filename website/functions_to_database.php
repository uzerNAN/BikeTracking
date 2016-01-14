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
?>