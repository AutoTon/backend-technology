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

### 4.6 追加文件内容

```
echo 'MAVEN_HOME='$maven_dir'/apache-maven-3.3.9
export PATH=$PATH:$MAVEN_HOME/bin' | sudo tee -a /etc/profile
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

## 8 SHELL

### 判断语法

+ [ -a FILE ] 如果 FILE 存在则为真。
+ [ -b FILE ] 如果 FILE 存在且是一个块特殊文件则为真。
+ [ -c FILE ] 如果 FILE 存在且是一个字特殊文件则为真。
+ [ -d FILE ] 如果 FILE 存在且是一个目录则为真。
+ [ -e FILE ] 如果 FILE 存在则为真。
+ [ -f FILE ] 如果 FILE 存在且是一个普通文件则为真。
+ [ -g FILE ] 如果 FILE 存在且已经设置了SGID则为真。
+ [ -h FILE ] 如果 FILE 存在且是一个符号连接则为真。
+ [ -k FILE ] 如果 FILE 存在且已经设置了粘制位则为真。
+ [ -p FILE ] 如果 FILE 存在且是一个名字管道(F如果O)则为真。
+ [ -r FILE ] 如果 FILE 存在且是可读的则为真。
+ [ -s FILE ] 如果 FILE 存在且大小不为0则为真。
+ [ -t FD ] 如果文件描述符 FD 打开且指向一个终端则为真。
+ [ -u FILE ] 如果 FILE 存在且设置了SUID (set user ID)则为真。
+ [ -w FILE ] 如果 FILE 如果 FILE 存在且是可写的则为真。
+ [ -x FILE ] 如果 FILE 存在且是可执行的则为真。
+ [ -O FILE ] 如果 FILE 存在且属有效用户ID则为真。
+ [ -G FILE ] 如果 FILE 存在且属有效用户组则为真。
+ [ -L FILE ] 如果 FILE 存在且是一个符号连接则为真。
+ [ -N FILE ] 如果 FILE 存在 and has been mod如果ied since it was last read则为真。
+ [ -S FILE ] 如果 FILE 存在且是一个套接字则为真。
+ [ FILE1 -nt FILE2 ] 如果 FILE1 has bee changed more recently than FILE2, or 如果 FILE1 exists and FILE2 does not则为真。
+ [ FILE1 -ot FILE2 ] 如果 FILE1 比 FILE2 要老, 或者 FILE2 存在且 FILE1 不存在则为真。
+ [ FILE1 -ef FILE2 ] 如果 FILE1 和 FILE2 指向相同的设备和节点号则为真。
+ [ -o OPTIONNAME ] 如果 shell选项 “OPTIONNAME” 开启则为真。
+ [ -z STRING ] “STRING” 的长度为零则为真。
+ [ -n STRING ] or [ STRING ] “STRING” 的长度为非零 non-zero则为真。
+ [ STRING1 == STRING2 ] 如果2个字符串相同。 “=” may be used instead of “==” for strict POSIX compliance则为真。
+ [ STRING1 != STRING2 ] 如果字符串不相等则为真。

### 技巧

（1）避免默认情况下，遇到不存在的变量，会忽略并继续执行。

```
set -o nounset
```

（2）遇到命令执行返回码为非0时则退出脚本的执行，采用`||`、`&&`可将前面命令的返回码生吞。

```
set -e
```

（3）避免默认情况下，遇到执行出错，会跳过并继续执行。

```
set -o errexit
```

（4）修饰常量

```
readonly
```

（5）修饰函数内变量

```
local
```

（6）代替`（反单引号），能支持内嵌，不用转义。

```
$()
```

（7）代替[]，功能更强大，可以使用&&、||、<、==、=~（正则）等

```
[[]]
```

（8）检查脚本的语法

```
bash -n <shell-script>
```

（9）追踪脚本里的每个命令的执行

```
bash -v/-x <shell-script>
```

## 9 软件安装

### 9.1 node.js

官网下载安装包（非源码）

```
tar xvJf node-v8.9.4-linux-x64.tar.xz
vim /etc/profile
```

增加2行配置：

```
export NODE_HOME=/{your dir}/node-v8.9.4-linux-x64
export PATH=$NODE_HOME/bin:$PATH
source /etc/profile
```

验证

```
node -v
npm -v
```

## 10 修改open-files

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