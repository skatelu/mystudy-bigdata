# Kubernetes-资源清单概念

## 一、K8S 中的资源

* K8s 中所有的内容都抽象为资源， 资源实例化之后，叫做对象

### 1、名称空间级别

* 工作负载型资源： Pod、ReplicaSet、Deployment ...
* 服务发现及负载均衡型资源:  Service、Ingress...
* 配置与存储型资源：Volume、CSI ...
* 特殊类型的存储卷：ConfigMap、Secre ...

### 2、集群级资源

* Namespace、Node、ClusterRole、ClusterRoleBinding

### 3、元数据型资源

* HPA、PodTemplate、LimitRange

## 二、资源清单

* **在 k8s 中，一般使用 yaml 格式的文件来创建符合我们预期期望的 pod （对象），这样的 yaml 文件我们一般称为  资源清单**

* 后续的软件安装，创建，基本上都得使用资源清单进行配置

### 2.1、资源清单格式

* 内容解析

  ```yaml
  apiVersion: group/apiversion  # 如果没有给定 group 名称，那么默认为 core，可以使用 kubectl api-versions # 获取当前 k8s 版本上所有的 apiVersion 版本信息( 每个版本可能不同 )
  kind: pod   #资源类别  pod、RC等 官方建议 写全名
  metadata：  #资源元数据
  	name
  	namespace
  	lables
  	annotations   # 主要目的是方便用户阅读查找
  spec: # 期望的状态（disired state）
  status：# 当前状态，本字段有 Kubernetes 自身维护，用户不能去定义
  ```

  <!--配置清单主要有五个一级字段，其中status用户不能定义，有k8s自身维护-->

  * 判断资源类别需要在哪个路径去创建，不同的资源类别应该安装在哪个位置

    ```shell
    kubectl explain 资源类别
    # 如查看 pod 资源类别 应该安装的位置
    kubectl explain pod
    # 查看 资源类别的 元数据信息
    kubectl explain pod.metadata
    ```

### 2.2、资源清单的常用命令

#### 2.2.1、获取 apiversion 版本信息

* 查看 所有apiversion 版本的信息

  ```shell
  [root@k8s-master01 ~]# kubectl api-versions
  admissionregistration.k8s.io/v1beta1
  apiextensions.k8s.io/v1beta1
  apiregistration.k8s.io/v1
  apiregistration.k8s.io/v1beta1
  apps/v1
  apps/v1beta1
  apps/v1beta2
  ...
  v1 # v1最特殊，其他的 apiversion 版本信息都是 group/apiversion 但v1只有一个，因为省略了核心 core 这个组
  ```

#### 2.2.2、获取资源的 apiVersion 版本信息

* 使用 explain 命令

  ```shell
  [root@k8s-master01 ~]# kubectl explain pod
  KIND:     Pod
  VERSION:  v1
  .....(以下省略)
  
  [root@k8s-master01 ~]# kubectl explain Ingress
  KIND:     Ingress
  VERSION:  extensions/v1beta1
  ```

#### 2.2.3、获取字段设置帮助文档

* 还是explain 关键字

  ```shell
  [root@k8s-master01 ~]# kubectl explain pod
  KIND:     Pod
  VERSION:  v1
  
  DESCRIPTION:
       Pod is a collection of containers that can run on a host. This resource is
       created by clients and scheduled onto hosts.
  
  FIELDS:
     apiVersion    <string>
       ........
       ........
  ```

#### 2.2.4、字段配置格式

* 改关键子端是什么类型，是否必须等

  ```shell
  apiVersion <string>          #表示字符串类型
  metadata <Object>            #表示需要嵌套多层字段
  labels <map[string]string>   #表示由k:v组成的映射
  finalizers <[]string>        #表示字串列表
  ownerReferences <[]Object>   #表示对象列表
  hostPID <boolean>            #布尔类型
  priority <integer>           #整型
  name <string> -required-     #如果类型后面接 -required-，表示为必填字段
  ```

### 2.3、通过定义清单文件创建 Pod

* 下面是一个类似 资源清单的模板

  ```yaml
  apiVersion: v1
  kind: Pod
  metadata:
    name: pod-demo
    namespace: default
    labels:
      app: myapp
  spec:
    containers:
    - name: myapp-1
      image: harbor.hongfu.com/library/myapp:v1
    - name: busybox-1
      image: harbor.hongfu.com/library/busybox:v1
      command:
      - "/bin/sh"
      - "-c"
      - "sleep 3600"
  ```

* shell 命令 用来查看相关 pod的资源清单文件

  ```shell
  kubectl get pod xx.xx.xx -o yaml  
  <!--使用 -o 参数 加 yaml，可以将资源的配置以 yaml的格式输出出来，也可以使用json，输出为json格式--> 
  ```

* <!--资源被创建的流程：Kubectl（Yaml） >>> KubeapiServer（Json）-->





get pod -l app  通过 -l 删选出复合名称的 pod
