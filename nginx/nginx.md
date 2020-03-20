# nginx

## 代理

### 正向代理

正向代理类似一个跳板机，通过正向代理访问客户端无法访问到的服务端网络，比如翻墙。客户端需配置正向代理IP及端口。

![](assets/forward-proxy.png)

### 反向代理

以代理服务器来接受internet上的连接请求，然后将请求转发给内部网络上的服务器，并将从服务器上得到的结果返回给internet上请求连接的客户端，此时代理服务器对外就表现为一个服务器。客户端不需任何配置。

![](assets/reverse-proxy.png)

## 跨域

当两个域具有相同的协议(如http), 相同的端口(如80)，相同的host（如www.google.com)，则认为它们是相同的域（协议，域名，端口都必须相同）。

### 作用

限制跨域资源访问的作用可从服务器和客户端两个方面进行分析： 

+ 对于服务器：当收到一个请求时，会检查该请求来源，如果来源的客户端页面自己无法识别，而且服务器的数据又是比较敏感的，则可能做出限制或者拒绝访问（例如，黑客对服务器的攻击）。 
+ 对于客户端：浏览器的同源策略可限制对跨域资源的访问，若其与服务器的域不相同，则浏览器可能进行限制甚至拒绝访问（例如，黑客通过让你访问他的服务器数据来攻击你的客户端页面）。

跨域访问失败时，实际上浏览器发送请求成功，浏览器也接收到了响应，但是它会限制xmlhttprequest接受响应并在js控制台报错。

###解决

`服务端`通过在`响应`中增加`Access-Control-Allow-Origin`标识的 `header`，指定服务器端允许进行跨域资源访问的来源域 

> 注：`Access-Control-Allow-Origin: *`，表示该资源谁都可以用。

## expires缓存

### 优点

可以降低网站带宽，节约成本，同时提升用户访问体验，是web服务非常重要的功能。

### 缺点

被缓存的页面或数据更新了，用户看到的可能还是旧的内容，反而影响用户体验。

> 解决：缩短缓存时间。改名缓存文件。

### 应用

（1）根据文件扩展名进行判断，添加expires功能

```
location ~ .*\.gif$ {
    expires 3650d;
}
```

（2）根据目录进行判断，添加expires功能

```
location ~ ^/(images|static)/ {
    expires 365d;
}
```

## gzip压缩功能

对于大于1k的纯文本文件图片、视频等不要压缩。因为不但不会减少，在压缩时消耗CPU内存资源。

（1）开启

```
gzip on;
```

（2）设置允许压缩的页面最小字节数

```
gzip_min_length 1k;
```

（3）压缩缓冲区大小

```
gzip_buffers 4 16k;
```

（4）压缩比率

设置为1时压缩比最小，处理速度最快；设置为9时压缩比最大，传输速度快，但处理最慢，也比较消耗CPU资源。

```
gzip_comp_level 2;
```

## 错误页面优雅展示

（1）403跳转

```
server {
listen 80;
server_name www.example.com;
location / {
    root html/www;
    index index.html;
}
error_page 403 /403.html; #此路径相对于root
}
```

（2）404错误本地文件优雅展示

```
error_page 404 /404.html;
```

（3）50x页面本地文件优雅展示

```
error_page 500 502 503 504 /50x.html;
location = /50x.html {
root /data/www/html;
}
```

（4）改变状态码为新的状态码，并显示指定的文件内容

```
error_page 404 =200 /empty.gif;
```

（5）错误状态码url重定向

```
error_page 403 http://example.com/forbidden.html;
error_page 404 =301 http://example.com/notfound.html;
```