create table student
(
   student_id integer not null,
   student_nid varchar(20) not null,
   student_name varchar(255) not null,
   primary key(student_id)
);

insert into student values(10001, 'A1111111','Tom');
insert into student values(10002, 'B2222222','Jerry');
insert into student values(10003, 'C3333333','Hans');