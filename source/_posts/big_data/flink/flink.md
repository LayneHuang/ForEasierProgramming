---
title: Flink
date: 2023-3-3 21:00:00
categories: [ Flink ]
---

Flink

<!-- more -->

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

Trigger a savepoint

```shell
bin/flink savepoint :jobId [:targetDir]
```

Cancel job with savepoint

```shell
bin/flink cancel -s [:targetDir] :jobId

```

Resume from savepoint

```shell
bin/flink run -s :savepointPath [:runArgs]
```

Disposing savepoint

```shell
bin/flink savepoint -d :savepointPath
```