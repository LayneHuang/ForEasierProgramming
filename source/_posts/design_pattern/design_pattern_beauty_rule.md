---
title: 设计模式之美 (设计原则)
date: 2020-04-02 01:46:00
categories: [ DesignPatterns ]
---

设计原则包括SOLID,KISS,YAGNI,DRY,LOD等  
SOLID： 单一职责原则、开闭原则、里式替换原则、接口隔离原则、依赖反转原则  

## 1.单一职责原则

### 1.1 如何理解单一职责原则
它的意思就是一个类或模块只负责完成一个职责（或功能），也就是说让每个类或模块的功能更单一、粒度更细。  
实际上，在真正的软件开发中，我们也没有必要过于未雨绸缪，过度设计。我们可以先写一个粗粒度的类，随着业务的发展，再进行拆分。  

### 1.2 如何判断类的职责是否足够单一
1.代码行数、函数或属性过多  
2.依赖的其他类过多  
3.私有方法过多（不是跟函数过多差不多嘛？）  
4.比较难给出一个合适的名字  
5.类中大量的方法都是集中操作类中的某几个属性（这个比较能体现出来）  

### 1.3 类的职责是否越单一越好
有时候拆分过细，反而会降低内聚行，影响代码的可维护性。（如把Serialization拆分成Serializer和Deserializer）

## 2.开闭原则
作者认为这是 SOLID 中最难理解（什么样的改动才被定义为“扩展”，什么定义为“修改”），也是最有用的原则。  
如何做到**对扩展开放，修改关闭**。在追求扩展性的同时，避免影响代码的可读性。

### 2.1 理解“对扩展开放，修改关闭”
简略的描述就是：在增加一个新功能应该是，在已有代码的基础上扩展代码（新增块、类、方法），而非修改已有的代码（修改模块、类、方法）。    
这一块的Demo还是挺有价值的。  
AlertRule存储告警规则，可以自由设置。  
Notification是警告通知类，支持邮件、短信、微信、手机等多种通知渠道。  
NotificationEmergencyLevel表示通知的紧急程度，包括SEVERE（严重）、URGENCY（紧急）、NORMAL（普通）、TRIVIA（无关紧要），不同的紧急程度发送到不同的渠道。
```java
public class Alert {
    private AlertRule rule;
    private Noitication noitication;

    public Alert(AlertRule rule, Noitication noitication) {
        this.rule = rule;
        this.noitication = noitication;
    }

    public void check(String api, long requestCount, long errorCount, long durationOfSeconds) {
        long tps = requestCount / durationOfSeconds;
        if (tps > rule.getMatchedRule(api).getMaxTps()) {
            noitication.notify(NotificationEmergencyLevel.URGENCY, "....");
        }
        if (errorCount > getMatchedRule(api).getMaxErrorCount()) {
            noitication.notify(NotificationEmergencyLevel.SEVERE, "....");
        }
    }
}
```  
业务逻辑主要集中在check()函数中。当接口的TPS超过某个预先设置的最大阈值，  
以及请求出错数大于某个允许值时，就会触发警告。  

现在要增加一个功能，就是接口每秒种**超时**请求个数大于预先设置的某个阈值时，就要触发警告。  
最简单的改动是入参增加一个timeoutCount，check函数中多加一个判断值。  
这种改动是基于“修改”的，而如何通过“扩展”的方法，来实现同样的功能呢？

先重构一下Alert的代码，让他的扩展性更好一些。重构的主要内容包含两部分：  
1.将check函数的多个入参封装成ApiStatInfo类  
2.引入handler的概念，将if判断逻辑分散在各个handler中  
```java
import java.util.ArrayList;

public class Alert {
    private List<AlertHandler> alertHandlers = new ArrayList<>();

    public void addAlertHandler(AlertHandler handler) {
        alertHandlers.add(handler);
    }

    public void check(ApiStatInfo apiStatInfo) {
        for (AlertHandler handler : alertHandlers) {
            handler.check(apiStatInfo);
        }
    }
}

public class ApiStatInfo {
    // 省略construtor/getter/setter
    private String api;
    private long requestCount;
    private long errorCount;
    private long timeoutCount;
    private long durationOfSecounds;
}

public abstract class AlertHandler {
    protected AlertRule rule;
    protected Noitication noitication;

    public AlertHandler(AlertRule rule, Noitication noitication) {
        this.rule = rule;
        this.noitication = noitication;
    }

    public abstract void check(ApiStatInfo apiStatInfo);
}

// 省略原先的处理，TpsAlertHandler,ErrorAlertHandler
public class TimeoutAlertHandler extends AlertHandler {
    public TimeoutAlertHandler(AlertRule rule, Noitication noitication) {
        super(rule, noitication);
    }

    @Override
    public void check(ApiStatInfo apiStatInfo) {
        if (apiStatInfo.getTimeoutCount() > rule.getMatchedRule(apiStatInfo.getApi()).getMaxTimeoutCount()) {
            noitication.notify(NotificationEmergencyLevel.URGENCY, "....");
        }
    }
}
```

具体使用的代码(调用端)：
```java
public class ApplicationContext {
    private AlertRule alertRule;
    private Notification notification;
    private Alert alert;

    public void initializeBeans() {
        alertRule = new AlertRule(/*.省略参数.*/); //省略一些初始化代码
        notification = new Notification(/*.省略参数.*/); //省略一些初始化代码
        alert = new Alert();
        alert.addAlertHandler(new TpsAlertHandler(alertRule, notification));
        alert.addAlertHandler(new ErrorAlertHandler(alertRule, notification));
    }

    public Alert getAlert() {
        return alert;
    }

    // 饿汉式单例
    private static final ApplicationContext instance = new ApplicationContext();

    private ApplicationContext() {
        instance.initializeBeans();
    }

    public static ApplicationContext getInstance() {
        return instance;
    }
}

public class Demo {
    public static void main(String[] args) {
        ApiStatInfo apiStatInfo = new ApiStatInfo();
        // ...省略设置apiStatInfo数据值的代码
        ApplicationContext.getInstance().getAlert().check(apiStatInfo);
    }
}
```

## 3.里式替换原则（LSP）
这条原则跟之前讲的“多态”有点相似，也有区别之处。

### 3.1 里式替换原则的理解
子类对象能够替换程序中父类对象出现的任何地方，并且保证原来程序的逻辑行为不变及正确性不被破坏。  
（这个原则根据字面意思其实也很好理解）  

### 3.2 哪些代码违背了LSP
#### 1.子类违背了父类声明要实现的功能  
父类中提供的sortOrderByAmount()是要按照订单的金额排序。而子类重写这个函数后，是按照日期排序。  
#### 2.子类违背了父类对输入、输出、异常的约定  
父类的某个函数：运行出错的时候返回null，获取数据为空时返回空集合。  
而子类重载之后，运行出错返回异常（exception），获取数据为空返回null。
父类中约定输入数据可以是任意整数，而子类约定只能是正整数，子类的输入校验比父类更严格了。  
父类某个函数约定只会抛出ArgumentNullException，而子类抛出任何其他异常都违背了LSP。  
#### 3.子类违背了父类注释罗列的任何特殊说明

## 4.接口隔离原则（ISP）
客户端不应该强迫依赖它不需要的接口。“客户端”可以理解为接口的调用者。  
“接口”也可以理解为：  
1.一组API接口集合  
2.单个API接口或函数  
3.OOP中的接口概念

### 4.1 把接口理解为一组API接口集合
比如说微服务用户系统提供了一组跟用于相关的API给其他系统调用。（注册、登录、获取用户信息等）  
```java
public interface UserService {
    boolean register(String cellphone, String password);
    boolean login(String cellphone, String password);
    UserInfo getUserInfoById(long id);
    UserInfo getUserInfoByCellphone(String cellphone);
}

public class UserServiceImpl implements UserService {
    //...
}
```
而当后台需要实现一个删除用户的操作时，不仅仅只在UserService上添加接口，还要注意一些安全隐患。  
删除用户是一个非常慎重的操作，只希望通过后台管理系统来执行。  
（之前架构的Spring-Boot都直接把接口放在Controller上，鉴权也放在Controller上，Impl放在Service上）  
如果在代码设计层面，应该把删除操作独立放到另外一个接口RestrictedUserService中，然后它单独打包给后台管理系统来使用。

### 4.2 把接口理解为单个API接口或函数
这一点跟单一职责原则基本差不多，只不过接口隔离更针对于接口而言。

### 4.3 把接口理解为OOP中的接口概念
假设项目有3个外部系统：Redis、MySQL、Kafka。每个系统都有一系列配置（比如地址、端口、访问超时时间）。  
分别设计了3个类：RedisConfig、MySQLConfig、KafkaConfig  
现在有一个需求，实现了一个ScheduleUpdater来调用Redis、Kafka的update方法更新配置，但不希望MySQL更新。  
有另外一个需求，配置查看，需要查看Redis、MySQL的配置，而不需要Kafka的。  
最终的设计是把接口分成Updater、Viewer，让配置按需来实现。  

（感觉这个3点说得差不多，都是需要接口粒度更细，职责更单一）

## 5.依赖反转原则
讲到控制反转原则，还有2个看着相似的概念（其实没什么关系），就是控制反转，依赖注入。

### 5.1 依赖反转与控制反转的区别
控制反转，缩写IOC。通过一个例子了解一下控制反转：
```java
public class UserServiceTest {
    public static boolean doTest() {
        // ... 
    }

    public static void main(String[] args) {//这部分逻辑可以放到框架中
        if (doTest()) {
            System.out.println("Test succeed.");
        } else {
            System.out.println("Test failed.");
        }
    }
}
```
上面的代码中，所有的流程都由程序员来控制。如果我们抽象出一个下面这样一个框架。  
我们再来看，如何利用框架来实现同样的功能。
```java
public abstract class TestCase {
    public void run() {
        if (doTest()) {
            System.out.println("Test succeed.");
        } else {
            System.out.println("Test failed.");
        }
    }

    public abstract boolean doTest();
}

public class JunitApplication {
    private static final List<TestCase> testCases = new ArrayList<>();

    public static void register(TestCase testCase) {
        testCases.add(testCase);
    }

    public static final void main(String[] args) {
        for (TestCase c: testCases) {
            c.run();
        }
    }
}
```
把这个简化版本的测试框架引入到工程中，我们只需要在框架预留的扩展点，  
也就是TestCase类中的doTest()抽象函数中，填充具体的测试代码就可以实现之前的功能。

这个例子，就是典型的通过框架来实现“控制反转”的例子。  
**框架提供了一个可扩展的代码骨架，用来组装对象、管理整个执行流程。**  
程序员利用框架进行开发时，只需要往预留的扩展点上添加跟自己业务相关的代码，就可以利用框架来驱动整个程序流程。  

这里的“控制”指的是对程序执行流程的控制，而“反转”指的是在没有使用框架之前，程序员自己控制整个程序的执行。  
在使用框架之后，整个程序的执行流程可以通过框架来控制。流程的控制权从程序员“反转”到框架。  
（挺Nice的一个例子）

### 5.2 依赖注入（DI）
不通过new()的方式在类内部创建依赖对象，而是将依赖类的对象在外部创建好之后，通过构造函数，函数参数等方式传递（或注入）给类使用。  
```java
public class Notification {
  private MessageSender messageSender;
  
  public Notification(MessageSender messageSender) {
    this.messageSender = messageSender;
  }
  
  public void sendMessage(String cellphone, String message) {
    this.messageSender.send(cellphone, message);
  }
}

public interface MessageSender {
  void send(String cellphone, String message);
}

// 短信发送类
public class SmsSender implements MessageSender {
  @Override
  public void send(String cellphone, String message) {
    //....
  }
}

// 站内信发送类
public class InboxSender implements MessageSender {
  @Override
  public void send(String cellphone, String message) {
    //....
  }
}

//使用Notification
MessageSender messageSender = new SmsSender();
Notification notification = new Notification(messageSender);
```

### 5.3 依赖反转原则
高层模块（调用方）不要依赖低层模块（被调用方）。  
高层模块和低层模块应该通过抽象来互相依赖。  
除此之外，抽象不要依赖具体实现，具体实现依赖抽象。  
突然发现有一句话描述得挺好： 上层定义接口，下层实现接口。  

## 6.KISS与YAGNI
KISS: Keep it simple and stupid.  
YAGNI: you ain't gonna nedd it.  
这两个理解起来就比较容易了。就是要让程序简单可读（KISS），不要过度设计（YAGNI）。

## 7.迪米特法制
不该有直接依赖关系的类，不要有依赖。有依赖关系的类之间，**尽量只依赖必要的接口**。