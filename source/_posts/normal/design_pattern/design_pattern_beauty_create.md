# 四.设计模式与范式 —— 创建型

## 1.单例模式
一个类只允许创建一个对象

### 1.1 单例模式需要解决的问题
1.资源访问冲突（这个看并发编程就能搞得定）  
2.全局唯一类（配置类、ID生成器等）

### 1.2 如何实现一个单例
1.构造函数需要的是private访问权限，才能避免外部通过new创建实例  
2.考虑对象创建时的线程安全问题。（懒加载时??）  
3.考虑是否支持延迟加载。  
4.考虑getInstance()性能是否高（是否加锁）。（这个有点不是很懂）

#### 1.饿汉式（类创建时就初始化好）  
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

#### 2.懒汉式  
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
#### 3.静态内部类  
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

#### 4.枚举
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

### 1.3 单例模式的一些缺点
1.对OOP的特性支持不友好  
2.会隐藏类之间的依赖关系（不是构造函数注入，不知道具体依赖）  
3.对代码的扩展性不友好（跟1.差不多，毕竟也是因为违反基于接口而非实现编程带来的后果）
4.对代码的测试性不友好  
5.不支持有参数的构造函数  
**解决方案**：
通过工厂模式、IOC容器（比如Spring IOC，貌似都有注解定义它是否单例了）来保证，提高扩展性。

### 1.4 集群模式下的单例
单例一般来讲就是进程内的单例，也就是说一个进程只有一个单例对象。  

#### 1.如何实现线程内的单例
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

#### 2.如何实现集群下的单例  
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

## 2.工厂模式
工厂模式分为三种更细分的类型：简单工厂、工厂方法和抽象工厂

### 2.1 简单工厂
在不同情境下，我们可能需要创建不同类型的类去处理业务。  
比如说我们需要用不同的解析器去处理不同的文件，根据文件名后缀去创建解析器。
```java
public class RuleConfigSource {
    public RuleConfig load(String ruleConfigFilePath) {
        String ruleConfigFileExtension = getFileExtension(ruleConfigFilePath);
        IRuleConfigParser parser = null;
        if ("json".equalsIgnoreCase(ruleConfigFileExtension)) {
            parser = new JsonRuleConfigParser();
        } else if ("xml".equalsIgnoreCase(ruleConfigFileExtension)) {
            parser = new XmlRuleConfigParser();
        } else if ("yaml".equalsIgnoreCase(ruleConfigFileExtension)) {
            parser = new YamlRuleConfigParser();
        } else if ("properties".equalsIgnoreCase(ruleConfigFileExtension)) {
            parser = new PropertiesRuleConfigParser();
        } else {
            throw new InvalidRuleConfigException(
                    "Rule config file format is not supported: " + ruleConfigFilePath);
        }

        String configText = "";
        //从ruleConfigFilePath文件中读取配置文本到configText中
        RuleConfig ruleConfig = parser.parse(configText);
        return ruleConfig;
    }

    private String getFileExtension(String filePath) {
        // ....
    }
}
```
按照单一职责设计原则来说，把创建这一步移出来作为一个类（简单工厂模式）：
```java
public class RuleConfigParserFactory {
    public static IRuleConfigParser createParser(String configFormat) {
        IRuleConfigParser parser = null;
        if ("json".equalsIgnoreCase(configFormat)) {
            parser = new JsonRuleConfigParser();
        } else if ("xml".equalsIgnoreCase(configFormat)) {
            parser = new XmlRuleConfigParser();
        } else if ("yaml".equalsIgnoreCase(configFormat)) {
            parser = new YamlRuleConfigParser();
        } else if ("properties".equalsIgnoreCase(configFormat)) {
            parser = new PropertiesRuleConfigParser();
        }
        return parser;
    }
}
```
在上面的代码实现中，我们每次调用createParser()都会创建一个新的parser。  
实际上如果parser可以复用的话，就可以把之前创建的parser缓存起来。(有点类似单例和工厂的结合)
```java
public class RuleConfigParserFactory {
    private static final Map<String, RuleConfigParser> cachedParsers = new HashMap<>();

    static {
        cachedParsers.put("json", new JsonRuleConfigParser());
        cachedParsers.put("xml", new XmlRuleConfigParser());
        cachedParsers.put("yaml", new YamlRuleConfigParser());
        cachedParsers.put("properties", new PropertiesRuleConfigParser());
    }

    public static IRuleConfigParser createParser(String configFormat) {
        if (configFormat == null || configFormat.isEmpty()) {
            return null;//返回null还是IllegalArgumentException全凭你自己说了算
        }
        IRuleConfigParser parser = cachedParsers.get(configFormat.toLowerCase());
        return parser;
    }
}
```

### 2.2 工厂方法
如果把if的逻辑去掉，比较经典的方法就是利用多态进行重构。
```java
public interface IRuleConfigParserFactory {
    IRuleConfigParser createParser();
}

public class JsonRuleConfigParserFactory implements IRuleConfigParserFactory {
    @Override
    public IRuleConfigParser createParser() {
        return new JsonRuleConfigParser();
    }
}

public class XmlRuleConfigParserFactory implements IRuleConfigParserFactory {
    @Override
    public IRuleConfigParser createParser() {
        return new XmlRuleConfigParser();
    }
}

public class YamlRuleConfigParserFactory implements IRuleConfigParserFactory {
    @Override
    public IRuleConfigParser createParser() {
        return new YamlRuleConfigParser();
    }
}

public class PropertiesRuleConfigParserFactory implements IRuleConfigParserFactory {
    @Override
    public IRuleConfigParser createParser() {
        return new PropertiesRuleConfigParser();
    }
}
```
实际上，这就是工厂方法模式的典型代码实现。  
这样当我们新增一个parser的时候，只需要新增一个实现了IRuleConfigParserFactory接口的Factory类即可。  
所以工厂方法模式比起简单工厂模式更符合开闭原则。

从工厂方法的使用来看：
```java
public class RuleConfigSource {
    public RuleConfig load(String ruleConfigFilePath) {
        String ruleConfigFileExtension = getFileExtension(ruleConfigFilePath);

        IRuleConfigParserFactory parserFactory = null;
        if ("json".equalsIgnoreCase(ruleConfigFileExtension)) {
            parserFactory = new JsonRuleConfigParserFactory();
        } else if ("xml".equalsIgnoreCase(ruleConfigFileExtension)) {
            parserFactory = new XmlRuleConfigParserFactory();
        } else if ("yaml".equalsIgnoreCase(ruleConfigFileExtension)) {
            parserFactory = new YamlRuleConfigParserFactory();
        } else if ("properties".equalsIgnoreCase(ruleConfigFileExtension)) {
            parserFactory = new PropertiesRuleConfigParserFactory();
        } else {
            throw new InvalidRuleConfigException("Rule config file format is not supported: " + ruleConfigFilePath);
        }
        IRuleConfigParser parser = parserFactory.createParser();

        String configText = "";
        //从ruleConfigFilePath文件中读取配置文本到configText中
        RuleConfig ruleConfig = parser.parse(configText);
        return ruleConfig;
    }

    private String getFileExtension(String filePath) {
        //...解析文件名获取扩展名，比如rule.json，返回json
        return "json";
    }
}
```
工厂类对象的创建逻辑又耦合进了load函数中，跟我们最初版本的代码非常相似。  
引入工厂方法不仅没有解决问题，反而让设计更复杂了。那怎么解决这个问题呢？  

我们可以为工厂类再创建一个简单工厂，也就是工厂的工厂，用来创建工厂类对象。  
（真的有点蠢了）
```java
public class RuleConfigSource {
    public RuleConfig load(String ruleConfigFilePath) {
        String ruleConfigFileExtension = getFileExtension(ruleConfigFilePath);

        IRuleConfigParserFactory parserFactory = RuleConfigParserFactoryMap.getParserFactory(ruleConfigFileExtension);
        if (parserFactory == null) {
            throw new InvalidRuleConfigException("Rule config file format is not supported: " + ruleConfigFilePath);
        }
        IRuleConfigParser parser = parserFactory.createParser();

        String configText = "";
        //从ruleConfigFilePath文件中读取配置文本到configText中
        RuleConfig ruleConfig = parser.parse(configText);
        return ruleConfig;
    }

    private String getFileExtension(String filePath) {
        //...解析文件名获取扩展名，比如rule.json，返回json
        return "json";
    }
}

//因为工厂类只包含方法，不包含成员变量，完全可以复用，
//不需要每次都创建新的工厂类对象，所以，简单工厂模式的第二种实现思路更加合适。
public class RuleConfigParserFactoryMap { //工厂的工厂
    private static final Map<String, IRuleConfigParserFactory> cachedFactories = new HashMap<>();

    static {
        cachedFactories.put("json", new JsonRuleConfigParserFactory());
        cachedFactories.put("xml", new XmlRuleConfigParserFactory());
        cachedFactories.put("yaml", new YamlRuleConfigParserFactory());
        cachedFactories.put("properties", new PropertiesRuleConfigParserFactory());
    }

    public static IRuleConfigParserFactory getParserFactory(String type) {
        if (type == null || type.isEmpty()) {
            return null;
        }
        IRuleConfigParserFactory parserFactory = cachedFactories.get(type.toLowerCase());
        return parserFactory;
    }
}
```
实际上，对于规则配置文件解析这个应用场景来说，工厂模式需要额外创建诸多的Factory类，也会增加代码的复杂性。  
而且每个Factory类只做了简单的 new 操作，功能太单薄，没必要设计成独立的类。  
所以在这个应用场景下，简单工厂模式比工厂方法模式更加合适。  
当对象创建逻辑比较复杂再用工厂方法模式。

### 2.3 抽象工厂
抽象工厂的应用场景比较特殊，没有前两种常用。所以不是学习的重点，了解一下就可以了。  

在简单工厂和工厂方法中，类只有一种分类方式。（比如之前的按配置文件格式分类）  
当类有两种分类方式时（比如也可以按照解析对象，Rule规则配置还是System系统配置来分类）  
那我们就要写8个parser类了。
```text
// 针对规则配置的解析器：基于接口IRuleConfigParser
JsonRuleConfigParser
XmlRuleConfigParser
YamlRuleConfigParser
PropertiesRuleConfigParser

// 针对系统配置的解析器：基于接口ISystemConfigParser
JsonSystemConfigParser
XmlSystemConfigParser
YamlSystemConfigParser
PropertiesSystemConfigParser
```
抽象工厂就是针对这种场景而诞生的。
我们可以让一个工厂负责创建多个不同类型的对象（IRuleConfigParser、ISystemConfigParser等），
而不是只创建一种parser对象。
```java
public interface IConfigParserFactory {
    IRuleConfigParser createRuleParser();

    ISystemConfigParser createSystemParser();
    //此处可以扩展新的parser类型，比如IBizConfigParser
}

public class JsonConfigParserFactory implements IConfigParserFactory {
    @Override
    public IRuleConfigParser createRuleParser() {
        return new JsonRuleConfigParser();
    }

    @Override
    public ISystemConfigParser createSystemParser() {
        return new JsonSystemConfigParser();
    }
}

public class XmlConfigParserFactory implements IConfigParserFactory {
    @Override
    public IRuleConfigParser createRuleParser() {
        return new XmlRuleConfigParser();
    }

    @Override
    public ISystemConfigParser createSystemParser() {
        return new XmlSystemConfigParser();
    }
}

// 省略YamlConfigParserFactory和PropertiesConfigParserFactory代码
```

### 2.4 依赖注入容器（Dependency Injection）
当创建对象是一个“大工程”时，即涉及很多类的创建。  
一种是设计复杂的if-else分支判断，另一种是对象创建需要组装多个其他类对象或者需要复杂的初始化过程。  
依赖注入框架（Dependency Injection Container），简称DI容器，就是用来解决这个问题。  

#### 1.工厂模式和DI容器有何区别
DI容器的底层基本设计思路就是基于工厂模式的。DI容器相当于一个大的工厂类。（负责的是整个应用中所有类对象的创建）  
除此之外，DI容器还要处理配置的解析，对象生命周期的管理。  

#### 2.DI容器的核心功能
配置解析：我们将需要由DI容器来创建的类对象和创建类对象的必要信息（使用哪个构造函数及对应构造函数参数是什么等等），
放到配置文件中。容器读取配置文件，根据其提供的信息创建对象。  
对象创建：利用“反射”机制，在程序运行的过程中，动态地加载类，创建对象。  
生命周期管理：在Spring框架中，可以通过配置scope属性，区分不同类型的对象（是否单例）等。
还提供init-method,destroy-method提供对象创建销毁时函数调用。  
（Spring都有相应的注解去处理）  

#### 3.简单的ID容器实现
把理解的东西放到下面代码的注解当中。  
执行入口：
```java
public interface ApplicationContext {
    Object getBean(String beanId);
}

public class ClassPathXmlApplicationContext implements ApplicationContext {
    private BeansFactory beansFactory;
    private BeanConfigParser beanConfigParser;

    /**
     * 由工厂类和配置解析器构成
     */
    public ClassPathXmlApplicationContext(String configLocation) {
        this.beansFactory = new BeansFactory();
        this.beanConfigParser = new XmlBeanConfigParser();
        loadBeanDefinitions(configLocation);
    }

    /**
     * 根据文件路径，扫描配置后加载进来，放到工厂类中
     */
    private void loadBeanDefinitions(String configLocation) {
        InputStream in = null;
        try {
            in = this.getClass().getResourceAsStream("/" + configLocation);
            if (in == null) {
                throw new RuntimeException("Can not find config file: " + configLocation);
            }
            List<BeanDefinition> beanDefinitions = beanConfigParser.parse(in);
            beansFactory.addBeanDefinitions(beanDefinitions);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // TODO: log error
                }
            }
        }
    }

    @Override
    public Object getBean(String beanId) {
        return beansFactory.getBean(beanId);
    }
}
```
配置解析器及加载类信息的定义：
```java
public interface BeanConfigParser {
    List<BeanDefinition> parse(InputStream inputStream);
    List<BeanDefinition> parse(String configContent);
}

public class XmlBeanConfigParser implements BeanConfigParser {

    @Override
    public List<BeanDefinition> parse(InputStream inputStream) {
        String content = null;
        // TODO:...
        return parse(content);
    }

    @Override
    public List<BeanDefinition> parse(String configContent) {
        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        // TODO:...
        return beanDefinitions;
    }

}

public class BeanDefinition {
    private String id;              // Bean id
    private String className;       // 类名
    private List<ConstructorArg> constructorArgs = new ArrayList<>();  // 参数
    private Scope scope = Scope.SINGLETON;      // 类类型
    private boolean lazyInit = false;           // 是否懒加载
    // 省略必要的getter/setter/constructors

    public boolean isSingleton() {
        return scope.equals(Scope.SINGLETON);
    }


    public static enum Scope {
        SINGLETON,
        PROTOTYPE
    }

    public static class ConstructorArg {
        private boolean isRef;
        private Class type;
        private Object arg;
        // 省略必要的getter/setter/constructors
    }
}
```
核心工厂类：
```java
public class BeansFactory {
    // 跟简单工厂的第二种差不多，用个Map的存好创建的单例对象
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    // 通过配置读取的所有类的信息(BeanDefinition)
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();

    public void addBeanDefinitions(List<BeanDefinition> beanDefinitionList) {
        // 读入配置
        for (BeanDefinition beanDefinition : beanDefinitionList) {
            this.beanDefinitions.putIfAbsent(beanDefinition.getId(), beanDefinition);
        }
        // 饿汉式创建
        for (BeanDefinition beanDefinition : beanDefinitionList) {
            if (beanDefinition.isLazyInit() == false && beanDefinition.isSingleton()) {
                createBean(beanDefinition);
            }
        }
    }

    public Object getBean(String beanId) {
        BeanDefinition beanDefinition = beanDefinitions.get(beanId);
        if (beanDefinition == null) {
            throw new NoSuchBeanDefinitionException("Bean is not defined: " + beanId);
        }
        return createBean(beanDefinition);
    }

    @VisibleForTesting
    protected Object createBean(BeanDefinition beanDefinition) {
        // 之前已经用饿汉式创建过的话，就直接返回喽
        if (beanDefinition.isSingleton() && singletonObjects.contains(beanDefinition.getId())) {
            return singletonObjects.get(beanDefinition.getId());
        }

        // 这里就是“反射”创建新的类对象了
        Object bean = null;
        try {
            Class beanClass = Class.forName(beanDefinition.getClassName());
            List<BeanDefinition.ConstructorArg> args = beanDefinition.getConstructorArgs();
            if (args.isEmpty()) {
                bean = beanClass.newInstance();
            } else {
                Class[] argClasses = new Class[args.size()];
                Object[] argObjects = new Object[args.size()];
                for (int i = 0; i < args.size(); ++i) {
                    BeanDefinition.ConstructorArg arg = args.get(i);                   
                    if (!arg.getIsRef()) {
                        // 基础类型直接放属性中
                        argClasses[i] = arg.getType();
                        argObjects[i] = arg.getArg();
                    } else {
                        // 组合了其他类的话，就递归创建
                        BeanDefinition refBeanDefinition = beanDefinitions.get(arg.getArg());
                        if (refBeanDefinition == null) {
                            throw new NoSuchBeanDefinitionException("Bean is not defined: " + arg.getArg());
                        }
                        argClasses[i] = Class.forName(refBeanDefinition.getClassName());
                        argObjects[i] = createBean(refBeanDefinition);
                    }
                }
                bean = beanClass.getConstructor(argClasses).newInstance(argObjects);
            }
        } catch (ClassNotFoundException | IllegalAccessException
                | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new BeanCreationFailureException("", e);
        }

        if (bean != null && beanDefinition.isSingleton()) {
            singletonObjects.putIfAbsent(beanDefinition.getId(), bean);
            return singletonObjects.get(beanDefinition.getId());
        }
        return bean;
    }
}
```

## 3.建造者模式
Builder模式其实挺简单的，只要弄清楚它的结构和应用场景就没问题了。  
Spring的类注解中也有@Builder这个注解，直接就帮开发者省略的很多的过程。  

### 3.1 建造者模式的应用场景
为了避免构造函数的参数列表过长，影响代码的可读性和易用性，我们可以通过构造函数配合set方法来解决。
但是，如果存在下面情况中的一种，我们就要考虑使用建造者模式了。  

1.我们把类的必填属性放在构造函数中，强制创建对象的时候就设置。  
如果必填参数很多，构造函数又会出现参数列表很长的问题。  
如果我们把必填函数放在set()方法设置，那校验这些必填属性的逻辑就无处安放了。  

2.如果类的属性之间有一定的依赖关系或者约束条件。（比如说minSize<maxSize）  

3.如果我们希望创建不可变对象，也就是说，对象创建之后，就不能再修改内部的属性值。  

### 3.2 建造者模式和工厂模式的区别
工厂模式：用来创建**不同但相关类型的对象**（继承同一父类或接口的一组子类）。  
建造者模式：用来创建**一种类型的复杂对象**，可以通过设置不同的可选参数，“定制化”地创建对象。
