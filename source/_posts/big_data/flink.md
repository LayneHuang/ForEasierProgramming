---
title: Flink
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
flink:1.16.0-scala_2.12 taskmanage
```

### maven dependencies

```xml

<dependencies>
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-scala_2.12</artifactId>
        <version>1.10.1</version>
    </dependency>
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-streaming-scala_2.12</artifactId>
        <version>1.10.1</version>
    </dependency>
</dependencies>
```

### Checkpoint

Base on `Chandy-Lamport` algorithm (Asynchronous Barrier Snapshotting technique) to save `State` to ensure Fault
Tolerance.

### Savepoint

After i modify my Flink code and resubmit the job, Flink will generate a new JobID for us.
We can use savepoint to reload the previous data in our new Job.