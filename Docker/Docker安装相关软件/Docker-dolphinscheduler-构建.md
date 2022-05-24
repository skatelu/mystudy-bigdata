# DockerFile 构建 Dolphinscheduler镜像

```shell
docker build --build-arg VERSION=2.0.6-SNAPSHOT -t yjl/dolphinscheduler:test1 .
```



## 运行一个Dolphinscheduler实例

```shell
docker run -d --name dolphinscheduler \
-e DATABASE_HOST="192.168.66.10" -e DATABASE_PORT="3306" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="root" -e DATABASE_PASSWORD="123456" \
-e ZOOKEEPER_QUORUM="192.168.66.10:38001,192.168.66.11:38001,192.168.66.12:38001" \
-p 12345:12345 \
apache/dolphinscheduler:2.0.5 all
```

