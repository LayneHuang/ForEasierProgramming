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

when us modify savepoints, checkpoints and upload dir position.
we need to make sure the folder has authority to modify.(use cmd `chmod` to deal with that)

### K8S

{% link 'official
document' https://nightlies.apache.org/flink/flink-docs-release-1.17/docs/deployment/resource-providers/standalone/kubernetes/ [title] %}

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
    taskmanager.numberOfTaskSlots: 2
    blob.server.port: 6124
    jobmanager.rpc.port: 6123
    taskmanager.rpc.port: 6122
    queryable-state.proxy.ports: 6125
    jobmanager.memory.process.size: 1600m
    taskmanager.memory.process.size: 1728m
    parallelism.default: 2
    # scheduler-mode: reactive
    execution.checkpointing.enabled: true
    execution.checkpointing.interval: 10s
    execution.checkpointing.externalized-checkpoint-retention: RETAIN_ON_CANCELLATION
    # state.backend: rocksdb
    state.backend.type: rocksdb
    state.checkpoints.num-retained: 1
    state.checkpoints.dir: file:///opt/flink/data/checkpoints
    state.savepoints.dir: file:///opt/flink/data/savepoints
    state.backend.local-recovery: true
    process.taskmanager.working-dir: /opt/flink/data/task
    web.upload.dir: /opt/flink/data/uploadJars
  log4j-console.properties: |+
    # This affects logging for both user code and Flink
    rootLogger.level = INFO
    rootLogger.appenderRef.console.ref = ConsoleAppender
    rootLogger.appenderRef.rolling.ref = RollingFileAppender

    # Uncomment this if you want to _only_ change Flink's logging
    #logger.flink.name = org.apache.flink
    #logger.flink.level = INFO

    # The following lines keep the log level of common libraries/connectors on
    # log level INFO. The root logger does not override this. You have to manually
    # change the log levels here.
    logger.akka.name = akka
    logger.akka.level = INFO
    logger.kafka.name= org.apache.kafka
    logger.kafka.level = INFO
    logger.hadoop.name = org.apache.hadoop
    logger.hadoop.level = INFO
    logger.zookeeper.name = org.apache.zookeeper
    logger.zookeeper.level = INFO

    # Log all infos to the console
    appender.console.name = ConsoleAppender
    appender.console.type = CONSOLE
    appender.console.layout.type = PatternLayout
    appender.console.layout.pattern = %d{yyyy-MM-dd HH:mm:ss,SSS} %-5p %-60c %x - %m%n

    # Log all infos in the given rolling file
    appender.rolling.name = RollingFileAppender
    appender.rolling.type = RollingFile
    appender.rolling.append = false
    appender.rolling.fileName = ${sys:log.file}
    appender.rolling.filePattern = ${sys:log.file}.%i
    appender.rolling.layout.type = PatternLayout
    appender.rolling.layout.pattern = %d{yyyy-MM-dd HH:mm:ss,SSS} %-5p %-60c %x - %m%n
    appender.rolling.policies.type = Policies
    appender.rolling.policies.size.type = SizeBasedTriggeringPolicy
    appender.rolling.policies.size.size=100MB
    appender.rolling.strategy.type = DefaultRolloverStrategy
    appender.rolling.strategy.max = 10

    # Suppress the irrelevant (wrong) warnings from the Netty channel handler
    logger.netty.name = org.jboss.netty.channel.DefaultChannelPipeline
    logger.netty.level = OFF
```

#### Service

```yaml
---
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
---
apiVersion: v1
kind: Service
metadata:
  namespace: my-flink
  name: taskmanager-hl
spec:
  clusterIP: None
  selector:
    app: flink
    component: taskmanager
```

### RABC

```yaml
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: flink
  namespace: my-flink
---
kind: Role
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: flink
  namespace: my-flink
rules:
  - apiGroups: [ "" ]
    resources: [ "endpoints" ]
    verbs: [ "get" ]
  - apiGroups: [ "" ]
    resources: [ "events" ]
    verbs: [ "create" ]
---
kind: RoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: flink
  namespace: my-flink
subjects:
  - kind: ServiceAccount
    name: flink
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: flink
```

### POD

jobmanager

```yaml
apiVersion: apps/v1
kind: StatefulSet
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
      serviceAccount: flink
      serviceAccountName: flink
      securityContext:
        runAsUser: 9999
        fsGroup: 9999
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
            - name: flink-data
              mountPath: /opt/flink/data
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
        - name: flink-data
          persistentVolumeClaim:
            claimName: flink-nas-pvc
```

taskmanager

```yaml
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  namespace: my-flink
  name: flink-taskmanager
spec:
  serviceName: taskmanager-hl
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
      serviceAccount: flink
      serviceAccountName: flink
      securityContext:
        runAsUser: 9999
        fsGroup: 9999
      containers:
        - name: taskmanager
          image: flink:1.17.0-scala_2.12
          env:
            - name: POD_NAME
              valueFrom:
                fieldRef:
                  fieldPath: metadata.name
          args: [ "taskmanager", "-Dtaskmanager.resource-id=$(POD_NAME)" ]
          ports:
            - containerPort: 6122
              name: rpc
            - containerPort: 6125
              name: query-state
            - containerPort: 6121
              name: metrics
          livenessProbe:
            tcpSocket:
              port: 6122
            initialDelaySeconds: 30
            periodSeconds: 60
          volumeMounts:
            - name: flink-config-volume
              mountPath: /opt/flink/conf/
            - name: flink-data
              mountPath: /opt/flink/data
      volumes:
        - name: flink-config-volume
          configMap:
            name: flink-config
            items:
              - key: flink-conf.yaml
                path: flink-conf.yaml
              - key: log4j-console.properties
                path: log4j-console.properties
        - name: flink-data
          persistentVolumeClaim:
            claimName: flink-nas-pvc
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