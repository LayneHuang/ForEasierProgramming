---
title: EMQ X
date: 2022-10-11 21:30:00
categories: [ mq ]
---

用于设备与服务器通讯的一个消息队列协议

{% link 'emqx official blog' https://www.emqx.com/zh/mqtt [title] %}

{% link 'emqx official docs(v5.0)' https://www.emqx.io/docs/zh/v5.0 [title] %}


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

### Security

```shell
/opt/emqx/etc/plugins/emqx_auth_mnesia.conf
```

{% link 'EMQX Auth
Design' https://docs.emqx.com/zh/enterprise/v3.0/auth.html#mqtt-%E8%AE%A4%E8%AF%81%E8%AE%BE%E8%AE%A1 [title] %}

modify emqx.conf

```txt
allow_anonymous = false
```

configure username and password

```text
## Password hash.
##
## Value: plain | md5 | sha | sha256 | sha512
auth.mnesia.password_hash = sha256

##--------------------------------------------------------------------
## ClientId Authentication
##--------------------------------------------------------------------

## Examples
##auth.client.1.clientid = id
##auth.client.1.password = passwd
##auth.client.2.clientid = dev:devid
##auth.client.2.password = passwd2
##auth.client.3.clientid = app:appid
##auth.client.3.password = passwd3
##auth.client.4.clientid = client~!@#$%^&*()_+
##auth.client.4.password = passwd~!@#$%^&*()_+

##--------------------------------------------------------------------
## Username Authentication
##--------------------------------------------------------------------

## Examples:
##auth.user.1.username = admin
##auth.user.1.password = public
##auth.user.2.username = feng@emqtt.io
##auth.user.2.password = public
##auth.user.3.username = name~!@#$%^&*()_+
##auth.user.3.password = pwsswd~!@#$%^&*()_+
```

open the plugins in dashboard

{% img /images/pic_emqx_plugins_auth.png %}

### Deploy in ack

you can use helm in ack to deploy the EMQX cluster.

1. In ack cloudshell, add hlem's emqx chats, and update it

```shell
helm repo add emqx https://repos.emqx.io/charts
helm repo update
```

```shell

helm repo add emqx https://repos.emqx.io/charts
helm repo update

helm install emqx emqx/emqx --set service.type=NodePort

kubectl apply -f emqx-acl-cm.yaml
## kubectl get cm emqx-env -o yaml > emqx-env.yaml
kubectl apply -f emqx-env.yaml

## kubectl get statefulset emqx -o yaml > emqx-statefulset.yaml
kubectl apply -f emqx-statefulset.yaml
```

emqx-acl-cm.yaml

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: emqx-acl-cm
  namespace: my-emqx
data:
  acl.conf: |-
    {allow, {username, {re, "^dashboard$"}}, subscribe, ["$SYS/#"]}.
    {allow, {ipaddr, "127.0.0.1"}, all, ["$SYS/#", "#"]}.
    {deny, all, subscribe, ["$SYS/#", {eq, "#"}]}.
    {allow, all}.
```