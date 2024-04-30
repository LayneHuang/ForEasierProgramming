---
title: Flink
date: 2023-3-3 21:00:00
categories: [ Flink ]
---

Flink

<!-- more -->

### Maven dependencies

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

you must config the follow parameter in flink-conf.yaml.
otherwise, checkpoints will be deleted after JobManager restart or shutdown

```yaml
execution.checkpointing.externalized-checkpoint-retention: RETAIN_ON_CANCELLATION
```

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

`tips`:
You need to pay attention to the `Parallelism` you had set in that job (and run with that args).
Otherwise, it will restart failure by savepoint.

### Security for Flink Dashboard

Flink haven't security authentication itself. only can use nginx

``` conf
server {
    listen 80;
    server_name example.com;

    location / {
        auth_basic "Restricted Access";
        auth_basic_user_file "/path/to/passwords";
    }
}
```

or you can disable submit page of Flink

```yaml
web.submit.enable: false
```

### Least Data Sum (seem like LRU)

if some keys have no data arrive in source, how can we trigger the SlidingWindows(EventTime) to finish the calculation ?

1. Watermark's function `withIdleness()` can help us to trigger calculation **once**. But it can't open the
   window in next range.
2. Use `registerEventTimeTimer()` and overwrite `onTimer()` to generate a **empty** element to trigger new window. 
