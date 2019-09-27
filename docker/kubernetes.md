# kubernetes

## 架构

![](assets/kubernetes-framework.png)

### 节点

#### Master

##### Api Server

+ 提供API服务。
+ 整个集群的持久化数据，则由`kube-apiserver`处理后保存在`etcd`.

##### Scheduler

负责容器的调度。

##### Controller Manager

负责容器编排。

#### Node

##### kubelet

负责与容器运行时交互--CRI

## 常用命令

### namespace

#### 查看所有namespace

```
kubectl get namespaces
```

### node

#### 查看所有node

```
kubectl get nodes
```

### pod

#### 查看指定pod的日志

```
kubectl log <pod-name>
```

#### 查看pod

```
kubectl get pods -o wide | grep 10.142.233.72
```

#### 从宿主机登录到具体pod内部

```
kubectl exec -it <pod-name> bash
```