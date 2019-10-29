FROM jeanblanchard/java:8
MAINTAINER MHS, mhs@into.software
COPY @project.artifactId@.jar /app/balena-@project.artifactId@.jar
#COPY debug.jar /app/balena-@project.artifactId@.jar
COPY balena-drones.properties /app
COPY logback.xml /app
WORKDIR /app
ENTRYPOINT ["java","-jar","/app/balena-@project.artifactId@.jar"]
EXPOSE 8080
