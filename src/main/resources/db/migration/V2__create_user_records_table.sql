create table users
(
id 					INT(11) NOT NULL AUTO_INCREMENT,
username			varchar(50) not null,
password			varchar(50) not null,
role				varchar(50) not null,
realname			varchar(50),
contact 			varchar(20),
email				varchar(50),
description			varchar(100),
status			    varchar(10) default('不在线'),
ip					varchar(20),
token				varchar(100),
last_action			varchar(50),
created_at        	TIMESTAMP,
updated_at        	TIMESTAMP
);
insert into users (username,password,role,realname,contact,email) values ('system','123456','system','Teclan','010-547103','teclan@gmail.com');
insert into users (username,password,role,realname,contact,email) values ('security','123456','security','Teclan','010-547103','teclan@gmail.com');
insert into users (username,password,role,realname,contact,email) values ('audit','123456','audit','Teclan','010-547103','teclan@gmail.com');
