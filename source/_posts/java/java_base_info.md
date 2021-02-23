---
title: Java 基础简单汇总
date: 2020-11-16 23:21:31
categories: Java
---

### 1.函数式接口

用到 Comparator 时看到它有注解定义 @FunctionalInterface （函数式接口）而函数式接口具有以下特点(与普通接口不同的点):  

1.接口有且仅有一个抽象方法  
2.允许定义静态方法  
3.允许定义默认方法  
4.允许 Object 中的 public 方法（所有类继承自 Object ）  
5.该注解不是必须的，如果一个接口符合"函数式接口"定义，那么加不加该注解都没有影响。加上该注解能够更好地让编译器进行检查。  
如果编写的不是函数式接口，但是加上了@FunctionInterface，那么编译器会报错。

```java
// 正确的函数式接口
@FunctionalInterface
public interface TestInterface {
 
    // 抽象方法
    public void sub();
 
    // java.lang.Object中的public方法
    public boolean equals(Object var1);
 
    // 默认方法
    public default void defaultMethod(){
    
    }
 
    // 静态方法
    public static void staticMethod(){
 
    }
}

// 错误的函数式接口(有多个抽象方法)
@FunctionalInterface
public interface TestInterface2 {

    void add();
    
    void sub();
}
```
所以 Comparator 接口就给出了许多默认方法，比如整数排序，浮点数排序，自然序，逆序排序的默认方法。   
在 Comparator.comparing() 这个静态方法基本上支持对 对象 的排序。  
（应用场景：给出默认方法，不需要开发者再独自去实现这个单独的接口）  
如 Runnable , Callable 都是函数式接口。

### 2.finally
如果 try 语句块提前 return ， finally 就会在 return 之前执行 ( a 传入大于 0 的值)  
否则就在 try{} 语句后执行，在 Step 4 之前执行。
若有 Exception ， Step 2 就会在 Step 3 之前执行。
```java
public class Sample {
    public static void solve(int a) {
        try {
            System.out.println("Step 1.try 执行");
            if (a > 0) {
                return;
            }
        } catch(Exception e) {
            System.out.println("Step 2.catch Exception 执行");
        } finally {
            System.out.println("Step 3.finally 执行");
        }
        System.out.println("Step 4.后面语句执行");
    }   
}
```

### 3.getCanonicalPath

1.getAbsolutePath():  
返回的是定义时的路径对应的相对路径，但不会处理“.”和“..”的情况    

2.getCanonicalPath():  
返回的是规范化的绝对路径，相当于将getAbsolutePath()中的“.”和“..”解析成对应的正确的路径  

举例如下：
```java
public class Sample {
    public void solve() {
        File file = new File(".\\test.txt");
        System.out.println(file.getPath());
        System.out.println(file.getAbsolutePath());
        System.out.println(file.getCanonicalPath());
    }
}
 /*
返回的结果为:
    .\test.txt
    E:\workspace\Test\.\test.txt
    E:\workspace\Test\test.txt
    “..”的情况类似
*/
```

### 4.子类覆盖父类的成员变量
```java
public class Main {

    static class Father {
        public int id = 1;

        public void printId() {
            System.out.print(id);
        }
    }

    static class Son extends Father {
        public int id = 100;

        @Override
        public void printId() {
            System.out.print(id);
        }
    }

    public static void main(String[] args) {
        Father person = new Son();
        System.out.println(person.id);
        person.printId();
    }
}
```
对应的输出是:  
1  
100  
因为 person 引用对象还是 Father 的，它直接使用的成员变量 person.id 还是没有被覆盖。  
方法被子类复写之后，引用的是子类的 id 输出就变为 100 ，如果没有复写就是引用父类的方法，即输出 1。  
这个在 5.3 里面有解释

### [5.静态代码块](https://www.cnblogs.com/ysocean/p/8194428.html)
通常正常人都不会写静态代码块这个东西（还真别说，就是有这样写的）
```java
public class Main{

    static class Father {
        public static int m = 1;

        static {
            System.out.println("init father");
        }
    }

    static class Son extends Father {
        static {
            m = 100;
            System.out.println("init son");
            //System.out.println(m);
        }

        //public static int m = 150;
    }

    private Main() {

    }

    public static void main(String[] args) {
        System.out.println(Son.m);
    }
}
```
这里输出的还是 1， 父类的静态变量 m 并没有被子类的静态代码块覆盖。    
将子类的

静态代码块的一些特性：  
1.类被加载的时候执行。  
2.跟静态方法相同的是，不能访问普通变量，但静态方法属于被动执行，而静态代码块属于主动执行。  

#### 5.1 构造代码块
构造代码块即静态代码块去掉了 static，它在构造函数执行之前执行（每次创建对象都会执行一次）

#### 5.2 父子类执行顺序
静态代码块 > 构造代码块 > 构造函数 > 普通代码块  
```java
public class SuperClass {
    static{
        System.out.println("父类静态代码块");
    }
    {
        System.out.println("父类构造代码块");
    }
    public SuperClass(){
        System.out.println("父类构造函数");
    }
}

public class SubClass extends SuperClass {
    static{
        System.out.println("子类静态代码块");
    }
     
    {
        System.out.println("子类构造代码块");
    }
     
    public SubClass(){
        System.out.println("子类构造函数");
    }

    public static void main(String[] args) {
        SubClass sb = new SubClass();
        System.out.println("------------");
        SubClass sb1 = new SubClass();
    }
}
```
结果：
```text
父类静态代码块
子类静态代码块
父类构造代码块
父类构造函数
子类构造代码块
子类构造函数
------------
父类构造代码块
父类构造函数
子类构造代码块
子类构造函数
```

#### 5.3 [覆盖和隐藏](https://blog.csdn.net/u013771764/article/details/81430303)
```java
public class Main4{

    static class Father {
        public static int m = 1;

        static {
            System.out.println("init father");
        }
    }

    static class Son extends Father {
        static {
            m = 100;
            System.out.println("init son");
            //System.out.println(m);
        }

        public static int m = 150;
    }

    private Main4() {

    }

    public static void main(String[] args) {
        System.out.println(Son.m);
        Father person = new Son();
        System.out.println(person.m);
    }
}
```
这个明显就是隐藏了子类的静态变量

### 6.安全随机数生成
java.Security.SecureRandom()

### 7.只引用类静态 final 变量 类并未被加载
正确来说应该是类被加载了，但是还没被初始化
```java
class Show {
    public static final String SOMETHING = "haha";
    static {
        System.out.println("init");
    }   
}
class Test {
    public static void main(String[] args) {
        System.out.println(Show.SOMETHING);
    }
}
```

### 8.ArrayList 中元素添加 transient 的作用
{% img /images/pic_java_base_info.png %}  
1. ArrayList 重写了 readObject(), writeObject() 方法  
2. 防止扩用猴

### 9.CSRF、LDAP、ORNL是啥？

### 10.JAVA数组翻转
```
Collections.reverse(arrayList);
```
