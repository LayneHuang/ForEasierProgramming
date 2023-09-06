---
title: Flink Deployment
date: 2023-3-3 21:00:00
categories: [ Flink ]
---

Deployment of Flink

<!-- more -->

### Docker

```shell
docker pull flink:1.16.0-scala_2.12
```

create docker sub net

```shell
docker network create flink-network
```

```shell
docker run -d \
--rm \
--name=jobmanager \
--network flink-network \
-p 9081:8081 \
-e FLINK_PROPERTIES="jobmanager.rpc.address: jobmanager" \
flink:1.16.0-scala_2.12 jobmanager

docker run -d \
--rm \
--name=taskmanager \
--network flink-network \
-e FLINK_PROPERTIES="jobmanager.rpc.address: jobmanager" \
flink:1.16.0-scala_2.12 taskmanager
```

### K8S

#### ConfigMap

default `flink-conf.yaml` in docker image

```yaml
jobmanager.rpc.address: jobmanager
jobmanager.rpc.port: 6123
jobmanager.bind-host: 0.0.0.0
jobmanager.memory.process.size: 1600m
taskmanager.bind-host: 0.0.0.0
taskmanager.memory.process.size: 2048m
taskmanager.numberOfTaskSlots: 2
parallelism.default: 2
jobmanager.execution.failover-strategy: region
rest.address: 0.0.0.0
rest.bind-address: 0.0.0.0
blob.server.port: 6124
query.server.port: 6125
```

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  namespace: my-flink
  name: flink-config
  labels:
    app: flink
data:
  flink-conf.yaml: |+
    jobmanager.rpc.address: jobmanager-service
    jobmanager.rpc.port: 6123
    jobmanager.bind-host: 0.0.0.0
    jobmanager.memory.process.size: 1600m
    taskmanager.bind-host: 0.0.0.0
    taskmanager.memory.process.size: 2048m
    taskmanager.numberOfTaskSlots: 2
    parallelism.default: 2
    jobmanager.execution.failover-strategy: region
    rest.address: 0.0.0.0
    rest.bind-address: 0.0.0.0
    blob.server.port: 6124
    query.server.port: 6125
```

#### Service

```yaml

apiVersion: v1
kind: Service
metadata:
  namespace: my-flink
  name: jobmanager-service
spec:
  type: ClusterIP
  ports:
    - name: rpc
      port: 6123
    - name: blob-server
      port: 6124
    - name: webui
      port: 8081
  selector:
    app: flink
    component: jobmanager
```

### POD

jobmanager

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  namespace: my-flink
  name: flink-jobmanager
spec:
  replicas: 1
  selector:
    matchLabels:
      app: flink
      component: jobmanager
  template:
    metadata:
      labels:
        app: flink
        component: jobmanager
    spec:
      containers:
        - name: jobmanager
          image: flink:1.17.0-scala_2.12
          args: ["jobmanager"]
          ports:
            - containerPort: 6123
              name: rpc
            - containerPort: 6124
              name: blob-server
            - containerPort: 8081
              name: webui
          livenessProbe:
            tcpSocket:
              port: 6123
            initialDelaySeconds: 30
            periodSeconds: 60
          volumeMounts:
            - name: flink-config-volume
              mountPath: /opt/flink/conf
          securityContext:
            runAsUser: 9999
      volumes:
        - name: flink-config-volume
          configMap:
            name: flink-config
            items:
              - key: flink-conf.yaml
                path: flink-conf.yaml
```

taskmanager

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: flink-taskmanager
spec:
  replicas: 2
  selector:
    matchLabels:
      app: flink
      component: taskmanager
  template:
    metadata:
      labels:
        app: flink
        component: taskmanager
    spec:
      containers:
      - name: taskmanager
        image: flink:1.17.0-scala_2.12
        args: ["taskmanager"]
        ports:
        - containerPort: 6122
          name: rpc
        - containerPort: 6125
          name: query-state
        livenessProbe:
          tcpSocket:
            port: 6122
          initialDelaySeconds: 30
          periodSeconds: 60
        volumeMounts:
        - name: flink-config-volume
          mountPath: /opt/flink/conf/
        securityContext:
          runAsUser: 9999
      volumes:
      - name: flink-config-volume
        configMap:
          name: flink-config
          items:
          - key: flink-conf.yaml
            path: flink-conf.yaml
```