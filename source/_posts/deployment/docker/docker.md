---

title: Docker
date: 2021-06-25 16:40:00
categories: [ Docker ]
---

### Command

```shell
### Chceck CPU or memery status
docker stats

### Remove all the images
docker rmi $(docker images -q)

### 运行镜像
docker run [args]
### 容器内部的root拥有外部的root权限
--privileged
### 后台运行
-d
### 时区设置
-e TZ=Asia/Shanghai

### 进入容器
docker exec -it redis /bin/bash
docker exec -it redis sh

### 复制文件
docker cp -a image-name:/filePath /localPath

### 保存镜像到本地
docker save -o output.tar image-name:tag

### 加载本地镜像
docker load -i saved-image.tar

### 登录到 docker 镜像仓库
docker login your-registry-url

### 镜像推送到镜像仓库
docker push image-name:tag

```

### Configure JDK base on CentOS

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

### Build Docker Image By Docker file

```shell
docker build -t [image_name:version] [Dockerfile Path]
```

### Chinese Domestic mirror

add configuration to `/etc/docker/daemon.json`

```json
{
  "registry-mirrors": [
    "https://registry.docker-cn.com",
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.baidubce.com",
    "https://ccr.ccs.tencentyun.com"
  ]
}
```

restart service

```shell
sudo systemctl daemon-reload
sudo systemctl restart docker
```