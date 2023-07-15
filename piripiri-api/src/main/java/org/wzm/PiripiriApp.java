package org.wzm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// Press ⇧ twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
@SpringBootApplication
@MapperScan("org.wzm.mapper")
@EnableAsync
@EnableScheduling
@EnableElasticsearchRepositories(basePackages = "org.wzm.repository")
public class PiripiriApp {
    public static void main(String[] args) {
        ApplicationContext app = SpringApplication.run(PiripiriApp.class, args);
//        WebSocketService.setApplicationContext(app);
    }
}