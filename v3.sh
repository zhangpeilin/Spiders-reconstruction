#!/bin/bash
# 服务名称
# 源jar名称，mvn打包之后，target目录下的jar包名称
# shellcheck disable=SC2154
MODULE=$param
# jenkins克隆git工程目录
JENKINS_HOME=$WORKSPACE
BASE_PROJECT=/var/jenkins_home/workspace/Spiders-reconstruction
SSH=/var/jenkins_home/.ssh

# 等待三秒
echo sleep 3s
sleep 1
echo sleep 2s
sleep 1
echo sleep 1s
sleep 1
echo "将jar包复制到target文件夹中"
mkdir "$JENKINS_HOME"/target
# shellcheck disable=SC2035
# shellcheck disable=SC2061
find $BASE_PROJECT -name *.jar -exec cp {}  "$JENKINS_HOME"/target \;
# shellcheck disable=SC2061
find "$JENKINS_HOME" -name *.jar -exec cp {}  "$JENKINS_HOME"/target \;
echo "进入target子目录开始构建镜像"
# shellcheck disable=SC2164
cd $JENKINS_HOME/target
echo "将Dockerfile文件复制到打包处"
cp $JENKINS_HOME/Dockerfile ./
cp $SSH -r .

# 修改文件权限
echo "修改jar包权限"
# shellcheck disable=SC2035
chmod 755 *.jar

echo "看看docker能不能用"
docker -v

echo "停止容器"
# 停止容器
docker stop "$MODULE"

echo "删除容器"
# 删除容器
docker rm "$MODULE"
echo "删除镜像"
# 删除镜像
docker rmi "$MODULE"
echo "打包镜像"
# 打包镜像
docker build -t "$MODULE" --target "$MODULE" .
echo "创建网桥"
docker network create --subnet=172.18.0.0/16 mynetwork

case $MODULE in
common-gateway) echo "启动网关容器" && docker run -d -p 8089:8089 --net mynetwork --ip 172.18.0.6 --name common-gateway common-gateway
  ;;
common-config-center) echo "启动配置中心" && docker run -d -p 3344:3344 --net mynetwork --name common-config-center common-config-center
  ;;
common-dao-center) echo "启动数据库服务" && docker run -d -p 8087:8087 --net mynetwork --name common-dao-center common-dao-center
  ;;
common-app-eureka) echo "启动注册中心" && docker run -d -p 7001:7001 --net mynetwork --ip 172.18.0.4 --name common-app-eureka common-app-eureka
esac