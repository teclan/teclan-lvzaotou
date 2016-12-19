create table content_records
(
id 						INT(11) NOT NULL AUTO_INCREMENT,
name					varchar(50),
content					varchar(2000),
description				varchar(100),
created_at        		TIMESTAMP,
updated_at        		TIMESTAMP
);
