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

Build:
> gradle clean build

Start the server:
> gradle run

# Help

Please fork and PR to improve the code.
