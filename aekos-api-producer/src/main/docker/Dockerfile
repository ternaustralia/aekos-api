FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD aekos-api-producer-*-SNAPSHOT.jar app.jar
ADD aekos-api-producer-docker.properties /root/aekos-api.properties
RUN sh -c 'touch /app.jar'
ENTRYPOINT [\
  "java",\
  "-Djava.security.egd=file:/dev/./urandom",\
  "-Dlogging.config=classpath:logback-spring-prod.xml",\
  "-jar","/app.jar"]
