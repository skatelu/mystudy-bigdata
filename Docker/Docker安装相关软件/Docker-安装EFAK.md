# Docker 安装EFAK

## 编译源码

### 下载源码

```shell
git@github.com:smartloli/EFAK.git
```

### 进行编译

```shell
mvn clean package -DskipTests
```

* 会在 EFAK\efak-web\target\efak-web-2.1.0-bin.tar.gz 生成该tar包

### 源码修改

* 需要在pom文件中加上

  ```xml
          <maven.compiler.target>1.8</maven.compiler.target>
          <maven.compiler.source>1.8</maven.compiler.source>
  ```

* 目的是为了指定 jdk版本为 1.8  不然构建的时候，会试用默认的 jdk1.5 导致构建失败



## 下载相关docker镜像构建文本

```http
git@github.com:nick-zh/docker-kafka-eagle.git
```

* 获取 EFAK kafka监控系统的 Docker镜像创建文件

## 修改 DockerFile 封装镜像文件

### Dcokfile

```shell
FROM openjdk:8-bullseye

ENV KE_HOME=/opt/efak
ENV EFAK_VERSION=2.1.0
# Set config defaults
ENV EFAK_CLUSTER_JMX_ACL=false
ENV EFAK_CLUSTER_JMX_USER=keadmin
ENV EFAK_CLUSTER_JMX_PASSWORD=keadmin123
ENV EFAK_CLUSTER_JMX_SSL=false
ENV EFAK_CLUSTER_JMX_TRUSTSTORE_LOCATION='/Users/dengjie/workspace/ssl/certificates/kafka.truststore'
ENV EFAK_CLUSTER_JMX_TRUSTSTORE_PASSWORD=ke123456
ENV EFAK_CLUSTER_JMX_URI='service:jmx:rmi:///jndi/rmi://%s/jmxrmi'
ENV EFAK_CLUSTER_KAFKA_EAGLE_BROKER_SIZE=1
ENV EFAK_CLUSTER_KAFKA_EAGLE_OFFSET_STORAGE=kafka
ENV EFAK_CLUSTER_KAFKA_EAGLE_SASL_ENABLE=false
ENV EFAK_CLUSTER_KAFKA_EAGLE_SASL_PROTOCOL=SASL_PLAINTEXT
ENV EFAK_CLUSTER_KAFKA_EAGLE_SASL_MECHANISM=SCRAM-SHA-256
ENV EFAK_CLUSTER_KAFKA_EAGLE_SASL_JAAS_CONFIG='org.apache.kafka.common.security.scram.ScramLoginModule required username="admin" password="admin-secret";'
ENV EFAK_CLUSTER_KAFKA_EAGLE_SASL_CGROUP_ENABLE=false
ENV EFAK_CLUSTER_KAFKA_EAGLE_SASL_CGROUP_TOPICS=kafka_ads01,kafka_ads02
ENV EFAK_CLUSTER_ZK_LIST=zookeeper:2181
ENV EFAK_DB_DRIVER=org.sqlite.JDBC
ENV EFAK_DB_USERNAME=root
ENV EFAK_DB_PASSWORD=smartloli
ENV EFAK_DB_URL=jdbc:sqlite:/hadoop/efak/db/ke.db
ENV EFAK_KAFKA_CLUSTER_ALIAS='cluster'
ENV EFAK_KAFKA_ZK_LIMIT_SIZE=25
ENV EFAK_METRICS_CHARTS=false
ENV EFAK_METRICS_RETAIN=30
ENV EFAK_SQL_DISTRIBUTED_MODE_ENABLE=FALSE
ENV EFAK_SQL_FIX_ERROR=false
ENV EFAK_SQL_TOPIC_PREVIEW_RECORDS_MAX=10
ENV EFAK_SQL_TOPIC_RECORDS_MAX=5000
ENV EFAK_SQL_WORKNODE_PORT=8787
ENV EFAK_SQL_WORKNODE_RPC_TIMEOUT=300000
ENV EFAK_SQL_WORKNODE_SERVER_PATH='/Users/dengjie/workspace/kafka-eagle-plus/kafka-eagle-common/src/main/resources/works'
ENV EFAK_SQL_WORKNODE_FETCH_THRESHOLD=5000
ENV EFAK_SQL_WORKNODE_FETCH_TIMEOUT=20000
ENV EFAK_TOPIC_TOKEN=keadmin
ENV EFAK_WEBUI_PORT=8048
ENV EFAK_ZK_ACL_ENABLE=false
ENV EFAK_ZK_ACL_SCHEMA=digest
ENV EFAK_ZK_ACL_USERNAME=test
ENV EFAK_ZK_ACL_PASSWORD=test123
ENV EFAK_ZK_CLUSTER_ALIAS='cluster'

# 1. install command/library/software
# If install slowly, you can replcae debian's mirror with new mirror, Example:
# RUN { \
#   echo "deb http://mirrors.tuna.tsinghua.edu.cn/debian/ buster main contrib non-free"; \
#   echo "deb http://mirrors.tuna.tsinghua.edu.cn/debian/ buster-updates main contrib non-free"; \
#   echo "deb http://mirrors.tuna.tsinghua.edu.cn/debian/ buster-backports main contrib non-free"; \
#   echo "deb http://mirrors.tuna.tsinghua.edu.cn/debian-security buster/updates main contrib non-free"; \
# } > /etc/apt/sources.list

ADD system-config.properties /tmp
ADD entrypoint.sh /usr/bin

#RUN apk --update add wget gettext tar bash sqlite
RUN apt-get update && apt-get upgrade -y && apt-get install -y sqlite3 gettext dos2unix

ADD ./efak-web-2.1.0-bin.tar.gz /opt 
RUN mv /opt/efak-web-2.1.0 /opt/efak
#get and unpack kafka eagle
RUN mkdir -p /opt/efak/conf
RUN cd /opt && ls efak/
RUN chmod +x /opt/efak/bin/ke.sh

RUN dos2unix /opt/efak/bin/*.sh && \
    dos2unix /usr/bin/*.sh && \
    dos2unix /opt/efak/kms/bin/*.sh

EXPOSE 8048 8080

ENTRYPOINT ["entrypoint.sh"]

WORKDIR /opt/efak
```

## 加载相关文件

### 文件传输

* 将相关文件加载到安装过 docker 19.03 版本以上的服务器目录中

  ```shell
  [admin@iZp1e00oc5m4p9tktct91bZ ~]$ cd /dmp/docker_file/
  [admin@iZp1e00oc5m4p9tktct91bZ docker_file]$ cd kafka-efak/
  [admin@iZp1e00oc5m4p9tktct91bZ kafka-efak]$ ll
  total 81572
  -rwxrwxr-x 1 admin admin     3136 Jun  7 17:20 Dockerfile
  -rwxrwxr-x 1 admin admin 83509617 Jun  7 16:43 efak-web-2.1.0-bin.tar.gz
  -rwxrwxr-x 1 admin admin      336 Jun  7 16:47 entrypoint.sh
  -rwxrwxr-x 1 admin admin     4248 Jun  7 16:43 system-config.properties
  [admin@iZp1e00oc5m4p9tktct91bZ kafka-efak]$ 
  
  ```

## 进行镜像封装

### 封装镜像

```shell
sudo docker build -t zhzt/efak:2.1.0 .
```

* 运行完成后，会在镜像仓库中出现该镜像

  ```shell
  [admin@iZp1e00oc5m4p9tktct91bZ kafka-efak]$ sudo docker images
  [sudo] password for admin: 
  REPOSITORY               TAG                 IMAGE ID            CREATED             SIZE
  zhzt/efak                2.1.0               137dceec9d9d        17 minutes ago      762MB
  [admin@iZp1e00oc5m4p9tktct91bZ kafka-efak]$ 
  
  ```

  

## 生成容器实例

* 运行该镜像文件

  ```shell
  sudo docker run --name kafka-eagle --network host -d \
  -v /etc/localtime:/etc/localtime:ro \
  -v /opt/kafka-eagle/logs/:/opt/efak/kms/logs \
  -v /opt/kafka-eagle/conf/:/opt/efak/conf \
  -e EFAK_CLUSTER_ZK_LIST=192.168.60.100:38001,192.168.60.102:38001,192.168.60.103:38001 \
  zhzt/efak:2.1.0
  ```

* 挂在到相关目录下，就可以更改配置文件信息等



### 查看容器日志

* 若docker启动时发生错误，可查看该容器日志内容，进行调整

```shell
docker inspect --format '{{.LogPath}}' 60f486ec7c33
```





## 安装EAFK

## 下载相关文件

```http
http://download.kafka-eagle.org/
```

