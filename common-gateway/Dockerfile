FROM azul/zulu-openjdk:8u332-8.62.0.19
COPY *.jar /common-gateway.jar
#CMD ["--server.port=8081"]
EXPOSE 8089
ENTRYPOINT ["java","-jar","/common-gateway.jar"]