# java基础

## 基本数据类型（8种）

+ boolean
+ char
+ byte
+ short
+ int：32位
+ long
+ float
+ double：小数默认类型

## finalize使用

### 场景一

发生在jvm进行垃圾回收前调用的方法，所以这个方法所代表的含义就是在jvm垃圾回收前所需要做的有关于内存回收的操作。

### 场景二

重写了finalize的类实例创建时JVM会多创建一个对应的Finalizer对象（指向该实例），GC的时候并不会直接回收实例，而是放置到Finalizer的一个处理队列，至少要经过2次GC才能回收，销毁的速度比创建的速度会慢很多，而且每个实例都会驻留对应的Finalizer对象。

## Integer

（1）Integer f1=100, f2=100, f3=150, f4=150; f1==f2（true）， f3 == f4（false）

答案：当我们给一个Integer对象赋一个int值的时候，会调用Integer类的静态方法valueOf，如果整型字面量的值在-128到127之间，那么不会new新的Integer对象，而是直接引用常量池中的Integer对象

> 注：上限值是可以设置的：-XX:AutoBoxCacheMax=N

（2）存在i+1<i的数

Integer.MAX_VALUE

## String

（1）为什么是final class？

很多字符串操作都会产生新的String对象，会带来一定的性能消耗

### 字符串拼接

底层都是可修改的char[]/byte[]（JDK9），初始化长度为初始字符串长度+16，可能需要经过多次扩容，尽可能初始化时指定长度。

+ StringBuffer：线程安全，性能稍低，通过把各种修改数据的方法都加上`synchronized`关键字实现
+ StringBuilder：线程不安全

## 引用

### 强引用

+ 程序`new`一个对象，并赋给一个引用变量，这个引用变量就是强引用。
+ 不会被GC回收。

### 软引用

+ 如果一个对象只具有软引用，则内存空间足够，垃圾回收器就不会回收它；如果内存空间不足了，就会回收这些对象的内存。
+ 可通过get()获取原对象，将引用转化为强引用。
+ 可关联到ReferenceQueue。

#### 使用

```
SoftReference ref = new SoftReference(new MyDate());
```

#### 场景

可用来实现内存敏感的高速缓存。

### 弱引用

+ gc线程活动的时候，一旦发现了只具有弱引用的对象，不管当前内存空间足够与否，都会回收它的内存。
+ 可通过get()获取原对象，将引用转化为强引用。
+ 可关联到ReferenceQueue。

#### 使用

```
WeakReference ref = new WeakReference(new MyDate());
```

#### 场景

常见于图片缓存

### 幻象引用（虚引用）

+ 如果一个对象仅持有虚引用，那么它就和没有任何引用一样，在任何时候都可能被垃圾回收器回收。
+ 可用来跟踪对象被垃圾回收器回收的活动，当一个虚引用关联的对象被垃圾收集器回收之前会收到一条系统通知。
+ get()方法返回的一直是null。

#### 使用

```
ReferenceQueue queue = new ReferenceQueue();
PhantomReference ref = new PhantomReference(new MyDate(), queue);
```

### Reachability Fence

+ 保护没有强引用的对象不被GC回收。
+ 例如：new Resource().action() 这在异步编程中很常见。

