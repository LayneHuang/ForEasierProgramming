# Git Tips

### 1.gitconfig
本地目录下 C:\Users\账号\.gitconfig 可以配置 github 的提交信息

### 2.误操作 git reset 强制回滚后恢复 commit
git reflog 命令可以查看所有 HEAD 的历史   
回滚reset：
```
git reset --hard 98abc5a
```