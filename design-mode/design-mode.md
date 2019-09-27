# 设计模式

## 单例模式

### 饿汉式

### 懒汉式

### 双重检测（线程安全）

### 静态内部类持有对象的引用（线程安全）

## 简单工厂模式

比如根据字段生成不同的异步任务

### 类图

![](assets/simple-factory.png)

### 特点

工厂与产品是`一对多`的关系。

### 缺点

+ 工厂类集中了所有产品的创建逻辑；
+ 系统扩展困难，若新增产品，需要修改工厂类代码逻辑。

### 示例

```
abstract class Product {
    //所有产品类的公共业务方法
    public void methodSame() {
    //公共方法的实现
    }
    //声明抽象业务方法
    public abstract void methodDiff();
}
```

```
class ConcreteProduct extends Product {
	//实现业务方法
	public void methodDiff() {
	//业务方法的实现
	}
}
```

```
class Factory {
	//静态工厂方法
	public static Product getProduct(String arg) {
		Product product = null;
		if (arg.equalsIgnoreCase("A")) {
			product = new ConcreteProductA();
			//初始化设置product
		}
		else if (arg.equalsIgnoreCase("B")) {
			product = new ConcreteProductB();
			//初始化设置product
		}
		return product;
	}
}
```

```
class Client {
	public static void main(String args[]) {
		Product product;
		product = Factory.getProduct("A"); //通过工厂类创建产品对象
		product.methodSame();
		product.methodDiff();
	}
}
```

## 模板方法模式

比如异步任务，定义了任务的执行、异常、成功等操作。

## 策略模式

定义了一系列策略，通常为接口的不同实现，业务场景自行决定采用哪种策略（实现）。

## 适配器模式

Java JWT、Java IO、Spring Web MVC

### 特点

适配接口与被适配接口没有层次关系。如InputStream与Reader。

## 装饰器模式

JavaIO、Spring Web MVC

### 特点

装饰者与被装饰者有层次关系。如InputStream、FileInputStream。

## 享元模式

Integer缓存、String的intern()、ThreadLocal

### 特点

共享常用的对象，减少对象的创建

## 责任链模式

Filter、Netty的handler

## 拦截器模式

AOP