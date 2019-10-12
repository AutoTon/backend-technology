# 多线程与高并发

## runnable与callable区别

+ Callable规定的方法是call(),Runnable规定的方法是run().
+ Callable的任务执行后可返回值，而Runnable的任务是不能返回值
+ call方法可以抛出异常，run方法不可以
+ 运行Callable任务可以拿到一个Future对象，Future 表示异步计算的结果。它提供了检查计算是否完成的方法，以等待计算的完成，并获取计算的结果。计算完成后只能使用 get 方法来获取结果，如果线程没有执行完，Future.get()方法可能会阻塞当前线程的执行；如果线程出现异常，Future.get()会throws InterruptedException或者ExecutionException；如果线程已经取消，会跑出CancellationException。取消由cancel 方法来执行。isDone确定任务是正常完成还是被取消了。一旦计算完成，就不能再取消计算。

## 设计一个通用的高并发接口

### 做得少

（1）功能上，尽量不涉及一些难以缓存和预热的数据，比如用户ID。

+ 将活动ID作为KEY，存储成功秒杀用户ID的列表
+ 将个性化数据的功能在业务流程上后移

（2）处理信息量要少

控制接口的业务对象的数量级，若对象过多，可分次分批来处理。比如加上必要条件的筛选。

### 做的巧

（1）选择合理的业务实现方式

比如某类目下所有商品的最低价，实时读接口会比较耗时，可改为作业离线计算，计算结果写表。

（2）选择合理的缓存类型和缓存调用时机

## 秒杀实践

### 业务分析

+ 瞬时流量大
+ 参与用户多，可秒杀商品数量少
+ 请求读多写少
+ 秒杀状态转换实时性要求高

### 流程

#### 活动未开始

用户进入活动页，2种请求：一种加载活动页信息，一种查询活动状态得到未开始的结果。

#### 活动进行中

持续时间非常短，瞬时秒杀请求占比增高。

#### 活动结束

请求情况同活动开始前

### 缓存

（1）请求前端资源

varnish：将活动ID及城市ID作为KEY，缓存页面

（2）活动开始时间数据

ehcache：这个数据是一个较固定且不会发生变化的属性。

（3）剩余库存个数

memcached：全局需要一致，秒杀过程中变化非常快。

（4）秒杀队列

memcached：限制可参与秒杀的用户数量

（5）成功秒杀用户ID

redis：set结构保存

### 分库存

比如有500000个秒杀名额，分50份存储

## 并发编程

### 线程安全

#### 线程不安全的类如何在多线程环境使用？

实例封装（Java监视器模式）

（1）

将线程不安全的类封装到另一个对象，在对象加锁访问

（2）

```
public class PersonSet {
    
    private final Set<Person> mySet = new HashSet<>();
    
    public synchronized void addPerson(Person person) {
        mySet.add(person);
    }

    public synchronized boolean containsPerson(Person person) {
        return mySet.contains(person);
    }
    
}
```

（3）

```
public class PersonSet {

    private final Object myLock = new Object();
    Widget widget;

    void someMethod() {
        synchronized (myLock) {
            // 访问或修改widget的状态
        }
    }

}
```