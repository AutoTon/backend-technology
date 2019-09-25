# linux 常用命令

## 1. 磁盘相关

### 1.1 查看子目录磁盘使用情况

```
du -h --max-depth=1 <dir>
```
### 1.2 查看各个挂载盘的使用情况

```
df -hl
```

## 2. 内存相关

### 2.1 查看内存使用情况

```
free -h
```

## 3. 网络相关

### 3.1 统计连接数

```
netstat -n | awk '/^tcp/ {++S[$NF]} END {for(a in S) print a, S[a]}'
```

### 3.2 查看指定端口是否被占用

```
lsof -i:<port>
```
> 注意： 该命令只能查看当前用户下的进程是否占用了指定端口，若要查询该端口是否真正占用，需要加上sudo

若提示命令未找到，执行下面命令安装`lsof`

```
sudo yum install lsof
```

### 3.3 设置socks5代理

```
ssh -N -f -D *:<local-port> <remote-user>@<remote-host> > ./sshproxy_vm_test.log 2>&1
```

### 3.4 查询指定进程的连接数

```
nsenter -t <pid> -n netstat | grep ESTABLISHED
```

## 4 文件相关

### 4.1 传输与拷贝

#### 4.1.1 scp

（1）拷贝本地文件至远程主机

```
scp <local-file> <remote-user>@<remote-host>:<remote-path>
```

（2）拷贝远程主机文件至本地

```
scp <remote-user>@<remote-host>:<remote-path> <local-path>
```

### 4.2 递归查找执行目录下含有指定字符串的文件

```
grep -rn "ui green button" <dir>
```

### 4.3 查找指定目录下拥有者不是root的文件

```
find <dir> -type f -uid +1000
```

### 4.4 获取指定行的日志

```
sed -n "190502, 190512p" <log-file> 
```

### 4.5 清理

#### 4.5.1 清理大文件

```
cat /dev/null > <big-file>
```

#### 4.5.2 检查进程是否存在未释放的文件描述符

```
lsof | grep delete
```

## 5 压缩与解压

### 5.1 tar

（1）解压

```
tar -zxvf xxx.tar.gz
```

（2）压缩

```
tar -czf zookeeper-3.4.7.tar.gz zookeeper-3.4.7
```

### 5.2 zip

（1）解压

```
unzip ROOT.war -d ROOT
```

（2）压缩

```
zip -r nexus-2.14.3-02-bundle.zip nexus-2.14.3-02
```

## 6 http请求

### 6.1 curl

#### 6.1.1 模拟DELETE请求

```
curl -v -X DELETE 192.168.33.1:8080/girls/3
```

#### 6.1.2 设置域名请求头访问

```
curl -v -H"host: gatewaymonitor.eop.ctg.ctgae.com:8011" http://10.142.232.175:8011
```

## 7 系统设置

### 7.1 调整时区

```
sudo timedatectl set-timezone 'Asia/Shanghai'
```

### 7.2 查看内核日志

```
cat -n /var/log/messages
```

### 7.3 查看linux系统版本

```
lsb_release -a
```

## 8 修改open-files

（1）查看`file-max`

```
cat /proc/sys/fs/file-max
```

（2）调整`file-max`

```
echo 6555360 > /proc/sys/fs/file-max
echo 6555360 >> /etc/sysctl.conf
sysctl -p
```

（3）在`/etc/security/limits.conf`最后增加两行配置

```
*                -       nofile         102400
*                -       nproc          102400
```

`su`到`root`用户，重启`ssh`服务

（4）修改`/etc/security/limits.d/90-nproc.conf`，默认值`1024`改为`16384`。					
```
*          soft    nproc     16384
root       soft    nproc     unlimited
```

（5）不用重启即可生效

```
echo "ulimit -HSn 102400" >> /etc/profile
source /etc/profile
```

（6）检查`/etc/ssh/sshd_config`配置文件

需要关注`UsePAM`，在有`/etc/pam.d/sshd`的文件时，此参数`UsePAM`的值必须开启，并设置为`yes`
然后重启`sshd`服务

（7）验证

```
ulimit -HSn
cat /proc/sys/fs/file-max
```