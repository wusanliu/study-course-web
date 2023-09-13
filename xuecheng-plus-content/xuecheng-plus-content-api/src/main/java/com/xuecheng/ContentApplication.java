package com.xuecheng;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Xue
 * @create 2023-09-03-11:12
 */
@EnableSwagger2Doc
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.xuecheng.content.feginclient"})
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class,args);
    }
}
