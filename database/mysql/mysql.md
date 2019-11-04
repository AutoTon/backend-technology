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