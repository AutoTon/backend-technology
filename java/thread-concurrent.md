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

#### 为现有的线程安全类添加原子方法？

（1）修改类的源码，以添加新的原子操作

（2）集成该线程安全类，并添加原子操作

```
public class ImprovedVector<T> extends Vector<T>{  
    public synchronized boolean putIfAbsent(T x){  
        boolean flag=contains(x);  
        if(!flag)  
             add(x);  
        return !flag;  
    }  
}  
```

（3）使用客户端加锁方式

```
public class ImprovedList<T>{  
    public List<T> list=Collections.synchronizedList(new ArrayList<T>());  
    public synchronized boolean putIfAbsent(T x){  
            synchronized(list){  
            boolean flag=list.contains(x);  
            if(!flag)  
                 list.add(x);  
            return !flag;  
        }  
    }  
}  
```

（4）使用组合方式（推荐）

```
public class ImprovedList<T> implements List<T>{  
    private final List<T> list;  
    public ImprovedList(List<T> list){  
         this.list=list;  
    }  
    public synchronized boolean putIfAbsent(T x){  
            boolean flag=list.contains(x);  
            if(!flag)  
                 list.add(x);  
            return !flag;  
        }  
    }  
    ...实现List<T>接口中的其他方法  
}  
```

### 锁

#### 不可重入锁

若当前线程执行某个方法已经获取了该锁，那么在方法中尝试再次获取锁时，就会获取不到被`阻塞`。

```
public class NonReentrantLock {

    private boolean isLocked = false;
    public synchronized void lock() throws InterruptedException{
        while(isLocked){
            wait();
        }
        isLocked = true;
    }
    public synchronized void unlock(){
        isLocked = false;
        notify();
    }
}
```

#### 可重入锁

线程可以进入它已经拥有的锁的同步代码块，有一个int型统计重入次数。

```
public class ReentrantLock {

    boolean isLocked = false;
    Thread  lockedBy = null;
    int lockedCount = 0;
    public synchronized void lock()
            throws InterruptedException{
        Thread thread = Thread.currentThread();
        while(isLocked && lockedBy != thread){
            wait();
        }
        isLocked = true;
        lockedCount++;
        lockedBy = thread;
    }
    public synchronized void unlock(){
        if(Thread.currentThread() == this.lockedBy){
            lockedCount--;
            if(lockedCount == 0){
                isLocked = false;
                notify();
            }
        }
    }
}
```

##### 应用

+ synchronized
+ java.util.concurrent.locks.ReentrantLock

### JDK线程安全类

#### 同步容器

`Collections.synchronizedXxx`

封装底层容器，并对每个公有方法都进行同步。

#### 并发容器

##### ConcurrentHashMap

+ 分段锁机制
+ size()、isEmpty()返回值不精确

###### HashMap

+ hash冲突>8时，将桶的链表转为红黑树，优化查询性能；
+ hash冲突<=6时，将桶的红黑树转化为链表。不设置为8是为了避免map在临界值8时频繁插入删除元素时红黑树与链表的转化。
+ 转化都在插入时进行。

##### CopyOnWriteArrayList（读写分离）

+ 往一个容器添加元素的时候，不直接往当前容器添加，而是先将当前容器进行Copy，复制出一个新的容器，然后新的容器里添加元素，添加完元素之后，再将原容器的引用指向新的容器。
+ 允许多线程遍历，遍历过程不会抛出ConcurrentModificationException 异常，底层是生成快照，读线程对各自的快照进行遍历。

###### 适用场景

适用于`读多写少`的并发场景：比如`白名单`，`黑名单`，商品类目的访问和更新场景，假如我们有一个搜索网站，用户在这个网站的搜索框中，输入关键字搜索内容，但是某些关键字不允许被搜索。这些不能被搜索的关键字会被放在一个黑名单当中，黑名单每天晚上更新一次。当用户搜索时，会检查当前关键字在不在黑名单当中，如果在，则提示不能搜索。

```
public class BlackListServiceImpl {

    private static CopyOnWriteMap<String, Boolean> blackListMap = new CopyOnWriteMap<String, Boolean>(1000);

    public static boolean isBlackList(String id) {
        return blackListMap.get(id) == null ? false : true;
    }

    public static void addBlackList(String id) {
        blackListMap.put(id, Boolean.TRUE);
    }
    
    /**
     * 批量添加黑名单
     *
     * @param ids
     */
    public static void addBlackList(Map<String,Boolean> ids) {
        
        blackListMap.putAll(ids);
        
    }

}
```

###### 缺点

+ 内存占用问题：在进行写操作的时候，内存里会同时驻扎两个对象的内存，旧的对象和新写入的对象。如果这些对象占用的内存比较大，那么有可能造成频繁的Yong GC和Full GC。
+ 数据一致性问题： 读到的数据可能已过时

##### BlockQueue

+ 提供可阻塞的put()、take()方法，适用于生产者-消费者模式
+ `LinkedBlockingQueue`：内部使用2个可重入锁`ReentrantLock`，分别用于入队、出队。保证了同一时刻只有一个线程入队/出队。

##### BlockDeque	

双端队列，适用于工作密取模式，每个消费者有自己的消费队列，消费完可去获取其他消费者队尾的工作，减少了竞争。

#### 同步工具类

##### Lacth（闭锁）

一次性对象，一旦到达终止状态，不可被重置。

`CountDownLatch`

```
public long timeTasks(int nThreads, final Runnable task) throws InterruptedException {
        final CountDownLatch startGate = new CountDownLatch(1);
        final CountDownLatch endGate = new CountDownLatch(nThreads);
        
        for (int i = 0; i < nThreads; i++) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        startGate.await();
                        try {
                            task.run();
                        } finally {
                            endGate.countDown();
                        }
                    } catch (InterruptedException e) {
                        
                    } 
                }
            };
            t.start();
        }
        
        long start = System.nanoTime();
        startGate.countDown();
        endGate.await();
        long end = System.nanoTime();
        return end - start;
    }
```

##### FutureTask

```
public class Preloader {
    private final FutureTask<ProductInfo> future = new FutureTask<ProductInfo>(new Callable<ProductInfo>() {
        @Override
        public ProductInfo call() throws Exception {
            return loadProductInfo();
        }
    });
    private final Thread thread = new Thread(future);
    
    public void start() {
        thread.start();
    }
    
    public ProductInfo get() throws InterruptedException {
        try {
            return future.get();
        } catch (ExecutionException e) {
            // ...
        }
    }
            
}
```

##### 信号量

控制同时访问某个特定资源的操作数量。

```
public class BoundedHashSet<T> {
    private final Set<T> set;
    private final Semaphore semaphore;

    public BoundedHashSet(int bound) {
        this.set = Collections.synchronizedSet(new HashSet<T>());
        semaphore = new Semaphore(bound);
    }

    public boolean add(T t) throws InterruptedException {
        semaphore.acquire();
        boolean wasAdded = false;
        try {
            wasAdded = set.add(t);
            return wasAdded;
        } finally {
            if (!wasAdded) {
                semaphore.release();
            }
        }
    }
    
    public boolean remove(Object o) {
        boolean wasRemoved = set.remove(o);
        if (wasRemoved) {
            semaphore.release();
        }
        return wasRemoved;
    }
}
```

##### Barrier（栅栏）

阻塞一组线程直到某个事件发生。

`CyclicBarrier`

在释放等待线程之后可以被重用。（与LatchLock有区别）

```
public class CyclicBarrierTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		//如果将参数改为4，但是下面只加入了3个选手，这永远等待下去
		//Waits until all parties have invoked await on this barrier. 
		CyclicBarrier barrier = new CyclicBarrier(3);

		ExecutorService executor = Executors.newFixedThreadPool(3);
		executor.submit(new Thread(new Runner(barrier, "1号选手")));
		executor.submit(new Thread(new Runner(barrier, "2号选手")));
		executor.submit(new Thread(new Runner(barrier, "3号选手")));

		executor.shutdown();
	}
}

class Runner implements Runnable {
	// 一个同步辅助类，它允许一组线程互相等待，直到到达某个公共屏障点 (common barrier point)
	private CyclicBarrier barrier;

	private String name;

	public Runner(CyclicBarrier barrier, String name) {
		super();
		this.barrier = barrier;
		this.name = name;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000 * (new Random()).nextInt(8));
			System.out.println(name + " 准备好了...");
			// barrier的await方法，在所有参与者都已经在此 barrier 上调用 await 方法之前，将一直等待。
			barrier.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			e.printStackTrace();
		}
		System.out.println(name + " 起跑！");
	}
}
```

## 极客时间

### 并发编程

+ 分工：将一个大任务拆分为多个小任务（偏业务），并交给多线程去执行（偏实现）
+ 同步：多线程间的通信
+ 互斥：保证同一时刻只有一个线程访问共享资源。常用工具为锁。

### 三大问题

+ 可见性：CPU缓存

```
public class Test {

    private long count = 0;

    public long getCount() {
        return count;
    }

    private void add10K() {
        int idx = 0;
        while(idx++ < 10000) {
            count += 1;
        }
    }

    public static long calc() throws InterruptedException {
        final Test test = new Test();
        // 创建两个线程，执行add()操作
        Thread th1 = new Thread(()->{
            test.add10K();
        });
        Thread th2 = new Thread(()->{
            test.add10K();
        });
        // 启动两个线程
        th1.start();
        th2.start();
        // 等待两个线程执行结束
        th1.join();
        th2.join();
        return test.getCount();
    }

    public static void main(String[] args) throws InterruptedException {
        long count = calc();
        System.out.println(count);
    }
}
```

![](images/visible-problem.png)

+ 原子性：线程切换，`CPU`能保证的原子操作是`CPU指令`级别的，而不是高级语言的操作符

+ 有序性：编译器为了优化性能，有时候会改变程序中指令的先后顺序

```

public class Singleton {
  static Singleton instance;
  static Singleton getInstance(){
    if (instance == null) {
      synchronized(Singleton.class) {
        if (instance == null)
          instance = new Singleton();
        }
    }
    return instance;
  }
}
```

我们以为的`new`操作应该是：

（1）分配一块内存M；
（2）在内存M上初始化Singleton对象；
（3）然后M的地址赋值给instance变量。

但是实际上优化后的执行路径却是这样的：

（1）分配一块内存M；
（2）将M的地址赋值给instance变量；
（3）最后在内存M上初始化Singleton对象

> 在java中`Long`类型是`64位`的，在`32位`的系统中，Cpu指令要进行多次操作，无法保证原子性。

### Happens-before原则

+ 程序的顺序性规则：前面一个操作的结果对后续操作是可见的。
+ volatile变量规则：对一个volatile变量的写操作，Happens-Before于后续对这个volatile变量的读操作。
+ 传递性：如果A`Happens-Before`B，且B`Happens-Before`C，那么A`Happens-Before`C。

```
class VolatileExample {
  int x = 0;
  volatile boolean v = false;
  public void writer() {
    x = 42;
    v = true;
  }
  public void reader() {
    if (v == true) {
      // 这里x会是多少呢？
    }
  }
}
```

![](images/happens-before-example.png)

+ 管程中锁的规则：对一个锁的解锁`Happens-Before`于后续对这个锁的加锁。

> `synchronized`是Java里对管程的实现。

```

synchronized (this) { //此处自动加锁
  // x是共享变量,初始值=10
  if (this.x < 12) {
    this.x = 12; 
  }  
} //此处自动解锁
```

+ 线程start()规则：指主线程A启动子线程B后，子线程B能够看到主线程在启动子线程B前的操作。

```

Thread B = new Thread(()->{
  // 主线程调用B.start()之前
  // 所有对共享变量的修改，此处皆可见
  // 此例中，var==77
});
// 此处对共享变量var修改
var = 77;
// 主线程启动子线程
B.start();
```

+ 线程join()规则：如果在线程A中，调用线程B的join()并成功返回，那么线程B中的任意操作`Happens-Before`于该join()操作的返回。

```

Thread B = new Thread(()->{
  // 此处对共享变量var修改
  var = 66;
});
// 例如此处对共享变量修改，
// 则这个修改结果对线程B可见
// 主线程启动子线程
B.start();
B.join()
// 子线程所有对共享变量的修改
// 在主线程调用B.join()之后皆可见
// 此例中，var==66
```

### 互斥锁

