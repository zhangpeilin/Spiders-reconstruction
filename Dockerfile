FROM azul/zulu-openjdk:8u332-8.62.0.19 as common-gateway
COPY *gateway*.jar /common-gateway.jar
EXPOSE 8089
ENTRYPOINT ["java","-jar","/common-gateway.jar"]

FROM azul/zulu-openjdk:8u332-8.62.0.19 as common-app-eureka
COPY *eureka*.jar /common-app-eureka.jar
EXPOSE 7001
ENTRYPOINT ["java","-jar","/common-app-eureka.jar"]

FROM azul/zulu-openjdk:8u332-8.62.0.19 as common-config-center
COPY *config*.jar /common-config-center.jar
COPY .ssh /root/.ssh/
EXPOSE 3344
ENTRYPOINT ["java","-jar","/common-config-center.jar"]

#FROM azul/zulu-openjdk:8u332-8.62.0.19 as common-dao-center
#COPY *dao*.jar /common-dao-center.jar
#EXPOSE 8087
#ENTRYPOINT ["java","-jar","/common-dao-center.jar"]

FROM azul/zulu-openjdk:8u332-8.62.0.19 as spider-unit
COPY *unit*.jar /spider-unit.jar
COPY .ssh /root/.ssh/
EXPOSE 2233
ENTRYPOINT ["java","-jar","/spider-unit.jar"]

FROM azul/zulu-openjdk:8u332-8.62.0.19 as spider-on-bika
COPY *bika*.jar /spider-on-bika.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/spider-on-bika.jar"]