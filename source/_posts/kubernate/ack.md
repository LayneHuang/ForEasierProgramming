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

{% link '
AliyunDocument' https://help.aliyun.com/document_detail/151506.html?spm=a2c4g.330059.0.0.43625bbfgoKxyy [title] %}

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

### Expose your port in SLB

{% link 'EMQX deployment simple' https://blog.csdn.net/emqx_broker/article/details/111029898 [title] %}

ingress use `--tcp-services-configmap` and `--udp-services-configmap` to mapping port.

{% img /images/pic_ack_tcp_services.png %}

nginx-ingress-controller yaml config:

```yaml
containers:
  - args:
      - '--tcp-services-configmap=$(POD_NAMESPACE)/tcp-services'
      - '--udp-services-configmap=$(POD_NAMESPACE)/udp-services'

```

nginx-ingress-lb yaml config:

```yaml
apiVersion: v1
kind: Service
metadata:
  labels:
    app: nginx-ingress-lb
  name: nginx-ingress-lb
  namespace: kube-system
spec:
  type: LoadBalancer
  ports:
    - name: http
      port: 80
      protocol: TCP
      targetPort: 80
    - name: https
      port: 443
      protocol: TCP
      targetPort: 443
    - name: emqx-tcp
      port: 1883
      protocol: TCP
      targetPort: 1883
  selector:
    app: ingress-nginx
```

Finally, Restart your pod(nginx-ingress-controller), and check the status in SLB

### Configmap mounting

Finally, ChatGPT teach me to use ConfigMap's subPath for mounting

Soft link(default):

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: my-pod
spec:
  containers:
    - name: my-container
      image: my-image
      volumeMounts:
        - name: oss-volume
          mountPath: /etc/config
  volumes:
    - name: oss-volume
      configMap:
        name: oss-config
        items:
          - key: config.yaml
            path: config.yaml
```

File replace：

If you want to obtain the actual configuration file inside the container instead of a soft
link, You can achieve this by adding the `subPath` attribute in the yaml file of the pod.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: my-pod
spec:
  containers:
    - name: my-container
      image: my-image
      volumeMounts:
        - name: oss-volume
          mountPath: /etc/config/config.yaml
          subPath: config.yaml
  volumes:
    - name: oss-volume
      configMap:
        name: oss-config
```

### Data Mounting in ACK

when i deploy the RabbitMQ StatefulSet in ACK , the pod need to do `chmod` operation in `/var/lib/rabbitmq` file.
you can't mount it to OSS(the file mounting in OSS not support ServiceAccount to modify its attributes).

FAE told me to use NAS instead of OSS. (But it's still not working, event use initContainer
to `chmod 777 /var/lib/rabbitmq`)

Finally, NAS can firstly mount to ECS, i change file attribute in ECS, and work.

{% img /images/pic_ack_1.png %}

run `chmod` in ECS node, And RabbitMQ start successfully.

{% img /images/pic_ack_2.png %}

### front end no-cache load pic from the same url

in oss, we can get the temp url just like

```
http://host/path?param1=aaa&param2=bbb
```

and we can only add the timestamp after url, front will not cache it

```
param.setUrl(param.getUrl() + "&timestamp=" + System.currentTimeMillis());
```