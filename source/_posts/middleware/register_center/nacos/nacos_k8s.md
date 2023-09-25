---
title: Nacos K8S
date: 2023-08-15 21:30:00
categories: [ nacos, k8s ]
---

### deployment

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

so, we add this 2 configure to --env when we start up images (also can set it to configmap)

```yaml
env:
  - name: NACOS_AUTH_ENABLE
    value: "true"
  - name: NACOS_AUTH_IDENTITY_KEY
    value: "identityKey"
  - name: NACOS_AUTH_IDENTITY_VALUE
    value: "identityValue"
```

### yaml from github

project:

```shell
git clone https://github.com/nacos-group/nacos-k8s.git
```

nacos-quick-start.yaml(add auth config):

the length of identityKey, identityValue must greater than or equal 32 bytes

```yaml
---
apiVersion: v1
kind: Service
metadata:
  name: nacos-headless
  labels:
    app: nacos-headless
spec:
  type: ClusterIP
  clusterIP: None
  ports:
    - port: 8848
      name: server
      targetPort: 8848
    - port: 9848
      name: client-rpc
      targetPort: 9848
    - port: 9849
      name: raft-rpc
      targetPort: 9849
    - port: 7848
      name: old-raft-rpc
      targetPort: 7848
  selector:
    app: nacos
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: nacos-cm
data:
  mysql.host: "mysql"
  mysql.db.name: "nacos_devtest"
  mysql.port: "3306"
  mysql.user: "nacos"
  mysql.password: "nacos"
  auth.enable: "true"
  auth.identity.key: "identityKey"
  auth.identity.value: "identityValue"
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: nacos
spec:
  serviceName: nacos-headless
  replicas: 3
  template:
    metadata:
      labels:
        app: nacos
      annotations:
        pod.alpha.kubernetes.io/initialized: "true"
    spec:
      affinity:
        podAntiAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
            - labelSelector:
                matchExpressions:
                  - key: "app"
                    operator: In
                    values:
                      - nacos
              topologyKey: "kubernetes.io/hostname"
      containers:
        - name: nacos
          imagePullPolicy: Always
          image: nacos/nacos-server:latest
          resources:
            requests:
              memory: "2Gi"
              cpu: "500m"
          ports:
            - containerPort: 8848
              name: client
            - containerPort: 9848
              name: client-rpc
            - containerPort: 9849
              name: raft-rpc
            - containerPort: 7848
              name: old-raft-rpc
          env:
            - name: NACOS_REPLICAS
              value: "3"
            - name: NACOS_AUTH_ENABLE
              valueFrom:
                configMapKeyRef:
                  name: nacos-cm
                  key: auth.enable
            - name: NACOS_AUTH_IDENTITY_KEY
              valueFrom:
                configMapKeyRef:
                  name: nacos-cm
                  key: auth.identity.key
            - name: NACOS_AUTH_IDENTITY_VALUE
              valueFrom:
                configMapKeyRef:
                  name: nacos-cm
                  key: auth.identity.value
            - name: MYSQL_SERVICE_HOST
              valueFrom:
                configMapKeyRef:
                  name: nacos-cm
                  key: mysql.host
            - name: MYSQL_SERVICE_DB_NAME
              valueFrom:
                configMapKeyRef:
                  name: nacos-cm
                  key: mysql.db.name
            - name: MYSQL_SERVICE_PORT
              valueFrom:
                configMapKeyRef:
                  name: nacos-cm
                  key: mysql.port
            - name: MYSQL_SERVICE_USER
              valueFrom:
                configMapKeyRef:
                  name: nacos-cm
                  key: mysql.user
            - name: MYSQL_SERVICE_PASSWORD
              valueFrom:
                configMapKeyRef:
                  name: nacos-cm
                  key: mysql.password
            - name: SPRING_DATASOURCE_PLATFORM
              value: "mysql"
            - name: NACOS_SERVER_PORT
              value: "8848"
            - name: NACOS_APPLICATION_PORT
              value: "8848"
            - name: PREFER_HOST_MODE
              value: "hostname"
            - name: NACOS_SERVERS
              value: "nacos-0.nacos-headless.default.svc.cluster.local:8848 nacos-1.nacos-headless.default.svc.cluster.local:8848 nacos-2.nacos-headless.default.svc.cluster.local:8848"
  selector:
    matchLabels:
      app: nacos
```