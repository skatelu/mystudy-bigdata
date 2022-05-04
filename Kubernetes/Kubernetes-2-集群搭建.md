# Kubernetes 集群的搭建（一主二从）

## 1、准备

### 1.1 三台虚拟机、一台安装了ikuai的路由器

* 192.168.66.101
* 192.168.66.102
* 192.168.66.103
* ikuai 路由器

### 1.2 创建虚拟机、并初始化网络信息(此处用的是ikuai，可以直接使用VM自带的VMnet8 创建的网络)

* 创建好三台虚拟机后，由于ikuai上设置的网关是 192.168.66.222 ，因此仅主机模式下，网络段为 192.168.66.0-192.168.66.255

* 设置三台虚拟机的ip地址信息

  * 192.168.66.101

    ```properties
    TYPE=Ethernet
    PROXY_METHOD=none
    BROWSER_ONLY=no
    BOOTPROTO=static
    DEFROUTE=yes
    IPV4_FAILURE_FATAL=no
    IPV6INIT=yes
    IPV6_AUTOCONF=yes
    IPV6_DEFROUTE=yes
    IPV6_FAILURE_FATAL=no
    IPV6_ADDR_GEN_MODE=stable-privacy
    NAME=ens33
    UUID=feb21105-b51c-4467-929b-39ab3c0287a8
    DEVICE=ens33
    ONBOOT=yes
    IPADDR=192.168.66.101
    NETMASK=255.255.255.0
    GATEWAY=192.168.66.222
    DNS1=192.168.66.222
    DNS2=114.114.114.114
    ```

  * 192.168.66.102

    ```properties
    TYPE=Ethernet
    PROXY_METHOD=none
    BROWSER_ONLY=no
    BOOTPROTO=static
    DEFROUTE=yes
    IPV4_FAILURE_FATAL=no
    IPV6INIT=yes
    IPV6_AUTOCONF=yes
    IPV6_DEFROUTE=yes
    IPV6_FAILURE_FATAL=no
    IPV6_ADDR_GEN_MODE=stable-privacy
    NAME=ens33
    UUID=c67b2e86-6e69-4f67-8073-0328bf7f7907
    DEVICE=ens33
    ONBOOT=yes
    IPADDR=192.168.66.102
    NETMASK=255.255.255.0
    GATEWAY=192.168.66.222
    DNS1=192.168.66.222
    DNS2=114.114.114.114
    ```

  * 192.168.66.103

    ```properties
    TYPE=Ethernet
    PROXY_METHOD=none
    BROWSER_ONLY=no
    BOOTPROTO=static
    DEFROUTE=yes
    IPV4_FAILURE_FATAL=no
    IPV6INIT=yes
    IPV6_AUTOCONF=yes
    IPV6_DEFROUTE=yes
    IPV6_FAILURE_FATAL=no
    IPV6_ADDR_GEN_MODE=stable-privacy
    NAME=ens33
    UUID=9bdd0914-4764-46a2-8c60-daf660bcdd02
    DEVICE=ens33
    ONBOOT=yes
    IPADDR=192.168.66.103
    NETMASK=255.255.255.0
    GATEWAY=192.168.66.222
    DNS1=192.168.66.222
    DNS2=114.114.114.114
    ```

* 测试 ping www.baidu.com 可以ping同即可



## 2、系统初始化(每台机器都执行)

### 2.1、设置系统主机名以及 Host 文件的相互解析

* 设置主机名

```shell
# 192.168.66.101
hostnamectl  set-hostname  k8s-master01
# 192.168.66.102
hostnamectl  set-hostname  k8s-node01
# 192.168.66.103
hostnamectl  set-hostname  k8s-node02
```

* 设置host 文件信息(三台机器分贝执行)

  ```shell
  vim /etc/hosts
  ```

  * 添加一下内容

  ```shell
  192.168.66.101  k8s-master01    m1
  192.168.66.102  k8s-node01      n1
  192.168.66.103  k8s-node02      n2
  ```

* 可以在一台机器创建完成后，用scp 命令 转发

  ```shell
  scp /etc/hosts root@192.168.66.102:/etc/hosts
  ```

### 2.2、安装依赖包

* 每台机器都执行

```shell
yum install -y conntrack ntpdate ntp ipvsadm ipset  iptables curl sysstat libseccomp wget  vim net-tools git
```

### 2.3、设置防火墙为 Iptables 并设置空规则

* 每台机器都执行

```shell
# 关闭防火墙
systemctl  stop firewalld  &&  systemctl  disable firewalld
# 安装 iptables
yum -y install iptables-services  &&  systemctl  start iptables  &&  systemctl  enable iptables  &&  iptables -F  &&  service iptables save
```

### 2.4、关闭 SELINUX

* kubernetes 不建议开启交换分区，因为会影响性能

```shell
# 关闭交换分区
swapoff -a && sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab

# 关闭 SELINUX
setenforce 0 && sed -i 's/^SELINUX=.*/SELINUX=disabled/' /etc/selinux/config
```

### 2.5、调整内核参数，对于 K8S

```shell
# 创建 kubernetes.conf 文件
cat > kubernetes.conf <<EOF
net.bridge.bridge-nf-call-iptables=1 # 在ipv4的网络下，所有经过网桥的流量，都必须经过防火墙处理
net.bridge.bridge-nf-call-ip6tables=1 # 在ipv6的网络下，所有经过网桥的流量，都必须经过防火墙处理
net.ipv4.ip_forward=1 # 数据包通过 ipv4 转发
net.ipv4.tcp_tw_recycle=0
vm.swappiness=0 # 禁止使用 swap 空间，只有当系统 OOM 时才允许使用它
vm.overcommit_memory=1 # 不检查物理内存是否够用
vm.panic_on_oom=0 # 开启 OOM	
fs.inotify.max_user_instances=8192
fs.inotify.max_user_watches=1048576
fs.file-max=52706963
fs.nr_open=52706963
net.ipv6.conf.all.disable_ipv6=1
net.netfilter.nf_conntrack_max=2310720
EOF

# 复制到 /etc 文件夹下，每次启动都会执行
cp kubernetes.conf  /etc/sysctl.d/kubernetes.conf

# 在不重启的情况下，使该文件生效
sysctl -p /etc/sysctl.d/kubernetes.conf
```

### 2.6、调整系统时区



```shell
# 设置系统时区为 中国/上海
timedatectl set-timezone Asia/Shanghai
# 将当前的 UTC 时间写入硬件时钟
timedatectl set-local-rtc 0
# 重启依赖于系统时间的服务
systemctl restart rsyslog 
systemctl restart crond
```

* 也可以设置时间同步服务器，自行百度  可以使用 chrony.conf 同步阿里云的时间

*  chrony.conf 主节点配置文件修改地方

  ```properties
  #server 1.centos.pool.ntp.org iburst
  #server 2.centos.pool.ntp.org iburst
  #server 3.centos.pool.ntp.org iburst
  server ntp1.aliyun,com iburst
  server ntp2.aliyun,com iburst
  server ntp3.aliyun,com iburst
  ```

### 2.7、关闭系统不需要服务

```shell
# 这个是邮件服务，可以关闭  因为是最小化安装，禁用这个即可
systemctl stop postfix && systemctl disable postfix
```

### 2.8、设置 rsyslogd 和 systemd journald

```shell
# 持久化保存日志的目录
mkdir /var/log/journal

# 创建相关的目录文件
mkdir /etc/systemd/journald.conf.d

# 编写具体的 linux 日志记录配置文件
cat > /etc/systemd/journald.conf.d/99-prophet.conf <<EOF
[Journal]
# 持久化保存到磁盘
Storage=persistent

# 压缩历史日志
Compress=yes

SyncIntervalSec=5m
RateLimitInterval=30s
RateLimitBurst=1000

# 最大占用空间 10G
SystemMaxUse=10G

# 单日志文件最大 200M
SystemMaxFileSize=200M

# 日志保存时间 2 周
MaxRetentionSec=2week

# 不将日志转发到 syslog
ForwardToSyslog=no
EOF

# 创建配置文件后，使其生效
systemctl restart systemd-journald
```

### 2.9、升级系统内核为 4.44(最好能升级)

* <u>CentOS 7.x 系统自带的 3.10.x 内核存在一些 Bugs，导致运行的 Docker、Kubernetes 不稳定，例如： rpm -Uvh http://www.elrepo.org/elrepo-release-7.0-3.el7.elrepo.noarch.rpm</u>

  ```shell
  # rpm 网络安装，但因为访问不了外网，无法下载，手动安装下载的镜像包
  rpm -Uvh http://www.elrepo.org/elrepo-release-7.0-3.el7.elrepo.noarch.rpm
  # 安装完成后检查 /boot/grub2/grub.cfg 中对应内核 menuentry 中是否包含 initrd16 配置，如果没有，再安装一次！
  yum --enablerepo=elrepo-kernel install -y kernel-lt
  -----------------------------------------------------------------
  ```

  ```shell
  # 但是网络是在国外，下载速度非常慢，所以可以通过下载好的rpm包进行安装
  yum -y install kernel-lt-4.4.222-1.el7.elrepo.x86_64.rpm
  # 查看当前安装的内核文件名称
  cat /boot/grub2/grub.cfg | grep 4.4
  # 设置开机从新内核启动
  grub2-set-default 'CentOS Linux (4.4.222-1.el7.elrepo.x86_64) 7 (Core)'
  ```

  



## 3、Kubernetes的部署（容器的方式安装）

* 因为是国外网站，此处用已经下载好的镜像文件进行安装

### 3.1、kube-proxy开启ipvs的前置条件 

```shell
# 加载网桥 防火墙的连接模块
modprobe br_netfilter

# 创建相关文件
cat > /etc/sysconfig/modules/ipvs.modules <<EOF
#!/bin/bash
modprobe -- ip_vs
modprobe -- ip_vs_rr
modprobe -- ip_vs_wrr
modprobe -- ip_vs_sh
modprobe -- nf_conntrack_ipv4
EOF

# 增加执行权限并执行
chmod 755 /etc/sysconfig/modules/ipvs.modules && bash /etc/sysconfig/modules/ipvs.modules && lsmod | grep -e ip_vs -e nf_conntrack_ipv4
```



### 3.2、安装 Docker 软件

```shell
# 安装当前docker的依赖包
yum install -y yum-utils device-mapper-persistent-data lvm2

# 添加 aliyun的 docker 源
yum-config-manager \
  --add-repo \
  http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
 
# 安装docker-ce版本
yum install -y docker-ce

## 创建 /etc/docker 目录
mkdir /etc/docker

# 配置 daemon. 此处阿里云镜像加速，用的是个人的
cat > /etc/docker/daemon.json <<EOF
{
  "exec-opts": ["native.cgroupdriver=systemd"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m"
  },
  "insecure-registries": ["hub.hongfu.com"],
  "registry-mirrors": ["https://1vs4aqgk.mirror.aliyuncs.com"]
}
EOF

# 设置阿里云镜像加速,设置成自己的或公司的
vim /etc/docker/daemon.json
"registry-mirrors": ["https://1vs4aqgk.mirror.aliyuncs.com"]

# 创建管理脚本
mkdir -p /etc/systemd/system/docker.service.d

# 重启docker服务
systemctl daemon-reload && systemctl restart docker && systemctl enable docker

# 重新启动宿主机
reboot
```

### 3.2、安装 Kubeadm （主从配置）

* 基于这个文件安装 Kubernetes 
* 它是 CNCF 官方，开发的 Kubernetes 安装工具，将当前所有的进程以容器化的方式运行
* 缺点
  * 镜像在国外
  * 集群证书有效期只有一年
    * 集群更新
    * 修改 kubeadm 证书颁发代码
  * 掩盖了部分通讯逻辑

启动流程

* master 
  * systemd -> kubelet -> 容器组件
* 添加 yum 源 三台机器都安装

```shell
cat <<EOF > /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=http://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64
enabled=1
gpgcheck=0
repo_gpgcheck=0
gpgkey=http://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg http://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
EOF

# 安装 Kubernetes 的客户端、kubeadm、kubelet
yum -y  install  kubeadm-1.15.1 kubectl-1.15.1 kubelet-1.15.1
# 开机自启动 kubelet
systemctl enable kubelet.service
```

* 执行完成后，kubelet 在每台机器上已进程的形式 运行着



### 3.3、用脚本，将下载好的镜像文件上传到安装好的docker容器中

* /opt/software/kubeadm-basic.images 为解压后 相关容器组件的文件夹

```shell
vim loadimages.sh

---
#!/bin/bash
ls /opt/software/kubeadm-basic.images > /tmp/images.cache

for i in $( cat /tmp/images.cache )
do 
	docker load -i /opt/software/kubeadm-basic.images/$i
	echo $i
done

rm -rf /tmp/images.cache

# 写入上面内容后，loadimages.sh 添加可执行权限
chmod a+x loadimages.sh
---

# 分发到其它各个节点上
scp -r loadimages.sh kubeadm-basic.images root@n1:/opt/software/
scp -r loadimages.sh kubeadm-basic.images root@n2:/opt/software/
```



### 3.4、初始化主节点

```shell
# 执行该命令，打印文件，即如何去执行主节点 
kubeadm config print init-defaults > kubeadm-config.yaml

# 对文件进行修改
vim kubeadm-config.yaml
	
# 需要修改以下内容	
localAPIEndpoint:#
    advertiseAddress: 192.168.66.101 # 我们主服务器端的地址是多少 ip
    bindPort: 6443 # 当前的端口，外面访问 kubernetes集群使用
 nodeRegistration:
  	criSocket: /var/run/dockershim.sock
 	name: k8s-master01 # 当前的节点名称

kubernetesVersion: v1.15.1 # 当前kubernetes的版本
networking:# 当前kubernetes内部的域名
  dnsDomain: cluster.local
  serviceSubnet: 10.96.0.0/12# 当前的ipvs负载均衡的网段
  podSubnet: "10.244.0.0/16" # 需要添加这段内容,当前的pod网段，不指定会自动生成，因为用flannel 所以得指定这个
---# 增加这些内容 不同的配置文件写在同一个文件中 --- 表示分隔符
# 指定当前负载调用的方式 为 ipvs
apiVersion: kubeproxy.config.k8s.io/v1alpha1
kind: KubeProxyConfiguration
featureGates:
  SupportIPVSProxyMode: true
mode: ipvs


# 修改完成配置文件后，运行下面命令，进行初始化
kubeadm init --config=kubeadm-config.yaml --experimental-upload-certs | tee kubeadm-init.log
```

* 执行上面命令打印的日志信息

  ```latex
  Flag --experimental-upload-certs has been deprecated, use --upload-certs instead
  [init] Using Kubernetes version: v1.15.1 # 告诉我们当前版本
  [preflight] Running pre-flight checks
  	[WARNING SystemVerification]: this Docker version is not on the list of validated versions: 20.10.14. Latest validated version: 18.09
  [preflight] Pulling images required for setting up a Kubernetes cluster
  [preflight] This might take a minute or two, depending on the speed of your internet connection
  # 'kubeadm config images pull' 帮助我们下载所有所需的镜像 和国外的节点上才行，外网
  [preflight] You can also perform this action in beforehand using 'kubeadm config images pull'
  # /var/lib/kubelet/kubeadm-flags.env kubernetes 环境变量配置地址
  [kubelet-start] Writing kubelet environment file with flags to file "/var/lib/kubelet/kubeadm-flags.env"
  # /var/lib/kubelet/config.yaml  当前kubelete 用到的ymal文件 
  [kubelet-start] Writing kubelet configuration to file "/var/lib/kubelet/config.yaml"
  [kubelet-start] Activating the kubelet service
  # 证书存放目录 /etc/kubernetes/pki
  [certs] Using certificateDir folder "/etc/kubernetes/pki"
  [certs] Generating "ca" certificate and key
  [certs] Generating "apiserver" certificate and key
  # apiServer 的访问地址
  [certs] apiserver serving cert is signed for DNS names [k8s-master01 kubernetes kubernetes.default kubernetes.default.svc kubernetes.default.svc.cluster.local] and IPs [10.96.0.1 192.168.66.101]
  [certs] Generating "apiserver-kubelet-client" certificate and key
  [certs] Generating "front-proxy-ca" certificate and key
  [certs] Generating "front-proxy-client" certificate and key
  [certs] Generating "etcd/ca" certificate and key
  [certs] Generating "etcd/peer" certificate and key
  [certs] etcd/peer serving cert is signed for DNS names [k8s-master01 localhost] and IPs [192.168.66.101 127.0.0.1 ::1]
  [certs] Generating "etcd/healthcheck-client" certificate and key
  [certs] Generating "etcd/server" certificate and key
  [certs] etcd/server serving cert is signed for DNS names [k8s-master01 localhost] and IPs [192.168.66.101 127.0.0.1 ::1]
  [certs] Generating "apiserver-etcd-client" certificate and key
  [certs] Generating "sa" key and public key
  # 各个组件的配置文件信息
  [kubeconfig] Using kubeconfig folder "/etc/kubernetes"
  [kubeconfig] Writing "admin.conf" kubeconfig file
  [kubeconfig] Writing "kubelet.conf" kubeconfig file
  [kubeconfig] Writing "controller-manager.conf" kubeconfig file
  [kubeconfig] Writing "scheduler.conf" kubeconfig file
  # /etc/kubernetes/manifests 清单文件目录地址
  [control-plane] Using manifest folder "/etc/kubernetes/manifests"
  [control-plane] Creating static Pod manifest for "kube-apiserver"
  [control-plane] Creating static Pod manifest for "kube-controller-manager"
  [control-plane] Creating static Pod manifest for "kube-scheduler"
  [etcd] Creating static Pod manifest for local etcd in "/etc/kubernetes/manifests"
  [wait-control-plane] Waiting for the kubelet to boot up the control plane as static Pods from directory "/etc/kubernetes/manifests". This can take up to 4m0s
  [apiclient] All control plane components are healthy after 19.506156 seconds
  [upload-config] Storing the configuration used in ConfigMap "kubeadm-config" in the "kube-system" Namespace
  [kubelet] Creating a ConfigMap "kubelet-config-1.15" in namespace kube-system with the configuration for the kubelets in the cluster
  [upload-certs] Storing the certificates in Secret "kubeadm-certs" in the "kube-system" Namespace
  [upload-certs] Using certificate key:
  7ba78240ef9acc6b1a5665e40875a38a469603dd82bde086d3fd3495b6ba5a8f
  [mark-control-plane] Marking the node k8s-master01 as control-plane by adding the label "node-role.kubernetes.io/master=''"
  [mark-control-plane] Marking the node k8s-master01 as control-plane by adding the taints [node-role.kubernetes.io/master:NoSchedule]
  [bootstrap-token] Using token: abcdef.0123456789abcdef
  [bootstrap-token] Configuring bootstrap tokens, cluster-info ConfigMap, RBAC Roles
  [bootstrap-token] configured RBAC rules to allow Node Bootstrap tokens to post CSRs in order for nodes to get long term certificate credentials
  [bootstrap-token] configured RBAC rules to allow the csrapprover controller automatically approve CSRs from a Node Bootstrap Token
  [bootstrap-token] configured RBAC rules to allow certificate rotation for all node client certificates in the cluster
  [bootstrap-token] Creating the "cluster-info" ConfigMap in the "kube-public" namespace
  [addons] Applied essential addon: CoreDNS
  [addons] Applied essential addon: kube-proxy
  
  Your Kubernetes control-plane has initialized successfully!
  
  # 如果我们要使用这个集群，需要执行下面命令
  To start using your cluster, you need to run the following as a regular user:
  
    mkdir -p $HOME/.kube
    sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
    # 设置权限
    sudo chown $(id -u):$(id -g) $HOME/.kube/config
  
  You should now deploy a pod network to the cluster.
  Run "kubectl apply -f [podnetwork].yaml" with one of the options listed at:
    https://kubernetes.io/docs/concepts/cluster-administration/addons/
  
  Then you can join any number of worker nodes by running the following on each as root:
  # 加入其它 node节点，执行下面命令
  kubeadm join 192.168.66.101:6443 --token abcdef.0123456789abcdef \
      --discovery-token-ca-cert-hash sha256:b156d357a1d4902ab14c820b00c4ce9c2a03fcd097d313604493f602bade8518
  ```

### 3.5、加入主节点以及其余工作节点

* 根据上面的日志信息进行执行

  ```shell
  # 如果我们要使用这个集群，需要执行下面命令
  mkdir -p $HOME/.kube
  sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
  # 设置权限
  sudo chown $(id -u):$(id -g) $HOME/.kube/config
  # 其它 node节点加入，执行下面命令,直接在node的宿主机上执行
  kubeadm join 192.168.66.101:6443 --token abcdef.0123456789abcdef \
      --discovery-token-ca-cert-hash sha256:b156d357a1d4902ab14c820b00c4ce9c2a03fcd097d313604493f602bade8518
  ```

### 3.6、安装 flannel 网络（扁平网络）

```shell
# 创建镜像文件目录，因为是外网，直接以镜像包进行安装
mkdir -p /opt/software/kubernetes/install/flannel

# 将镜像安装包导入 并解压，并导入到docker中，注意，在每个节点都得进行导入
tar -zxvf flannel.tar.gz

# 加载到docker环境中
docker load -i flannel.tar

# flannel 应用文件的安装 注意：该条命名只在主节点上执行
kubectl apply -f kube-flannel.yml
```

* 如果能链接外网的话，可以执行该条命令

  ```shell
  kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml
  ```

* 测试 flannel 是否安装成功

  ```shell
  # 查看当前所有运行的pod
  kubectl get pod -n kube-system
  ```

  









