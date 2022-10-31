---
title: EMQ X
date: 2022-10-11 21:30:00
categories: mqtt
---

用于设备与服务器通讯的一个消息队列协议

{% link '开源broker选型相关blog' https://www.jianshu.com/p/3e33adcb0ed5 [title] %}

{% link '常用配置相关参考blog' https://blog.csdn.net/weixin_36292503/article/details/121696714 [title] %}

<!-- more -->

最后使用的服务器 broker 是 EMQ X。    
Client 可以下载 MQTT X。  
{% link 'MQTT X' https://mqttx.app/ [title] %}

EMQ X有相关的 Dashboard，会比直接在后台使用命令行方便。  
地址: {host}:18083  
{% img /images/pic_emqx_1.png %}

### EMQ X Docker 部署

{% link '参考blog' https://blog.csdn.net/lzsm_/article/details/125307471 [title] %}

先从 docker 中拉镜像

```shell
docker pull emqx/emqx:4.4.4
```

运行镜像(最后参数为镜像ID)

```shell
docker run -d --name emqx -p 1883:1883 -p 8081:8081 -p 8083:8083 -p 8084:8084 -p 8883:8883 -p 18083:18083 68440db9c488
```

### Spring 相关接入

maven依赖

```xml

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-integration</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-stream</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-mqtt</artifactId>
    </dependency>
</dependencies>
```

### WebHook接入

服务器需要管理设备上下线状态。

{% link '参考blog' https://blog.csdn.net/An1090239782/article/details/124817608 [title] %}

先把容器中 etc/plugins/emqx_web_hook.conf copy 出来

```shell
docker cp -a emqx:/opt/emqx/etc/plugins/emqx_web_hook.conf emqx_web_hook.conf
```

编辑配置, 将上下线的打开

{% img /images/pic_emqx_2.png %}

```conf
web.hook.url = http://localhost/service/mqtt/webhook

web.hook.rule.client.connect.1       = {"action": "on_client_connect"}
#web.hook.rule.client.connack.1       = {"action": "on_client_connack"}
#web.hook.rule.client.connected.1     = {"action": "on_client_connected"}
web.hook.rule.client.disconnected.1  = {"action": "on_client_disconnected"}
#web.hook.rule.client.subscribe.1     = {"action": "on_client_subscribe"}
#web.hook.rule.client.unsubscribe.1   = {"action": "on_client_unsubscribe"}
#web.hook.rule.session.subscribed.1   = {"action": "on_session_subscribed"}
#web.hook.rule.session.unsubscribed.1 = {"action": "on_session_unsubscribed"}
#web.hook.rule.session.terminated.1   = {"action": "on_session_terminated"}
#web.hook.rule.message.publish.1      = {"action": "on_message_publish"}
#web.hook.rule.message.delivered.1    = {"action": "on_message_delivered"}
#web.hook.rule.message.acked.1        = {"action": "on_message_acked"}

```

最后在 dashboard 中启动 webhook (暂不知道能否在配置文件中开启)

### 修改 dashboard 密码

```shell
./bin/emqx_ctl admins passwd admin 123456
```