//package com.ecommerce.user;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.autoconfigure.domain.EntityScan;
//import org.springframework.boot.builder.SpringApplicationBuilder;
//import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
//import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//
////@SpringBootApplication(scanBasePackages = {
////    "com.ecommerce.user",
////    "com.ecommerce.common"
////})
//@EntityScan(basePackages = {
//    "com.ecommerce.user.domain"
//})
//@EnableJpaRepositories(basePackages = {
//    "com.ecommerce.user.repository"
//})
//@EnableJpaAuditing
//public class UserServiceApplication extends SpringBootServletInitializer {
//
//    @Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//        return application.sources(UserServiceApplication.class);
//    }
//
//    public static void main(String[] args) {
//        SpringApplication.run(UserServiceApplication.class, args);
//    }
//}