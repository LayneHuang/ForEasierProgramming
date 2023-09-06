---
title: Flink-ML
date: 2023-7-27 21:00:00
categories: [ Flink ]
---

Recently, i need to use k-means algorithm to do something in flink.
And I found that flink-ml library can solve that easily

<!-- more -->

### How to run the demo

{% link 'Quick
Start' https://nightlies.apache.org/flink/flink-ml-docs-master/docs/try-flink-ml/java/quick-start/ [title] %}

`Tips`:

#### Flink Version must be match

#### dependency lib need to add to `/opt/flink/lib` and restart cluster

because i use docker to run flink, and i need `docker commit` cmd to make a new docker image for adding flink-ml
dependency.

```
flink-ml-core-1.17-2.4-SNAPSHOT.jar
flink-ml-lib-1.17-2.4-SNAPSHOT.jar
flink-ml-iteration-1.17-2.4-SNAPSHOT.jar
```

#### run the example

Enter the `jobmanager` container

```shell
./flink run \
-c org.apache.flink.ml.examples.clustering.KMeansExample \
/opt/flink/lib/flink-ml-examples-1.17-2.4-SNAPSHOT.jar
```

{% img /images/pic_flink_ml_example.png %}