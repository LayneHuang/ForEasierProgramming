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

### 3.里式替换原则（LSP）
这条原则跟之前讲的“多态”有点相似，也有区别之处。

#### 3.1 里式替换原则的理解
子类对象能够替换程序中父类对象出现的任何地方，并且保证原来程序的逻辑行为不变及正确性不被破坏。  
（这个原则根据字面意思其实也很好理解）  

#### 3.2 哪些代码违背了LSP
**1.子类违背了父类声明要实现的功能**  
父类中提供的sortOrderByAmount()是要按照订单的金额排序。而子类重写这个函数后，是按照日期排序。  
**2.子类违背了父类对输入、输出、异常的约定**  
父类的某个函数：运行出错的时候返回null，获取数据为空时返回空集合。  
而子类重载之后，运行出错返回异常（exception），获取数据为空返回null。
父类中约定输入数据可以是任意整数，而子类约定只能是正整数，子类的输入校验比父类更严格了。  
父类某个函数约定只会抛出ArgumentNullException，而子类抛出任何其他异常都违背了LSP。  
**3.子类违背了父类注释罗列的任何特殊说明**

### 4.接口隔离原则（ISP）
客户端不应该强迫依赖它不需要的接口。“客户端”可以理解为接口的调用者。  
“接口”也可以理解为：  
1.一组API接口集合  
2.单个API接口或函数  
3.OOP中的接口概念

#### 4.1 把接口理解为一组API接口集合
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

#### 4.2 把接口理解为单个API接口或函数
这一点跟单一职责原则基本差不多，只不过接口隔离更针对于接口而言。

#### 4.3 把接口理解为OOP中的接口概念
假设项目有3个外部系统：Redis、MySQL、Kafka。每个系统都有一系列配置（比如地址、端口、访问超时时间）。  
分别设计了3个类：RedisConfig、MySQLConfig、KafkaConfig  
现在有一个需求，实现了一个ScheduleUpdater来调用Redis、Kafka的update方法更新配置，但不希望MySQL更新。  
有另外一个需求，配置查看，需要查看Redis、MySQL的配置，而不需要Kafka的。  
最终的设计是把接口分成Updater、Viewer，让配置按需来实现。  

（感觉这个3点说得差不多，都是需要接口粒度更细，职责更单一）

### 5.依赖反转原则

#### 5.1 依赖反转与控制反转的区别
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

#### 5.2 依赖注入（DI）
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

#### 5.3 依赖反转原则
高层模块（调用方）不要依赖低层模块（被调用方）。  
高层模块和低层模块应该通过抽象来互相依赖。  
除此之外，抽象不要依赖具体实现，具体实现依赖抽象。

### 6.KISS与YAGNI
KISS: Keep it simple and stupid.  
YAGNI: you ain't gonna nedd it.  
这两个理解起来就比较容易了。就是要让程序简单可读（KISS），不要过度设计（YAGNI）。

### 7.迪米特法制
不该有直接依赖关系的类，不要有依赖。有依赖关系的类之间，**尽量只依赖必要的接口**。

## 三.规范与重构
这一章先略过，主要内容先看《重构第二版》

## 四.设计模式与范式
一些比较熟悉的模式，就直接记其特别的点。

### 1.单例模式
一个类只允许创建一个对象

#### 1.1 单例模式需要解决的问题
1.资源访问冲突（这个看并发编程就能搞得定）  
2.全局唯一类（配置类、ID生成器等）

#### 1.2 如何实现一个单例
1.构造函数需要的是private访问权限，才能避免外部通过new创建实例  
2.考虑对象创建时的线程安全问题。（懒加载时??）  
3.考虑是否支持延迟加载。  
4.考虑getInstance()性能是否高（是否加锁）。（这个有点不是很懂）

**1.饿汉式（类创建时就初始化好）**  
```java
public class IdGenerator {
    private AtomicLong id = new AtomicLong(0);
    private static final IdGenerator instance = new IdGenerator();

    private IdGenerator() {
    }

    public static IdGenerator getInstance() {
        return instance;
    }

    public long getId() {
        return id.incrementAndGet();
    }
}
```
如果初始化的时间长，最好不要等到真正用到它的时候，采取执行这个耗时长的操作。  
如果实例占用资源多，按照fail-fast设计原则（有问题及早暴露），那我们也希望在程序启动时就将这个实例初始化好。  
如果资源不够，程序会在启动时候就触发OOM。  

**2.懒汉式**  
支持延迟加载。(低版本Java有指令重排问题)
```java
public class IdGenerator { 
  private AtomicLong id = new AtomicLong(0);
  private static IdGenerator instance;
  private IdGenerator() {}
  public static IdGenerator getInstance() {
    if (instance == null) {
      synchronized(IdGenerator.class) { // 此处为类级别的锁
        if (instance == null) {
          instance = new IdGenerator();
        }
      }
    }
    return instance;
  }
  public long getId() { 
    return id.incrementAndGet();
  }
}
```
**3.静态内部类**  
```java
public class IdGenerator {
    private AtomicLong id = new AtomicLong(0);

    private IdGenerator() {
    }

    private static class SingletonHolder {
        private static final IdGenerator instance = new IdGenerator();
    }

    public static IdGenerator getInstance() {
        return SingletonHolder.instance;
    }

    public long getId() {
        return id.incrementAndGet();
    }
}
```
SingletonHolder是一个静态内部类，当外部类IdGenerator被加载的时候，并不会创建SingletonHolder。
只有调用getInstance()时，SingletonHolder才会被加载。instance的唯一性，创建过程的线程安全都由JVM保证。

**4.枚举**
```java
public enum IdGenerator {
  INSTANCE;
  private AtomicLong id = new AtomicLong(0);
 
  public long getId() { 
    return id.incrementAndGet();
  }
}
```
同样，唯一性，创建过程的线程安全都由JVM保证。简单是简单，但是有点不是太直观的感觉。

#### 1.3 单例模式的一些缺点
1.对OOP的特性支持不友好  
2.会隐藏类之间的依赖关系（不是构造函数注入，不知道具体依赖）  
3.对代码的扩展性不友好（跟1.差不多，毕竟也是因为违反基于接口而非实现编程带来的后果）
4.对代码的测试性不友好  
5.不支持有参数的构造函数  
**解决方案**：
通过工厂模式、IOC容器（比如Spring IOC，貌似都有注解定义它是否单例了）来保证，提高扩展性。

#### 1.4 集群模式下的单例
单例一般来讲就是进程内的单例，也就是说一个进程只有一个单例对象。  

**1.如何实现线程内的单例**  
其实Java提供了ThreadLocal工具类，可以更加轻松地实现线程单例。不过ThreadLocal底层实现原理也是基于下面代码所示的HashMap。
```java
public class IdGenerator {
    private AtomicLong id = new AtomicLong(0);

    private static final ConcurrentHashMap<Long, IdGenerator> instances
            = new ConcurrentHashMap<>();

    private IdGenerator() {}

    public static IdGenerator getInstance() {
        Long currentThreadId = Thread.currentThread().getId();
        instances.putIfAbsent(currentThreadId, new IdGenerator());
        return instances.get(currentThreadId);
    }

    public long getId() {
        return id.incrementAndGet();
    }
}
```

**2.如何实现集群下的单例**  
集群唯一，也就是说多个进程共享同一个对象，不能创建同一个类的多个对象。  
具体来说，我们需要把这个单例对象序列化并储存到外部共享储存区（比如文件，外部的内存也行嘛，文件就降级了）。
进程在使用这个类的时候，需要先从外部共享存储区中将它读入到内存，并反序列化成对象，然后再使用。
使用完成后还需要储存回外部存储区。    

**在使用过程中还需要注意互斥。**  
为了保证任何时刻，在进程内都只有一份对象存在，一个进程在获取到对象之后，需要对对象加锁，避免其他进程再其它获取。
在进程用完这个对象之后，还需要显示地将对象从内存中删除，并且释放对对象的加锁。
```java
public class IdGenerator {
    private AtomicLong id = new AtomicLong(0);
    private static IdGenerator instance;
    private static SharedObjectStorage storage = FileSharedObjectStorage(/*入参省略，比如文件地址*/);
    private static DistributedLock lock = new DistributedLock(); // 分布式锁?

    private IdGenerator() {
    }

    public synchronized static IdGenerator getInstance() {
        if (instance == null) {
            lock.lock();
            instance = storage.load(IdGenerator.class);
        }
        return instance;
    }

    public synchronized void freeInstance() {
        storage.save(this, IdGeneator.class);
        instance = null; //释放对象
        lock.unlock();
    }

    public long getId() {
        return id.incrementAndGet();
    }
}

// IdGenerator使用举例
IdGenerator idGeneator = IdGenerator.getInstance();
long id = idGenerator.getId();
IdGenerator.freeInstance();
```
