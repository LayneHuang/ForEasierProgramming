---
title: Ack
date: 2023-06-13 21:40:00
categories: [ Kubernetes, AliyunAck ]
---

The process of creating an ack instance

### Create a cluster in ack

1 ACK cluster must cost 2 SLB instance:

1. for ack      (Must Pay by Traffic)
2. for ingress  (Independently purchase and config after cluster created)

1 ACK cluster must cost 3 EIP(elasticity public ip) instance:

1. for ack SLB
2. for ingress SLB
3. for nat (service call the public network service)

### How ingress use existing SLB

{% link 'AliyunDocument' https://help.aliyun.com/document_detail/151506.html?spm=a2c4g.330059.0.0.43625bbfgoKxyy [title] %}

restart nginx-ingress-lb(svc) and nginx-ingress-controller(pod), config the load balance id

```yaml
# nginx ingress slb service
apiVersion: v1
kind: Service
metadata:
  name: nginx-ingress-lb
  namespace: kube-system
  labels:
    app: nginx-ingress-lb
  annotations:
    # 指明SLB实例地址类型为私网类型。
    service.beta.kubernetes.io/alibaba-cloud-loadbalancer-address-type: intranet
    # 修改为您的私网SLB实例ID。
    service.beta.kubernetes.io/alibaba-cloud-loadbalancer-id: <YOUR_INTRANET_SLB_ID>
    # 是否自动创建SLB端口监听（会覆写已有端口监听），也可手动创建端口监听。
    service.beta.kubernetes.io/alibaba-cloud-loadbalancer-force-override-listeners: 'true'
spec:
  type: LoadBalancer
  # route traffic to other nodes
  externalTrafficPolicy: "Cluster"
  ports:
    - port: 80
      name: http
      targetPort: 80
    - port: 443
      name: https
      targetPort: 443
  selector:
    # select app=ingress-nginx pods
    app: ingress-nginx
```