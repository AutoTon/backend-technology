# docker

## 查询docker容器对应的进程ID

```
docker inspect -f '{{.State.Pid}}' <containerid>
```

查询连接数还可继续执行下面的命令：

```
nsenter -t <pid> -n netstat | grep ESTABLISHED
```