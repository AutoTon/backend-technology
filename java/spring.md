# spring

## bean生命周期

生命周期执行的过程如下:

+ （1）spring对bean进行实例化,默认bean是单例
+ （2）spring对bean进行依赖注入
+ （3）如果bean实现了BeanNameAware接口,spring将bean的id传给setBeanName()方法
+ （4）如果bean实现了BeanFactoryAware接口,spring将调用setBeanFactory方法,将BeanFactory实例传进来
+ （5）如果bean实现了ApplicationContextAware()接口,spring将调用setApplicationContext()方法将应用上下文的引用传入
+ （6）如果bean实现了BeanPostProcessor接口,spring将调用它们的postProcessBeforeInitialization接口方法
+ （7）如果bean实现了InitializingBean接口,spring将调用它们的afterPropertiesSet接口方法,类似的如果bean使用了init-method属性声明了初始化方法,该方法也会被调用
+ （8）如果bean实现了BeanPostProcessor接口,spring将调用它们的postProcessAfterInitialization接口方法
+ （9）此时bean已经准备就绪,可以被应用程序使用了,他们将一直驻留在应用上下文中,直到该应用上下文被销毁
+ （10）若bean实现了DisposableBean接口,spring将调用它的distroy()接口方法。同样的,如果bean使用了destroy-method属性声明了销毁方法,则该方法被调用

### 单例管理的对象

+ （1）默认情况下,spring在读取xml文件的时候,就会创建对象。
+ （2）在创建的对象的时候(先调用构造器),会去调用init-method=".."属性值中所指定的方法.
+ （3）对象在被销毁的时候,会调用destroy-method="..."属性值中所指定的方法.(例如调用container.destroy()方法的时候)
+ （4）lazy-init="true",可以让这个对象在第一次被访问的时候创建

### 非单例管理的对象

+ （1）spring读取xml文件的时候,不会创建对象
+ （2）在每一次访问这个对象的时候,spring容器都会创建这个对象,并且调用init-method=".."属性值中所指定的方法
+ （3）对象销毁的时候,spring容器不会帮我们调用任何方法,因为是非单例,这个类型的对象有很多个,spring容器一旦把这个对象交给你之后,就不再管理这个对象了

## 事务传播级别

+ PROPAGATION_REQUIRED（默认）：假如当前正要运行的事务不在另外一个事务里，那么就起一个新的事务
+ PROPAGATION_SUPPORTS：假设当前在事务中。即以事务的形式执行。假设当前不再一个事务中，那么就以非事务的形式执行
+ PROPAGATION_MANDATORY：必须在一个事务中执行。也就是说，他仅仅能被一个父事务调用。否则，他就要抛出异常
+ PROPAGATION_REQUIRES_NEW：即使存在父事务调用，也会另起一个事务。父事务不会引起当前事务。
+ PROPAGATION_NOT_SUPPORTED：不支持事务。
+ PROPAGATION_NEVER：不能在事务中执行，若存在父事务，则会抛出异常。
+ PROPAGATION_NESTED：与PROPAGATION_REQUIRES_NEW区别在于子事务跟父事务是一起提交的。



