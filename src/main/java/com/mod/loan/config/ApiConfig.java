package com.mod.loan.config;

import com.mod.loan.util.OkHttpReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {

    @Bean
    public OkHttpReader okHttpReader() {
        return new OkHttpReader();
    }

}
