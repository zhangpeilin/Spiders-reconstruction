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

echo "进入gateway子目录开始构建镜像"
cd $JENKINS_HOME/$GATEWAY/target

echo "将Dockerfile文件复制到打包处"
cp ../Dockerfile ./

# 修改文件权限
echo "修改jar包权限"
chmod 755 common-gateway-1.0-SNAPSHOT.jar

echo "看看docker能不能用"
docker -v

echo "停止容器"
# 停止容器
docker stop $GATEWAY

echo "删除容器"
# 删除容器
docker rm $GATEWAY
echo "删除镜像"
# 删除镜像
docker rmi $GATEWAY
echo "打包镜像"
# 打包镜像
docker build -t $GATEWAY .
echo "运行镜像"
# 运行镜像
docker run -d -p 8089:8089 --name $GATEWAY $GATEWAY