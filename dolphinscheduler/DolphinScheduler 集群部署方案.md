# DolphinScheduler 集群部署方案

## 环境准备

* 系统要求，内存最少  4G centos7+

* JDK：下载[JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html) (1.8+)，并将 JAVA_HOME 配置到以及 PATH 变量中。如果你的环境中已存在，可以跳过这步。

* 二进制包：在[下载页面](https://dolphinscheduler.apache.org/zh-cn/download/download.html)下载 DolphinScheduler 二进制包

* 数据库：[PostgreSQL](https://www.postgresql.org/download/) (8.2.15+) 或者 [MySQL](https://dev.mysql.com/downloads/mysql/) (5.7+)，两者任选其一即可，如 MySQL 则需要 JDBC Driver 8.0.16

* 注册中心：[ZooKeeper](https://zookeeper.apache.org/releases.html) (3.4.6+)，[下载地址](https://zookeeper.apache.org/releases.html)

* 进程树分析

  - macOS安装`pstree`

  - Fedora/Red/Hat/CentOS/Ubuntu/Debian安装`psmisc`

  - **注意：** 如果以下命令报错，解决方案看下面这篇文章

    ```shell
    yum install psmisc -y
    ```

  - https://zhuanlan.zhihu.com/p/474661754

  > ***注意:\*** DolphinScheduler 本身不依赖 Hadoop、Hive、Spark，但如果你运行的任务需要依赖他们，就需要有对应的环境支持

## 准备 DolphinScheduler 启动环境

* 集群环境下需要几个系统安装的 JDK、ZooKeeper、等软件在同一位置，各个服务器安装的环境一致

### 配置jdk环境变量

* 自行百度搜索即可

### 配置用户免密及权限

创建部署用户，并且一定要配置 `sudo` 免密。以创建 dolphinscheduler 用户为例

* 因为相关的一些命令 在执行的时候，linux系统会加上 sudo 的前缀，所以需要进行免密

```shell
# 创建用户需使用 root 登录
useradd dolphinscheduler

# 添加密码
echo "dolphinscheduler" | passwd --stdin dolphinscheduler

# 配置 sudo 免密
sed -i '$adolphinscheduler  ALL=(ALL)  NOPASSWD: NOPASSWD: ALL' /etc/sudoers
sed -i 's/Defaults    requirett/#Defaults    requirett/g' /etc/sudoers

# 修改目录权限，使得部署用户对二进制包解压后的 apache-dolphinscheduler-*-bin 目录有操作权限
chown -R dolphinscheduler:dolphinscheduler apache-dolphinscheduler-*-bin
```

> ***注意:\***
>
> - 因为任务执行服务是以 `sudo -u {linux-user}` 切换不同 linux 用户的方式来实现多租户运行作业，所以部署用户需要有 sudo 权限，而且是免密的。初学习者不理解的话，完全可以暂时忽略这一点
> - 如果发现 `/etc/sudoers` 文件中有 "Defaults requirett" 这行，也请注释掉

### 配置机器SSH免密登陆

由于安装的时候需要向不同机器发送资源，所以要求各台机器间能实现SSH免密登陆。配置免密登陆的步骤如下

#### 配置自身免密登录

```shell
su dolphinscheduler

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

### 启动zookeeper

进入 zookeeper 的安装目录，将 `zoo_sample.cfg` 配置文件复制到 `conf/zoo.cfg`，并将 `conf/zoo.cfg` 中 dataDir 中的值改成 `dataDir=./tmp/zookeeper`

```shell
# 启动 zookeeper
./bin/zkServer.sh start
```

## 修改相关配置

* 完成了基础环境的准备后，在运行部署命令前，还需要根据环境修改配置文件。配置文件在路径在`conf/config/install_config.conf`下，一般部署只需要修改**INSTALL MACHINE、DolphinScheduler ENV、Database、Registry Server**部分即可完成部署，下面对必须修改参数进行说明

  ```shell
  # ---------------------------------------------------------
  # INSTALL MACHINE
  # ---------------------------------------------------------
  # 需要配置master、worker、API server，所在服务器的IP均为机器IP或者localhost
  # 如果是配置hostname的话，需要保证机器间可以通过hostname相互链接
  # 如下图所示，部署 DolphinScheduler 机器的 hostname 为 ds1,ds2,ds3,ds4,ds5，其中 ds1,ds2 安装 master 服务，ds3,ds4,ds5安装 worker 服务，alert server安装在ds4中，api server 安装在ds5中
  ips="ds1,ds2,ds3,ds4,ds5"
  masters="ds1,ds2"
  workers="ds3:default,ds4:default,ds5:default"
  alertServer="ds4"
  apiServers="ds5"
  pythonGatewayServers="ds5"
  
  # DolphinScheduler安装路径，如果不存在会创建
  installPath="~/dolphinscheduler"
  
  # 部署用户，填写在 **配置用户免密及权限** 中创建的用户
  deployUser="dolphinscheduler"
  
  # ---------------------------------------------------------
  # DolphinScheduler ENV
  # ---------------------------------------------------------
  # JAVA_HOME 的路径，是在 **前置准备工作** 安装的JDK中 JAVA_HOME 所在的位置
  # jdk 的安装路径
  javaHome="/your/java/home/here"
  
  # ---------------------------------------------------------
  # Database
  # ---------------------------------------------------------
  # 数据库的类型，用户名，密码，IP，端口，元数据库db。其中 DATABASE_TYPE 目前支持 mysql, postgresql, H2
  # 请确保配置的值使用双引号引用，否则配置可能不生效
  DATABASE_TYPE="mysql"
  SPRING_DATASOURCE_URL="jdbc:mysql://ds1:3306/ds_201_doc?useUnicode=true&characterEncoding=UTF-8"
  # 如果你不是以 dolphinscheduler/dolphinscheduler 作为用户名和密码的，需要进行修改
  SPRING_DATASOURCE_USERNAME="dolphinscheduler"
  SPRING_DATASOURCE_PASSWORD="dolphinscheduler"
  
  # ---------------------------------------------------------
  # Registry Server
  # ---------------------------------------------------------
  # 注册中心地址，zookeeper服务的地址
  registryServers="localhost:2181"
  ```

* **注意**

  * 该目录别乱写，若找不到，在执行shell命令时，会出现错误

  ```shell
  # The directory to store local data for all machine we config above. Make sure user `deployUser` have permissions to read and write this directory.
  dataBasedirPath="/tmp/dolphinscheduler"
  
  ```

  * 出现的错误

  ```verilog
  [LOG-PATH]: /home/dolphinscheduler/dolphinscheduler/logs/5026253113856_1/13/20.log, [HOST]:  192.168.60.103
  [INFO] 2022-03-30 16:17:14.237 TaskLogLogger-class org.apache.dolphinscheduler.plugin.task.shell.ShellTask:[83] - shell task params {"resourceList":[],"localParams":[],"rawScript":"echo 'hello world 1'","dependence":{},"conditionResult":{"successNode":[],"failedNode":[]},"waitStartTimeout":{},"switchResult":{}}
  [INFO] 2022-03-30 16:17:14.280 TaskLogLogger-class org.apache.dolphinscheduler.plugin.task.shell.ShellTask:[137] - raw script : echo 'hello world 1'
  [INFO] 2022-03-30 16:17:14.281 TaskLogLogger-class org.apache.dolphinscheduler.plugin.task.shell.ShellTask:[138] - task execute path : ./tmp/dolphinscheduler/exec/process/5026238282880/5026253113856_1/13/20
  [INFO] 2022-03-30 16:17:14.282 TaskLogLogger-class org.apache.dolphinscheduler.plugin.task.shell.ShellTask:[86] - tenantCode user:dolphinscheduler, task dir:13_20
  [INFO] 2022-03-30 16:17:14.283 TaskLogLogger-class org.apache.dolphinscheduler.plugin.task.shell.ShellTask:[91] - create command file:./tmp/dolphinscheduler/exec/process/5026238282880/5026253113856_1/13/20/13_20.command
  [INFO] 2022-03-30 16:17:14.284 TaskLogLogger-class org.apache.dolphinscheduler.plugin.task.shell.ShellTask:[117] - command : #!/bin/sh
  BASEDIR=$(cd `dirname $0`; pwd)
  cd $BASEDIR
  source /home/dolphinscheduler/dolphinscheduler/conf/env/dolphinscheduler_env.sh
  ./tmp/dolphinscheduler/exec/process/5026238282880/5026253113856_1/13/20/13_20_node.sh
  [INFO] 2022-03-30 16:17:14.295 TaskLogLogger-class org.apache.dolphinscheduler.plugin.task.shell.ShellTask:[285] - task run command: sudo -u dolphinscheduler sh ./tmp/dolphinscheduler/exec/process/5026238282880/5026253113856_1/13/20/13_20.command
  [INFO] 2022-03-30 16:17:14.319 TaskLogLogger-class org.apache.dolphinscheduler.plugin.task.shell.ShellTask:[176] - process start, process id is: 10737
  [INFO] 2022-03-30 16:17:14.357 TaskLogLogger-class org.apache.dolphinscheduler.plugin.task.shell.ShellTask:[200] - process has exited, execute path:./tmp/dolphinscheduler/exec/process/5026238282880/5026253113856_1/13/20, processId:10737 ,exitStatusCode:127 ,processWaitForStatus:true ,processExitValue:127
  [INFO] 2022-03-30 16:17:15.321 TaskLogLogger-class org.apache.dolphinscheduler.plugin.task.shell.ShellTask:[66] -  -> welcome to use bigdata scheduling system...
  	sh: ./tmp/dolphinscheduler/exec/process/5026238282880/5026253113856_1/13/20/13_20.command: 没有那个文件或目录
  [INFO] 2022-03-30 16:17:15.323 TaskLogLogger-class org.apache.dolphinscheduler.plugin.task.shell.ShellTask:[60] - FINALIZE_SESSION
  ```

  

## 初始化数据库

* DolphinScheduler 元数据存储在关系型数据库中，目前支持 PostgreSQL 和 MySQL，如果使用 MySQL 则需要手动下载 [mysql-connector-java 驱动](https://downloads.mysql.com/archives/c-j/) (8.0.16) 并移动到 DolphinScheduler 的 lib目录下。下面以 MySQL 为例，说明如何初始化数据库

  ```shell
  mysql -uroot -p
  
  mysql> CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
  
  # 修改 {user} 和 {password} 为你希望的用户名和密码
  mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'%' IDENTIFIED BY '{password}';
  mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'localhost' IDENTIFIED BY '{password}';
  
  mysql> flush privileges
  ```

* 完成上述步骤后，您已经为 DolphinScheduler 创建一个新数据库，现在你可以通过快速的 Shell 脚本来初始化数据库

  ```shell
  # 在 apache-dolphinscheduler-2.0.3-bin 目录下
  sh script/create-dolphinscheduler.sh
  ```

## 启动 DolphinScheduler

* 使用上面创建的**部署用户**运行以下命令完成部署，部署后的运行日志将存放在 logs 文件夹内

  ```shell
  sh install.sh
  ```

  > ***注意:\*** 第一次部署的话，可能出现 5 次`sh: bin/dolphinscheduler-daemon.sh: No such file or directory`相关信息，次为非重要信息直接忽略即可

## 登录 DolphinScheduler

* 浏览器访问地址 http://localhost:12345/dolphinscheduler 即可登录系统UI。默认的用户名和密码是 **admin/dolphinscheduler123**

## 启停服务

```shell
# 一键停止集群所有服务
sh ./bin/stop-all.sh

# 一键开启集群所有服务
sh ./bin/start-all.sh

# 启停 Master
sh ./bin/dolphinscheduler-daemon.sh stop master-server
sh ./bin/dolphinscheduler-daemon.sh start master-server

# 启停 Worker
sh ./bin/dolphinscheduler-daemon.sh start worker-server
sh ./bin/dolphinscheduler-daemon.sh stop worker-server

# 启停 Api
sh ./bin/dolphinscheduler-daemon.sh start api-server
sh ./bin/dolphinscheduler-daemon.sh stop api-server

# 启停 Logger
sh ./bin/dolphinscheduler-daemon.sh start logger-server
sh ./bin/dolphinscheduler-daemon.sh stop logger-server

# 启停 Alert
sh ./bin/dolphinscheduler-daemon.sh start alert-server
sh ./bin/dolphinscheduler-daemon.sh stop alert-server

# 启停 Python Gateway
sh ./bin/dolphinscheduler-daemon.sh start python-gateway-server
sh ./bin/dolphinscheduler-daemon.sh stop python-gateway-server
```

> ***注意:\***：服务用途请具体参见《系统架构设计》小节





## 修改相关端口方法

### 修改applicationApi启动端口

* 默认值  12345

* 修改配置文件  dolphinscheduler/conf/application-api.properties

  ```properties
  # server port
  server.port=38364
  ```

* 修改  dolphinscheduler/conf/install_config.conf 这个文件

  ```properties
  # server port
  server.port=38364
  ```

### 修改Master 监听端口

* 默认值  5678

* 修改配置文件 /dolphinscheduler/conf/master.properties

  ```properties
  # master listen port
  master.listen.port=38365
  
  ```

* 修改配置文件  /dolphinscheduler/conf/application-master.yaml

  ```properties
  server:
    port: 38365
  
  ```

### 修改Worker 监听端口

* 默认：1234

* 修改配置文件   /dolphinscheduler/conf/worker.properties 

  ```properties
  # worker listener port
  worker.listen.port=38366
  # 这个是 修改默认 worker组的名称
  # default worker groups separated by comma, like 'worker.groups=default,test'
  worker.groups=38366
  
  # alert server listen host
  alert.listen.host=192.168.60.103
  ```

*  修改 dolphinscheduler/conf/install_config.conf  

  ```properties
  # 修改集群监听的端口号
  # A comma separated list of machine <hostname>:<workerGroup> or <IP>:<workerGroup>.All hostname or IP must be a
  # subset of configuration `ips`, And workerGroup have default value as `default`, but we recommend you declare behind the hosts
  # Example for hostnames: workers="ds1:default,ds2:default,ds3:default", Example for IPs: workers="192.168.8.1:default,192.168.8.2:default,192.168.8.3:default"
  workers="192.168.60.102:38366,192.168.60.103:38366"
  
  ```

  

## 安装在JDK11 环境下，需要修改启动参数

### 出现的错误

```verilog
Unrecognized VM option 'UseParNewGC'
Error: Could not create the Java Virtual Machine.
Error: A fatal exception has occurred. Program will exit.
```

### 原因

* 因为在JDK11 环境下，已经将 UseParNewGC 这个垃圾回收器给删除掉了
* 所以在启动的时候会报 不知道该参数

### 解决方案

* 将集群中  /dmp/dolphinscheduler/bin 目录下的 dolphinscheduler-daemon.sh 中将该启动参数删除即可

  ```shell  
  # 删除启动参数中的 -XX:+UseParNewGC 即可
  export DOLPHINSCHEDULER_OPTS="-server -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m -Xss512k -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintGCDetails -Xloggc:$DOLPHINSCHEDULER_LOG_DIR/gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=dump.hprof -XshowSettings:vm $DOLPHINSCHEDULER_OPTS"
  ```