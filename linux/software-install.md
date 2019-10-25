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