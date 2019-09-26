# 设计模式

## 单例模式

### 饿汉式

### 懒汉式

### 双重检测（线程安全）

### 静态内部类持有对象的引用（线程安全）

## 简单工厂模式

比如根据字段生成不同的异步任务

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