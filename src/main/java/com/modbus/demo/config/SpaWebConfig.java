package com.modbus.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/tcp/server").setViewName("forward:/index.html");
        registry.addViewController("/tcp/client").setViewName("forward:/index.html");
        registry.addViewController("/rtu/server").setViewName("forward:/index.html");
        registry.addViewController("/rtu/client").setViewName("forward:/index.html");
        registry.addViewController("/poll").setViewName("forward:/index.html");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }
}
