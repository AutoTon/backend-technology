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

### 为什么是final class？

+ 性能：很多字符串操作都会产生新的String对象，会带来一定的性能消耗，JVM实现了字符串池，进而节省很多堆空间。
+ 线程安全

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

## 编译执行与解释执行

+ 编码 -> 编译 -> 解释 -> 运行
+ Javac将源代码编译成字节码，即.class文件
+ JVM的解释器将字节码转换成机器码
+ JIT编译器（Jsut-In-Time）能够在运行时将热点代码编译成机器码，这类属于编译执行
+ Write Once, Run Everywhere. 实际是JVM对不同操作系统下字节码的不同解释

## JRE与JDK

### JRE

Java运行环境，提供了JVM和基础类库

### JDK

JRE的超集，还包括了编译器和诊断工具，比如jstack、jmap等

## 异常处理

### 分类

#### Error

正常情况下，不大可能出现的情况，大多数会导致程序处于非正常、不可恢复状态，不建议捕获。

（1）OutOfMemoryError

（2）NoClassDefFoundError

+ 如果JVM或者ClassLoader实例尝试加载（可以通过正常的方法调用，也可能是使用new来创建新的对象）类的时候却找不到类的定义。要查找的类在编译的时候是存在的，运行的时候却找不到了。这个时候就会导致NoClassDefFoundError.
+ 原因：打包过程漏掉了部分类，或者jar包出现损坏或者篡改。解决这个问题的办法是查找那些在开发期间存在于类路径下但在运行期间却不在类路径下的类。

#### Exception

程序正常运行中，可以预料的意外情况。

##### 可检查异常checked

代码必须显示进行捕获。

（1）ClassNotFoundException

+ Java支持使用Class.forName方法来动态地加载类，任意一个类的类名如果被作为参数传递给这个方法都将导致该类被加载到JVM内存中，如果这个类在类路径中没有被找到，那么此时就会在运行时抛出ClassNotFoundException异常。
+ 当一个类已经某个类加载器加载到内存中了，此时另一个类加载器又尝试着动态地从同一个包中加载这个类。通过控制动态类加载过程，可以避免上述情况发生。

##### 不检查异常unchecked

即所谓的运行时异常

（1）NullPointerException

（2）ArrayIndexOutOfBoundsException

### 基本原则

+ 尽量不要捕获类似Exception这样的异常，应该捕获特定异常
+ 不要生吞异常，可打印日志或继续往上抛出
+ try-catch代码段会产生额外的性能开销，尽量不要一个大的try包住整段的代码

## 集合

### Map

#### 哈希算法

+ MD5
+ SHA算法

##### 应用 -- Bloom Filter

+ 判断元素不在集合，那么肯定不在。
+ 判断元素存在集合中，有一定的概率判断错误。

###### 场景

不适合"零错误"的应用场景。

###### 原理

原理：利用多个不同的Hash函数来确认元素是否真的存在。

#### HashTable

+ 线程安全
+ 一个对象只有一把锁，性能低。

#### HashMap

（1）底层

数组+链表（1.7）， 数组+链表/红黑树（1.8）

（2）链表过长

链表转为红黑树（jdk8）

（3）resize

容量扩大为原来的2倍，index要么不变要么加上旧容量

#### LinkedHashMap

继承HashMap，内部额外维护一个双向链表。

#### ConcurrentHashMap

##### JDK1.7

（1）线程安全

采用"分段锁"，把一个大的Map拆分成n个小的segment，根据key.hashCode()确定存放到哪个segment（初始化后segment数量不可更改）。

Segment继承了ReenTrantLock，有一个属性HashEntry[]。

（2）延迟初始化

除了第一个Segment之外，剩余的Segment采用的是延迟初始化的机制，后续元素在put方法里面初始化，采用的是自旋+CAS。

##### JDK1.8

（1）底层实现变化

+ 取消分段锁，底层使用Node<K,V>[]进行存储，对数组每一行的元素进行加锁。
+ 并发控制使用`synchronized`和`CAS`来操作。

（2）put方法的处理逻辑

+ 如果没有初始化就先调用initTable()方法来进行初始化过程
+ 如果没有hash冲突就直接CAS插入
+ 如果还在进行扩容操作就先进行扩容
+ 如果存在hash冲突，就加锁来保证线程安全，这里有两种情况，一种是链表形式就直接遍历到尾端插入，一种是红黑树就按照红黑树结构插入
+ 如果该链表的数量大于阈值8，就要先转换成黑红树的结构，break再一次进入循环
+ 如果添加成功就调用addCount()方法统计size，并且检查是否需要扩容

### List

#### Vector

线程安全，相关方法加了synchronized，数组实现。

#### ArrayList

线程不安全，数组实现。

#### LinkedList

底层是用双向链表实现。