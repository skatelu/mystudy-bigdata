# Docker 安装集群版Flink·

## 1、docker 安装单机版的Flink

### 执行创建命令(测试版看是否正常运行)

* 创建flink-JobManager容器

  ```shell
  docker run \
      --rm \
      --name=flink-jobmanager \
      --network=host \
      --privileged=true \
      -p 8081:8081 \
      --env FLINK_PROPERTIES="jobmanager.rpc.address: docker10" \
      flink:1.13-scala_2.12-java11 jobmanager
  ```
  
* 创建 taskManager 容器

  ```shell
  docker run \
      --rm \
      --name=flink-taskmanager \
      --network=host \
      --privileged=true \
      --env FLINK_PROPERTIES="jobmanager.rpc.address: docker10" \
      flink:1.13-scala_2.12-java11 taskmanager
  ```


### 在后台运行flink

* 创建flink-JobManager容器

  ```shell
  docker run \
      -d \
      --name=flink-jobmanager \
      --network=host \
      --privileged=true \
      -v /opt/flink/conf:/opt/flink/conf \
      -v /opt/flink/log:/opt/flink/log \
      --env FLINK_PROPERTIES="jobmanager.rpc.address: docker10" \
      flink:1.13-scala_2.12-java11 jobmanager
  ```

* 创建 flink-taskManager 容器

  ```shell
  docker run -d \
      --name=flink-taskmanager \
      --network=host \
      --privileged=true \
      -v /opt/flink/conf:/opt/flink/conf \
      -v /opt/flink/log:/opt/flink/log \
      --env FLINK_PROPERTIES="jobmanager.rpc.address: docker10" \
      flink:1.13-scala_2.12-java11 taskmanager
  ```

  



