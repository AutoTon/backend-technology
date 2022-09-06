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

### 1.3 根据进程id查询所在路径

```
ll /proc/<pid>/exe
```

### 1.4 根据进程ID查询执行文件目录

```
sudo ls -lr /proc/{pid} | grep cwd
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

### 4.7 多个压缩文件查找字符串

```
zgrep '/serverops/servermove/batchAlloRes' *.log.gz | grep ERROR
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

### 7.4 发送公钥

```
sshpass -p <password> ssh-copy-id -p <port> -i ~/.ssh/id_rsa.pub -o StrictHostKeyChecking=no <user>@<ip>
```

其中，`sshpass`可传递密码以避免交互式输入，`StrictHostKeyChecking=no`自动应答ssh连接的`yes`。

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

### urlEncode

```
urlencode() {

  local LANG=C
  local length="${#1}"
  i=0
  while :
  do
    [ $length -gt $i ]&&{
      local c="${1:$i:1}"
      case $c in
      [a-zA-Z0-9.~_-]) printf "$c" ;;
      *) printf '%%%02X' "'$c" ;; 
      esac
    }||break
    let i++
  done
}
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

## 11 CPU相关

### 查看系统CPU核数

```
# 关于grep和wc的用法请查询它们的手册或者网络搜索
$ grep 'model name' /proc/cpuinfo | wc -l
2
```

### 负载相关

#### 查看负载

```
$ uptime
02:34:03 up 2 days, 20:14,  1 user,  load average: 0.63, 0.83, 0.88
```

依次为：当前时间、系统运行时间、正在登陆的用户数，过去1分钟、5分钟、15分钟的平均负载。

#### 平均负载

单位时间内，系统处于可运行状态和不可中断状态的平均进程数，也就是平均活跃进程数。

+ 可运行状态的进程：指正在使用CPU或者正在等待CPU的进程，也就是我们常用ps命令看到的，处于R状态（Running 或 Runnable）的进程。
+ 不可中断状态的进程：正处于内核态关键流程中的进程，并且这些流程是不可打断的，比如最常见的是等待硬件设备的I/O响应，也就是我们在ps命令中看到的D状态（Uninterruptible Sleep，也称为 Disk Sleep）的进程。

理想状态下，平均负载等于CPU核数。

#### 查看CPU负载

```
mpstat -P ALL 5
```

#### 查看进程负载

```
# 间隔5秒后输出一组数据
$ pidstat -u 5 1
```

### 平均负载过高时，如何调优

#### CPU密集型进程

```
mpstat -P ALL 5
# -P ALL表示监控所有CPU，5表示每5秒刷新一次数据，观察是否有某个cpu的%usr会很高，但iowait应很低
pidstat -u 5 1
# 每5秒输出一组数据，观察哪个进程%cpu很高，但是%wait很低，极有可能就是这个进程导致cpu飚高
```

#### IO密集型进程

```
mpstat -P ALL 5
# 观察是否有某个cpu的%iowait很高，同时%usr也较高
pidstat -u 5 1
观察哪个进程%wait较高，同时%CPU也较高
```

#### 大量进程

```
pidstat -u 5 1
# 观察那些%wait较高的进程是否有很多
```

## 查看系统的上下文切换情况

```
# 每隔5秒输出1组数据
$ vmstat 5
procs -----------memory---------- ---swap-- -----io---- -system-- ------cpu-----
 r  b   swpd   free   buff  cache   si   so    bi    bo   in   cs us sy id wa st
 0  0      0 7005360  91564 818900    0    0     0     0   25   33  0  0 100  0  0
```

+ cs（context switch）：每秒上下文切换的次数。
+ in（interrupt）：每秒中断的次数。
+ r（Running or Runnable）：就绪队列的长度，也就是正在运行和等待CPU的进程数。
+ b（Blocked）：处于不可中断睡眠状态的进程数。

### 查看进程的上下文切换情况

```
# 每隔5秒输出1组数据
$ pidstat -w 5
Linux 4.15.0 (ubuntu)  09/23/18  _x86_64_  (2 CPU)

08:18:26      UID       PID   cswch/s nvcswch/s  Command
08:18:31        0         1      0.20      0.00  systemd
08:18:31        0         8      5.40      0.00  rcu_sched
...
```

+  cswch：每秒自愿上下文切换（voluntary context switches）的次数。是指进程无法获取所需资源，导致的上下文切换。比如说， I/O、内存等系统资源不足时，就会发生自愿上下文切换。
+  nvcswch：每秒非自愿上下文切换（non voluntary context switches）的次数。是指进程由于时间片已到等原因，被系统强制调度，进而发生的上下文切换。比如说，大量进程都在争抢CPU时，就容易发生非自愿上下文切换。

### 观察中断的变化情况

```
# -d 参数表示高亮显示变化的区域
$ watch -d cat /proc/interrupts
           CPU0       CPU1
...
RES:    2450431    5279697   Rescheduling interrupts
...
```

### 查看系统CPU统计信息

```
# 只保留各个CPU的数据
$ cat /proc/stat | grep ^cpu
cpu  280580 7407 286084 172900810 83602 0 583 0 0 0
cpu0 144745 4181 176701 86423902 52076 0 301 0 0 0
cpu1 135834 3226 109383 86476907 31525 0 282 0 0 0
```

> 注意：这里列的数值指的是开机以来统计的节拍数。

+ user（us）：代表用户态CPU时间。注意，它不包括下面的nice时间，但包括了guest时间。
+ nice（ni）：代表低优先级用户态CPU时间，也就是进程的nice值被调整为1-19之间时的CPU时间。这里注意，nice可取值范围是-20到19，数值越大，优先级反而越低。
+ system（sys）：代表内核态CPU时间。
+ idle（id）：代表空闲时间。注意，它不包括等待I/O的时间（iowait）。
+ iowait（wa）：代表等待I/O的CPU时间。
+ irq（hi）：代表处理硬中断的CPU时间。
+ softirq（si）：代表处理软中断的CPU时间。
+ steal（st）：代表当系统运行在虚拟机中的时候，被其他虚拟机占用的CPU时间。
+ guest（guest）：代表通过虚拟化运行其他操作系统的时间，也就是运行虚拟机的CPU时间。
+ guest_nice（gnice）：代表以低优先级运行虚拟机的时间。

### CPU使用率

除了空闲时间外的其他时间占总CPU时间的百分比。

![](images/cpu-usage.png)

![](images/average-cpu-usage.png)

> top工具实际上是使用了3秒的间隔进行统计。ps使用的却是进程的整个生命周期。

#### 查看CPU使用率

##### top

```
# 默认每3秒刷新一次
$ top
top - 11:58:59 up 9 days, 22:47,  1 user,  load average: 0.03, 0.02, 0.00
Tasks: 123 total,   1 running,  72 sleeping,   0 stopped,   0 zombie
%Cpu(s):  0.3 us,  0.3 sy,  0.0 ni, 99.3 id,  0.0 wa,  0.0 hi,  0.0 si,  0.0 st
KiB Mem :  8169348 total,  5606884 free,   334640 used,  2227824 buff/cache
KiB Swap:        0 total,        0 free,        0 used.  7497908 avail Mem

  PID USER      PR  NI    VIRT    RES    SHR S  %CPU %MEM     TIME+ COMMAND
    1 root      20   0   78088   9288   6696 S   0.0  0.1   0:16.83 systemd
    2 root      20   0       0      0      0 S   0.0  0.0   0:00.05 kthreadd
    4 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 kworker/0:0H
...
```

> 默认显示的是所有CPU的平均值。

##### pidstat

```
# 每隔1秒输出一组数据，共输出5组
$ pidstat 1 5
15:56:02      UID       PID    %usr %system  %guest   %wait    %CPU   CPU  Command
15:56:03        0     15006    0.00    0.99    0.00    0.00    0.99     1  dockerd

...

Average:      UID       PID    %usr %system  %guest   %wait    %CPU   CPU  Command
Average:        0     15006    0.00    0.99    0.00    0.00    0.99     -  dockerd
```

#### 查找CPU使用率过高的进程的函数

```
# -g开启调用关系分析，-p指定php-fpm的进程号21515
$ perf top -g -p 21515
```

### 系统CPU使用率高，却找不到占用CPU高的进程？

```
$ top
...
%Cpu(s): 80.8 us, 15.1 sy,  0.0 ni,  2.8 id,  0.0 wa,  0.0 hi,  1.3 si,  0.0 st
...

  PID USER      PR  NI    VIRT    RES    SHR S  %CPU %MEM     TIME+ COMMAND
 6882 root      20   0    8456   5052   3884 S   2.7  0.1   0:04.78 docker-containe
 6947 systemd+  20   0   33104   3716   2340 S   2.7  0.0   0:04.92 nginx
 7494 daemon    20   0  336696  15012   7332 S   2.0  0.2   0:03.55 php-fpm
 7495 daemon    20   0  336696  15160   7480 S   2.0  0.2   0:03.55 php-fpm
10547 daemon    20   0  336696  16200   8520 S   2.0  0.2   0:03.13 php-fpm
10155 daemon    20   0  336696  16200   8520 S   1.7  0.2   0:03.12 php-fpm
10552 daemon    20   0  336696  16200   8520 S   1.7  0.2   0:03.12 php-fpm
15006 root      20   0 1168608  66264  37536 S   1.0  0.8   9:39.51 dockerd
 4323 root      20   0       0      0      0 I   0.3  0.0   0:00.87 kworker/u4:1
...
```

+ 应用里直接调用了其他二进制程序，这些程序通常运行时间比较短，通过top等工具也不容易发现。
+ 应用本身在不停地崩溃重启，而启动过程的资源初始化，很可能会占用相当多的CPU。
