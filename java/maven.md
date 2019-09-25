# maven

## 发布jar包至私服

```
mvn deploy:deploy-file -DgroupId=commons-cli -DartifactId=commons-cli -Dversion=1.0 -Dpackaging=jar -Dfile=commons-cli-1.0.jar -DpomFile=commons-cli-1.0.pom -Durl=http://10.142.232.119:8081/artifactory/ctg-private-releases/ -DrepositoryId=ctg-private-releases
```

## 一键修改父子模块的pom版本

```
mvn versions:set -DnewVersion='3.0.0-SNAPSHOT'
```

## 接入sonar执行代码检查

```
mvn sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=8803414aa7857c759e460d38acc19735a60eeb24
```