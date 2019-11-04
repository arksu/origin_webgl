DROP DATABASE IF EXISTS origin;
CREATE DATABASE origin;

USE origin;

CREATE TABLE IF NOT EXISTS `users` (
    `id`            INT(11)         NOT NULL    AUTO_INCREMENT,
    `login`         VARCHAR(64)     NOT NULL,
    `password`      VARCHAR(64)     NOT NULL,
    `createTime`    TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    `ssid`         CHAR(32)        NULL        DEFAULT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `login_uniq` (`login`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8;

