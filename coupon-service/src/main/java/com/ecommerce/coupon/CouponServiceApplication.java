package com.ecommerce.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = {"com.ecommerce.coupon", "com.ecommerce.common"})
public class CouponServiceApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CouponServiceApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(CouponServiceApplication.class, args);
    }
}