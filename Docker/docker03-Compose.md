# Docker-Compose

* 编排工具

* Docker 提倡理念是 “**一个容器一个进程**”，假设**一个服务需要由多个进程组成**，就需**要多个容器组成一个系统**，相互分工和配合对外提供完整服务 

* 在启动容器时，同一台主机下如果两个容器之间需要由数据交流，使用 --link 选项建立两个容器之间的互联，前提是建立的 mariadb 已经开启
    docker start db
    docker start MywordPress
  停止：
    docker stop db MywordPress 或 docker stop MywordPress 在 docker top db
* 

## 一、基本语法

```shell
version: '2'
services:
  web:
    image: dockercloud/hello-world
    ports:
      - 8080
    networks:
      - front-tier
      - back-tier
 
  redis:
    image: redis
    links:
      - web
    networks:
      - back-tier
 
  lb:
    image: dockercloud/haproxy
    ports:
      - 80:80
    links:
      - web
    networks:
      - front-tier
      - back-tier
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock 
 
networks:
  front-tier:
    driver: bridge
  back-tier:
driver: bridge
```

### 1.1、image

```yaml
# services 定义一组服务 
services:
  web:
    image: hello-world
```

```yaml
# 镜像可用格式
image: redis
image: ubuntu:14.04
image: tutum/influxdb
image: example-registry.com:4000/postgresql
image: a4bc65fd
```



### 1.2、build

* 服务除了可以基于指定的镜像，还可以基于一份 Dockerfile，在使用 up 启动之时执行构建任务，这个构建标签就是 build，它可以指定 Dockerfile 所在文件夹的路径。Compose 将会利用它自动构建这个镜像，然后使用这个镜像启动服务容器

```yaml
build: /path/to/build/dir
```

* 也可以是相对路径，只要上下文确定就可以读取到 Dockerfile

  ```yaml
  build: ./dir
  ```

  ```yaml
  # DockerFile 创建对象，并且用 image 定义的名称命名次镜像
  build:
    context: ../
    dockerfile: path/of/Dockerfile
     args:
      buildno: 1
      password: secret
  image: webapp:tag
  ```

### 1.3、command

```yaml
# == 指定镜像启动的命令
command: [bundle, exec, thin, -p, 3000]
```

### 1.4、container_name：<项目名称><服务名称><序号>

```yaml
container_name: app
```





## 二、安装

```shell
curl -L https://github.com/docker/compose/releases/download/2.4.1/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
```



### 手动安装

* 官网上下载docker-compose linux x86_64 文件

* 修改名称为 docker-compose 并放到 /usr/local/bin 目录下 添加可执行权限 chmod a+x docker-compose

* ```shell
  docker-compose version # 查看docker-compose版本信息
  ```

* 

### docker-Compose 命令

* 启动一个新的命令

  ```shell
  # up 启动一个新的项目  会在当前目录下寻找 docker-compose.yaml 文件
  docker-compose up -d 
  ```

* 常用命令

```shell
-f			# 指定使用的 yaml 文件位置		注意，需要放在 子命令之前
ps			# 显示所有容器信息			
restart	    # 重新启动容器				
logs	    # 查看日志信息	所有容器的前台信息日志，按照时间，合并输出
config -q   # 验证 yaml 配置文件是否正确
stop	    # 停止容器
start	    # 启动容器  注意该命令会判断当前容器状态，是否已经启动，如果已经启动，则不需要重新启动
up -d	    # 启动容器项目（创建新项目时使用）
pause	    # 暂停容器
unpause	    # 恢复暂停
rm	    	# 删除容器
```









