create table if not exists account
(
    id                 bigint auto_increment primary key,
    login              varchar(128) unique not null,
    password           varchar(128)        not null,
    email              varchar(128)        null default null,
    ssid               char(32) unique     not null comment 'токен для авторизации api запросов',
    ws_token           char(32)            null default null comment 'токен для авторизации WS сессии',
    selected_character bigint              null default null comment 'выбранный персонаж при переключении на ws сессию',
    last_logged        datetime            not null,
    created            datetime                 default current_timestamp
);

create table if not exists `character`
(
    id          bigint primary key,
    account_id  bigint       not null,
    name        varchar(128) not null,

    region      int          not null comment 'на каком континенте находится объект, либо ид дома (инстанса, локации)',
    x           int          not null comment 'координаты в игровых еденицах внутри континента (из этого расчитываем супергрид и грид)',
    y           int          not null,
    level       int          not null comment 'уровень (слой) глубины где находится объект',
    heading     tinyint      not null comment 'угол поворота, то куда смотрит объект',

    deleted     bool         not null default false,
    online_time bigint       not null default 0,
    created     datetime     not null default current_timestamp,

    foreign key (account_id) references account (id)
);

create table if not exists grid
(
    region     int    not null,
    x          int    not null,
    y          int    not null,
    level      int    not null,
    last_tick  bigint not null,
    tiles_blob blob   not null,
    constraint unique uniq_grid (region, x, y, level)
) engine = MyISAM;

create table if not exists inventory
(
    id           bigint primary key,
    inventory_id bigint   not null,
    type         int      not null comment 'тип объекта',
    x            int      not null,
    y            int      not null,
    quality      smallint not null comment 'качество',
    count        int      not null comment 'количество в стаке',
    last_tick    bigint   not null comment 'время последнего апдейта объекта',
    deleted      bool     not null
);

create table if not exists object
(
    id          bigint primary key,
    region      int          not null comment 'на каком континенте находится объект, либо ид дома (инстанса, локации)',
    x           int          not null comment 'координаты в игровых еденицах внутри континента (из этого расчитываем супергрид и грид)',
    y           int          not null,
    level       int          not null comment 'уровень (слой) глубины где находится объект',
    heading     tinyint      not null comment 'угол поворота, то куда смотрит объект',
    grid_x      int          not null comment 'координаты грида в котором находится объект',
    grid_y      int          not null,
    type        int          not null comment 'тип объекта',
    quality     smallint     not null  comment 'качество',
    hp          int          not null  comment 'здоровье (hit points) объекта',
    create_tick bigint       not null comment 'время создания, игровой тик сервера',
    last_tick   bigint       not null  comment 'время последнего апдейта объекта',
    data        varchar(255) null default null comment 'внутренние данные объекта в json формате',
    index idx (region, x, y, level)
);

create table if not exists global_var
(
    name         varchar(32)   not null primary key,
    value_long   bigint        null,
    value_string varchar(1024) null
);