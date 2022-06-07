# Hadoop 安装（3.X）

## Hadoop 集群规划

|      | docker10              | docker11                         | docker12                        |
| ---- | --------------------- | -------------------------------- | ------------------------------- |
| HDFS | NameNode<br/>DataNode | <br />DataNode                   | SecondaryNameNode<br />DataNode |
| YARN | <br />NodeManager     | ResourceManager<br />NodeManager | <br />NodeManager               |

### 相关端口规划

* NameNode 端口为 50000
* Hadoop的数据存储目录
  * /opt/hadoop/data
* 配置 HDFS 网页登录使用的静态用户
  * dmp
* nn web 访问地址
  * 39110
  * 39120





## 问题

### 用root用户启东时出现

* ERROR: Attempting to operate on hdfs namenode as root

* 解决方案

  * 在 /etc/profile.d/my_env.sh 中添加

  ```properties
  # resolve hadoop3.1.3 Attempting to operate on hdfs namenode as root
  export HDFS_NAMENODE_USER=root
  export HDFS_DATANODE_USER=root
  export HDFS_SECONDARYNAMENODE_USER=root
  export YARN_RESOURCEMANAGER_USER=root
  export YARN_NODEMANAGER_USER=root
  ```

  

* ERROR: JAVA_HOME is not set and could not be found.



### 启动yarn时出现

* Caused by: java.lang.NoClassDefFoundError: javax/activation/DataSource

  * 直接下载activation-1.1.1.jar到lib目录下，或者本地上传到${HADOOP_HOME}/share/hadoop/[yarn](https://so.csdn.net/so/search?q=yarn&spm=1001.2101.3001.7020)/lib目录下：

## Centos环境准备

### 相关工具

* 虚拟机可以正常上网

* 安装epel-release

  ```shell
  yum install -y epel-release
  ```

  

* net-tool：工具包集合，包含ifconfig等命令
  ```shell
  yum install -y net-tools
  ```

* vim：编辑器

  ```shell
  yum install -y vim
  ```

### 关闭防火墙

* 关闭防火墙

  ```shell
  [root@hadoop100 ~]# systemctl stop firewalld
  [root@hadoop100 ~]# systemctl disable firewalld.service
  ```



## 安装在指定用户下

* 需要安装在指定用户下的时候，可以使用此步骤

### 创建用户

* **创建hadoop**用户，并修改**hadoop**用户的密码

  ```shell
  [root@hadoop100 ~]# useradd hadoop
  [root@hadoop100 ~]# passwd hadoop
  ```

* **配置atguigu用户具有root权限，方便后期加sudo执行root权限的命令**

  ```shell
  [root@hadoop100 ~]# vim /etc/sudoers
  ```

  * 修改/etc/sudoers文件，在%wheel这行下面添加一行，如下所示：

    ```shell
    ## Allow root to run any commands anywhere
    root    ALL=(ALL)     ALL
    
    ## Allows people in group wheel to run all commands
    %wheel  ALL=(ALL)       ALL
    atguigu   ALL=(ALL)     NOPASSWD:ALL
    ```

  * 注意：atguigu这一行不要直接放到root行下面，因为所有用户都属于wheel组，你先配置了atguigu具有免密功能，但是程序执行到%wheel行时，该功能又被覆盖回需要密码。所以atguigu要放到%wheel这行下面。

* **在/opt目录下创建文件夹，并修改所属主和所属组**

  * 在/opt目录下创建module、software文件夹

    ```shell
    [root@hadoop100 ~]# mkdir /opt/module
    [root@hadoop100 ~]# mkdir /opt/software
    ```

  * 修改module、software文件夹的所有者和所属组均为hadoop用户

    ```shell
    [root@hadoop100 ~]# chown hadoop:hadoop /opt/module 
    [root@hadoop100 ~]# chown hadoop:hadoop /opt/software
    ```

  * 查看module、software文件夹的所有者和所属组

    ```shell
    [root@hadoop100 ~]# cd /opt/
    [root@hadoop100 opt]# ll
    总用量 12
    drwxr-xr-x. 2 hadoop hadoop 4096 5月  28 17:18 module
    drwxr-xr-x. 2 root    root    4096 9月   7 2017 rh
    drwxr-xr-x. 2 hadoop hadoop 4096 5月  28 17:18 software
    
    ```



### 配置机器SSH免密登陆

由于安装的时候需要向不同机器发送资源，所以要求各台机器间能实现SSH免密登陆。配置免密登陆的步骤如下

#### 配置自身免密登录

```shell
su hadoop #用户名

ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa && \
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys && \
chmod 600 ~/.ssh/authorized_keys
```

> ***注意:\*** 配置完成后，可以通过运行命令 `ssh localhost` 判断是否成功，如果不需要输入密码就能ssh登陆则证明成功

#### 配置机器之间的免密登录

```shell
ssh-copy-id -i ~/.ssh/id_rsa.pub 远程机器的ip
```

* 例子

  ```shell
  ssh-copy-id -i ~/.ssh/id_rsa.pub 192.168.60.102
  ```

  * 表示本机  ssh 命令 可以免密登录 192.168.60.102 这台机器



## 安装JDK

* 需要使用JDK1.8及以上的版本 此处安装的是JDK11

* 可以将jdk的环境变量添加到 /etc/profile.d/my_env.sh  中，centos在启动的时候会访问该目录下的文件
* 

## Hadoop的安装

### 上传并解压到 安装目录

```shell
[root@docker12 hadoop]# pwd
/opt/hadoop
[root@docker12 hadoop]# ll
总用量 0
drwxr-xr-x. 9 root root 149 9月  12 2019 hadoop-3.1.3
```

### 添加hadoop 环境变量

* 获取Hadoop安装路径

  ```shell
  [root@docker10 hadoop-3.1.3]# pwd
  /opt/hadoop/hadoop-3.1.3
  ```

* 打开/etc/profile.d/my_env.sh文件

  ```shell
  
  ```

  *  在my_env.sh文件末尾添加如下内容：（shift+g）

    ```shell
    #HADOOP_HOME
    export HADOOP_HOME=/opt/hadoop/hadoop-3.1.3
    export PATH=$PATH:$HADOOP_HOME/bin
    export PATH=$PATH:$HADOOP_HOME/sbin
    
    ```

  * 保存并退出： :wq

* 让修改后的文件生效

  ```shell
  [root@docker10 hadoop-3.1.3]# source /etc/profile
  ```

* 测试是否安装成功

  ```shell
  [root@docker10 hadoop-3.1.3]# hadoop version
  Hadoop 3.1.3
  Source code repository https://gitbox.apache.org/repos/asf/hadoop.git -r ba631c436b806728f8ec2f54ab1e289526c90579
  Compiled by ztang on 2019-09-12T02:47Z
  Compiled with protoc 2.5.0
  From source with checksum ec785077c385118ac91aadde5ec9799
  This command was run using /opt/hadoop/hadoop-3.1.3/share/hadoop/common/hadoop-common-3.1.3.jar
  ```

* 重新启动（可以不这么做）

## 编写集群分发脚本

### **scp（secure copy）安全拷贝**

* scp定义

  * cp可以实现服务器与服务器之间的数据拷贝。（from server1 to server2）

* 基本语法

  * scp    -r    	$pdir/$fname       			  $user@$host:$pdir/$fname

    命令  递归   要拷贝的文件路径/名称  	目的地用户@主机:目的地路径/名称



### **rsync远程同步工具**

* rsync主要用于备份和镜像。具有速度快、避免复制相同内容和支持符号链接的优点。

* rsync和scp区别：用rsync做文件的复制要比scp的速度快，rsync只对差异文件做更新。scp是把所有文件都复制过去

* 基本语法

  * rsync    -av    		 $pdir/$fname       			$user@$host:$pdir/$fname

    命令  	选项参数  要拷贝的文件路径/名称     目的地用户@主机:目的地路径/名称

  * 选项参数说明

    | 选项 | 功能         |
    | ---- | ------------ |
    | -a   | 归档拷贝     |
    | -v   | 显示复制过程 |

### **xsync集群分发脚本**

* 需求：循环复制文件到所有节点的相同目录下
* 需求分析：







## 修改默认端口号

### 端口规划

| type          | name                                          | ip       | port  |
| ------------- | --------------------------------------------- | -------- | ----- |
| core-NameNode | fs.defaultFS                                  | docker10 | 38501 |
|               |                                               |          |       |
| hdfs-site     | dfs.namenode.http-address                     | docker10 | 38500 |
| hdfs-site     | dfs.namenode.secondary.http-address           | docker12 | 38510 |
| hdfs-site     | dfs.datanode.address                          | 0.0.0.0  | 38511 |
| hdfs-site     | dfs.datanode.http.address                     | 0.0.0.0  | 38512 |
| hdfs-site     | dfs.datanode.ipc.address                      | 0.0.0.0  | 38513 |
|               |                                               |          |       |
| yarn-site     | yarn.resourcemanager.webapp.address           | docker11 | 38520 |
| yarn-site     | yarn.resourcemanager.address                  | docker11 | 38521 |
| yarn-site     | yarn.resourcemanager.scheduler.address        | docker11 | 38522 |
| yarn-site     | yarn.resourcemanager.resource-tracker.address | docker11 | 38523 |
| yarn-site     | yarn.resourcemanager.admin.address            | docker11 | 38524 |
| yarn-site     | yarn.nodemanager.localizer.address            | 0.0.0.0  | 38525 |
| yarn-site     | yarn.nodemanager.webapp.address               | 0.0.0.0  | 38526 |
|               |                                               |          |       |
| yarn-site     | yarn.log.server.url                           | docker12 | 38530 |
|               |                                               |          |       |
| mapred-site   | mapreduce.jobhistory.webapp.address           | docker10 | 38540 |
| mapred-site   | mapreduce.jobhistory.address                  | docker10 | 38541 |
|               |                                               |          |       |
| mapred-site   | mapreduce.shuffle.port                        | null     | 38551 |
|               |                                               |          |       |

### core-site.xml

```xml
<configuration>
    <!-- 指定NameNode的地址 -->
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://docker10:38501</value>
    </property>

    <!-- 指定hadoop数据的存储目录 -->
    <property>
        <name>hadoop.tmp.dir</name>
        <value>/opt/hadoop/hadoop-3.1.3/data</value>
    </property>

    <!-- 配置HDFS网页登录使用的静态用户为atguigu -->
    <property>
        <name>hadoop.http.staticuser.user</name>
        <value>root</value>
    </property>

</configuration>
```



### hdfs-site.xml

```xml
<configuration>

    <!-- nn web端访问地址-->
    <property>
        <name>dfs.namenode.http-address</name>
        <value>docker10:38500</value>
    </property>
    
    <!-- 2nn web端访问地址-->
    <property>
        <name>dfs.namenode.secondary.http-address</name>
        <value>docker12:38510</value>
    </property>
    <!-- DataNode 地址 -->
    <property>
    	<name>dfs.datanode.address</name>
        <value>0.0.0.0:38511</value>
    </property>
    
    <property>
       	<name>dfs.datanode.http.address</name>
      	<value>0.0.0.0:38512</value>
    </property>

    <property>
    	<name>dfs.datanode.ipc.address</name>
    	<value>0.0.0.0:38513</value>
    </property>
</configuration>
```







### yarn-site.xml

```xml
<configuration>

<!-- Site specific YARN configuration properties -->
    <!-- 指定MR走shuffle -->
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>

    <!-- 指定ResourceManager的地址 配置此项后，yarn会使用默认的 port
    	如果设置了其他的配置信息，则会覆盖此默认端口号
	-->
    <!--
    <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>docker11</value>
    </property>
    -->
    <property>
        <name>yarn.resourcemanager.address</name>
       	<value>docker11:38521</value>
    </property>

    <property>
        <name>yarn.resourcemanager.scheduler.address</name>
        <value>docker11:38522</value>
    </property>

    <property>
        <name>yarn.resourcemanager.resource-tracker.address</name>
        <value>docker11:38523</value>
    </property>

    <property>
        <name>yarn.resourcemanager.admin.address</name>
        <value>docker11:38524</value>
    </property>

    <property>
        <name>yarn.resourcemanager.webapp.address</name>
        <value>docker11:38520</value>
    </property>

    <property>
        <name>yarn.nodemanager.localizer.address</name>
        <value>0.0.0.0:38525</value>
    </property>

    <property>
        <name>yarn.nodemanager.webapp.address</name>
        <value>0.0.0.0:38526</value>
    </property>

    <!-- 环境变量的继承 -->
    <property>
        <name>yarn.nodemanager.env-whitelist</name>
        <value>JAVA_HOME,HADOOP_COMMON_HOME,HADOOP_HDFS_HOME,HADOOP_CONF_DIR,CLASSPATH_PREPEND_DISTCACHE,HADOOP_YARN_HOME,HADOOP_MAPRED_HOME</value>
    </property>
    
    <!-- 开启日志聚集功能 -->
    <property>
        <name>yarn.log-aggregation-enable</name>
        <value>true</value>
    </property>
    <!-- 设置日志聚集服务器地址 -->
    <property>  
        <name>yarn.log.server.url</name>  
        <value>http://docker10:38530/jobhistory/logs</value>
    </property>
    <!-- 设置日志保留时间为7天 -->
    <property>
        <name>yarn.log-aggregation.retain-seconds</name>
        <value>604800</value>
    </property>
</configuration>

```

### mapred-site.xml

```xml
<configuration>
    <!-- 指定MapReduce程序运行在Yarn上 -->
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>

    <!-- 历史服务器端地址 -->
    <property>
        <name>mapreduce.jobhistory.address</name>
        <value>docker10:38541</value>
    </property>

    <!-- 历史服务器web端地址 -->
    <property>
        <name>mapreduce.jobhistory.webapp.address</name>
        <value>docker10:38540</value>
    </property>

    <property>
       	<name>mapreduce.shuffle.port</name>
     	<value>38551</value>
    </property>
</configuration>

```



### works

```json
docker10
docker11
docker12
```



:wq

10.166.147.61   app-61
10.166.147.62   app-62
10.166.147.63   app-63