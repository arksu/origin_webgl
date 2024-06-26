use origin;

create table if not exists account
(
    id   int auto_increment primary key,
    name varchar(128) not null
);

create table if not exists `character`
(
    id         int auto_increment primary key ,
    account_id int not null,
    name       varchar(128),
    foreign key (account_id) references account(id)
);