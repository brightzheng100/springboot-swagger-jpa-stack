create table student
(
   id integer not null,
   nid varchar(20) not null,
   name varchar(255) not null,
   primary key(id)
);

insert into student values(10001, 'A1111111','Tom');

insert into student values(10002, 'B2222222','Jerry');

insert into student values(10003, 'C3333333','Hans');