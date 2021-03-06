# 缓存

## 效率

内存 <= ehcache < redis <= memcached < mysql

## 类型

### vanish

可作为反向代理，缓存前端资源。

### ehcache

#### 优点

存储在当前机器内存中，存取非常快。

#### 缺点

内存有限，每台机器内存中各存一份，失效时间不一致，一般用来缓存不常变化且个数较少的数据。

### memcache

kv分布式缓存集群，可以缓存个数较多的数据，可承接高流量访问，读取缓存时远程连接，一般耗时小。

### redis

内存的kv存储，可以保存list、hash、set、sorted set等数据结构。

## 击穿

### 场景

查询必然不存在的数据，请求透过缓存，直击数据库

### 防范

#### 简单过滤

+ 参数校验：长度、类型。。。
+ 缓存临时空数据：若查询数据不存在，也把查询结果空放到redis中

#### 先行查询

查询之前先判断这个商品是否存在，把所有的商品ID加载到JVM内存/其他存储。将请求拦截在缓存和数据库之前。（需要维护）

##### 缺点

内存消耗大：需要的存储空间越来越大，检索速度也越来越慢。

##### 优点

开发简单；使用方便，满足很多场景；过滤精准。

#### 先行查询的优化

不保存所有的ID信息，只在内存中做一个标记。-- 布隆过滤器 Bloom filter（可使用redis的bitmap）

##### 缺点

有一定的误识别率，删除困难，需要维护。

##### 优点

内存占用少。

# redis

## bitmap

本质是string，自动扩容，最大长度为2^32，底层存储的是值的二进制

### 相关命令

（1）赋值

```
setbit <key> <bit位置> 1/0
```

（2）获取值

```
getbit <key> <bit位置>
```

（3）统计key对应的值bit=1的数量

```
bitcount <key>
```

### 实际应用

#### 朋友圈点赞

key：朋友圈ID
 
（1）点赞

```
setbit <朋友圈ID> <点赞用户ID> 1
```

（2）取消点赞

```
setbit <朋友圈ID> <点赞用户ID> 0
```

（3）统计点赞数量

```
bitcount <朋友圈ID> 
```

（4）判断是否点赞

```
getbit <朋友圈ID> <点赞用户ID>
```

> 缺陷：无法保留点赞顺序

#### 分布式Bloom过滤器

（1）Guava Bloom过滤器的缺点

+ 基于JVM内存，重启即失效
+ 本地内存无法用在分布式场景
+ 不支持大数据量存储

（2）Redis Bloom过滤器

+ 需要网络IO，性能比Guava的低
+ 不支持删除：可通过定时替换Bloom过滤器来解决
