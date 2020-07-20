package com.yifan.lightning.deal;

import com.yifan.lightning.deal.dao.UserDOMapper;
import com.yifan.lightning.deal.dataobject.UserDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 */
@SpringBootApplication(scanBasePackages = {"com.yifan.lightning.deal"})
@RestController
@MapperScan("com.yifan.lightning.deal.dao")
// @EnableScheduling注解告诉Spring要扫描由@Scheduled标注的定时任务
@EnableScheduling
// 启用Spring Cache，此处采用redis作为缓存的实现
@EnableCaching
public class App {

    public static void main( String[] args )
    {
        SpringApplication.run(App.class, args);
    }
}
