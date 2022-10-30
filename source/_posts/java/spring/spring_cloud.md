---
title: Spring Cloud
date: 2022-10-13 21:00:00
categories: Spring
---

Spring Cloud Alibaba 各组件接入

<!-- more -->

## 服务发现 Nacos Discovery

### Nacos Server

下载后, 打开 nacos/bin

Linux/Unix/Mac

```shell
sh startup.sh -m standalone

```

Windows

```shell
startup.cmd -m standalone
```

{% link '
服务接入' https://github.com/alibaba/spring-cloud-alibaba/blob/2021.x/spring-cloud-alibaba-examples/nacos-example/nacos-discovery-example/readme-zh.md [title]
%}

{% link 'nacos dashboard' localhost:8848/nacos/ %}

初始账号密码: nacos/nacos

### maven依赖

```xml

<dependencies>
    <!--应用监控-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <!--服务发现-->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
</dependencies>
```

### docker 

在 docker 中运行也主要要加上
```shell
MODE=standalone
```

同时要多暴露2个端口
```shell
9848
9849
```


### Dubbo接入

