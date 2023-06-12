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

{% link 'nacos-k8s官方文档' https://nacos.io/zh-cn/docs/use-nacos-with-kubernetes.html [title] %}
{% link 'nacos集群部署github' https://github.com/nacos-group/nacos-k8s [title] %}