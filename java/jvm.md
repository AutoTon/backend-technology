# jvm

## 每隔1秒实时打印jvm信息

```
jstat -gcutil -h 20 <pid> 1000
```

## 获取jvm当前存活对象信息

```
jmap -histo:live <pid>
```

## dump

```
jmap -dump:format=b,file=<dump-file> <pid>
```