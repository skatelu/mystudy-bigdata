# Docker-Compose

* 编排工具

* Docker 提倡理念是 “**一个容器一个进程**”，假设**一个服务需要由多个进程组成**，就需**要多个容器组成一个系统**，相互分工和配合对外提供完整服务 

* 在启动容器时，同一台主机下如果两个容器之间需要由数据交流，使用 --link 选项建立两个容器之间的互联，前提是建立的 mariadb 已经开启
    docker start db
    docker start MywordPress
  停止：
    docker stop db MywordPress 或 docker stop MywordPress 在 docker top db
* 



