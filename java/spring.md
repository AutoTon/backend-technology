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

#### 循环依赖

默认开启允许循环依赖，也可通过下面的方式关闭：

```
BeanFactory.setAllowCircularReferences(false);
```

底层有3个缓存：

+ singletonObjects：一级缓存，存放已创建好的spring bean
+ singletonFactories：二级缓存，产生bean（可提前执行aop返回代理bean）。不直接用二级缓存拿的原因是性能考虑，二级缓存产生对象的消耗比较大。
+ earlySingletonObjects：三级缓存，存放创建好的单例对象，注意还不是spring bean，从二级缓存中取出，取出时同时会移除二级缓存的引用。

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

## 动态代理

### JDK动态代理

+ 动态创建一个代理类，实现java.lang.reflect.InvocationHandler。
+ 代理类持有被代理的对象，在invoke方法中对被代理方法的前后加入额外的逻辑处理。

```
public class DynaProxyHello implements InvocationHandler{

    private Object target;//目标对象
    /**
     * 通过反射来实例化目标对象
     * @param object
     * @return
     */
    public Object bind(Object object){
        this.target = object;
        return Proxy.newProxyInstance(this.target.getClass().getClassLoader(), this.target.getClass().getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        Object result = null;
        Logger.start();//添加额外的方法
        //通过反射机制来运行目标对象的方法
        result = method.invoke(this.target, args);
        Logger.end();
        return result;
    }

}
```

### cglib动态代理

+ 动态创建一个代理类，为目标对象的子类；
+ 设置回调函数，在真实方法前后加上额外的逻辑。

```
public class CGLIBProxy {
    //写法1
    private Object targetObject;
    private Object createProxy(Object obj){
        targetObject = obj;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetObject.getClass());//设置代理对象，父类，说明是继承，所有代理对象不能为final
        enhancer.setCallback(new MyHandler());
        return enhancer.create();//创建代理
    }
    class MyHandler implements MethodInterceptor{
        @Override
        public Object intercept(Object arg0, Method method, Object[] args,
                MethodProxy arg3) throws Throwable {
            System.out.println("开启事务..");  
            Object returnValue = method.invoke(targetObject, args);  
            System.out.println("提交事务....");  
            return returnValue;  
        }
    }
    @Test
    public  void test1() {  
        CGLIBProxy cglibProxy = new CGLIBProxy();  
        Customer customer = (Customer)cglibProxy.createProxy(new Customer());  
        customer.eat();  
    }  
    //写法2
    @Test
    public void test2(){
        Customer customer = new Customer();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(customer.getClass());
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object proxy, Method method, Object[] args,
                    MethodProxy arg3) throws Throwable {
                if(method.getName().equals("eat")){  
                    System.out.println("customer的eat方法被拦截了。。。。");  
                    Object invoke = method.invoke(proxy, args);  
                    System.out.println("真实方法拦截之后。。。。");  
                    return invoke;  
                }  
                // 不拦截 
                return method.invoke(proxy, args);  
            }
        });
        Customer cus = (Customer) enhancer.create();
        cus.eat();
    }
}
```
