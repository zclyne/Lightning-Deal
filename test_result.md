# 测试环境

线程数：300

Ramp-up time: 5

循环次数: 50

访问路径：/resources/list-item.html (nginx), list-item.html (tomcat)

# 测试结果

### 完全默认配置：
TPS: 204.93

### Tomcat调优后：

TPS: 212.56

Average Response Time: 830.66

### 配置nginx动静分离后（nginx与tomcat没有配置长连接）：

TPS: 200.52

Averate Response Time: 858.84

### nginx与tomcat配置长连接后：

TPS: 189.98

Average Response Time: 882.23