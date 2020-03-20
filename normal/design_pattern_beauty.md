# 设计模式之美
这个也是来自于极客时间《设计模式之美》的阅读笔记。

这个课程有100讲。主要包含：
1.面向对象
2.设计原则
3.规范与重构
4.设计模式与范式(实战例子)

2020年过年期间看了前面两章，一些原理的东西，总结的时候还是要重新回顾一下。

## 一.面向对象
第一章讲的大多是概念性的东西，主要是面向对象编程的一些好处。
与面向过程编程的对比，抽象类和接口等等
抽取其中部分总结

### 1.面向对象与面向过程

#### 1.1 滥用Getter、Setter
在设计实现类的时候，除非真的需要，否则不要给属性定义Setter方法。
尽管Getter方法相对Setter方法安全一些，但是如果返回的是集合容器，也要防范集合内部数据被修改。（Java语言提供了Collections.unmodifiableList()方法，可以让Getter返回一个不可修改的集合容器）。
虽然说集合容器不可修改，但是如果是 List<Item> items 这样，列表里面的对象还是可修改的。。（怎么解决呢？）

#### 1.2 滥用全局变量和全局方法
有时会把程序中用到的常量放在一个Constant类中。
大而全的Constants类会带来以下影响：
1.修改成本大，多人修改易带来冲突
2.增加代码编译时间
3.影响代码复用性（如果在另外一个项目，复用本项目开发的某个类，而这个类又依赖Constants类。即它依赖Constants的一小部分常量，仍然要把整个类一并引入，也就引入了很多无关的常量）

正常方式：
1.将Constants拆分成更单一的多个类，比如MySQL配置相关的，就放到MySQLConstants中，Redis相关的就放到RedisConstants中。
2.哪个类用到了某个常量，就直接把这个常量定义到这个类中。
（我感觉通常来说，按2.来，然后MySQL中某个常量要复用，在放到1.中）

#### 1.3 Utils类
共用的一些逻辑，细化一些设计就好了

### 2.接口VS抽象类
在面向对象编程中，抽象类和接口是两个经常被用到的语法概念，是面向对象的四大特性，以及很多设计模式，设计思想，设计原则编程实现的基础。
比如，我们可以使用接口来实现面向对象的抽象特性、多态特性和基于接口而非实现的设计原则，使用抽象类来实现面向对象的继承特性和模板设计模式等等。

抽象类：
1.抽象类不允许被实例化
2.抽象类可以包含属性和方法
3.子类继承抽象类

接口：
1.接口不能包含属性
2.接口只能声明方法
3.类实现接口的时候，必须实现接口中声明的所有方法

**抽象类更多的是为了代码复用，而接口就更加侧重于解耦。**接口是对行为的一种抽象，相当于一组协议或契约。
接口实现了约定和实现分离，可以降低代码间的耦合性，提高代码的可扩展性。
（如果类之间继承结构稳定，层次较浅，关系不复杂，就可以大胆的使用继承）

### 3.组合由于继承
继承是面向对象的四大特性之一，用来表示类之间的is-a关系，可以解决代码复用的问题。虽然继承有诸多作用，但继承层次过深、过复杂，也会影响代码的可维护性。

#### 3.1 组合相比继承有哪些优势？
实际上，我们可以利用组合、接口、委托，来解决继承存在的问题。
// todo: 代码

## 二.设计原则
设计原则包括SOLID,KISS,YAGNI,DRY,LOD等
SOLID： 单一职责原则、开闭原则、里式替换原则、接口隔离原则、依赖反转原则

### 1.单一职责原则

#### 1.1 如何理解单一职责原则
它的意思就是一个类或模块只负责完成一个职责（或功能），也就是说让每个类或模块的功能更单一、粒度更细。
实际上，在真正的软件开发中，我们也没有必要过于未雨绸缪，过度设计。我们可以先写一个粗粒度的类，随着业务的发展，再进行拆分。

#### 1.2 如何判断类的职责是否足够单一
1.代码行数、函数或属性过多
2.依赖的其他类过多
3.私有方法过多（不是跟函数过多差不多嘛？）
4.比较难给出一个合适的名字
5.类中大量的方法都是集中操作类中的某几个属性（这个比较能体现出来）

#### 1.3 类的职责是否越单一越好
有时候拆分过细，反而会降低内聚行，影响代码的可维护性。（如把Serialization拆分成Serializer和Deserializer）

### 2.开闭原则
作者认为这是 SOLID 中最难理解（什么样的改动才被定义为“扩展”，什么定义为“修改”），也是最有用的原则。
如何做到**对扩展开放，修改关闭**。在追求扩展性的同时，避免影响代码的可读性。

#### 2.1 理解“对扩展开放，修改关闭”
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