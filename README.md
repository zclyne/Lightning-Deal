# 秒杀电商平台

这是一个秒杀电商网站平台，提供用户注册、用户登录、商品下单、秒杀活动添加等功能

## 开发工具

IntelliJ IDEA

## 压力测试工具

Apache Jmeter

## 技术

前端：Bootstrap + jQuery

后端：Spring Boot + MyBatis + MySQL

中间件：Druid数据库连接池 + Redis缓存

服务器端：Nginx + Tomcat集群

## 并发优化方向

1. 采用Tomcat集群，配置Nginx反向代理与负载均衡，使流量被均匀分摊给Tomcat服务器
2. Nginx配置动静分离，进一步减小Tomcat服务器压力
3. 使用Redis对商品详情页做缓存，降低MySQL数据库访问次数
4. 配置本地热点缓存 + Nginx代理缓存

## 当前优化结果

[当前优化结果](./test_result.md)

## 使用方法

1. 将本项目clone到本地，确保本地已经安装好MySQL数据库以及Redis

2. 执行项目根目录下的lightning_deal.sql，创建项目数据库，其中包含一些测试数据

   ```shell
   mysql -uroot -proot < ./lightning_deal.sql
   ```

3. 使用IntelliJ IDEA打开该项目，并执行App.java中的main方法

4. Tomcat已经启动在8081端口