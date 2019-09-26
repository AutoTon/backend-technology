# mysql原理篇

## 通讯

+ 客户端与服务端是半双工的通信方式，即同一时刻只能由客户端发送消息给服务端，或者服务端发送消息给客户端。
+ 客户端的消息传送有大小限制，参数max_allowed_packet
+ 一旦服务端向客户端返回消息，可能会拆分成多个数据包，客户端就必须要等待完整接收完毕。因此有时加上limit限时提取的数据是有必要的。

# mysql开发篇

## 数据类型

+ 更小的通常更好。占用更小的磁盘空间。
+ 避免NULL。尽量设计为NOT NULL。

> 注：对于日期，可允许NULL，避免"0000-00-00 00:00:00"

## 索引

### 底层

`B+树`实现，最左前缀匹配（索引多个列）

### 哈希索引

应用：比如url一列一般比较长，那么可以增加一列，对url做CRC编码，索引使用CRC编码后的数据列。只适用于等值索引，不适合范围索引。

### 选择性计算公式

```
SELECT COUNT(DISTINCT your_column)/COUNT(*) FROM your_table;
```

## 主键

最好选择顺序的主键策略，尽量避免采用像UUID这种随机的字符串作为主键，影响性能！

## 创建数据库指定编码和排序规则

```
CREATE DATABASE `test2` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

## 分页的优化

### 延迟关联

```
SELECT film.id, film.description FROM film INNER JOIN (SELECT id FROM film ORDER BY title LIMIT 10000, 10) AS f USING(id);
```

### 范围查询

```
SELECT * FROM film WHERE id < 10000 ORDER BY title DESC LIMIT 50;
```

# mysql运维篇

## 慢查询

### 开启慢查询

```
set global slow_query_log='ON';
```

### 查询数据库慢查询日志

```
show VARIABLES LIKE '%slow%';
sudo cat /var/lib/mysql/bss1-slow.log
```

### 分析慢日志

```
/app/mysql/bin/mysqldumpslow  /app/mysql/dbdata/data/slow.log
```

## 事务

### 查询数据库提交事务的锁

```
select * from information_schema.innodb_trx
kill {thread_id}
```

### 查询并设置事务级别

```
show variables like '%isolation%';
SET GLOBAL tx_isolation='READ-COMMITTED';
```

## 用户管理

### 创建用户并授权

```
CREATE USER 'username'@'host' IDENTIFIED BY 'password';
GRANT ALL ON *.* TO 'pig'@'%';
flush privileges
```

## 设置传输包大小

```
show variables like '%max_allowed_packet%';
set global max_allowed_packet = 2*1024*1024*10;
set global slave_max_allowed_packet = 20*1024*1024;
```