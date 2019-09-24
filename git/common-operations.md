# git 常用操作

## 1. 拉取代码

```
git clone <your-repository-url>
```

## 2. 分支操作

### 2.1 创建分支

```
git branch <new-branch-name>
```

### 2.2 通过远程分支创建对应的本地分支

```
git checkout -b <new-branch-name> <origin/target-branch-name>
```

### 2.3 设置本地分支追踪远程分支

```
git branch -u <origin/target-branch-name> local-branch-name
```

### 2.4 切换分支

```
git checkout <target-branch-name>
```

### 2.5 创建分支并切换到新分支

```
git checkout -b <new-branch-name>
```

### 2.6 合并目标分支到当前分支

```
git merge <target-branch-name>
```

或者

```
git checkout <target-branch-name>; git rebase <source-branch-name>
```

### 2.7 强制更新本地分支内容到远程分支

```
git push -u origin <local-branch-name> -f
```

### 2.8 更新分支

#### 2.8.1 第一种方式

```
git pull
```

等价于

```
git fetch; git merge <origin/target-branch-name>
```

#### 2.8.2 第二种方式

```
git pull --rebase
```

等价于

```
git fetch; git rebase <origin/target-branch-name>
```

## 3. tag操作

### 3.1 对当前分支打tag

```
git tag <new-tag-name>
```

### 3.2 指定commit打tag

```
git tag <new-tag-name> <commit-id>
```