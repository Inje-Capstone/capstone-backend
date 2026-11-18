package com.injecapstone.capstonebackend.ai.config;

import com.injecapstone.capstonebackend.ai.client.AiClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);

    @Bean
    public AiClient aiClient(AiProperties properties) {
        if (!properties.enabled()) {
            return AiClient.disabled();
        }

        String baseUrl = properties.baseUrl().replaceAll("/+$", "");

        return new AiClient(
                baseUrl,
                restClient(baseUrl, properties.timeout()),
                restClient(baseUrl, properties.proxyTimeout())
        );
    }

    private RestClient restClient(String baseUrl, Duration readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
}
