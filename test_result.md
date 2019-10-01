# 测试环境
线程数: 2000

Ramp-up time: 5

循环次数: 50

测试服务器：局域网内Mac Mini 2014，4GB内存，128GB固态硬盘，搭载系统为CentOS 7

访问路径：/item/get?id=4
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
TPS: 

Average Response Time: 