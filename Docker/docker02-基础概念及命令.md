# docker 基础概念

* Docker 三个重要概念：**仓库 (Repository)、镜像 (image) 和 容器 (Container)**











# docker 基础指令

## Docker 指令的基本用法：

* **docker + 命令关键字(COMMAND) + 一系列的参数**

### docker info		守护进程的系统资源设置

### docker search		Docker 仓库的查询

### docker pull		Docker 仓库的下载

### docker images		Docker 镜像的查询

### docker rmi		Docker	镜像的删除

### docker ps		容器的查询

### docker run		容器的创建启动

* docker run 后面跟的参数信息

  ```shell
  --restart=always   		容器的自动启动
  -h x.xx.xx	 		设置容器主机名
  --dns xx.xx.xx.xx	 		设置容器使用的 DNS 服务器
  --dns-search			DNS 搜索设置
  --add-host hostname:IP		注入 hostname <> IP 解析
  --rm				服务停止时自动删除    
  --name test		容器别名，不指定的话自动分配
  -p 30084:80 			指定端口信息
  -d			放在后台运行
  
  --link db:mysql		会将db作为别名，注入到hosts文件进行解析，链接 mysql容器 		
  ```

### docker start/stop/restart	容器启动停止

### # Docker 指令除了单条使用外，还支持赋值、解析变量、嵌套使用



## Docker 单一容器管理

* **每个容器被创建后，都会分配一个 CONTAINER ID 作为容器的唯一标示，后续对容器的启动、停止、修改、删除等所有操作，都是通过 CONTAINER ID 来完成，偏向于数据库概念中的主键**

### docker ps --no-trunc				查看

### docker stop/start CONTAINERID 		停止

### docker start/stop MywordPress 			通过容器别名启动/停止

### docker inspect MywordPress   			查看容器所有基本信息

### docker logs MywordPress  			查看容器日志

* 查看到的是容器前台输出的信息

### docker stats MywordPress  			查看容器所占用的系统资源

### docker exec 容器名 容器内执行的命令  		容器执行命令

### docker exec -it 容器名 /bin/bash  			登入容器的bash



