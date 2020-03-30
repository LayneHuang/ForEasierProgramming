# ForEasyCode

## 基础
#### [设计模式之美](https://github.com/LayneHuang/ForEasyCode/blob/master/normal/design_pattern_beauty.md)
#### [重构](https://github.com/LayneHuang/ForEasyCode/blob/master/normal/restructure.md)

## 前端
#### [React & Redux](https://github.com/LayneHuang/ForEasyCode/blob/master/website/react/react.md)

## Java后台
#### [Effective Java 2.0](https://github.com/LayneHuang/ForEasyCode/blob/master/java/effectivejava.md)
#### [并发编程](https://github.com/LayneHuang/ForEasyCode/blob/master/java/concurrent_programming.md)

## 数据分析

## 简录
```text
[2020.2.4]
工作两年有余，17年~19年都比较迷茫，不知道究竟向着什么方向努力
经过之前的一些面试知道了自己的一些弱势点在哪里，了解的知识是不够全面。
目前的做法是通过看书（kindle电子书）和极客时间的专栏结合（有音频，有Sample可以加深印象）
然后自己再做总结去再进一步加深印象提升。
2020年的春节假期因为武汉肺炎病毒的事情延长了许久。刚好有时间提前回来去做总结。
之后的目标一方面是向着能把一些编程比赛（codeforces）的分打高一点（纯兴趣）。
另一方面得是工程技术得到全面的学习（工作的基础嘛），同时向着自己兴趣方面去靠拢（数据分析）。
最近因为Kobe的去世，看了很多很多关于他的记录片（我的青春结束了）。
他对篮球的热爱，坚持，不断学习进步，不畏惧挑战。
对自己职业生涯的理解，对自身（臂展，弹跳，得分技巧）和比赛的理解都做到很极致。
曼巴精神~~~我希望之后自己也能把自己喜欢的事情做到极致。

其中他讲到有一段话印象很深刻：
我会跟他说专注于当下的事情就好
把注意力放在当下做好眼前的事情
你可以这样来想
如果我想要成为NBA的最佳投手
那我要怎么做
我会每天一醒来就投1000次篮
星期一1000次
星期二1000次
星期三1000次
我只专注于做好每天的事情
但等到一年过去之后我就是这世界上的最佳投手
但要完成这个目标你必须要一步一个脚印做好
你不用去担心过去或者是未来
```
附上:[Dear Basketball](https://www.bilibili.com/video/av16997700?from=search&seid=1465929823314277301)

### 3.建造者模式
Builder模式其实挺简单的，只要弄清楚它的结构和应用场景就没问题了。  
Spring的类注解中也有@Builder这个注解，直接就帮开发者省略的很多的过程。  

#### 3.1 建造者模式的应用场景
为了避免构造函数的参数列表过长，影响代码的可读性和易用性，我们可以通过构造函数配合set方法来解决。
但是，如果存在下面情况中的一种，我们就要考虑使用建造者模式了。  

1.我们把类的必填属性放在构造函数中，强制创建对象的时候就设置。  
如果必填参数很多，构造函数又会出现参数列表很长的问题。  
如果我们把必填函数放在set()方法设置，那校验这些必填属性的逻辑就无处安放了。  

2.如果类的属性之间有一定的依赖关系或者约束条件。（比如说minSize<maxSize）  

3.如果我们希望创建不可变对象，也就是说，对象创建之后，就不能再修改内部的属性值。  

#### 3.2 建造者模式和工厂模式的区别
工厂模式：用来创建**不同但相关类型的对象**（继承同一父类或接口的一组子类）。  
建造者模式：用来创建**一种类型的复杂对象**，可以通过设置不同的可选参数，“定制化”地创建对象。

### 4.代理模式
代理模式的原理和代码实现都不难掌握。  
它在不改变原始类（或称为被代理类）代码的情况下，通过引入代理类给原始类附加功能。  
（类似 React 的 middleware， 高阶组件HOC的写法 ）

#### 4.1 代理模式原理解析
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

#### 4.2 动态代理（Dynamic Proxy）
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

#### 4.3 代理模式的应用场景
1.业务系统的非功能性需求开发  
比如：监控、统计、鉴权、限流、事务、幂等、日志。（好像Spring框架或者微服务框架都会有）
如果你熟悉 Java语言 和 Spring开发框架，这部分工作都是可以在Spring AOP切面中完成的。  

2.代理模式在RPC、缓存中的应用  
实际上，RPC 框架也可以看做一种代理模式，GoF的《设计模式》一书中把它称作远程代理。
通过远程代理，将网络通信、数据解码等细节隐藏起来。客户端在使用RPC服务的时候，
就像调用本地函数一样，无需了解跟服务器交互的细节。

3.代理模式在缓存中的应用  
假设我们要开发一个接口请求的缓存功能，对于某些接口请求，如果入参相同，在设定的过期时间内，
直接返回缓存结果，而不是重新进行逻辑处理。比如，针对获取个人用户信息的需求，我们可以开发两个接口，
一个支持缓存，一个支持实时查询。  
最简单的方式就是，给每个接口开发两个不同的接口。但这样做，显然增加了开发成本，而且会让代码看起来非常臃肿。
也不方便缓存接口的集中管理（增加、删除缓存接口）、集中配置（比如配置每个接口缓存过期时间）。  
如果是基于Spring框架来开发的话，就可以在 AOP 切面中完成接口缓存功能。
在启动的时候，我们从配置文件中加载需要支持缓存的接口，以及相应的缓存策略（比如过期时间）等。
当请求到来的时候，我们再 AOP 切面中拦截请求，如果请求中带有支持缓存字段，就从缓存中取（内存或Redis），然后直接返回。
