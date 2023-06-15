---
title: Kubernetes Tips
date: 2021-12-06 16:40:00
categories: [ Kubernetes ]
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