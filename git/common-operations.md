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

### 2.9 将其他分支的指定commit复制到当前分支下

```
git cherry-pick <commit1> <commit2> ...
```

### 2.10 撤销变更

#### 2.10.1 只影响本地分支

```
git reset HEAD~1
```

> 注：上面的命令用到了相对引用

#### 2.10.2 可以push更新远程分支

```
git revert HEAD
```

## 2.11 让指定分支指向某一次提交

```
git branch -f <target-branch-name> <commit-id>
```

## 2.12 退出合并

```
git merge --abort
```

## 2.13 将指定分支替换为另一个分支内容

```
# 切换到目标分支
git checkout <target-branch-name> 
# 将本地的目标分支重置成源分支
git reset --hard <source-branch-name> 
# 再推送到远程仓库
git push origin <target-branch-name> --force 
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

### 3.3 删除tag

```
git push origin --delete tag <tag-name>
```

## 4. 相对引用

+ `^`: 向上移动一个commit
+ `~<num>`: 向上移动num个commit

## 5. 免密设置

### 5.1 记住密码

```
git config credential.helper "store --file ~/.my-credentials"
```

### 5.2 设置过期时间

```
git config credential.helper 'cache --timeout=3600'
```

### 5.3 删除免密设置

```
git credential-manager delete
```