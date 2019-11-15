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

### 死锁

4个必要条件：

+ 互斥，共享资源X和Y只能被一个线程占用；（无法破坏）
+ 占有且等待，线程T1已经取得共享资源X，在等待共享资源Y的时候，不释放共享资源X；
+ 不可抢占，其他线程不能强行抢占线程T1占有的资源；
+ 循环等待，线程T1等待线程T2占有的资源，线程T2等待线程T1占有的资源，就是循环等待。

破坏其他三个条件之一，即可解决`死锁`。

+ 占用且等待：我们可以一次性申请所有的资源，这样就不存在等待了。

```

class Allocator {
  private List<Object> als =
    new ArrayList<>();
  // 一次性申请所有资源
  synchronized boolean apply(
    Object from, Object to){
    if(als.contains(from) ||
         als.contains(to)){
      return false;  
    } else {
      als.add(from);
      als.add(to);  
    }
    return true;
  }
  // 归还资源
  synchronized void free(
    Object from, Object to){
    als.remove(from);
    als.remove(to);
  }
}

class Account {
  // actr应该为单例
  private Allocator actr;
  private int balance;
  // 转账
  void transfer(Account target, int amt){
    // 一次性申请转出账户和转入账户，直到成功
    while(!actr.apply(this, target))
      ；
    try{
      // 锁定转出账户
      synchronized(this){              
        // 锁定转入账户
        synchronized(target){           
          if (this.balance > amt){
            this.balance -= amt;
            target.balance += amt;
          }
        }
      }
    } finally {
      actr.free(this, target)
    }
  } 
}
```

+ 不可抢占：占用部分资源的线程进一步申请其他资源时，如果申请不到，可以主动释放它占有的资源，这样不可抢占这个条件就破坏掉了。

> `synchronized`做不到，可以用`java.util.concurrent`这个包下面提供的`Lock`。

+ 循环等待：可以靠按序申请资源来预防。所谓按序申请，是指资源是有线性顺序的，申请的时候可以先申请资源序号小的，再申请资源序号大的，这样线性化后自然就不存在循环了。

```

class Account {
  private int id;
  private int balance;
  // 转账
  void transfer(Account target, int amt){
    Account left = this        ①
    Account right = target;    ②
    if (this.id > target.id) { ③
      left = target;           ④
      right = this;            ⑤
    }                          ⑥
    // 锁定序号小的账户
    synchronized(left){
      // 锁定序号大的账户
      synchronized(right){ 
        if (this.balance > amt){
          this.balance -= amt;
          target.balance += amt;
        }
      }
    }
  } 
}
```

### 等待-通知机制

```

class Allocator {
  private List<Object> als;
  // 一次性申请所有资源
  synchronized void apply(
    Object from, Object to){
    // 经典写法
    while(als.contains(from) ||
         als.contains(to)){
      try{
        // 释放锁
        wait();
      }catch(Exception e){
      }   
    } 
    als.add(from);
    als.add(to);  
  }
  // 归还资源
  synchronized void free(
    Object from, Object to){
    als.remove(from);
    als.remove(to);
    notifyAll();
  }
}
```

> notify()是会随机地通知等待队列中的一个线程，而notifyAll()会通知等待队列中的所有线程。尽量使用notifyAll()，避免有些线程永远不会被通知。被唤醒后线程会去竞争锁，竞争成功后继续执行wait()之后的代码。

> wait()会释放锁。

> 只能在synchronized中使用。

### 性能的度量指标

+ 吞吐量：指的是单位时间内能处理的请求数量。吞吐量越高，说明性能越好。
+ 延迟：指的是从发出请求到收到响应的时间。延迟越小，说明性能越好。
+ 并发量：指的是能同时处理的请求数量，一般来说随着并发量的增加、延迟也会增加。所以延迟这个指标，一般都会是基于并发量来说的。例如并发量是 1000 的时候，延迟是 50 毫秒。

### 管程（Monitor）

管程和信号量是等价的，所谓等价指的是用管程能够实现信号量，也能用信号量实现管程。

指的是管理共享变量以及对共享变量的操作过程，让他们支持并发。

#### MESA模型

![](images/mesa-model.png)

##### 编程范式

```
while(条件不满足) {
  wait();
}
```

##### 示例

```

public class BlockedQueue<T>{
  final Lock lock =
    new ReentrantLock();
  // 条件变量：队列不满  
  final Condition notFull =
    lock.newCondition();
  // 条件变量：队列不空  
  final Condition notEmpty =
    lock.newCondition();

  // 入队
  void enq(T x) {
    lock.lock();
    try {
      while (队列已满){
        // 等待队列不满 
        notFull.await();
      }  
      // 省略入队操作...
      //入队后,通知可出队
      notEmpty.signal();
    }finally {
      lock.unlock();
    }
  }
  // 出队
  void deq(){
    lock.lock();
    try {
      while (队列已空){
        // 等待队列不空
        notEmpty.await();
      }
      // 省略出队操作...
      //出队后，通知可入队
      notFull.signal();
    }finally {
      lock.unlock();
    }  
  }
}
```

#### JAVA简易实现

基于MESA模型，synchronized 关键字修饰的代码块，在编译期会自动生成相关加锁和解锁的代码，只支持一个条件变量。

![](images/java-monitor.png)

> wait()若加了超时的参数，超时后，会从条件变量的等待队列放入入口等待队列。

### 线程

#### 通用的生命周期

![](images/thread-period.png)

+ 初始状态，指的是线程已经被创建，但是还不允许分配 CPU 执行。这个状态属于编程语言特有的，不过这里所谓的被创建，仅仅是在编程语言层面被创建，而在操作系统层面，真正的线程还没有创建。
+ 可运行状态，指的是线程可以分配 CPU 执行。在这种状态下，真正的操作系统线程已经被成功创建了，所以可以分配 CPU 执行。
+ 当有空闲的 CPU 时，操作系统会将其分配给一个处于可运行状态的线程，被分配到 CPU 的线程的状态就转换成了运行状态。
+ 运行状态的线程如果调用一个阻塞的 API（例如以阻塞方式读文件）或者等待某个事件（例如条件变量），那么线程的状态就会转换到休眠状态，同时释放 CPU 使用权，休眠状态的线程永远没有机会获得 CPU 使用权。当等待的事件出现了，线程就会从休眠状态转换到可运行状态。
+ 线程执行完或者出现异常就会进入终止状态，终止状态的线程不会切换到其他任何状态，进入终止状态也就意味着线程的生命周期结束了。

#### Java中线程的生命周期

+ NEW（初始化状态）
+ RUNNABLE（可运行 / 运行状态）
+ BLOCKED（阻塞状态）
+ WAITING（无时限等待）
+ TIMED_WAITING（有时限等待）
+ TERMINATED（终止状态）

#### 创建线程的数量

##### IO密集型计算

I/O操作执行的时间相对于CPU计算来说都非常长

最佳线程数 = CPU 核数 * [ 1 +（I/O耗时 / CPU耗时）]

##### CPU密集型计算

线程数量 = CPU核数 + 1

### Lock

+ 能够响应中断。synchronized的问题是，持有锁A后，如果尝试获取锁B失败，那么线程就进入阻塞状态，一旦发生死锁，就没有任何机会来唤醒阻塞的线程。但如果阻塞状态的线程能够响应中断信号，也就是说当我们给阻塞的线程发送中断信号的时候，能够唤醒它，那它就有机会释放曾经持有的锁A。这样就破坏了不可抢占条件了。
+ 支持超时。如果线程在一段时间之内没有获取到锁，不是进入阻塞状态，而是返回一个错误，那这个线程也有机会释放曾经持有的锁。这样也能破坏不可抢占条件。
+ 非阻塞地获取锁。如果尝试获取锁失败，并不进入阻塞状态，而是直接返回，那这个线程也有机会释放曾经持有的锁。这样也能破坏不可抢占条件。

```
// 支持中断的API
void lockInterruptibly() 
  throws InterruptedException;
// 支持超时的API
boolean tryLock(long time, TimeUnit unit) 
  throws InterruptedException;
// 支持非阻塞获取锁的API
boolean tryLock();
```

实现上利用了`volatil`e相关的Happens-Before规则来保证多线程的可见性，Java SDK里面的ReentrantLock，内部持有一个volatile的成员变量state。

#### 可重入锁（ReentrantLock）

线程可以重复获取同一把锁。

```

class X {
  private final Lock rtl =
  new ReentrantLock();
  int value;
  public int get() {
    // 获取锁
    rtl.lock();         ②
    try {
      return value;
    } finally {
      // 保证锁能释放
      rtl.unlock();
    }
  }
  public void addOne() {
    // 获取锁
    rtl.lock();  
    try {
      value = 1 + get(); ①
    } finally {
      // 保证锁能释放
      rtl.unlock();
    }
  }
}
```

#### 用锁的最佳实践

+ 永远只在更新对象的成员变量时加锁
+ 永远只在访问可变的成员变量时加锁
+ 永远不在调用其他对象的方法时加锁

### Condition

实现了管程模型里面的`条件变量`。

```

public class BlockedQueue<T>{
  final Lock lock =
    new ReentrantLock();
  // 条件变量：队列不满  
  final Condition notFull =
    lock.newCondition();
  // 条件变量：队列不空  
  final Condition notEmpty =
    lock.newCondition();

  // 入队
  void enq(T x) {
    lock.lock();
    try {
      while (队列已满){
        // 等待队列不满
        notFull.await();
      }  
      // 省略入队操作...
      //入队后,通知可出队
      notEmpty.signal();
    }finally {
      lock.unlock();
    }
  }
  // 出队
  void deq(){
    lock.lock();
    try {
      while (队列已空){
        // 等待队列不空
        notEmpty.await();
      }  
      // 省略出队操作...
      //出队后，通知可入队
      notFull.signal();
    }finally {
      lock.unlock();
    }  
  }
}
```

异步转同步示例

```

// 创建锁与条件变量
private final Lock lock 
    = new ReentrantLock();
private final Condition done 
    = lock.newCondition();

// 调用方通过该方法等待结果
Object get(int timeout){
  long start = System.nanoTime();
  lock.lock();
  try {
  while (!isDone()) {
    done.await(timeout);
      long cur=System.nanoTime();
    if (isDone() || 
          cur-start > timeout){
      break;
    }
  }
  } finally {
  lock.unlock();
  }
  if (!isDone()) {
  throw new TimeoutException();
  }
  return returnFromResponse();
}
// RPC结果是否已经返回
boolean isDone() {
  return response != null;
}
// RPC结果返回时调用该方法   
private void doReceived(Response res) {
  lock.lock();
  try {
    response = res;
    if (done != null) {
      done.signal();
    }
  } finally {
    lock.unlock();
  }
}
```

### 信号量（Semaphore）

Semaphore可以允许多个线程访问一个临界区。

![](images/semaphore-model.png)

+ init()：设置计数器的初始值。
+ down()：计数器的值减1；如果此时计数器的值小于0，则当前线程将被阻塞，否则当前线程可以继续执行。对应Java SDK并发包里的acquire()。
+ up()：计数器的值加1；如果此时计数器的值小于或者等于0，则唤醒等待队列中的一个线程，并将其从等待队列中移除。对应Java SDK并发包里的release()。

```

class Semaphore{
  // 计数器
  int count;
  // 等待队列
  Queue queue;
  // 初始化操作
  Semaphore(int c){
    this.count=c;
  }
  // 
  void down(){
    this.count--;
    if(this.count<0){
      //将当前线程插入等待队列
      //阻塞当前线程
    }
  }
  void up(){
    this.count++;
    if(this.count<=0) {
      //移除等待队列中的某个线程T
      //唤醒线程T
    }
  }
}
```

#### 限流器

```

class ObjPool<T, R> {
  final List<T> pool;
  // 用信号量实现限流器
  final Semaphore sem;
  // 构造函数
  ObjPool(int size, T t){
    pool = new Vector<T>(){};
    for(int i=0; i<size; i++){
      pool.add(t);
    }
    sem = new Semaphore(size);
  }
  // 利用对象池的对象，调用func
  R exec(Function<T,R> func) {
    T t = null;
    sem.acquire();
    try {
      t = pool.remove(0);
      return func.apply(t);
    } finally {
      pool.add(t);
      sem.release();
    }
  }
}
// 创建对象池
ObjPool<Long, String> pool = 
  new ObjPool<Long, String>(10, 2);
// 通过对象池获取t，之后执行  
pool.exec(t -> {
    System.out.println(t);
    return t.toString();
});
```

### 读写锁（ReadWriteLock）

+ 允许多个线程同时读共享变量；
+ 只允许一个线程写共享变量；
+ 如果一个写线程正在执行写操作，此时禁止读线程读共享变量。

#### 缓存的快速实现

```
class Cache<K,V> {
  final Map<K, V> m =
    new HashMap<>();
  final ReadWriteLock rwl =
    new ReentrantReadWriteLock();
  // 读锁
  final Lock r = rwl.readLock();
  // 写锁
  final Lock w = rwl.writeLock();
  // 读缓存
  V get(K key) {
    r.lock();
    try { return m.get(key); }
    finally { r.unlock(); }
  }
  // 写缓存
  V put(K key, V value) {
    w.lock();
    try { return m.put(key, v); }
    finally { w.unlock(); }
  }
}
```

##### 实现缓存的按需加载

```

class Cache<K,V> {
  final Map<K, V> m =
    new HashMap<>();
  final ReadWriteLock rwl = 
    new ReentrantReadWriteLock();
  final Lock r = rwl.readLock();
  final Lock w = rwl.writeLock();
 
  V get(K key) {
    V v = null;
    //读缓存
    r.lock();         ①
    try {
      v = m.get(key); ②
    } finally{
      r.unlock();     ③
    }
    //缓存中存在，返回
    if(v != null) {   ④
      return v;
    }  
    //缓存中不存在，查询数据库
    w.lock();         ⑤
    try {
      //再次验证
      //其他线程可能已经查询过数据库
      v = m.get(key); ⑥
      if(v == null){  ⑦
        //查询数据库
        v=省略代码无数
        m.put(key, v);
      }
    } finally{
      w.unlock();
    }
    return v; 
  }
}
```

##### 锁的降级

```

class CachedData {
  Object data;
  volatile boolean cacheValid;
  final ReadWriteLock rwl =
    new ReentrantReadWriteLock();
  // 读锁  
  final Lock r = rwl.readLock();
  //写锁
  final Lock w = rwl.writeLock();
  
  void processCachedData() {
    // 获取读锁
    r.lock();
    if (!cacheValid) {
      // 释放读锁，因为不允许读锁的升级
      r.unlock();
      // 获取写锁
      w.lock();
      try {
        // 再次检查状态  
        if (!cacheValid) {
          data = ...
          cacheValid = true;
        }
        // 释放写锁前，降级为读锁
        // 降级是可以的
        r.lock(); ①
      } finally {
        // 释放写锁
        w.unlock(); 
      }
    }
    // 此处仍然持有读锁
    try {use(data);} 
    finally {r.unlock();}
  }
}
```

#### StampedLock

比读写锁性能更好的锁，JDK1.8+支持。

三种模式：

+ 写锁：类似读写锁的写锁，但加锁成功之后，会返回一个stamp，解锁需要传入这个stamp。
+ 悲观读锁：类似读写锁的读锁，但加锁成功之后，会返回一个stamp，解锁需要传入这个stamp。
+ 乐观读：无锁操作。允许一个线程获取写锁。

注意事项：

+ 不支持重入。
+ 不支持条件变量。
+ 一定不要调用中断操作，如果需要支持中断功能，一定使用可中断的悲观读锁 readLockInterruptibly() 和写锁 writeLockInterruptibly()。

##### 读模板

```
final StampedLock sl = 
  new StampedLock();

// 乐观读
long stamp = 
  sl.tryOptimisticRead();
// 读入方法局部变量
......
// 校验stamp
if (!sl.validate(stamp)){
  // 升级为悲观读锁
  stamp = sl.readLock();
  try {
    // 读入方法局部变量
    .....
  } finally {
    //释放悲观读锁
    sl.unlockRead(stamp);
  }
}
//使用方法局部变量执行业务操作
......
```

##### 写模板

```
long stamp = sl.writeLock();
try {
  // 写共享变量
  ......
} finally {
  sl.unlockWrite(stamp);
}
```

### 计数器

#### CountDownLatch

主要用来解决一个线程等待多个线程的场景。

```
// 创建2个线程的线程池
Executor executor = 
  Executors.newFixedThreadPool(2);
while(存在未对账订单){
  // 计数器初始化为2
  CountDownLatch latch = 
    new CountDownLatch(2);
  // 查询未对账订单
  executor.execute(()-> {
    pos = getPOrders();
    latch.countDown();
  });
  // 查询派送单
  executor.execute(()-> {
    dos = getDOrders();
    latch.countDown();
  });
  
  // 等待两个查询操作结束
  latch.await();
  
  // 执行对账操作
  diff = check(pos, dos);
  // 差异写入差异库
  save(diff);
}
```

#### CyclicBarrier

一组线程之间互相等待。CyclicBarrier的计数器是可以循环利用的，而且具备自动重置的功能，一旦计数器减到0会自动重置到你设置的初始值。还可以设置回调函数。

```
// 订单队列
Vector<P> pos;
// 派送单队列
Vector<D> dos;
// 执行回调的线程池 
Executor executor = 
  Executors.newFixedThreadPool(1);
final CyclicBarrier barrier =
  new CyclicBarrier(2, ()->{
    executor.execute(()->check());
  });
  
void check(){
  P p = pos.remove(0);
  D d = dos.remove(0);
  // 执行对账操作
  diff = check(p, d);
  // 差异写入差异库
  save(diff);
}
  
void checkAll(){
  // 循环查询订单库
  Thread T1 = new Thread(()->{
    while(存在未对账订单){
      // 查询订单库
      pos.add(getPOrders());
      // 等待
      barrier.await();
    }
  });
  T1.start();  
  // 循环查询运单库
  Thread T2 = new Thread(()->{
    while(存在未对账订单){
      // 查询运单库
      dos.add(getDOrders());
      // 等待
      barrier.await();
    }
  });
  T2.start();
}
```

### 容器

#### 同步容器

基于`synchronized`关键字实现线程安全。用迭代器遍历时要加锁。

```
List list = Collections.
  synchronizedList(new ArrayList());
synchronized (list) {  
  Iterator i = list.iterator(); 
  while (i.hasNext())
    foo(i.next());
}    
```

#### 并发容器

队列里只有ArrayBlockingQueue和LinkedBlockingQueue是支持有界的，所以在使用其他无界队列时，一定要充分考虑是否存在导致OOM的隐患。

##### CopyOnWriteArrayList

+ 仅适用于写操作非常少的场景，而且能够容忍读写的短暂不一致。
+ CopyOnWriteArrayList迭代器是只读的，不支持增删改。因为迭代器遍历的仅仅是一个快照，而对快照进行增删改是没有意义的。

##### ConcurrentHashMap

key是无序的。key和value都不能为空。

##### ConcurrentSkipListMap

key是有序的。key和value都不能为空。

##### CopyOnWriteArraySet

类比CopyOnWriteArrayList。

##### ConcurrentSkipListSet

类比ConcurrentSkipListMap。

##### 单端阻塞队列

只能队尾入队，队首出队。当队列已满时，入队操作阻塞；当队列已空时，出队操作阻塞。

+ ArrayBlockingQueue
+ LinkedBlockingQueue
+ SynchronousQueue
+ LinkedTransferQueue
+ PriorityBlockingQueue 
+ DelayQueue

##### 双端阻塞队列

LinkedBlockingDeque

##### 单端非阻塞队列

ConcurrentLinkedQueue

##### 双端非阻塞队列

ConcurrentLinkedDeque

### 无锁方案

CPU提供的CAS指令。CAS指令包含3个参数：共享变量的内存地址A、用于比较的值B 和共享变量的新值C；并且只有当内存中地址A处的值等于B时，才能将内存中地址A处的值更新为新值C。

CAS+自旋

```

class SimulatedCAS{
  volatile int count;
  // 实现count+=1
  addOne(){
    do {
      newValue = count+1; //①
    }while(count !=
      cas(count,newValue) //②
  }
  // 模拟实现CAS，仅用来帮助理解
  synchronized int cas(
    int expect, int newValue){
    // 读目前count的值
    int curValue = count;
    // 比较目前count值是否==期望值
    if(curValue == expect){
      // 如果是，则更新count的值
      count= newValue;
    }
    // 返回写入前的值
    return curValue;
  }
}
```

> 存在ABA问题。解决方案是增加一个版本维度。

以下的无锁方案是针对一个共享变量的，如果需要解决多个变量的原子性问题，建议还是使用互斥锁方案。

#### 原子化的基本数据类型

+ AtomicBoolean
+ AtomicInteger 
+ AtomicLong

#### 原子化的对象引用类型

+ AtomicReference
+ AtomicStampedReference 
+ AtomicMarkableReference

#### 原子化数组

+ AtomicIntegerArray
+ AtomicLongArray 
+ AtomicReferenceArray

#### 原子化对象属性更新器

+ AtomicIntegerFieldUpdater
+ AtomicLongFieldUpdater 
+ AtomicReferenceFieldUpdater

> 对象属性必须是volatile类型的，只有这样才能保证可见性。利用它们可以原子化地更新对象的属性，底层利用反射机制实现。

#### 原子化的累加器

+ DoubleAccumulator
+ DoubleAdder
+ LongAccumulator 
+ LongAdder

### 线程池

```
//简化的线程池，仅用来说明工作原理
class MyThreadPool{
  //利用阻塞队列实现生产者-消费者模式
  BlockingQueue<Runnable> workQueue;
  //保存内部工作线程
  List<WorkerThread> threads 
    = new ArrayList<>();
  // 构造方法
  MyThreadPool(int poolSize, 
    BlockingQueue<Runnable> workQueue){
    this.workQueue = workQueue;
    // 创建工作线程
    for(int idx=0; idx<poolSize; idx++){
      WorkerThread work = new WorkerThread();
      work.start();
      threads.add(work);
    }
  }
  // 提交任务
  void execute(Runnable command){
    workQueue.put(command);
  }
  // 工作线程负责消费任务，并执行任务
  class WorkerThread extends Thread{
    public void run() {
      //循环取任务并执行
      while(true){ ①
        Runnable task = workQueue.take();
        task.run();
      } 
    }
  }  
}

/** 下面是使用示例 **/
// 创建有界阻塞队列
BlockingQueue<Runnable> workQueue = 
  new LinkedBlockingQueue<>(2);
// 创建线程池  
MyThreadPool pool = new MyThreadPool(
  10, workQueue);
// 提交任务  
pool.execute(()->{
    System.out.println("hello");
});
```

#### ThreadPoolExecutor

```
ThreadPoolExecutor(
  int corePoolSize,
  int maximumPoolSize,
  long keepAliveTime,
  TimeUnit unit,
  BlockingQueue<Runnable> workQueue,
  ThreadFactory threadFactory,
  RejectedExecutionHandler handler) 
```

+ corePoolSize：最小保留线程数
+ maximumPoolSize：最大线程数
+ keepAliveTime & unit：空闲线程的存活时间
+ workQueue：工作队列
+ threadFactory：自定义如何创建线程，例如可以给线程指定一个有意义的名字。
+ handler：自定义任务的拒绝策略。如果线程池中所有的线程都在忙碌，并且工作队列也满了（前提是工作队列是有界队列），那么此时提交任务，线程池就会拒绝接收。

##### 拒绝策略

+ CallerRunsPolicy：提交任务的线程自己去执行该任务。
+ AbortPolicy：`默认`的拒绝策略，会throws RejectedExecutionException。
+ DiscardPolicy：直接丢弃任务，没有任何异常抛出。
+ DiscardOldestPolicy：丢弃最老的任务，其实就是把最早进入工作队列的任务丢弃，然后把新任务加入到工作队列。

##### 注意事项

+ 强烈建议使用有界队列。负载高时无界队列会导致OOM，导致所有请求都无法处理。
+ 默认拒绝策略要慎重使用。会抛出运行时异常。

##### 获取任务执行结果

```
// 提交Runnable任务
Future<?> 
  submit(Runnable task);
// 提交Callable任务
<T> Future<T> 
  submit(Callable<T> task);
// 提交Runnable任务及结果引用  
<T> Future<T> 
  submit(Runnable task, T result);
```

###### Future接口

API如下：

```
// 取消任务
boolean cancel(
  boolean mayInterruptIfRunning);
// 判断任务是否已取消  
boolean isCancelled();
// 判断任务是否已结束
boolean isDone();
// 获得任务执行结果，调用时若任务未结束则会阻塞
get();
// 获得任务执行结果，调用时若任务未结束则会阻塞，支持超时
get(long timeout, TimeUnit unit);
```

使用示例：

```
ExecutorService executor 
  = Executors.newFixedThreadPool(1);
// 创建Result对象r
Result r = new Result();
r.setAAA(a);
// 提交任务
Future<Result> future = 
  executor.submit(new Task(r), r);  
Result fr = future.get();
// 下面等式成立
fr === r;
fr.getAAA() === a;
fr.getXXX() === x

class Task implements Runnable{
  Result r;
  //通过构造函数传入result
  Task(Result r){
    this.r = r;
  }
  void run() {
    //可以操作result
    a = r.getAAA();
    r.setXXX(x);
  }
}
```

###### FutureTask

实现了Runnable和Future接口。

```
// 创建FutureTask
FutureTask<Integer> futureTask
  = new FutureTask<>(()-> 1+2);
// 创建线程池
ExecutorService es = 
  Executors.newCachedThreadPool();
// 提交FutureTask 
es.submit(futureTask);
// 获取计算结果
Integer result = futureTask.get();
```

###### `烧水泡茶`案例

![](images/make-tea.png)

```
// 创建任务T2的FutureTask
FutureTask<String> ft2
  = new FutureTask<>(new T2Task());
// 创建任务T1的FutureTask
FutureTask<String> ft1
  = new FutureTask<>(new T1Task(ft2));
// 线程T1执行任务ft1
Thread T1 = new Thread(ft1);
T1.start();
// 线程T2执行任务ft2
Thread T2 = new Thread(ft2);
T2.start();
// 等待线程T1执行结果
System.out.println(ft1.get());

// T1Task需要执行的任务：
// 洗水壶、烧开水、泡茶
class T1Task implements Callable<String>{
  FutureTask<String> ft2;
  // T1任务需要T2任务的FutureTask
  T1Task(FutureTask<String> ft2){
    this.ft2 = ft2;
  }
  @Override
  String call() throws Exception {
    System.out.println("T1:洗水壶...");
    TimeUnit.SECONDS.sleep(1);
    
    System.out.println("T1:烧开水...");
    TimeUnit.SECONDS.sleep(15);
    // 获取T2线程的茶叶  
    String tf = ft2.get();
    System.out.println("T1:拿到茶叶:"+tf);

    System.out.println("T1:泡茶...");
    return "上茶:" + tf;
  }
}
// T2Task需要执行的任务:
// 洗茶壶、洗茶杯、拿茶叶
class T2Task implements Callable<String> {
  @Override
  String call() throws Exception {
    System.out.println("T2:洗茶壶...");
    TimeUnit.SECONDS.sleep(1);

    System.out.println("T2:洗茶杯...");
    TimeUnit.SECONDS.sleep(2);

    System.out.println("T2:拿茶叶...");
    TimeUnit.SECONDS.sleep(1);
    return "龙井";
  }
}
// 一次执行结果：
T1:洗水壶...
T2:洗茶壶...
T1:烧开水...
T2:洗茶杯...
T2:拿茶叶...
T1:拿到茶叶:龙井
T1:泡茶...
上茶:龙井
```

### CompletableFuture

```
//任务1：洗水壶->烧开水
CompletableFuture<Void> f1 = 
  CompletableFuture.runAsync(()->{
  System.out.println("T1:洗水壶...");
  sleep(1, TimeUnit.SECONDS);

  System.out.println("T1:烧开水...");
  sleep(15, TimeUnit.SECONDS);
});
//任务2：洗茶壶->洗茶杯->拿茶叶
CompletableFuture<String> f2 = 
  CompletableFuture.supplyAsync(()->{
  System.out.println("T2:洗茶壶...");
  sleep(1, TimeUnit.SECONDS);

  System.out.println("T2:洗茶杯...");
  sleep(2, TimeUnit.SECONDS);

  System.out.println("T2:拿茶叶...");
  sleep(1, TimeUnit.SECONDS);
  return "龙井";
});
//任务3：任务1和任务2完成后执行：泡茶
CompletableFuture<String> f3 = 
  f1.thenCombine(f2, (__, tf)->{
    System.out.println("T1:拿到茶叶:" + tf);
    System.out.println("T1:泡茶...");
    return "上茶:" + tf;
  });
//等待任务3执行结果
System.out.println(f3.join());

void sleep(int t, TimeUnit u) {
  try {
    u.sleep(t);
  }catch(InterruptedException e){}
}
// 一次执行结果：
T1:洗水壶...
T2:洗茶壶...
T1:烧开水...
T2:洗茶杯...
T2:拿茶叶...
T1:拿到茶叶:龙井
T1:泡茶...
上茶:龙井
```

```
//使用默认线程池
static CompletableFuture<Void> 
  runAsync(Runnable runnable)
static <U> CompletableFuture<U> 
  supplyAsync(Supplier<U> supplier)
//可以指定线程池  
static CompletableFuture<Void> 
  runAsync(Runnable runnable, Executor executor)
static <U> CompletableFuture<U> 
  supplyAsync(Supplier<U> supplier, Executor executor)  
```

> 根据不同的业务类型创建不同的线程池，以避免互相干扰。默认使用的是公共的java.util.concurrent.ForkJoinPool。

#### CompletionStage接口

##### 串行

```
CompletableFuture<String> f0 = 
  CompletableFuture.supplyAsync(
    () -> "Hello World")      //①
  .thenApply(s -> s + " QQ")  //②
  .thenApply(String::toUpperCase);//③

System.out.println(f0.join());
//输出结果
HELLO WORLD QQ
```

##### AND聚合

```
CompletionStage<R> thenCombine(other, fn);
CompletionStage<R> thenCombineAsync(other, fn);
CompletionStage<Void> thenAcceptBoth(other, consumer);
CompletionStage<Void> thenAcceptBothAsync(other, consumer);
CompletionStage<Void> runAfterBoth(other, action);
CompletionStage<Void> runAfterBothAsync(other, action);
```

##### OR聚合

```
CompletionStage applyToEither(other, fn);
CompletionStage applyToEitherAsync(other, fn);
CompletionStage acceptEither(other, consumer);
CompletionStage acceptEitherAsync(other, consumer);
CompletionStage runAfterEither(other, action);
CompletionStage runAfterEitherAsync(other, action);
```

```
CompletableFuture<String> f1 = 
  CompletableFuture.supplyAsync(()->{
    int t = getRandom(5, 10);
    sleep(t, TimeUnit.SECONDS);
    return String.valueOf(t);
});

CompletableFuture<String> f2 = 
  CompletableFuture.supplyAsync(()->{
    int t = getRandom(5, 10);
    sleep(t, TimeUnit.SECONDS);
    return String.valueOf(t);
});

CompletableFuture<String> f3 = 
  f1.applyToEither(f2,s -> s);

System.out.println(f3.join());
```

##### 异常处理

```
CompletionStage exceptionally(fn);
CompletionStage<R> whenComplete(consumer);
CompletionStage<R> whenCompleteAsync(consumer);
CompletionStage<R> handle(fn);
CompletionStage<R> handleAsync(fn);
```

```
CompletableFuture<Integer> 
  f0 = CompletableFuture
    .supplyAsync(()->7/0))
    .thenApply(r->r*10)
    .exceptionally(e->0);
System.out.println(f0.join());
```

### CompletionService

批量执行异步任务。

```
ExecutorCompletionService(Executor executor)；
ExecutorCompletionService(Executor executor, BlockingQueue<Future<V>> completionQueue)。
```

> 任务执行结果的Future对象就是加入到completionQueue中。

```
// 创建线程池
ExecutorService executor = 
  Executors.newFixedThreadPool(3);
// 创建CompletionService
CompletionService<Integer> cs = new 
  ExecutorCompletionService<>(executor);
// 异步向电商S1询价
cs.submit(()->getPriceByS1());
// 异步向电商S2询价
cs.submit(()->getPriceByS2());
// 异步向电商S3询价
cs.submit(()->getPriceByS3());
// 将询价结果异步保存到数据库
for (int i=0; i<3; i++) {
  Integer r = cs.take().get();
  executor.execute(()->save(r));
}
```

### 分治任务模型

+ 任务分解:将任务迭代地分解为子任务，直至子任务可以直接计算出结果；
+ 结果合并:逐层合并子任务的执行结果，直至获得最终结果。

![](images/fork-join.png)

#### 案例一`斐波那契数列`

```
static void main(String[] args){
  //创建分治任务线程池  
  ForkJoinPool fjp = 
    new ForkJoinPool(4);
  //创建分治任务
  Fibonacci fib = 
    new Fibonacci(30);   
  //启动分治任务  
  Integer result = 
    fjp.invoke(fib);
  //输出结果  
  System.out.println(result);
}
//递归任务
static class Fibonacci extends 
    RecursiveTask<Integer>{
  final int n;
  Fibonacci(int n){this.n = n;}
  protected Integer compute(){
    if (n <= 1)
      return n;
    Fibonacci f1 = 
      new Fibonacci(n - 1);
    //创建子任务  
    f1.fork();
    Fibonacci f2 = 
      new Fibonacci(n - 2);
    //等待子任务结果，并合并结果  
    return f2.compute() + f1.join();
  }
}
```

#### 案例二`统计单词数量`

```
static void main(String[] args){
  String[] fc = {"hello world",
          "hello me",
          "hello fork",
          "hello join",
          "fork join in world"};
  //创建ForkJoin线程池    
  ForkJoinPool fjp = 
      new ForkJoinPool(3);
  //创建任务    
  MR mr = new MR(
      fc, 0, fc.length);  
  //启动任务    
  Map<String, Long> result = 
      fjp.invoke(mr);
  //输出结果    
  result.forEach((k, v)->
    System.out.println(k+":"+v));
}
//MR模拟类
static class MR extends 
  RecursiveTask<Map<String, Long>> {
  private String[] fc;
  private int start, end;
  //构造函数
  MR(String[] fc, int fr, int to){
    this.fc = fc;
    this.start = fr;
    this.end = to;
  }
  @Override protected 
  Map<String, Long> compute(){
    if (end - start == 1) {
      return calc(fc[start]);
    } else {
      int mid = (start+end)/2;
      MR mr1 = new MR(
          fc, start, mid);
      mr1.fork();
      MR mr2 = new MR(
          fc, mid, end);
      //计算子任务，并返回合并的结果    
      return merge(mr2.compute(),
          mr1.join());
    }
  }
  //合并结果
  private Map<String, Long> merge(
      Map<String, Long> r1, 
      Map<String, Long> r2) {
    Map<String, Long> result = 
        new HashMap<>();
    result.putAll(r1);
    //合并结果
    r2.forEach((k, v) -> {
      Long c = result.get(k);
      if (c != null)
        result.put(k, c+v);
      else 
        result.put(k, v);
    });
    return result;
  }
  //统计单词数量
  private Map<String, Long> 
      calc(String line) {
    Map<String, Long> result =
        new HashMap<>();
    //分割单词    
    String [] words = 
        line.split("\\s+");
    //统计单词数量    
    for (String w : words) {
      Long v = result.get(w);
      if (v != null) 
        result.put(w, v+1);
      else
        result.put(w, 1L);
    }
    return result;
  }
}
```