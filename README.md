# SPIDER 3.0

## 一、 使用文档

1. 根据数据库建表语句创建数据库：https://github.com/zhangpeilin/Spiders-reconstruction/blob/master/scripts/database.sql
2. 当前模块已实现：哔哩哔哩视频下载、专栏图片保存、表情包批量存储；各常见漫画平台的批量下载；QQ空间图片原图批量下载；eh漫画下载；bika漫画搜索、下载和归档分类查询；图片鉴黄；相似图检测；相似漫画压缩包检测去重。

## 二、 快速开始
### 2.1 安装插件到本地
    下载并编译工程，在Spiders-reconstruction工程中执行mvn-install，安装插件到本地maven仓库。
### 2.2 依赖环境
    安装JDK1.8，Maven3.5.2，Docker1.13.1，MySQL8.0.13，并执行数据库脚本。
    配置数据库信息到需要启动模块中，或配置到git配置中心。
### 2.3 运行模块
    spider-on-xxx工程均可独立启动，依赖其他common模块。
### 2.4 gitconfig配置
    所有spider-on-xxx均支持gitconfig配置中心存放配置文件，配置文件名与domainKey一致。/src/main/resources/application.yml中为模版配置，可以迁移到服务端使用。/src/main/resources/bootstrap.yml为启用git配置中心时配置模版。

## 三 可扩展百度、腾讯、华为AI开放平台接口
    需要自行注册相关平台服务账号，并配置相关参数
### 1.百度：https://ai.baidu.com/
    baiduAI开放平台，支持图片识别、相似图片检索，人脸识别、人脸对比等
### 2.华为：https://console.huaweicloud.com/devcloud/
    支持华为CodeArts 流水线，支持代码构建、代码部署、代码发布等，支持华为云托管机器、自有虚拟机托管等。
### 3.阿里云：https://console.cloud.aliyun.com/
    支持阿里云OSS，短信发送服务，数据库存储等

## 三、 扩展机制

1. v1.sh,v2.sh,v3.sh脚本为Jenkins打包工具使用脚本，可根据需要配置集成到Jenkins服务中，实现自动化打包部署镜像。
2. Dockerfile文件为打包镜像使用文件，可参考：https://docs.docker.com/engine/reference/builder/

## 四. 环境配置

1. 本工程基于SpringBoot2.2.2.RELEASE,jdk1.8.0_202安装编译调试通过，如需正常使用请确保基础环境满足要求。

## 五. 开发注意事项

**请遵守如下规范：**

### 5.1 命名规范：
    新增模块请按照可独立启动模块的方式，在spider-on-xxx工程中实现，并添加到pom.xml中。
### 5.2 代码规范：
    公共方法请添加到common-util工程中。
### 5.3 配置规范：
    如需使用数据库服务，请引用spider-dao组件，请勿自行实现。
## 六. 联系方式
    如有疑问和咨询请发邮件：512239520@163.com
