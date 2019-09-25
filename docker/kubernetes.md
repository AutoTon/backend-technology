# kubernetes

## namespace

### 查看所有namespace

```
kubectl get namespaces
```

## node

### 查看所有node

```
kubectl get nodes
```

## pod

### 查看指定pod的日志

```
kubectl log <pod-name>
```

### 查看pod

```
kubectl get pods -o wide | grep 10.142.233.72
```

### 从宿主机登录到具体pod内部

```
kubectl exec -it <pod-name> bash
```