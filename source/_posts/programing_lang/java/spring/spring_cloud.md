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
# 用于客户端gRPC请求服务端口，客户端向服务器发起连接
-p 9848:9848
# 用于服务端gRPC请求服务端口，用于服务间同步  
-p 9849:9849
```

```shell script
docker run -d --name zhong-nacos -e MODE=standalone \
-e TIME_ZONE=Asia/Shanghai \
-p 8848:8848 -p 9848:9848 -p 9849:9849 \
-v /home/nacos/bin:/home/nacos/bin \
nacos/nacos-server
```

配置调整

```shell
docker cp zhong-nacos:/home/nacos/bin/docker-startup.sh /home/nacos/bin/docker-startup.sh
```

{% img /images/pic_nacos_1.jpg %}

### 服务间异步调用 session 处理

{% link '参考blog' https://juejin.cn/post/7101492042898866184 [title] %}

异步调用可以通过 RequestContextHolder 来获取原请求参数并设置至到异步线程中

```java
public class Demo {
    public void call() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        taskExecutor.execute(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            // todo: do something
        });
    }
}
```

如果有过滤器通过token就能完成鉴权的话, 可以利用Feign接口的`@RequestHeader`配置token

```
// WebConfigure中, authenticationFilter 进行token读取 
http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

invoke

```java
public class Simple {
    public void callAsync() {
        String token = getLoginUserInfo().getToken();
        String params = "test";
        executor.execute(() -> asyncFunc(token, params));
    }
}
```

api

```java
@Component
@FeignClient(name = "test-service")
public interface TestService {

    @PostMapping(value = "/test/asyncFunc")
    Response asyncFunc(@RequestHeader(value = "Authorization") String token,
                       @RequestBody String params);
}
```