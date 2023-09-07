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
    execution.checkpointing.interval: 1min
    execution.checkpointing.max-concurrent-checkpoints: 1
    execution.checkpointing.min-pause: 0
    execution.checkpointing.mode: AT_LEAST_ONCE
    execution.checkpointing.timeout: 1min
    execution.checkpointing.tolerable-failed-checkpoints: 0
    execution.checkpointing.unaligned: false
    state.checkpoints.dir: file:///data/checkpoint
  log4j-console.properties: |+
    # Allows this configuration to be modified at runtime. The file will be checked every 30 seconds.
    monitorInterval=30
    # This affects logging for both user code and Flink
    rootLogger.level = INFO
    rootLogger.appenderRef.console.ref = ConsoleAppender
    rootLogger.appenderRef.rolling.ref = RollingFileAppender
    logger.akka.name = akka
    logger.akka.level = INFO
    logger.kafka.name= org.apache.kafka
    logger.kafka.level = INFO
    logger.hadoop.name = org.apache.hadoop
    logger.hadoop.level = INFO
    logger.zookeeper.name = org.apache.zookeeper
    logger.zookeeper.level = INFO
    logger.shaded_zookeeper.name = org.apache.flink.shaded.zookeeper3
    logger.shaded_zookeeper.level = INFO
    # Log all infos to the console
    appender.console.name = ConsoleAppender
    appender.console.type = CONSOLE
    appender.console.layout.type = PatternLayout
    appender.console.layout.pattern = %d{yyyy-MM-dd HH:mm:ss,SSS} %-5p %-60c %x - %m%n
    # Log all infos in the given rolling file
    appender.rolling.name = RollingFileAppender
    appender.rolling.type = RollingFile
    appender.rolling.append = true
    appender.rolling.fileName = ${sys:log.file}
    appender.rolling.filePattern = ${sys:log.file}.%i
    appender.rolling.layout.type = PatternLayout
    appender.rolling.layout.pattern = %d{yyyy-MM-dd HH:mm:ss,SSS} %-5p %-60c %x - %m%n
    appender.rolling.policies.type = Policies
    appender.rolling.policies.size.type = SizeBasedTriggeringPolicy
    appender.rolling.policies.size.size=100MB
    appender.rolling.policies.startup.type = OnStartupTriggeringPolicy
    appender.rolling.strategy.type = DefaultRolloverStrategy
    appender.rolling.strategy.max = ${env:MAX_LOG_FILE_NUMBER:-10}
    # Suppress the irrelevant (wrong) warnings from the Netty channel handler
    logger.netty.name = org.jboss.netty.channel.DefaultChannelPipeline
    logger.netty.level = OFF
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
          args: [ "jobmanager" ]
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
              - key: log4j-console.properties
                path: log4j-console.properties
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
          args: [ "taskmanager" ]
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
              - key: log4j-console.properties
                path: log4j-console.properties
```

### ingress

upload size limit
```yaml
  annotations:
    # nginx.ingress.kubernetes.io/cors-allow-headers: >-
      #lang,DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization
    # nginx.ingress.kubernetes.io/cors-allow-methods: 'PUT,GET,POST,OPTIONS'
    # nginx.ingress.kubernetes.io/cors-allow-origin: '*'
    # nginx.ingress.kubernetes.io/enable-cors: 'true'
    nginx.ingress.kubernetes.io/proxy-body-size: 500m
    nginx.ingress.kubernetes.io/proxy-max-temp-file-size: 1024m
    # nginx.ingress.kubernetes.io/rewrite-target: /$2
    # nginx.ingress.kubernetes.io/service-weight: ''
```