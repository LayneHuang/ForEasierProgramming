# 四.设计模式与范式 —— 行为型

## 观察者模式
观察者模式其实就是订阅者发布者模式

### 1.Java内置的API支持  
#### 观察者（订阅者）：
```java
public interface Observer {
    void update(Observable o, Object arg);
}
```
订阅者就相对简单很多了，当发布者有变化的时候，就会调用接口的update()方法进行更新。  

#### 被观察者（发布者）：
```java
public class Observable {
    private boolean changed = false;
    private Vector<Observer> obs;

    public Observable() {
        obs = new Vector<>();
    }
    public synchronized void addObserver(Observer o) {
        if (o == null)
            throw new NullPointerException();
        if (!obs.contains(o)) {
            obs.addElement(o);
        }
    }
    public synchronized void deleteObserver(Observer o) {
        obs.removeElement(o);
    }
    public void notifyObservers() {
        notifyObservers(null);
    }
    public void notifyObservers(Object arg) {
        Object[] arrLocal;

        synchronized (this) {
            if (!changed)
                return;
            arrLocal = obs.toArray();
            clearChanged();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((Observer)arrLocal[i]).update(this, arg);
    }
    public synchronized void deleteObservers() {
        obs.removeAllElements();
    }
    protected synchronized void setChanged() {
        changed = true;
    }
    protected synchronized void clearChanged() {
        changed = false;
    }
    public synchronized boolean hasChanged() {
        return changed;
    }
    public synchronized int countObservers() {
        return obs.size();
    }
}
```
可以看得出来，订阅者列表是用Vector实现的，保证线程安全。  
changed控制是否去更新观察者，如果调用notifyObservers()之前没有先调用setChanged()，观察者就“不会”被通知。（为不同场景提供了弹性）  
而这个实现也有缺点，就是Observable是一个类，而不是接口。违反了设计原则针对接口而非实现编程，不方便扩展。  
  

