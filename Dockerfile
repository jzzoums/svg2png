FROM openjdk:8-jre-alpine

ENV APP_HOME /home
WORKDIR $APP_HOME

COPY target/svgtopng-0.0.1-SNAPSHOT.jar $APP_HOME

CMD ["java", "-jar", "svgtopng-0.0.1-SNAPSHOT.jar"]
