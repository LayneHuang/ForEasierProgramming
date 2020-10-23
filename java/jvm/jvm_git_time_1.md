# Java 虚拟机基本原理

## JVM 如何处理异常的？

### 异常实例构造十分昂贵
Java 虚拟机便需要生成该异常的栈轨迹（stack trace）。  
该操作会逐一访问当前线程的 Java 栈帧，并且记录下各种调试信息，  
包括栈帧所指向方法的名字，方法所在的类名、文件名，以及在代码中的第几行触发该异常。

### Suppressed 异常以及语法糖
这个新特性允许开发人员讲一个异常赋予另一个异常之上。  
因此，抛出的异常可以附带多个异常的信息。（这个是真的没关注过）  
给出的 Demo 变化
```java
public class Demo {
    public void func() {
        FileInputStream in0 = null;
        FileInputStream in1 = null;
        FileInputStream in2 = null;
        try {
            in0 = new FileInputStream(new File("in0.txt"));
            try {
                in1 = new FileInputStream(new File("in1.txt"));
                try {
                    in2 = new FileInputStream(new File("in2.txt"));
                } finally {
                    if (in2 != null) in2.close();
                }
            } finally {
                if (in1 != null) in1.close();
            }
        } finally {
            if (in0 != null) in0.close();
        }
    }
}
```
变成：
```java
public class Foo implements AutoCloseable {
    private final String name;

    public Foo(String name) {
        this.name = name;
    }

    @Override
    public void close() {
        throw new RuntimeException(name);
    }

    public static void main(String[] args) {
        try (Foo foo0 = new Foo("Foo0"); // try-with-resources
             Foo foo1 = new Foo("Foo1"); 
             Foo foo2 = new Foo("Foo2")) {
            throw new RuntimeException("Initial");
        }
    }
}
```
原来还可以这样写的。

## JVM 如何实现反射
首先是一个十分生动的例子，就是我们的 IDEA ，当对象后面打入一个点号的时候，给出的提示。  
