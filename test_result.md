# 商品详情测试
### 测试环境
测试路径：/item/get?id=4。在配置nginx shared dictionary后，使用/luaitem/get?id=4

线程数: 2000

Ramp-up time: 5

循环次数: 50

测试服务器：局域网内Mac Mini 2014，4GB内存，128GB固态硬盘，搭载系统为CentOS 7
# 测试结果

### 完全默认配置：
TPS: 1592.84

Average Response Time: 1169.88
### Tomcat调优后：
TPS: 2672.30

Average Response Time: 625.68
### 配置nginx动静分离后（nginx与tomcat没有配置长连接）：
TPS: 2048.05

Averate Response Time: 530.18 
### nginx与tomcat配置长连接后：
TPS: 2334.38

Average Response Time: 482.84
### 配置redis集中式缓存后：
TPS: 2474.76

Average Response Time: 473.72
### 配置Guava cache本地数据热点缓存后：
TPS: 3935.77

Average Response Time: 274.61
### 配置Nginx Proxy Cache后（不采用）：
TPS: 807.10

Average Response Time: 839.27 

由于Nginx Proxy Cache是将内容缓存在硬盘的文件系统上而不是内存中，因此实际性能反而会有比较严重的下降，所以不采用该策略
### 配置Nginx Shared Dictionary后：
TPS: 1189.17

Average Response Time: 374.81

数据存疑，压测结果不稳定，因此不采用该方式，而是直接配置Nginx直连Redis
### 配置Nginx直接连接Redis后：
TPS: 3772.59

Average Response Time: 290.53
# 下单功能测试
### 测试环境
测试路径：/order/createorder?token=b072430ffae340ebb4f61fbfd3e6a457

请求参数：method=POST, itemId=4, amount=1

线程数: 2000

Ramp-up time: 5

循环次数: 50

测试服务器：局域网内Mac Mini 2014，4GB内存，128GB固态硬盘，搭载系统为CentOS 7
### 默认配置：
TPS: 651.72

Average Response Time: 1976.15
### 使用Redis缓存进行交易验证优化后：
TPS: 
 
Average Response Time: 
### 