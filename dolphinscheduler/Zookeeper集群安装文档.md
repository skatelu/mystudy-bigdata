# zookeeper集群安装文档

## 准备工作

* 安装JDK1.8+
* 下载zookeeper linux安装文件

## 进行部署

* 解压zookeeper文件到指定地址

  ```shell
  tar -zxvf apache-zookeeper-3.6.3-bin.tar.gz -C ../
  ```

* 解压后 在 apache-zookeeper-3.6.3-bin 文件夹中创建 data 和 logs 目录

  ```shell
  mkdir data
  mkdir logs
  ```

* 在data目录中创建myid文件，并在不同的机器中，写入不同的数字

  ```shell
  # 第一台
  echo 1 > data/myid
  # 第二台
  echo 2 > data/myid
  # 第三台
  echo 3 > data/myid
  ```

* 创建配置文件，在conf目录下，将 zoo_sample.cfg 文件复制成 zoo.cfg文件

  ```shell
  [root@localhost conf]# cp zoo_sample.cfg zoo.cfg
  ```

* 修改配置文件

  ```properties
  # 将从上面创建的data文件跟logs文件 设置成数据目录和日志文件目录
  dataDir=/opt/apache-zookeeper-3.6.3-bin/data
  dataLogDir=/opt/apache-zookeeper-3.6.3-bin/logs
  # zookeeper 服务的端口号，需要的话可以更换
  clientPort=2181
  # 打开下面两个配置
  # 清理后保存的日志文件个数
  # The number of snapshots to retain in dataDir
  autopurge.snapRetainCount=3
  # 配置实现自动清理日志 时间间隔 小时
  # Purge task interval in hours
  # Set to "0" to disable auto purge feature
  autopurge.purgeInterval=0
  
  # 在配置文件最后，设置zookeeper集群地址，其中 server.0对应 data文件夹中 myid文件中的数值
  server.1=192.168.60.100:2888:3888
  server.2=192.168.60.102:2888:3888
  server.3=192.168.60.103:2888:3888
  ```

* 每个节点都配置完成后，执行 zkServer.sh start 进行启动

* zkServer.sh status 可以查看zookeeper状态



## zookeeper 批量执行脚本

脚本名称为zk_run.sh

将下面代码粘贴进zk_run.sh

添加执行权限 chmode +x zk_run.sh

运行脚本 ./zk_run.sh start

**如果发现zookeeper没有启动起来，尝试在每个zookeeper的zookeeper-3.4.10/bin/zkEnv.sh脚本最上面添加下面这一行**

**export JAVA_HOME=/opt/jdk1.8.0_92**

到此，我的脚本可以正常运行

如果你的还不行，试试下面这个

把profile的配置信息echo到.bashrc中 echo ‘source /etc/profile’ >> ~/.bashrc

```
#!/bin/bash  

usage="Usage: $0 (start|stop|status)"

if [ $# -lt 1 ]; then
  echo $usage
  exit 1
fi

behave=$1

iparray=(node1 node2 node3)

path="/home/orco/resources/zookeeper-3.4.10"

echo "$behave zkServer cluster"

for ip in ${iparray[*]}  
do

    echo "ssh to $ip"

    ssh $ip "$path/bin/zkServer.sh $behave $path/conf/zoo.cfg"

    sleep 2s

done

exit 0   
```













