package com.miguel.gamescollection;

import com.miguel.gamescollection.config.DemoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DemoProperties.class)
public class GamesCollectionApplication {

    public static void main(String[] args) {

        SpringApplication.run(GamesCollectionApplication.class, args);
    }

}
