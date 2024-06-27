create table if not exists account
(
    id          bigint auto_increment primary key,
    login       varchar(128) unique not null,
    password    varchar(128)        not null,
    email       varchar(128)        null default null,
    ssid        char(32) unique     not null comment 'токен для авторизации api запросов',
    ws_token    char(32)            null default null comment 'токен для авторизации WS сессии',
    last_logged datetime            not null,
    created     datetime                 default current_timestamp
);

create table if not exists `character`
(
    id          bigint primary key,
    account_id  bigint not null,
    name        varchar(128),
    deleted     bool     default false,
    online_time bigint   default 0,
    created     datetime default current_timestamp,
    
    foreign key (account_id) references account (id)
);