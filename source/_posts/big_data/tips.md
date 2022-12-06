---
title: 大数据框架
date: 2022-12-06 21:00:00
categories: bigdata
---

FROM: https://www.jianshu.com/p/11653dc89478

# Spark vs Flink

```text
Flink是一个流处理系统，采用Dataflow架构。其节点的数据传输方式为，当一条数据被处理完成后，序列化到缓存中，然后立刻通过网络传输到下一个节点，
由下一个节点继续处理（Flink以固定的缓存块，大小设置为0则为纯流）。Spark是批处理系统，其数据节点间的传输方式为，当一条数据被处理完成后，序列化到缓存中，
并不会立刻通过网络传输到下一个节点，当缓存写满，就持久化到本地硬盘上，当所有数据都被处理完成后，才开始将处理后的数据通过网络传输到下一个节点。
所以批处理系统更适合处理吞吐量大的任务，流处理系统适合处理低延时要求的任务。

任务的调度不同，flink 的拓扑图生成提交执行之后（分布到TaskManager的slot中后），除非故障，否则拓扑部件执行位置不变，并行度由每一个算子并行度决定
（每一个算子可以设置自己的并行读），Flink的slot的在TaskManager创建时就已经确定。Spark是构建 DGA 图，划分Stage,生成Taskset,Executor申请Task, 
并根据任务创建线程执行任务。 Flink支持三种时间机制：事件时间，注入时间，处理时间，同时支持 watermark 机制处理滞后数据。Spark Streaming 只支持处理时间，
Structured streaming 支持处理时间和事件时间，同时支持 watermark 机制处理滞后数据。

Flink和Spark虽然都支持Exactly once的语义一致性，但是其原理不同，Spark 使用checkpoint,只能保证数据不丢失，不能做到一致性。
在使用kafka时需，维护offset,同时结果输出和 offset 提交必须在一个事务，才能保证一致性。 Flink使用两阶段提交协议以及预提交(pre-commit)阶段来解决语义一致性。 

Spark与Flink背压不同，Spark Streaming 在原有的架构上加入了一个 RateController， 利用的算法是 PID，需要的反馈数据是任务处理的结束时间、调度时间、处理时间、消息条数，
这些数据是通过 SparkListener 体系获得，然后通过 PIDRateEsimator 的 compute 计算得到一个速率，进而可以计算得到一个 offset，然后跟限速设置最大消费条数比较得到一个最终要消费的消息最大
offset。与 Spark Streaming 的背压不同的是，Flink 背压是 jobmanager 针对每一个 task 每 50ms 触发 100 次
Thread.getStackTrace() 调用，求出阻塞的占比。

```

# Spark 和 Flink 的应用场景

```text
1.Spark 适合于吞吐量比较大的场景，数据量非常大而且逻辑复杂的批数据处理，并且对计算效率有较高要求（比如用大数据分析来构建推荐系统进行个性化推荐、广告定点投放等）。
2.其次，Spark是批处理架构，适合基于历史数据的批处理。最好是具有大量迭代计算场景的批处理。
3.Spark可以支持近实时的流处理，延迟性要求在在数百毫秒到数秒之间。
4.Spark的生态更健全，SQL操作也更加健全，已经存在Spark生态的可以直接使用。
5.Flink 主要用来处理要求低延时的任务，实时监控、实时报表、流数据分析和实时仓库。
6.Flink可以用于事件驱动型应用，数据管道，数据流分析等。
```

