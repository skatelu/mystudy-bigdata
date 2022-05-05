# Docker 安装Kafka集群

## 配置机器sudo免密执行

```shell
vim /etc/sudoers  
# 修改配置
root    ALL=(ALL)       ALL  
# 源文件中就有
yunweijia ALL=(ALL) NOPASSWD:ALL  
# 新增用户
yunweijia%yunweijia ALL=(ALL) NOPASSWD:ALL  
# 新增组yunweijia
```

## 下载Kafka镜像文件

* 我这边用的是  **wurstmeister/kafka:2.12-2.5.1**

  ```shell
  [root@localhost-test1 ~]# docker pull wurstmeister/kafka:2.12-2.5.1
  ```




## 创建docker - kafka容器数据卷

* 分别在三台服务器上执行一下命令

* 创建三个文件夹，分别用来做 kafka容器的映射，将配置信息、data数据、log信息映射到宿主机文件夹，用来做持久化备份使用

  ```shell
  [root@dolphinscheduler02 ~]# mkdir -p /opt/kafka_cluster/config
  [root@dolphinscheduler02 ~]# mkdir -p /opt/kafka_cluster/data
  [root@dolphinscheduler02 ~]# mkdir -p /opt/kafka_cluster/logs
  ```

* 创建完成后的目录信息

  ```shell
  [root@dolphinscheduler02 ~]# find /opt/kafka_cluster/
  /opt/kafka_cluster/
  /opt/kafka_cluster/config
  /opt/kafka_cluster/data
  /opt/kafka_cluster/logs
  ```

## 搭建 Kafka 集群

* 此处用的kafka版本是  **wurstmeister/kafka:2.12-2.5.1**

### 运行镜像文件命令

* 服务器1

  ```shell
  docker run --name kafka --network host -d \
  -v /etc/hosts:/etc/hosts \
  -v /opt/kafka_cluster/data:/kafka \
  -v /opt/kafka_cluster/config:/opt/kafka/config \
  -v /opt/kafka_cluster/logs:/opt/kafka/logs \
  -e JMX_PORT=38006 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=192.168.60.100:38001,192.168.60.102:38001,192.168.60.103:38001 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://192.168.60.100:38005 \
  -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:38005 \
  wurstmeister/kafka:2.12-2.5.1
  ```

* 服务器2

  ```shell
  docker run --name kafka --network host -d \
  -v /etc/hosts:/etc/hosts \
  -v /opt/kafka_cluster/data:/kafka \
  -v /opt/kafka_cluster/config:/opt/kafka/config \
  -v /opt/kafka_cluster/logs:/opt/kafka/logs \
  -e JMX_PORT=38006 \
  -e KAFKA_BROKER_ID=2 \
  -e KAFKA_ZOOKEEPER_CONNECT=192.168.60.100:38001,192.168.60.102:38001,192.168.60.103:38001 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://192.168.60.102:38005 \
  -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:38005 \
  wurstmeister/kafka:2.12-2.5.1
  ```

* 服务器3

  ```shell
  docker run --name kafka --network host -d \
  -v /etc/hosts:/etc/hosts \
  -v /opt/kafka_cluster/data:/kafka \
  -v /opt/kafka_cluster/config:/opt/kafka/config \
  -v /opt/kafka_cluster/logs:/opt/kafka/logs \
  -e JMX_PORT=38006 \
  -e KAFKA_BROKER_ID=3 \
  -e KAFKA_ZOOKEEPER_CONNECT=192.168.60.100:38001,192.168.60.102:38001,192.168.60.103:38001 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://192.168.60.103:38005 \
  -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:38005 \
  wurstmeister/kafka:2.12-2.5.1
  ```

### 查看kafka是否启动

```shell
[root@dolphinscheduler01 ~]# docker ps
CONTAINER ID   IMAGE                           COMMAND                  CREATED              STATUS              PORTS                               NAMES
509fbb02defd   wurstmeister/kafka:2.12-2.5.1   "start-kafka.sh"         About a minute ago   Up About a minute                                       kafka
a2c3d09c6a26   zookeeper:3.4.14                "/docker-entrypoint.…"   6 hours ago          Up 2 minutes                                            zookeeper
ea60f7d81f6d   mysql:5.7                       "docker-entrypoint.s…"   2 days ago           Up 2 minutes        33060/tcp, 0.0.0.0:3307->3306/tcp   mysql
[root@dolphinscheduler01 ~]#
```



### 修改kafka配置文件

* 在 /opt/kafka_cluster/config 目录下，修改 server.properties

  ```properties
  # The id of the broker. This must be set to a unique integer for each broker. 根据自己的机器进行配置
  broker.id=1
  port=9092
  # Switch to enable topic deletion or not, default value is false
  #delete.topic.enable=true
  ############################# Socket Server Settings #############################
  # The address the socket server listens on. It will get the value returned from 
  # java.net.InetAddress.getCanonicalHostName() if not configured.
  #   FORMAT:
  #     listeners = listener_name://host_name:port
  #   EXAMPLE:
  #     listeners = PLAINTEXT://your.host.name:9092
  listeners=PLAINTEXT://0.0.0.0:38005
  # Hostname and port the broker will advertise to producers and consumers. If not set, 
  # it uses the value for "listeners" if configured.  Otherwise, it will use the value
  # returned from java.net.InetAddress.getCanonicalHostName().
  advertised.listeners=PLAINTEXT://192.168.60.100:38005
  # Maps listener names to security protocols, the default is for them to be the same. See the config documentation for more details
  #listener.security.protocol.map=PLAINTEXT:PLAINTEXT,SSL:SSL,SASL_PLAINTEXT:SASL_PLAINTEXT,SASL_SSL:SASL_SSL
  # The number of threads handling network requests（最好设置为cpu核数+1）
  num.network.threads=4
  # The number of threads doing disk I/O（最好设置为cpu核数的2倍值）
  num.io.threads=8
  # The send buffer (SO_SNDBUF) used by the socket server
  socket.send.buffer.bytes=102400
  # The receive buffer (SO_RCVBUF) used by the socket server
  socket.receive.buffer.bytes=102400
  # The maximum size of a request that the socket server will accept (protection against OOM)
  socket.request.max.bytes=104857600
  ############################# Log Basics #############################
  # A comma seperated list of directories under which to store log files 根据原有的配置文件的路径进行修改
  log.dirs=/kafka/kafka-logs-dolphinscheduler01
  # The default number of log partitions per topic. More partitions allow greater
  # parallelism for consumption, but this will also result in more files across
  # the brokers.
  num.partitions=1
  # The number of threads per data directory to be used for log recovery at startup and flushing at shutdown.
  # This value is recommended to be increased for installations with data dirs located in RAID array.
  num.recovery.threads.per.data.dir=1
  ############################# Log Flush Policy #############################
  # Messages are immediately written to the filesystem but by default we only fsync() to sync
  # the OS cache lazily. The following configurations control the flush of data to disk.
  # There are a few important trade-offs here:
  #    1. Durability: Unflushed data may be lost if you are not using replication.
  #    2. Latency: Very large flush intervals may lead to latency spikes when the flush does occur as there will be a lot of data to flush.
  #    3. Throughput: The flush is generally the most expensive operation, and a small flush interval may lead to exceessive seeks.
  # The settings below allow one to configure the flush policy to flush data after a period of time or
  # every N messages (or both). This can be done globally and overridden on a per-topic basis.
  # The number of messages to accept before forcing a flush of data to disk
  #log.flush.interval.messages=10000
  # The maximum amount of time a message can sit in a log before we force a flush
  #log.flush.interval.ms=1000
  ############################# Log Retention Policy #############################
  # The following configurations control the disposal of log segments. The policy can
  # be set to delete segments after a period of time, or after a given size has accumulated.
  # A segment will be deleted whenever *either* of these criteria are met. Deletion always happens
  # from the end of the log.
  # The minimum age of a log file to be eligible for deletion due to age
  log.retention.hours=168
  # A size-based retention policy for logs. Segments are pruned from the log as long as the remaining
  # segments don't drop below log.retention.bytes. Functions independently of log.retention.hours.
  #log.retention.bytes=1073741824
  # The maximum size of a log segment file. When this size is reached a new log segment will be created.
  log.segment.bytes=1073741824
  # The interval at which log segments are checked to see if they can be deleted according
  # to the retention policies
  log.retention.check.interval.ms=300000
  ############################# Zookeeper #############################
  # Zookeeper connection string (see zookeeper docs for details).
  # This is a comma separated host:port pairs, each corresponding to a zk
  # server. e.g. "127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002".
  # You can also append an optional chroot string to the urls to specify the
  # root directory for all kafka znodes.
  zookeeper.connect=192.168.60.100:38001,192.168.60.102:38001,192.168.60.103:38001
  # Timeout in ms for connecting to zookeeper
  zookeeper.connection.timeout.ms=30000
  ```

* 在各个机器上修改完成后，进行重新启动

  ```shell
  docker retsrt kafka
  ```

* 查看docker 中kafka是否启动

  ```shell
  [root@dolphinscheduler01 config]# docker ps
  CONTAINER ID   IMAGE                           COMMAND                  CREATED          STATUS          PORTS                               NAMES
  509fbb02defd   wurstmeister/kafka:2.12-2.5.1   "start-kafka.sh"         13 minutes ago   Up 2 minutes                                        kafka
  a2c3d09c6a26   zookeeper:3.4.14                "/docker-entrypoint.…"   6 hours ago      Up 14 minutes                                       zookeeper
  ea60f7d81f6d   mysql:5.7                       "docker-entrypoint.s…"   2 days ago       Up 14 minutes   33060/tcp, 0.0.0.0:3307->3306/tcp   mysql
  [root@dolphinscheduler01 config]# 
  
  ```



## 校验kafka集群是否正确











