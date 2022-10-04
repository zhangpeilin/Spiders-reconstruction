FROM azul/zulu-openjdk:8u332-8.62.0.19 as dao
COPY *dao*.jar /common-dao-center.jar
EXPOSE 8087
ENTRYPOINT ["java","-jar","/common-dao-center.jar"]

FROM azul/zulu-openjdk:8u332-8.62.0.19 as config
COPY *config*.jar /common-config-center.jar
EXPOSE 3344
ENTRYPOINT ["java","-jar","/common-config-center.jar"]

FROM azul/zulu-openjdk:8u332-8.62.0.19 as eureka
COPY *eureka*.jar /common-app-eureka.jar
EXPOSE 7001
ENTRYPOINT ["java","-jar","/common-app-eureka.jar"]

FROM azul/zulu-openjdk:8u332-8.62.0.19 as gateway
COPY *gateway*.jar /common-gateway.jar
EXPOSE 8089
ENTRYPOINT ["java","-jar","/common-gateway.jar"]