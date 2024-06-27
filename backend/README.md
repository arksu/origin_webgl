# Backend (game server)
for Origin MMO game

# How it works

- [Kotlin](https://github.com/JetBrains/kotlin) as programming language
- [MariaDB](https://github.com/mariadb) database
- [HikariCP](https://github.com/brettwooldridge/HikariCP) as JDBC connection pool
- [Exposed](https://github.com/JetBrains/Exposed/) as Kotlin ORM Framework
- [Gson](https://github.com/google/gson) for handling JSON
- [Ktor](https://github.com/ktorio/ktor) as web framework
- [HOCON](https://github.com/lightbend/config/blob/master/HOCON.md) config files

# Getting started

- install JVM 8+
- install MariaDB database for your platform
- start MariaDB server
- create a database into your MariaDB server named 'origin' 
- by default server configured for dabatase name `origin`, db user `origin` and password `origin`, you can change this in `server.conf` file
- game server is configured to start on [8020](http://localhost:8020) port.

# Fast start

### Run database via docker
You can use MariaDB from docker image:
```shell
docker run --name mariadb-origin -e MYSQL_ROOT_PASSWORD=1 -e MYSQL_DATABASE=origin -e MYSQL_USER=origin -e MYSQL_PASSWORD=origin -d -p 3406:3306 mariadb:latest
```
This will run MariaDB in docker container, create a database 'origin', user 'origin' and set password 'origin'

Local port 3406 will be redirected -> to container port 3306 (to avoid conflicts with the already installed MariaDB/MySQL). This port already pre-configured into `server.conf`

### OR Setup database locally
If you can't use docker or prefer native apps
- Install MariaDB 10+
- create database `origin`
- create user `origin`
- set password `origin` for this user
- these credentials are pre-configured into default `server.conf`)
- **IMPORTANT!** check `port` for database into `server.conf`, by default it is set to 3406, but MariaDB use 3306

You can use this SQL:
```mysql
CREATE DATABASE origin;
CREATE USER 'origin'@'localhost' IDENTIFIED BY 'origin';
GRANT ALL PRIVILEGES ON origin.* TO origin@localhost;
```
See [server.conf](/backend/config/server.conf)

### Also, you can install phpMyAdmin for easy admin the database
```shell
docker run --name pma-origin -d -e PMA_USER=origin -e PMA_PASSWORD=origin --link mariadb-origin:db -p 8030:80 phpmyadmin
```
phpMyAdmin will start at 8030 port on localhost
  
### Generate map
```shell
gradle mapgen
``` 
This will generate image file `map.png` into **backend** dir

By default this command will generate 1 supergrid, please see [MapGenerator.kt](/src/com/origin/util/MapGenerator.kt) file for details

### Import generated map into database
```shell
gradle mapimport
``` 
This will import image file `map.png` (placed into root `backend` dir) into databse with previously generated map by `mapgen` command

### Start the server
```shell
gradle run
```
After start server will be listened 8020 port for incoming connections

### Build the server
```shell
./gradlew clean build
docker build --rm -t origin:latest .
```

### Run docker container (example)
```shell
docker-compose -f docker-compose.prod.yaml up -d
```

# Configure
config file `./config/server.conf` with HOCON format

# Help

Please fork and PR to improve the code.
