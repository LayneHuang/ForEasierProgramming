---
title: Rabbit MQ
date: 2023-06-02 21:30:00
categories: [ mq ]
---

### Exchange Type

Direct Exchange(Default), you don't need to config routing key.

Fanout Exchange, messages are broadcasted to all the queues that are bound to it. it doesn't use routing key to decide
which queue to use.

Topic Exchange, message are routed base on wildcard matching of routing key. The routing key can consist of several
words separated by dots. (* and # can be use)

Headers Exchange, message are routed base on the message header attribute value instead of routing keys. The headers
attribute is a dictionary containing arbitrary key-value pair that describe the message.

### One Service (Multiple instance) how to use Fanout

#### One of the Service will receive the msg

determine the queue's name

```java
public class Config {

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value= "#{QueueName}"),
                    exchange = @Exchange(value = "#{ExchangeName}", type = ExchangeTypes.FANOUT)
            )
    )
    public void solve() {

    }
}
```

#### All Services receive the same msg at the same time

@Queue will random gen a queue for spring service (if you not config attribute "name")

```java
public class Config {

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue,
                    exchange = @Exchange(value = "#{ExchangeName}", type = ExchangeTypes.FANOUT)
            )
    )
    public void solve() {

    }
}
```

{% img /images/pic_rabbitmq_1.png %}

