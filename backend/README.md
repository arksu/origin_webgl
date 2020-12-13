# Backend (game server)
for Origin MMO game

# How it works

- [Kotlin](https://github.com/JetBrains/kotlin) as programming language
- [MariaDB](https://github.com/mariadb) database
- [HikariCP](https://github.com/brettwooldridge/HikariCP) as JDBC connection pool
- [Exposed](https://github.com/JetBrains/Exposed/) as Kotlin ORM Framework
- [Gson](https://github.com/google/gson) for handling JSON
- [Ktor](https://github.com/ktorio/ktor) as web framework

# Getting started

- install JVM 8+
- install MariaDB database for your platform
- start MariaDB server
- create a database into your MariaDB server named 'origin' 
- by default server configured for db user 'root' and password '1', you can change this in 'server.conf' file
- game server is configured to start on [8020](http://localhost:8020) port.

###Build
```shell
gradle clean build
```

###Generate map
```shell
gradle mapgen
``` 
This will generate image file `map.png` into **backend** dir
By default generate 1 supergrid, please see [MapGenerator.kt](/src/com/origin/utils/MapGenerator.kt) file

###Import generated map into database
```shell
gradle mapimport
``` 
This will import image file `map.png` into databse with previously generated map by `mapgen` command

###Start the server
```shell
gradle run
```
# Help

Please fork and PR to improve the code.
