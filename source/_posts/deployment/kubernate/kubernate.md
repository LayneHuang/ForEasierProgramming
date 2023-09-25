---
title: Kubernetes Tips
date: 2021-12-06 16:40:00
categories: [ k8s ]
---

### Kubernetes 常用命令

```shell
# 以 yaml 方式创建服务或负载
kubectl -n [namespace] apply -f [service/deployment].yaml
```

### nacos-k8s

use nacos-quick-start.yaml for deployment, and change nacos(mysql config map)

{% link 'nacos-k8s Official Documents' https://nacos.io/zh-cn/docs/use-nacos-with-kubernetes.html [title] %}
{% link 'nacos k8s github' https://github.com/nacos-group/nacos-k8s [title] %}

open nacos dashboard auth:

After version2.2.1 nacos's application.properties will remove default value of auth.
We have to mount config map to application.properties

{% img /images/pic_nacos_k8s_conf.png %}

```yaml
### 开启鉴权
nacos.core.auth.enabled=true

  ### 关闭使用user-agent判断服务端请求并放行鉴权的功能
nacos.core.auth.enable.userAgentAuthWhite=false

  ### 配置自定义身份识别的key（不可为空）和value（不可为空）
nacos.core.auth.server.identity.key=example
nacos.core.auth.server.identity.value=example
```

copy pod's application.properties

```shell
kubectl cp nacos-0:/home/nacos/conf/application.properties /home/nacos/application.properties
```

we can find 2 configure parameter in that:

```yaml
nacos.core.auth.server.identity.key=${NACOS_AUTH_IDENTITY_KEY:}
nacos.core.auth.server.identity.value=${NACOS_AUTH_IDENTITY_VALUE:}
```

so, we add this 2 configure to --env when we start up images

```yaml
env:
  - name: NACOS_AUTH_IDENTITY_KEY
    value: "indentityKey"
  - name: NACOS_AUTH_IDENTITY_VALUE
    value: "indentityValue"
```

### --privileged=true

when container need file modify auth (as the same usage of --privileged=true in docker)

```yaml
containers:
  - image: xxx:1.0.0
    securityContext:
      privileged: true
```

`defaultMode: 493` = `chmod 755` in linux

```yaml
- configMap:
    defaultMode: 493
    items:
      - key: emqx_web_hook.conf
        path: emqx_web_hook.conf
    name: my-emqx-conf
  name: emqx-plugins
```

### How to pull your private images(/etc/containerd/config.toml)

{% link 'nacos k8s github' https://github.com/nacos-group/nacos-k8s [title] %}

```
[plugins."io.containerd.grpc.v1.cri".registry]
  [plugins."io.containerd.grpc.v1.cri".registry.mirrors]
    # domestic
    [plugins."io.containerd.grpc.v1.cri".registry.mirrors."docker.io"]
      endpoint = ["http://registry-vpc.cn-hangzhou.aliyuncs.com"]
    # private
    [plugins."io.containerd.grpc.v1.cri".registry.mirrors."your_host:port"]
      endpoint = ["http://your_host:port"]
  [plugins."io.containerd.grpc.v1.cri".registry.configs]i1
    [plugins."io.containerd.grpc.v1.cri".registry.configs."your_host:port".tls]
      insecure_skip_verify = true
```

restart conatinerd

```shell
systemctl restart containerd
systemctl status containerd

```

### serviceAccount & securityContext

securityContext: Identity required for DevOps to enter the console
serviceAccount: the process in pods use it.

when securityContext is `{}` and serviceAccount is `rabbitmq`
{% img /images/pic_k8s_1.png %}

{% img /images/pic_k8s_2.png %}