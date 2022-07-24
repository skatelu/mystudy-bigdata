# 搭建时间同步服务器

## 1. NTP服务简介

* NTP（Network Time Protocol，网络时间协议）是用来使网络中的各个计算机时间同步的一种协议。它的用途是把计算机的时钟同步到世界协调时UTC，其精度在局域网内可达0.1ms，在互联网上绝大多数的地方其精度可以达到1-50ms。
*  NTP服务器就是利用NTP协议提供时间同步服务的

## 2. NTP服务搭建

### 2.1 环境准备

* | 主机名称 | IP地址      | 系统版本 | 角色      |
  | -------- | ----------- | -------- | --------- |
  |          | 10.200.1.84 | CentOS 7 | NTP服务器 |
  |          | 10.200.1.85 | CentOS 7 | 客户端    |
  |          | 10.200.1.86 | CentOS 7 | 客户端    |

### 2.2. 安装NTP服务

* 查看系统是否安装 ntp服务

  ```shell
  [root@localhost ~]# rpm -qa ntp
  ntp-4.2.6p5-29.el7.centos.2.x86_64
  ```

* 若没有安装可以使用 YUM 命令进行安装即可

  ```shell
  yum -y install ntp
  ```

### 2.3. 配置NTP服务

* ntp 服务器默认是不运行客户端进行时间同步的，所有我们需要配置文件设置允许。NTP服务的默认配置文件是/etc/ntp.conf

* 对原来的 ntp.conf 文件进行备份

  ```shell
  cp /etc/ntp.conf{,_$(date +%Y%m%d%H)}
  ```

#### 2.3.1 NTP服务端

* 修改ntp.conf  文件

* vim /etc/ntp.conf

  ```properties
  ………省略内容………
  # 以下为 NTP服务默认的时间同步源，先将其注释
  #server 0.centos.pool.ntp.org iburst 
  #server 1.centos.pool.ntp.org iburst 
  #server 2.centos.pool.ntp.org iburst 
  #server 3.centos.pool.ntp.org iburst 
  # 添加新的时间同步源
  server time1.aliyun.com
  server 0.cn.pool.ntp.org iburst
  server 1.cn.pool.ntp.org iburst
  server 2.cn.pool.ntp.org iburst
  server 3.cn.pool.ntp.org iburst
  
  #新增:当外部时间不可用时，使用本地时间.
  server 172.16.128.171 iburst
  fudge 127.0.0.1 stratum 10
  
  #新增:允许上层时间服务器主动修改本机时间.
  restrict 0.cn.pool.ntp.org nomodify notrap noquery
  restrict 1.cn.pool.ntp.org nomodify notrap noquery
  restrict 2.cn.pool.ntp.org nomodify notrap noquery
  ```

#### 2.3.2 更新服务端系统与硬件时间

**可以连接互联网**

```shell
ntpdate cn.pool.ntp.org
```

**服务器不可联网：手动设置为当前时间 **

```shell
date –s "2019-02-19 16:00:00"
```

**关键操作，设置时间后，需要把时间更新到BIOS，执行如下命令：**

```shell
hwclock -w
```



#### 2.3.2 配置客户端 其余的机器都为客户端

* vim /etc/ntp.conf

  ```properties
  # 将以下四行注释掉：
  #server 0.centos.pool.ntp.org iburst
  #server 1.centos.pool.ntp.org iburst
  #server 2.centos.pool.ntp.org iburst
  #server 3.centos.pool.ntp.org iburst
  
  # add by JavaAlpha ntp server
  server 10.200.1.84 iburst
  ```

* 更新时间：

  ```shell
  ntpdate 10.200.1.84
  ```

  

### 2.4. 启动NTP服务

```shell
systemctl start ntpdate
systemctl start ntpd
```

#### 加入防火墙：

```shell
firewall-cmd --permanent --add-service=ntp
firewall-cmd --reload
```



### 2.5 启动完毕，检查服务是否可用：

```shell
ntpq –p
```

```shell
[root@localhost ~]# ntpq -p
     remote           refid      st t when poll reach   delay   offset  jitter
==============================================================================
*203.107.6.88    100.107.25.114   2 u  101  512  377   21.963   -3.384   3.980
+219.216.128.25  10.10.10.123     2 u  204  256  377   26.700   -7.212   1.281
 LOCAL(0)        .LOCL.          10 l 132m   64    0    0.000    0.000   0.000
 
```









