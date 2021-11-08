package com.technology.apollo;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * apollo配置项样例
 */
@Configuration
public class SegmentOnlinePropertiesConfig {

    @RefreshScope
    @Bean
    public SegmentOnlineChangeStepProperties segmentOnlineChangeStepProperties() {
        return new SegmentOnlineChangeStepProperties();
    }

}
