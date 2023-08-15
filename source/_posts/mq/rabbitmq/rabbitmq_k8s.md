---
title: Rabbit MQ K8S
date: 2023-08-15 21:30:00
categories: [ mq, k8s ]
---

### RABC

```yaml
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: rabbitmq
  namespace: default
---
kind: Role
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: rabbitmq
  namespace: default
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
  name: rabbitmq
  namespace: default
subjects:
  - kind: ServiceAccount
    name: rabbitmq
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: rabbitmq
```

### PVC

Refer to ack `Data Mounting in ACK`, you can create pvc in ack's dashboard

### ConfigMap

```yaml

---
apiVersion: v1
kind: ConfigMap
metadata:
  name: rabbitmq-config
  namespace: rabbit-mq
data:
  enabled_plugins: |
    [rabbitmq_management,rabbitmq_peer_discovery_k8s].
  rabbitmq.conf: |
    cluster_formation.peer_discovery_backend = rabbit_peer_discovery_k8s
    cluster_formation.k8s.host = kubernetes.default.svc.cluster.local
    cluster_formation.k8s.address_type = hostname
    cluster_formation.k8s.hostname_suffix = .rabbitmq-headless.default.svc.cluster.local
    cluster_formation.node_cleanup.interval = 30
    cluster_formation.node_cleanup.only_log_warning = true
    cluster_partition_handling = autoheal
    queue_master_locator=min-masters
    vm_memory_high_watermark.absolute = 1GB
    disk_free_limit.absolute = 2GB
    loopback_users.guest = false
```

### Service

```yaml
---
apiVersion: v1
kind: Service
metadata:
  name: rabbitmq-headless
  namespace: default
spec:
  clusterIP: None
  ports:
    - name: epmd
      port: 4369
      protocol: TCP
      targetPort: 4369
    - name: cluster-links
      port: 25672
      protocol: TCP
      targetPort: 25672
  selector:
    app: rabbitmq
  sessionAffinity: None
  type: ClusterIP

---
apiVersion: v1
kind: Service
metadata:
  name: rabbitmq-external
  namespace: default
spec:
  ports:
    - name: http
      port: 15672
      protocol: TCP
      targetPort: 15672
    - name: amqp
      port: 5672
      protocol: TCP
      targetPort: 5672
  selector:
    app: rabbitmq
  sessionAffinity: None
  type: ClusterIP
```

### POD

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: rabbitmq-cluster
  namespace: default
spec:
  replicas: 1
  selector:
    matchLabels:
      app: rabbitmq
  serviceName: rabbitmq-headless
  template:
    metadata:
      labels:
        app: rabbitmq
    spec:
      volumes:
        - configMap:
            defaultMode: 420
            name: rabbitmq-config
          name: rabbitmq-conf-tmp
        - emptyDir: { }
          name: rabbitmq-conf
        - name: rabbitmq-data
          persistentVolumeClaim:
            claimName: rabbitmq-nas-pvc
      initContainers:
        # Since k8s 1.9.4, config maps mount read-only volumes. Since the Docker image also writes to the config file,
        # the file must be mounted as read-write. We use init containers to copy from the config map read-only
        # path, to a read-write path
        - name: init
          image: registry-cn-shenzhen-vpc.ack.aliyuncs.com/acs/busybox:v1.29.2
          volumeMounts:
            - mountPath: /tmp/rabbitmq
              name: rabbitmq-conf-tmp
            - mountPath: /etc/rabbitmq
              name: rabbitmq-conf
          command:
            - sh
            - -c
            # the newline is needed since the Docker image entrypoint scripts appends to the config file
            - cp /tmp/rabbitmq/rabbitmq.conf /etc/rabbitmq/rabbitmq.conf && echo '' >> /etc/rabbitmq/rabbitmq.conf;
              cp /tmp/rabbitmq/enabled_plugins /etc/rabbitmq/enabled_plugins
      containers:
        - name: rabbitmq
          image: registry.cn-shenzhen.aliyuncs.com/lishanbin/rabbitmq:3.9.10-management
          livenessProbe:
            exec:
              command:
                - rabbitmq-diagnostics
                - status
            failureThreshold: 3
            initialDelaySeconds: 60
            periodSeconds: 60
            successThreshold: 1
            timeoutSeconds: 15
          ports:
            - name: amqp
              containerPort: 5672
              protocol: TCP
            - name: management-http
              containerPort: 15672
              protocol: TCP
            - name: epmd
              containerPort: 4369
              protocol: TCP
          resources:
            limits:
              memory: 512Mi
            requests:
              memory: 256Mi
          env:
            - name: MY_POD_NAME
              valueFrom:
                fieldRef:
                  fieldPath: metadata.name  # get pod.metadata.name, e.g. rabbitmq-cluster-0
            - name: MY_POD_NAMESPACE
              valueFrom:
                fieldRef:
                  fieldPath: metadata.namespace  # get pod.metadata.namespace
            - name: RABBITMQ_DEFAULT_USER
              value: "rabbitmq_root"
            - name: RABBITMQ_DEFAULT_PASS
              value: "JFw21-***-h8"
            - name: RABBITMQ_USE_LONGNAME
              value: "true"
            - name: K8S_SERVICE_NAME
              value: "rabbitmq-headless"
            - name: RABBITMQ_NODENAME
              value: "rabbit@$(MY_POD_NAME).$(K8S_SERVICE_NAME).$(MY_POD_NAMESPACE).svc.cluster.local"
            - name: K8S_HOSTNAME_SUFFIX
              value: .$(K8S_SERVICE_NAME).$(MY_POD_NAMESPACE).svc.cluster.local
            - name: RABBITMQ_ERLANG_COOKIE
              value: "91/rHX2a3GZw3RCHT1Q9y/G0Wo3cbX3qS06DyD4fAUs="    # generator by: echo $(openssl rand -base64 32)
          volumeMounts:
            - mountPath: /etc/rabbitmq
              name: rabbitmq-conf
            - mountPath: /var/lib/rabbitmq/mnesia
              name: rabbitmq-data
```