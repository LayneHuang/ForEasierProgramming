---
title: opencv
date: 2022-11-21 21:00:00
tag: opencv
---

{% link 'docs' https://opencv-java-tutorials.readthedocs.io/en/latest/ [title] %}  
{% link 'blog' https://blog.csdn.net/qq_37131111/article/details/126588443 [title] %}

### Dockerfile

docker 下部署 opencv 环境, 具备cmake, opencv, jdk三个包...

```shell
FROM centos:centos7

# JDK
RUN mkdir /usr/local/java
ADD jdk-8u341-linux-x64.tar.gz /usr/local/java
RUN ln -s /usr/local/java/jdk1.8.0_341 /usr/local/java/jdk

ENV JAVA_HOME /usr/local/java/jdk
ENV JRE_HOME ${JAVA_HOME}/jre
ENV CLASSPATH .:${JAVA_HOME}/lib:${JRE_HOME}/lib
ENV PATH ${JAVA_HOME}/bin:$PATH

# opencv
WORKDIR /home
ADD opencv-4.6.0.zip /home
ADD cmake-3.22.0-rc3.tar.gz /home
RUN yum -y install gcc gcc-c++ openssl openssl-devel tar ant unzip zip
WORKDIR /home/cmake-3.22.0-rc3
RUN ./bootstrap --prefix=/usr --datadir=share/cmake --docdir=doc/cmake && \
    make -j8 && \
    make install
WORKDIR /home
RUN unzip opencv-4.6.0.zip
WORKDIR /home/opencv-4.6.0
RUN mkdir build
WORKDIR /home/opencv-4.6.0/build
RUN rm -f CMakeCache.txt && \
    cmake -D WITH_TBB=ON -D WITH_EIGEN=ON .. && \
    make -j8 &&\
    make install
```
