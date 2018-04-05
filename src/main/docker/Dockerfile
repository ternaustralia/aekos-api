FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD aekos-api-1.0-SNAPSHOT.jar app.jar
ADD aekos-api-production.properties /root/aekos-api.properties
RUN sh -c 'touch /app.jar'
EXPOSE 80
ENTRYPOINT [\
  "java",\
  "-Djava.security.egd=file:/dev/./urandom",\
  "-Dlogging.config=classpath:logback-spring-prod.xml",\
  "-Dserver.port=80",\
  "-jar","/app.jar"]
