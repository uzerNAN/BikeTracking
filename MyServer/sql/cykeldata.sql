create database CykelData;
use CykelData;

CREATE USER sqluser IDENTIFIED BY 'sqluserpw'; 

grant usage on *.* to sqluser@localhost identified by 'sqluserpw'; 
grant all privileges on CykelData.* to sqluser@localhost;


CREATE TABLE Sessions(
	ID INT unique AUTO_INCREMENT,
	PRIMARY KEY(ID)
	);

CREATE TABLE Node(
	sessionID INT,
	time BIGINT,
	longitude DOUBLE,
	latitude DOUBLE,
	speed DOUBLE,
	acc DOUBLE,
	PRIMARY KEY (SessionID, time)
	);
	
	
	