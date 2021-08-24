# 1 系统介绍
本系统为一个带加密功能的简易 API 转发服务，能够解决在数据传输过程中存在的信息泄露风险，提高数据传输的安全性。

## 1.1 系统组成

该系统包含以下两个部分：

- 接收端：负责接收客户主机的API调用请求，并将请求结果返回给客户主机。客户主机与接收端之间的数据交换是明文的，因此需确保接收端部署到可信的服务器上。
- 转发端：负责转发请求，并将请求结果返回给接收端。转发端只接收**可信接收端**发来的转发请求，其它转发请求则会被拒绝。

> 注：在以下说明中，接收端和转发端统称为”节点 (Node)“。

### 1.2 系统示意图

**接收端与转发端是多对多的关系，即一个接收端可以将请求转发给多个转发端，一个转发端可以接收多个接收端的转发请求。**

![API 转发服务系统示意图](https://cdn.jsdelivr.net/gh/Orainge/API-Service/pic/1.png)

## 1.3 常见部署架构

通常情况下，该系统可以加密请求的数据，使数据能在公网环境中安全穿过。

![常见部署架构](https://cdn.jsdelivr.net/gh/Orainge/API-Service/pic/2.png)

# 2 技术说明

## 2.1 系统项目介绍

该系统由Maven进行管理，包含 3 个 model：

- api-service-forwarder：转发端
- api-service-receiver：接收端
- api-service-utils：项目工具包

其中，转发端和接收端依赖 ```api-service-utils```，因此如果运行项目时提示找不到相应类时，需要手动安装依赖到本地 Maven 仓库。

```sh
cd /path/to/project # 进入项目目录
cd api-service-utils # 进入工具包目录

# 以下安装方式二选一
# 安装到本地 Maven 仓库（同时安装源码）
mvn source:jar install 
# 安装到本地 Maven 仓库（不安装源码）
mvn install
```

## 2.2 使用场景

- 客户端发送请求到服务端时，请求数据包和响应数据包需要进行加密，防止别人截取时被解密。
- 不能修改客户端，需要无感知替换服务端。
- 该系统可自由配置，在变更配置时仅需修改配置文件，无需修改代码。

## 2.3 项目优势

- 使用反向代理：对于客户计算机发起的请求，转发端会将该请求原封不动地发送给转发端，让转发端进行请求，并将请求结果返回给接收端，这样就保证客户端在请求时感知不到请求端和转发端的存在，实现无缝替换。
- 配置统一管理：所有转发的配置只在转发端进行配置，接收端只需要填写对应转发端的ID和预共享密钥即可接收转发请求，方便对转发端进行二次开发（例如API管理系统、集群管理等）。
- 节点数据交换基于 HTTP 请求：节点之间的数据交换基于HTTP请求，意味着无需为了配置该系统专门的系统进程管理，只需在服务器上启动节点后，附加在 Nginx 上即可使用，方便配置。

## 2.4 可靠性

该系统的数据交换可靠性由两部分组成：

- 节点需授权连接：转发端接收到接收端的转发请求时，会验证请求头中的授权信息是否正确。在本系统中，使用了**HOTP(HMAC-based One-Time Password，表示基于HMAC算法加密的一次性密码)**的验证方式，使用**预共享密钥+动态时间**的方式生成动态验证码，该验证码只在请求产生后的10秒钟内有效，即使有人截获请求包，也无法伪造接收端的请求让转发端转发，有效预防了重放攻击。

  ![节点之间连接需要授权](https://cdn.jsdelivr.net/gh/Orainge/API-Service/pic/3.png)

- 节点请求内容进行加密传输：节点之间传递信息时，会使用对应转发端的**预共享密钥**进行加密，确保只有对应的接收端才能解密密文，防止数据被截获后内容被破译。

  ![接收端密钥配置](https://cdn.jsdelivr.net/gh/Orainge/API-Service/pic/4.png)

  ![转发端密钥配置](https://cdn.jsdelivr.net/gh/Orainge/API-Service/pic/5.png)

  ![节点之间交换的数据是密文](https://cdn.jsdelivr.net/gh/Orainge/API-Service/pic/6.png)

# 3 系统运行

## 3.1 转发端

转发端打包完成后会生成 .jar 包，该 .jar 包可直接运行。

```sh
java -jar api-service-forwarder-1.0.jar
```

## 3.2 接收端

接收端打包完成后会生成 .jar 包，该 .jar 包可直接运行。

```sh
java -jar api-service-receiver-1.0.jar
```

## 3.3 反向代理配置

如有需要，可以为转发端或接收端配置反向代理。

以下以 Nginx 配置为例：

```nginx
location /apiService {
  proxy_pass  http://127.0.0.1:9090; # 转发端或接收端的 URL
  proxy_redirect off;
  proxy_set_header           Host $host:$server_port;
  proxy_set_header  X-Real-IP  $remote_addr;
  proxy_set_header           X-Forwarded-For $proxy_add_x_forwarded_for;
  index  index.html index.htm;
  add_header X-Frame-Options "SAMEORIGIN";
}
```

