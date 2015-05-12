

<html lang="en">
<head>
	<title>Heatmap</title>
	<meta charset="utf-8">

	<link rel="stylesheet" href="css/style.css" type="text/css" media="all">
	<script type="text/javascript" src="js/jquery-1.5.2.js" ></script>

	<script type="text/javascript" src="js/jquery.nivo.slider.pack.js"></script>
	<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&signed_in=true&libraries=visualization,places"></script>
	<script>

var map, pointarray, heatmap;
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

	//2.perform database query
	$query 	= "select * ";
	$query .= "from node ";
	$result = mysqli_query($connection,$query);
	// test if there was a query error
	if(!$result)
		die("Database query failed");

			
			$data = array();
			$i = 0;
			//3. use returned database
			while($row = mysqli_fetch_array($result)){
				//output data from each row
				//var_dump($row);
				$data[$i] = $row;
				$i++;
			}

	$query2 = "SELECT count(longitude) ";
	$query2.= "FROM node" ;
	$count = mysqli_query($connection, $query2);
	if(!$count)
		die("Count query failed");
	$count = mysqli_fetch_row($count)
?>
var m,n;
var data = <?php echo json_encode( $data ) ?>;
var NumofRows = <?php echo json_encode( $count ) ?>; 
var Data = [];

 for (m=0; m < NumofRows; m++){
		 Data.push(new google.maps.LatLng(data[m][3], data[m][2]));
 }


function initialize() {
  var markers = [];	
  var mapOptions = {
    zoom: 13,
    center: new google.maps.LatLng(59.2911, 17.9327),
    mapTypeId: google.maps.MapTypeId.ROADMAP
  };

  map = new google.maps.Map(document.getElementById("heat_map"),
      mapOptions);

  var pointArray = new google.maps.MVCArray(Data);

  heatmap = new google.maps.visualization.HeatmapLayer({
    data: pointArray
  });

  heatmap.setMap(map);
  
  // Create the search box and link it to the UI element.
  var input = /** @type {HTMLInputElement} */(
      document.getElementById('pac-input'));
  map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);

  var searchBox = new google.maps.places.SearchBox((input));
  // [START region_getplaces]
  // Listen for the event fired when the user selects an item from the
  // pick list. Retrieve the matching places for that item.
  google.maps.event.addListener(searchBox, 'places_changed', function() {
    var places = searchBox.getPlaces();

    if (places.length == 0) {
      return;
    }
    for (var i = 0, marker; marker = markers[i]; i++) {
      marker.setMap(null);
    }

    // For each place, get the icon, place name, and location.
    markers = [];
    var bounds = new google.maps.LatLngBounds();
    for (var i = 0, place; place = places[i]; i++) {
      var image = {
        url: place.icon,
        size: new google.maps.Size(71, 71),
        origin: new google.maps.Point(0, 0),
        anchor: new google.maps.Point(17, 34),
        scaledSize: new google.maps.Size(25, 25)
      };

      // Create a marker for each place.
      var marker = new google.maps.Marker({
        map: map,
        icon: image,
        title: place.name,
        position: place.geometry.location
      });

      markers.push(marker);

      bounds.extend(place.geometry.location);
    }

    map.fitBounds(bounds);
  });
  // [END region_getplaces]

  // Bias the SearchBox results towards places that are within the bounds of the
  // current map's viewport.
  google.maps.event.addListener(map, 'bounds_changed', function() {
    var bounds = map.getBounds();
    searchBox.setBounds(bounds);
  });
}

function toggleHeatmap() {
  heatmap.setMap(heatmap.getMap() ? null : map);
}

function changeGradient() {
  var gradient = [
    'rgba(0, 255, 255, 0)',
    'rgba(0, 255, 255, 1)',
    'rgba(0, 191, 255, 1)',
    'rgba(0, 127, 255, 1)',
    'rgba(0, 63, 255, 1)',
    'rgba(0, 0, 255, 1)',
    'rgba(0, 0, 223, 1)',
    'rgba(0, 0, 191, 1)',
    'rgba(0, 0, 159, 1)',
    'rgba(0, 0, 127, 1)',
    'rgba(63, 0, 91, 1)',
    'rgba(127, 0, 63, 1)',
    'rgba(191, 0, 31, 1)',
    'rgba(255, 0, 0, 1)'
  ]
  heatmap.set('gradient', heatmap.get('gradient') ? null : gradient);
}

function changeRadius() {
  heatmap.set('radius', heatmap.get('radius') ? null : 20);
}

function changeOpacity() {
  heatmap.set('opacity', heatmap.get('opacity') ? null : 0.2);
}

google.maps.event.addDomListener(window, 'load', initialize);

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
				<li id="menu_active" class="end"><a href="heatmap.php"><span><span>Heatmap</span></span></a></li>
			</ul>
		</nav>
	</header>
<!-- / header -->

	<input id="pac-input" class="controls" type="text" placeholder="Search Box">
    <div id="heat_map"></div>
		<div id="panel">
      <button onclick="toggleHeatmap()">Toggle Heatmap</button>
      <button onclick="changeGradient()">Change gradient</button>
      <button onclick="changeRadius()">Change radius</button>
      <button onclick="changeOpacity()">Change opacity</button>
    </div>
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
			mysqli_free_result($result);

	//5.close database connection
	mysqli_close($connection);
?>