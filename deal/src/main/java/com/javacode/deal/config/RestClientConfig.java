package com.javacode.deal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${calculator.service.base-url}")
    private String badeURL;

    @Bean
    public RestClient calculatorrestClient(RestClient.Builder builder) {
        return builder.baseUrl(badeURL).build();
    }
}
