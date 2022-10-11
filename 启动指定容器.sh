case $MODULE in
common-gateway) echo "启动网关容器" && docker restart common-gateway
  ;;
common-config-center) echo "启动配置中心" && docker restart common-config-center common-config-center
  ;;
common-dao-center) echo "启动数据库服务" && docker restart common-dao-center
  ;;
common-app-eureka) echo "启动注册中心" && docker restart common-app-eureka
esac