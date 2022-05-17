# Docker 安装单机版 zookeeper

## Docker安装zookeeper

### 创建相关数据卷

```shell
[root@dolphinscheduler03 ~]# mkdir -p /opt/zookeeper-standalone/data
[root@dolphinscheduler03 ~]# mkdir -p /opt/zookeeper-standalone/conf
[root@dolphinscheduler03 ~]# mkdir -p /opt/zookeeper-standalone/datalog
```



### 创建Docker实例

```shell
docker run -d -p 2181:2181 --name=zookeeper-standalone \
-v /opt/zookeeper-standalone/conf:/conf \
-v /opt/zookeeper-standalone/data:/data \
-v /opt/zookeeper-standalone/datalog:/datalog \
zookeeper:3.4.14
```



### 想要跟随docker一样开机运行

docker update --restart=always











# Docker 安装zookeeper 集群

## 注意，目前使用的Docker 的Host模式安装，后续会完善K8s安装

## centos7 安装docker环境

* 详情见 dockers安装文档

## 下载Zookeeper 镜像文件

* 此处 我是用的是 zookeeper 3.4.13 官方镜像

  ```shell
  [root@localhost-test1 ~]# docker pull zookeeper:3.4.13
  ```


## 创建 zookeeper 容器数据卷

* **用于映射zookeeper 节点数据、配置信息、log日志文件**，持久化数据与配置信息

  ```shell
  [root@dolphinscheduler03 ~]# mkdir -p /opt/zookeeper/data
  [root@dolphinscheduler03 ~]# mkdir -p /opt/zookeeper/conf
[root@dolphinscheduler03 ~]# mkdir -p /opt/zookeeper/datalog
  ```
  
* 创建完成后，目录结构如下

  ```shell
  [root@localhost-test1 zookeeper]# find .
  .
  ./conf
  ./data
  ./datalog
  ```

## 搭建zookeeper集群

### 在data文件夹中创建 myid文件

* 在不同服务器的 myid 文件中写入不同的数字，代表不同的zookeeper

  ```shell
  [root@localhost-test1 zookeeper]# echo "1" > data/myid
  ```

  ```shell
  [root@localhost-test1 zookeeper]# echo "2" > data/myid
  ```

  ```shell
  [root@localhost-test1 zookeeper]# echo "3" > data/myid
  ```

* 上述配置文件中的server.x，数字x对应到data/myid文件中的值

## 使用命令行启动zookeeper(host网络模式)

```shell
docker run -d --name=zookeeper \
--restart=always \
--net=host \
-v /opt/zookeeper/conf:/conf \
-v /opt/zookeeper/data:/data \
-v /opt/zookeeper/datalog:/datalog \
-v /opt/zookeeper/logs:/logs zookeeper:3.4.14

```

* 相关命令解释
  * -tid：表示以后台窗口模式运行，也可以改成 -d
  * --name=zookeeper：本次创建的容器的名称
  * --restart=always：表示docker启动后该容器就立即启动，若启动失败，则会一直进行重新启动中
  * --net=host：host网络模式，表示与宿主机共享一个网络
  * -v /dmp/zookeeper/conf:/conf：宿主机与docker容器映射文件夹  可读可写 默认 rw

## 修改 zookeeper 配置文件信息

* 执行命令后，成功创建zookeeper容器，此时，zookeeper的配置文件会映射到 /opt/zookeeper/conf 文件夹下
* 将zoo.cfg文件中的内容修改如下

```properties
# The number of milliseconds of each tick
tickTime=2000
# The number of ticks that the initial 
# synchronization phase can take
initLimit=10
# The number of ticks that can pass between 
# sending a request and getting an acknowledgement
syncLimit=5
# the directory where the snapshot is stored.
# do not use /tmp for storage, /tmp here is just 
# example sakes.
# zookeeper镜像中设定会将zookeeper的data与dataLog分别映射到/data, /datalog
# 本质上，这个配置文件是为zookeeper的容器所用，容器中路径的配置与容器所在的宿主机上的路径是有区别的，要区分清楚。
dataDir=/data
dataLogDir=/datalog
# the port at which the clients will connect
clientPort=38001
# the maximum number of client connections.
# increase this if you need to handle more clients
maxClientCnxns=30000
#
# Be sure to read the maintenance section of the 
# administrator guide before turning on autopurge.
#
# http://zookeeper.apache.org/doc/current/zookeeperAdmin.html#sc_maintenance
#
# The number of snapshots to retain in dataDir
autopurge.snapRetainCount=3
autopurge.purgeInterval=0
# Purge task interval in hours
# Set to "0" to disable auto purge feature
server.1=192.168.60.100:38002:38003
server.2=192.168.60.102:38002:38003
server.3=192.168.60.103:38002:38003

```

* **重新启动zookeeper容器**

  ```shell
  [root@localhost-test1 zookeeper]# docker restart zookeeper
  ```



## 查看zookeeper集群状态

* 在 zookeeper的容器的前端交互界面进行操作

* 进入zookeeper容器，client端交互界面

  ```shell
  [root@localhost-test1 zookeeper]# docker exec -it zookeeper /bin/bash
  ```

* 查看zookeeper运行情况

  ```shell
  echo stat | nc 192.168.60.100
  ```

  * 返回结果

  ```shell
  [root@localhost-test1 zookeeper]# docker exec -it zookeeper /bin/bash
  root@localhost-test1:/zookeeper-3.4.14# echo stat | nc 192.168.60.100
  no port[s] to connect to
  root@localhost-test1:/zookeeper-3.4.14# echo stat | nc 192.168.60.100 38001
  Zookeeper version: 3.4.14-4c25d480e66aadd371de8bd2fd8da255ac140bcf, built on 03/06/2019 16:18 GMT
  Clients:
   /192.168.60.100:37168[0](queued=0,recved=1,sent=0)
  
  Latency min/avg/max: 0/0/0
  Received: 1
  Sent: 0
  Connections: 1
  Outstanding: 0
  Zxid: 0x0
  Mode: follower
  Node count: 4
  root@localhost-test1:/zookeeper-3.4.14# echo stat | nc 192.168.60.102 38001
  Zookeeper version: 3.4.14-4c25d480e66aadd371de8bd2fd8da255ac140bcf, built on 03/06/2019 16:18 GMT
  Clients:
   /192.168.60.100:46266[0](queued=0,recved=1,sent=0)
  
  Latency min/avg/max: 0/0/0
  Received: 1
  Sent: 0
  Connections: 1
  Outstanding: 0
  Zxid: 0x100000000
  Mode: follower
  Node count: 4
  root@localhost-test1:/zookeeper-3.4.14# echo stat | nc 192.168.60.103 38001
  Zookeeper version: 3.4.14-4c25d480e66aadd371de8bd2fd8da255ac140bcf, built on 03/06/2019 16:18 GMT
  Clients:
   /192.168.60.100:47310[0](queued=0,recved=1,sent=0)
  
  Latency min/avg/max: 0/0/0
  Received: 1
  Sent: 0
  Connections: 1
  Outstanding: 0
  Zxid: 0x100000000
  Mode: leader
  Node count: 4
  Proposal sizes last/min/max: -1/-1/-1
  root@localhost-test1:/zookeeper-3.4.14# 
  
  ```

## zookeeper 集群安装完成

