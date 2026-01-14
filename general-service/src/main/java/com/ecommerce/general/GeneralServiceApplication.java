package com.ecommerce.general;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.ecommerce") // 범위를 최상위로 확장
@EntityScan(basePackages = {
        "com.ecommerce.user.domain",
        "com.ecommerce.product.domain",
        "com.ecommerce.order.domain"
})
@EnableJpaRepositories(basePackages = {
        "com.ecommerce.user.repository",
        "com.ecommerce.product.repository",
        "com.ecommerce.order.repository"
})
@EnableJpaAuditing
public class GeneralServiceApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(GeneralServiceApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(GeneralServiceApplication.class, args);
    }
}