---
title: 设计模式之美 (结构型)
date: 2020-04-02 01:46:00
categories: [ DesignPatterns ]
---

## 代理模式
代理模式的原理和代码实现都不难掌握。  
它在不改变原始类（或称为被代理类）代码的情况下，通过引入代理类给原始类附加功能。  
（类似 React 的 middleware， 高阶组件HOC的写法 ）

### 代理模式原理解析
在之前计数器例子的前提下，开发一个MetricsController类，用来收集接口请求的原始数据（如访问时间、处理时长等）。
```java
public class UserController {
    //...省略其他属性和方法...
    private MetricsCollector metricsCollector; // 依赖注入

    public UserVo login(String telephone, String password) {
        long startTimestamp = System.currentTimeMillis();

        // ... 省略login逻辑...

        long endTimeStamp = System.currentTimeMillis();
        long responseTime = endTimeStamp - startTimestamp;
        RequestInfo requestInfo = new RequestInfo("login", responseTime, startTimestamp);
        metricsCollector.recordRequest(requestInfo);

        //...返回UserVo数据...
    }

    public UserVo register(String telephone, String password) {
        long startTimestamp = System.currentTimeMillis();

        // ... 省略register逻辑...

        long endTimeStamp = System.currentTimeMillis();
        long responseTime = endTimeStamp - startTimestamp;
        RequestInfo requestInfo = new RequestInfo("register", responseTime, startTimestamp);
        metricsCollector.recordRequest(requestInfo);

        //...返回UserVo数据...
    }
}
```
很明显，上面的写法有2个问题：  
1.性能计算器框架代码侵入到业务代码中，跟业务代码高度耦合。如果未来需要替换这个框架，那替换成本会比较大。  
2.收集接口请求的代码跟业务代码无关，本就本应该放在一个类中。业务类最好职责更单一，只聚焦业务处理。  

为了将框架代码和业务代码解耦，代理模式就派上用场了。  
代理类 UserControllerProxy 和原始类 UserController 实现相同的接口 IUserController。  
UserController 只负责业务功能，代理类 UserControllerProxy 负责在业务代码执行前后附加其他逻辑代码，并通过**委托的方式**执行业务代码。  
具体代码：
```java
public interface IUserController {
    UserVo login(String telephone, String password);
    UserVo register(String telephone, String password);
}

public class UserController implements IUserController {
    //...省略其他属性和方法...

    @Override
    public UserVo login(String telephone, String password) {
        //...省略login逻辑...
        //...返回UserVo数据...
    }

    @Override
    public UserVo register(String telephone, String password) {
        //...省略register逻辑...
        //...返回UserVo数据...
    }
}

public class UserControllerProxy implements IUserController {
    private MetricsCollector metricsCollector;
    private UserController userController;

    public UserControllerProxy(UserController userController) {
        this.userController = userController;
        this.metricsCollector = new MetricsCollector();
    }

    @Override
    public UserVo login(String telephone, String password) {
        long startTimestamp = System.currentTimeMillis();

        // 委托
        UserVo userVo = userController.login(telephone, password);

        long endTimeStamp = System.currentTimeMillis();
        long responseTime = endTimeStamp - startTimestamp;
        RequestInfo requestInfo = new RequestInfo("login", responseTime, startTimestamp);
        metricsCollector.recordRequest(requestInfo);

        return userVo;
    }

    @Override
    public UserVo register(String telephone, String password) {
        long startTimestamp = System.currentTimeMillis();

        UserVo userVo = userController.register(telephone, password);

        long endTimeStamp = System.currentTimeMillis();
        long responseTime = endTimeStamp - startTimestamp;
        RequestInfo requestInfo = new RequestInfo("register", responseTime, startTimestamp);
        metricsCollector.recordRequest(requestInfo);

        return userVo;
    }
}

// UserControllerProxy使用举例
// 因为原始类和代理类实现相同的接口，是基于接口而非实现编程
// 将UserController类对象替换为UserControllerProxy类对象，不需要改动太多代码
// IUserController userController = new UserControllerProxy(new UserController());
```
参照基于接口而非实现编程的设计思想，将原始类对象替代为代理类对象的时候，为了让代码改动尽量少，
在刚刚的代理模式的代码实现中，代理类和原始类需要实现相同的接口。但是如果原始类并没有定义接口，
并且原始类代码并不是我们开发维护的（它来自一个第三方库），**我们也没办法直接修改原始类给他重新定义一个接口。**  

对于这种外部类的扩展，我们一般采用继承的方式。让代理类继承原始类，然后扩展功能。
```java
public class UserControllerProxy extends UserController {
  private MetricsCollector metricsCollector;

  public UserControllerProxy() {
    this.metricsCollector = new MetricsCollector();
  }

  public UserVo login(String telephone, String password) {
    long startTimestamp = System.currentTimeMillis();

    UserVo userVo = super.login(telephone, password);

    long endTimeStamp = System.currentTimeMillis();
    long responseTime = endTimeStamp - startTimestamp;
    RequestInfo requestInfo = new RequestInfo("login", responseTime, startTimestamp);
    metricsCollector.recordRequest(requestInfo);

    return userVo;
  }

  public UserVo register(String telephone, String password) {
    long startTimestamp = System.currentTimeMillis();

    UserVo userVo = super.register(telephone, password);

    long endTimeStamp = System.currentTimeMillis();
    long responseTime = endTimeStamp - startTimestamp;
    RequestInfo requestInfo = new RequestInfo("register", responseTime, startTimestamp);
    metricsCollector.recordRequest(requestInfo);

    return userVo;
  }
}

// UserControllerProxy使用举例
// UserController userController = new UserControllerProxy();
```

### 动态代理（Dynamic Proxy）
如果有许多要附加的功能的原始类是，代理类的数量会成倍增加，增加代码维护成本。  
并且每个代理类中的代码都有点模板式的“重复代码”。  

动态代理，我们不事先为每个原始类编写代理类，而是在运行的时候，动态地创建原始类对应的代理类。  
对于Java语言，**利用Java的反射语法**即可实现。（理解写在代码注释中）
```java
public class MetricsCollectorProxy {
    private MetricsCollector metricsCollector;

    public MetricsCollectorProxy() {
        this.metricsCollector = new MetricsCollector();
    }

    public Object createProxy(Object proxiedObject) {
        Class<?>[] interfaces = proxiedObject.getClass().getInterfaces();
        DynamicProxyHandler handler = new DynamicProxyHandler(proxiedObject);
        return Proxy.newProxyInstance(proxiedObject.getClass().getClassLoader(), interfaces, handler);
    }

    private class DynamicProxyHandler implements InvocationHandler {
        private Object proxiedObject;

        public DynamicProxyHandler(Object proxiedObject) {
            this.proxiedObject = proxiedObject;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            long startTimestamp = System.currentTimeMillis();
            Object result = method.invoke(proxiedObject, args);
            long endTimeStamp = System.currentTimeMillis();
            long responseTime = endTimeStamp - startTimestamp;
            String apiName = proxiedObject.getClass().getName() + ":" + method.getName();
            RequestInfo requestInfo = new RequestInfo(apiName, responseTime, startTimestamp);
            metricsCollector.recordRequest(requestInfo);
            return result;
        }
    }
}

// MetricsCollectorProxy使用举例
// MetricsCollectorProxy proxy = new MetricsCollectorProxy();
// IUserController userController = (IUserController) proxy.createProxy(new UserController());
```
实际上，Spring AOP 底层就是基于动态代理，用户配置好需要给哪些类创建代理，
并定义好在执行原始类前后执行哪些附加功能。Spring 为这些类创建动态代理对象，**并在JVM中替代原始类对象**（有点高级）。
（是时候了解一下 JAVA 反射相关的语法拉~）

### 代理模式的应用场景
#### 业务系统的非功能性需求开发
比如：监控、统计、鉴权、限流、事务、幂等、日志。（好像Spring框架或者微服务框架都会有）
如果你熟悉 Java语言 和 Spring开发框架，这部分工作都是可以在Spring AOP切面中完成的。  

#### 代理模式在RPC、缓存中的应用
实际上，RPC 框架也可以看做一种代理模式，GoF的《设计模式》一书中把它称作远程代理。
通过远程代理，将网络通信、数据解码等细节隐藏起来。客户端在使用RPC服务的时候，
就像调用本地函数一样，无需了解跟服务器交互的细节。

#### 代理模式在缓存中的应用
假设我们要开发一个接口请求的缓存功能，对于某些接口请求，如果入参相同，在设定的过期时间内，
直接返回缓存结果，而不是重新进行逻辑处理。比如，针对获取个人用户信息的需求，我们可以开发两个接口，
一个支持缓存，一个支持实时查询。  
最简单的方式就是，给每个接口开发两个不同的接口。但这样做，显然增加了开发成本，而且会让代码看起来非常臃肿。
也不方便缓存接口的集中管理（增加、删除缓存接口）、集中配置（比如配置每个接口缓存过期时间）。  
如果是基于Spring框架来开发的话，就可以在 AOP 切面中完成接口缓存功能。
在启动的时候，我们从配置文件中加载需要支持缓存的接口，以及相应的缓存策略（比如过期时间）等。
当请求到来的时候，我们再 AOP 切面中拦截请求，如果请求中带有支持缓存字段，就从缓存中取（内存或Redis），然后直接返回。

## 装饰器模式
动态地将责任附加到对象上。想要扩展功能，装饰器模式提供有别于继承的另一种选择。

### 装饰器的应用场景
Java IO 类库非常庞大和复杂（我一开始使用的时候确实也搞不太懂），有十几个类，负责IO数据的读取和写入。  
可以按照两个维度划分为4类：

| |**字节流** |**字符流** |
|:---|:---|:---|
|**读入流**|InputSteam|Reader|
|**输出流**|OutputSteam|Writer|

**特殊点：**  

1.装饰器类和原始类继承相同的父类（或者实现相同的接口），这样我们可以对原始类“嵌套”多个装饰器类。  

2.装饰器类是对功能的增强，这也是装饰器模式的一个重要特点。  
实际上，符合“组合关系”这种结构的设计模式很多，如代理模式，桥接模式，还有现在的装饰器模式。
尽管代码结构相似，但是其意图还是不同的。
**代理模式附加的是跟原始类无关的功能，而装饰器模式中，附加的是跟原始类相关的增强功能。**

### BufferedInputStream
看查JDK的源码发现，BufferedInputStream、DataInputStream 是继承自 FilterInputStream 的（而并非 InputSteam）。  
InputStream 它本来就是抽象类而非接口，一些默认方法（read()、available()）都有默认实现。  
但是对于一些不需要增加缓存的函数来说，BufferedInputStream 都要对其重新实现一次。
```java
public class BufferedInputStream extends InputStream {
    protected volatile InputStream in;

    protected BufferedInputStream(InputStream in) {
        this.in = in;
    }

    // f()函数不需要增强，只是重新调用一下InputStream in对象的f()
    public void f() {
        in.f();
    }
}
```
所以 Java IO 抽象出一个装饰器父类 FilterInputStream 把 InputStream 的抽象方法实现了，
那么 BufferedInputStream 就只需要实现它需要增强的方法就可以了。

## 享元模式
享元模式的意图是复用对象，节省内存，前提是享元对象是**不可变对象**。  
当一个系统存在大量重复对象，如果这些对象是不可变对象，我们就可以利用享元模式将对象设计成享元。  
专栏给出的例子是**象棋（享元）**和很多状态的棋盘。  

### 享元模式在Integer、String中的应用
基本类型有一个自动装箱（autoboxing）和自动拆箱（Unboxing）的过程（这个自己了解把）。  
下面这段代码真的很帮助理解
```java
Integer i1 = 56;
Integer i2 = 56;
Integer i3 = 129;
Integer i4 = 129;
System.out.println(i1 == i2); // true
System.out.println(i3 == i4); // false
System.out.println(i3 == 129); // true

System.out.println(new Integer(129) == 129); // true , 应该是自动拆箱了
System.out.println(Integer.valueOf(129) == 129); // true

System.out.println(new Integer(129) == new Integer(129)); // false 在堆上
System.out.println(Integer.valueOf(129) == Integer.valueOf(129)); // false 缓冲区上无值

System.out.println(new Integer(56) == new Integer(56)); // false 在堆上
System.out.println(Integer.valueOf(56) == Integer.valueOf(56)); // treu 缓冲区上有值
```
对于上面的代码，第一个输出返回的是 true，而第二个输出返回的是 false 。  
原因是Integer用到了享元模式来复用对象，对于要创建Integer的值在 -128 ~ 127 之间（常用），
就会从 IntegerCache 类中直接返回，否则才调用 new 方法创建。
```java
public static Integer valueOf(int i) {
    if (i >= IntegerCache.low && i <= IntegerCache.high)
        return IntegerCache.cache[i + (-IntegerCache.low)];
    return new Integer(i);
}
```
对于String，原理也是一样的。
