FROM azul/zulu-openjdk-alpine:17-jre

WORKDIR /opt/app

COPY build/libs/backend.jar backend.jar

EXPOSE 8010

CMD ["java", "-jar", "backend.jar"]
