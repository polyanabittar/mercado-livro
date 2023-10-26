CREATE TABLE customer(
	id int auto_increment primary key,
    nome varchar(255) not null,
    email varchar(255) not null unique
);
