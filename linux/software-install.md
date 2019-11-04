# zookeeper

解压

```
tar -zxvf zookeeper-3.x.x.tar.gz
```

新建配置文件

```
vi conf/zoo.cfg
```

输入以下内容

```
# The number of milliseconds of each tick
tickTime=2000
# The number of ticks that the initial 
# synchronization phase can take
initLimit=100
# The number of ticks that can pass between 
# sending a request and getting an acknowledgement
syncLimit=5
# the directory where the snapshot is stored.
# do not use /tmp for storage, /tmp here is just 
# example sakes.
dataDir=/app/service-stage-test/zookeeper-3.4.9/data
# the port at which the clients will connect
clientPort=8181
# the maximum number of client connections.
# increase this if you need to handle more clients
#maxClientCnxns=60
#
# Be sure to read the maintenance section of the 
# administrator guide before turning on autopurge.
#
# http://zookeeper.apache.org/doc/current/zookeeperAdmin.html#sc_maintenance
#
# The number of snapshots to retain in dataDir
#autopurge.snapRetainCount=3
# Purge task interval in hours
# Set to "0" to disable auto purge feature
#autopurge.purgeInterval=1
```

启动

```
bin/zkServer.sh start
```

设置JVM参数

```
vi conf/java.env
```

输入以下内容

```
export JVMFLAGS="-Xms1024m -Xmx1024m $JVMFLAGS" 
```

访问zk

```
bin/zkCli.sh -server "127.0.0.1:2181"
```

# nexus

## 安装

```
unzip nexus-2.14.3-02-bundle.zip
```

## 启动

```
cd nexus-2.14.3-02/bin/
./nexus start &
```

> 注意：默认的Nexus端口是`8081`，如果需要修改该端口，请修改文件`conf/nexus.properties`

```
application-port=8081
```

# gogs

## 安装

```
tar -zxvf gogs_v0.11.4.0405_linux_amd64.tar.gz
```

## 创建数据库

```
DROP DATABASE IF EXISTS gogs;
CREATE DATABASE IF NOT EXISTS gogs CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

## 启动

```
nohup ./gogs web -p 8989 &
```

# jdk

## 安装

解压文件

```
tar –zxvf jdk-8u45-linux-x64.tar.gz
```

配置环境变量

```
vi /etc/profile
```

在文件末尾加入下面内容

```
JAVA_HOME=/usr/local/java/jdk1.8.0_45
PATH=$JAVA_HOME/bin:$PATH
CLASSPATH=$JAVA_HOME/jre/lib/ext:$JAVA_HOME/lib/tools.jar
export PATH JAVA_HOME CLASSPATHs
```

执行下面命令以生效

```
source /etc/profile
```

验证是否成功

```
java -version
```

# nginx

## 安装gcc编译环境

判断系统是否已安装gcc

```
rpm -qa|grep gcc
```

没有则需要安装

```
yum install gcc
yum install gcc-c++
```

安装好之后，再次执行下面命令验证

```
rpm -qa|grep gcc
```

## 安装pcre

```
yum install pcre-devel
```

## 安装zlib

```
yum install zlib-devel
```

## 安装nginx（单机版）

解压文件

```
tar -zxvf nginx-1.x.x.tar.gz
cd nginx-1.x.x
```

编译安装

```
./configure  --with-http_stub_status_module  --prefix=/usr/local/nginx
make
make install
```

启动验证

```
cd /usr/local/nginx/sbin
./nginx
```

查看nginx版本，以及configure参数

```
./nginx -V
```

平滑重启nginx

```
./nginx -s reload
```

## 安装nginx（高可用版）


