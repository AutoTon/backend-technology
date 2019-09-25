# tomcat

## 修改超时时间

编辑`conf/web.xml`文件:

```
<session-config>
	<session-timeout>120</session-timeout>
</session-config>
```

## 修改日志输出

编辑`bin/catalina.sh`文件：

```
if [ -z "$CATALINA_OUT" ] ; then
  CATALINA_OUT=/dev/null #"$CATALINA_BASE"/logs/catalina.out
fi
```

## 修改访问日志格式

编辑`conf/server.xml`文件的Host标签：

```
%h %l %u %t &quot;%r&quot; %s %b %S %D
%a   这是记录访问者的IP，在日志里是127.0.0.1
%A   这是记录本地服务器的IP，在日志里是192.168.254.108
%b   发送信息的字节数，不包括http头，如果字节数为0的话，显示为-
%B   发送信息的字节数，不包括http头。
%h   服务器的名称。如果resolveHosts为false的话，这里就是IP地址了，例如我的日志里是10.217.14.16
%H   访问者的协议，这里是HTTP/1.0
%l   官方解释：Remote logical username from identd (可能这样翻译：记录浏览者进行身份验证时提供的名字)(always returns '-')
%m   访问的方式，是GET还是POST
%p   本地接收访问的端口 
%q   比如你访问的是aaa.jsp?bbb=ccc，那么这里就显示?bbb=ccc，就是querystring的意思
%r   First line of the request (method and request URI) 请求的方法和URL
%s   http的响应状态码 
%S   用户的session ID,这个session ID大家可以另外查一下详细的解释，反正每次都会生成不同的session ID
%t   请求时间
%u   得到了验证的访问者，否则就是"-"
%U   访问的URL地址，我这里是/rightmainima/leftbott4.swf
%v   服务器名称，可能就是你url里面写的那个吧，我这里是localhost
%D   Time taken to process the request,in millis，请求消耗的时间，以毫秒记
%T   Time taken to process the request,in seconds，请求消耗的时间，以秒记
```