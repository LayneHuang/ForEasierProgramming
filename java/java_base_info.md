# Java 基础简单汇总

### 1.函数式接口
用到 Comparator 时看到它有注解定义 @FunctionalInterface （函数式接口）  
而函数式接口具有以下特点(与普通接口不同的点):  
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
```

### 3.getCanonicalPath

1.getAbsolutePath():  
返回的是定义时的路径对应的相对路径，但不会处理“.”和“..”的情况    

2.getCanonicalPath():  
返回的是规范化的绝对路径，相当于将getAbsolutePath()中的“.”和“..”解析成对应的正确的路径  

举例如下：
```java
File file = new File(".\\test.txt");
System.out.println(file.getPath());
System.out.println(file.getAbsolutePath());
System.out.println(file.getCanonicalPath());

 /*
返回的结果为:
    .\test.txt
    E:\workspace\Test\.\test.txt
    E:\workspace\Test\test.txt
    “..”的情况类似
*/
```