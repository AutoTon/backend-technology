# mysql原理篇

## 通讯

+ 客户端与服务端是半双工的通信方式，即同一时刻只能由客户端发送消息给服务端，或者服务端发送消息给客户端。
+ 客户端的消息传送有大小限制，参数max_allowed_packet
+ 一旦服务端向客户端返回消息，可能会拆分成多个数据包，客户端就必须要等待完整接收完毕。因此有时加上limit限时提取的数据是有必要的。

## 查询语句的执行

![](images/query-sql-workflow.png)

## 查询缓存

不建议开启。可通过下面命令查询是否开启

```
show global VARIABLES like 'query_cache_type';
```

## 日志模块

### redo log（InnoDB引擎特有的日志）

![](images/redo-log-workflow.png)

循环写，空间固定会用完。

+ write pos：记录当前写的位置
+ check point：记录当前擦除的位置

> `write pos`和`checkpoint`之间可以用来记录新的操作。如果`write pos`追上`checkpoint`，这时候不能再执行新的更新，得停下来先擦掉一些记录，把`checkpoint`推进一下。

#### 参数

```
show global VARIABLES like 'innodb_flush_log_at_trx_commit';
```

`innodb_flush_log_at_trx_commit`设置为1表示每次事务的`redo log`都直接持久化到磁盘。建议设置为1。

### binlog（Server层日志）

追加写，记录的是语句的原始逻辑。通过`事务ID`可以与`redo log`对应。

#### 参数

```
show global VARIABLES like 'sync_binlog';
```

`sync_binlog`设置为1表示每次事务的`binlog`都持久化到磁盘。建议设置为1。

## 更新语句的执行

两阶段提交，避免任意时刻crash造成的binlog与redo log的数据不一致。

![](images/update-sql-workflow.png)

## 事务隔离

+ 读未提交（read uncommitted）：一个事务还没提交时，它做的变更就能被别的事务看到。
+ 读提交（read committed）：一个事务提交之后，它做的变更才会被其他事务看到。
+ 可重复读（repeatable read）：一个事务执行过程中看到的数据，总是跟这个事务在启动时看到的数据是一致的。当然在可重复读隔离级别下，未提交变更对其他事务也是不可见的。
+ 串行化（serializable）：顾名思义是对于同一行记录，“写”会加“写锁”，“读”会加“读锁”。当出现读写锁冲突的时候，后访问的事务必须等前一个事务执行完成，才能继续执行。

查看当前的事务隔离级别：

```
show variables like 'transaction_isolation';
```

查询当前超过60秒的长事务：

```
select * from information_schema.innodb_trx where TIME_TO_SEC(timediff(now(),trx_started))>60
```

## 索引

主键长度越小，普通索引的叶子节点就越小，普通索引占用的空间也就越小。

下面的语句可以重建索引。应用于删除记录不会删除对应的索引。

```
alter table T engine=InnoDB;
```

### 主键索引（聚簇索引）

叶子节点存的是整行数据。

### 非主键索引（二级索引）

叶子节点内容是主键的值。基于非主键索引的查询需要多扫描一棵索引树。

### 唯一索引与普通索引的选择

`唯一索引`用不上`change buffer`的优化机制，因此从性能角度建议优先考虑`非唯一索引`。

若写入的数据不在内存，对于唯一索引需要把数据页从磁盘读入内存；对于普通索引只需要将数据写入`change buffer`，减少了随机磁盘访问。

如果所有的更新后面，都马上伴随着对这个记录的查询，那么应该关闭`change buffer`。

### 如何给字符串建立索引

使用前缀索引，定义好长度，就可以做到既节省空间，又不用额外增加太多的查询成本。

```
select 
  count(distinct email) as L, 
  count(distinct left(email,4)）as L4,
  count(distinct left(email,5)）as L5,
  count(distinct left(email,6)）as L6,
  count(distinct left(email,7)）as L7,
from SUser;
```

预先设定一个可以接受的损失比例，比如5%。然后，在返回的L4~L7中，找出不小于L*95%的值，假设这里L6、L7都满足，你就可以选择前缀长度为6。

#### 考虑前缀容易重复的场景

##### 倒序存储

```
select field_list from t where id_card = reverse('input_id_card_string');
```

##### 额外添加hash字段

```
alter table t add id_card_crc int unsigned, add index(id_card_crc);
```

## 锁

### 全局锁

加全局读锁：

```
Flush tables with read lock;
```

适用于不支持事务的引擎的`做全库逻辑备份`。

对于事务引擎，可使用`mysqldump`带上参数`–single-transaction`。原理是启动时会开启一个事务，类似RR，拿到一个一致性视图。

### 表级锁

#### 表锁

```
lock tables T read/write;
```

#### 元数据锁（MDL）

读写锁之间互斥，写锁之间互斥，由系统默认在增删查改的时候自动加上。

##### 如何安全地给小表加字段？

+ 可以查到当前执行中的事务。如果你要做DDL变更的表刚好有长事务在执行，要考虑先暂停DDL，或者kill掉这个长事务。
+ 若变更的表是一个热点表，请求很频繁，在`alter table`语句里面设定等待时间。若超时再通过重试命令重复这个过程。

MariaDB、AliDB支持下面命令:

```
ALTER TABLE tbl_name NOWAIT add column ...
ALTER TABLE tbl_name WAIT N add column ... 
```

### 行锁

#### 两阶段锁协议

在InnoDB事务中，行锁是在需要的时候才加上的，但并不是不需要了就立刻释放，而是要等到事务结束时才释放。

#### 死锁

![](images/dead-lock-demo.png)

+ innodb_lock_wait_timeout：锁定超时时间，默认为50s
+ innodb_deadlock_detect：开启死锁检测，若发现死锁，主动回滚其中一个事务。死锁检测要耗费大量的CPU资源。

### 与事务隔离的结合

一个数据版本，对于一个`事务视图`来说，

+ 自己的更新总是可见；
+ 版本未提交，不可见；
+ 版本已提交，但是是在视图创建后提交的，不可见；
+ 版本已提交，而且是在视图创建前提交的，可见。

> 事务更新数据的时候，只能用当前读。如果当前的记录的行锁被其他事务占用的话，就需要进入锁等待。

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

# 命令行

## 输入带有特殊符号的密码

密码用`单引号`括起来，如

```
mysql -uroot -p'root123!@#'
```

## 导入外部脚本

```
mysql -u$mysql_user -p'$mysql_password' < $work_dir/$gogs_sql_script
```

## 执行带有特殊符号的字符串变量密码

```
mysql_password=ctgae123!@#
mysql_script="mysql -u$mysql_user -p'$mysql_password' < $work_dir/$gogs_sql_script "
eval $mysql_script
```

## 用navicat连接数据库缓慢

原因：MySQL数据库收到一个网络连接后，首先拿到对方的IP地址，然后对这个IP地址进行反向DNS解析从而得到这个IP地址对应的主机名。用主机名在权限系统里面进行权限判断。反向DNS解析是耗费时间的，有可能让用户感觉起来很慢。甚至有的时候，反向解析出来的主机名并没有指向这个IP地址，这时候就无法连接成功了。

如果由于DNS反查导致登陆很慢，那么在MySQL服务器上使用`show processlist`会看到类似如下连接：

```
123 |592|unauthenticated user|192.168.3.20:35320|NULL|Connect| |login|NULL| 
|593|unauthenticated user|192.168.3.20:35321|NULL|Connect| |login|NULL| 
|594|unauthenticated user|192.168.3.20:35322|NULL|Connect| |login|NULL|
```

修改`/etc/my.cnf`文件，追加下面内容:

```
[mysqld] 
skip_name_resolve
```

重启mysql服务

```
sudo service mysqld restart
```

# mysql安装篇

如果是CentOs7.x的系统，如果直接点击rpm包安装会得到错误提示。因为CentOS的默认数据库已经不再是MySQL了，而是MariaDB。

查看当前安装的mariadb包

```
rpm -qa | grep mariadb
```

将它们统统强制性卸载掉

```
rpm -e --nodeps mariadb-libs-5.5.35-3.el7.x86_64
rpm -e --nodeps mariadb-5.5.35-3.el7.x86_64
rpm -e --nodeps mariadb-server-5.5.35-3.el7.x86_64
```

安装

```
cd /usr/local/mysql
rpm -ivh /usr/local/mysql/MySQL-server-5.6.23-1.el6.x86_64.rpm 
rpm -ivh /usr/local/mysql/MySQL-client-5.6.23-1.el6.x86_64.rpm 
rpm -ivh /usr/local/mysql/MySQL-devel-5.6.23-1.el6.x86_64.rpm
```

设置配置文件

```
cp /usr/share/mysql/my-default.cnf /etc/my.cnf
```

修改为

```
[client]
password = 123456
port = 3306
default_character_set=utf8 
[mysqld]
port = 3306
character_set_server=utf8
character_set_client=utf8
collation-server=utf8_general_ci
#(注意linux下mysql安装完后是默认：表名区分大小写，列名不区分大小写； 0：区分大小写，1：不区分大小写)
lower_case_table_names=1
#(设置最大连接数，默认为 151，MySQL服务器允许的最大连接数16384; )
max_connections=1000
[mysql]
default_character_set=utf8
```

启动mysql服务

```
service mysql start
```

注：安装MySQL后登陆MySQL时如果遇到错误

```
ERROR 1045 (28000): Access denied for user 'root'@'localhost' (using password: YES)
```

其实mysql的安装日志信息中，会看到Mysql生成了root用户的随机密码，并放在了`/root/.mysql_secret`文件中

```
A RANDOM PASSWORD HAS BEEN SET FOR THE MySQL root USER !
You will find that password in '/root/.mysql_secret'.
```

找到这个文件的密码，使用该密码登录root账号，并修改密码

```
set password=password('yourpasswd');
```