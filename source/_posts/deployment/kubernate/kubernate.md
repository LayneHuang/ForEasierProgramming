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

### Problem: Back-off restarting failed container in pod

Add the follow cmd in your pod, ensure pod will not stop soon after container started

```yaml
command: [ "/bin/bash", "-ce", "tail -f /dev/null" ]
```

another way:

```yaml
command:
  - /bin/bash
  - '-ce'
  - tail -f /dev/null
```


