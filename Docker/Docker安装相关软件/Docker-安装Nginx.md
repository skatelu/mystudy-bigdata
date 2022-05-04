# Docker 安装Nginx

## 首先从网络上下载镜像

* 这里安装是 stable 稳定版本

  ```shell
  docker pull nginx:stable
  ---
  docker images # 查看镜像文件
  ```

  

## 安装Docker-Nginx 并设置端口为 38210 并将容器卷映射出来

```shell
docker run -p 38210:80 \
--restart=always \
--name nginx-zhzt \
--privileged=true \
-v /opt/nginx/html:/usr/share/nginx/html:ro \
-v /opt/nginx/conf/nginx.conf:/etc/nginx/nginx.conf:ro \
-v /opt/nginx/logs:/var/log/nginx \
-d nginx:stable

```

