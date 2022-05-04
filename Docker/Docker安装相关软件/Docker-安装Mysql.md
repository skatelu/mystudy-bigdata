# Docker-安装Mysql

## 下载Mysql 镜像 此处下载的是 8.0

```shell
docker pull mysql:8.0
```

## 安装运行Docker实例

### 创建容器卷、运行实例、设置密码、端口等

```shell
docker run -d -p 3306:3306 \
--privileged=true \
-v /opt/mysql/log:/var/log/mysql \
-v /opt/mysql/data:/var/lib/mysql \
-v /opt/mysql/conf:/etc/mysql/conf.d \
-e MYSQL_ROOT_PASSWORD=123456 \
--name mysql mysql:8.0
```

### 创建配置文件，设置字符集

```properties
[client]
default_character_set=utf8mb4
[mysql]
default-character-set = utf8mb4
[mysqld]
collation_server = utf8mb4_general_ci
character_set_server = utf8mb4
```

### 重启mysql容器，查看字符集，注意得在服务器端看，客户端看是没用的

```shell
# 重新启动 mysql 容器
docker restart mysql
---
# 进入容器查看mysql，字符集
docker exec -it 027c2da05f34 /bin/bash
root@027c2da05f34:/# mysql -uroot -p
---
mysql> show variables like 'character%';
+--------------------------+--------------------------------+
| Variable_name            | Value                          |
+--------------------------+--------------------------------+
| character_set_client     | utf8mb4                        |
| character_set_connection | utf8mb4                        |
| character_set_database   | utf8mb4                        |
| character_set_filesystem | binary                         |
| character_set_results    | utf8mb4                        |
| character_set_server     | utf8mb4                        |
| character_set_system     | utf8mb3                        |
| character_sets_dir       | /usr/share/mysql-8.0/charsets/ |
+--------------------------+--------------------------------+
8 rows in set (0.01 sec)

mysql> 

```

### 安装完成



