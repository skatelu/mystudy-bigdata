# Centos 系统的相关设置

## 安装好centos后修改yum源为 163

* 修改 yum源

  ```shell
  cd /etc/yum.repos.d/
  ---
  mkdir back
  ---
  mv * back/
  ---
  curl http://mirrors.163.com/.help/CentOS7-Base-163.repo 
  ---
  curl http://mirrors.163.com/.help/CentOS7-Base-163.repo > 163.repo # 设置yum源为163的网址
  --- 
  yum clean all
  ---
  yum -y install net-tools vim lrzsz
  ```



## 设置防火墙为 Iptables 并设置空规则

* 每台机器都执行

```shell
# 关闭防火墙
systemctl  stop firewalld  &&  systemctl  disable firewalld
# 安装 iptables
yum -y install iptables-services  &&  systemctl  start iptables  &&  systemctl  enable iptables  &&  iptables -F  &&  service iptables save
```

# 安装Docker（推荐 19.03)

### 安装前需要执行，添加相关依赖

```shell
# 添加gcc 与 gcc-c++
yum -y install gcc && \
yum -y install gcc-c++

# 增加docker 的依赖
yum install -y yum-utils
```



## 三种安装方式

### 官方脚本安装 Script

* 及其不推荐安装，生产环境中不会选择最新版本安装，稳定性，Bug未知

* 会自动监测安装平台，选择安装适合当前平台的最新稳定版本，脚本如下

  ```shell
  yum update 
  ---
  curl -sSL https://get.docker.com/ | sh
  ---
  systemctl start docker
  ---
  systemctl enable docker
  ---
  docker run hello-world
  ```

### yum 源安装（可以选择版本）



### rpm包安装方式(最常用的安装方式)

* 查看 centos系统的核心版本

  ```shell
  [root@docker12 software]# cat /etc/redhat-release 
  CentOS Linux release 7.6.1810 (Core)
  ```

* docker rpm包下载地址

  ```http
  https://download.docker.com/linux/centos/7/x86_64/stable/Packages/
  ```

  ```http
  https://mirrors.aliyun.com/docker-ce/linux/centos/7.6/x86_64/stable/Packages/
  ```

* 需要下载5个安装包

  ```shell
  # 安装基础，首先需要安装 containerd.io-1.6.4-3.1.el7.x86_64.rpm，再就是 docker-ce-rootless-extras-20.10.9-3.el7.x86_64.rpm、docker-scan-plugin-0.9.0-3.el7.x86_64.rpm
  containerd.io-1.6.4-3.1.el7.x86_64.rpm
  docker-scan-plugin-0.9.0-3.el7.x86_64.rpm
  docker-ce-rootless-extras-20.10.9-3.el7.x86_64.rpm
  ----------------------
  docker-ce-20.10.9-3.el7.x86_64.rpm
  docker-ce-cli-20.10.9-3.el7.x86_64.rpm
  ```



* 下载好的rpm包进行安装

* 将下载好的 rpm docker文件进行上传

  ```shell
  # 此处我上传到 /opt/software 目录下
  [root@docker12 software]# ll /opt/software/
  总用量 99524
  -rw-r--r-- 1 root root 34647136 7月   8 09:09 containerd.io-1.6.4-3.1.el7.x86_64.rpm
  -rw-r--r-- 1 root root 23785744 7月   8 09:08 docker-ce-20.10.9-3.el7.x86_64.rpm
  -rw-r--r-- 1 root root 30801216 7月   8 09:10 docker-ce-cli-20.10.9-3.el7.x86_64.rpm
  -rw-r--r-- 1 root root  8427040 7月   8 09:05 docker-ce-rootless-extras-20.10.9-3.el7.x86_64.rpm
  -rw-r--r-- 1 root root  3927120 7月   8 10:35 docker-scan-plugin-0.9.0-3.el7.x86_64.rpm
  
  ```

* 在该文件夹下运行 yum 安装命令

  ```shell
  # 跳转到有 docker rpm包的文件夹下面
  cd /opt/software/
  # 安装docker 前提环境
  yum -y install containerd.io-1.6.4-3.1.el7.x86_64.rpm
  yum -y install docker-scan-plugin-0.9.0-3.el7.x86_64.rpm
  # yum 安装的时候，会优先查找当前文件夹下是否有该安装包，没有的话再去网络源上寻找安装
  yum -y install docker-ce-*
  ```

* 安装完成后，将docker设为开机自启动

  ```shell
  systemctl enable docker
  ```

* 重新启动centos 使其与 iptables结合

* 测试是否成功的话，运行 hello-world 软件

  ```shell
  docker run hello-world
  ```

#### 其他机器安装

* 用scp命令将安装包 copy 到其他系统上

  ```shell
  scp -r /opt/software/ root@192.168.66.11:/opt/
  ```

* 重复上面的内容安装即可

安装的是docker1703 版本

docker 在 1703 的后续版本中封禁了一个功能

* 给每个容器单独赋予了一个能够在外部访问地址的权限

1709 版本以后多了一个功能

* 多级的镜像构建



## docker设置国内加速配置

### 设置DaoCloud的Docker Hub 加速器

* 小公司，现在还可以，速度可以，但是不如 阿里 等大公司稳定

* 可以暂时使用这个，人少，速度快

* Linux 系统使用

  ```shell
  curl -sSL https://get.daocloud.io/daotools/set_mirror.sh | sh -s http://f1361db2.m.daocloud.io
  
  # 会在 /etc/docker/ 文件夹下生成  daemon.json 内容如下
  {"registry-mirrors": ["http://f1361db2.m.daocloud.io"]}
  ```

  * 会生成  daemon.json 文件

* 重新启动docker

  ```shell
  systemctl restart docker
  ```




## 修改Docker文件存储地址

### 默认存储地址

* docker 默认的数据目录是/var/lib/docker，比如要修改到/data/docker/docker_data

### 两种方式

#### 1、创建软连接

* 将 /var/lib/docker 文件移动到 /data/

* 创建软连接

  ```shell
  ln -s 源文件  目标文件
  ln -s /data/docker /var/lib/docker
  ```

* 即 docker 存储地址还是  /var/lib/docker  只不过 /var/lib/docker 只是个镜像文件，通过软连接存储在别的地方

#### 2、修改docker的配置文件

* 转移数据

  ```shell
  mv /var/lib/docker /data
  ```

* 修改启动配置文件

  ```shell
  vim /lib/systemd/system/docker.service
  #修改下行后面加上 --graph /data/docker
  ExecStart=/usr/bin/dockerd -H fd:// --containerd=/run/containerd/containerd.sock --graph /data/docker
  ```



#### 重新启动 docker

```shell
systemctl restart docker
```





### 设置阿里云的docker镜像加速器

* 后续完善时 添加
* 改的话只需要修改 daemon.json 文件中的内容即可



## docker 卸载

### 1、查找主机上关于Docker的软件包

* `rpm -qa | grep docker` – – 列出包含docker字段的软件的信息

  ```shell
  # rpm -qa | grep docker – – 列出包含docker字段的软件的信息
  [root@localhost opt]# rpm -qa | grep docker
  docker-ce-17.03.0.ce-1.el7.centos.x86_64
  docker-ce-selinux-17.03.0.ce-1.el7.centos.noarch
  ```

### 2、使用`yum remove`卸载软件

* 卸载命令

  ```shell
  # yum remove -y docker-ce-17.03.0.ce-1.el7.centos.x86_64
  # yum remove -y docker-ce-selinux-17.03.0.ce-1.el7.centos.noarch
  ```

### 3、查看相关命令

* 查看docker 是否卸载

  ```shell
  rpm -qa | grep docker
  -----
  docker ps
  ```

  