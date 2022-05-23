[TOC]

# DockerFile

## 语法

#### FROM（指定基础 image）

构建指令，必须指定且需要在 Dockerfile 其他指令的前面。后续的指令都依赖于该指令指定的 image。FROM 指令指定的基础 image 可以是官方远程仓库中的，也可以位于本地仓库

```dockerfile
FROM centos:7.2
FROM centos
```



#### MAINTAINER（用来指定镜像创建者信息）

构建指令，用于将 image 的制作者相关的信息写入到 image 中。当我们对该 image 执行 docker inspect 命令时，输出中有相应的字段记录该信息。

```dockerfile
MAINTAINER  wangyang "wangyang@itxdl.cn"
```

​		

#### LABEL（将元数据添加到镜像）

##### 标签的定义

```dockerfile
# 指令将元数据添加到镜像。`LABEL` 是键值对。要在 `LABEL` 值中包含空格，请像在命令行中一样使用引号和反斜杠
LABEL "com.example.vendor"="ACME Incorporated"
LABEL com.example.label-with-value="foo"
LABEL version="1.0"
LABEL description="This text illustrates \
that label-values can span multiple lines."

# 多行标签定义方式
LABEL multi.label1="value1" multi.label2="value2" other="value3"
LABEL multi.label1="value1" \
      multi.label2="value2" \
      other="value3"
```



##### 标签的继承

基础或父镜像（`FROM` 行中的镜像）中包含的标签由您的镜像继承。如果标签已经存在但具有不同的值，则最近应用的值将覆盖任何先前设置的值



##### 查看镜像标签

```shell
$ docker image inspect --format='' myimage
```



##### 案例

```dockerfile
# dockerfile
FROM busybox
LABEL author=wangyanglinux
CMD echo wangyanglinux

# 构建镜像
[root@k8s-master01 testd]# docker build -t wangyanglinux:0.0.1 . --no-cache

# 安装 jq
[root@k8s-master01 testd]# yum install epel-release
[root@k8s-master01 testd]# yum install jq

# 查看标签
[root@k8s-master01 testd]# docker image inspect wangyanglinux:0.0.1 --format "{{json .ContainerConfig.Labels}}" | jq
{
  "author": "wangyanglinux"
}
```





#### RUN（安装软件用）

构建指令，RUN 可以运行任何被基础 image 支持的命令。如基础 image 选择了 Centos，那么软件管理部分只能使用 Centos 的包管理命令

```dockerfile
RUN cd /tmp && curl -L 'http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.8/bin/apache-tomcat-7.0.8.tar.gz' | tar -xz 
RUN ["/bin/bash", "-c", "echo hello"]
```



#### USER（设置container容器的用户）

设置指令，设置启动容器的用户，默认是 root 用户

```dockerfile
USER daemon  =  ENTRYPOINT ["memcached", "-u", "daemon"]  
```



#### EXPOSE（指定容器需要映射到宿主机器的端口）

设置指令，该指令会将容器中的端口映射成宿主机器中的某个端口。当你需要访问容器的时候，可以不是用容器的 IP 地址而是使用宿主机器的 IP 地址和映射后的端口。要完成整个操作需要两个步骤，首先在 Dockerfile 使用 EXPOSE 设置需要映射的容器端口，然后在运行容器的时候指定 -P 选项加上 EXPOSE 设置的端口，这样 EXPOSE 设置的端口号会被随机映射成宿主机器中的一个端口号。也可以指定需要映射到宿主机器的那个端口，这时要确保宿主机器上的端口号没有被使用。EXPOSE指令可以一次设置多个端口号，相应的运行容器的时候，可以配套的多次使用 -p 选项

```dockerfile
# 映射多个端口  
EXPOSE port1 port2 port3  

# 随机暴露需要运行的端口
docker run -P image

# 相应的运行容器使用的命令  
docker run -p port1 -p port2 -p port3 image  

# 还可以指定需要映射到宿主机器上的某个端口号  
docker run -p host_port1:port1 -p host_port2:port2 -p host_port3:port3 image  
```



#### ENV（用于设置环境变量）

构建指令，在 image 中设置一个环境变量。设置了后，后续的 RUN 命令都可以使用，container 启动后，可以通过 docker inspect 查看这个环境变量，也可以通过在 docker run --env key=value 时设置或修改环境变量。假如你安装了 JAVA 程序，需要设置 JAVA_HOME，那么可以在 Dockerfile 中这样写：

```dockerfile
ENV JAVA_HOME /path/to/java/dirent
```



#### ARG（设置变量）

##### 起作用的时机

- arg 是在 build 的时候存在的, 可以在 Dockerfile 中当做变量来使用
- env 是容器构建好之后的环境变量, 不能在 Dockerfile 中当参数使用



##### 案例

```dockerfile
# Dockerfile
FROM redis:3.2-alpine

LABEL maintainer="wangyanglinux@163.com"

ARG REDIS_SET_PASSWORD=developer
ENV REDIS_PASSWORD ${REDIS_SET_PASSWORD}

VOLUME /data

EXPOSE 6379

CMD ["sh", "-c", "exec redis-server --requirepass \"$REDIS_PASSWORD\""]
```



```dockerfile
FROM nginx:1.13.1-alpine

LABEL maintainer="wangyanglinux@163.com"

RUN mkdir -p /etc/nginx/cert \
    && mkdir -p /etc/nginx/conf.d \
    && mkdir -p /etc/nginx/sites

COPY ./nginx.conf /etc/ngixn/nginx.conf
COPY ./conf.d/ /etc/nginx/conf.d/
COPY ./cert/ /etc/nginx/cert/

COPY ./sites /etc/nginx/sites/


ARG PHP_UPSTREAM_CONTAINER=php-fpm
ARG PHP_UPSTREAM_PORT=9000
RUN echo "upstream php-upstream { server ${PHP_UPSTREAM_CONTAINER}:${PHP_UPSTREAM_PORT}; }" > /etc/nginx/conf.d/upstream.conf

VOLUME ["/var/log/nginx", "/var/www"]

WORKDIR /usr/share/nginx/html
```

<!--这里的变量用的就是 `ARG` 而不是 `ENV`了,因为这条命令运行在 `Dockerfile` 当中的, 像这种临时使用一下的变量没必要存环境变量的值就很适合使用 `ARG`-->



#### ADD（从 src 复制文件到 container 的 dest 路径）

```dockerfile
ADD <src> <dest>  
		<src> 是相对被构建的源目录的相对路径，可以是文件或目录的路径，也可以是一个远程的文件 url;
		<dest> 是 container 中的绝对路径
```



#### COPY （从 src 复制文件到 container 的 dest 路径）

```dockerfile
COPY <src> <dest> 
```



#### VOLUME（指定挂载点）

设置指令，使容器中的一个目录具有持久化存储数据的功能，该目录可以被容器本身使用，也可以共享给其他容器使用。我们知道容器使用的是 AUFS，这种文件系统不能持久化数据，当容器关闭后，所有的更改都会丢失。当容器中的应用有持久化数据的需求时可以在 Dockerfile中 使用该指令

```dockerfile
FROM base  
VOLUME ["/tmp/data"]  
```



#### WORKDIR（切换目录）

设置指令，可以多次切换(相当于cd命令)，对RUN,CMD,ENTRYPOINT生效

```dockerfile
WORKDIR /p1   
RUN touch a.txt
WORKDIR p2 
RUN vim a.txt
```



#### CMD（设置 container 启动时执行的操作）

设置指令，用于 container 启动时指定的操作。该操作可以是执行自定义脚本，也可以是执行系统命令。该指令只能在文件中存在一次，如果有多个，则只执行最后一条

```dockerfile
CMD echo “Hello, World!” 

CMD /usr/local/nginx/sbin/nginx  tail -f /usr/local/nginx/logs/access.log
```



#### ENTRYPOINT（设置container启动时执行的操作）

设置指令，指定容器启动时执行的命令，可以多次设置，但是只有最后一个有效。

```dockerfile
ENTRYPOINT ls -l 
```



该指令的使用分为两种情况，一种是独自使用，另一种和 CMD 指令配合使用。当独自使用时，如果你还使用了 CMD 命令且 CMD 是一个完整的可执行的命令，那么 CMD 指令和 ENTRYPOINT 会互相覆盖只有最后一个 CMD 或者 ENTRYPOINT 有效

```dockerfile
CMD echo “Hello, World!” 
ENTRYPOINT ls -l
# CMD 指令将不会被执行，只有 ENTRYPOINT 指令被执行 
ENTRYPOINT ls -l
CMD echo “Hello, World!” 
```



另一种用法和 CMD 指令配合使用来指定 ENTRYPOINT 的默认参数，这时 CMD 指令不是一个完整的可执行命令，仅仅是参数部分；ENTRYPOINT 指令只能使用 JSON 方式指定执行命令，而不能指定参数

```dockerfile
FROM ubuntu  
CMD ["-l"]  
ENTRYPOINT ["/usr/bin/ls"]
```



#### CMD 与 ENTRYPOINT 的较量

官方释义：https://docs.docker.com/engine/reference/builder/#cmd

cmd 给出的是一个容器的默认的可执行体。也就是容器启动以后，默认的执行的命令

```dockerfile
FROM centos
CMD echo "hello cmd!"

docker run xx 
==> hello cmd!

# 如果我们在 run 时指定了命令或者有entrypoint，那么 cmd 就会被覆盖。仍然是上面的 image。run 命令变了：
docker run xx echo glgl
==> glgl
```



cmd 是默认体系，entrypoint 是正统地用于定义容器启动以后的执行体

```dockerfile
FROM centos 
CMD ["p in cmd"]
ENTRYPOINT ["echo"]


[root@k8s-master01 testd]# docker run --name test1  ent:v1
p in cmd
[root@k8s-master01 testd]# docker run --name test2 ent:v1  p in run
p in run


# ENTRYPOINT shell 模式任何 run 和 cmd 的参数都无法被传入到 entrypoint 里。官网推荐第一种用法
FROM centos
CMD ["p in cmd"]
ENTRYPOINT echo
```



#### ONBUILD（在子镜像中执行）

ONBUILD 指定的命令在构建镜像时并不执行，而是在它的子镜像中执行

```dockerfile
ONBUILD ADD . /app/src
ONBUILD RUN /usr/local/bin/python-build --dir /app/src
```



#### STOPSIGNAL signal

STOPSIGNAL 指令设置将发送到容器以退出的系统调用信号。这个信号可以是一个有效的无符号数字，与内核的`syscall`表中的位置相匹配，例如`9`，或者是`SIGNAME`格式的信号名，例如：SIGKILL

```shell
SIGHUP 1 A 终端挂起或者控制进程终止

SIGINT 2 A 键盘中断（如break键被按下）

SIGQUIT 3 C 键盘的退出键被按下

SIGILL 4 C 非法指令

SIGABRT 6 C 由abort(3)发出的退出指令

SIGFPE 8 C 浮点异常

SIGKILL 9 AEF Kill信号

SIGSEGV 11 C 无效的内存引用

SIGPIPE 13 A 管道破裂: 写一个没有读端口的管道

SIGALRM 14 A 由alarm(2)发出的信号

SIGTERM 15 A 终止信号

SIGUSR1 30,10,16 A 用户自定义信号1

SIGUSR2 31,12,17 A 用户自定义信号2

SIGCHLD 20,17,18 B 子进程结束信号

SIGCONT 19,18,25 进程继续（曾被停止的进程）

SIGSTOP 17,19,23 DEF 终止进程

SIGTSTP 18,20,24 D 控制终端（tty）上按下停止键

SIGTTIN 21,21,26 D 后台进程企图从控制终端读

SIGTTOU 22,22,27 D 后台进程企图从控制终端写
```

​	

#### SHELL （覆盖命令的shell模式所使用的默认 shell）

Linux 的默认shell是 [“/bin/sh”, “-c”]，Windows 的是 [“cmd”, “/S”, “/C”]。SHELL 指令必须以 JSON 格式编写。SHELL 指令在有两个常用的且不太相同的本地 shell:cmd 和 powershell，以及可选的 sh 的 windows 上特别有用



#### HEALTHCHECK （容器健康状况检查命令）	

```dockerfile
HEALTHCHECK [OPTIONS] CMD command
	[OPTIONS] 的选项支持以下三中选项
		--interval=DURATION 两次检查默认的时间间隔为 30 秒
    --timeout=DURATION 健康检查命令运行超时时长，默认 30 秒
    --retries=N 当连续失败指定次数后，则容器被认为是不健康的，状态为 unhealthy，默认次数是3
    
	CMD后边的命令的返回值决定了本次健康检查是否成功，具体的返回值如下：
		0: success - 表示容器是健康的
		1: unhealthy - 表示容器已经不能工作了
		2: reserved - 保留值
    
HEALTHCHECK NONE

# 第一个的功能是在容器内部运行一个命令来检查容器的健康状况
# 第二个的功能是在基础镜像中取消健康检查命令
# 为了帮助调试失败的探测，command 写在 stdout 或 stderr 上的任何输出文本（UTF-8编码）都将存储在健康状态中，并且可以通过 docker inspect 进行查询。 这样的输出应该保持简短（目前只存储前4096个字节）
```



##### 注意

HEALTHCHECK 命令只能出现一次，如果出现了多次，只有最后一个生效



##### 模板

```dockerfile
HEALTHCHECK --interval=5m --timeout=3s \
CMD curl -f http://localhost/ || exit 1
```



##### 查看容器的健康状态

```shell
$ docker inspect –format ‘{{json .State.Health.Status}}’ cID
```





## 构建DockerFile镜像

### 相关构建命令

* --build-arg VERSION=1.3.8-SNAPSHOT 给DockerFile 的文件中的变量赋值

```shell
docker build --build-arg VERSION=1.3.8-SNAPSHOT -t apache/dolphinscheduler:1.3.8-SNAPSHOT .
```

