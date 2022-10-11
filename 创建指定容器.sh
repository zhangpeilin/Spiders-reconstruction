case $MODULE in
common-gateway) echo "启动网关容器" ;  docker run -d -p 8089:8089 --net mynetwork --ip 172.18.0.6 --name common-gateway common-gateway
  ;;
common-config-center) echo "启动配置中心" ; docker run -d -p 3344:3344 --net mynetwork --name common-config-center common-config-center
  ;;
common-dao-center) echo "启动数据库服务" ; docker run -d -p 8087:8087 --net mynetwork --name common-dao-center common-dao-center
  ;;
common-app-eureka) echo "启动注册中心" ; docker run -d -p 7001:7001 --net mynetwork --ip 172.18.0.4 --name common-app-eureka common-app-eureka

esac