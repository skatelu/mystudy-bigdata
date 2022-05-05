# Docker 安装Nacos

## 从镜像仓库中下载 Nacos镜像

```shell
docker pull nacos/nacos-server:v2.0.4
```



## Docker 安装单机版nacos

### 不需要链接数据库的版本

```shell
docker run -d -p 38310:8848 -p 38311:9848 -p 38312:9849 \
--restart=always \
--privileged=true \
-e MODE=standalone \
-e JVM_XMS=600m \
-e JVM_XMX=600m \
-e JVM_XMN=350m \
--name nacos_zhzt nacos/nacos-server:v2.0.4
```

* 如果需要添加配置文件跟log日志的容器卷的话，需要添加下面两行

```shell
-v /opt/nacos/conf:/home/nacos/conf \
-v /opt/nacos/logs:/home/nacos/logs \
```



### 需要链接数据库的版本

```shell
docker run -d -p 38220:8848 -p 38221:9848 -p 38222:9849 \
--restart=always \
--privileged=true \
-e MODE=standalone \
-e MYSQL_SERVICE_PORT=3306 \
-e MYSQL_SERVICE_USER=root \
-e MYSQL_SERVICE_DB_NAME=nacos \
-e MYSQL_SERVICE_PASSWORD=123456 \
-e SPRING_DATASOURCE_PLATFORM=mysql \
-e MYSQL_SERVICE_HOST=192.168.66.12 \
--name nacos_zhzt nacos/nacos-server:v2.0.4
```



# Docker 安装集群版

