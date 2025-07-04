package com.guilhermezuriel.reduceme.application.config.infra;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class InfraProperties {

    @Value("${reduceme.api_url_base}")
    private String apiUrlBase;

    @Getter
    private static String STATIC_API_URL_BASE;

    @PostConstruct
    private void init() {
        STATIC_API_URL_BASE = this.apiUrlBase;
    }
}
