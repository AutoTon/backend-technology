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