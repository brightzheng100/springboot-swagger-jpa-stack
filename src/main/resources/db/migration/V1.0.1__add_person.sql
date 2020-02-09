CREATE TABLE person (
	person_id integer not null,
	person_first_name varchar(255) not null,
	person_last_name varchar(255) not null,
   	primary key(person_id)
);

insert into person (person_id, person_first_name, person_last_name) values (1, 'Bright', 'Zheng');
