#!/bin/bash
# 服务名称

# 源jar名称，mvn打包之后，target目录下的jar包名称
EUREKA=common-app-eureka
DAO=common-dao-center
GATEWAY=common-gateway
CONFIG=common-config-center
# jenkins克隆git工程目录
JENKINS_HOME=/var/jenkins_home/workspace/Spiders-reconstruction

# 等待三秒
echo sleep 3s
sleep 1
echo sleep 2s
sleep 1
echo sleep 1s
sleep 1
echo "将jar包复制到target文件夹中"
mkdir $JENKINS_HOME/target
# shellcheck disable=SC2035
# shellcheck disable=SC2061
find $JENKINS_HOME -name *.jar -exec cp {}  $JENKINS_HOME/target \;
echo "进入target子目录开始构建镜像"
# shellcheck disable=SC2164
cd $JENKINS_HOME/target
echo "将Dockerfile文件复制到打包处"
cp $JENKINS_HOME/Dockerfile ./

# 修改文件权限
echo "修改jar包权限"
# shellcheck disable=SC2035
chmod 755 *.jar

echo "看看docker能不能用"
docker -v

echo "停止容器"
# 停止容器
docker stop $GATEWAY $EUREKA $DAO $CONFIG

echo "删除容器"
# 删除容器
docker rm $GATEWAY $EUREKA $DAO $CONFIG
echo "删除镜像"
# 删除镜像
docker rmi $GATEWAY $EUREKA $DAO $CONFIG
echo "打包镜像"
# 打包镜像
docker build -t $GATEWAY --target $GATEWAY .
#echo "运行镜像"
# 运行镜像
#docker run -d -p 8089:8089 --name $GATEWAY $GATEWAY