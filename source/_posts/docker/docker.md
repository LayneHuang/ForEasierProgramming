---
title: Docker tips
date: 2021-06-25 16:40:00
categories: Docker
---

### Docker常用命令

删除所有镜像

```shell
docker rmi $(docker images -q)
```

运行镜像

```shell
docker run [args]
```

`--privileged` 容器内部的root拥有外部的root权限
`-d` 后台运行

进入容器

```shell
docker exec -it redis /bin/bash
docker exec -it redis sh
```

复制文件

```shell
docker cp -a emqx:/opt/emqx/etc/plugins/emqx_web_hook.conf emqx_web_hook.conf
```

时区

```shell
-e TZ=Asia/Shanghai
```

### 以 centos 为基础并设置 jdk

```shell
FROM centos:centos7

RUN mkdir /usr/local/java
ADD jdk-8u341-linux-x64.tar.gz /usr/local/java
RUN ln -s /usr/local/java/jdk1.8.0_341 /usr/local/java/jdk

ENV JAVA_HOME /usr/local/java/jdk
ENV JRE_HOME ${JAVA_HOME}/jre
ENV CLASSPATH .:${JAVA_HOME}/lib:${JRE_HOME}/lib
ENV PATH ${JAVA_HOME}/bin:$PATH
```

### 通过 Dockerfile 构建镜像

```shell
docker build -t [image_name:version] [Dockerfile Path]
```


# 服务器环境配置

### nginx

{% link '指南' https://www.cnblogs.com/tod4/p/16659260.html [title] %}